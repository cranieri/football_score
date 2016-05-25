name := "FootballScore"

version := "1.0"

scalaVersion := "2.11.7"

organization := "com.footballscores" //<co id="example-app-info"/>

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/")


libraryDependencies ++= {
  val akkaVersion       = "2.4.6" //<co id="akkaVersion"/>
  val sprayVersion      = "1.3.3"
  Seq(
    "com.typesafe.akka" %% "akka-actor"      % akkaVersion, //<co id="actorDep"/>
    "com.typesafe.akka" %% "akka-http-core"  % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental"  % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"  % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
    "ch.qos.logback"    %  "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-testkit"    % akkaVersion   % "test",
    "com.typesafe.akka" %% "akka-http-testkit"   % akkaVersion   % "test",
    "com.typesafe.akka"         %%  "akka-slf4j"                     % akkaVersion,
    "com.typesafe.akka"         %%  "akka-persistence"  % akkaVersion,
    "com.typesafe.akka"         %%  "akka-cluster"                   % akkaVersion,
    "com.typesafe.akka"         %%  "akka-contrib"                   % akkaVersion,
    "com.typesafe.akka"         %%  "akka-testkit"                   % akkaVersion   % "test",
    "com.typesafe.akka"         %%  "akka-multi-node-testkit"        % akkaVersion   % "test",
    "io.spray"                  %%  "spray-can"                      % sprayVersion,
    "io.spray"                  %%  "spray-client"                   % sprayVersion,
    "io.spray"                  %%  "spray-json"                     % "1.3.2",
    "io.spray"                  %%  "spray-routing"                  % sprayVersion,
    "commons-io"                %   "commons-io"                     % "2.4",
    "org.scalatest"             %%  "scalatest"                      % "2.2.4"       % "test",
    "io.spray"                  %%  "spray-can"                      % sprayVersion,
    "io.spray"                  %%  "spray-routing"                  % sprayVersion,
    "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
    "mysql" % "mysql-connector-java" % "5.1.12",
    "com.github.dnvriend" %% "akka-persistence-jdbc" % "2.2.23"
  )
}

cancelable in Global := true

parallelExecution in Test := false

fork := true
