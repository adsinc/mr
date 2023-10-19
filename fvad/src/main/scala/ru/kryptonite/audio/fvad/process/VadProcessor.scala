package ru.kryptonite.audio.fvad.process

trait VadProcessor {

  def removeSilence(data: Array[Short]): Array[Short]
  def process(data: Array[Short]): IndexedSeq[(Array[Short], Boolean)]

}
