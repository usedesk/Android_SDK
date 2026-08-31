package ru.usedesk.chat_sdk.data.repository.api.loader

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse
import ru.usedesk.chat_sdk.entity.UsedeskRating

class RatingResponseConverterTest {

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    @Test
    fun `new rating keeps arbitrary option count and server order`() {
        val payload = payload(
            """
            {
              "buttons_csi": {
                "button_type": "action",
                "nps_type": "three_point",
                "icon_type": "stars",
                "data": [
                  {"label": "low", "id": "low-id"},
                  {"label": "middle", "id": "middle-id"},
                  {"label": "high", "id": "high-id"}
                ]
              },
              "userRating": "middle-id"
            }
            """
        )

        val rating = RatingResponseConverter.convert(payload)!!

        assertEquals(UsedeskRating.Appearance.STARS, rating.appearance)
        assertEquals(listOf("low-id", "middle-id", "high-id"), rating.options.map { it.id })
        assertEquals(listOf("low", "middle", "high"), rating.options.map { it.label })
        assertEquals(listOf("low-id", "middle-id", "high-id"), rating.options.map { it.data })
        assertEquals(listOf(null, null, null), rating.options.map { it.reaction })
        assertEquals("middle-id", rating.selectedId)
    }

    @Test
    fun `new rating accepts numeric ids and rating`() {
        val payload = payload(
            """
            {
              "buttons": [{"type":"action", "icon":"like", "data":"GOOD_CHAT"}],
              "buttons_csi": {
                "button_type": "action",
                "nps_type": "five_point",
                "icon_type": "stars",
                "data": [
                  {"label":"very_bad", "id":14},
                  {"label":"bad", "id":13},
                  {"label":"normal", "id":12},
                  {"label":"good", "id":11},
                  {"label":"very_good", "id":10}
                ]
              },
              "userRating": 12
            }
            """
        )

        val rating = RatingResponseConverter.convert(payload)!!

        assertEquals(listOf("14", "13", "12", "11", "10"), rating.options.map { it.id })
        assertEquals("12", rating.selectedId)
    }

    @Test
    fun `legacy feedback becomes current android dislike-like rating`() {
        val payload = payload(
            """
            {
              "buttons": [
                {"type":"action", "icon":"other", "data":"OPEN_URL"},
                {"type":"action", "icon":"like", "data":"GOOD_CHAT"}
              ]
            }
            """
        )

        val rating = RatingResponseConverter.convert(payload)!!

        assertEquals(UsedeskRating.Appearance.REACTIONS, rating.appearance)
        assertEquals(listOf("3", "1"), rating.options.map { it.id })
        assertEquals(listOf("dislike", "like"), rating.options.map { it.label })
        assertEquals(listOf("DISLIKE", "LIKE"), rating.options.map { it.data })
        assertEquals(
            listOf(
                UsedeskRating.Option.Reaction.DISLIKE,
                UsedeskRating.Option.Reaction.LIKE
            ),
            rating.options.map { it.reaction }
        )
    }

    @Test
    fun `ordinary legacy action buttons do not become rating`() {
        val payload = payload(
            """
            {"buttons":[{"type":"action", "icon":"link", "data":"OPEN_URL"}]}
            """
        )

        assertNull(RatingResponseConverter.convert(payload))
    }

    @Test
    fun `malformed new rating falls back to legacy without showing a partial scale`() {
        val rating = RatingResponseConverter.convert(
            payload(
                """
                {
                  "buttons": [{"type":"action", "icon":"like", "data":"GOOD_CHAT"}],
                  "buttons_csi": {
                    "button_type":"action",
                    "nps_type":"five_point",
                    "icon_type":"stars",
                    "data":[
                      {"label":"very_bad", "id":14},
                      {"label":"bad"}
                    ]
                  }
                }
                """
            )
        )!!

        assertEquals(UsedeskRating.Appearance.REACTIONS, rating.appearance)
        assertEquals(listOf("3", "1"), rating.options.map(UsedeskRating.Option::id))
    }

    @Test
    fun `legacy rating without buttons still restores selected reaction`() {
        val rating = RatingResponseConverter.convert(payload("""{"userRating":"LIKE"}"""))!!

        assertEquals("1", rating.selectedId)
    }

    @Test
    fun `unknown completed rating does not reopen legacy fallback`() {
        val payload = payload(
            """
            {
              "buttons": [
                {"type":"action", "icon":"like", "data":"GOOD_CHAT"},
                {"type":"action", "icon":"dislike", "data":"BAD_CHAT"}
              ],
              "userRating":"11"
            }
            """
        )

        assertNull(RatingResponseConverter.convert(payload))
    }

    @Test
    fun `unknown completed rating does not reopen new scale`() {
        val payload = payload(
            """
            {
              "buttons_csi": {
                "button_type":"action",
                "nps_type":"five_point",
                "icon_type":"stars",
                "data":[
                  {"label":"very_bad", "id":14},
                  {"label":"very_good", "id":10}
                ]
              },
              "userRating":"999"
            }
            """
        )

        assertNull(RatingResponseConverter.convert(payload))
    }

    @Test
    fun `feedback request serializes server option id as data`() {
        val json = gson.toJson(
            SocketRequest.Rating(messageId = "message-42", ratingData = "14")
        )

        val root = gson.fromJson(json, Map::class.java)
        val payload = root["payload"] as Map<*, *>
        assertEquals("@@server/chat/CALLBACK", root["type"])
        assertEquals("action", payload["type"])
        assertEquals("14", payload["data"])
        assertEquals("message-42", payload["message_id"])
        assertTrue(payload.keys.containsAll(listOf("type", "data", "message_id")))
    }

    @Test
    fun `legacy feedback request keeps its original callback value`() {
        val json = gson.toJson(
            SocketRequest.Rating(messageId = "message-42", ratingData = "LIKE")
        )

        val root = gson.fromJson(json, Map::class.java)
        val payload = root["payload"] as Map<*, *>
        assertEquals("LIKE", payload["data"])
    }

    private fun payload(json: String): SocketResponse.AddMessage.Message.Payload =
        gson.fromJson(json, SocketResponse.AddMessage.Message.Payload::class.java)
}
