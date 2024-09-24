addSbtPlugin(
  "com.coralogix" % "sbt-protofetch" %
    System.getProperty("sbt.protofetch.version")
)

addSbtPlugin("com.eed3si9n"                    % "sbt-projectmatrix" % "0.9.0")
addSbtPlugin("com.thesamet"                    % "sbt-protoc"        % "1.0.6")
libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin"    % "0.11.14"
