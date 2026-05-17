package yabadado.tool.stdfparser.record

/** File Attributes Record — always the first record; establishes byte order. */
data class FAR(
    val cpuType: UByte,     // 1 = big-endian (Sun/Motorola), 2 = little-endian (Intel/x86)
    val stdfVersion: UByte  // always 4
) : Record(0u, 10u)
