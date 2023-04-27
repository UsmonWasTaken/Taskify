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

package app.taskify.auth.domain.usecases.signin

import app.taskify.auth.domain.R
import app.taskify.core.domain.Text
import app.taskify.core.test.matcher.FakeEmailMatcher
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class SignInValidationUseCaseTest {

  private lateinit var fakeEmailMatcher: FakeEmailMatcher
  private lateinit var signInValidationUseCase: SignInValidationUseCase

  private val defaultEmail = "hello@johndoe.com"
  private val defaultPassword = "12345678"

  @Before
  fun setup() {
    fakeEmailMatcher = FakeEmailMatcher()
    signInValidationUseCase = SignInValidationUseCase(
      emailMatcher = fakeEmailMatcher.mock,
    )
  }

  @Test
  fun emptyEmail_returnsSignInValidationResultWithEmptyEmailError() {
    val email = ""
    val password = defaultPassword

    val validationResult = signInValidationUseCase(email, password)

    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.emailError).isEqualTo(Text(R.string.empty_email))
    fakeEmailMatcher.verifyEmailMatcherNeverCalled()
  }

  @Test
  fun invalidEmail_returnsSignInValidationResultWithInvalidEmailError() {
    val email = "hunter2"
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = false)
    val validationResult = signInValidationUseCase(email, password)

    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.emailError).isEqualTo(Text(R.string.invalid_email))
  }

  @Test
  fun emptyPassword_returnsSignInValidationResultWithEmptyPasswordError() {
    val email = defaultEmail
    val password = ""

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signInValidationUseCase(email, password)
    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.passwordError).isEqualTo(Text(R.string.empty_password))
  }

  @Test
  fun validInputs_returnsNull() {
    val email = defaultEmail
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signInValidationUseCase(email, password)

    assertThat(validationResult).isNull()
  }
}
