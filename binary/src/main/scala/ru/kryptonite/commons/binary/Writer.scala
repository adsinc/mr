package ru.kryptonite.commons.binary

import java.io.OutputStream
import java.nio.charset.{ Charset, StandardCharsets }

/**
 *  Allows efficient writing of binary data to underlying storage.
 */
trait Writer {

  /** Number of bytes written */
  def length: Int

  def writeByte(value: Byte): this.type

  def writeShortLE(value: Short): this.type = {
    ensureRemaining(2)
    writeByte(value.toByte)
    writeByte((value >> 8).toByte)
    this
  }

  def writeIntLE(value: Int): this.type = {
    writeByte(value.toByte)
    writeByte((value >> 8).toByte)
    writeByte((value >> 16).toByte)
    writeByte((value >> 24).toByte)
    this
  }

  def writeLongLE(value: Long): this.type = {
    ensureRemaining(8)
    writeByte(value.toByte)
    writeByte((value >> 8).toByte)
    writeByte((value >> 16).toByte)
    writeByte((value >> 24).toByte)
    writeByte((value >> 32).toByte)
    writeByte((value >> 40).toByte)
    writeByte((value >> 48).toByte)
    writeByte((value >> 56).toByte)
    this

  }

  def writeFloatLE(value: Float): this.type = writeIntLE(java.lang.Float.floatToIntBits(value))

  def writeDoubleLE(value: Double): this.type = writeLongLE(java.lang.Double.doubleToLongBits(value))

  def writeBytes(value: Array[Byte]): this.type = {
    ensureRemaining(value.length)
    for (i <- value.indices) {
      writeByte(value(i))
    }
    this
  }

  def writeBinary(value: Binary): this.type = {
    ensureRemaining(value.size)
    for (b <- value.iterator) {
      writeByte(b)
    }
    this
  }

  def writeString(value: String, charset: Charset = StandardCharsets.UTF_8): this.type = {
    val bytes = value.getBytes(charset)
    writeBytes(bytes)
  }

  /**
   *  Ensures that all data is written to underlying storage
   *
   *  @return
   */
  def flush(): Unit

  protected def ensureRemaining(needed: Int): Unit
}

object Writer {

  def apply(os: OutputStream): Writer = new StreamWriter(os)

  def array(initialCapacity: Int = 16): ArrayWriter = new ArrayWriter(initialCapacity)
}
