package ru.kryptonite.audio.tools.processing.commands

sealed trait Command

case object Convert extends Command

sealed trait Vad extends Command

object Vad {
  case object Filter extends Vad

  case object Chunk extends Vad
}

case object Empty extends Command
