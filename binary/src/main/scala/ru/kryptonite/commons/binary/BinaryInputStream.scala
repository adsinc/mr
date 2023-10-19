package ru.kryptonite.commons.binary

import java.io.InputStream

final class BinaryInputStream(binary: Binary) extends InputStream {

  private var position = 0

  override def read(): Int =
    if (remaining > 0) {
      val r = binary(position) & 0xff
      position += 1
      r
    } else {
      -1
    }

  override def read(b: Array[Byte]): Int =
    read(b, 0, b.length)

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    val nbytes = math.min(remaining, len)
    if (nbytes > 0) {
      binary.copyTo(position, b, off, nbytes)
      position += nbytes
      nbytes
    } else {
      -1
    }
  }

  override def skip(n: Long): Long = {
    val nbytes = math.min(remaining.toLong, n).toInt
    position += nbytes
    nbytes.toLong
  }

  override def available(): Int = remaining

  private def remaining: Int = binary.size - position

}

object BinaryInputStream {

  def apply(binary: Binary): BinaryInputStream = new BinaryInputStream(binary)
}
