import sbt._

private object AppDependencies {

  import play.core.PlayVersion

  private val play26Bootstrap     = "1.5.0"
  private val playHmrcApiVersion  = "4.1.0-play-26"
  private val domainVersion       = "5.6.0-play-26"
  private val emailAddressVersion = "3.4.0"

  private val scalaMockVersion     = "4.4.0"
  private val scalaTestVersion     = "3.0.8"
  private val wireMockVersion      = "2.21.0"
  private val pegdownVersion       = "1.6.0"
  private val scalaTestPlusVersion = "3.1.2"
  private val refinedVersion       = "0.9.4"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-play-26" % play26Bootstrap,
    "uk.gov.hmrc" %% "play-hmrc-api"     % playHmrcApiVersion,
    "uk.gov.hmrc" %% "domain"            % domainVersion,
    "uk.gov.hmrc" %% "emailaddress"      % emailAddressVersion,
    "eu.timepit"  %% "refined"           % refinedVersion
  )

  trait TestDependencies {
    lazy val scope: String        = "test"
    lazy val test:  Seq[ModuleID] = ???
  }

  object Test {

    def apply(): Seq[ModuleID] =
      new TestDependencies {

        override lazy val test: Seq[ModuleID] = testCommon(scope) ++ Seq(
            "org.scalamock" %% "scalamock" % scalaMockVersion % scope,
            "org.scalatest" %% "scalatest" % scalaTestVersion % scope
          )
      }.test
  }

  object IntegrationTest {

    def apply(): Seq[ModuleID] =
      new TestDependencies {
        override lazy val scope: String = "it"

        override lazy val test: Seq[ModuleID] = testCommon(scope) ++ Seq(
            "com.github.tomakehurst" % "wiremock" % wireMockVersion % scope
          )
      }.test
  }

  private def testCommon(scope: String) = Seq(
    "org.pegdown"            % "pegdown"             % pegdownVersion       % scope,
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current  % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope
  )

  // Transitive dependencies in scalatest/scalatestplusplay drag in a newer version of jetty that is not
  // compatible with wiremock, so we need to pin the jetty stuff to the older version.
  // see https://groups.google.com/forum/#!topic/play-framework/HAIM1ukUCnI
  val jettyVersion = "9.2.13.v20150730"

  val overrides: Seq[ModuleID] = Seq(
    "org.eclipse.jetty"           % "jetty-server"       % jettyVersion,
    "org.eclipse.jetty"           % "jetty-servlet"      % jettyVersion,
    "org.eclipse.jetty"           % "jetty-security"     % jettyVersion,
    "org.eclipse.jetty"           % "jetty-servlets"     % jettyVersion,
    "org.eclipse.jetty"           % "jetty-continuation" % jettyVersion,
    "org.eclipse.jetty"           % "jetty-webapp"       % jettyVersion,
    "org.eclipse.jetty"           % "jetty-xml"          % jettyVersion,
    "org.eclipse.jetty"           % "jetty-client"       % jettyVersion,
    "org.eclipse.jetty"           % "jetty-http"         % jettyVersion,
    "org.eclipse.jetty"           % "jetty-io"           % jettyVersion,
    "org.eclipse.jetty"           % "jetty-util"         % jettyVersion,
    "org.eclipse.jetty.websocket" % "websocket-api"      % jettyVersion,
    "org.eclipse.jetty.websocket" % "websocket-common"   % jettyVersion,
    "org.eclipse.jetty.websocket" % "websocket-client"   % jettyVersion
  )

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()

}
