package yabadado.tool.stdfparser.parser

import yabadado.tool.stdfparser.record.Record
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal interface RecordParser {
    /**
     * Parses [body] bytes into a typed [Record].
     *
     * @throws IllegalArgumentException if [body] is truncated or otherwise malformed.
     * Callers are expected to catch this and fall back to [yabadado.tool.stdfparser.record.UnknownRecord].
     */
    fun parse(body: ByteArray, order: ByteOrder): Record
}

internal fun ByteArray.toOrderedBuffer(order: ByteOrder): ByteBuffer =
    ByteBuffer.wrap(this).order(order)
