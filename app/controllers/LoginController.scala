package controllers

import javax.inject._
import play.api.mvc._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import collection._
import scala.concurrent.{Await, ExecutionContext, Future}
import models.{Game, Registration, User}
import models.JsonFormats._
import play.api.libs.json._
import reactivemongo.api.Cursor
import scala.concurrent.duration.Duration

class LoginController @Inject()(
                                    components: ControllerComponents,
                                    val mongoService: MongoService
                                  ) extends AbstractController(components) with play.api.i18n.I18nSupport {

  implicit def ec: ExecutionContext = components.executionContext

  val collection: Future[JSONCollection] = mongoService.userCollection

  def showLoginForm(): Action[AnyContent] = Action {implicit request:Request[AnyContent] =>
    Ok
  }
}