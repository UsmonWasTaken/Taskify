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
import app.taskify.auth.domain.usecases.signin.FakeSignInValidationUseCase
import app.taskify.auth.domain.usecases.signin.SignInValidationResult
import app.taskify.core.domain.Text
import com.google.common.truth.Truth.assertThat

class SignInViewModelRobot {

  private lateinit var fakeSignInValidationUseCase: FakeSignInValidationUseCase
  private lateinit var fakeSignInUseCase: FakeSignInUseCase
  private lateinit var savedStateHandle: SavedStateHandle

  private lateinit var viewModel: SignInViewModel

  fun buildViewModel() = apply {
    fakeSignInValidationUseCase = FakeSignInValidationUseCase()
    fakeSignInUseCase = FakeSignInUseCase()
    savedStateHandle = SavedStateHandle()
    viewModel = SignInViewModel(
      signInValidationUseCase = fakeSignInValidationUseCase.mock,
      signInUseCase = fakeSignInUseCase.mock,
      savedStateHandle = savedStateHandle,
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

  suspend fun assertViewStates(
    vararg expectedViewStates: SignInViewState,
    action: suspend SignInViewModelRobot.() -> Unit,
  ) = apply {
    viewModel.viewState.test {
      action()
      for (expectedViewState in expectedViewStates) {
        assertThat(awaitItem()).isEqualTo(expectedViewState)
      }
      cancelAndIgnoreRemainingEvents()
    }
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
      cancelAndIgnoreRemainingEvents()
    }
  }

  suspend fun assertMessages(
    vararg expectedMessages: Text,
    action: suspend SignInViewModelRobot.() -> Unit,
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

  fun mockSignInValidationResultForCredentials(
    email: String,
    password: String,
    validationResult: SignInValidationResult,
  ) = apply {
    fakeSignInValidationUseCase.mockValidationResultForCredentials(email, password, validationResult)
  }

  fun mockSignInResultForCredentials(
    email: String,
    password: String,
    vararg signInResult: SignInResult,
  ) = apply {
    fakeSignInUseCase.mockSignInResultForCredentials(email, password, *signInResult)
  }

  /* Call verifications */

  fun verifySignInUseCaseNeverCalled() = apply {
    fakeSignInUseCase.verifyUseCaseNeverCalled()
  }

  fun verifySignInVerificationUseCaseNeverCalled() = apply {
    fakeSignInValidationUseCase.verifyUseCaseNeverCalled()
  }
}
