/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.core.dribbble.data.api.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

import java.util.Date

/**
 * Models a dribbble user
 */
@Parcelize
data class User(
    val id: Long,
    val name: String,
    val username: String,
    val html_url: String,
    val avatar_url: String?,
    val links: Map<String, String>? = null,
    val shots_count: Int? = null,
    val teams_count: Int? = null,
    val type: String? = null,
    val pro: Boolean?,
    val created_at: Date? = null
) : Parcelable {

    val highQualityAvatarUrl: String?
        get() = avatar_url?.replace("/normal/", "/original/")
}
