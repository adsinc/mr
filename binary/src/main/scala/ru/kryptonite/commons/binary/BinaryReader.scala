package ru.kryptonite.commons.binary

import java.nio.charset.Charset

private[binary] final class BinaryReader(binary: Binary) extends Reader {

  private var position = 0

  override def remaining: Int = binary.size - position

  override def readByte(): Byte = {
    val res = binary.getByte(position)
    position += 1
    res
  }

  override def readShortLE(): Short = {
    val res = binary.getShortLE(position)
    position += 2
    res
  }

  override def readIntLE(): Int = {
    val res = binary.getIntLE(position)
    position += 4
    res
  }

  override def readLongLE(): Long = {
    val res = binary.getLongLE(position)
    position += 8
    res
  }

  override def readFloatLE(): Float = {
    val res = binary.getFloatLE(position)
    position += 4
    res
  }

  override def readDoubleLE(): Double = {
    val res = binary.getDoubleLE(position)
    position += 8
    res
  }

  override def readBinary(numBytes: Int): Binary = {
    val res = binary.slice(position, position + numBytes)
    position += numBytes
    res
  }

  override def readString(n: Int, charset: Charset): String = {
    val res = binary.getString(position, n, charset)
    position += n
    res
  }

  override def skip(n: Int): Unit = {
    checkRemaining(n)
    position += n
  }
}
