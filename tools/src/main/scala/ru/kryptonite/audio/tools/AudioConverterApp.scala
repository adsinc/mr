package ru.kryptonite.audio.tools

import org.slf4j.LoggerFactory
import ru.kryptonite.audio.tools.processing.commands._
import ru.kryptonite.commons.util.CloseableOps
import scopt.OptionParser

import java.io.File

class AudioConverterApp(args: AudioConverterApp.Args) extends CloseableOps {

  val log = LoggerFactory.getLogger(this.getClass)

  def run(): Unit =
    try {
      args.cmd match {
        case Vad.Filter =>
          val vadFilter = VadFiltration(args.in, args.mode, args.frameDurationMs)
          val channels  = vadFilter.filter()
          vadFilter.saveChannels(channels, args.out)
        case Convert =>
          val converter = Conversion(args.in, args.channel)
          converter.convertAndSave(args.out)
        case Vad.Chunks =>
          val vadChunks = VadChunking(args.in, args.out, args.mode, args.frameDurationMs)
          vadChunks.chunks()
        case _ =>
      }
    } catch {
      case ex: Throwable =>
        log.error(s"Failed to ${args.cmd} $ex")
    }

}

object AudioConverterApp extends App {

  val AcceptableModes     = List(0, 1, 2, 3)
  val AcceptableDurations = List(10, 20, 30)

  case class Args(
      cmd: Command = Empty,
      in: File = new File(""),
      out: File = new File(""),
      channel: Int = 0,
      mode: Int = 0,
      frameDurationMs: Int = 10
  )

  val parser = new OptionParser[Args]("audio-converter.sh") {
    help('h', "help")
    head("Convert audio")
    opt[File]('1', "in")
      .text("Input file (required)")
      .required()
      .action((x, args) => args.copy(in = x))
    opt[File]('2', "out")
      .text("Output file (required)")
      .required()
      .action((x, args) => args.copy(out = x))
    cmd("convert")
      .action((_, args) => args.copy(cmd = Convert))
      .children(
        opt[Int]('c', "channel")
          .text(s"Index of channel inside input file (default: 0)")
          .optional()
          .action((x, args) => args.copy(channel = x)),
      )
    cmd("vad")
      .children(
        cmd("filter")
          .action((_, args) => args.copy(cmd = Vad.Filter)),
        cmd("chunks")
          .action((_, args) => args.copy(cmd = Vad.Chunks)),
        opt[Int]('m', name = "mode")
          .validate(x =>
            if (AcceptableModes.contains(x))
              success
            else
              failure(
                "Valid modes are 0 (\"quality\"), 1 (\"low bitrate\"), 2 (\"aggressive\"), and 3 (\"very aggressive\")"
              )
          )
          .text("mode of a VAD instance")
          .action((x, args) => args.copy(mode = x)),
        opt[Int]('f', name = "frameDurationMs")
          .validate(x =>
            if (AcceptableDurations.contains(x))
              success
            else
              failure(
                "Only frames with a length of 10, 20 or 30 ms are supported"
              )
          )
          .text("Duration of frame in ms")
          .action((x, args) => args.copy(frameDurationMs = x))
      )
    checkConfig(c =>
      if (c.cmd == Empty) failure("Missing command")
      else success
    )

  }

  parser.parse(args, Args()) match {
    case Some(args) =>
      new AudioConverterApp(args).run()
    case None =>
    // Do nothing
  }

}
