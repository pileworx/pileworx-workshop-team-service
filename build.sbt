lazy val workshopV = "0.0.1-SNAPSHOT"
lazy val akkaHttpV = "10.1.11"
lazy val akkaV = "2.6.1"
lazy val scalatestV = "3.1.0"
lazy val scalamockV = "4.4.0"
lazy val logbackV = "1.2.3"

ThisBuild / organization := "io.pileworx.workshop"
ThisBuild / version      := workshopV
ThisBuild / scalaVersion := "2.13.1"
ThisBuild / scalacOptions := Seq("-feature", "-deprecation", "-encoding", "utf8")

lazy val sharedDependencies = Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "ch.qos.logback" % "logback-classic" % logbackV,

  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaV % Test,
  "org.scalatest" %% "scalatest" % scalatestV % Test,
  "org.scalamock" %% "scalamock" % scalamockV % Test
)

lazy val common = (project in file("common"))
  .settings(
    libraryDependencies ++= sharedDependencies
  )

lazy val domain = (project in file("domain")).dependsOn(common)

lazy val app = (project in file("app")).dependsOn(domain)

lazy val port = (project in file("port")).dependsOn(app)

lazy val root = (project in file(".")).dependsOn(port)
  .enablePlugins(JavaAppPackaging)
  .settings(
    packageName in Docker := "pileworx/workshop",
    version in Docker := workshopV,
    dockerExposedPorts := Seq(8080)
  )
  .settings(
    name := "pileworx-workshop-starter"
  )