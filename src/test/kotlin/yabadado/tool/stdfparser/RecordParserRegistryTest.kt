package yabadado.tool.stdfparser

import yabadado.tool.stdfparser.parser.RecordParserRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordParserRegistryTest {

    @Test
    fun `registry covers exactly the expected set of STDF record types`() {
        // RecordParserRegistry.init already throws if parsers != expectedKeys,
        // so merely accessing the object validates completeness at runtime.
        // This test makes that guarantee explicit and visible in CI output.
        assertEquals(
            RecordParserRegistry.expectedKeys.size,
            12,
            "Expected 12 supported STDF v4 record types"
        )
    }

    @Test
    fun `all expected type-subtype pairs resolve to a non-null parser`() {
        val missing = RecordParserRegistry.expectedKeys.filter { (type, sub) ->
            RecordParserRegistry.find(type, sub) == null
        }
        assertTrue(missing.isEmpty(), "No parser found for: $missing")
    }

    @Test
    fun `unknown type-subtype pair returns null`() {
        val result = RecordParserRegistry.find(0xFFu.toUByte(), 0xFFu.toUByte())
        assertEquals(null, result, "Unregistered type/subtype should return null")
    }
}
