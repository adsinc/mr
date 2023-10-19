package ru.kryptonite.commons.binary

import com.twitter.io.Buf

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.{ Charset, StandardCharsets }

/**
 *  An immutable sequence of bytes.
 *
 *  @note Most methods have default implementation that might be optimized in subclasses.
 */
trait Binary extends Serializable {

  @transient
  override lazy val hashCode: Int = {
    var hash = 0
    hash = Binary.hashBytes(iterator)
    if (hash == 0) {
      hash = 1
    }
    hash
  }

  /**
   *  Gets the byte at the given index (alias of [[Binary.getByte]]).
   */
  def apply(index: Int): Byte = getByte(index)

  /** Gets the number of bytes. */
  def size: Int

  /** Returns `true` if the [[Binary]] is empty */
  def isEmpty: Boolean = size == 0

  def getByte(offset: Int): Byte

  def getShortLE(offset: Int): Short = {
    checkSize(offset + 2)
    val res = (getByte(offset) & 0xff) |
      ((getByte(offset + 1) & 0xff) << 8)
    res.toShort
  }

  def getIntLE(offset: Int): Int = {
    checkSize(offset + 4)
    val res = (getByte(offset) & 0xff) |
      ((getByte(offset + 1) & 0xff) << 8) |
      ((getByte(offset + 2) & 0xff) << 16) |
      ((getByte(offset + 3) & 0xff) << 24)
    res
  }

  def getLongLE(offset: Int): Long = {
    checkSize(offset + 8)
    val res: Long = (getByte(offset).toLong & 0xff) |
      ((getByte(offset + 1) & 0xff).toLong << 8) |
      ((getByte(offset + 2) & 0xff).toLong << 16) |
      ((getByte(offset + 3) & 0xff.toLong) << 24) |
      ((getByte(offset + 4) & 0xff.toLong) << 32) |
      ((getByte(offset + 5) & 0xff.toLong) << 40) |
      ((getByte(offset + 6) & 0xff.toLong) << 48) |
      ((getByte(offset + 7) & 0xff.toLong) << 56)
    res
  }

  def getFloatLE(offset: Int): Float = java.lang.Float.intBitsToFloat(getIntLE(offset))

  def getDoubleLE(offset: Int): Double = java.lang.Double.longBitsToDouble(getLongLE(offset))

  def getString(offset: Int, length: Int, charset: Charset = StandardCharsets.UTF_8): String = {
    val array = new Array[Byte](length)
    copyTo(offset, array, 0, length)
    new String(array, charset)
  }

  /** Creates a new iterator over all bytes */
  def iterator: Iterator[Byte] = new Iterator[Byte] {

    private var pos = 0

    override def hasNext: Boolean = pos < Binary.this.size

    override def next(): Byte = {
      val t = getByte(pos)
      pos += 1
      t
    }
  }

  /**
   *  Creates a slice of this [[Binary]]
   *
   *  @param from      the initial index of new slice, inclusive
   *  @param until     the final index of new slice, exclusive
   *  @return
   */
  def slice(from: Int, until: Int): Binary

  /** Copies to twitter.io.Buf
   *
   *  @param offset the initial index of copied slice, inclusive
   *  @param length number of copied items
   */
  def copyToBuf(offset: Int, length: Int): Buf

  /** Copies a slice of this Binary to the destination. Similar to `java.lang.System.arraycopy` */
  def copyTo(offset: Int, dest: Array[Byte], destPos: Int, length: Int): Unit = {
    checkSize(offset + length)
    for (i <- 0 until length) {
      dest(destPos + i) = getByte(offset + i)
    }
  }

  /**
   *  Copies a slice of the `Binary` to a new `Array`.
   *
   *  @param from      the initial index of copied slice, inclusive
   *  @param until     the final index of copied slice, exclusive
   *  @return          a new `Array`
   */
  def copyToArray(from: Int, until: Int): Array[Byte] = {
    val res = new Array[Byte](until - from)
    copyTo(from, res, 0, until - from)
    res
  }

  /**
   *  Copies all content of the `Binary` to a new `Array`.
   *
   *  @return          a new `Array`
   */
  def copyToArray(): Array[Byte] =
    copyToArray(0, size)

  def canEqual(other: Any): Boolean = other.isInstanceOf[Binary]

  override def equals(obj: scala.Any): Boolean = obj match {
    case o: AnyRef if this.eq(o) =>
      true
    case o: Binary =>
      o.canEqual(this) && iterator.sameElements(o.iterator)
    case _ =>
      false
  }

  protected def checkSize(needed: Int): Unit =
    if (size < needed) {
      throw new IndexOutOfBoundsException(s"Tried to access $needed byte(s) when length is $size")
    }

}

object Binary {

  val Empty: Binary = new ArrayBinary(new Array[Byte](0), 0, 0)

  def owned(data: ByteBuffer): Binary =
    new ByteBufferBinary(data.slice())

  def owned(data: Array[Byte]): Binary =
    new ArrayBinary(data, 0, data.length)

  def owned(data: Array[Byte], offset: Int, count: Int): Binary =
    new ArrayBinary(data, offset, count)

  /**
   *  Constructs a new [[Binary]] with the contents from `java.io.RandomAccessFile`.
   *
   *  @note Modifying contents of `randomAccessFile` while using [[Binary]] leads to undefined behavior.
   *  @note The `randomAccessFile` must be closed after using [[Binary]].
   */
  def apply(randomAccessFile: RandomAccessFile): Binary = {
    val len = randomAccessFile.length()
    val buf = randomAccessFile.getChannel.map(FileChannel.MapMode.READ_ONLY, 0, len)
    Binary.owned(buf)
  }

  def fromBuf(b: Buf, offset: Int, length: Int): Binary =
    if (offset + length <= b.length) {
      b match {
        case buf: Buf.ByteBuffer =>
          owned(Buf.ByteBuffer.Owned.extract(buf.slice(offset, offset + length)))
        case _: Buf =>
          val a = new Array[Byte](length)
          b.slice(offset, offset + length).write(a, 0)
          Binary.owned(a, 0, length)
      }
    } else {
      throw new IndexOutOfBoundsException(s"Tried to access ${offset + length} byte(s) when length is ${b.length}")
    }

  private def hashBytes(iter: Iterator[Byte]): Int =
    iter.foldLeft(0)(_ * 31 + _)

}
