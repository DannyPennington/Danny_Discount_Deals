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
import scala.concurrent.duration.Duration

class AccountController @Inject()(
                                   components: ControllerComponents,
                                   val mongoService: MongoService
                                 ) extends AbstractController(components) with play.api.i18n.I18nSupport {

  implicit def ec: ExecutionContext = components.executionContext
  val collection: Future[JSONCollection] = mongoService.userCollection

  def getAccount(email:String): User = {
    Await.result(mongoService.searchHelper(email), Duration.Inf).head
  }

  def showDetails: Action[AnyContent] = Action {implicit request:Request[AnyContent] =>
    val user = getAccount(request.session.get("user").getOrElse(""))
    val details = s"Name: ${user.forename} ${user.surname}" + "\n" + s"Email address: ${user.email}" + "\n" + s"Order history: ${user.orders}"
    Ok(views.html.account(user))
  }

}
