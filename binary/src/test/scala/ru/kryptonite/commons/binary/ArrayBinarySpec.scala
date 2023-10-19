package ru.kryptonite.commons.binary

import com.twitter.io.Buf
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ArrayBinarySpec extends AnyFlatSpec with Matchers with BinaryBehaviors {

  "An ArrayBinary" should behave like binary(s => new ArrayBinary(s.toArray, 0, s.size))

  it should "be created from Buf" in {
    val bytes = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)

    val buf: Buf    = Buf.ByteArray.Owned(bytes)
    val arrayBinary = Binary.fromBuf(buf, 0, bytes.length)

    arrayBinary.copyToArray().shouldEqual(bytes)
  }

  it should "be created from part of Buf" in {
    val bytes = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)

    val buf: Buf    = Buf.ByteArray.Owned(bytes)
    val arrayBinary = Binary.fromBuf(buf, 2, 4)

    arrayBinary.copyToArray().shouldEqual(bytes.slice(2, 6))
  }

  it should "throw exception by fromBuf if params are not valid" in {
    val bytes = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)

    val buf: Buf = Buf.ByteArray.Owned(bytes)

    an[IndexOutOfBoundsException] should be thrownBy {
      Binary.fromBuf(buf, 5, 104)
    }
  }

  it should "copy to Buf" in {
    val bytes = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)

    val arrayBinary: Binary = Binary.owned(bytes)
    val buf: Buf            = arrayBinary.copyToBuf(0, bytes.length)

    val (arr, start, end) = Buf.ByteArray.Owned.unapply(buf.asInstanceOf[Buf.ByteArray]).get
    start.shouldEqual(0)
    end.shouldEqual(bytes.length)
    arr.shouldEqual(bytes)
  }

  it should "copy to Buf a part of Binary" in {
    val bytes = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)

    val arrayBinary: Binary = Binary.owned(bytes)
    val buf: Buf            = arrayBinary.copyToBuf(2, 4)

    val (arr, start, end) = Buf.ByteArray.Owned.unapply(buf.asInstanceOf[Buf.ByteArray]).get
    start.shouldEqual(0)
    end.shouldEqual(4)
    arr.shouldEqual(bytes.slice(2, 6))
  }

  it should "throw exception by copyToBuf if params are not valid" in {
    val bytes = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)

    val arrayBinary: Binary = Binary.owned(bytes)
    an[IndexOutOfBoundsException] should be thrownBy {
      arrayBinary.copyToBuf(5, 404)
    }
  }
}
