import sbt._

object AppDependencies {

  private val mongoVersion = "1.3.0"
  private val bootstrapVersion = "7.19.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % mongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % mongoVersion,
    "org.scalatest"           %% "scalatest"                  % "3.2.15",
    "org.mockito"              % "mockito-core"               % "5.2.0",
    "org.scalatestplus"       %% "mockito-4-6"                % "3.2.15.0",
    "org.scalacheck"          %% "scalacheck"                 % "1.17.0",
    "org.scalatestplus"       %% "scalacheck-1-17"            % "3.2.15.0",
    "com.vladsch.flexmark"     % "flexmark-all"               % "0.62.2"
  ).map(_ % "test, it")
}
