package controllers

import akka.stream.Materializer
import models.User
import play.api.libs.json.Json
import play.api.test.FakeRequest
import repositories.UserRepository
import setup.ISpecBaseSpecification

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UserControllerISpec extends ISpecBaseSpecification {

  private val userController: UserController = lazyApp.injector.instanceOf[UserController]
  private val userRepository: UserRepository = lazyApp.injector.instanceOf[UserRepository]
  private implicit val materialiser: Materializer = lazyApp.injector.instanceOf[Materializer]

  "createUser" should {
    "create a new user" in {
      val fakeRequest = FakeRequest()
        .withMethod(POST)
          .withJsonBody(Json.parse("{\"_id\":null,\"firstName\": \"Harry\",\"lastName\": \"Potter\",\"isAdmin\": true}"))
      val eventualResult = userController.createUser()(fakeRequest)
      status(eventualResult) must beEqualTo(CREATED)

      val resultFromDB = Await.result(userRepository.getUser("Harry", "Potter"), Duration.Inf)

      resultFromDB.get must beMostlyEqualTo(User(null, "Harry", "Potter", true))
    }
  }

  def beMostlyEqualTo = (be_==(_: User)) ^^^ ((_: User).copy(_id=null))

  "createUser" should {
    "return bad request if the json is malformed" in {
      val fakeRequest = FakeRequest()
        .withMethod(POST)
        .withJsonBody(Json.parse("{\"_id\":null,\"first\": \"Harry\",\"last\": \"Potter\",\"isAdmin\": true}"))
      val eventualResult = userController.createUser()(fakeRequest)
      status(eventualResult) must beEqualTo(BAD_REQUEST)
    }
  }


}
