package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readU1
import yabadado.tool.stdfparser.parser.readU4
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.PCR
import java.nio.ByteOrder

internal object PcrParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): PCR {
        val buf = body.toOrderedBuffer(order)
        // retestCount, abortCount, goodCount, functionalCount are independently optional
        // trailing fields per STDF v4 spec — each may be absent from the end of the record.
        // null means "not written by the tester"; 0xFFFFFFFF (UInt.MAX_VALUE) means "unknown".
        return PCR(
            headNumber = buf.readU1(),
            siteNumber = buf.readU1(),
            partCount = buf.readU4(),
            retestCount = if (buf.hasRemaining()) buf.readU4() else null,
            abortCount = if (buf.hasRemaining()) buf.readU4() else null,
            goodCount = if (buf.hasRemaining()) buf.readU4() else null,
            functionalCount = if (buf.hasRemaining()) buf.readU4() else null
        )
    }
}
