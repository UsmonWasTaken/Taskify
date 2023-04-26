#!/bin/sh

#
# Copyright 2023 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#

######## KTLINT-GRADLE HOOK START ########

CHANGED_FILES="$(git --no-pager diff --name-status --no-color --cached | awk '$1 != "D" && $2 ~ /\.kts|\.kt/ { print $2}')"

if [ -z "$CHANGED_FILES" ]; then
  echo "No Kotlin staged files."
  exit 0
fi

echo "Running ktlint over these files:"
echo "$CHANGED_FILES"

./gradlew --quiet formatKotlin -PinternalKtlintGitFilter="$CHANGED_FILES"

echo "Completed ktlint run."

echo "$CHANGED_FILES" | while read -r file; do
  if [ -f "$file" ]; then
    git add "$file"
  fi
done

echo "Completed ktlint hook."
######## KTLINT-GRADLE HOOK END ########
