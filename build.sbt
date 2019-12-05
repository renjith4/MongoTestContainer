name := "MongoTestContainer"
 
version := "1.0" 
      
lazy val `mongotestcontainer` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

val akkaVersion= "2.6.0-M8"

libraryDependencies ++= Seq(specs2 % Test, guice )

libraryDependencies ++= Seq(
  "org.reactivemongo"   %% "play2-reactivemongo"        % "0.19.2-play27",
  "org.reactivemongo"   %% "reactivemongo-bson-macros"  % "0.19.2",
  "io.swagger"          %% "swagger-play2"              % "1.7.1",
  "org.webjars"         %  "swagger-ui"                 % "3.2.2",
  "com.dimafeng"        %% "testcontainers-scala"       % "0.33.0"    % Test
)

import play.sbt.routes.RoutesKeys

//if this is not present then your path won't correctly bind to bsonId
RoutesKeys.routesImport += "play.modules.reactivemongo.PathBindables._"


scalacOptions in Test ++= Seq("-Yrangepos")

fork in Test := true // allow to apply extra setting to Test

unmanagedResourceDirectories in Test += baseDirectory.value / "test/resources"

//Remember that these JavaOptions settings are not honoured by IntelliJ, but used by sbt
javaOptions in Test += "-Dconfig.resource=$test.conf"

coverageExcludedPackages := "<empty>;Reverse.*;router\\.*;"