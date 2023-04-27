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

package app.taskify.core.domain

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class TextTest {

  private val context = ApplicationProvider.getApplicationContext<Context>()

  @Test
  fun staticStringText_ReturnsGivenString() {
    val expectedString = "String"
    val text = Text(expectedString)
    assertThat(text.getString(context)).isEqualTo(expectedString)
  }

  @Test
  fun resourceString_ReturnsSpecifiedStringResource() {
    val expectedString = context.resources.getString(R.string.test_string)
    val text = Text(R.string.test_string)
    assertThat(text.getString(context)).isEqualTo(expectedString)
  }

  @Test
  fun resourceStringWithOneArg_ReturnsSpecifiedStringResourceWithGivenArgument() {
    val expectedString = context.resources.getString(R.string.test_string_with_one_arg, 5)
    val text = Text(R.string.test_string_with_one_arg, 5)
    assertThat(text.getString(context)).isEqualTo(expectedString)
  }

  @Test
  fun resourceStringWithTwoArgs_ReturnsSpecifiedStringResourceWithGivenArguments() {
    val expectedString = context.resources.getString(R.string.test_string_with_two_arg, 5, 6)
    val text = Text(R.string.test_string_with_two_arg, 5, 6)
    assertThat(text.getString(context)).isEqualTo(expectedString)
  }

  @Test
  fun testOverwrittenEqualsFunction() {
    val text1 = Text("Test")
    val text2 = Text("Test")
    assertThat(text1).isEqualTo(text2)

    val text3 = Text(R.string.test_string)
    val text4 = Text(R.string.test_string)
    assertThat(text3).isEqualTo(text4)

    val text5 = Text(R.string.test_string_with_one_arg, 1)
    val text6 = Text(R.string.test_string_with_one_arg, 1)
    assertThat(text5).isEqualTo(text6)

    val text7 = Text(R.string.test_string_with_two_arg, 1, 2)
    val text8 = Text(R.string.test_string_with_two_arg, 1, 2)
    assertThat(text7).isEqualTo(text8)
  }

  @Test
  fun testOverwrittenHashcodeFunction() {
    val text1 = Text("Test")
    val text2 = Text("Test")
    assertThat(text1.hashCode()).isEqualTo(text2.hashCode())

    val text3 = Text(R.string.test_string)
    val text4 = Text(R.string.test_string)
    assertThat(text3.hashCode()).isEqualTo(text4.hashCode())

    val text5 = Text(R.string.test_string_with_one_arg, 1)
    val text6 = Text(R.string.test_string_with_one_arg, 1)
    assertThat(text5.hashCode()).isEqualTo(text6.hashCode())

    val text7 = Text(R.string.test_string_with_two_arg, 1, 2)
    val text8 = Text(R.string.test_string_with_two_arg, 1, 2)
    assertThat(text7.hashCode()).isEqualTo(text8.hashCode())
  }
}
