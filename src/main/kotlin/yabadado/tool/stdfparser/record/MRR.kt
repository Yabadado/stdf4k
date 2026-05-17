package yabadado.tool.stdfparser.record

/** Master Results Record — end-of-lot summary. */
data class MRR(
    val finishTime: UInt,           // FINISH_T U*4
    val dispositionCode: Char = ' ', // DISP_COD C*1
    val userDescription: String = "", // USR_DESC C*n
    val execDescription: String = ""  // EXC_DESC C*n
) : Record(1u, 20u)
