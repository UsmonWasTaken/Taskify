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

package app.taskify.core.data.inject

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import app.taskify.core.data.PreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatastoreModule {

  private const val DATASTORE_PREFERENCES_NAME = "app.taskify:preferences"

  private val Context.dataStore: PreferencesDataStore by preferencesDataStore(DATASTORE_PREFERENCES_NAME)

  @Singleton
  @Provides
  fun provideDatastoreInstance(@ApplicationContext context: Context): PreferencesDataStore = context.dataStore
}
