import sbt._

object AppDependencies {

  private val mongoVersion = "0.71.0"
  private val bootstrapVersion = "7.1.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % mongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % mongoVersion,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"
  ).map(_ % "test, it")
}
