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

package app.taskify.auth.data.repository

import app.taskify.auth.data.fake.FakeFirebaseAuth
import app.taskify.auth.domain.util.EmailAlreadyInUseException
import app.taskify.auth.domain.util.InvalidCredentialsException
import app.taskify.core.domain.exception.NetworkException
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProdAuthRepositoryTest {

  private lateinit var fakeFirebaseAuth: FakeFirebaseAuth
  private lateinit var repository: ProdAuthRepository

  private val defaultEmail = "hello@johndoe.com"
  private val defaultPassword = "12345678"

  @Before
  fun setup() {
    fakeFirebaseAuth = FakeFirebaseAuth()
    repository = ProdAuthRepository(fakeFirebaseAuth.mock)
  }

  @Test
  fun networkDisconnectedWhileSigningIn() = runTest {
    val email = defaultEmail
    val password = defaultPassword

    fakeFirebaseAuth.throwWhenSignInCalledWithCredentials(
      email = email,
      password = password,
      exception = FirebaseNetworkException("DISCONNECTED"),
    )

    val result = repository.signIn(email, password)

    assertThat(result.isFailure).isTrue()
    assertThat(result.exceptionOrNull()).isNotNull()
    assertThat(result.exceptionOrNull()).isInstanceOf(NetworkException::class.java)
  }

  @Test
  fun networkDisconnectedWhileSigningUp() = runTest {
    val email = defaultEmail
    val password = defaultPassword

    fakeFirebaseAuth.throwWhenSignUpCalledWithCredentials(
      email = email,
      password = password,
      exception = FirebaseNetworkException("DISCONNECTED"),
    )

    val result = repository.signUp(email, password)

    assertThat(result.isFailure).isTrue()
    assertThat(result.exceptionOrNull()).isNotNull()
    assertThat(result.exceptionOrNull()).isInstanceOf(NetworkException::class.java)
  }

  @Test
  fun emailAlreadyInUse() = runTest {
    val email = defaultEmail
    val password = defaultPassword

    fakeFirebaseAuth.throwWhenSignUpCalledWithCredentials(
      email = email,
      password = password,
      exception = FirebaseAuthException("ERROR_EMAIL_ALREADY_IN_USE", "email is already in use"),
    )

    val result = repository.signUp(email, password)

    assertThat(result.isFailure).isTrue()
    assertThat(result.exceptionOrNull()).isNotNull()
    assertThat(result.exceptionOrNull()).isInstanceOf(EmailAlreadyInUseException::class.java)
  }

  @Test
  fun invalidCredentials() = runTest {
    val email = defaultEmail
    val password = defaultPassword

    fakeFirebaseAuth.throwWhenSignInCalledWithCredentials(
      email = email,
      password = password,
      exception = FirebaseAuthException("ERROR_INVALID_CREDENTIAL", "email or password went wrong"),
    )

    val result = repository.signIn(email, password)

    assertThat(result.isFailure).isTrue()
    assertThat(result.exceptionOrNull()).isNotNull()
    assertThat(result.exceptionOrNull()).isInstanceOf(InvalidCredentialsException::class.java)
  }

  @Test
  fun unexpectedExceptionOnSignIn() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val unexpectedException = RuntimeException()

    fakeFirebaseAuth.throwWhenSignInCalledWithCredentials(
      email = email,
      password = password,
      exception = unexpectedException,
    )

    val result = repository.signIn(email, password)

    assertThat(result.isFailure).isTrue()
    assertThat(result.exceptionOrNull()).isNotNull()
    assertThat(result.exceptionOrNull()).isEqualTo(unexpectedException)
  }

  @Test
  fun unexpectedExceptionOnSignUp() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val unexpectedException = RuntimeException()

    fakeFirebaseAuth.throwWhenSignUpCalledWithCredentials(
      email = email,
      password = password,
      exception = unexpectedException,
    )

    val result = repository.signUp(email, password)

    assertThat(result.isFailure).isTrue()
    assertThat(result.exceptionOrNull()).isNotNull()
    assertThat(result.exceptionOrNull()).isEqualTo(unexpectedException)
  }

  @Test
  fun signInIsSuccessful() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val userId = "user id"

    fakeFirebaseAuth.mockSignInUserIdForCredentials(email, password, userId)

    val result = repository.signIn(email, password)

    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrNull()).isNotNull()
    assertThat(result.getOrNull()).isEqualTo(userId)
  }

  @Test
  fun signUpIsSuccessful() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val userId = "user id"

    fakeFirebaseAuth.mockSignUpUserIdForCredentials(email, password, userId)

    val result = repository.signUp(email, password)

    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrNull()).isNotNull()
    assertThat(result.getOrNull()).isEqualTo(userId)
  }
}
