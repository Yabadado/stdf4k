package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readC1
import yabadado.tool.stdfparser.parser.readCn
import yabadado.tool.stdfparser.parser.readU4
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.MRR
import java.nio.ByteOrder

internal object MrrParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): MRR {
        val buf = body.toOrderedBuffer(order)
        return MRR(
            finishTime = buf.readU4(),
            dispositionCode = buf.readC1(),
            userDescription = buf.readCn(),
            execDescription = buf.readCn()
        )
    }
}
