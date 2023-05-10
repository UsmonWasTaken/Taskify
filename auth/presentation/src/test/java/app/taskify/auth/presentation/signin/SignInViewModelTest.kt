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

import app.taskify.auth.domain.repository.SignInResult.Authenticated
import app.taskify.auth.domain.repository.SignInResult.Authenticating
import app.taskify.auth.domain.repository.SignInResult.Failure.InvalidCredentials
import app.taskify.auth.domain.repository.SignInResult.Failure.NoNetworkConnection
import app.taskify.auth.domain.repository.SignInResult.Failure.Unknown
import app.taskify.auth.domain.repository.SignInResult.RetrievingProfile
import app.taskify.auth.domain.usecases.signin.SignInValidationResult
import app.taskify.auth.domain.usecases.signin.SignInValidationResult.EmailError
import app.taskify.auth.domain.usecases.signin.SignInValidationResult.PasswordError
import app.taskify.auth.presentation.signin.SignInNavigationEvent.NavigateToSignUp
import app.taskify.core.test.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
  fun `Sign in clicked with valid credentials, Successful sign in results collected`() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val inputValidationResult = SignInValidationResult(emailError = null, passwordError = null)

    viewModelRobot
      // Preparing
      .buildViewModel()
      .mockSignInValidationResultForCredentials(email, password, inputValidationResult)
      // Validating the initial states
      .assertViewState(SignInViewState())
      .enterEmail(email)
      .assertViewState(SignInViewState(email = email))
      .enterPassword(password)
      .assertViewState(SignInViewState(email = email, password = password))
      // Verifying the use cases never called yet
      .verifySignInVerificationUseCaseNeverCalled()
      .verifySignInUseCaseNeverCalled()
      // Testing the successful sign in results
      .clickSignIn()
      .emitSignInResult(Authenticating)
      .assertViewState(SignInViewState(email = email, password = password, loadingText = Authenticating.description))
      .emitSignInResult(RetrievingProfile)
      .assertViewState(SignInViewState(email = email, password = password, loadingText = RetrievingProfile.description))
      .emitSignInResult(Authenticated)
      .assertViewState(SignInViewState(loadingText = Authenticated.description))
  }

  @Test
  fun `Submit with empty and invalid email`() {
    val emptyEmail = ""
    val invalidEmail = "invalid"
    val password = defaultPassword
    val emptyEmailInputValResult = SignInValidationResult(emailError = EmailError.Empty, passwordError = null)
    val invalidEmailInputValResult = SignInValidationResult(emailError = EmailError.Invalid, passwordError = null)

    viewModelRobot
      // Preparing
      .buildViewModel()
      .enterPassword(password)
      .mockSignInValidationResultForCredentials(emptyEmail, password, emptyEmailInputValResult)
      .mockSignInValidationResultForCredentials(invalidEmail, password, invalidEmailInputValResult)
      // Verifying the validation use cases never called yet
      .verifySignInVerificationUseCaseNeverCalled()
      // 1. Testing with empty email
      .enterEmail(emptyEmail)
      .assertViewState(SignInViewState(email = emptyEmail, password = password))
      .clickSignIn()
      .assertViewState(
        SignInViewState(email = emptyEmail, emailError = EmailError.Empty.description, password = password),
      )
      // 2. Testing with invalid email
      .enterEmail(invalidEmail)
      .assertViewState(SignInViewState(email = invalidEmail, password = password))
      .clickSignIn()
      .assertViewState(
        SignInViewState(email = invalidEmail, emailError = EmailError.Invalid.description, password = password),
      )
      // Verifying the sign in use case never called
      .verifySignInUseCaseNeverCalled()
  }

  @Test
  fun `Submit with empty password`() {
    val email = defaultEmail
    val emptyPassword = ""
    val emptyPasswordInputValResult = SignInValidationResult(emailError = null, passwordError = PasswordError.Empty)

    viewModelRobot
      // Preparing
      .buildViewModel()
      .enterEmail(email)
      .mockSignInValidationResultForCredentials(email, emptyPassword, emptyPasswordInputValResult)
      // Verifying the use cases never called yet
      .verifySignInVerificationUseCaseNeverCalled()
      // Testing with empty password
      .enterPassword(emptyPassword)
      .clickSignIn()
      .assertViewState(
        SignInViewState(email = email, password = emptyPassword, passwordError = PasswordError.Empty.description),
      )
      // Verifying the sign in use case never called
      .verifySignInUseCaseNeverCalled()
  }

  @Test
  fun `Click sign up, NavigateToSignUp event occurs`() = runTest {
    viewModelRobot
      .buildViewModel()
      .assertNavigationEvents(
        expectedNavigationEvents = arrayOf(NavigateToSignUp),
        action = {
          clickSignUp()
        },
      )
  }

  @Test
  fun `Network disconnected, Credentials was invalid, or Unexpected error occurred, Show an error message`() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val inputValidationResult = SignInValidationResult(emailError = null, passwordError = null)
    val unexpectedException = RuntimeException()

    viewModelRobot
      // Preparing
      .buildViewModel()
      .enterEmail(email)
      .enterPassword(password)
      .mockSignInValidationResultForCredentials(email, password, inputValidationResult)
      // 1. Network disconnected while authenticating
      .assertMessages(
        expectedMessages = arrayOf(NoNetworkConnection.description),
        action = {
          clickSignIn()
          backgroundScope.launch {
            emitSignInResult(NoNetworkConnection)
          }
        },
      )
      // 2. Credentials was invalid while authenticating
      .assertMessages(
        expectedMessages = arrayOf(InvalidCredentials.description),
        action = {
          clickSignIn()
          backgroundScope.launch {
            emitSignInResult(InvalidCredentials)
          }
        },
      )
      // 3. Unexpected exception occurred while authenticating
      .assertMessages(
        expectedMessages = arrayOf(Unknown(unexpectedException).description),
        action = {
          clickSignIn()
          backgroundScope.launch {
            emitSignInResult(Unknown(unexpectedException))
          }
        },
      )
  }
}
