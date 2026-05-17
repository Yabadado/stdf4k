package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readCn
import yabadado.tool.stdfparser.parser.readI1
import yabadado.tool.stdfparser.parser.readR4
import yabadado.tool.stdfparser.parser.readU1
import yabadado.tool.stdfparser.parser.readU4
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.PTR
import java.nio.ByteOrder

internal object PtrParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): PTR {
        val buf = body.toOrderedBuffer(order)
        val testNumber = buf.readU4()
        val headNumber = buf.readU1()
        val siteNumber = buf.readU1()
        val testFlag = buf.readU1()
        val paramFlag = buf.readU1()
        // STDF v4 spec §PTR: RESULT (R*4) occupies exactly 4 bytes and is ALWAYS present in the
        // binary stream, regardless of TEST_FLG.  TEST_FLG bit1 = 1 means the value is logically
        // invalid (test not executed / no result), so we consume the bytes but expose null.
        // Do NOT skip or conditionally read this field — all subsequent offsets depend on it.
        val resultInvalid = (testFlag.toInt() and 0x02) != 0
        require(buf.remaining() >= 4) {
            "PTR RESULT field missing: need 4 bytes, only ${buf.remaining()} remaining"
        }
        val result = buf.readR4().let { if (resultInvalid) null else it }
        val testText = buf.readCn()
        val alarmId = buf.readCn()
        if (!buf.hasRemaining()) return PTR(testNumber, headNumber, siteNumber, testFlag, paramFlag, result, testText, alarmId)
        val optionalFlag = buf.readU1()
        // Once OPT_FLAG is declared, the 3 scale bytes + 2 R*4 limits (11 bytes) must all be present.
        require(buf.remaining() >= 11) {
            "PTR optional section truncated: need ≥11 bytes after OPT_FLAG, got ${buf.remaining()}"
        }
        val resultScale = buf.readI1()
        val lowLimitScale = buf.readI1()
        val highLimitScale = buf.readI1()
        val lowLimit = buf.readR4()
        val highLimit = buf.readR4()
        val units = buf.readCn()
        val resultFormat = buf.readCn()
        val lowLimitFormat = buf.readCn()
        val highLimitFormat = buf.readCn()
        val lowSpec = if (buf.hasRemaining()) buf.readR4() else null
        val highSpec = if (buf.hasRemaining()) buf.readR4() else null
        return PTR(
            testNumber = testNumber,
            headNumber = headNumber,
            siteNumber = siteNumber,
            testFlag = testFlag,
            paramFlag = paramFlag,
            result = result,
            testText = testText,
            alarmId = alarmId,
            optionalFlag = optionalFlag,
            resultScale = resultScale,
            lowLimitScale = lowLimitScale,
            highLimitScale = highLimitScale,
            lowLimit = lowLimit,
            highLimit = highLimit,
            units = units,
            resultFormat = resultFormat,
            lowLimitFormat = lowLimitFormat,
            highLimitFormat = highLimitFormat,
            lowSpec = lowSpec,
            highSpec = highSpec
        )
    }
}
