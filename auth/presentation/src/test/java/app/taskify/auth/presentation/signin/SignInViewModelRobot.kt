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
import app.cash.turbine.test
import app.taskify.auth.domain.repository.SignInResult
import app.taskify.auth.domain.usecases.signin.FakeSignInUseCase
import app.taskify.auth.domain.usecases.signin.MockSignInValidationUseCase
import app.taskify.auth.domain.usecases.signin.SignInValidationResult
import app.taskify.core.domain.Text
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class SignInViewModelRobot {

  private lateinit var mockSignInValidationUseCase: MockSignInValidationUseCase
  private lateinit var fakeSignInUseCase: FakeSignInUseCase
  private lateinit var savedStateHandle: SavedStateHandle

  private lateinit var viewModel: SignInViewModel

  fun buildViewModel() = apply {
    mockSignInValidationUseCase = MockSignInValidationUseCase()
    fakeSignInUseCase = FakeSignInUseCase()
    savedStateHandle = SavedStateHandle()
    viewModel = SignInViewModel(
      signInValidationUseCase = mockSignInValidationUseCase.mock,
      signInUseCase = fakeSignInUseCase,
      savedStateHandle = savedStateHandle,
      ioDispatcher = UnconfinedTestDispatcher(),
    )
  }

  /* View Model Events */

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

  fun assertViewState(expectedViewState: SignInViewState) = apply {
    assertThat(viewModel.viewState.value).isEqualTo(expectedViewState)
  }

  suspend fun assertNavigationEvents(
    vararg expectedNavigationEvents: SignInNavigationEvent,
    action: suspend SignInViewModelRobot.() -> Unit,
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
    action: SignInViewModelRobot.() -> Unit,
  ) = apply {
    viewModel.messageFlow.test {
      action()
      for (expectedMessage in expectedMessages) {
        assertThat(awaitItem()).isEqualTo(expectedMessage)
      }
      cancelAndConsumeRemainingEvents().takeIf { it.isNotEmpty() }?.let { events ->
        println("Consuming events are available: $events")
      }
    }
  }

  /* Preparing dependencies */

  fun mockSignInValidationResultForCredentials(
    email: String,
    password: String,
    validationResult: SignInValidationResult,
  ) = apply {
    mockSignInValidationUseCase.mockValidationResultForCredentials(email, password, validationResult)
  }

  suspend fun emitSignInResult(result: SignInResult) = apply {
    fakeSignInUseCase.emit(result)
  }

  /* Call verifications */

  fun verifySignInUseCaseNeverCalled() = apply {
    fakeSignInUseCase.verifyInvokeNeverCalled()
  }

  fun verifySignInVerificationUseCaseNeverCalled() = apply {
    mockSignInValidationUseCase.verifyInvokeNeverCalled()
  }
}
