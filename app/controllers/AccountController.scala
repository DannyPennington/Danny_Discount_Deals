package controllers

import javax.inject._
import play.api.mvc._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import collection._
import scala.concurrent.{Await, ExecutionContext, Future}
import models.{Game, Login, User, Password}
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
    if (request.flash.get("pchange").isDefined) {
      Ok(views.html.account(user, "Password successfully changed"))
    }
    else {
      Ok(views.html.account(user,""))
    }
  }

  def showPassword: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (request.flash.get("invalid").isDefined) {
      Ok(views.html.changepassword(Password.PasswordForm, "Current password not correct"))
    }
    else {
      Ok(views.html.changepassword(Password.PasswordForm, ""))
    }
  }

  def updatePassword: Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    Password.PasswordForm.bindFromRequest.fold({ formWithErrors =>
      BadRequest(views.html.changepassword(formWithErrors,""))
    }, { password =>
      val user = getAccount(request.session.get("user").getOrElse(""))
      if (password.c_password == user.password) {
        Redirect(routes.AccountController.setPassword(password.n_password))
      }
      else {
        Redirect(routes.AccountController.showPassword()).flashing("invalid" -> "yes")
      }
    })
  }

  def setPassword(n_password:String): Action[AnyContent] = Action.async { implicit request:Request[AnyContent] =>
    val user = getAccount(request.session.get("user").getOrElse(""))
    val newUser = new User(user.forename, user.surname, user.email, n_password, List.empty[Game], List.empty[Game])
    val futureResult = collection.flatMap(_.update.one(user, newUser).map(_.n))
    futureResult.map(_ => Redirect(routes.AccountController.showDetails()).flashing("pchange" -> "yes"))
  }

}
