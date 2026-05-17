package yabadado.tool.stdfparser.parser.impl

import yabadado.tool.stdfparser.parser.RecordParser
import yabadado.tool.stdfparser.parser.readC1
import yabadado.tool.stdfparser.parser.readCn
import yabadado.tool.stdfparser.parser.readU1
import yabadado.tool.stdfparser.parser.readU2
import yabadado.tool.stdfparser.parser.readU4
import yabadado.tool.stdfparser.parser.toOrderedBuffer
import yabadado.tool.stdfparser.record.MIR
import java.nio.ByteOrder

internal object MirParser : RecordParser {
    override fun parse(body: ByteArray, order: ByteOrder): MIR {
        val buf = body.toOrderedBuffer(order)
        val setupTime = buf.readU4()
        val startTime = buf.readU4()
        val stationNumber = buf.readU1()
        return MIR(
            setupTime = setupTime,
            startTime = startTime,
            stationNumber = stationNumber,
            modeCode = buf.readC1(),
            retestCode = buf.readC1(),
            protectionCode = buf.readC1(),
            // burnInTime (U*2) and commandModeCode (C*1) are adjacent optional fields.
            // Per STDF v4 they should co-exist, but we read each independently so that a file
            // with only burnInTime present (2-byte remainder) doesn't throw.
            burnInTime = if (buf.hasRemaining()) {
                require(buf.remaining() >= 2) {
                    "MIR: burnInTime requires 2 bytes (U*2), got ${buf.remaining()}"
                }
                buf.readU2()
            } else UShort.MAX_VALUE,
            commandModeCode = if (buf.hasRemaining()) buf.readC1() else ' ',
            lotId = buf.readCn(),
            partType = buf.readCn(),
            nodeName = buf.readCn(),
            testerType = buf.readCn(),
            jobName = buf.readCn(),
            jobRevision = buf.readCn(),
            subLotId = buf.readCn(),
            operatorName = buf.readCn(),
            execType = buf.readCn(),
            execVersion = buf.readCn(),
            testCode = buf.readCn(),
            testTemperature = buf.readCn(),
            userText = buf.readCn(),
            auxFile = buf.readCn(),
            packageType = buf.readCn(),
            familyId = buf.readCn(),
            dateCode = buf.readCn(),
            facilityId = buf.readCn(),
            floorId = buf.readCn(),
            processId = buf.readCn(),
            operationFrequency = buf.readCn(),
            specName = buf.readCn(),
            specVersion = buf.readCn(),
            flowId = buf.readCn(),
            setupId = buf.readCn(),
            designRevision = buf.readCn(),
            engineerId = buf.readCn(),
            romCode = buf.readCn(),
            serialNumber = buf.readCn(),
            supervisorName = buf.readCn()
        )
    }
}
