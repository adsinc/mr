package ru.kryptonite.audio.tools.processing

import org.apache.commons.io.IOUtils
import ru.kryptonite.commons.binary.{ Binary, BinaryInputStream, Reader, Writer }
import ru.kryptonite.commons.util.CloseableOps

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, OutputStream }
import javax.sound.sampled.AudioFormat.Encoding.{ ALAW, PCM_SIGNED }
import javax.sound.sampled.{ AudioFileFormat, AudioFormat, AudioInputStream, AudioSystem }

object AudioConverter extends CloseableOps {

  val DefaultFrequency = 16000f
  val DefaultBitrate   = 16

  def writeWave(data: Binary, os: OutputStream, channels: Int): Unit = {
    val frameSize   = channels * 2
    val numSamples  = data.size / frameSize
    val audioFormat = new AudioFormat(PCM_SIGNED, DefaultFrequency, 16, channels, frameSize, DefaultFrequency, false)
    val ais         = new AudioInputStream(BinaryInputStream(data), audioFormat, numSamples.toLong)
    val _           = AudioSystem.write(ais, AudioFileFormat.Type.WAVE, os)
  }

  def writeALAWWave(data: Binary, os: OutputStream, channels: Int): Unit = {
    val frameSize            = channels * 2
    val numSamples           = data.size / frameSize
    val alawFormat           = new AudioFormat(ALAW, DefaultFrequency / 2, 8, channels, frameSize, DefaultFrequency / 2, false)
    val alawAudioInputStream = new AudioInputStream(BinaryInputStream(data), alawFormat, numSamples.toLong)
    val pcm8Format =
      new AudioFormat(PCM_SIGNED, DefaultFrequency / 2, 16, channels, frameSize, DefaultFrequency / 2, false)
    val pcm8AudioInputStream  = AudioSystem.getAudioInputStream(pcm8Format, alawAudioInputStream)
    val pcm16Format           = new AudioFormat(PCM_SIGNED, DefaultFrequency, 16, channels, frameSize, DefaultFrequency, false)
    val pcm16AudioInputStream = AudioSystem.getAudioInputStream(pcm16Format, pcm8AudioInputStream)
    val _                     = AudioSystem.write(pcm16AudioInputStream, AudioFileFormat.Type.WAVE, os)
  }

  def convertToWave(data: Binary, channels: Int): Binary =
    withCloseable(new ByteArrayOutputStream()) { os =>
      writeWave(data, os, channels)
      Binary.owned(os.toByteArray)
    }

  def convertALAWToWave(data: Binary, channels: Int): Binary =
    withCloseable(new ByteArrayOutputStream()) { os =>
      writeALAWWave(data, os, channels)
      Binary.owned(os.toByteArray)
    }

  def readMono(data: Binary): Binary =
    withCloseable(AudioSystem.getAudioInputStream(new ByteArrayInputStream(data.copyToArray())))(readMono)

  def readMono(ais: AudioInputStream): Binary = {
    val fmt = ais.getFormat
    require(fmt.getChannels == 1, s"Expected mono audio stream, got stream with ${fmt.getChannels} channels")
    require(
      isFormatValid(fmt),
      s"Unsupported audio stream: only PCM (signed, little-endian) ${DefaultFrequency.toInt} HZ $DefaultBitrate bit is supported. Format: $fmt"
    )
    Binary.owned(IOUtils.toByteArray(ais))
  }

  def readLeft(data: Binary): Binary =
    readChannel(data, 0)

  def readRight(data: Binary): Binary =
    readChannel(data, 1)

  def readChannel(data: Binary, channelNum: Int): Binary = {
    val bis = new ByteArrayInputStream(data.copyToArray())
    withCloseable(AudioSystem.getAudioInputStream(bis)) { ais =>
      val channel = readChannelByIndex(channelNum)(ais)
      convertToWave(channel, 1)
    }
  }

  private def isFormatValid(fmt: AudioFormat): Boolean =
    fmt.getEncoding == AudioFormat.Encoding.PCM_SIGNED && !fmt.isBigEndian &&
      fmt.getSampleRate == DefaultFrequency &&
      fmt.getSampleSizeInBits == DefaultBitrate

  private def readChannelByIndex(channelIndex: Int)(ais: AudioInputStream): Binary = {
    val format = ais.getFormat
    require(
      channelIndex < format.getChannels,
      s"Invalid channel index $channelIndex for with with ${format.getChannels} channels"
    )
    require(
      isFormatValid(format),
      s"Unsupported audio stream: only PCM (signed, little-endian) ${DefaultFrequency.toInt} HZ $DefaultBitrate bit is supported. Format: $format"
    )

    val binary   = Binary.owned(IOUtils.toByteArray(ais))
    val nSamples = binary.size / format.getChannels / 2 // 16-bit samples
    val reader   = Reader(binary)
    val writer   = Writer.array(binary.size / format.getChannels)
    for {
      _ <- 0 until nSamples
      c <- 0 until format.getChannels
    } {
      val s = reader.readShortLE()
      if (c == channelIndex) {
        writer.writeShortLE(s)
      }
    }
    writer.toBinary
  }
}
