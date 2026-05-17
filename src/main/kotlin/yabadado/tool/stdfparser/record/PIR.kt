package yabadado.tool.stdfparser.record

/** Part Information Record — marks the start of testing a single part. */
data class PIR(
    val headNumber: UByte, // HEAD_NUM U*1
    val siteNumber: UByte  // SITE_NUM U*1
) : Record(5u, 10u)
