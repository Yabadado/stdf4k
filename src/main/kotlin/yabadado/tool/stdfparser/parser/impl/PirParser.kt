package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readU1
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.PIR
import java.nio.ByteOrder

internal object PirParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): PIR {
        val buf = body.toOrderedBuffer(order)
        return PIR(
            headNumber = buf.readU1(),
            siteNumber = buf.readU1()
        )
    }
}
