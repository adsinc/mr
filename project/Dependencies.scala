import sbt._

object Dependencies {

  object Version {
    val Scala212 = "2.12.13"
    val Scala213 = "2.13.4"

    val ApacheCommons   = "1.9"
    val Breeze          = "1.0"
    val Jackson         = "2.12.1"
    val Twitter         = "20.12.0"
    val CommonsIO       = "2.8.0"
    val Logback         = "1.2.3"
    val NativeLibLoader = "2.3.4"
    val ScalaTest       = "3.2.3"
    val Mockito         = "1.16.13"
    val Scalacheck      = "1.15.2"
    val Scopt           = "4.0.0"
  }

  val commons = Def.setting(
    Seq(
      "commons-io" % "commons-io" % Version.CommonsIO
    )
  )

  val binary = Def.setting(
    Seq(
      "com.twitter" %% "util-core" % Version.Twitter
    )
  )

  val binaryTest = Def.setting(
    Seq(
      "ch.qos.logback"  % "logback-classic" % Version.Logback,
      "org.mockito"    %% "mockito-scala"   % Version.Mockito,
      "org.scalacheck" %% "scalacheck"      % Version.Scalacheck,
      "org.scalatest"  %% "scalatest"       % Version.ScalaTest
    ).map(_ % "test")
  )

  val fvad = Def.setting(
    Seq(
      "org.scalanlp" %% "breeze"            % Version.Breeze,
      "org.scijava"   % "native-lib-loader" % Version.NativeLibLoader
    )
  )

  val fvadTests = Def.setting(
    Seq(
      "commons-io"     % "commons-io" % Version.CommonsIO,
      "org.scalatest" %% "scalatest"  % Version.ScalaTest
    ).map(_ % "test") ++
      Seq(
        "ch.qos.logback" % "logback-classic" % Version.Logback % "runtime"
      )
  )

  val tools = Def.setting(
    Seq(
      "commons-codec"                 % "commons-codec"        % Version.ApacheCommons,
      "commons-io"                    % "commons-io"           % Version.CommonsIO,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % Version.Jackson,
      "ch.qos.logback"                % "logback-classic"      % Version.Logback,
      "com.github.scopt"             %% "scopt"                % Version.Scopt
    )
  )

  val toolsTests = Def.setting(
    Seq(
      "org.scalatest" %% "scalatest" % Version.ScalaTest
    ).map(_ % "test")
  )

}
