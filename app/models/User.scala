package models

import play.api.libs.json.{Json, OFormat}
import reactivemongo.bson.BSONObjectID

case class User(_id: Option[BSONObjectID], firstName:String, lastName:String, isAdmin: Boolean)

object User {
  // Don't remove the following line as it is needed for BSon format handling
  import reactivemongo.play.json._
  implicit val userFormat: OFormat[User] = Json.format[User]
}