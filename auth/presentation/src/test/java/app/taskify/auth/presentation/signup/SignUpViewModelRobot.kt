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
import app.cash.turbine.test
import app.taskify.auth.domain.repository.SignUpResult
import app.taskify.auth.domain.usecases.signup.FakeSignUpUseCase
import app.taskify.auth.domain.usecases.signup.MockSignUpValidationUseCase
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult
import app.taskify.core.domain.Text
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class SignUpViewModelRobot {

  private lateinit var mockSignUpValidationUseCase: MockSignUpValidationUseCase
  private lateinit var fakeSignUpUseCase: FakeSignUpUseCase
  private lateinit var savedStateHandle: SavedStateHandle

  private lateinit var viewModel: SignUpViewModel

  @OptIn(ExperimentalCoroutinesApi::class)
  fun buildViewModel() = apply {
    mockSignUpValidationUseCase = MockSignUpValidationUseCase()
    fakeSignUpUseCase = FakeSignUpUseCase()
    savedStateHandle = SavedStateHandle()
    viewModel = SignUpViewModel(
      signUpValidationUseCase = mockSignUpValidationUseCase.mock,
      signUpUseCase = fakeSignUpUseCase,
      savedStateHandle = savedStateHandle,
      ioDispatcher = UnconfinedTestDispatcher(),
    )
  }

  /* View Model Events */

  fun enterDisplayName(displayName: CharSequence) = apply {
    viewModel.onDisplayNameChanged(displayName)
  }

  fun enterEmail(email: CharSequence) = apply {
    viewModel.onEmailChanged(email)
  }

  fun enterPassword(password: CharSequence) = apply {
    viewModel.onPasswordChanged(password)
  }

  fun clickSignIn() = apply {
    viewModel.onSignInClicked()
  }

  fun clickSignUp() = apply {
    viewModel.onSignUpClicked()
  }

  /* Assertions */

  fun assertViewState(expectedViewState: SignUpViewState) = apply {
    assertThat(viewModel.viewState.value).isEqualTo(expectedViewState)
  }

  suspend fun assertNavigationEvents(
    vararg expectedNavigationEvents: SignUpNavigationEvent,
    action: SignUpViewModelRobot.() -> Unit,
  ) = apply {
    viewModel.navigationFlow.test {
      action()
      for (expectedNavigationEvent in expectedNavigationEvents) {
        assertThat(awaitItem()).isEqualTo(expectedNavigationEvent)
      }
      cancelAndConsumeRemainingEvents().takeIf { it.isNotEmpty() }?.let { events ->
        println("Consuming events are available: $events")
      }
    }
  }

  suspend fun assertMessages(
    vararg expectedMessages: Text,
    action: suspend SignUpViewModelRobot.() -> Unit,
  ) = apply {
    viewModel.messageFlow.test {
      action()
      for (expectedMessage in expectedMessages) {
        assertThat(awaitItem()).isEqualTo(expectedMessage)
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  /* Preparing dependencies */

  fun mockSignUpValidationResultForCredentials(
    displayName: String,
    email: String,
    password: String,
    validationResult: SignUpValidationResult,
  ) = apply {
    mockSignUpValidationUseCase.mockValidationResultForCredentials(displayName, email, password, validationResult)
  }

  suspend fun emitSignUpResult(result: SignUpResult) = apply {
    fakeSignUpUseCase.emit(result)
  }

  /* Call verifications */

  fun verifySignUpUseCaseNeverCalled() = apply {
    fakeSignUpUseCase.verifyInvokeNeverCalled()
  }

  fun verifySignUpValidationUseCaseNeverCalled() = apply {
    mockSignUpValidationUseCase.verifyUseCaseNeverCalled()
  }
}
