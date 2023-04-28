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

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import app.taskify.core.domain.Text
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("error")
fun TextInputLayout.error(error: Text?) {
  isErrorEnabled = error != null
  this.error = error?.getString(context)
}

@BindingAdapter("isVisible")
fun View.isVisible(isVisible: Boolean) {
  visibility = if (isVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("text")
fun TextView.text(value: Text) {
  text = value.getString(context)
}
