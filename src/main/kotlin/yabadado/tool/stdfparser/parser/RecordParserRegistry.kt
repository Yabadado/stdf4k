package yabadado.tool.stdfparser.parser

import yabadado.tool.stdfparser.parser.impl.AtrParser
import yabadado.tool.stdfparser.parser.impl.FarParser
import yabadado.tool.stdfparser.parser.impl.HbrParser
import yabadado.tool.stdfparser.parser.impl.MirParser
import yabadado.tool.stdfparser.parser.impl.MrrParser
import yabadado.tool.stdfparser.parser.impl.PcrParser
import yabadado.tool.stdfparser.parser.impl.PirParser
import yabadado.tool.stdfparser.parser.impl.PrrParser
import yabadado.tool.stdfparser.parser.impl.PtrParser
import yabadado.tool.stdfparser.parser.impl.SbrParser
import yabadado.tool.stdfparser.parser.impl.SdrParser
import yabadado.tool.stdfparser.parser.impl.WirParser

internal object RecordParserRegistry {

    /**
     * All (type, subType) pairs that this library supports, per the STDF v4 spec.
     * Exposed as `internal` for use in [RecordParserRegistryTest] only — not part of the public API.
     */
    internal val expectedKeys: Set<Pair<UByte, UByte>> = setOf(
        0u.toUByte() to 10u.toUByte(),  // FAR
        0u.toUByte() to 20u.toUByte(),  // ATR
        1u.toUByte() to 10u.toUByte(),  // MIR
        1u.toUByte() to 20u.toUByte(),  // MRR
        1u.toUByte() to 30u.toUByte(),  // PCR
        1u.toUByte() to 40u.toUByte(),  // HBR
        1u.toUByte() to 50u.toUByte(),  // SBR
        1u.toUByte() to 80u.toUByte(),  // SDR
        2u.toUByte() to 10u.toUByte(),  // WIR
        5u.toUByte() to 10u.toUByte(),  // PIR
        5u.toUByte() to 20u.toUByte(),  // PRR
        15u.toUByte() to 10u.toUByte(), // PTR
    )

    private val parsers: Map<Pair<UByte, UByte>, RecordParser> = mapOf(
        (0u.toUByte() to 10u.toUByte()) to FarParser,
        (0u.toUByte() to 20u.toUByte()) to AtrParser,
        (1u.toUByte() to 10u.toUByte()) to MirParser,
        (1u.toUByte() to 20u.toUByte()) to MrrParser,
        (1u.toUByte() to 30u.toUByte()) to PcrParser,
        (1u.toUByte() to 40u.toUByte()) to HbrParser,
        (1u.toUByte() to 50u.toUByte()) to SbrParser,
        (1u.toUByte() to 80u.toUByte()) to SdrParser,
        (2u.toUByte() to 10u.toUByte()) to WirParser,
        (5u.toUByte() to 10u.toUByte()) to PirParser,
        (5u.toUByte() to 20u.toUByte()) to PrrParser,
        (15u.toUByte() to 10u.toUByte()) to PtrParser,
    )

    init {
        val missing = expectedKeys - parsers.keys
        val unexpected = parsers.keys - expectedKeys
        check(missing.isEmpty()) { "RecordParserRegistry is missing parsers for: $missing" }
        check(unexpected.isEmpty()) { "RecordParserRegistry has unrecognised entries: $unexpected" }
    }

    fun find(type: UByte, subType: UByte): RecordParser? = parsers[type to subType]
}
