package yabadado.tool.stdfparser.record

/** Site Description Record — describes tester hardware configuration per site group. */
data class SDR(
    val headNumber: UByte,          // HEAD_NUM U*1
    val siteGroup: UByte,           // SITE_GRP U*1
    val siteNumbers: List<UByte>,   // SITE_NUM xU*1 (count prefix + array)
    val handlerType: String = "",   // HAND_TYP C*n
    val handlerId: String = "",     // HAND_ID  C*n
    val cardType: String = "",      // CARD_TYP C*n
    val cardId: String = "",        // CARD_ID  C*n
    val loadboardType: String = "", // LOAD_TYP C*n
    val loadboardId: String = "",   // LOAD_ID  C*n
    val dibType: String = "",       // DIB_TYP  C*n
    val dibId: String = "",         // DIB_ID   C*n
    val cableType: String = "",     // CABL_TYP C*n
    val cableId: String = "",       // CABL_ID  C*n
    val contactorType: String = "", // CONT_TYP C*n
    val contactorId: String = "",   // CONT_ID  C*n
    val laserType: String = "",     // LASR_TYP C*n
    val laserId: String = "",       // LASR_ID  C*n
    val extraType: String = "",     // EXTR_TYP C*n
    val extraId: String = ""        // EXTR_ID  C*n
) : Record(1u, 80u)
