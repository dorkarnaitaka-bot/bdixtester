package com.jubayerhossain.bdixtester

import com.jubayerhossain.bdixtester.core.model.ServerCategory
import com.jubayerhossain.bdixtester.core.model.VerificationState
import com.jubayerhossain.bdixtester.data.catalog.BuiltInCatalog
import com.jubayerhossain.bdixtester.data.catalog.FtpLinks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInCatalogAuditTest {

    @Test
    fun suppliedFtpLinksCatalogIsPreservedVerbatimAndDocumentsCountDiscrepancy() {
        val summary = BuiltInCatalog.computeAuditSummary(FtpLinks.links)
        val sourceEntries = BuiltInCatalog.parseSourceCatalog(FtpLinks.links)

        assertEquals(369, FtpLinks.links.size)
        assertEquals(369, summary.originalEndpointCount)
        assertEquals(365, summary.uniqueEndpointCount)
        assertEquals(4, summary.exactDuplicateCount)
        assertEquals(371, summary.expectedSourceCount)
        assertEquals(2, summary.discrepancyCount)

        assertEquals(369, sourceEntries.size)
        sourceEntries.forEachIndexed { index, endpoint ->
            assertEquals("builtin:$index", endpoint.id)
            assertEquals(index, endpoint.sourceIndex)
            assertEquals(FtpLinks.links[index], endpoint.originalUrl)
            assertEquals(ServerCategory.UNCATEGORIZED, endpoint.category)
            assertFalse(endpoint.isFeatured)
            assertEquals(VerificationState.UNKNOWN, endpoint.verificationState)
        }

        // Verify unusual path syntax preserved verbatim (double slash at index 116)
        assertEquals("http://172.27.27.26//2TB4", sourceEntries[116].originalUrl)

        // Verify exact duplicates remain independent auditable records
        assertEquals("http://10.16.100.202/ftps10", sourceEntries[45].originalUrl)
        assertEquals("http://10.16.100.202/ftps10", sourceEntries[81].originalUrl)
        assertEquals("builtin:45", sourceEntries[45].id)
        assertEquals("builtin:81", sourceEntries[81].id)
    }

    @Test
    fun distinctPortsAndPathsOnSameHostAreNeverCollapsed() {
        val sample = listOf(
            "http://example.local/",
            "http://example.local:8080/",
            "http://example.local/movies/",
            "http://example.local/"
        )
        val summary = BuiltInCatalog.computeAuditSummary(sample)
        val parsed = BuiltInCatalog.parseSourceCatalog(sample)

        assertEquals(4, summary.originalEndpointCount)
        assertEquals(3, summary.uniqueEndpointCount)
        assertEquals(1, summary.exactDuplicateCount)
        assertEquals(4, parsed.size)
        assertTrue(parsed.any { it.originalUrl == "http://example.local:8080/" })
        assertTrue(parsed.any { it.originalUrl == "http://example.local/movies/" })
    }
}
