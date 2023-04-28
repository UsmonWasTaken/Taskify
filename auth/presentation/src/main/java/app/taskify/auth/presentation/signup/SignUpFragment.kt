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

package app.taskify.auth.presentation.signup

import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.viewModels
import app.taskify.auth.presentation.R
import app.taskify.auth.presentation.databinding.FragmentSignUpBinding
import app.taskify.auth.presentation.signup.SignUpNavigationEvent.NavigateBackToSignIn
import app.taskify.auth.presentation.signup.SignUpNavigationEvent.NavigateToMain
import app.taskify.core.presentation.BaseFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : BaseFragment<FragmentSignUpBinding>(R.layout.fragment_sign_up) {

  @VisibleForTesting
  internal val viewModel: SignUpViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.viewModel = viewModel

    viewModel.messageFlow.lifecycleAwareCollect { message ->
      Snackbar.make(binding.signUpRoot, message.getString(requireContext()), Snackbar.LENGTH_SHORT).show()
    }

    viewModel.navigationFlow.lifecycleAwareCollectLatest { event ->
      when (event) {
        NavigateToMain -> {
          // Navigate to the main feature from here.
        }

        NavigateBackToSignIn -> {
          // Navigate back to the Sign In fragment from here.
        }
      }
    }
  }
}
