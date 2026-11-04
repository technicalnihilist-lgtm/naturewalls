package com.naturewalls.app.data.remote

import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * The official Wikimedia Commons action API.
 *
 * A single generic endpoint is used because every call in NatureWalls is a `generator` query
 * (search or category) that returns `query.pages[].imageinfo`, plus a `continue` block that we
 * feed straight back in for pagination.
 */
interface WikimediaApi {

    @GET("w/api.php")
    suspend fun query(@QueryMap params: Map<String, String>): CommonsResponse
}
