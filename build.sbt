version in ThisBuild := "1.0"

scalaVersion in ThisBuild := "2.12.11"

lazy val akkaVersion = "2.6.5"
lazy val akkaGrpcVersion = "0.8.4"

lazy val root = (project in file("."))
  .aggregate(akka)

lazy val akka = (project in file("akka"))
  .enablePlugins(AkkaGrpcPlugin)
  .enablePlugins(JavaAgent)
  .settings(
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.10" % "runtime;test",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
      "ch.qos.logback" % "logback-classic"  % "1.2.3",
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test"
    ),
    dockerBaseImage := "adoptopenjdk/openjdk11:jre",
    dockerExposedPorts += 8080,
    packageName in Docker := "traefik-reproducer"
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)

lazy val gatling = (project in file("gatling"))
  .settings(
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.3.1",
      "io.gatling"            % "gatling-test-framework"    % "3.3.1",
      "com.github.phisgr"     %% "gatling-grpc"             % "0.7.0",
      "com.thesamet.scalapb"  %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb"  %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      ),
    // The code generated by akka-grpc has private modifiers on things we need for gatling.
    // We instead revert to scalapb's generated code which we generate here, and only for the service we use.
    PB.targets in Compile := Seq(
      scalapb.gen(flatPackage = true, grpc = true) -> (sourceManaged in Compile).value
    ),
  )
  .enablePlugins(GatlingPlugin)
  .dependsOn(akka)
