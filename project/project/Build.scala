import sbt._
object PluginDef extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn (assembly, altDep, depGraph)
  lazy val assembly = RootProject(uri("git://github.com/eed3si9n/sbt-assembly.git#070"))
  lazy val altDep = RootProject(uri("git://github.com/reportgrid/xsbt-alt-deps")) 
  lazy val depGraph = RootProject(uri("git://github.com/jrudolph/sbt-dependency-graph.git#v0.5.1"))
}

// vim: set ts=4 sw=4 et:
