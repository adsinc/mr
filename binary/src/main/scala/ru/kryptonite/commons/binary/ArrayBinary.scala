package ru.kryptonite.commons.binary

import com.twitter.io.Buf

/**
 *  A [[Binary]] that wraps byte array.
 *
 *  @todo Implementation of inherited methods should be optimized
 */
private[binary] final class ArrayBinary(array: Array[Byte], offset: Int, count: Int) extends Binary {

  override def size: Int = count

  override def getByte(offset: Int): Byte = array(this.offset + offset)

  override def slice(from: Int, until: Int): Binary = {
    checkSize(until)
    new ArrayBinary(array, offset + from, until - from)
  }

  def copyToBuf(offset: Int, length: Int): Buf = {
    val a: Array[Byte] = copyToArray(offset, offset + length)
    Buf.ByteArray.Owned(a)
  }

  override def copyTo(offset: Int, dest: Array[Byte], destPos: Int, length: Int): Unit = {
    checkSize(offset + length)
    System.arraycopy(array, this.offset + offset, dest, destPos, length)
  }
}
