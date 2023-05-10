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

package app.taskify.auth.data.inject

import app.taskify.auth.data.repository.ProdAuthRepository
import app.taskify.auth.domain.repository.AuthRepository
import app.taskify.auth.domain.usecases.signin.ProdSignInUseCase
import app.taskify.auth.domain.usecases.signin.SignInUseCase
import app.taskify.auth.domain.usecases.signup.ProdSignUpUseCase
import app.taskify.auth.domain.usecases.signup.SignUpUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AuthBindsModule {

  @Binds
  fun bindAuthRepository(target: ProdAuthRepository): AuthRepository

  @Binds
  fun bindSignInUseCase(target: ProdSignInUseCase): SignInUseCase

  @Binds
  fun bindSignUpUseCase(target: ProdSignUpUseCase): SignUpUseCase
}
