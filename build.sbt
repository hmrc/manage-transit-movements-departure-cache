import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import play.sbt.routes.RoutesKeys

val appName = "manage-transit-movements-departure-cache"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.8",
    PlayKeys.playDefaultPort         := 10126,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    ThisBuild / scalafmtOnCompile := true,
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s"
    ),
      RoutesKeys.routesImport ++= Seq("models._","models.Sort._")
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest extend Test)
  .settings(integrationTestSettings(): _*)
  .settings(inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
