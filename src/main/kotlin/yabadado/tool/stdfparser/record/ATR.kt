package yabadado.tool.stdfparser.record

/** Audit Trail Record — records program invocations that modified the file. */
data class ATR(
    val modifiedTime: UInt,  // seconds since midnight 1 Jan 1970 UTC (Unix epoch)
    val commandLine: String = ""  // C*n; optional trailing field — absent when ATR body is only 4 bytes
) : Record(0u, 20u)
