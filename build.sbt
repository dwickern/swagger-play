ThisBuild / organization := "com.github.dwickern"

lazy val play27 = ConfigAxis("play27", "play2.7")
lazy val play28 = ConfigAxis("play28", "play2.8")

lazy val scala212 = "2.12.13"
lazy val scala213 = "2.13.4"

lazy val root = (project in file("."))
  .aggregate(swagger.projectRefs: _*)
  .settings(
    // for IntelliJ import: pick one project from the matrix to use
    swagger.finder(play28, VirtualAxis.jvm)(scala213).settings,
    target := baseDirectory.value / "target",
    ideSkipProject := false,
    compile / skip := true,
    publish / skip := true,
  )

lazy val swagger = (projectMatrix in file("."))
  .settings(
    name := "swagger-play",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.3",
      "org.slf4j" % "slf4j-api" % "1.7.30",
      "org.specs2" %% "specs2-core" % "4.6.0" % Test,
      "org.specs2" %% "specs2-mock" % "4.6.0" % Test,
      "org.specs2" %% "specs2-junit" % "4.6.0" % Test,
      "org.mockito" % "mockito-core" % "3.2.0" % Test,
    ),
    scalacOptions -= "-Xfatal-warnings",
    Test / scalacOptions ~= filterConsoleScalacOptions,
    Test / parallelExecution := false, // Swagger uses global state which breaks parallel tests
    Test / publishArtifact := false,
    publishTo := sonatypePublishToBundle.value,
    pomIncludeRepository := { _ => false },
    publishMavenStyle := true,
    releaseCrossBuild := true,
    ideSkipProject := true,
  )
  .customRow(
    scalaVersions = Seq(scala213, scala212),
    axisValues = Seq(play27, VirtualAxis.jvm),
    _.settings(
      moduleName := name.value + "2.7",
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play" % "2.7.9",
        "com.typesafe.play" %% "routes-compiler" % "2.7.9",
        "io.swagger" % "swagger-core" % "1.5.24",
        "io.swagger" %% "swagger-scala-module" % "1.0.6",
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.10",
        "com.typesafe.play" %% "play-ebean" % "5.0.2" % Test,
      )
    )
  )
  .customRow(
    scalaVersions = Seq(scala213, scala212),
    axisValues = Seq(play28, VirtualAxis.jvm),
    _.settings(
      moduleName := name.value + "2.8",
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play" % "2.8.7",
        "com.typesafe.play" %% "routes-compiler" % "2.8.7",
        "io.swagger" % "swagger-core" % "1.6.1",
        "io.swagger" %% "swagger-scala-module" % "1.0.6", // FIXME: no version supports jackson 2.10
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.5",
        "com.typesafe.play" %% "play-ebean" % "6.0.0" % Test,
      )
    )
  )

Global / excludeLintKeys += ideSkipProject

ThisBuild / pomExtra := {
  <url>http://swagger.io</url>
  <licenses>
    <license>
      <name>Apache License 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:swagger-api/swagger-play.git</url>
    <connection>scm:git:git@github.com:swagger-api/swagger-play.git</connection>
  </scm>
  <developers>
    <developer>
      <id>fehguy</id>
      <name>Tony Tam</name>
      <email>fehguy@gmail.com</email>
    </developer>
    <developer>
      <id>ayush</id>
      <name>Ayush Gupta</name>
      <email>ayush@glugbot.com</email>
    </developer>
    <developer>
      <id>rayyildiz</id>
      <name>Ramazan AYYILDIZ</name>
      <email>rayyildiz@gmail.com</email>
    </developer>
    <developer>
      <id>benmccann</id>
      <name>Ben McCann</name>
      <url>http://www.benmccann.com/</url>
    </developer>
    <developer>
      <id>frantuma</id>
      <name>Francesco Tumanischvili</name>
      <url>http://www.ft-software.net/</url>
    </developer>
    <developer>
      <id>gmethvin</id>
      <name>Greg Methvin</name>
      <url>https://methvin.net/</url>
    </developer>
    <developer>
      <id>dwickern</id>
      <name>Derek Wickern</name>
      <url>dwickern@gmail.com</url>
    </developer>
  </developers>
}

import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
)
