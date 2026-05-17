package yabadado.tool.stdfparser.record

/**
 * Wafer Information Record — marks the start of a wafer test.
 *
 * [waferId] is an optional trailing field; it defaults to `""` when absent from the binary record.
 */
data class WIR(
    val headNumber: UByte,   // HEAD_NUM U*1
    val siteGroup: UByte,    // SITE_GRP U*1
    val startTime: UInt,     // START_T  U*4
    val waferId: String = "" // WAFER_ID C*n
) : Record(2u, 10u)
