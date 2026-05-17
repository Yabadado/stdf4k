package yabadado.tool.stdfparser.record

/**
 * Master Information Record — one per file, describes the test setup.
 * Fields after [stationNumber] are optional trailing fields; absent ones use their defaults.
 */
data class MIR(
    // Required
    val setupTime: UInt,                    // SETUP_T  U*4
    val startTime: UInt,                    // START_T  U*4
    val stationNumber: UByte,              // STAT_NUM U*1
    // Optional C*1 (single char, default ' ')
    val modeCode: Char = ' ',              // MODE_COD
    val retestCode: Char = ' ',            // RTST_COD
    val protectionCode: Char = ' ',        // PROT_COD
    // Optional U*2 (default 0xFFFF = unknown)
    val burnInTime: UShort = UShort.MAX_VALUE, // BURN_TIM
    // Optional C*1
    val commandModeCode: Char = ' ',       // CMOD_COD
    // Optional C*n strings (default "")
    val lotId: String = "",                // LOT_ID
    val partType: String = "",             // PART_TYP
    val nodeName: String = "",             // NODE_NAM
    val testerType: String = "",           // TSTR_TYP
    val jobName: String = "",              // JOB_NAM
    val jobRevision: String = "",          // JOB_REV
    val subLotId: String = "",             // SBLOT_ID
    val operatorName: String = "",         // OPER_NAM
    val execType: String = "",             // EXEC_TYP
    val execVersion: String = "",          // EXEC_VER
    val testCode: String = "",             // TEST_COD
    val testTemperature: String = "",      // TST_TEMP
    val userText: String = "",             // USER_TXT
    val auxFile: String = "",              // AUX_FILE
    val packageType: String = "",          // PKG_TYP
    val familyId: String = "",             // FAMLY_ID
    val dateCode: String = "",             // DATE_COD
    val facilityId: String = "",           // FACIL_ID
    val floorId: String = "",              // FLOOR_ID
    val processId: String = "",            // PROC_ID
    val operationFrequency: String = "",   // OPER_FRQ
    val specName: String = "",             // SPEC_NAM
    val specVersion: String = "",          // SPEC_VER
    val flowId: String = "",               // FLOW_ID
    val setupId: String = "",              // SETUP_ID
    val designRevision: String = "",       // DSGN_REV
    val engineerId: String = "",           // ENG_ID
    val romCode: String = "",              // ROM_COD
    val serialNumber: String = "",         // SERL_NUM
    val supervisorName: String = ""        // SUPR_NAM
) : Record(1u, 10u)
