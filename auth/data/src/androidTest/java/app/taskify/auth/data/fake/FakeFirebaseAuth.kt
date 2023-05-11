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

package app.taskify.auth.data.fake

import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk

class FakeFirebaseAuth {

  val mock: FirebaseAuth = mockk()

  fun mockSignInUserIdForCredentials(email: String, password: String, userId: String) {
    every { mock.signInWithEmailAndPassword(email, password).isComplete } returns true
    every { mock.signInWithEmailAndPassword(email, password).exception } returns null
    every { mock.signInWithEmailAndPassword(email, password).isCanceled } returns false
    every { mock.signInWithEmailAndPassword(email, password).result.user?.uid } answers { userId }
  }

  fun throwWhenSignInCalledWithCredentials(email: String, password: String, exception: Exception) {
    every { mock.signInWithEmailAndPassword(email, password) } throws exception
  }

  fun mockSignUpUserIdForCredentials(email: String, password: String, userId: String) {
    every { mock.createUserWithEmailAndPassword(email, password).isComplete } returns true
    every { mock.createUserWithEmailAndPassword(email, password).exception } returns null
    every { mock.createUserWithEmailAndPassword(email, password).isCanceled } returns false
    every { mock.createUserWithEmailAndPassword(email, password).result.user?.uid } answers { userId }
  }

  fun throwWhenSignUpCalledWithCredentials(email: String, password: String, exception: Exception) {
    every { mock.createUserWithEmailAndPassword(email, password) } throws exception
  }
}
