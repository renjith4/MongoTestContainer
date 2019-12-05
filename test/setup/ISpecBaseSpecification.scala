package setup

import java.time.Duration

import com.dimafeng.testcontainers.FixedHostPortGenericContainer
import com.typesafe.config.{Config, ConfigFactory}
import org.junit.runner.Description
import org.specs2.specification.BeforeAfterAll
import org.testcontainers.containers.wait.strategy.Wait
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import play.api.test.PlaySpecification
import play.api.{Application, Configuration, Logging, Mode}

//adapted from https://github.com/ChristopherDavenport/testcontainers-specs2
trait ISpecBaseSpecification extends PlaySpecification with BeforeAfterAll with Results with Logging{ self =>

  // IntelliJ doesn't honour JavaOptions settings in build.sbt as of now and hence we need this
  protected lazy val config: Config = SingleInitialisationContainer.config

  protected lazy val lazyApp: Application = new GuiceApplicationBuilder()
    .in(Mode.Test)
    .configure(Configuration(config))
    .build()

  implicit val suiteDescription: Description =
    Description.createSuiteDescription(self.getClass)

  override def beforeAll(): Unit = {
    try {
      if (!SingleInitialisationContainer.isContainerRunning)
        logger.info(">>>>>>>>Starting Test Container")
      SingleInitialisationContainer.container.start()
      beforeAllSpecs()
    } catch {
      case e: Exception => logger.error("Error during test setup: beforeAll failed", e)
    }
  }

  def dropDatabase(): Unit = {
  }

  override def afterAll(): Unit = {
    try {
      afterAllSpecs()
      if (SingleInitialisationContainer.isContainerRunning) {
        dropDatabase()
      }
    } catch {
      case e: Exception => logger.error("Error during test teardown: afterAll failed", e)
    }
  }

  def beforeAllSpecs(): Unit = {}
  def afterAllSpecs(): Unit  = {}
}

object SingleInitialisationContainer {

  val config: Config = ConfigFactory.load("test.conf")

  val container = FixedHostPortGenericContainer(
    "mongo:4.0.8-xenial",
    exposedHostPort = 27017,
    exposedContainerPort = 27017,
    env = Map(),
    command = Seq(
      "--dbpath", "data/db",
    ),
    waitStrategy = Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(3))
  )

  def isContainerRunning: Boolean = container.containerId != null

}