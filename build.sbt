import ReleaseTransformations._
import Dependencies.Version
import ProjectKeys._

lazy val IntegrationTest = config("it").extend(Test)

addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias("lint", "compile:scalafix --check ; test:scalafix --check")

inThisBuild(
  Seq(
    organization := "ru.kryptonite",
    scalaVersion := Version.Scala213,
    scalacOptions ++= Seq(
      "-encoding",
      "utf-8",           // Source files encoding.
      "-target:jvm-1.8", // Target JVM 8
    ),
    javacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-source",
      "1.8"
    ),
    Compile / compile / javacOptions ++= Seq(
      "-target",
      "1.8",
      "-Xlint:deprecation" // Print deprecation warning details.
    ),
    autoAPIMappings := true, // will use external ScalaDoc links for managed dependencies
    updateOptions := updateOptions.value.withCachedResolution(true),
    addCompilerPlugin(scalafixSemanticdb),
  )
)

lazy val commons = Project(id = "commons", base = file("commons"))
  .settings(
    scalaVersion := "2.13.4",
    crossScalaVersions := Vector(scalaVersion.value, "2.11.12", "2.12.12"),
    releaseCrossBuild := false,
    libraryDependencies ++= Dependencies.commons.value,
  )

lazy val binary = Project(id = "binary", base = file("binary"))
  .settings(
    scalaVersion := "2.13.4",
    crossScalaVersions := Vector(scalaVersion.value, "2.11.12", "2.12.12"),
    releaseCrossBuild := true,
    Compile / exportJars := true,
    libraryDependencies ++= Dependencies.binary.value,
    libraryDependencies ++= Dependencies.binaryTest.value,
    Test / parallelExecution := false,
  )

lazy val fvad = Project(id = "audio-fvad", base = file("fvad"))
  .dependsOn(commons)
  .settings(
    crossScalaVersions := Vector(Version.Scala213, Version.Scala212),
    libraryDependencies ++= Dependencies.fvad.value,
    libraryDependencies ++= Dependencies.fvadTests.value,
    exportJars := true,
    Test / parallelExecution := false,
    javah / target := file("fvad/native/src"),
  )

lazy val tools = Project(id = "audio-tools", base = file("tools"))
  .dependsOn(commons, binary, fvad)
  .settings(
    crossScalaVersions := Vector(Version.Scala213, Version.Scala212),
    distTask,
    distWithJar,
    distWithLibs,
    Compile / exportJars := true,
    libraryDependencies ++= Dependencies.tools.value,
    libraryDependencies ++= Dependencies.toolsTests.value,
  )

lazy val root = Project(id = "audio-toolkit", base = file("."))
  .aggregate(fvad, tools, commons, binary)
  .dependsOn(tools)
  .enablePlugins(GitBranchPrompt)
  .settings(
    Compile / run / mainClass := Some("ru.kryptonite.audio.tools.AudioConverterApp"),
    crossScalaVersions := Nil,
    cleanFiles += baseDirectory.value / "dist",
    copyFiles := Seq(
      "src/main/bin" -> "bin",
    ),
    distTask,
    distWithVer,
    publish / skip := true,
    releaseCrossBuild := false, // set to `false` for non-cross build projects
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publish"),
      Release.sbtDist,
      setNextVersion,
      commitNextVersion,
      pushChanges
    ),
  )
