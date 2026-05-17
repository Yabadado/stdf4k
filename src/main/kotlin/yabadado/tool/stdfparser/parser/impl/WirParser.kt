package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readCn
import yabadado.tool.stdfparser.parser.readU1
import yabadado.tool.stdfparser.parser.readU4
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.WIR
import java.nio.ByteOrder

internal object WirParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): WIR {
        val buf = body.toOrderedBuffer(order)
        return WIR(
            headNumber = buf.readU1(),
            siteGroup = buf.readU1(),
            startTime = buf.readU4(),
            waferId = buf.readCn()
        )
    }
}
