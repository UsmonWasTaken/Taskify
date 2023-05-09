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

import app.taskify.auth.domain.repository.SignInResult
import app.taskify.auth.domain.repository.SignInResult.Authenticated
import app.taskify.auth.domain.repository.SignInResult.Authenticating
import app.taskify.auth.domain.repository.SignInResult.RetrievingProfile
import app.taskify.auth.domain.usecases.signin.SignInValidationResult
import app.taskify.auth.domain.usecases.signin.SignInValidationResult.EmailError
import app.taskify.auth.domain.usecases.signin.SignInValidationResult.PasswordError
import app.taskify.auth.presentation.signin.SignInNavigationEvent.NavigateToMain
import app.taskify.core.domain.Text
import app.taskify.core.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SignInViewModelTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private lateinit var viewModelRobot: SignInViewModelRobot

  private val defaultEmail = "hello@johndoe.com"
  private val defaultPassword = "12345678"

  @Before
  fun setup() {
    viewModelRobot = SignInViewModelRobot()
  }

  @Test
  fun test_UpdateCredentials() = runTest {
    val email = defaultEmail
    val password = defaultPassword

    val initialViewState = SignInViewState()
    val emailEnteredViewState = initialViewState.copy(email = email)
    val passwordEnteredViewState = emailEnteredViewState.copy(password = password)

    viewModelRobot
      .buildViewModel()
      .assertViewStates(
        initialViewState,
        emailEnteredViewState,
        passwordEnteredViewState,
      ) {
        enterEmail(email)
        enterPassword(password)
      }
      // Sign In use cases should not be called until SignIn button click
      .verifySignInUseCaseNeverCalled()
      .verifySignInVerificationUseCaseNeverCalled()
  }

  @Test
  fun test_SubmitEmptyEmail_SubmitInvalidEmail() = runTest {
    val emptyEmail = ""
    val invalidEmail = "invalid"
    val password = defaultPassword

    val initialViewState = SignInViewState()
    val passwordEnteredViewState = initialViewState.copy(password = password)
    val viewStateWithEmptyEmailError = passwordEnteredViewState.copy(
      emailError = EmailError.Empty.description,
    )
    val emailEnteredViewState = viewStateWithEmptyEmailError.copy(
      email = invalidEmail,
      emailError = null,
    )
    val viewStateWithInvalidEmailError = emailEnteredViewState.copy(
      emailError = EmailError.Invalid.description,
    )

    val emptyEmailValidationResult = SignInValidationResult(emailError = EmailError.Empty, passwordError = null)
    val invalidEmailValidationResult = SignInValidationResult(emailError = EmailError.Invalid, passwordError = null)

    viewModelRobot
      .buildViewModel()
      .mockSignInValidationResultForCredentials(emptyEmail, password, emptyEmailValidationResult)
      .mockSignInValidationResultForCredentials(invalidEmail, password, invalidEmailValidationResult)
      .assertViewStates(
        initialViewState,
        passwordEnteredViewState,
        viewStateWithEmptyEmailError,
        emailEnteredViewState,
        viewStateWithInvalidEmailError,
      ) {
        enterEmail(emptyEmail)
        enterPassword(password)
        clickSignIn()
        enterEmail(invalidEmail)
        clickSignIn()
      }
      .verifySignInUseCaseNeverCalled()
  }

  @Test
  fun test_SubmitEmptyPassword() = runTest {
    val email = defaultEmail
    val emptyPassword = ""

    val initialViewState = SignInViewState()
    val emailEnteredViewState = initialViewState.copy(email = email)
    val viewStateWithEmptyPasswordError = emailEnteredViewState.copy(
      passwordError = PasswordError.Empty.description,
    )

    val emptyPasswordValidationResult = SignInValidationResult(emailError = null, passwordError = PasswordError.Empty)

    viewModelRobot
      .buildViewModel()
      .mockSignInValidationResultForCredentials(email, emptyPassword, emptyPasswordValidationResult)
      .assertViewStates(
        initialViewState,
        emailEnteredViewState,
        viewStateWithEmptyPasswordError,
      ) {
        enterEmail(email)
        enterPassword(emptyPassword)
        clickSignIn()
      }
      .verifySignInUseCaseNeverCalled()
  }

  @Test
  fun test_SignUpClick() = runTest {
    val navigateToSignUpEvent = SignInNavigationEvent.NavigateToSignUp

    viewModelRobot
      .buildViewModel()
      .assertNavigationEvents(navigateToSignUpEvent) {
        clickSignUp()
      }
  }

  @Test
  fun test_NetworkDisconnected_InvalidCredentials_UnexpectedErrorOccurs() = runTest {
    val email = defaultEmail
    val password = defaultPassword

    val expectedViewStateAfterFailure = SignInViewState(email = email, password = password)

    val inputValidationResult = SignInValidationResult(emailError = null, passwordError = null)
    val noNetworkSignInResult = SignInResult.Failure.NoNetworkConnection
    val invalidCredentialsSignInResult = SignInResult.Failure.InvalidCredentials
    val unexpectedExceptionMessage = "UNEXPECTED"
    val unexpectedErrorSignInResult = SignInResult.Failure.Unknown(RuntimeException(unexpectedExceptionMessage))

    viewModelRobot
      .buildViewModel()
      .enterEmail(email)
      .enterPassword(password)
      .mockSignInValidationResultForCredentials(email, password, inputValidationResult)
      // Mock for Network error
      .mockSignInResultForCredentials(email, password, noNetworkSignInResult)
      .assertMessages(
        SignInResult.Failure.NoNetworkConnection.description,
      ) { clickSignIn() }
      .assertViewState(expectedViewStateAfterFailure)
      // Mock for invalid credentials error
      .mockSignInResultForCredentials(email, password, invalidCredentialsSignInResult)
      .assertMessages(
        SignInResult.Failure.InvalidCredentials.description,
      ) { clickSignIn() }
      .assertViewState(expectedViewStateAfterFailure)
      // Mock for unexpected error
      .mockSignInResultForCredentials(email, password, unexpectedErrorSignInResult)
      .assertMessages(
        Text(unexpectedExceptionMessage),
      ) { clickSignIn() }
      .assertViewState(expectedViewStateAfterFailure)
  }

  @Test
  fun test_Authenticating_RetrievingProfile() = runTest {
    val email = defaultEmail
    val password = defaultPassword

    val initialViewState = SignInViewState(email = email, password = password)
    val authenticatingViewState = initialViewState.copy(
      loadingText = Authenticating.description,
    )
    val retrievingProfileViewState = authenticatingViewState.copy(
      loadingText = RetrievingProfile.description,
    )

    val inputValidationResult = SignInValidationResult(emailError = null, passwordError = null)
    val signInResults = arrayOf(Authenticating, RetrievingProfile)

    viewModelRobot
      .buildViewModel()
      .enterEmail(email)
      .enterPassword(password)
      .mockSignInValidationResultForCredentials(email, password, inputValidationResult)
      .mockSignInResultForCredentials(email, password, *signInResults)
      .assertViewStates(
        initialViewState,
        authenticatingViewState,
        retrievingProfileViewState,
      ) { clickSignIn() }
  }

  @Test
  fun test_SuccessfullyAuthenticated() = runTest {
    val email = defaultEmail
    val password = defaultPassword

    val inputValidationResult = SignInValidationResult(emailError = null, passwordError = null)
    val signInResult = Authenticated
    val viewStateAfterAuthenticated = SignInViewState(loadingText = Authenticated.description)

    viewModelRobot
      .buildViewModel()
      .enterEmail(email)
      .enterPassword(password)
      .mockSignInValidationResultForCredentials(email, password, inputValidationResult)
      .mockSignInResultForCredentials(email, password, signInResult)
      .assertNavigationEvents(NavigateToMain) {
        clickSignIn()
      }
      .assertViewState(viewStateAfterAuthenticated)
  }
}
