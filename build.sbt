val versions = new {
  val sbt             = "1.6.0"
  val commonsCompress = "1.27.1"
  val toml4j          = "0.7.3"
  val munit           = "1.0.2"
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
  version       := "0.1.0-SNAPSHOT"
)

lazy val `sbt-protofetch` = (project in file("sbt-protofetch"))
  .enablePlugins(SbtPlugin)
  .settings(common)
  .settings(
    common,
    pluginCrossBuild / sbtVersion := versions.sbt,
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
