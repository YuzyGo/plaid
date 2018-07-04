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

package io.plaidapp.core.dribbble.data.api.search

import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.api.model.Shot
import java.io.IOException

class DribbbleSearchRemoteDataSource(private val service: DribbbleSearchService) {

    suspend fun search(
        query: String,
        page: Int
    ): Result<List<Shot>> {
        val response = service.searchCr(query, page).await()
        return if (response.isSuccessful) {
            Result.Success(response.body().orEmpty())
        } else {
            Result.Error(IOException("Error getting comments ${response.code()} ${response.message()}"))
        }
    }

    companion object {
        @Volatile private var INSTANCE: DribbbleSearchRemoteDataSource? = null

        fun getInstance(service: DribbbleSearchService): DribbbleSearchRemoteDataSource {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DribbbleSearchRemoteDataSource(service).also { INSTANCE = it }
            }
        }
    }
}
