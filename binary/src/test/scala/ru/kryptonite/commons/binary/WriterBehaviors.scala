package ru.kryptonite.commons.binary

import org.scalatest.flatspec.AnyFlatSpec

trait WriterBehaviors { this: AnyFlatSpec =>

  def writer[W <: Writer](construct: => W, result: W => Binary): Unit = {
    val bytes = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)

    it should "write byte" in {
      val w = construct
      w.writeByte(10)
      val r = result(w).copyToArray()
      assertResult(Array(10))(r)
    }

    it should "write bytes" in {
      val w = construct
      w.writeBytes(bytes)
      val r = result(w).copyToArray()
      assertResult(bytes)(r)
    }

    it should "write binary" in {
      val w = construct
      w.writeBinary(Binary.owned(bytes))
      val r = result(w).copyToArray()
      assertResult(bytes)(r)
    }

    it should "write large binary" in {
      val w          = construct
      val largeBytes = Array.fill[Byte](4096)(0)
      w.writeBinary(Binary.owned(largeBytes))
      val r = result(w).copyToArray()
      assertResult(largeBytes)(r)

    }
  }
}
