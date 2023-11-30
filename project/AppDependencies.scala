import sbt._

object AppDependencies {

  private val mongoVersion = "1.5.0"
  private val bootstrapVersion = "8.1.0"
  val cryptoJsonVersion = "7.6.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % mongoVersion,
    "org.typelevel"           %% "cats-core"                  % "2.9.0",
    "com.github.dwickern"     %% "scala-nameof"               % "4.0.0" % "provided",
    "uk.gov.hmrc"             %% "crypto-json-play-30"        % cryptoJsonVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % mongoVersion,
    "org.scalatest"           %% "scalatest"                  % "3.2.15",
    "org.mockito"              % "mockito-core"               % "5.2.0",
    "org.scalatestplus"       %% "mockito-4-6"                % "3.2.15.0",
    "org.scalacheck"          %% "scalacheck"                 % "1.17.0",
    "org.scalatestplus"       %% "scalacheck-1-17"            % "3.2.15.0",
    "com.vladsch.flexmark"     % "flexmark-all"               % "0.62.2"
  ).map(_ % "test, it")
}
