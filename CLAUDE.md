# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw.cmd clean package

# Run all tests
JAVA_HOME="/d/DevTools/jdk-22" ./mvnw.cmd test

# Run a single test class
JAVA_HOME="/d/DevTools/jdk-22" ./mvnw.cmd test -Dtest=StdfReaderTest
```

`JAVA_HOME` points to the JDK at `D:\DevTools\jdk-22`.

## Work Cycle

每個功能或修正都遵循以下循環，並以 `/spec` → `/plan` → `/build` → `/test` → `/review` 推進：

1. **釐清需求** (`/spec`) — 用 `agent-skills:interview-me` 與 `agent-skills:spec-driven-development` 釐清目標，產出 `SPEC.md`
2. **拆解任務** (`/plan`) — 將 spec 拆成可獨立測試的最小任務，更新 `SPEC.md` 的任務清單
3. **TDD 實作** (`/build`) — 先寫失敗測試 (RED)，再實作最小程式碼通過 (GREEN)，最後 commit
4. **驗證** (`/test`) — 跑完整測試套件確認沒有 regression：`mvnw.cmd test`
5. **更新 CLAUDE.md** — 若這次變更影響架構、型別映射、指令或已知問題，立即更新本文件

> **自動提醒**：編輯任何 `.kt` 檔案後，hook 會注入提示要求檢查 CLAUDE.md 是否需更新；每次 Claude 停止時也會顯示提醒訊息。

## Architecture

A pure Kotlin streaming library for reading **STDF v4** (Standard Test Data Format) binary files used in semiconductor testing.

### Public API

```kotlin
File("lot.stdf").asStdfSequence()            // → Sequence<Record>
inputStream.asStdfSequence()                  // → Sequence<Record>

// Filter by type
file.asStdfSequence().filterIsInstance<PTR>()
```

### Streaming flow

```
File / InputStream
      ↓
   StdfReader.readRecords()
      ↓  reads 4-byte header (length U*2, type U*1, subtype U*1)
      ↓  reads body bytes (length bytes)
      ↓
   RecordParserRegistry.find(type, subtype) → RecordParser?
      ↓
   RecordParser.parse(body, byteOrder) → Record   (or UnknownRecord)
      ↓
   yield(record)                    ← lazy, one at a time
```

### Byte order handling (critical)

STDF byte order is auto-detected from the FAR header before cpuType is known:
- FAR body is always 2 bytes, so the length field `[0x00, 0x02]` → BIG_ENDIAN, `[0x02, 0x00]` → LITTLE_ENDIAN.
- After parsing FAR, byte order is confirmed via `cpuType`: **1 = BIG_ENDIAN (Sun/Motorola)**, **2 = LITTLE_ENDIAN (Intel)**.
- The original mapping was reversed (1=LE, 2=BE) — this was a bug.
- Both headers AND bodies use the same byte order throughout the file.

### Package layout

```
yabadado.tool.stdfparser/
├── StdfReader.kt                     ← public API: asStdfSequence()
├── record/                           ← immutable data classes
│   ├── Record.kt                     ← sealed class base
│   ├── FAR, ATR, MIR, MRR, PCR
│   ├── HBR, SBR, SDR, WIR
│   ├── PIR, PRR, PTR
│   └── UnknownRecord.kt
└── parser/                           ← internal parsing
    ├── RecordParser.kt               ← interface + ByteArray.toOrderedBuffer()
    ├── StdfBuffer.kt                 ← ByteBuffer extension helpers
    ├── RecordParserRegistry.kt       ← maps (type, subType) → RecordParser
    └── impl/                         ← one object per record type
        └── FarParser, AtrParser, MirParser, MrrParser, ...
```

### STDF record type/subtype mapping

| Type | Sub | Class | Notes |
|------|-----|-------|-------|
| 0 | 10 | `FAR` | always first; establishes byte order |
| 0 | 20 | `ATR` | |
| 1 | 10 | `MIR` | 37 fields, most optional trailing C*n |
| 1 | 20 | `MRR` | |
| 1 | 30 | `PCR` | |
| 1 | 40 | `HBR` | |
| 1 | 50 | `SBR` | |
| 1 | 80 | `SDR` | siteNumbers is a `List<UByte>` |
| 2 | 10 | `WIR` | |
| 5 | 10 | `PIR` | |
| 5 | 20 | `PRR` | |
| 15 | 10 | `PTR` | result is `Float?`; null when TEST_FLG bit1 set |

### STDF binary type → Kotlin mapping

| STDF | Read via | Kotlin type |
|------|----------|-------------|
| U\*1 | `buf.readU1()` | `UByte` |
| U\*2 | `buf.readU2()` | `UShort` |
| U\*4 | `buf.readU4()` | `UInt` |
| I\*1 | `buf.readI1()` | `Byte` |
| I\*2 | `buf.readI2()` | `Short` |
| R\*4 | `buf.readR4()` | `Float` |
| C\*1 | `buf.readC1()` | `Char` (1 byte, no length prefix) |
| C\*n | `buf.readCn()` | `String` (1-byte length, then N ASCII bytes) |

Optional trailing fields: check `buf.hasRemaining()` before reading; use `null` or default if absent.

### Test resources

Two real STDF binary files in `src/test/resources/`:
- `demofile.stdf` — BIG_ENDIAN file (cpuType=1), contains FAR + MIR + PIR/PRR pairs + PTR records
- `lot2.stdf` — same format, second lot

Use `File("src/test/resources/demofile.stdf").asStdfSequence()` for integration tests.
