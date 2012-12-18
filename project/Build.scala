import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "danuze"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        "play-aws" % "play-aws_2.9.1" % "0.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
        resolvers += "Local Play Repository" at "/Users/jefw/Java/play-2.0.4/repository/local"
    )

}
