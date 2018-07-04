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

import android.text.TextUtils

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.io.IOException
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.regex.Matcher
import java.util.regex.Pattern

import io.plaidapp.core.dribbble.data.api.model.Images
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.api.model.User
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * Dribbble API does not have a search endpoint so we have to do gross things :(
 */
class DribbbleSearchConverter private constructor() : Converter<ResponseBody, List<Shot>> {

    /** Factory for creating converter. We only care about decoding responses.  */
    class Factory : Converter.Factory() {

        override fun responseBodyConverter(
            type: Type?,
            annotations: Array<Annotation>?,
            retrofit: Retrofit?
        ): Converter<ResponseBody, *>? {
            return INSTANCE
        }
    }

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): List<Shot> {
        val shotElements = Jsoup.parse(value.string(), HOST).select("li[id^=screenshot]")
        val shots = ArrayList<Shot>(shotElements.size)
        for (element in shotElements) {
            val shot = parseShot(element, DATE_FORMAT)
            if (shot != null) {
                shots.add(shot)
            }
        }
        return shots
    }

    companion object {
        internal val INSTANCE = DribbbleSearchConverter()

        private val HOST = "https://dribbble.com"
        private val PATTERN_PLAYER_ID = Pattern.compile("users/(\\d+?)/", Pattern.DOTALL)
        private val DATE_FORMAT = SimpleDateFormat("MMMM d, yyyy")

        private fun parseShot(element: Element, dateFormat: SimpleDateFormat): Shot? {
            val descriptionBlock = element.select("a.dribbble-over").first()
            // API responses wrap description in a <p> tag. Do the same for consistent display.
            var description = descriptionBlock.select("span.comment").text().trim { it <= ' ' }
            if (!TextUtils.isEmpty(description)) {
                description = "<p>$description</p>"
            }
            var imgUrl = element.select("img").first().attr("src")
            if (imgUrl.contains("_teaser.")) {
                imgUrl = imgUrl.replace("_teaser.", ".")
            }
            val createdAt: Date? =  try {
                dateFormat.parse(descriptionBlock.select("em.timestamp").first().text())
            } catch (e: ParseException) {
                null
            }

            return Shot(
                id = java.lang.Long.parseLong(element.id().replace("screenshot-", "")),
                html_url = HOST + element.select("a.dribbble-link").first().attr("href"),
                title = descriptionBlock.select("strong").first().text(),
                description = description,
                images = Images(normal = imgUrl),
                animated = element.select("div.gif-indicator").first() != null,
                created_at = createdAt,
                likes_count = java.lang.Long.parseLong(
                        element.select("li.fav").first().child(0).text()
                            .replace(",".toRegex(), "")
                    ),
                comments_count = java.lang.Long.parseLong(
                        element.select("li.cmnt").first().child(0).text().replace(
                            ",".toRegex(),
                            ""
                        )),
                views_count =
                    java.lang.Long.parseLong(
                        element.select("li.views").first().child(0)
                            .text().replace(",".toRegex(), "")
                    ),
                user = parsePlayer(element.select("h2").first())
            )
        }

        private fun parsePlayer(element: Element): User {
            val userBlock = element.select("a.url").first()
            var avatarUrl = userBlock.select("img.photo").first().attr("src")
            if (avatarUrl.contains("/mini/")) {
                avatarUrl = avatarUrl.replace("/mini/", "/normal/")
            }
            val matchId = PATTERN_PLAYER_ID.matcher(avatarUrl)
            var id: Long = -1L
            if (matchId.find() && matchId.groupCount() == 1) {
                id = java.lang.Long.parseLong(matchId.group(1))
            }
            val slashUsername = userBlock.attr("href")
            val username = slashUsername.substring(1)
            return User(
                id = id,
                name = userBlock.text(),
                username =  username,
                html_url = HOST + slashUsername,
                avatar_url = avatarUrl,
                pro = element.select("span.badge-pro").size > 0
            )
        }
    }
}
