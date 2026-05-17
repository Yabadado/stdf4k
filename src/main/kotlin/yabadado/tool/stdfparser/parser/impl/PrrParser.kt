package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readCn
import yabadado.tool.stdfparser.parser.readI2
import yabadado.tool.stdfparser.parser.readU1
import yabadado.tool.stdfparser.parser.readU2
import yabadado.tool.stdfparser.parser.readU4
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.PRR
import java.nio.ByteOrder

internal object PrrParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): PRR {
        val buf = body.toOrderedBuffer(order)
        // softBin…partText are independently optional trailing fields per STDF v4 spec —
        // null means "not written by the tester". If a field is absent, all subsequent fields
        // are also absent. readCn() returns "" when the buffer is exhausted.
        return PRR(
            headNumber = buf.readU1(),
            siteNumber = buf.readU1(),
            partFlag = buf.readU1(),
            testCount = buf.readU2(),
            hardBin = buf.readU2(),
            softBin = if (buf.hasRemaining()) buf.readU2() else null,
            xCoord = if (buf.hasRemaining()) buf.readI2() else null,
            yCoord = if (buf.hasRemaining()) buf.readI2() else null,
            testTime = if (buf.hasRemaining()) buf.readU4() else null,
            partId = buf.readCn(),
            partText = buf.readCn()
        )
    }
}
