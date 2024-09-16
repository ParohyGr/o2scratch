package com.parohy.outwo.core

import okhttp3.*
import okhttp3.Headers.Companion.headersOf
import okio.IOException

fun httpGet(url: HttpUrl, headers: Headers = headersOf()) = request(url, headers) { get() }

fun request(url: HttpUrl,
  headers: Headers = headersOf(),
  tag: String? = null,
  config: Request.Builder.() -> Request.Builder): Request =
  Request.Builder().url(url).headers(headers).tag(tag).config().build()

fun <V> OkHttpClient.execute(request: Request, parse: (Json) -> V): Result<V> =
  call(request).map {
    it.use { response ->
      parse(parseJsonTree(response.body!!.charStream()))
    }
  }

private fun OkHttpClient.call(request: Request) =
  try {
    val response = newCall(request).execute()
    if (response.isSuccessful)
      Result.success(response)
    else
      Result.failure(IOException(Throwable(response.message)))
  } catch (e: Exception) {
    Result.failure(e)
  }