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

package app.taskify.auth.presentation.signin

import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.viewModels
import app.taskify.auth.presentation.R
import app.taskify.auth.presentation.databinding.FragmentSignInBinding
import app.taskify.auth.presentation.signin.SignInNavigationEvent.NavigateToMain
import app.taskify.auth.presentation.signin.SignInNavigationEvent.NavigateToSignUp
import app.taskify.core.presentation.BaseFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : BaseFragment<FragmentSignInBinding>(R.layout.fragment_sign_in) {

  @VisibleForTesting
  internal val viewModel: SignInViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = viewModel

    viewModel.messageFlow.lifecycleAwareCollect { message ->
      Snackbar.make(binding.signInRoot, message.getString(requireContext()), Snackbar.LENGTH_SHORT).show()
    }

    viewModel.navigationFlow.lifecycleAwareCollectLatest { event ->
      when (event) {
        NavigateToMain -> {
          // Navigate to the main feature from here.
        }

        NavigateToSignUp -> {
          // Navigate to the Sign Up fragment from here.
        }
      }
    }
  }
}
