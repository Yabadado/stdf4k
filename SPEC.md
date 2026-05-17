# STDF Reader — Specification

## Goal

A pure Kotlin library that reads STDF v4 binary files via streaming, decoding records lazily so arbitrarily large files never require full in-memory loading.

## Public API

```kotlin
// Primary entry point
fun File.asStdfSequence(): Sequence<Record>
fun InputStream.asStdfSequence(): Sequence<Record>

// Usage
File("lot.stdf").asStdfSequence()
    .filterIsInstance<PTR>()
    .forEach { println(it) }
```

## Streaming Contract

- Each call to `asStdfSequence()` returns a cold `Sequence<Record>`.
- Records are decoded one at a time; only the current record's bytes are held in memory.
- The caller controls consumption (collect, filter, take, etc.).
- Byte order is determined from the first record (FAR); all subsequent records use that order.

## Supported Records

| Type | Sub | Class | Description |
|------|-----|-------|-------------|
| 0 | 10 | `FAR` | File Attributes — establishes byte order |
| 0 | 20 | `ATR` | Audit Trail |
| 1 | 10 | `MIR` | Master Information |
| 1 | 20 | `MRR` | Master Results |
| 1 | 30 | `PCR` | Part Count |
| 1 | 40 | `HBR` | Hardware Bin |
| 1 | 50 | `SBR` | Software Bin |
| 1 | 80 | `SDR` | Site Description |
| 2 | 10 | `WIR` | Wafer Information |
| 5 | 10 | `PIR` | Part Information |
| 5 | 20 | `PRR` | Part Results |
| 15 | 10 | `PTR` | Parametric Test Result |
| * | *  | `UnknownRecord` | Any unrecognised record |

## Record Model Rules

- All records are immutable `data class` extending `sealed class Record`.
- Required fields are non-null. Optional STDF trailing fields use `null` (numeric) or `""` (strings).
- `UByte`, `UShort`, `UInt` are used for STDF unsigned types U\*1, U\*2, U\*4.
- `Byte`, `Short`, `Float` for signed I\*1, I\*2, R\*4.
- C\*1 fields (fixed single-char) are `Char`, defaulting to `' '` when absent.
- C\*n fields (length-prefixed strings) are `String`, defaulting to `""` when absent.

## Binary Parsing Rules

- 4-byte record header: `[length: U*2][type: U*1][subtype: U*1]` (length = body bytes only).
- Multi-byte numeric fields respect byte order from FAR.
- Single-byte fields (U\*1, C\*1, B\*1) are byte-order-independent.
- C\*n: read 1-byte length, then read that many ASCII bytes.
- C\*1: read 1 byte directly as a character (no length prefix).
- Optional trailing fields: if the buffer is exhausted, use the field's default value.

## Error Handling

- Truncated header (< 4 bytes): end of sequence.
- Truncated body (< declared length): end of sequence.
- Unknown record type: wrap in `UnknownRecord` and continue.
- Malformed field within a known record: surface as `UnknownRecord` with raw bytes.

## Out of Scope

- REST API / HTTP layer.
- Writing STDF files.
- Record types beyond the 12 listed above.
