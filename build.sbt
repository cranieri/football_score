name := "FootballScore"
version := "1.0"
scalaVersion := "2.11.4"

organization := "com.footballscores" //<co id="example-app-info"/>

libraryDependencies ++= {
  val akkaVersion       = "2.4.4" //<co id="akkaVersion"/>
  Seq(
    "com.typesafe.akka" %% "akka-actor"      % akkaVersion, //<co id="actorDep"/>
    "com.typesafe.akka" %% "akka-http-core"  % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental"  % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"  % akkaVersion,
    "io.spray"          %% "spray-json"      % "1.3.1",
    "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
    "ch.qos.logback"    %  "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-testkit"    % akkaVersion   % "test",
    "com.typesafe.akka" %% "akka-http-testkit"   % akkaVersion   % "test",
    "org.scalatest"     %% "scalatest"       % "2.2.0"       % "test"
  )
}

cancelable in Global := true
