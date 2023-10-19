package ru.kryptonite.commons.binary

import java.io.OutputStream
import java.util.concurrent.atomic.AtomicInteger

private[binary] final class StreamWriter(os: OutputStream) extends Writer {

  private val numWritten = new AtomicInteger(0)

  override def length: Int = numWritten.get()

  override def writeByte(value: Byte): StreamWriter.this.type = {
    os.write(value.toInt)
    numWritten.incrementAndGet()
    this
  }

  /**
   *  Ensures that all data is written to underlying storage
   *
   *  @return
   */
  override def flush(): Unit =
    os.flush()

  override protected def ensureRemaining(needed: Int): Unit = ()

}
