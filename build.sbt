lazy val workshopV = "0.0.1-SNAPSHOT"
lazy val akkaHttpV = "10.1.11"
lazy val akkaV = "2.6.1"
lazy val scalatestV = "3.0.8"
lazy val scalamockV = "4.4.0"
lazy val logbackV = "1.2.3"

val commonSetting = Seq(
  version := workshopV,
  organization := "io.pileworx.workshop",
  scalaVersion := "2.13.1",
  scalacOptions := Seq("-feature", "-deprecation", "-encoding", "utf8"),
  assemblyMergeStrategy in assembly := {
    case "module-info.class" => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  test in assembly := {}
)

lazy val sharedDependencies = Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaV,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaV,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "ch.qos.logback" % "logback-classic" % logbackV,
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
)

lazy val testDependencies = Seq(
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaV % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaV % Test,
  "org.scalatest" %% "scalatest" % scalatestV % Test,
  "org.scalamock" %% "scalamock" % scalamockV % Test
)

lazy val common = (project in file("common"))
  .settings(commonSetting: _*)
  .settings(
    libraryDependencies ++= (sharedDependencies ++ testDependencies),
    assemblyJarName in assembly := "team-common.jar"
  )

lazy val domain = (project in file("domain")).dependsOn(common)
  .settings(commonSetting: _*)
  .settings(
    libraryDependencies ++= testDependencies,
    assemblyJarName in assembly := "team-domain.jar"
  )

lazy val app = (project in file("app")).dependsOn(domain)
  .settings(commonSetting: _*)
  .settings(
    assemblyJarName in assembly := "team-app.jar"
  )

lazy val port = (project in file("port")).dependsOn(app)
  .settings(commonSetting: _*)
  .settings(
    assemblyJarName in assembly := "team-port.jar"
  )

lazy val root = (project in file(".")).dependsOn(port)
  .settings(commonSetting: _*)
  .settings(
    name := "pileworx-workshop-team-service",
    mainClass in assembly := Some("io.pileworx.workshop.team.Application")
  )
  .aggregate(
    common, domain, app, port
  )