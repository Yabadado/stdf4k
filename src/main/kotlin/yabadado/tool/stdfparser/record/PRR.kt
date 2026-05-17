package yabadado.tool.stdfparser.record

/**
 * Part Results Record — marks the end of testing a single part.
 *
 * [partFlag] bits: bit0=supersedesPrevious, bit1=abnormalEnd, bit2=failed, bit3=noPassFail, bit4=noXY.
 */
data class PRR(
    val headNumber: UByte,         // HEAD_NUM U*1
    val siteNumber: UByte,         // SITE_NUM U*1
    val partFlag: UByte,           // PART_FLG B*1
    val testCount: UShort,         // NUM_TEST U*2
    val hardBin: UShort,           // HARD_BIN U*2
    val softBin: UShort? = null,   // SOFT_BIN U*2 optional (0xFFFF = unknown)
    val xCoord: Short? = null,     // X_COORD  I*2 optional
    val yCoord: Short? = null,     // Y_COORD  I*2 optional
    val testTime: UInt? = null,    // TEST_T   U*4 optional (ms)
    val partId: String = "",       // PART_ID  C*n
    val partText: String = ""      // PART_TXT C*n
) : Record(5u, 20u)
