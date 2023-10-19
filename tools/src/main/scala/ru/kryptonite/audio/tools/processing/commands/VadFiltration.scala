package ru.kryptonite.audio.tools.processing.commands

import org.apache.commons.io.IOUtils
import ru.kryptonite.audio.fvad.process.FVadProcessor
import ru.kryptonite.audio.tools.processing.AudioConverter
import ru.kryptonite.audio.tools.processing.AudioConverter.withCloseable
import ru.kryptonite.commons.binary.{ Binary, Reader, Writer }

import java.io.{ ByteArrayInputStream, File, FileInputStream, FileOutputStream }
import javax.sound.sampled.AudioSystem
import scala.collection.mutable
import scala.util.{ Failure, Try }

case class VadFiltration(in: File, mode: Int, frameDuration: Int) {

  def filter(): IndexedSeq[Binary] =
    withCloseable(new FileInputStream(in)) { is =>
      val bytes  = IOUtils.toByteArray(is)
      val binary = Binary.owned(bytes)

      val bis    = new ByteArrayInputStream(bytes)
      val ais    = AudioSystem.getAudioInputStream(bis)
      val format = ais.getFormat

      val vadProcessor = FVadProcessor(AudioConverter.DefaultFrequency.toInt, mode, frameDuration)

      (0 until format.getChannels)
        .map(i => AudioConverter.readChannel(binary, i))
        .map { bin =>
          val arrayReader = Reader(bin)
          val shortArray  = mutable.ArrayBuilder.make[Short]
          while (arrayReader.remaining > 0) {
            shortArray += arrayReader.readShortLE()
          }
          shortArray.result()
        }
        .map(vadProcessor.removeSilence)
        .map { filteredShort =>
          val writer = Writer.array(filteredShort.length * 2)
          for (s <- filteredShort)
            writer.writeShortLE(s)
          writer.toBinary
        }
    }

  def saveChannels(channels: IndexedSeq[Binary], out: File): Unit = {
    val (fileName, extension) = out.getName.split("\\.") match {
      case Array(name, "wav") => (name, ".wav")
      case _                  => throw new IllegalArgumentException(s"Unsupported file name. Expected: <file_name>.wav")
    }
    channels.zipWithIndex
      .foreach {
        case (binary, channelInd) =>
          val suffix = s"_$channelInd"
          val parentPath = Option(out.getParent) match {
            case Some(s) => s + File.separator
            case None    => ""
          }
          val newFileName = parentPath + fileName + suffix + extension
          val indexedOut  = new File(newFileName)
          withCloseable(new FileOutputStream(indexedOut)) { os =>
            Try(AudioConverter.writeWave(binary, os, 1)) match {
              case Failure(ex) =>
                println(s"Failed to save channel:$channelInd\n")
                throw ex
              case _ =>
            }
          }
      }
  }

}
