package ru.kryptonite.commons.binary

import java.io.RandomAccessFile
import java.nio.charset.{ Charset, StandardCharsets }

/**
 *  Provides stateful API to extract binary data from underlying storage.
 *
 *  @note Most methods have default implementation that might be optimized in subclasses.
 */
trait Reader {

  def remaining: Int

  def readByte(): Byte

  def readShortLE(): Short = {
    checkRemaining(2)
    val res = (readByte() & 0xff) |
      ((readByte() & 0xff) << 8)
    res.toShort
  }

  def readIntLE(): Int = {
    checkRemaining(4)
    val res = (readByte() & 0xff) |
      ((readByte() & 0xff) << 8) |
      ((readByte() & 0xff) << 16) |
      ((readByte() & 0xff) << 24)
    res
  }

  def readLongLE(): Long = {
    checkRemaining(8)
    val res: Long = (readByte().toLong & 0xff) |
      ((readByte() & 0xff).toLong << 8) |
      ((readByte() & 0xff).toLong << 16) |
      ((readByte() & 0xff.toLong) << 24) |
      ((readByte() & 0xff.toLong) << 32) |
      ((readByte() & 0xff.toLong) << 40) |
      ((readByte() & 0xff.toLong) << 48) |
      ((readByte() & 0xff.toLong) << 56)
    res
  }

  def readFloatLE(): Float = java.lang.Float.intBitsToFloat(readIntLE())

  def readDoubleLE(): Double = java.lang.Double.longBitsToDouble(readLongLE())

  def readBinary(n: Int): Binary = {
    val buf = new Array[Byte](n)
    for (i <- 0 until n) {
      buf(i) = readByte()
    }
    Binary.owned(buf)
  }

  def readString(n: Int, charset: Charset = StandardCharsets.UTF_8): String = {
    val buf = new Array[Byte](n)
    for (i <- 0 until n) {
      buf(i) = readByte()
    }
    new String(buf, charset)
  }

  def skip(n: Int): Unit =
    for (_ <- 0 until n) {
      readByte()
    }

  def readRemainingBinary(): Binary =
    readBinary(remaining)

  protected def checkRemaining(needed: Int): Unit =
    if (remaining < needed) {
      throw new IndexOutOfBoundsException(s"Tried to read $needed byte(s) when only $remaining bytes remaining")
    }

}

object Reader {

  /**
   *  Constructs a new [[Reader]] from the [[Binary]].
   */
  def apply(binary: Binary): Reader = new BinaryReader(binary)

  /**
   *  Constructs a new [[Reader]] from the `java.io.RandomAccessFile`.
   *
   *  @note Modifying contents of `randomAccessFile` while using [[Reader]] leads to undefined behavior.
   *  @note The `randomAccessFile` must be closed after using [[Reader]].
   */
  def apply(randomAccessFile: RandomAccessFile): Reader = Reader(Binary(randomAccessFile))
}
