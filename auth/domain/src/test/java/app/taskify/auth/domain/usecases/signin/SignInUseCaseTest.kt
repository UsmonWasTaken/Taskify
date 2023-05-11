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

import app.cash.turbine.test
import app.taskify.auth.domain.fake.FakeAuthRepository
import app.taskify.auth.domain.fake.FakeProfileRepository
import app.taskify.auth.domain.repository.SignInResult.Authenticated
import app.taskify.auth.domain.repository.SignInResult.Authenticating
import app.taskify.auth.domain.repository.SignInResult.Failure.InvalidCredentials
import app.taskify.auth.domain.repository.SignInResult.Failure.NoNetworkConnection
import app.taskify.auth.domain.repository.SignInResult.Failure.Unknown
import app.taskify.auth.domain.repository.SignInResult.RetrievingProfile
import app.taskify.auth.domain.util.InvalidCredentialsException
import app.taskify.core.domain.exception.NetworkException
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SignInUseCaseTest {

  private lateinit var fakeAuthRepository: FakeAuthRepository
  private lateinit var fakeProfileRepository: FakeProfileRepository
  private lateinit var signInUseCase: SignInUseCase

  private val defaultEmail = "hello@johndoe.com"
  private val defaultPassword = "12345678"

  @Before
  fun setup() {
    fakeAuthRepository = FakeAuthRepository()
    fakeProfileRepository = FakeProfileRepository()
    signInUseCase = ProdSignInUseCase(
      authRepository = fakeAuthRepository.mock,
      profileRepository = fakeProfileRepository.mock,
    )
  }

  @Test
  fun `Network disconnected while authenticating, emits NoNetworkConnection result`() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val authResult = Result.failure<String>(NetworkException())

    fakeAuthRepository.mockSignInResultForEmailAndPassword(email, password, authResult)

    signInUseCase(email, password).test {
      assertThat(awaitItem()).isEqualTo(Authenticating)
      assertThat(awaitItem()).isEqualTo(NoNetworkConnection)
      awaitComplete()
    }

    fakeProfileRepository.verifyRetrieveProfileNeverCalled()
  }

  @Test
  fun `Credentials was invalid while authenticating, emits InvalidCredentials result`() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val authResult = Result.failure<String>(InvalidCredentialsException())

    fakeAuthRepository.mockSignInResultForEmailAndPassword(email, password, authResult)

    signInUseCase(email, password).test {
      assertThat(awaitItem()).isEqualTo(Authenticating)
      assertThat(awaitItem()).isEqualTo(InvalidCredentials)
      awaitComplete()
    }

    fakeProfileRepository.verifyRetrieveProfileNeverCalled()
  }

  @Test
  fun `Unexpected exception occurred while authenticating, emits Unknown result with the exception`() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val unexpectedException = RuntimeException()
    val authResult = Result.failure<String>(unexpectedException)

    fakeAuthRepository.mockSignInResultForEmailAndPassword(email, password, authResult)

    signInUseCase(email, password).test {
      assertThat(awaitItem()).isEqualTo(Authenticating)
      assertThat(awaitItem()).run {
        isInstanceOf(Unknown::class.java)
        isEqualTo(Unknown(unexpectedException))
      }
      awaitComplete()
    }

    fakeProfileRepository.verifyRetrieveProfileNeverCalled()
  }

  @Test
  fun `Network disconnected while retrieving the profile, emits NoNetworkConnection result`() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val userId = "userId"
    val authResult = Result.success(userId)
    val profileResult = Result.failure<Unit>(NetworkException())

    fakeAuthRepository.mockSignInResultForEmailAndPassword(email, password, authResult)
    fakeProfileRepository.mockRetrieveProfileResultForEmailAndPassword(userId, profileResult)

    signInUseCase(email, password).test {
      assertThat(awaitItem()).isEqualTo(Authenticating)
      assertThat(awaitItem()).isEqualTo(RetrievingProfile)
      assertThat(awaitItem()).isEqualTo(NoNetworkConnection)
      awaitComplete()
    }
  }

  @Test
  fun `Unexpected exception occurred while retrieving the profile emits Unknown result with the exception`() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val userId = "userId"
    val unexpectedException = RuntimeException()
    val authResult = Result.success(userId)
    val profileResult = Result.failure<Unit>(unexpectedException)

    fakeAuthRepository.mockSignInResultForEmailAndPassword(email, password, authResult)
    fakeProfileRepository.mockRetrieveProfileResultForEmailAndPassword(userId, profileResult)

    signInUseCase(email, password).test {
      assertThat(awaitItem()).isEqualTo(Authenticating)
      assertThat(awaitItem()).isEqualTo(RetrievingProfile)
      assertThat(awaitItem()).run {
        isInstanceOf(Unknown::class.java)
        isEqualTo(Unknown(unexpectedException))
      }
      awaitComplete()
    }
  }

  @Test
  fun `Everything was OK, emits Authenticated result`() = runTest {
    val email = defaultEmail
    val password = defaultPassword
    val userId = "userId"
    val authResult = Result.success(userId)
    val profileResult = Result.success(Unit)

    fakeAuthRepository.mockSignInResultForEmailAndPassword(email, password, authResult)
    fakeProfileRepository.mockRetrieveProfileResultForEmailAndPassword(userId, profileResult)

    signInUseCase(email, password).test {
      assertThat(awaitItem()).isEqualTo(Authenticating)
      assertThat(awaitItem()).isEqualTo(RetrievingProfile)
      assertThat(awaitItem()).isEqualTo(Authenticated)
      awaitComplete()
    }
  }
}
