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

import app.taskify.auth.domain.repository.AuthRepository
import app.taskify.auth.domain.util.EmailAlreadyInUseException
import app.taskify.auth.domain.util.InvalidCredentialsException
import app.taskify.core.domain.exception.NetworkException
import app.taskify.core.domain.util.resultOf
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class ProdAuthRepository @Inject constructor(
  private val firebaseAuth: FirebaseAuth,
) : AuthRepository {

  override suspend fun signIn(email: String, password: String): Result<String> = runCatching {
    val signInResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
    val userId = signInResult?.user?.uid
    checkNotNull(userId)
  }

  override suspend fun signUp(email: String, password: String): Result<String> = runCatching {
    val signInResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    val userId = signInResult?.user?.uid
    checkNotNull(userId)
  }

  private inline fun <T> runCatching(result: () -> T): Result<T> = resultOf(result) { throwable ->
    when (throwable) {
      is FirebaseNetworkException -> NetworkException(cause = throwable)
      is FirebaseAuthException -> firebaseAuthExceptionToDomainException(throwable)
      else -> throwable
    }
  }

  // Handle all of the possible auth and profile exceptions here.
  private fun firebaseAuthExceptionToDomainException(exception: FirebaseAuthException): Throwable {
    return when (exception.errorCode) {
      "ERROR_EMAIL_ALREADY_IN_USE" -> EmailAlreadyInUseException(cause = exception)

      "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_EMAIL", "ERROR_WRONG_PASSWORD" -> {
        InvalidCredentialsException(cause = exception)
      }

      else -> exception
    }
  }
}
