package com.example.passedpath.feature.care.data.remote.datasource

import com.example.passedpath.feature.care.data.remote.mapper.toCareDependentLocationStreamEventOrNull
import com.example.passedpath.feature.care.domain.model.CareDependentLocationStreamEvent
import java.io.IOException
import okhttp3.CacheControl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener

interface CareDependentLocationStreamRemoteDataSource {
    fun connect(
        listener: CareDependentLocationStreamListener
    ): CareDependentLocationStreamConnection
}

interface CareDependentLocationStreamListener {
    fun onEvent(event: CareDependentLocationStreamEvent)
    fun onFailure(throwable: Throwable)
}

interface CareDependentLocationStreamConnection {
    fun cancel()
}

class OkHttpCareDependentLocationStreamRemoteDataSource(
    private val eventSourceFactory: EventSource.Factory,
    private val baseUrl: String
) : CareDependentLocationStreamRemoteDataSource {
    override fun connect(
        listener: CareDependentLocationStreamListener
    ): CareDependentLocationStreamConnection {
        val request = Request.Builder()
            .url(streamUrl())
            .header("Accept", "text/event-stream")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .build()

        val eventSource = eventSourceFactory.newEventSource(
            request,
            object : EventSourceListener() {
                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    data.toCareDependentLocationStreamEventOrNull(type)?.let(listener::onEvent)
                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: Response?
                ) {
                    listener.onFailure(
                        t ?: IOException("Care dependent location stream failed: ${response?.code}")
                    )
                }
            }
        )

        return object : CareDependentLocationStreamConnection {
            override fun cancel() {
                eventSource.cancel()
            }
        }
    }

    private fun streamUrl() = baseUrl.toHttpUrl()
        .newBuilder()
        .encodedPath(StreamPath)
        .build()

    private companion object {
        const val StreamPath = "/api/care/dependents/stream"
    }
}
