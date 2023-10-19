package ru.kryptonite.audio.tools.processing.commands

import org.apache.commons.io.IOUtils
import ru.kryptonite.audio.tools.processing.AudioConverter
import ru.kryptonite.audio.tools.processing.AudioConverter.withCloseable
import ru.kryptonite.commons.binary.Binary

import java.io.{ File, FileInputStream, FileOutputStream }

case class Conversion(in: File, channelIndex: Int) {

  def convert(): Binary =
    withCloseable(new FileInputStream(in)) { is =>
      val bytes  = IOUtils.toByteArray(is)
      val binary = Binary.owned(bytes)
      AudioConverter.readChannel(binary, channelIndex)
    }

  def convertAndSave(out: File): Unit =
    withCloseable(new FileOutputStream(out)) { out =>
      val channel = convert()
      AudioConverter.writeWave(channel, out, 1)
    }
}
