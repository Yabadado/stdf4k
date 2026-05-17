package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readU1
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.FAR
import java.nio.ByteOrder

internal object FarParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): FAR {
        val buf = body.toOrderedBuffer(order)
        return FAR(
            cpuType = buf.readU1(),
            stdfVersion = buf.readU1()
        )
    }
}
