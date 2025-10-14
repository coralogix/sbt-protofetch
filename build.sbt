val versions = new {
  val scala212        = "2.12.20"
  val sbt             = "1.5.8"
  val commonsCompress = "1.28.0"
  val toml4j          = "0.7.3"
  val munit           = "1.2.1"
}

val common: Seq[Setting[_]] = Seq(
  organizationName := "Coralogix Ltd.",
  organization     := "com.coralogix",
  startYear        := Some(2024),
  licenses += ("Apache-2.0", url(
    "https://www.apache.org/licenses/LICENSE-2.0.html"
  )),
  headerLicense := Some(HeaderLicense.ALv2("2024", "Coralogix Ltd.")),
  homepage      := Some(url("https://github.com/coralogix/sbt-protofetch")),
  scalaVersion  := versions.scala212,
  versionScheme := Some("early-semver"),
  developers := List(
    Developer(
      "rtimush",
      "Roman Timushev",
      "roman.timushev@coralogix.com",
      url("https://www.coralogix.com")
    ),
    Developer(
      "m-kalai",
      "Marcel Kalai",
      "marcel.kalai@coralogix.com",
      url("https://www.coralogix.com")
    )
  )
)

lazy val `sbt-protofetch` = (project in file("sbt-protofetch"))
  .enablePlugins(SbtPlugin)
  .settings(common)
  .settings(
    crossScalaVersions := Seq(versions.scala212),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" =>
          versions.sbt
        case _ =>
          "2.0.0-M2" // just a preparation for Scala 3 / sbt 2
      }
    },
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-compress" % versions.commonsCompress,
      "io.hotmoka"         % "toml4j"           % versions.toml4j,
      "org.scalameta"     %% "munit"            % versions.munit % Test
    ),
    scriptedLaunchOpts ++= Seq(
      "-Xmx1024M",
      "-Dsbt.protofetch.version=" + version.value
    )
  )

lazy val root = (project in file("."))
  .aggregate(`sbt-protofetch`)
  .settings(common)
  .settings(
    publish / skip               := true,
    compile / skip               := true,
    scalafmtAll / aggregate      := false,
    scalafmtSbt / aggregate      := false,
    scalafmtCheckAll / aggregate := false,
    scalafmtSbtCheck / aggregate := false
  )
