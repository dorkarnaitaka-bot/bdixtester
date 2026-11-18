package com.jubayerhossain.bdixtester.data.catalog

import com.jubayerhossain.bdixtester.core.model.CatalogAuditSummary
import com.jubayerhossain.bdixtester.core.model.CatalogSourceType
import com.jubayerhossain.bdixtester.core.model.ServerCategory
import com.jubayerhossain.bdixtester.core.model.ServerProtocol
import com.jubayerhossain.bdixtester.core.model.VerificationState
import com.jubayerhossain.bdixtester.core.validation.UrlParser
import com.jubayerhossain.bdixtester.domain.model.ServerEndpoint

object BuiltInCatalog {

    fun builtInIdForIndex(sourceIndex: Int): String = "builtin:$sourceIndex"

    /**
     * Parses [rawLinks] into immutable [ServerEndpoint] records preserving:
     * - all supplied entries in exact original order (`sourceIndex = 0..N-1`)
     * - exact duplicates via unique deterministic `builtin:<sourceIndex>` identity
     * - uncurated metadata rules: hostname fallback, `ServerCategory.UNCATEGORIZED`,
     *   `isFeatured = false`, `VerificationState.UNKNOWN`, and unchanged verbatim `originalUrl`.
     */
    fun parseSourceCatalog(rawLinks: List<String> = FtpLinks.links): List<ServerEndpoint> {
        val result = ArrayList<ServerEndpoint>(rawLinks.size)

        rawLinks.forEachIndexed { index, rawUrl ->
            val parsed = UrlParser.parse(rawUrl)
            val fallbackHost = parsed.host ?: rawUrl
            val displayName = buildDisplayName(fallbackHost, parsed.explicitPort)

            result.add(
                ServerEndpoint(
                    id = builtInIdForIndex(index),
                    name = displayName,
                    originalUrl = rawUrl,
                    host = fallbackHost,
                    explicitPort = parsed.explicitPort,
                    protocol = if (parsed.isValid) parsed.protocol else ServerProtocol.UNSUPPORTED,
                    category = ServerCategory.UNCATEGORIZED,
                    isFeatured = false,
                    sourceType = CatalogSourceType.BUILT_IN,
                    sourceIndex = index,
                    verificationState = VerificationState.UNKNOWN,
                    lastReviewedAtEpochMs = null,
                    description = null,
                    sourceAttribution = "User-supplied FtpLinks source (entry #$index)",
                    speedTestUrl = null,
                    speedTestMaxBytes = null,
                    isPrivateIp = parsed.isPrivateIp
                )
            )
        }
        return result
    }

    /**
     * Backward-compatible alias that returns all preserved source entries in source order.
     */
    fun parseRuntimeCatalog(rawLinks: List<String> = FtpLinks.links): List<ServerEndpoint> =
        parseSourceCatalog(rawLinks)

    fun computeAuditSummary(rawLinks: List<String> = FtpLinks.links): CatalogAuditSummary {
        val parsedList = rawLinks.map { UrlParser.parse(it) }
        val uniqueCount = rawLinks.toSet().size
        val duplicateCount = rawLinks.size - uniqueCount
        val validEntries = parsedList.filter { it.isValid }

        return CatalogAuditSummary(
            expectedSourceCount = FtpLinks.EXPECTED_SOURCE_COUNT,
            originalEndpointCount = rawLinks.size,
            discrepancyCount = FtpLinks.EXPECTED_SOURCE_COUNT - rawLinks.size,
            successfullyParsedCount = validEntries.size,
            uniqueEndpointCount = uniqueCount,
            exactDuplicateCount = duplicateCount,
            invalidUrlCount = parsedList.count { !it.isValid },
            httpEndpointCount = validEntries.count { it.protocol == ServerProtocol.HTTP },
            httpsEndpointCount = validEntries.count { it.protocol == ServerProtocol.HTTPS },
            ftpEndpointCount = validEntries.count { it.protocol == ServerProtocol.FTP },
            ftpsEndpointCount = validEntries.count { it.protocol == ServerProtocol.FTPS },
            unsupportedSchemeCount = parsedList.count {
                it.isValid && it.protocol == ServerProtocol.UNSUPPORTED
            },
            hostnameEndpointCount = validEntries.count { !it.isIpv4 },
            publicIpv4EndpointCount = validEntries.count { it.isIpv4 && !it.isPrivateIp },
            privateIpv4EndpointCount = validEntries.count { it.isPrivateIp },
            explicitPortCount = validEntries.count { it.explicitPort != null },
            nonRootPathCount = validEntries.count { it.hasNonRootPath },
            manualReviewCount = parsedList.count { it.requiresManualReview }
        )
    }

    fun buildDisplayName(host: String, explicitPort: Int?): String {
        return if (explicitPort != null) "$host:$explicitPort" else host
    }
}
