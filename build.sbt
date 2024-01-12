import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

val appName = "manage-transit-movements-departure-cache"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalaxbPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.12",
    PlayKeys.playDefaultPort         := 10126,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    ThisBuild / scalafmtOnCompile := true,
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
  .configs(IntegrationTest extend Test)
  .settings(integrationTestSettings(): _*)
  .settings(inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
