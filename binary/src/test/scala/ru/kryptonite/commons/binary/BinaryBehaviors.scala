package ru.kryptonite.commons.binary

import org.scalatest.flatspec.AnyFlatSpec

import java.util

trait BinaryBehaviors { this: AnyFlatSpec =>

  def binary(construct: Seq[Byte] => Binary): Unit = {
    val bytes = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9)
    val str   = "Some string"

    val emptyBin = construct(Seq())
    val bin      = construct(bytes.toIndexedSeq)
    val utfBin   = construct(str.getBytes().toSeq)

    it should "implement size" in {
      assertResult(bytes.length)(bin.size)
    }

    it should "implement isEmpty" in {
      assert(!bin.isEmpty)
      assert(emptyBin.isEmpty)
    }

    it should "implement getByte" in {
      assertResult(2)(bin(1))
      assertResult(3)(bin.getByte(2))
    }

    it should "implement getShortLE" in {
      assertResult(0x0302)(bin.getShortLE(1))
    }

    it should "implement getIntLE" in {
      assertResult(0x05040302)(bin.getIntLE(1))
    }

    it should "implement getLongLE" in {
      assertResult(0x0908070605040302L)(bin.getLongLE(1))
    }

    it should "implement getString" in {
      assertResult(str.substring(1, str.length - 1))(utfBin.getString(1, utfBin.size - 2))
    }

    it should "implement iterator" in {
      assertResult(bytes)(bin.iterator.toSeq)
    }

    it should "implement slice" in {
      val slice = bin.slice(2, 4)
      assertResult(util.Arrays.copyOfRange(bytes, 2, 4))(slice.copyToArray())
    }

    it should "implement nested slice" in {
      val slice1 = bin.slice(5, 9)
      assertResult(4)(slice1.size)
      val slice2 = slice1.slice(2, 4)
      assertResult(util.Arrays.copyOfRange(bytes, 7, 9))(slice2.copyToArray())
    }

    it should "implement copyTo" in {
      val a = new Array[Byte](3)
      bin.copyTo(2, a, 1, 2)
      assertResult(3)(a(1))
      assertResult(4)(a(2))
    }

    it should "implement equality" in {
      val bin2 = construct(bytes.toIndexedSeq)
      assert(bin == bin2)
    }

    it should "provide BinaryInputStream" in {
      val is = BinaryInputStream(bin)
      for (i <- bytes) {
        assertResult(i)(is.read())
      }
      assertResult(-1)(is.read())

      val is2   = BinaryInputStream(bin)
      val a     = new Array[Byte](bytes.length)
      val nRead = is2.read(a)
      assertResult(bytes.length)(nRead)
      assertResult(bytes)(a)
      assertResult(-1)(is2.read())

      val bytes3 = Array[Byte](-127, -63, -1)
      val bin3   = construct(bytes3.toIndexedSeq)
      val is3    = BinaryInputStream(bin3)
      assertResult(129)(is3.read())
      assertResult(193)(is3.read())
      assertResult(255)(is3.read())
      assertResult(-1)(is3.read())
      val a3 = new Array[Byte](bytes3.length)
      assertResult(-1)(is3.read(a3))
    }

  }

}
