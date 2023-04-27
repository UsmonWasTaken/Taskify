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

package app.taskify.core.test.preferences

import app.taskify.core.domain.preferences.AuthPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

@Suppress("unused")
class FakeAuthPreferences {

  val mock: AuthPreferences = mockk(relaxUnitFun = true)

  fun mockAuthenticatedForResult(result: Boolean) {
    every { mock.isAuthenticated } answers { flowOf(result) }
  }

  fun verifyIsAuthenticatedNeverCalled() {
    verify(exactly = 0) { mock.isAuthenticated }
  }
}
