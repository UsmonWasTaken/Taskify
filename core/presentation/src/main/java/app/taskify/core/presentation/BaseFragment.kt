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

package app.taskify.core.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseFragment<out T : ViewDataBinding>(
  @LayoutRes private val layoutId: Int,
) : Fragment(layoutId) {

  private var _binding: T? = null
  protected val binding: T
    get() = checkNotNull(_binding) {
      "DataBinding class instance is not available outside of onCreateView() and onDestroyView()"
    }

  @CallSuper
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding?.unbind()
    _binding = null
  }

  protected fun <T> Flow<T>.lifecycleAwareCollect(collector: FlowCollector<T>) {
    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        collect(collector)
      }
    }
  }

  protected fun <T> Flow<T>.lifecycleAwareCollectLatest(collector: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        collectLatest(collector)
      }
    }
  }
}
