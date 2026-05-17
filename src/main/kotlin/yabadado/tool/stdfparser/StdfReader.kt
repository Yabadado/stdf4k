package yabadado.tool.stdfparser

import org.slf4j.LoggerFactory
import yabadado.tool.stdfparser.parser.RecordParserRegistry
import yabadado.tool.stdfparser.record.FAR
import yabadado.tool.stdfparser.record.Record
import yabadado.tool.stdfparser.record.UnknownRecord
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Streams STDF v4 binary records from [inputStream].
 *
 * Byte order is auto-detected from the FAR header length field (always 2 bytes),
 * then confirmed via FAR.cpuType: 1 = BIG_ENDIAN (Sun/Motorola), 2 = LITTLE_ENDIAN (Intel).
 * All records are decoded lazily — only one record's bytes are held in memory at a time.
 */
class StdfReader(private val inputStream: InputStream) {

    private val log = LoggerFactory.getLogger(StdfReader::class.java)

    fun readRecords(): Sequence<Record> = sequence {
        val stream = inputStream.buffered()
        var offset = 0L

        val firstHeader = stream.readNBytes(4)
        if (firstHeader.size < 4) return@sequence

        // Auto-detect byte order from FAR header: its body length is always exactly 2.
        val beLen = ((firstHeader[0].toInt() and 0xFF) shl 8) or (firstHeader[1].toInt() and 0xFF)
        val leLen = ((firstHeader[1].toInt() and 0xFF) shl 8) or (firstHeader[0].toInt() and 0xFF)
        var byteOrder = when {
            beLen == 2 -> ByteOrder.BIG_ENDIAN
            leLen == 2 -> ByteOrder.LITTLE_ENDIAN
            else -> {
                log.error(
                    "Cannot detect byte order from FAR header bytes " +
                    "[0x%02X, 0x%02X]: neither interpretation yields length=2; aborting".format(
                        firstHeader[0].toInt() and 0xFF, firstHeader[1].toInt() and 0xFF
                    )
                )
                return@sequence
            }
        }

        // Emit first record (FAR), then confirm byte order via cpuType.
        val (firstRecord, firstConsumed) = yieldRecord(firstHeader, stream, byteOrder, offset)
        offset += 4 + firstConsumed
        if (firstRecord == null) return@sequence
        if (firstRecord is FAR) {
            val cpuByteOrder = when (firstRecord.cpuType.toInt()) {
                1 -> ByteOrder.BIG_ENDIAN    // Sun / Motorola (Sparc)
                2 -> ByteOrder.LITTLE_ENDIAN // Intel / x86
                else -> null
            }
            if (cpuByteOrder == null) {
                log.warn("Unknown FAR cpuType=${firstRecord.cpuType}; keeping auto-detected byte order ($byteOrder)")
            } else if (cpuByteOrder != byteOrder) {
                log.warn(
                    "FAR.cpuType=${firstRecord.cpuType} declares $cpuByteOrder but auto-detection yielded $byteOrder " +
                    "@offset=$offset; trusting FAR.cpuType"
                )
                byteOrder = cpuByteOrder
            } else {
                byteOrder = cpuByteOrder
            }
        }
        yield(firstRecord)

        while (true) {
            val header = stream.readNBytes(4)
            if (header.size < 4) break
            val (record, consumed) = yieldRecord(header, stream, byteOrder, offset)
            offset += 4 + consumed
            record?.let { yield(it) } ?: break
        }
    }

    /**
     * Returns (record, actualBodyBytesConsumed).
     * Record is null when the body was truncated — the caller should stop iteration.
     * The consumed count is always the true bytes read from the stream, even on truncation.
     */
    private fun yieldRecord(
        header: ByteArray,
        stream: InputStream,
        byteOrder: ByteOrder,
        offset: Long
    ): Pair<Record?, Int> {
        val buf = ByteBuffer.wrap(header).order(byteOrder)
        val bodyLength = buf.short.toUShort().toInt()
        val type = buf.get().toUByte()
        val subType = buf.get().toUByte()

        val body = stream.readNBytes(bodyLength)
        if (body.size < bodyLength) {
            log.warn("@offset=$offset truncated body for record type=$type sub=$subType: expected $bodyLength bytes, got ${body.size}")
            return Pair(null, body.size)
        }

        val record = runCatching {
            RecordParserRegistry.find(type, subType)?.parse(body, byteOrder)
                ?: UnknownRecord(type, subType, body)
        }.getOrElse { ex ->
            log.warn("@offset=$offset failed to parse record type=$type sub=$subType (${ex::class.simpleName}: ${ex.message})")
            UnknownRecord(type, subType, body)
        }
        return Pair(record, bodyLength)
    }
}

/**
 * Returns a cold, lazy [Sequence] of STDF v4 [Record]s decoded from this file.
 *
 * The file is opened on first iteration and closed automatically when the sequence is either
 * fully consumed or the iterator goes out of scope (via the coroutine `finally` block inside
 * `sequence {}`).  Calling this function multiple times opens a fresh file handle each time.
 *
 * The sequence is **not thread-safe** — do not share an iterator across threads, and do not
 * read from the underlying file independently while iteration is in progress.
 *
 * Unknown or unparseable records are returned as [UnknownRecord] and never cause an exception;
 * parse warnings are emitted via [org.slf4j.Logger] at WARN level.
 */
fun File.asStdfSequence(): Sequence<Record> = sequence {
    inputStream().use { stream ->
        yieldAll(StdfReader(stream).readRecords())
    }
}

/**
 * Returns a cold, lazy [Sequence] of STDF v4 [Record]s decoded from this stream.
 *
 * The stream is closed automatically when the sequence is fully consumed or abandoned.
 * The caller must not close or read the stream independently after calling this function.
 *
 * The sequence is **not thread-safe** — do not share an iterator across threads, and do not
 * read from the underlying stream independently while iteration is in progress.
 *
 * Unknown or unparseable records are returned as [UnknownRecord] and never cause an exception;
 * parse warnings are emitted via [org.slf4j.Logger] at WARN level.
 */
fun InputStream.asStdfSequence(): Sequence<Record> = sequence {
    use { stream ->
        yieldAll(StdfReader(stream).readRecords())
    }
}
