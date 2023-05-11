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

import app.taskify.auth.domain.repository.SignUpResult.Authenticated
import app.taskify.auth.domain.repository.SignUpResult.Authenticating
import app.taskify.auth.domain.repository.SignUpResult.Failure.EmailAlreadyInUse
import app.taskify.auth.domain.repository.SignUpResult.Failure.NoNetworkConnection
import app.taskify.auth.domain.repository.SignUpResult.Failure.Unknown
import app.taskify.auth.domain.repository.SignUpResult.SettingUpProfile
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult.DisplayNameError
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult.EmailError
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult.PasswordError
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MAX_DISPLAY_NAME_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MAX_PASSWORD_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MIN_DISPLAY_NAME_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MIN_PASSWORD_LENGTH
import app.taskify.core.test.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignUpViewModelTest {

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private lateinit var viewModelRobot: SignUpViewModelRobot

  private val defaultDisplayName = "John Doe"
  private val defaultEmail = "hello@johndoe.com"
  private val defaultPassword = "12345678"

  @Before
  fun setup() {
    viewModelRobot = SignUpViewModelRobot()
  }

  @Test
  fun `Submit with valid credentials, Authenticating, SettingUpProfile, and Authenticated SignUpResults`() = runTest {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = defaultPassword
    val inputValidationResult = SignUpValidationResult(displayNameError = null, emailError = null, passwordError = null)

    val credentialsEnteredViewState = SignUpViewState(displayName = displayName, email = email, password = password)
    val authenticatingViewState = credentialsEnteredViewState.copy(loadingText = Authenticating.description)
    val settingUpProfileViewState = credentialsEnteredViewState.copy(loadingText = SettingUpProfile.description)
    val authenticatedViewState = SignUpViewState(loadingText = Authenticated.description)

    viewModelRobot
      // Preparing
      .buildViewModel()
      .mockSignUpValidationResultForCredentials(displayName, email, password, inputValidationResult)
      // Validating the entered credentials
      .assertViewState(SignUpViewState())
      .enterDisplayName(displayName)
      .enterEmail(email)
      .enterPassword(password)
      .assertViewState(credentialsEnteredViewState)
      // Verifying the use cases never called yet
      .verifySignUpValidationUseCaseNeverCalled()
      .verifySignUpUseCaseNeverCalled()
      // Testing the successful sign in results
      .clickSignUp()
      .emitSignUpResult(Authenticating)
      .assertViewState(authenticatingViewState)
      .emitSignUpResult(SettingUpProfile)
      .assertViewState(settingUpProfileViewState)
      .emitSignUpResult(Authenticated)
      .assertViewState(authenticatedViewState)
  }

  @Test
  fun `Submit with empty, short, and long display names, Empty, TooShort, TooLong display name validation results`() {
    val emptyDisplayName = ""
    val shortDisplayName = "a".repeat(MIN_DISPLAY_NAME_LENGTH - 1)
    val longDisplayName = "a".repeat(MAX_DISPLAY_NAME_LENGTH + 1)
    val email = defaultEmail
    val password = defaultPassword

    val emptyDisplayNameValidationResult = SignUpValidationResult(displayNameError = DisplayNameError.Empty)
    val shortDisplayNameValidationResult = SignUpValidationResult(displayNameError = DisplayNameError.TooShort)
    val longDisplayNameValidationResult = SignUpValidationResult(displayNameError = DisplayNameError.TooLong)

    val initialViewState = SignUpViewState(email = email, password = password)

    viewModelRobot
      // Preparing
      .buildViewModel()
      .enterEmail(email)
      .enterPassword(password)
      .mockSignUpValidationResultForCredentials(emptyDisplayName, email, password, emptyDisplayNameValidationResult)
      .mockSignUpValidationResultForCredentials(shortDisplayName, email, password, shortDisplayNameValidationResult)
      .mockSignUpValidationResultForCredentials(longDisplayName, email, password, longDisplayNameValidationResult)
      .assertViewState(initialViewState)
      // 1. Test with empty display name
      .enterDisplayName(emptyDisplayName)
      .clickSignUp()
      .assertViewState(
        initialViewState.copy(displayNameError = DisplayNameError.Empty.description),
      )
      // 2. Test with too short display name
      .enterDisplayName(shortDisplayName)
      .clickSignUp()
      .assertViewState(
        initialViewState.copy(displayName = shortDisplayName, displayNameError = DisplayNameError.TooShort.description),
      )
      // 3. Test with too long display name
      .enterDisplayName(longDisplayName)
      .clickSignUp()
      .assertViewState(
        initialViewState.copy(displayName = longDisplayName, displayNameError = DisplayNameError.TooLong.description),
      )
      // Verifying the sign up use case never called
      .verifySignUpUseCaseNeverCalled()
  }

  @Test
  fun `Submit with empty, and invalid emails, Empty, and Invalid email validation results`() = runTest {
    val displayName = defaultDisplayName
    val emptyEmail = ""
    val invalidEmail = "invalid"
    val password = defaultPassword

    val initialViewState = SignUpViewState(displayName = displayName, password = password)
    val emptyEmailViewState = initialViewState.copy(email = emptyEmail, emailError = EmailError.Empty.description)
    val invalidEmailViewState = initialViewState.copy(email = invalidEmail, emailError = EmailError.Invalid.description)

    val emptyEmailValidationResult = SignUpValidationResult(emailError = EmailError.Empty)
    val invalidEmailValidationResult = SignUpValidationResult(emailError = EmailError.Invalid)

    viewModelRobot
      // Preparing
      .buildViewModel()
      .enterDisplayName(displayName)
      .enterPassword(password)
      .mockSignUpValidationResultForCredentials(displayName, emptyEmail, password, emptyEmailValidationResult)
      .mockSignUpValidationResultForCredentials(displayName, invalidEmail, password, invalidEmailValidationResult)
      .assertViewState(initialViewState)
      // 1. Test with empty email
      .enterEmail(emptyEmail)
      .clickSignUp()
      .assertViewState(emptyEmailViewState)
      // 2. Test with invalid email
      .enterEmail(invalidEmail)
      .clickSignUp()
      .assertViewState(invalidEmailViewState)
      // Verifying the sign up use case never called
      .verifySignUpUseCaseNeverCalled()
  }

  @Test
  fun `Submit with empty, short, and long passwords, Empty, TooShort, TooLong password validation results`() = runTest {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val emptyPassword = ""
    val shortPassword = "a".repeat(MIN_PASSWORD_LENGTH - 1)
    val longPassword = "a".repeat(MAX_PASSWORD_LENGTH + 1)

    val initialViewState = SignUpViewState(displayName = displayName, email = email, password = emptyPassword)
    val emptyPasswordViewState =
      initialViewState.copy(password = emptyPassword, passwordError = PasswordError.Empty.description)
    val shortPasswordViewState = initialViewState
      .copy(password = shortPassword, passwordError = PasswordError.TooShort.description)
    val longPasswordViewState = initialViewState
      .copy(password = longPassword, passwordError = PasswordError.TooLong.description)

    val emptyPasswordValidationResult = SignUpValidationResult(passwordError = PasswordError.Empty)
    val shortPasswordValidationResult = SignUpValidationResult(passwordError = PasswordError.TooShort)
    val longPasswordValidationResult = SignUpValidationResult(passwordError = PasswordError.TooLong)

    viewModelRobot
      // Preparing
      .buildViewModel()
      .enterEmail(email)
      .enterDisplayName(displayName)
      .mockSignUpValidationResultForCredentials(displayName, email, emptyPassword, emptyPasswordValidationResult)
      .mockSignUpValidationResultForCredentials(displayName, email, shortPassword, shortPasswordValidationResult)
      .mockSignUpValidationResultForCredentials(displayName, email, longPassword, longPasswordValidationResult)
      .assertViewState(initialViewState)
      // 1. Test with empty password
      .enterPassword(emptyPassword)
      .clickSignUp()
      .assertViewState(emptyPasswordViewState)
      // 2. Test with too short password
      .enterPassword(shortPassword)
      .clickSignUp()
      .assertViewState(shortPasswordViewState)
      // 3. Test with too long password
      .enterPassword(longPassword)
      .clickSignUp()
      .assertViewState(longPasswordViewState)
      // Verifying the sign up use case never called
      .verifySignUpUseCaseNeverCalled()
  }

  @Test
  fun test_SignInClick() = runTest {
    viewModelRobot
      .buildViewModel()
      .assertNavigationEvents(
        expectedNavigationEvents = arrayOf(SignUpNavigationEvent.NavigateBackToSignIn),
        action = {
          clickSignIn()
        },
      )
  }

  @Test
  fun `Network disconnected, or Unexpected error occurred, Show a corresponding error message`() = runTest {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = defaultPassword
    val inputValidationResult = SignUpValidationResult()
    val unexpectedException = RuntimeException()

    viewModelRobot
      .buildViewModel()
      .enterDisplayName(displayName)
      .enterEmail(email)
      .enterPassword(password)
      .mockSignUpValidationResultForCredentials(displayName, email, password, inputValidationResult)
      // 1. Network disconnected
      .assertMessages(
        expectedMessages = arrayOf(NoNetworkConnection.description),
        action = {
          clickSignUp()
          backgroundScope.launch {
            emitSignUpResult(NoNetworkConnection)
          }
        },
      )
      // 2. Unexpected exception occurred
      .assertMessages(
        expectedMessages = arrayOf(Unknown(unexpectedException).description),
        action = {
          clickSignUp()
          backgroundScope.launch {
            emitSignUpResult(Unknown(unexpectedException))
          }
        },
      )
  }

  @Test
  fun `Email is already in use`() = runTest {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = defaultPassword

    val inputValidationResult = SignUpValidationResult(displayNameError = null, emailError = null, passwordError = null)
    val initialViewState = SignUpViewState(displayName = displayName, email = email, password = password)
    val emailIsAlreadyInUseViewState = initialViewState.copy(emailError = EmailAlreadyInUse.description)

    viewModelRobot
      .buildViewModel()
      .enterDisplayName(displayName)
      .enterEmail(email)
      .enterPassword(password)
      .mockSignUpValidationResultForCredentials(displayName, email, password, inputValidationResult)
      .assertViewState(initialViewState)
      .clickSignUp()
      .emitSignUpResult(EmailAlreadyInUse)
      .assertViewState(emailIsAlreadyInUseViewState)
  }
}
