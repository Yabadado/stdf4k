package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readCn
import yabadado.tool.stdfparser.parser.readU1
import yabadado.tool.stdfparser.parser.readU1Array
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.SDR
import java.nio.ByteOrder

internal object SdrParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): SDR {
        val buf = body.toOrderedBuffer(order)
        val headNumber = buf.readU1()
        val siteGroup = buf.readU1()
        val siteCount = buf.readU1().toInt()
        val siteNumbers = buf.readU1Array(siteCount)
        return SDR(
            headNumber = headNumber,
            siteGroup = siteGroup,
            siteNumbers = siteNumbers,
            handlerType = buf.readCn(),
            handlerId = buf.readCn(),
            cardType = buf.readCn(),
            cardId = buf.readCn(),
            loadboardType = buf.readCn(),
            loadboardId = buf.readCn(),
            dibType = buf.readCn(),
            dibId = buf.readCn(),
            cableType = buf.readCn(),
            cableId = buf.readCn(),
            contactorType = buf.readCn(),
            contactorId = buf.readCn(),
            laserType = buf.readCn(),
            laserId = buf.readCn(),
            extraType = buf.readCn(),
            extraId = buf.readCn()
        )
    }
}
