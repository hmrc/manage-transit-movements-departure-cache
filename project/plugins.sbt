resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc"       % "sbt-auto-build"     % "3.14.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.4.0")
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.0")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"      % "1.9.3")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"       % "2.4.3")
addSbtPlugin("org.scalaxb"       % "sbt-scalaxb"        % "1.8.0")

libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always )