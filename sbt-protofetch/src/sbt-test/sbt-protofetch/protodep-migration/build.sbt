lazy val grpcDeps = (project in file("grpc-deps"))
  .enablePlugins(ProtofetchPlugin)
  .settings(
    protofetchModuleFile := baseDirectory.value / ".." / "protofetch.toml",
    Compile / PB.protoSources += protofetchOutputDirectory.value,
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb"
    ),
    Compile / PB.generate := (Compile / PB.generate)
      .dependsOn(protofetchFetch)
      .value,
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    )
  )

lazy val root = (project in file("."))
  .aggregate(grpcDeps)
  .dependsOn(grpcDeps)
