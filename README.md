# sbt-protofetch

This plugin uses [protofetch](https://github.com/coralogix/protofetch) to download Protobuf definition files.

## Setup

In `project/plugins.sbt`:
```scala
addSbtPlugin("com.coralogix" % "sbt-protofetch" % "0.1.0")
```

In `build.sbt`:
```scala
lazy val root = (project in file("."))
  .enablePlugins(ProtofetchPlugin)
```

## Usage

With the plugin enabled, you can use the `protofetchFetch` task
to fetch protobuf definitions defined in the `protofetch.toml` descriptor,
and the `protofetchOutputDirectory` to find out the location of the downloaded `.proto` files. 

In practice, you most likely want these protobuf definitions to be used
by the rest of the build process. For `sbt-protoc` the configuration snippet
will look like this:
```scala
lazy val root = (project in file("."))
  .enablePlugins(ProtofetchPlugin)
  .settings(
    // Use protofetch output as a sbt-protoc source
    Compile / PB.protoSources += protofetchOutputDirectory.value,
    Compile / PB.generate := (Compile / PB.generate).dependsOn(protofetchFetch).value,

    // The rest of the sbt-protoc configuration
    Compile / PB.targets := Seq(scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb"),
    libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  )
```

## Protofetch descriptor

It is recommended to put downloaded .proto files inside the `target` directory, for example
```toml
proto_out_dir = './target/proto'
# ...
```

## Protofetch lock files

The `protofetchFetch` task runs `protofetch fetch` or `protofetch fetch --locked`
depending on the value of the `insideCI` settings.
This means that the `protofetch.lock` file has to be committed to the repository.

## Usage with sbt-projectmatrix

To avoid running protofetch for every axis of you project matrix, it's recommended to create a separate
project that will fetch the .proto files:

```scala
lazy val protos = (project in file("protos"))
  .enablePlugins(ProtofetchPlugin)

lazy val app = (projectMatrix in file("app"))
  .jvmPlatform(scalaVersions = Seq("2.13.12", "3.3.1"))
  .settings(
    Compile / PB.protoSources += (protos / protofetchOutputDirectory).value,
    Compile / PB.generate := (Compile / PB.generate).dependsOn(protos / protofetchFetch).value,
    // ...
  )

```

## Migrating from sbt-protodep

If you previously used `Protodep.generateProject` from [sbt-protodep](https://github.com/coralogix/sbt-protodep/tree/master),
you can migrate to `sbt-protofetch` with the following snippet:

```scala
lazy val grpcDeps = (project in file("grpc-deps"))
  .enablePlugins(ProtofetchPlugin)
  .settings(
    protofetchModuleFile := baseDirectory.value / ".." / "protofetch.toml",
    Compile / PB.protoSources += protofetchOutputDirectory.value,
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb",
      scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value / "scalapb"
    ),
    Compile / PB.generate := (Compile / PB.generate).dependsOn(protofetchFetch).value,
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb"               %% "scalapb-runtime-grpc"                    % scalapb.compiler.Version.scalapbVersion,
      "io.grpc"                             % "grpc-netty"                              % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.5.0-2" % "protobuf",
      "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.5.0-2",
      "io.github.scalapb-json"             %% "scalapb-circe"                           % "0.11.2"
    )
  )
```

This gives you full control over dependencies and other aspects of code generation.

Note that this snippet is a drop-in replacement of what `sbt-protodep` does.
You don't necessarily need to have a dedicated `grpcDeps` sbt project,
these settings can as well be applied to some already existing project.
Also, you may not need all `libraryDependencies` that `sbt-protodep` used to add. 
