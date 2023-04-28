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

package app.taskify.auth.presentation.signin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.taskify.auth.domain.repository.SignInResult
import app.taskify.auth.domain.repository.SignInResult.Authenticated
import app.taskify.auth.domain.repository.SignInResult.Authenticating
import app.taskify.auth.domain.repository.SignInResult.Failure.InvalidCredentials
import app.taskify.auth.domain.repository.SignInResult.Failure.NoNetworkConnection
import app.taskify.auth.domain.repository.SignInResult.Failure.Unknown
import app.taskify.auth.domain.repository.SignInResult.RetrievingProfile
import app.taskify.auth.domain.usecases.signin.SignInUseCase
import app.taskify.auth.domain.usecases.signin.SignInValidationUseCase
import app.taskify.auth.presentation.signin.SignInNavigationEvent.NavigateToMain
import app.taskify.auth.presentation.signin.SignInNavigationEvent.NavigateToSignUp
import app.taskify.core.domain.Text
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

private const val VIEW_STATE = "sign_in_view_state_key"

@HiltViewModel
class SignInViewModel @Inject constructor(
  private val signInValidationUseCase: SignInValidationUseCase,
  private val signInUseCase: SignInUseCase,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

  val viewState = savedStateHandle.getStateFlow(VIEW_STATE, SignInViewState())

  private val navigationChannel = Channel<SignInNavigationEvent>()
  val navigationFlow = navigationChannel.receiveAsFlow()

  private val messageChannel = Channel<Text>()
  val messageFlow = messageChannel.receiveAsFlow()

  private var signInJob: Job? = null

  fun onEmailChanged(email: CharSequence) {
    savedStateHandle[VIEW_STATE] = viewState.viewStateWithUpdatedEmail(email)
  }

  fun onPasswordChanged(password: CharSequence) {
    savedStateHandle[VIEW_STATE] = viewState.viewStateWithUpdatedPassword(password)
  }

  fun onSignUpClicked() {
    navigationChannel.trySend(NavigateToSignUp)
  }

  fun onSignInClicked() {
    val email = viewState.value.email
    val password = viewState.value.password

    val validationResult = signInValidationUseCase(email, password)
    if (validationResult != null) {
      savedStateHandle[VIEW_STATE] = viewState.viewStateWithValidationErrors(validationResult)
      return
    }

    signInJob?.cancel()
    signInJob = signInUseCase(email, password)
      .flowOn(Dispatchers.IO)
      .onEach(::handleSignInResult)
      .launchIn(viewModelScope)
  }

  private fun handleSignInResult(signInResult: SignInResult) {
    when (signInResult) {
      Authenticating, RetrievingProfile -> {
        savedStateHandle[VIEW_STATE] = viewState.viewStateWithLoading(signInResult.description)
      }

      Authenticated -> {
        savedStateHandle[VIEW_STATE] = SignInViewState()
        navigationChannel.trySend(NavigateToMain)
      }

      NoNetworkConnection, InvalidCredentials, is Unknown -> {
        savedStateHandle[VIEW_STATE] = viewState.viewStateWithLoading(loadingText = null)
        messageChannel.trySend(signInResult.description)
      }
    }
  }
}
