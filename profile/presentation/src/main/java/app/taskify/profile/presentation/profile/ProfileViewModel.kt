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

package app.taskify.profile.presentation.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import app.taskify.core.domain.Text
import app.taskify.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

private const val VIEW_STATE = "profile_view_state_key"

@HiltViewModel
class ProfileViewModel @Inject constructor(
  @Suppress("UnusedPrivateProperty") private val profileRepository: ProfileRepository,
  savedStateHandle: SavedStateHandle,
) : ViewModel() {

  val viewState = savedStateHandle.getStateFlow(VIEW_STATE, ProfileViewState())

  private val navigationChannel = Channel<ProfileNavigationEvent>()
  val navigationFlow = navigationChannel.receiveAsFlow()

  private val messageChannel = Channel<Text>()
  val messageFlow = messageChannel.receiveAsFlow()

  fun onSignOutClicked() {
    TODO("Not yet implemented")
  }

  private fun retrieveProfileInfo() {
    TODO("Not yet implemented")
  }

  init {
    retrieveProfileInfo()
  }
}
