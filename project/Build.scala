import sbt._
import Keys._
import sbtassembly.Plugin.AssemblyKeys._
import sbt.NameFilter._

object CommonSettings {
  val buildOrganization = "org.apache"
  val buildScalaVersion = "2.9.1"

  val commonSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    resolvers += "Scala-Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
  )
}

object KafkaBuild extends Build {
  import CommonSettings._

  val nexusSettings = Seq(
    resolvers ++= Seq("ReportGrid repo" at            "http://nexus.reportgrid.com/content/repositories/releases",
                      "ReportGrid snapshot repo" at   "http://nexus.reportgrid.com/content/repositories/snapshots"),
                       credentials += Credentials(Path.userHome / ".ivy2" / ".rgcredentials")
                     )

  override def projectDefinitions(base: File) = {
    val coreSettings = commonSettings ++ sbtassembly.Plugin.assemblySettings ++ Seq(
      version := "0.7.5",
      libraryDependencies ++= Seq(
        "com.github.sgroschupf" % "zkclient"           % "0.1" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),

        "org.easymock"          % "easymock"           % "3.0"     % "test" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),

        "junit"                 % "junit"              % "4.1"     % "test" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),

        "org.scalatest"         % "scalatest"          % "1.2"     % "test" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),

        "net.sf.jopt-simple"    % "jopt-simple"        % "3.2" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),

        "log4j"                 % "log4j"              % "1.2.15" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),
        "org.apache.avro"      % "avro"               % "1.4.0" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),

        "commons-logging"      % "commons-logging"    % "1.0.4" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),

        "org.codehaus.jackson" % "jackson-core-asl"   % "1.5.5" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),

        "org.codehaus.jackson" % "jackson-mapper-asl" % "1.5.5" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),

        "org.apache.hadoop"    % "hadoop-core"        % "0.20.2" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")),

        "org.xerial.snappy"    % "snappy-java"        % "1.0.4.1" excludeAll(
                                                                    ExclusionRule(organization = "com.sun.jdmk"),
                                                                    ExclusionRule(organization = "com.sun.jmx"),
                                                                    ExclusionRule(organization = "javax.jms")) 
      ),
      mainClass := Some("kafka.Kafka"),
      test in assembly := {},
      publishTo <<= (version) { version: String =>
        val nexus = "http://nexus.reportgrid.com/content/repositories/"
        if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "snapshots/") 
        else                                   Some("releases"  at nexus + "releases/")
      },
      credentials += Credentials(Path.userHome / ".ivy2" / ".rgcredentials")
    )
  
    val distribution = TaskKey[File]("distribution")

    val distributionTask = distribution <<= (assembly,streams) map { (assemblyFile : File,s) =>
      val zipFiles = List((assemblyFile, ("kafka/core/target/" + assemblyFile.getName)), 
			  (new File("bin/new-kafka-server-start.sh"), "kafka/bin/kafka-server-start.sh")) ++
		     List("config/server.properties", "config/log4j.properties").map(fn => (new File(fn), "kafka/" + fn))

      IO.createDirectory(new File("dist"))
      val zipOutput = new File("dist/kafka-0.7.5.zip")
      s.log.info("Creating distribution zipfile: " + zipOutput)
      IO.zip(zipFiles, zipOutput)
      s.log.info("Zipfile complete")
      zipOutput
    }


    val core = Project("kafka-core", file("core"), settings = coreSettings).settings(nexusSettings : _*).settings(distributionTask)

    val examplesSettings = commonSettings ++ Seq(
      version := "0.7.5",
      libraryDependencies ++= Seq(
      )
    )
 
    val examples = Project("java-examples", file("examples"), settings = examplesSettings)
    
    val contribSettings = commonSettings ++ Seq(
      version := "0.7.5",
      libraryDependencies ++= Seq(
      )
    )
  
    val contrib = Project("contrib", file("contrib"), settings = contribSettings)
    
    val perfSettings = commonSettings ++ Seq(
      version := "0.7.5",
      libraryDependencies ++= Seq(
      )
    )
  
    val perf = Project("perf", file("perf"), settings = perfSettings)

    core :: examples :: contrib :: perf :: Nil
  }

}
