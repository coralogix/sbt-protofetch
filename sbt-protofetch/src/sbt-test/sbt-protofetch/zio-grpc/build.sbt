lazy val root = (project in file("."))
  .enablePlugins(ProtofetchPlugin)
  .settings(
    Compile / PB.protoSources += protofetchOutputDirectory.value,
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb",
      scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value / "scalapb"
    ),
    Compile / PB.generate := (Compile / PB.generate)
      .dependsOn(protofetchFetch)
      .value,
    libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  )
