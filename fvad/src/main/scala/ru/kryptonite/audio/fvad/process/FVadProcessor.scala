package ru.kryptonite.audio.fvad.process

import breeze.linalg.BitVector
import ru.kryptonite.audio.fvad.FVad

import scala.collection.mutable

class FVadProcessor protected (
    sampleRate: Int,
    mode: Int,
    frameDurationMs: Int
) extends VadProcessor
    with AutoCloseable {

  private val vad = FVad(sampleRate, mode)

  protected val samplesPerFrame: Int = frameDurationMs * sampleRate / 1000

  override def removeSilence(data: Array[Short]): Array[Short] = {
    val predictions = calcPredictions(data)

    val speech = mutable.ArrayBuilder.make[Short]
    predictions.activeKeysIterator.foreach { i =>
      speech ++= data.slice(i * samplesPerFrame, (i + 1) * samplesPerFrame)
    }
    speech.result()
  }

  override def process(data: Array[Short]): IndexedSeq[(Array[Short], Boolean)] = {
    val predictions = calcPredictions(data)

    predictions
      .mapPairs { (i, prediction) =>
        (data.slice(i * samplesPerFrame, (i + 1) * samplesPerFrame), prediction)
      }
      .toScalaVector()
  }

  protected def calcPredictions(data: Array[Short]): BitVector = {
    val framesPerRecord = data.length / samplesPerFrame

    val vadPredictions = BitVector.zeros(framesPerRecord)
    (0 until framesPerRecord).foreach { i =>
      if (vad.isSpeech(data.slice(i * samplesPerFrame, (i + 1) * samplesPerFrame)))
        vadPredictions(i) = true
    }

    vadPredictions
  }

  override def close(): Unit =
    vad.close()

}

object FVadProcessor {

  def apply(
      sampleRate: Int,
      mode: Int,
      frameDurationMs: Int = 10
  ): FVadProcessor = new FVadProcessor(sampleRate, mode, frameDurationMs)

}
