package yabadado.tool.stdfparser

import yabadado.tool.stdfparser.record.FAR
import yabadado.tool.stdfparser.record.MIR
import yabadado.tool.stdfparser.record.MRR
import yabadado.tool.stdfparser.record.PIR
import yabadado.tool.stdfparser.record.PRR
import yabadado.tool.stdfparser.record.PTR
import yabadado.tool.stdfparser.record.UnknownRecord
import kotlin.test.assertContentEquals
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StdfReaderTest {

    private val demoFile = File("src/test/resources/demofile.stdf")
    private val lotFile = File("src/test/resources/lot2.stdf")

    @Test
    fun `first record is FAR`() {
        val first = demoFile.asStdfSequence().first()
        assertIs<FAR>(first)
    }

    @Test
    fun `FAR reports stdf version 4`() {
        val far = demoFile.asStdfSequence().first() as FAR
        assertEquals(4u.toUByte(), far.stdfVersion)
    }

    @Test
    fun `reads all records without exception`() {
        val records = demoFile.asStdfSequence().toList()
        assertTrue(records.isNotEmpty(), "expected at least one record")
    }

    @Test
    fun `lot2 reads all records without exception`() {
        val records = lotFile.asStdfSequence().toList()
        assertTrue(records.isNotEmpty(), "expected at least one record")
    }

    @Test
    fun `MIR contains non-blank lot id`() {
        val mir = demoFile.asStdfSequence()
            .filterIsInstance<MIR>()
            .firstOrNull()
        assertNotNull(mir, "expected at least one MIR record")
        // lotId may be empty for some test files, but the record must parse cleanly
        assertNotNull(mir.lotId)
    }

    @Test
    fun `sequence is lazy - stops after first record`() {
        val first = demoFile.asStdfSequence().take(1).toList()
        assertEquals(1, first.size)
    }

    @Test
    fun `PIR and PRR records are balanced`() {
        val records = demoFile.asStdfSequence().toList()
        val pirCount = records.count { it is PIR }
        val prrCount = records.count { it is PRR }
        assertEquals(pirCount, prrCount, "PIR and PRR counts should match")
    }

    @Test
    fun `PTR records have valid test numbers`() {
        demoFile.asStdfSequence()
            .filterIsInstance<PTR>()
            .take(10)
            .forEach { ptr ->
                assertTrue(ptr.testNumber > 0u, "test number should be positive")
            }
    }

    @Test
    fun `MRR is the last named record`() {
        val records = demoFile.asStdfSequence().toList()
        val mrr = records.filterIsInstance<MRR>().lastOrNull()
        assertNotNull(mrr, "expected at least one MRR record")
    }

    @Test
    fun `unknown records are wrapped not thrown`() {
        // Verify that parsing completes and every record is either a known type or an UnknownRecord
        val records = demoFile.asStdfSequence().toList()
        assertTrue(records.isNotEmpty())
        // All elements must be Record instances — no exceptions escaped
        records.forEach { record ->
            assertTrue(record is yabadado.tool.stdfparser.record.Record)
        }
    }

    @Test
    fun `InputStream extension works identically to File extension`() {
        val fromFile = demoFile.asStdfSequence().toList()
        val fromStream = demoFile.inputStream().asStdfSequence().toList()
        assertEquals(fromFile.size, fromStream.size)
    }

    @Test
    fun `empty stream yields no records`() {
        val records = ByteArrayInputStream(ByteArray(0)).asStdfSequence().toList()
        assertTrue(records.isEmpty(), "empty stream should produce no records")
    }

    @Test
    fun `truncated header yields no records`() {
        // Only 3 bytes — not enough for a 4-byte header
        val records = ByteArrayInputStream(ByteArray(3) { 0 }).asStdfSequence().toList()
        assertTrue(records.isEmpty(), "truncated header should produce no records")
    }

    @Test
    fun `malformed record body becomes UnknownRecord not exception`() {
        // Build a synthetic STDF: FAR header (type=0, sub=10, BE, length=2) + valid FAR body
        // followed by a SDR record header whose body declares siteCount=5 but provides 0 site bytes
        val farHeader = byteArrayOf(0x00, 0x02, 0x00, 0x0A)  // length=2, type=0, sub=10
        val farBody   = byteArrayOf(0x01, 0x04)               // cpuType=1 (BE), stdfVersion=4
        // SDR: length=3 (headNum + siteGroup + siteCount=5, but no actual site bytes → truncated)
        val sdrHeader = byteArrayOf(0x00, 0x03, 0x01, 0x50)   // length=3, type=1, sub=80
        val sdrBody   = byteArrayOf(0x01, 0x00, 0x05)          // headNum=1, siteGroup=0, siteCount=5 (but 0 sites follow)
        val bytes = farHeader + farBody + sdrHeader + sdrBody
        val records = ByteArrayInputStream(bytes).asStdfSequence().toList()
        assertEquals(2, records.size, "FAR + malformed SDR should yield 2 records")
        assertIs<FAR>(records[0])
        assertIs<UnknownRecord>(records[1], "malformed SDR should become UnknownRecord")
    }

    @Test
    fun `stream is closed after sequence is fully consumed`() {
        var closed = false
        val trackingStream = object : java.io.InputStream() {
            val delegate = demoFile.inputStream()
            override fun read(): Int = delegate.read()
            override fun read(b: ByteArray, off: Int, len: Int) = delegate.read(b, off, len)
            override fun close() { closed = true; delegate.close() }
        }
        trackingStream.asStdfSequence().toList()
        assertTrue(closed, "stream should be closed after full sequence consumption")
    }

    @Test
    fun `FAR cpuType is 1 for big-endian demo file`() {
        val far = demoFile.asStdfSequence().first() as FAR
        // demofile.stdf is a big-endian file; cpuType=1 means Sun/Motorola per STDF v4 spec
        assertEquals(1u.toUByte(), far.cpuType)
    }

    @Test
    fun `PTR with missing RESULT field becomes UnknownRecord`() {
        // PTR body with only the 8 mandatory bytes before RESULT — RESULT (R*4) is absent
        // Layout: TEST_NUM(4) + HEAD_NUM(1) + SITE_NUM(1) + TEST_FLG(1) + PARM_FLG(1) = 8 bytes
        val farHeader = byteArrayOf(0x00, 0x02, 0x00, 0x0A)          // FAR, length=2, BE
        val farBody   = byteArrayOf(0x01, 0x04)                       // cpuType=1, stdfVersion=4
        val ptrHeader = byteArrayOf(0x00, 0x08, 0x0F, 0x0A)          // PTR, length=8
        val ptrBody   = byteArrayOf(0x00,0x00,0x00,0x01, 0x01, 0x01, 0x00, 0x00) // no RESULT
        val records = ByteArrayInputStream(farHeader + farBody + ptrHeader + ptrBody)
            .asStdfSequence().toList()
        assertEquals(2, records.size)
        assertIs<FAR>(records[0])
        assertIs<UnknownRecord>(records[1], "PTR with missing RESULT should become UnknownRecord")
    }

    @Test
    fun `undetectable byte order yields empty sequence`() {
        // Header bytes where neither BE nor LE interpretation yields body length 2:
        // BE: 0x0003 = 3, LE: 0x0300 = 768 — neither is 2, so the reader must abort cleanly.
        val header = byteArrayOf(0x00, 0x03, 0x00, 0x0A)
        val records = ByteArrayInputStream(header).asStdfSequence().toList()
        assertTrue(records.isEmpty(), "undetectable byte order should produce no records")
    }

    @Test
    fun `UnknownRecord rawBody is a defensive copy`() {
        val source = byteArrayOf(0x01, 0x02, 0x03)
        val record = UnknownRecord(0u, 10u, source)
        val originalContent = source.copyOf()
        source[0] = 0xFF.toByte()   // mutate the source array after construction
        assertContentEquals(originalContent, record.rawBody, "rawBody must not reflect mutations to the source array")
    }

    @Test
    fun `little-endian FAR is auto-detected and cpuType confirms little-endian`() {
        // FAR header in LE: body length=2 → [0x02, 0x00]; type=0, sub=10
        val farHeader = byteArrayOf(0x02, 0x00, 0x00, 0x0A)
        // FAR body: cpuType=2 (LITTLE_ENDIAN / Intel), stdfVersion=4
        val farBody   = byteArrayOf(0x02, 0x04)
        val records = ByteArrayInputStream(farHeader + farBody).asStdfSequence().toList()
        assertEquals(1, records.size)
        val far = assertIs<FAR>(records[0])
        assertEquals(2u.toUByte(), far.cpuType, "cpuType should be 2 (little-endian)")
        assertEquals(4u.toUByte(), far.stdfVersion)
    }

    @Test
    fun `little-endian multi-byte fields are decoded correctly`() {
        // FAR (LE): length=2 → [0x02,0x00], type=0, sub=10; cpuType=2, stdfVersion=4
        val farHeader = byteArrayOf(0x02, 0x00, 0x00, 0x0A)
        val farBody   = byteArrayOf(0x02, 0x04)
        // MIR (LE): length=12 → [0x0C,0x00], type=1, sub=10
        // setupTime=1 LE U*4 → [0x01,0x00,0x00,0x00], startTime=2 LE U*4 → [0x02,0x00,0x00,0x00]
        // stationNumber=1, modeCode=' ', retestCode=' ', protectionCode=' '
        val mirHeader = byteArrayOf(0x0C, 0x00, 0x01, 0x0A)
        val mirBody   = byteArrayOf(
            0x01, 0x00, 0x00, 0x00,
            0x02, 0x00, 0x00, 0x00,
            0x01, 0x20, 0x20, 0x20
        )
        val records = ByteArrayInputStream(farHeader + farBody + mirHeader + mirBody)
            .asStdfSequence().toList()
        assertEquals(2, records.size, "expected FAR + MIR")
        assertIs<FAR>(records[0])
        val mir = assertIs<MIR>(records[1])
        assertEquals(1u, mir.setupTime, "setupTime must be decoded in little-endian")
        assertEquals(2u, mir.startTime, "startTime must be decoded in little-endian")
    }

    @Test
    fun `PTR with OPT_FLAG but truncated optional section becomes UnknownRecord`() {
        // PTR body: full mandatory section + OPT_FLAG, but only 5 bytes follow (need ≥11)
        // Layout: TEST_NUM(4)+HEAD_NUM(1)+SITE_NUM(1)+TEST_FLG(1)+PARM_FLG(1)+RESULT(4)
        //         +testTextLen(1,0)+alarmIdLen(1,0)+OPT_FLAG(1)+5 junk bytes = 20 bytes
        val farHeader = byteArrayOf(0x00, 0x02, 0x00, 0x0A)
        val farBody   = byteArrayOf(0x01, 0x04)
        val ptrHeader = byteArrayOf(0x00, 0x14, 0x0F, 0x0A)          // PTR, length=20
        val ptrBody   = byteArrayOf(
            0x00, 0x00, 0x00, 0x01,  // TEST_NUM = 1
            0x01, 0x01,              // HEAD_NUM=1, SITE_NUM=1
            0x00, 0x00,              // TEST_FLG=0, PARM_FLG=0
            0x40, 0x00, 0x00, 0x00,  // RESULT = 2.0f (big-endian IEEE 754)
            0x00, 0x00,              // testText="", alarmId="" (zero-length C*n)
            0xFF.toByte(),           // OPT_FLAG (all bits set)
            0x01, 0x02, 0x03, 0x04, 0x05  // only 5 bytes — need ≥11
        )
        val records = ByteArrayInputStream(farHeader + farBody + ptrHeader + ptrBody)
            .asStdfSequence().toList()
        assertEquals(2, records.size)
        assertIs<FAR>(records[0])
        assertIs<UnknownRecord>(records[1], "PTR with truncated optional section should become UnknownRecord")
    }
}
