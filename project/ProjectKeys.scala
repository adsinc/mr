import sbt.Keys._
import sbt._

import scala.sys.process._

object ProjectKeys {

  // Settings
  lazy val distRoot = SettingKey[File]("dist-root", "The root directory of distributive")

  lazy val copyFiles = SettingKey[Seq[(String, String)]]("copy-files", "Additional files/directories to copy into dist")

  lazy val executableFiles =
    SettingKey[Seq[String]]("executable-files", "List of files in dist to set executable permission")

  lazy val distJar     = SettingKey[Option[String]]("dist-jar", "Name of jar to add to dist or don't add jar")
  lazy val distWithJar = distJar := Some(name.value + ".jar")

  lazy val distLibs = SettingKey[Option[String]](
    "dist-libs",
    "Relative path of dependecies to add to dependencies or don't add dependecies"
  )
  lazy val distWithLibs = distLibs := Some("lib")

  lazy val distVer     = SettingKey[Boolean]("dist-ver", "Should the version info be added to dist")
  lazy val distWithVer = distVer := true

  lazy val dockerLogin    = SettingKey[String]("docker-login", "Login to docker registry")
  lazy val dockerPassword = SettingKey[String]("docker-password", "Password for docker registry")
  lazy val dockerRegistry = SettingKey[String]("docker-registry", "Host name of docker registry")

  lazy val PerfConfig = config("perf") extend Test
  lazy val PerfSettings = inConfig(PerfConfig)(Defaults.testSettings) ++ Seq(
    parallelExecution in PerfConfig := false,
    scalaSource in PerfConfig := baseDirectory.value / "src/perf/scala",
    compile in PerfConfig := (compile in PerfConfig).dependsOn(compile in IntegrationTest).value,
    dependencyClasspath in PerfConfig := (dependencyClasspath in IntegrationTest).value
  )

  // Tasks
  lazy val distTask = TaskKey[sbt.File]("dist", "Create distributive") := {
    val baseDir       = baseDirectory.value
    val distDir       = (distRoot ?? file("dist")).value
    val moduleDistDir = if (baseDir.getCanonicalPath == file(".").getCanonicalPath) distDir else distDir / baseDir.name

    // Maybe copy jar
    val jar = (Keys.`package` in Compile).value
    for (jarName <- (distJar ?? None).value) {
      IO.copyFile(jar, moduleDistDir / jarName, preserveLastModified = true)
    }

    // Maybe copy libs
    val libs = (dependencyClasspath in Runtime).value
    for (libPath <- (distLibs ?? None).value) {
      val fullLibPath = moduleDistDir / libPath
      libs.map(_.data).filter(_.isFile).filter(_.name != jar.name).foreach { src =>
        val dest = fullLibPath / src.getName
        IO.copyFile(src, dest, preserveLastModified = true)
      }
    }

    // Maybe copy additional files
    val addFiles = (copyFiles ?? Seq()).value
    addFiles.foreach {
      case (src, dest) =>
        val fsrc  = baseDir / src
        val fdest = moduleDistDir / dest
        if (fsrc.isDirectory) {
          IO.copyDirectory(fsrc, fdest, preserveLastModified = true)
        } else {
          IO.copyFile(fsrc, fdest, preserveLastModified = true)
        }
    }

    // Maybe chmod
    val binFiles = (executableFiles ?? Seq()).value
    binFiles.foreach { s =>
      val f = moduleDistDir / s
      f.setExecutable(true, false)
    }

    // Maybe write version info
    if ((distVer ?? false).value) {
      val projectVer            = version.value
      val normalizedProjectName = normalizedName.value
      IO.write(moduleDistDir / "PROJECT", normalizedProjectName)
      IO.write(moduleDistDir / "VERSION", projectVer)
    }
    distDir
  }

  lazy val loginDocker = TaskKey[Unit]("loginDocker", "Login to docker repo") := {
    val login  = dockerLogin.value
    val passwd = dockerPassword.value
    val reg    = dockerRegistry.value
    val cmd    = s"""docker login -u $login -p $passwd $reg"""
    val res    = cmd !

    if (res != 0) {
      throw new RuntimeException(s"Non-zero exit code: $res")
    }
  }

}
