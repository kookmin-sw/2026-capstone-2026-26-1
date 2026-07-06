package com.example.passedpath.feature.care.presentation.deeplink

import android.net.Uri
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

object CareInviteDeepLinkParser {
    fun extractInviteCode(uri: Uri?): String? {
        return extractInviteCode(uri?.toString())
    }

    fun extractInviteCode(rawUrl: String?): String? {
        val trimmedUrl = rawUrl?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val uri = runCatching { URI(trimmedUrl) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase(Locale.US)
        val host = uri.host?.lowercase(Locale.US)

        if (scheme != CareInviteScheme ||
            host != CareInviteHost ||
            uri.path != CareInvitePath
        ) {
            return null
        }

        return uri.rawQuery
            ?.split("&")
            ?.firstNotNullOfOrNull { queryPart ->
                val parts = queryPart.split("=", limit = 2)
                val key = parts.getOrNull(0)?.urlDecode()
                if (key != CareInviteCodeQueryKey) {
                    null
                } else {
                    parts.getOrNull(1)
                        ?.urlDecode()
                        ?.trim()
                        ?.takeIf(String::isNotEmpty)
                }
            }
    }

    private fun String.urlDecode(): String {
        return URLDecoder.decode(this, StandardCharsets.UTF_8.name())
    }

    private const val CareInviteScheme = "https"
    private const val CareInviteHost = "passedpath.site"
    private const val CareInvitePath = "/care-relationship/invite"
    private const val CareInviteCodeQueryKey = "inviteCode"
}
