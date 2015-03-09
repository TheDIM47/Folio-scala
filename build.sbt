import play.PlayImport.PlayKeys._

name := "Folio-scala"

version := "1.0-SNAPSHOT"

lazy val `folioScala` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

resolvers ++= Seq(Resolver.mavenLocal
  , "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
  , "Typesafe Snapshots" at "https://repo.typesafe.com/typesafe/snapshots/"
)

libraryDependencies ++= Seq("net.sourceforge.jtds" % "jtds" % "1.3.1"
  , "com.h2database" % "h2" % "1.4.185"
  , "org.webjars" %% "webjars-play" % "2.3.0-2"
  , "org.webjars" % "modernizr" % "2.8.3"
  , "org.webjars" % "html5shiv" % "3.7.2"
  , "org.webjars" % "jquery" % "1.11.2"
  , "org.webjars" % "bootstrap" % "3.3.1"
  , "org.webjars" % "font-awesome" % "4.3.0-1"
  , "org.webjars" % "respond" % "1.4.2"
  , "org.webjars" % "requirejs" % "2.1.15"
  , "org.webjars" % "bootstrap-datepicker" % "1.3.1"
  , "org.webjars" % "bootstrap-datetimepicker" % "6aa746736d"
//  , "com.github.nscala-time" %% "nscala-time" % "1.8.0"
)


libraryDependencies ++= Seq(jdbc, anorm, cache, ws)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

routesImport ++= Seq("binders.CustomBinders._", "utils.Page", "dto._")
