package ru.kryptonite.commons.binary

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ArrayWriterSpec extends AnyFlatSpec with Matchers with WriterBehaviors {

  "An ArrayWriter" should behave like writer[ArrayWriter](Writer.array(), _.toBinary)

  it should "calculate correct capacity" in {
    assertResult(16)(ArrayWriter.capacityFor(1))
    assertResult(16)(ArrayWriter.capacityFor(16))
    assertResult(32)(ArrayWriter.capacityFor(17))
    assertResult(256)(ArrayWriter.capacityFor(129))
    assertResult(256)(ArrayWriter.capacityFor(255))
  }
}
