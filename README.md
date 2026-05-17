# stdf4k

A pure Kotlin streaming library for reading **STDF v4** (Standard Test Data Format) binary files used in semiconductor testing.

- **Lazy** — records are decoded one at a time; arbitrarily large files never require full in-memory loading
- **Zero runtime dependencies** (only SLF4J API; callers supply the binding)
- **Safe** — unknown or malformed records become `UnknownRecord` and never throw

---

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>yabadado.tool</groupId>
    <artifactId>stdfparser</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

---

## Quick Start

```kotlin
import yabadado.tool.stdfparser.asStdfSequence
import yabadado.tool.stdfparser.record.PTR
import java.io.File

// From a File
File("lot.stdf").asStdfSequence()
    .filterIsInstance<PTR>()
    .forEach { println("Test #${it.testNumber}: ${it.result}") }

// From an InputStream
inputStream.asStdfSequence().toList()
```

Both overloads return a **cold** `Sequence<Record>`. The file / stream is closed automatically when the sequence is fully consumed or abandoned.

---

## Supported Records

| Type | Sub | Class | Description |
|------|-----|-------|-------------|
| 0 | 10 | `FAR` | File Attributes — establishes byte order |
| 0 | 20 | `ATR` | Audit Trail |
| 1 | 10 | `MIR` | Master Information (37 fields) |
| 1 | 20 | `MRR` | Master Results |
| 1 | 30 | `PCR` | Part Count |
| 1 | 40 | `HBR` | Hardware Bin |
| 1 | 50 | `SBR` | Software Bin |
| 1 | 80 | `SDR` | Site Description |
| 2 | 10 | `WIR` | Wafer Information |
| 5 | 10 | `PIR` | Part Information |
| 5 | 20 | `PRR` | Part Results |
| 15 | 10 | `PTR` | Parametric Test Result |
| — | — | `UnknownRecord` | Any unrecognised record type |

---

## Type Mapping

| STDF type | Kotlin type | Notes |
|-----------|-------------|-------|
| U\*1 | `UByte` | |
| U\*2 | `UShort` | |
| U\*4 | `UInt` | |
| I\*1 | `Byte` | |
| I\*2 | `Short` | |
| R\*4 | `Float?` | `null` when TEST_FLG bit 1 is set (PTR only) |
| C\*1 | `Char` | 1 byte, no length prefix; defaults to `' '` when absent |
| C\*n | `String` | 1-byte length prefix then N ASCII bytes; defaults to `""` when absent |

---

## Error Handling

| Situation | Behaviour |
|-----------|-----------|
| Truncated header (< 4 bytes) | End of sequence |
| Truncated body (< declared length) | End of sequence |
| Unknown record type | Wrapped as `UnknownRecord`, iteration continues |
| Malformed field in a known record | Wrapped as `UnknownRecord`, iteration continues |

Warnings are emitted via SLF4J at `WARN` / `ERROR` level. No exception ever escapes `asStdfSequence()`.

---

## Byte Order Detection

1. Auto-detected from the FAR header length field (always exactly 2 bytes):
   - `[0x00, 0x02, …]` → Big-endian
   - `[0x02, 0x00, …]` → Little-endian
2. Confirmed via `FAR.cpuType`: `1` = Big-endian (Sun/Motorola), `2` = Little-endian (Intel).
3. If auto-detection and `cpuType` disagree, `cpuType` wins.

---

## Building

```bash
# Build
./mvnw clean package

# Test
./mvnw test
```

Requires JDK 17+.

---

## License

MIT
