package yabadado.tool.stdfparser.record

/** Hardware Bin Record — bin count per hardware bin. */
data class HBR(
    val headNumber: UByte,              // HEAD_NUM U*1
    val siteNumber: UByte,              // SITE_NUM U*1
    val binNumber: UShort,              // HBIN_NUM U*2
    val binCount: UInt,                 // HBIN_CNT U*4
    val passFailIndicator: Char = ' ',  // HBIN_PF  C*1 ('P', 'F', or ' ')
    val binName: String = ""            // HBIN_NAM C*n
) : Record(1u, 40u)
