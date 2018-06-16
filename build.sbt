name := "disqus-to-wxr"

version := "1.0"

scalaVersion := "2.12.6"

libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.12" % "1.1.0"

scalafmtOnCompile in ThisBuild := true

mainClass in assembly := Some("org.bruchez.olivier.disqustowxr.DisqusToWxr")

assemblyJarName in assembly := "disqus-to-wxr.jar"
