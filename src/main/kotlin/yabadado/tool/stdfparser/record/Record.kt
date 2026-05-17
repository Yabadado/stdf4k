package yabadado.tool.stdfparser.record

/**
 * Base sealed class for all STDF v4 records.
 *
 * Each record is identified by a ([type], [subType]) pair encoded in the 4-byte binary header.
 * Known record types are concrete `data class` subclasses; unrecognised or unparseable records
 * are represented as [UnknownRecord], which preserves the raw bytes for inspection.
 */
sealed class Record(val type: UByte, val subType: UByte)
