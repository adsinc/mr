package ru.kryptonite.commons.binary

import com.twitter.io.Buf
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.ByteBuffer

class ByteBufferBinarySpec extends AnyFlatSpec with Matchers with BinaryBehaviors {

  "An ByteBufferBinary" should behave like binary(s => new ByteBufferBinary(ByteBuffer.wrap(s.toArray)))

  it should "be created from Buf" in {
    val arr               = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)
    val bytes: ByteBuffer = ByteBuffer.wrap(arr)

    val buf: Buf           = Buf.ByteBuffer.Owned(bytes)
    val byteBuffer: Binary = Binary.fromBuf(buf, 0, arr.length)

    byteBuffer.copyToArray().shouldEqual(arr)
  }

  it should "be created from part of Buf" in {
    val arr               = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)
    val bytes: ByteBuffer = ByteBuffer.wrap(arr)

    val buf: Buf           = Buf.ByteBuffer.Owned(bytes)
    val byteBuffer: Binary = Binary.fromBuf(buf, 2, 4)
    byteBuffer.copyToArray().shouldEqual(arr.slice(2, 6))
  }

  it should "throw exception by fromBuf if params are not valid" in {
    val arr               = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)
    val bytes: ByteBuffer = ByteBuffer.wrap(arr)

    val buf: Buf = Buf.ByteBuffer.Owned(bytes)
    an[IndexOutOfBoundsException] should be thrownBy {
      Binary.fromBuf(buf, 5, 104)
    }
  }

  it should "copy to Buf" in {
    val arr               = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)
    val bytes: ByteBuffer = ByteBuffer.wrap(arr)

    val byteBuffer: Binary = Binary.owned(bytes)
    val buf: Buf           = byteBuffer.copyToBuf(0, arr.length)

    val bbuffer = Buf.ByteBuffer.Owned.unapply(buf.asInstanceOf[Buf.ByteBuffer]).get
    bbuffer.array().shouldEqual(arr)
  }

  it should "partial copying to Buf is not available" in {
    val arr               = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)
    val bytes: ByteBuffer = ByteBuffer.wrap(arr)

    val byteBuffer: Binary = Binary.owned(bytes)
    val buf: Buf           = byteBuffer.copyToBuf(2, 4)

    val bbuffer = Buf.ByteBuffer.Owned.unapply(buf.asInstanceOf[Buf.ByteBuffer]).get
    bbuffer.array().shouldEqual(arr.slice(2, 6))
  }

  it should "throw exception by copyToBuf if params are not valid" in {
    val arr               = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)
    val bytes: ByteBuffer = ByteBuffer.wrap(arr)

    val byteBuffer: Binary = Binary.owned(bytes)
    an[IllegalArgumentException] should be thrownBy {
      byteBuffer.copyToBuf(5, 404)
    }
  }
}
