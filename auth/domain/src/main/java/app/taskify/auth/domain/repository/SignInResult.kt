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

package app.taskify.auth.domain.repository

import app.taskify.auth.domain.R
import app.taskify.core.domain.Text

sealed class SignInResult(val description: Text) {

  object Authenticating : SignInResult(Text(R.string.authenticating))

  object RetrievingProfile : SignInResult(Text(R.string.retrieving_profile))

  object Authenticated : SignInResult(Text(R.string.authenticated))

  sealed class Failure(description: Text) : SignInResult(description) {

    object NoNetworkConnection : Failure(Text(R.string.no_network_connection))

    object InvalidCredentials : Failure(Text(R.string.invalid_credentials))

    data class Unknown(val throwable: Throwable) : Failure(Text(throwable.message ?: "Unknown error occurred."))
  }
}
