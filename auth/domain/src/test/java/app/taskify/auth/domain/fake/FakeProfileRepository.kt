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

package app.taskify.auth.domain.fake

import app.taskify.profile.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class FakeProfileRepository {

  val mock: ProfileRepository = mockk()

  fun mockRetrieveProfileResultForEmailAndPassword(
    userId: String,
    result: Result<Unit>,
  ) {
    coEvery { mock.retrieveProfile(userId) } returns result
  }

  fun mockSetupProfileResultForEmailAndPassword(
    displayName: String,
    email: String,
    userId: String,
    result: Result<Unit>,
  ) {
    coEvery { mock.setupProfile(displayName, email, userId) } returns result
  }

  fun verifyRetrieveProfileNeverCalled() {
    coVerify(exactly = 0) { mock.retrieveProfile(any()) }
  }

  fun verifySetupProfileNeverCalled() {
    coVerify(exactly = 0) { mock.setupProfile(any(), any(), any()) }
  }
}
