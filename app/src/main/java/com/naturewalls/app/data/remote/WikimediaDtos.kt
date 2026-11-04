package com.naturewalls.app.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response envelope of the Wikimedia Commons action API.
 *
 * We always call the API with `formatversion=2`, where `query.pages` is a JSON array.
 */
data class CommonsResponse(
    @SerializedName("batchcomplete") val batchComplete: Boolean? = null,
    @SerializedName("continue") val continueParams: Map<String, String>? = null,
    @SerializedName("query") val query: CommonsQuery? = null,
    @SerializedName("error") val error: CommonsError? = null
)

data class CommonsQuery(
    @SerializedName("pages") val pages: List<CommonsPage>? = null
)

data class CommonsPage(
    @SerializedName("pageid") val pageId: Long? = null,
    @SerializedName("ns") val namespace: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("missing") val missing: Boolean? = null,
    @SerializedName("imageinfo") val imageInfo: List<CommonsImageInfo>? = null
)

data class CommonsImageInfo(
    @SerializedName("url") val url: String? = null,
    @SerializedName("descriptionurl") val descriptionUrl: String? = null,
    @SerializedName("descriptionshorturl") val descriptionShortUrl: String? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null,
    @SerializedName("size") val size: Long? = null,
    @SerializedName("mime") val mime: String? = null,
    @SerializedName("thumburl") val thumbUrl: String? = null,
    @SerializedName("thumbwidth") val thumbWidth: Int? = null,
    @SerializedName("thumbheight") val thumbHeight: Int? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("extmetadata") val extMetadata: Map<String, ExtMetadataValue>? = null
)

data class ExtMetadataValue(
    @SerializedName("value") val value: String? = null,
    @SerializedName("source") val source: String? = null
)

data class CommonsError(
    @SerializedName("code") val code: String? = null,
    @SerializedName("info") val info: String? = null
)
