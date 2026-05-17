package yabadado.tool.stdfparser.record

/**
 * Wraps any record type not recognised by the parser registry.
 *
 * Not a `data class` because `ByteArray` equality is reference-based by default; the custom
 * [equals]/[hashCode]/[toString] below use [ByteArray.contentEquals]/[ByteArray.contentHashCode]
 * for value semantics.
 */
class UnknownRecord(
    val recordType: UByte,
    val recordSubType: UByte,
    rawBody: ByteArray
) : Record(recordType, recordSubType) {

    val rawBody: ByteArray = rawBody.copyOf()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownRecord) return false
        return recordType == other.recordType &&
            recordSubType == other.recordSubType &&
            rawBody.contentEquals(other.rawBody)
    }

    override fun hashCode(): Int {
        var result = recordType.hashCode()
        result = 31 * result + recordSubType.hashCode()
        result = 31 * result + rawBody.contentHashCode()
        return result
    }

    override fun toString() =
        "UnknownRecord(type=$recordType, subType=$recordSubType, bodySize=${rawBody.size})"
}
