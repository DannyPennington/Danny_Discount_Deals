package controllers
import javax.inject._
import play.api.mvc._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import collection._
import scala.concurrent.{Await, ExecutionContext, Future}
import models.{Game, Login, Registration, User}
import models.JsonFormats._
import play.api.libs.json._
import reactivemongo.api.Cursor
import java.time.LocalDate
import scala.concurrent.duration.Duration


class SearchController @Inject()(
                                 components: ControllerComponents,
                                 val mongoService: MongoService
                               ) extends AbstractController(components) with play.api.i18n.I18nSupport {

  implicit def ec: ExecutionContext = components.executionContext

  val collection: Future[JSONCollection] = mongoService.gameCollection

  def searchByName(name: String): Future[List[Game]] = {
    val cursor: Future[Cursor[Game]] = collection.map {
      _.find(Json.obj("name" -> name)).
        sort(Json.obj("name" -> -1)).
        cursor[Game]()
    }
    val futureGameList: Future[List[Game]] =
      cursor.flatMap(
        _.collect[List](
          -1,
          Cursor.FailOnError[List[Game]]()
        )
      )
    futureGameList
  }

  def create: Action[AnyContent] = Action.async {
    val game = Game("Halo",20,"15",List("Action","FPS"),"A shooty game",LocalDate.of(2004,5,5))
    val futureResult = collection.flatMap(_.insert.one(game))
    futureResult.map(_ => Ok(views.html.index("Game added")))
  }


}