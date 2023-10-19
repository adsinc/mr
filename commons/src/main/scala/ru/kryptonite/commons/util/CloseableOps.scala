package ru.kryptonite.commons.util

import scala.util.{ Failure, Success, Try }

trait CloseableOps {

  /**
   *  Performs specified computation on a `java.lang.AutoCloseable` closing it afterward.
   *
   *  if `body` fails, possible failure in `close()` is ignored to propagate original exception.
   *
   *  @param open
   *  @param body
   *  @tparam C
   *  @tparam T
   *  @return
   */
  def withCloseable[C <: AutoCloseable, T](open: => C)(body: C => T): T = {
    val c = open
    Try(body(c)) match {
      case Success(r) =>
        c.close()
        r
      case Failure(ex) =>
        Try(c.close())
        throw ex;
    }
  }

  /**
   *  Performs specified computation on the result of `open` function if it's defined.
   *
   *  if `body` fails, possible failure in `close()` is ignored to propagate original exception.
   *
   *  @param open
   *  @param body
   *  @tparam C
   *  @tparam T
   *  @return
   */
  def withMaybeCloseable[C <: AutoCloseable, T](open: => Option[C])(body: C => T): Option[T] =
    for (c <- open) yield {
      Try(body(c)) match {
        case Success(r) =>
          c.close()
          r
        case Failure(ex) =>
          Try(c.close())
          throw ex;
      }
    }

}

object CloseableOps extends CloseableOps
