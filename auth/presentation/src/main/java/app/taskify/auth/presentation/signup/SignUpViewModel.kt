/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package app.taskify.auth.presentation.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.taskify.auth.domain.repository.SignUpResult
import app.taskify.auth.domain.repository.SignUpResult.Authenticated
import app.taskify.auth.domain.repository.SignUpResult.Authenticating
import app.taskify.auth.domain.repository.SignUpResult.Failure.EmailAlreadyInUse
import app.taskify.auth.domain.repository.SignUpResult.Failure.NoNetworkConnection
import app.taskify.auth.domain.repository.SignUpResult.Failure.Unknown
import app.taskify.auth.domain.repository.SignUpResult.SettingUpProfile
import app.taskify.auth.domain.usecases.signup.SignUpUseCase
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase
import app.taskify.auth.presentation.signup.SignUpNavigationEvent.NavigateBackToSignIn
import app.taskify.auth.presentation.signup.SignUpNavigationEvent.NavigateToMain
import app.taskify.core.domain.Text
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

private const val VIEW_STATE = "sign_up_view_state_key"

@HiltViewModel
class SignUpViewModel @Inject constructor(
  private val signUpValidationUseCase: SignUpValidationUseCase,
  private val signUpUseCase: SignUpUseCase,
  private val savedStateHandle: SavedStateHandle,
  private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

  val viewState = savedStateHandle.getStateFlow(VIEW_STATE, SignUpViewState())

  private val navigationChannel = Channel<SignUpNavigationEvent>()
  val navigationFlow = navigationChannel.receiveAsFlow()

  private val messageChannel = Channel<Text>()
  val messageFlow = messageChannel.receiveAsFlow()

  private var signUpJob: Job? = null

  fun onDisplayNameChanged(displayName: CharSequence) {
    savedStateHandle[VIEW_STATE] = viewState.viewStateWithUpdatedDisplayName(displayName)
  }

  fun onEmailChanged(email: CharSequence) {
    savedStateHandle[VIEW_STATE] = viewState.viewStateWithUpdatedEmail(email)
  }

  fun onPasswordChanged(password: CharSequence) {
    savedStateHandle[VIEW_STATE] = viewState.viewStateWithUpdatedPassword(password)
  }

  fun onSignInClicked() {
    navigationChannel.trySend(NavigateBackToSignIn)
  }

  fun onSignUpClicked() {
    val displayName = viewState.value.displayName
    val email = viewState.value.email
    val password = viewState.value.password

    val validationResult = signUpValidationUseCase(displayName, email, password)
    if (!validationResult.areInputsValid) {
      savedStateHandle[VIEW_STATE] = viewState.viewStateWithValidationErrors(validationResult)
      return
    }

    signUpJob?.cancel()
    signUpJob = signUpUseCase(displayName, email, password)
      .flowOn(ioDispatcher)
      .onEach(::handleSignUpResult)
      .launchIn(viewModelScope)
  }

  private fun handleSignUpResult(signUpResult: SignUpResult) {
    when (signUpResult) {
      Authenticating, SettingUpProfile -> {
        savedStateHandle[VIEW_STATE] = viewState.viewStateWithLoading(signUpResult.description)
      }

      Authenticated -> {
        savedStateHandle[VIEW_STATE] = SignUpViewState(loadingText = signUpResult.description)
        navigationChannel.trySend(NavigateToMain)
      }

      EmailAlreadyInUse -> {
        savedStateHandle[VIEW_STATE] = viewState.value.copy(
          emailError = signUpResult.description,
          loadingText = null,
        )
      }

      NoNetworkConnection, is Unknown -> {
        savedStateHandle[VIEW_STATE] = viewState.viewStateWithLoading(loadingText = null)
        messageChannel.trySend(signUpResult.description)
      }
    }
  }
}
