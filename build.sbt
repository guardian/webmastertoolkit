name := "Webmaster Tools Helper"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
    "com.google.gdata" % "core" % "1.47.1",
    "com.gu" %% "configuration" % "3.9",
    "org.xerial" % "sqlite-jdbc" % "3.7.2",
    "com.github.nscala-time" %% "nscala-time" % "0.6.0"
)