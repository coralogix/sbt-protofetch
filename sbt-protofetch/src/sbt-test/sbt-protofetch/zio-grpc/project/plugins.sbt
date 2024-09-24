addSbtPlugin(
  "com.coralogix" % "sbt-protofetch" %
    System.getProperty("sbt.protofetch.version")
)

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.6")

libraryDependencies +=
  "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.6.0"
