name := "disqus-to-wxr"

version := "1.1"

scalaVersion := "2.13.8"

libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.12" % "1.1.1"

ThisBuild / scalafmtOnCompile := true

assembly / mainClass := Some("org.bruchez.olivier.disqustowxr.DisqusToWxr")

assembly / assemblyJarName := "disqus-to-wxr.jar"
