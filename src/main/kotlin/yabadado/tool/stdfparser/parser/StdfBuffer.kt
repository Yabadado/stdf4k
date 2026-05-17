package yabadado.tool.stdfparser.parser

import java.nio.ByteBuffer

/** Extension helpers for reading STDF binary types from a [ByteBuffer]. */

internal fun ByteBuffer.readU1(): UByte = get().toUByte()
internal fun ByteBuffer.readU2(): UShort = short.toUShort()
internal fun ByteBuffer.readU4(): UInt = int.toUInt()
internal fun ByteBuffer.readI1(): Byte = get()
internal fun ByteBuffer.readI2(): Short = short
internal fun ByteBuffer.readR4(): Float = float

/**
 * C*1 — fixed single-character field (no length prefix).
 * Returns ' ' if the buffer is exhausted.
 */
internal fun ByteBuffer.readC1(): Char =
    if (hasRemaining()) get().toInt().toChar() else ' '

/**
 * C*n — variable-length string: 1-byte length prefix, then that many ASCII bytes.
 * Returns "" if the buffer is exhausted or the length byte is 0.
 */
internal fun ByteBuffer.readCn(): String {
    if (!hasRemaining()) return ""
    val len = get().toUByte().toInt()
    if (len == 0) return ""
    require(remaining() >= len) { "C*n truncated: expected $len bytes, only ${remaining()} remaining" }
    val bytes = ByteArray(len)
    get(bytes)
    return String(bytes, Charsets.US_ASCII)
}

/**
 * Read exactly [count] U*1 values into a list.
 * Throws [IllegalArgumentException] if fewer than [count] bytes remain, preventing silent truncation.
 */
internal fun ByteBuffer.readU1Array(count: Int): List<UByte> {
    require(remaining() >= count) { "U*1 array truncated: expected $count bytes, only ${remaining()} remaining" }
    return List(count) { get().toUByte() }
}
