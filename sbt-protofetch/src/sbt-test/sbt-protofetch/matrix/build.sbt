lazy val protos = (project in file("protos"))
  .enablePlugins(ProtofetchPlugin)
  .settings(
    protofetchModuleFile := baseDirectory.value / ".." / "protofetch.toml"
  )

lazy val app = (projectMatrix in file("app"))
  .jvmPlatform(scalaVersions = Seq("2.13.12", "3.3.1"))
  .settings(
    Compile / PB.protoSources += (protos / protofetchOutputDirectory).value,
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb"
    ),
    Compile / PB.generate := (Compile / PB.generate)
      .dependsOn(protos / protofetchFetch)
      .value,
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    )
  )
