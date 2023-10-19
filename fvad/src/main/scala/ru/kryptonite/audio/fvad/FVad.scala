package ru.kryptonite.audio.fvad

import scala.annotation.nowarn

/** A stateful Vad instance that detects voice in a series of audio frames.
 *
 *  @param sampleRate Input sample rate in Hz for a VAD instance. Valid values are 8000, 16000, 32000 and 48000.
 *                    Note that internally all processing will be done 8000 Hz; input data in higher
 *                    sample rates will just be downsampled first.
 *  @param mode       Changes the VAD operating ("aggressiveness") mode of a VAD instance.
 *                    A more aggressive (higher mode) VAD is more restrictive in reporting speech.
 *                    Put in other words the probability of being speech when the VAD returns 1 is
 *                    increased with increasing mode. As a consequence also the missed detection rate goes up.
 *                    Valid modes are 0 ("quality"), 1 ("low bitrate"), 2 ("aggressive"), and 3 ("very aggressive").
 */
class FVad private (sampleRate: Int, mode: Int) extends AutoCloseable {

  private var state: Long = {
    val r = init(sampleRate, mode)
    if (r == 0) {
      throw new IllegalArgumentException(s"Couldn't construct Vad instance with sampleRate $sampleRate and mode $mode")
    }
    r
  }

  /** Calculates a VAD decision for an audio frame.
   *
   *  Only frames with a length of 10, 20 or 30 ms are supported, so for example at 8 kHz, `length`
   *  must be either 80, 160 or 240.
   *
   *  @param data The audio frame
   *  @return true if voice is detected, false otherwise
   */
  def isSpeech(data: Array[Short]): Boolean = {
    checkInitialized()
    process(state, data) match {
      case 1 => true
      case 0 => false
      case -1 =>
        throw new IllegalArgumentException(
          s"Couldn't process frame with ${data.length} audio samples, " +
            s"Vad sample rate: ${sampleRate}"
        )
    }
  }

  /** Reinitializes a VAD instance, clearing all state. */
  def reset(): Unit = {
    checkInitialized()
    _reset(state, sampleRate, mode)
  }

  override def close(): Unit = {
    _close(state)
    state = 0
  }

  private def checkInitialized(): Unit =
    if (state == 0) {
      throw new IllegalStateException("Illegal state: Vad is either not initialized or closed")
    }

  @native protected def init(@nowarn sampleRate: Int, @nowarn mode: Int): Long

  @native protected def process(@nowarn state: Long, @nowarn data: Array[Short]): Int

  @native protected def _reset(@nowarn state: Long, @nowarn sampleRate: Int, @nowarn mode: Int): Unit

  @native protected def _close(@nowarn state: Long): Unit

}

object FVad {

  /** Constructs a stateful Vad instance that detects voice in a series of audio frames.
   *
   *  @param sampleRate Input sample rate in Hz for a VAD instance. Valid values are 8000, 16000, 32000 and 48000.
   *                    Note that internally all processing will be done 8000 Hz; input data in higher
   *                    sample rates will just be downsampled first.
   *  @param mode       Changes the VAD operating ("aggressiveness") mode of a VAD instance.
   *                    A more aggressive (higher mode) VAD is more restrictive in reporting speech.
   *                    Put in other words the probability of being speech when the VAD returns 1 is
   *                    increased with increasing mode. As a consequence also the missed detection rate goes up.
   *                    Valid modes are 0 ("quality"), 1 ("low bitrate"), 2 ("aggressive"), and 3 ("very aggressive").
   */
  def apply(sampleRate: Int, mode: Int): FVad = {
    FVadJni.ensureLoaded()
    new FVad(sampleRate, mode)
  }
}
