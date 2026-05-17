package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readC1
import yabadado.tool.stdfparser.parser.readCn
import yabadado.tool.stdfparser.parser.readU1
import yabadado.tool.stdfparser.parser.readU2
import yabadado.tool.stdfparser.parser.readU4
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.HBR
import java.nio.ByteOrder

internal object HbrParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): HBR {
        val buf = body.toOrderedBuffer(order)
        return HBR(
            headNumber = buf.readU1(),
            siteNumber = buf.readU1(),
            binNumber = buf.readU2(),
            binCount = buf.readU4(),
            passFailIndicator = buf.readC1(),
            binName = buf.readCn()
        )
    }
}
