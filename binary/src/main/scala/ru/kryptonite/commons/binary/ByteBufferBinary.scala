package ru.kryptonite.commons.binary

import com.twitter.io.Buf

import java.nio.ByteBuffer

/**
 *  A [[Binary]] that wraps [[ByteBuffer]].
 *
 *  @todo Implementation of inherited methods should be optimized
 */
private[binary] final class ByteBufferBinary(byteBuffer: ByteBuffer) extends Binary {

  /** @inheritdoc */
  override def size: Int = byteBuffer.remaining()

  /** @inheritdoc */
  override def getByte(offset: Int): Byte = byteBuffer.get(offset)

  /** @inheritdoc */
  override def slice(from: Int, until: Int): Binary = {
    val newBuf = byteBuffer.duplicate()
    newBuf.position(from)
    newBuf.limit(until)
    new ByteBufferBinary(newBuf.slice())
  }

  /** @inheritdoc */
  def copyToBuf(offset: Int, length: Int): Buf = {
    val arr           = this.slice(offset, offset + length).copyToArray()
    val newByteBuffer = ByteBuffer.wrap(arr)
    Buf.ByteBuffer.Owned(newByteBuffer)
  }

  /** @inheritdoc */
  override def copyTo(offset: Int, dest: Array[Byte], destPos: Int, length: Int): Unit =
    // It's not possible to use ByteBuffer.get method to copy contents, since it's not thread-safe.
    // Optimized implementation is possible with `sub.misc.Unsafe`.
    super.copyTo(offset, dest, destPos, length)
}
