package models
import java.time._
import play.api.libs.json.OFormat

case class Game(
               name: String,
               price: Long,
               rating: String,
               genre: List[String],
               description: String,
               release: LocalDate
               )

object JsonFormats {
  import play.api.libs.json.Json

  implicit val userFormat: OFormat[Game] = Json.format[Game]
}