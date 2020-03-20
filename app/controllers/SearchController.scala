package controllers

import javax.inject._
import play.api.mvc._
import reactivemongo.play.json.collection.JSONCollection
import collection._
import scala.concurrent.{Await, ExecutionContext, Future}
import models.{Game, Login, Registration, Search, User}
import models.JsonFormats._
import play.api.libs.json._
import java.time.LocalDate
import reactivemongo.api.Cursor
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration


class SearchController @Inject()(
                                 components: ControllerComponents,
                                 val mongoService: MongoService
                               ) extends AbstractController(components) with play.api.i18n.I18nSupport {

  implicit def ec: ExecutionContext = components.executionContext

  val collection: Future[JSONCollection] = mongoService.gameCollection


  def searchByName(search: String): List[Game] = {
    val finalGames = ArrayBuffer.empty[Game]
    val games = Await.result(mongoService.findAllGames(),Duration.Inf)
    for (game <- games) {
      if (game.name.contains(search)) {
        finalGames += game
      }
    }
    finalGames.toList
  }
  def search(name:String): Action[AnyContent] = Action {implicit request:Request[AnyContent] =>
    Ok(views.html.searchresults(searchByName(name)))
  }

  def create: Action[AnyContent] = Action.async {
    val game = Game("Halo 2",20,"15",List("Action","FPS"),"Another shooty game",LocalDate.of(2004,5,5))
    val futureResult = collection.flatMap(_.insert.one(game))
    futureResult.map(_ => Ok(views.html.index("Game added")))
  }

  def searchHandler = Action(parse.form(Search.SearchForm)) { implicit request =>
    val search = request.body.name
    Redirect(routes.SearchController.search(search))
  }

  def searchPage: Action[AnyContent] = Action {implicit request:Request[AnyContent] =>
    Ok(views.html.search(Search.SearchForm,""))
  }

}