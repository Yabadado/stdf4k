package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readCn
import yabadado.tool.stdfparser.parser.readU4
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.ATR
import java.nio.ByteOrder

internal object AtrParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): ATR {
        val buf = body.toOrderedBuffer(order)
        return ATR(
            modifiedTime = buf.readU4(),
            commandLine = buf.readCn()
        )
    }
}
