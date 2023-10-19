package ru.kryptonite.audio.fvad

import org.scijava.nativelib.NativeLoader

private[fvad] object FVadJni {

  private lazy val libLoaded: Unit =
    NativeLoader.loadLibrary("fvad-jni")

  def ensureLoaded(): Unit = libLoaded

}
