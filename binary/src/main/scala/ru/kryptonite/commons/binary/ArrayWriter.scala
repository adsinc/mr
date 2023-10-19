package ru.kryptonite.commons.binary

import java.nio.charset.Charset

/**
 *  Simple implementation of [[Writer]] that uses exponentially growing array
 */
final class ArrayWriter private[binary] (initialCapacity: Int) extends Writer {

  private var buf: Array[Byte] = new Array[Byte](initialCapacity)
  private var position: Int    = 0

  override def length: Int = position

  override def writeByte(value: Byte): this.type = {
    ensureRemaining(1)
    buf(position) = value
    position += 1
    this
  }

  override def writeShortLE(value: Short): this.type = {
    ensureRemaining(2)
    buf(position) = value.toByte
    buf(position + 1) = (value >> 8).toByte
    position += 2
    this
  }

  override def writeIntLE(value: Int): this.type = {
    ensureRemaining(4)
    buf(position) = value.toByte
    buf(position + 1) = (value >> 8).toByte
    buf(position + 2) = (value >> 16).toByte
    buf(position + 3) = (value >> 24).toByte
    position += 4
    this
  }

  override def writeLongLE(value: Long): this.type = {
    ensureRemaining(8)
    buf(position) = value.toByte
    buf(position + 1) = (value >> 8).toByte
    buf(position + 2) = (value >> 16).toByte
    buf(position + 3) = (value >> 24).toByte
    buf(position + 4) = (value >> 32).toByte
    buf(position + 5) = (value >> 40).toByte
    buf(position + 6) = (value >> 48).toByte
    buf(position + 7) = (value >> 56).toByte
    position += 8
    this
  }

  override def writeBytes(value: Array[Byte]): this.type = {
    ensureRemaining(value.length)
    for (i <- value.indices) {
      buf(position + i) = value(i)
    }
    position += value.length
    this
  }

  override def writeBinary(value: Binary): this.type = {
    ensureRemaining(value.size)
    value.copyTo(0, buf, position, value.size)
    position += value.size
    this
  }

  override def writeString(value: String, charset: Charset): this.type = {
    val bytes = value.getBytes(charset)
    writeBytes(bytes)
  }

  /**
   *  Ensures that all data is written to underlying storage
   *
   *  @return
   */
  override def flush(): Unit = ()

  def toArray: Array[Byte] = {
    val tmp = new Array[Byte](position)
    System.arraycopy(buf, 0, tmp, 0, position)
    tmp
  }

  def toBinary: Binary =
    Binary.owned(toArray)

  protected def ensureRemaining(needed: Int): Unit =
    if (buf.length < position + needed) {
      val tmp = new Array[Byte](ArrayWriter.capacityFor(position + needed))
      System.arraycopy(buf, 0, tmp, 0, position)
      buf = tmp
    }
}

private[binary] object ArrayWriter {

  /**
   *  Calculates capacity that stores at least specified number of bytes.
   *
   *  Current implementation returns closest power of 2, but not least that 16 bytes.
   */
  def capacityFor(size: Int): Int =
    if (size < 16) 16 else Integer.highestOneBit(size + Integer.highestOneBit(size) - 1)
}
