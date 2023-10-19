import sbtrelease.ReleasePlugin.autoImport.{ releaseStepCommand, ReleaseStep }

object Release {
  lazy val sbtDist: ReleaseStep = releaseStepCommand("dist")
}
