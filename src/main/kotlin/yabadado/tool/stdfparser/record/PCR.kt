package yabadado.tool.stdfparser.record

/**
 * Part Count Record — summary count per head/site.
 *
 * Optional count fields are independently trailing per STDF v4 spec:
 * - `null` means the field was absent from the binary record (tester did not write it).
 * - [UInt.MAX_VALUE] (`0xFFFFFFFF`) means the field was present but the count is unknown.
 */
data class PCR(
    val headNumber: UByte,        // HEAD_NUM U*1
    val siteNumber: UByte,        // SITE_NUM U*1
    val partCount: UInt,          // PART_CNT U*4
    val retestCount: UInt? = null, // RTST_CNT U*4 optional
    val abortCount: UInt? = null,  // ABRT_CNT U*4 optional
    val goodCount: UInt? = null,   // GOOD_CNT U*4 optional
    val functionalCount: UInt? = null // FUNC_CNT U*4 optional
) : Record(1u, 30u)
