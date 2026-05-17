package yabadado.tool.stdfparser.record

/**
 * Parametric Test Result Record — one per parametric test execution.
 *
 * **Null semantics:** `null` means the field was absent from the binary record (trailing optional
 * field not written by the tester). STDF sentinel values (e.g., 0xFFFF for unknown bins) are
 * preserved as-is and are NOT converted to `null` — check the spec for each field's sentinel.
 *
 * [testFlag] bit meanings (B*1):
 *   bit0: alarm; bit1: result invalid (not executed) → [result] is `null`; bit2: result unreliable;
 *   bit3: timeout; bit4: not executed (no prior failing tests); bit5: not executed (one fail group);
 *   bit6: test failed; bit7: test passed.
 *   RESULT (R*4) is always present in the binary layout regardless of bit1.
 *
 * [optionalFlag] bit meanings (when the optional section is present):
 *   bit0=resultScaleInvalid, bit1=noLowLimit, bit2=noHighLimit,
 *   bit4=lowLimitLtLoSpec, bit5=highLimitGtHiSpec.
 */
data class PTR(
    val testNumber: UInt,               // TEST_NUM U*4
    val headNumber: UByte,              // HEAD_NUM U*1
    val siteNumber: UByte,              // SITE_NUM U*1
    val testFlag: UByte,                // TEST_FLG B*1
    val paramFlag: UByte,               // PARM_FLG B*1: bit0=scaleError, bit1=driftError, bit2=oscillation, bit3=limitFailed, bit4=sameAsFirstFail, bit5=impliedLimitsUsed, bit6=limitScaledFromSpec
    val result: Float? = null,          // RESULT   R*4  null if TEST_FLG bit1 set
    val testText: String = "",          // TEST_TXT C*n
    val alarmId: String = "",           // ALARM_ID C*n
    val optionalFlag: UByte? = null,    // OPT_FLAG B*1
    val resultScale: Byte? = null,      // RES_SCAL I*1
    val lowLimitScale: Byte? = null,    // LLM_SCAL I*1
    val highLimitScale: Byte? = null,   // HLM_SCAL I*1
    val lowLimit: Float? = null,        // LO_LIMIT R*4
    val highLimit: Float? = null,       // HI_LIMIT R*4
    val units: String = "",             // UNITS    C*n
    val resultFormat: String = "",      // C_RESFMT C*n
    val lowLimitFormat: String = "",    // C_LLMFMT C*n
    val highLimitFormat: String = "",   // C_HLMFMT C*n
    val lowSpec: Float? = null,         // LO_SPEC  R*4
    val highSpec: Float? = null         // HI_SPEC  R*4
) : Record(15u, 10u)
