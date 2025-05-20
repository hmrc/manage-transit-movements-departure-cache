import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "manage-transit-movements-departure-cache"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.5.0"
ThisBuild / scalafmtOnCompile := true

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalaxbPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    PlayKeys.playDefaultPort := 10126,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=src_managed/.*:s"
    ),
    RoutesKeys.routesImport ++= Seq("models._","models.Sort._")
  )
  .settings(
    Compile / scalaxb / scalaxbXsdSource := new File("./conf/xsd"),
    Compile / scalaxb / scalaxbDispatchVersion := "1.1.3",
    Compile / scalaxb / scalaxbPackageName := "generated"
  )
  .settings(CodeCoverageSettings.settings *)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(
    libraryDependencies ++= AppDependencies.test,
    DefaultBuildSettings.itSettings()
  )
