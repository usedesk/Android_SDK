package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

class ChangeRatingResponse {
    var rating: Rating? = null

    class Rating {
        var positive: Int? = null
        var negative: Int? = null
    }
}