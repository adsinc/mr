package ru.kryptonite.audio.fvad.process

import breeze.linalg.{ BitVector, DenseVector }
import breeze.signal.{ convolve, OptOverhang }

final class InterpolatingFVadProcessor protected (
    sampleRate: Int,
    mode: Int,
    frameDurationMs: Int,
    paddingDurationMs: Int,
    margin: Float
) extends FVadProcessor(sampleRate, mode, frameDurationMs) {

  private val paddingFrames = paddingDurationMs / frameDurationMs

  override def calcPredictions(data: Array[Short]): BitVector = {
    val originalPredictions = super.calcPredictions(data)

    approximatePredictions(originalPredictions)
  }

  private def approximatePredictions(predictions: BitVector): BitVector = {
    val framesPerRecord      = predictions.length
    val predictionsFloat     = predictions.toDenseVector.map(p => if (p) 1.0f else 0f)
    val paddingKernel        = DenseVector.fill(paddingFrames)(1.0f / paddingFrames)
    val smoothPredictionsWnd = convolve(predictionsFloat, paddingKernel, overhang = OptOverhang.PreserveLength)
    val smoothPredictions    = smoothPredictionsWnd >:> margin

    val shift = paddingFrames / 2

    val rightShiftedPredictions = BitVector.zeros(framesPerRecord)
    rightShiftedPredictions(0 until framesPerRecord - shift) := smoothPredictions(shift until framesPerRecord)

    val leftShiftedPredictions = BitVector.zeros(framesPerRecord)
    leftShiftedPredictions(shift until framesPerRecord) := smoothPredictions(0 until framesPerRecord - shift)

    smoothPredictions |:| rightShiftedPredictions |:| leftShiftedPredictions
  }

}

object InterpolatingFVadProcessor {

  def apply(
      sampleRate: Int,
      mode: Int,
      frameDurationMs: Int = 10,
      paddingDurationMs: Int = 500,
      margin: Float = 0.5f
  ): InterpolatingFVadProcessor =
    new InterpolatingFVadProcessor(sampleRate, mode, frameDurationMs, paddingDurationMs, margin)

}
