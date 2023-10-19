package ru.kryptonite.audio.fvad.process

import org.apache.commons.io.IOUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import ru.kryptonite.commons.io.ResourceLoader
import ru.kryptonite.commons.util.CloseableOps

import java.io.{ BufferedInputStream, ByteArrayInputStream, File }
import java.nio.ByteBuffer
import java.nio.file.{ Files, Paths }
import javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
import javax.sound.sampled.{ AudioFileFormat, AudioFormat, AudioInputStream, AudioSystem }
import scala.util.Random

class VadProcessorSpec extends AnyFunSpec with Matchers with CloseableOps with BeforeAndAfterAll {

  val SampleRate = 8000
  val SaveDir    = ".local"

  override def beforeAll(): Unit = {
    Files.createDirectories(Paths.get(SaveDir))
    ()
  }

  describe("VadProcessor") {

    def processSilence(processor: => VadProcessor with AutoCloseable): Unit =
      it(s"should return empty array for silence with ${processor.getClass.getSimpleName}") {
        withCloseable(processor) { vad =>
          val data = Array.fill[Short](SampleRate * 2000 / 1000)(0)

          val speech = vad.removeSilence(data)

          assert(speech.isEmpty)

          val processed = vad.process(data)

          assert(processed.nonEmpty)
          assert(processed.forall(_._2 == false))
        }
      }

    def processVoice(processor: => VadProcessor with AutoCloseable): Unit =
      it(s"should return given array for voice with ${processor.getClass.getSimpleName}") {
        withCloseable(processor) { vad =>
          val rnd  = new Random()
          val data = Array.fill[Short](SampleRate * 2000 / 1000)(rnd.nextInt(Short.MaxValue).toShort)

          val speech = vad.removeSilence(data)

          assert(speech.nonEmpty)
          assertResult(data)(speech)

          val processed = vad.process(data)

          assert(processed.nonEmpty)
          assert(processed.forall(_._2))
        }
      }

    def processSample(processor: => VadProcessor with AutoCloseable): Unit =
      it(s"should cut silence with ${processor.getClass.getSimpleName}") {
        withCloseable(processor) { vad =>
          val data = readWave("sample.wav")

          val speech = vad.removeSilence(data)

          assert(speech.nonEmpty)
          assert(speech.length < data.length)

          val speechBytes = ByteBuffer.allocate(speech.length * 2)
          speech.foreach(speechBytes.putShort)
          writeWave(speechBytes.array(), new File(s"$SaveDir/${processor.getClass.getSimpleName}.wav"), 1)

          val processed = vad.process(data)

          assert(processed.nonEmpty)
          assert(processed.exists(_._2))
          assert(!processed.forall(_._2))
        }
      }

    describe("FVadProcessor") {
      it.should(behave)
        .like(
          processSilence(FVadProcessor(SampleRate, 0))
        )

      it.should(behave)
        .like(
          processVoice(FVadProcessor(SampleRate, 0))
        )

      it.should(behave)
        .like(
          processSample(FVadProcessor(SampleRate, 3))
        )
    }

    describe("InterpolatingVadProcessor") {
      it.should(behave)
        .like(
          processSilence(InterpolatingFVadProcessor(SampleRate, 0))
        )

      it.should(behave)
        .like(
          processVoice(InterpolatingFVadProcessor(SampleRate, 0))
        )

      it.should(behave)
        .like(
          processSample(InterpolatingFVadProcessor(SampleRate, 3))
        )
    }

  }

  private def readWave(path: String): Array[Short] =
    ResourceLoader.default.withStream(s"classpath:/$path") { fis =>
      withCloseable(AudioSystem.getAudioInputStream(new BufferedInputStream(fis))) { ais =>
        IOUtils
          .toByteArray(ais)
          .grouped(2)
          .flatMap {
            case Array(hi, lo) => Some((hi << 8 | lo).toShort)
            case _             => None
          }
          .toArray
      }
    }

  private def writeWave(data: Array[Byte], file: File, channels: Int): Unit = {
    val numSamples  = data.length / (channels * 2)
    val sampleRate  = SampleRate.toFloat
    val audioFormat = new AudioFormat(PCM_SIGNED, sampleRate, 16, channels, channels * 2, sampleRate, false)
    val ais         = new AudioInputStream(new ByteArrayInputStream(data), audioFormat, numSamples.toLong)
    val _           = AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file)
  }

}
