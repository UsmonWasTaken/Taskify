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

import app.taskify.auth.domain.repository.SignUpResult
import app.taskify.auth.domain.repository.SignUpResult.Authenticated
import app.taskify.auth.domain.repository.SignUpResult.Authenticating
import app.taskify.auth.domain.repository.SignUpResult.SettingUpProfile
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult.DisplayNameError
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult.EmailError
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult.PasswordError
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MAX_DISPLAY_NAME_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MAX_PASSWORD_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MIN_DISPLAY_NAME_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MIN_PASSWORD_LENGTH
import app.taskify.auth.presentation.signup.SignUpNavigationEvent.NavigateToMain
import app.taskify.core.domain.Text
import app.taskify.core.test.CoroutinesTestScopeRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

  @get:Rule
  val coroutineTestScopeRule = CoroutinesTestScopeRule()

  private lateinit var viewModelRobot: SignUpViewModelRobot

  private val defaultDisplayName = "John Doe"
  private val defaultEmail = "hello@johndoe.com"
  private val defaultPassword = "12345678"

  @Before
  fun setup() {
    viewModelRobot = SignUpViewModelRobot()
  }

  @Test
  fun test_UpdateCredentials() = runTest {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = defaultPassword

    val initialViewState = SignUpViewState()
    val displayNameEnteredViewState = initialViewState.copy(displayName = displayName)
    val emailEnteredViewState = displayNameEnteredViewState.copy(email = email)
    val passwordEnteredViewState = emailEnteredViewState.copy(password = password)

    viewModelRobot
      .buildViewModel()
      .assertViewStates(
        initialViewState,
        displayNameEnteredViewState,
        emailEnteredViewState,
        passwordEnteredViewState,
      ) {
        enterDisplayName(displayName)
        enterEmail(email)
        enterPassword(password)
      }
      // Sign In use cases should not be called until SignIn button click
      .verifySignUpUseCaseNeverCalled()
      .verifySignUpVerificationUseCaseNeverCalled()
  }

  @Test
  fun test_SubmitEmptyDisplayName_SubmitShortDisplayName_SubmitLongDisplayName() = runTest {
    val emptyDisplayName = ""
    val shortDisplayName = "a".repeat(MIN_DISPLAY_NAME_LENGTH - 1)
    val longDisplayName = "a".repeat(MAX_DISPLAY_NAME_LENGTH + 1)
    val email = defaultEmail
    val password = defaultPassword

    val initialViewState = SignUpViewState(displayName = emptyDisplayName, email = email, password = password)

    val viewStateWithEmptyDisplayNameError = initialViewState.copy(
      displayNameError = DisplayNameError.Empty.description,
    )
    val shortDisplayNameEnteredViewState = viewStateWithEmptyDisplayNameError.copy(
      displayName = shortDisplayName,
      displayNameError = null,
    )
    val viewStateWithShortDisplayNameError = shortDisplayNameEnteredViewState.copy(
      displayNameError = DisplayNameError.TooShort.description,
    )
    val longDisplayNameEnteredViewState = viewStateWithShortDisplayNameError.copy(
      displayName = longDisplayName,
      displayNameError = null,
    )
    val viewStateWithLongDisplayNameError = longDisplayNameEnteredViewState.copy(
      displayNameError = DisplayNameError.TooLong.description,
    )

    val emptyDisplayNameValidationResult = SignUpValidationResult(
      displayNameError = DisplayNameError.Empty,
      emailError = null,
      passwordError = null,
    )
    val shortDisplayNameValidationResult = SignUpValidationResult(
      displayNameError = DisplayNameError.TooShort,
      emailError = null,
      passwordError = null,
    )
    val longDisplayNameValidationResult = SignUpValidationResult(
      displayNameError = DisplayNameError.TooLong,
      emailError = null,
      passwordError = null,
    )

    viewModelRobot
      .buildViewModel()
      .enterEmail(email)
      .enterPassword(password)
      .mockSignUpValidationResultForCredentials(emptyDisplayName, email, password, emptyDisplayNameValidationResult)
      .mockSignUpValidationResultForCredentials(shortDisplayName, email, password, shortDisplayNameValidationResult)
      .mockSignUpValidationResultForCredentials(longDisplayName, email, password, longDisplayNameValidationResult)
      .assertViewStates(
        initialViewState,
        viewStateWithEmptyDisplayNameError,
        shortDisplayNameEnteredViewState,
        viewStateWithShortDisplayNameError,
        longDisplayNameEnteredViewState,
        viewStateWithLongDisplayNameError,
      ) {
        enterDisplayName(emptyDisplayName)
        clickSignUp()
        enterDisplayName(shortDisplayName)
        clickSignUp()
        enterDisplayName(longDisplayName)
        clickSignUp()
      }
      .verifySignUpUseCaseNeverCalled()
  }

  @Test
  fun test_SubmitEmptyEmail_SubmitInvalidEmail() = runTest {
    val displayName = defaultDisplayName
    val emptyEmail = ""
    val invalidEmail = "invalid"
    val password = defaultPassword

    val initialViewState = SignUpViewState(displayName = displayName, password = password)

    val viewStateWithEmptyEmailError = initialViewState.copy(
      emailError = EmailError.Empty.description,
    )
    val emailEnteredViewState = viewStateWithEmptyEmailError.copy(
      email = invalidEmail,
      emailError = null,
    )
    val viewStateWithInvalidEmailError = emailEnteredViewState.copy(
      emailError = EmailError.Invalid.description,
    )

    val emptyEmailValidationResult = SignUpValidationResult(
      displayNameError = null,
      emailError = EmailError.Empty,
      passwordError = null,
    )
    val invalidEmailValidationResult = SignUpValidationResult(
      displayNameError = null,
      emailError = EmailError.Invalid,
      passwordError = null,
    )

    viewModelRobot
      .buildViewModel()
      .enterDisplayName(displayName)
      .enterPassword(password)
      .mockSignUpValidationResultForCredentials(displayName, emptyEmail, password, emptyEmailValidationResult)
      .mockSignUpValidationResultForCredentials(displayName, invalidEmail, password, invalidEmailValidationResult)
      .assertViewStates(
        initialViewState,
        viewStateWithEmptyEmailError,
        emailEnteredViewState,
        viewStateWithInvalidEmailError,
      ) {
        enterEmail(emptyEmail)
        clickSignUp()
        enterEmail(invalidEmail)
        clickSignUp()
      }
      .verifySignUpUseCaseNeverCalled()
  }

  @Test
  fun test_SubmitEmptyPassword_SubmitShortPassword_SubmitLongPassword() = runTest {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val emptyPassword = ""
    val shortPassword = "a".repeat(MIN_PASSWORD_LENGTH - 1)
    val longPassword = "a".repeat(MAX_PASSWORD_LENGTH + 1)

    val initialViewState = SignUpViewState(displayName = displayName, email = email, password = emptyPassword)

    val viewStateWithEmptyPasswordError = initialViewState.copy(
      passwordError = PasswordError.Empty.description,
    )
    val shortPasswordEnteredViewState = viewStateWithEmptyPasswordError.copy(
      password = shortPassword,
      passwordError = null,
    )
    val viewStateWithShortPasswordError = shortPasswordEnteredViewState.copy(
      passwordError = PasswordError.TooShort.description,
    )
    val longPasswordEnteredViewState = viewStateWithShortPasswordError.copy(
      password = longPassword,
      passwordError = null,
    )
    val viewStateWithLongPasswordError = longPasswordEnteredViewState.copy(
      passwordError = PasswordError.TooLong.description,
    )

    val emptyPasswordValidationResult = SignUpValidationResult(
      displayNameError = null,
      emailError = null,
      passwordError = PasswordError.Empty,
    )
    val shortPasswordValidationResult = SignUpValidationResult(
      displayNameError = null,
      emailError = null,
      passwordError = PasswordError.TooShort,
    )
    val longPasswordValidationResult = SignUpValidationResult(
      displayNameError = null,
      emailError = null,
      passwordError = PasswordError.TooLong,
    )

    viewModelRobot
      .buildViewModel()
      .enterEmail(email)
      .enterDisplayName(displayName)
      .mockSignUpValidationResultForCredentials(displayName, email, emptyPassword, emptyPasswordValidationResult)
      .mockSignUpValidationResultForCredentials(displayName, email, shortPassword, shortPasswordValidationResult)
      .mockSignUpValidationResultForCredentials(displayName, email, longPassword, longPasswordValidationResult)
      .assertViewStates(
        initialViewState,
        viewStateWithEmptyPasswordError,
        shortPasswordEnteredViewState,
        viewStateWithShortPasswordError,
        longPasswordEnteredViewState,
        viewStateWithLongPasswordError,
      ) {
        enterPassword(emptyPassword)
        clickSignUp()
        enterPassword(shortPassword)
        clickSignUp()
        enterPassword(longPassword)
        clickSignUp()
      }
      .verifySignUpUseCaseNeverCalled()
  }

  @Test
  fun test_SignInClick() = runTest {
    val navigateBackToSignInEvent = SignUpNavigationEvent.NavigateBackToSignIn

    viewModelRobot
      .buildViewModel()
      .assertNavigationEvents(navigateBackToSignInEvent) {
        clickSignIn()
      }
  }

  @Test
  fun test_NetworkDisconnected_UnexpectedErrorOccurs() = runTest {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = defaultPassword

    val expectedViewStateAfterFailure = SignUpViewState(displayName = displayName, email = email, password = password)

    val inputValidationResult = SignUpValidationResult(displayNameError = null, emailError = null, passwordError = null)
    val noNetworkSignUpResult = SignUpResult.Failure.NoNetworkConnection
    val unexpectedExceptionMessage = "UNEXPECTED"
    val unexpectedErrorSignUpResult = SignUpResult.Failure.Unknown(RuntimeException(unexpectedExceptionMessage))

    viewModelRobot
      .buildViewModel()
      .enterDisplayName(displayName)
      .enterEmail(email)
      .enterPassword(password)
      .mockSignUpValidationResultForCredentials(displayName, email, password, inputValidationResult)
      // Mock for Network error
      .mockSignUpResultForCredentials(displayName, email, password, noNetworkSignUpResult)
      .assertMessages(
        SignUpResult.Failure.NoNetworkConnection.description,
      ) { clickSignUp() }
      .assertViewState(expectedViewStateAfterFailure)
      // Mock for unexpected error
      .mockSignUpResultForCredentials(displayName, email, password, unexpectedErrorSignUpResult)
      .assertMessages(
        Text(unexpectedExceptionMessage),
      ) { clickSignUp() }
      .assertViewState(expectedViewStateAfterFailure)
  }

  @Test
  fun test_EmailIsAlreadyInUse() = runTest {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = defaultPassword

    val inputValidationResult = SignUpValidationResult(displayNameError = null, emailError = null, passwordError = null)
    val emailIsAlreadyInUseSignUpResult = SignUpResult.Failure.EmailAlreadyInUse
    val initialViewState = SignUpViewState(displayName = displayName, email = email, password = password)
    val expectedViewStateAfterFailure = initialViewState.copy(
      emailError = emailIsAlreadyInUseSignUpResult.description,
    )

    viewModelRobot
      .buildViewModel()
      .enterDisplayName(displayName)
      .enterEmail(email)
      .enterPassword(password)
      .mockSignUpValidationResultForCredentials(displayName, email, password, inputValidationResult)
      .mockSignUpResultForCredentials(displayName, email, password, emailIsAlreadyInUseSignUpResult)
      .assertViewStates(initialViewState, expectedViewStateAfterFailure) {
        clickSignUp()
      }
  }

  @Test
  fun test_Authenticating_RetrievingProfile() = runTest {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = defaultPassword

    val initialViewState = SignUpViewState(displayName = displayName, email = email, password = password)
    val authenticatingViewState = initialViewState.copy(
      loadingText = Authenticating.description,
    )
    val settingUpProfileViewState = authenticatingViewState.copy(
      loadingText = SettingUpProfile.description,
    )

    val inputValidationResult = SignUpValidationResult(displayNameError = null, emailError = null, passwordError = null)
    val signInResults = arrayOf(Authenticating, SettingUpProfile)

    viewModelRobot
      .buildViewModel()
      .enterDisplayName(displayName)
      .enterEmail(email)
      .enterPassword(password)
      .mockSignUpValidationResultForCredentials(displayName, email, password, inputValidationResult)
      .mockSignUpResultForCredentials(displayName, email, password, *signInResults)
      .assertViewStates(
        initialViewState,
        authenticatingViewState,
        settingUpProfileViewState,
      ) { clickSignUp() }
  }

  @Test
  fun test_SuccessfullyAuthenticated() = runTest {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = defaultPassword

    val inputValidationResult = SignUpValidationResult(displayNameError = null, emailError = null, passwordError = null)
    val signInResult = Authenticated
    val viewStateAfterAuthenticated = SignUpViewState(loadingText = Authenticated.description)

    viewModelRobot
      .buildViewModel()
      .enterDisplayName(displayName)
      .enterEmail(email)
      .enterPassword(password)
      .mockSignUpValidationResultForCredentials(displayName, email, password, inputValidationResult)
      .mockSignUpResultForCredentials(displayName, email, password, signInResult)
      .assertNavigationEvents(NavigateToMain) {
        clickSignUp()
      }
      .assertViewState(viewStateAfterAuthenticated)
  }
}
