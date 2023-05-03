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

import app.taskify.auth.domain.repository.AuthRepository
import app.taskify.auth.domain.repository.SignInResult
import app.taskify.auth.domain.repository.SignInResult.Authenticated
import app.taskify.auth.domain.repository.SignInResult.Authenticating
import app.taskify.auth.domain.repository.SignInResult.Failure.InvalidCredentials
import app.taskify.auth.domain.repository.SignInResult.Failure.NoNetworkConnection
import app.taskify.auth.domain.repository.SignInResult.Failure.Unknown
import app.taskify.auth.domain.repository.SignInResult.RetrievingProfile
import app.taskify.auth.domain.util.InvalidCredentialsException
import app.taskify.core.domain.exception.NetworkException
import app.taskify.profile.domain.repository.ProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SignInUseCase @Inject constructor(
  private val authRepository: AuthRepository,
  private val profileRepository: ProfileRepository,
) {

  operator fun invoke(email: String, password: String): Flow<SignInResult> = flow {
    emit(Authenticating)
    val authResult = authRepository.signIn(email, password)
    val userId = authResult.getOrElse { throwable ->
      emit(throwable.toFailureSignInResult())
      return@flow
    }
    emit(RetrievingProfile)
    profileRepository.retrieveProfile(userId).onFailure { throwable ->
      emit(throwable.toFailureSignInResult())
      return@flow
    }
    emit(Authenticated)
  }

  // Handle all of the possible auth and profile exceptions here.
  private fun Throwable.toFailureSignInResult(): SignInResult.Failure = when (this) {
    is NetworkException -> NoNetworkConnection
    is InvalidCredentialsException -> InvalidCredentials
    else -> Unknown(this)
  }
}
