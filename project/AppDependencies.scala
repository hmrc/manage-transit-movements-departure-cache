import sbt._

object AppDependencies {

  private val mongoVersion = "0.72.0"
  private val bootstrapVersion = "7.3.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % mongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % mongoVersion,
    "org.scalatest"           %% "scalatest"                  % "3.2.12",
    "org.scalatestplus"       %% "scalacheck-1-16"            % "3.2.13.0",
    "org.scalacheck"          %% "scalacheck"                 % "1.16.0",
    "org.mockito"              % "mockito-core"               % "4.8.0",
    "org.scalatestplus"       %% "mockito-4-5"                % "3.2.12.0",
    "com.vladsch.flexmark"     % "flexmark-all"               % "0.62.2"
  ).map(_ % "test, it")
}
