package controllers

import akka.actor.ActorSystem
import io.swagger.annotations._
import javax.inject._
import models.User
import play.api.libs.json.Json
import play.api.mvc._
import repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}
import models.User._
import play.api.Logging
import reactivemongo.bson.BSONObjectID


@Singleton
@Api(value = "/user")
class UserController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem, userRepository: UserRepository)(implicit exec: ExecutionContext) extends AbstractController(cc) with Logging {

  @ApiOperation(
    value = "Get user",
    response = classOf[User]
  )
  @ApiResponses(Array(new ApiResponse(code = 404, message = "User not found")))
  def getUser(@ApiParam(value = "Id of User in Bson Format. For example 507f191e810c19729de860ea") userId: BSONObjectID) = Action.async {
    userRepository.getUser(userId).map { maybeUser =>
      maybeUser.map { user: User =>
        Ok(Json.toJson(user))
      }.getOrElse(NotFound)
    }
  }

  @ApiOperation(
    value = "Add a new User",
    response = classOf[Void],
    code = 201
  )
  @ApiResponses(Array(new ApiResponse(code = 400, message = "Invalid User format")))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(value = "The User to add, in Json Format", required = true, dataType = "models.User", paramType = "body")
  ))
  def createUser(): Action[AnyContent] = Action.async { request =>
    val json = request.body.asJson
    json.get.validate[User].map { user: User =>
      userRepository.addUser(user).map { _ =>
        Created
      }
    }.getOrElse(Future.successful(BadRequest("Invalid User format")))
  }


  @ApiOperation(
    value = "Retrieve all Users present",
    response = classOf[User],
    responseContainer = "List"
  )
  def getAllUsers: Action[AnyContent] = Action.async {
    userRepository.getUsers().map { users: Seq[User] =>
      Ok(Json.toJson(users))
    }
  }


  @ApiOperation(
    value = "Update a user",
    response = classOf[Void],
    code = 200
  )
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid User format")
  )
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(value = "Updated User in json format", required = true, dataType = "models.User", paramType = "body")
  )
  )
  def updateUser(@ApiParam(value = "Id of user to be updated")userId: reactivemongo.bson.BSONObjectID) = Action.async{request =>
    val json = request.body.asJson
    json.get.validate[User].map { user =>

      userRepository.updateUser(userId, user).map {
        case true => Ok
        case false => NotFound
      }
    }.getOrElse(Future.successful(BadRequest("Invalid Json")))
  }

}
