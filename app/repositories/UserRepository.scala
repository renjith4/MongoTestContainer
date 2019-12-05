package repositories

import javax.inject.{Inject, Singleton}
import models.User
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import models.User._
import play.api.Logging
import play.api.libs.json.Json
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._

@Singleton
class UserRepository @Inject()(reactiveMongoApi: ReactiveMongoApi)(implicit ec:ExecutionContext ) extends Logging{

  def usersCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("users"))

  def getUser(id: BSONObjectID): Future[Option[User]] =
    usersCollection.flatMap(_.find(
      selector = BSONDocument("_id" -> id),
      projection = Option.empty[BSONDocument])
      .one[User])

  def addUser(user: User): Future[WriteResult] =
    usersCollection.flatMap(_.insert.one(user))

  def getUser(firstName:String, lastName:String ): Future[Option[User]] ={
    usersCollection.flatMap(_.find(
      selector = (BSONDocument("firstName" -> firstName, "lastName" -> lastName)),
      projection = Option.empty[BSONDocument])
      .one[User])
  }

  def getUsers(): Future[Seq[User]] = {

    usersCollection.flatMap { c =>
      c.find(Json.obj(), Option.empty[BSONDocument] ).cursor[User](ReadPreference.primary).collect[Seq](25,Cursor.FailOnError[Seq[User]]())
    }
  }

  def updateUser(userId: BSONObjectID, user: User): Future[Boolean] = {
    val selector = BSONDocument("_id" -> userId)
    val updateModifier = BSONDocument(
      f"$$set" -> BSONDocument(
        "firstName" -> user.firstName,
        "lastName" -> user.lastName,
        "isAdmin" -> user.isAdmin
      )
    )

    usersCollection.flatMap(
      _.update.one(q = selector, u = updateModifier,
        upsert = false, multi = false)
    )
  }.map{writeResult =>
    if(writeResult.ok ==true){
      true
    } else {
      logger.error(s"Error updating user $user :  ${writeResult.errmsg}")
      false
    }
  }

}

