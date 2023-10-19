package ru.kryptonite.commons.io

import org.apache.commons.io.IOUtils
import ru.kryptonite.commons.util.CloseableOps

import java.io.{ File, FileInputStream, FileNotFoundException, InputStream }
import java.net.URI
import java.nio.charset.StandardCharsets

class ResourceLoader(
    schemeHandlers: Map[String, ResourceLoader.SchemeHandler],
    noSchemeHandler: Option[ResourceLoader.SchemeHandler]
) extends CloseableOps {

  def exists(resourceName: String): Boolean =
    exists(new URI(resourceName))

  def exists(resourceUri: URI): Boolean =
    schemeHandler(resourceUri).exists(resourceUri)

  def stream(resourceName: String): InputStream =
    stream(new URI(resourceName))

  def stream(resourceUri: URI): InputStream =
    streamIfExists(resourceUri)
      .getOrElse(throw new IllegalArgumentException(s"Resource $resourceUri not found"))

  def streamIfExists(resourceName: String): Option[InputStream] =
    streamIfExists(new URI(resourceName))

  def streamIfExists(resourceUri: URI): Option[InputStream] =
    schemeHandler(resourceUri).apply(resourceUri)

  def withStream[T](resourceName: String)(body: InputStream => T): T =
    withCloseable(stream(resourceName))(body)

  def withStream[T](resourceUri: URI)(body: InputStream => T): T =
    withCloseable(stream(resourceUri))(body)

  def withStreamIfExists[T](resourceName: String)(body: InputStream => T): Option[T] =
    withMaybeCloseable(streamIfExists(resourceName))(body)

  def withStreamIfExists[T](resourceUri: URI)(body: InputStream => T): Option[T] =
    withMaybeCloseable(streamIfExists(resourceUri))(body)

  def resourceAsString(resourceName: String): String =
    withStream(resourceName)(is => IOUtils.toString(is, StandardCharsets.UTF_8))

  def resourceAsString(resourceUri: URI): String =
    withStream(resourceUri)(is => IOUtils.toString(is, StandardCharsets.UTF_8))

  def resourceAsStringIfExists(resourceName: String): Option[String] =
    withStreamIfExists(resourceName)(is => IOUtils.toString(is, StandardCharsets.UTF_8))

  def resourceAsStringIfExists(resourceUri: URI): Option[String] =
    withStreamIfExists(resourceUri)(is => IOUtils.toString(is, StandardCharsets.UTF_8))

  private def schemeHandler(resourceUri: URI): ResourceLoader.SchemeHandler = {
    val scheme = Option(resourceUri.getScheme).map(_.toLowerCase)
    scheme match {
      case Some(s) =>
        schemeHandlers.getOrElse(s, throw new IllegalArgumentException(s"No handler defined for scheme $s"))
      case None =>
        noSchemeHandler.getOrElse(throw new IllegalArgumentException("No handler defined for empty scheme"))
    }
  }
}

object ResourceLoader {

  trait SchemeHandler {

    def apply(uri: URI): Option[InputStream]

    def exists(uri: URI): Boolean
  }

  object ClasspathHandler extends SchemeHandler {

    override def apply(uri: URI): Option[InputStream] = {
      val path = uri.getSchemeSpecificPart
      Option(getClass.getResourceAsStream(path))
    }

    override def exists(uri: URI): Boolean = {
      val path = uri.getSchemeSpecificPart
      getClass.getResource(path) != null
    }

  }

  object FileHandler extends SchemeHandler {

    override def apply(uri: URI): Option[InputStream] = try {
      val path = uri.getSchemeSpecificPart
      Option(new FileInputStream(path))
    } catch {
      case _: FileNotFoundException =>
        None
    }

    override def exists(uri: URI): Boolean = {
      val path = uri.getSchemeSpecificPart
      new File(path).exists()
    }
  }

  object UriHandler extends SchemeHandler {

    override def apply(uri: URI): Option[InputStream] = try {
      Option(uri.toURL.openStream)
    } catch {
      case _: FileNotFoundException =>
        None
    }

    override def exists(uri: URI): Boolean = try {
      // Must open the connection to check if it exists
      CloseableOps.withCloseable(uri.toURL.openStream)(_ => true)
    } catch {
      case _: FileNotFoundException =>
        false
    }
  }

  val defaultHandlers: Map[String, SchemeHandler] = Map(
    "file"      -> FileHandler,
    "classpath" -> ClasspathHandler,
    "http"      -> UriHandler,
    "https"     -> UriHandler
  )

  val default: ResourceLoader = {
    new ResourceLoader(defaultHandlers, Some(FileHandler))
  }

}
