package models
import java.time._

case class Game(
               name: String,
               price: Long,
               rating: String,
               genre: List[String],
               description: String,
               release: LocalDate
               )
