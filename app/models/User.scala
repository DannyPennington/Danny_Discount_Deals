package models

import play.api.libs.json.OFormat

case class User(
               forename: String,
               surname: String,
               email: String,
               password: String,
               basket: List[Game],
               orders: List[Game]
               )

object JsonFormats {
  import play.api.libs.json.Json

  implicit val userFormat: OFormat[User] = Json.format[User]
  implicit val gameFormat: OFormat[Game] = Json.format[Game]

}