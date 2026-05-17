package yabadado.tool.stdfparser.record

/** Software Bin Record — bin count per software bin. */
data class SBR(
    val headNumber: UByte,              // HEAD_NUM U*1
    val siteNumber: UByte,              // SITE_NUM U*1
    val binNumber: UShort,              // SBIN_NUM U*2
    val binCount: UInt,                 // SBIN_CNT U*4
    val passFailIndicator: Char = ' ',  // SBIN_PF  C*1 ('P', 'F', or ' ')
    val binName: String = ""            // SBIN_NAM C*n
) : Record(1u, 50u)
