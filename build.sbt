import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "manage-transit-movements-departure-cache"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.8",
    PlayKeys.playDefaultPort         := 10126,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    ThisBuild / scalafmtOnCompile := true,
    RoutesKeys.routesImport ++= Seq("models.Frontend"),
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s"
    )
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest extend Test)
  .settings(integrationTestSettings(): _*)
  .settings(inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
