package ru.kryptonite.audio.fvad

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ru.kryptonite.commons.util.CloseableOps

import scala.util.Random

class FVadSpec extends AnyFlatSpec with Matchers with CloseableOps {

  "A FVad" should "be constructed and closed" in {
    val vad = FVad(8000, 0)
    vad.close()
  }

  it should "return 0 for silence" in {
    withCloseable(FVad(8000, 0)) { vad =>
      val data = Array.fill[Short](8000 * 20 / 1000)(0)
      assert(!vad.isSpeech(data))
    }
  }

  it should "return 1 for voice" in {
    withCloseable(FVad(8000, 0)) { vad =>
      val rnd  = new Random()
      val data = Array.fill[Short](8000 * 20 / 1000)(rnd.nextInt(Short.MaxValue).toShort)
      assert(vad.isSpeech(data))
    }
  }

  it should "reset vad" in {
    withCloseable(FVad(8000, 0)) { vad =>
      val data = Array.fill[Short](8000 * 20 / 1000)(0)
      assert(!vad.isSpeech(data))
      vad.reset()
      assert(!vad.isSpeech(data))
    }
  }

  it should "throw an exception after closing" in {
    withCloseable(FVad(8000, 0)) { vad =>
      val data = Array.fill[Short](8000 * 20 / 1000)(0)
      assert(!vad.isSpeech(data))
      vad.close()
      intercept[IllegalStateException] {
        vad.isSpeech(data)
      }
    }

  }

}
