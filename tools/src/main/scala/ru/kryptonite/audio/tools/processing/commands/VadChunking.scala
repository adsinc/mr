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

case class VadChunking(in: File, out: File, mode: Int, frameDuration: Int) {

  def chunks(): Unit =
    withCloseable(new FileInputStream(in)) { is =>
      val bytes  = IOUtils.toByteArray(is)
      val binary = Binary.owned(bytes)

      val bis    = new ByteArrayInputStream(bytes)
      val ais    = AudioSystem.getAudioInputStream(bis)
      val format = ais.getFormat

      var channelInd, fileNum = 0
      val vadProcessor        = FVadProcessor(AudioConverter.DefaultFrequency.toInt, mode, frameDuration)

      val (fileName, extension) = out.getName.split("\\.") match {
        case Array(name, "wav") => (name, ".wav")
        case _                  => throw new IllegalArgumentException(s"Unsupported file name. Expected: <file_name>.wav")
      }

      (0 until format.getChannels)
        .map { i =>
          channelInd = i
          AudioConverter.readChannel(binary, i)
        }
        .map { bin =>
          val arrayReader = Reader(bin)
          val shortArray  = mutable.ArrayBuilder.make[Short]
          while (arrayReader.remaining > 0) {
            shortArray += arrayReader.readShortLE()
          }
          shortArray.result()
        }
        .map(vadProcessor.process)
        .foreach {
          fileNum = 0
          seq =>
            for ((chunkedShort, isVoice) <- seq; if isVoice) {
              fileNum += 1
              val writer = Writer.array(chunkedShort.length * 2)
              for (s <- chunkedShort)
                writer.writeShortLE(s)
//              writer.toBinary

              val suffix_1 = s"_$channelInd"
              val suffix_2 = s"_$fileNum"
              val parentPath = Option(out.getParent) match {
                case Some(s) => s + File.separator
                case None    => ""
              }
              val newFileName = parentPath + fileName + suffix_1 + suffix_2 + extension
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
}
