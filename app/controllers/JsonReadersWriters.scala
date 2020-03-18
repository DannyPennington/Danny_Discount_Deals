package controllers

import javax.inject._
import play.api.mvc._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}
import collection._
import models.{Game, Registration, User}
import models.JsonFormats._
import play.api.libs.json.{JsValue, Json}
import reactivemongo.api.Cursor
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

class JsonReadersWriters @Inject()(
                                                    components: ControllerComponents,
                                                    val reactiveMongoApi: ReactiveMongoApi
                                                  ) extends AbstractController(components)
  with MongoController with ReactiveMongoComponents with play.api.i18n.I18nSupport {

  implicit def ec: ExecutionContext = components.executionContext

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("users"))

  def create: Action[AnyContent] = Action.async {
    val user = User("Test","User","test@user.com","password",List.empty[Game], List.empty[Game])
    val futureResult = collection.flatMap(_.insert.one(user))
    futureResult.map(_ => Ok(views.html.index("User added")))
  }

  def addUser(forename :String, surname :String, email :String, password :String): Action[AnyContent] = Action.async { implicit request:Request[AnyContent] =>
    val user = User(forename, surname, email, password, List.empty[Game], List.empty[Game])
    val futureResult = collection.flatMap(_.insert.one(user))
    futureResult.map(_ => Ok(views.html.index("User added")).withSession(request.session + ("user" -> user.email)))
  }

  def showRegistration: Action[AnyContent] = Action {implicit request:Request[AnyContent] =>
    Ok(views.html.registration(Registration.RegistrationForm))
  }

  def registerUser: Action[AnyContent] = Action { implicit request:Request[AnyContent] =>
    Registration.RegistrationForm.bindFromRequest.fold({ formWithErrors =>
      BadRequest(views.html.registration(formWithErrors))
    }, { register =>
      Redirect(routes.JsonReadersWriters.addUser(register.forename, register.surname, register.email, register.password)).flashing("new" -> "yes")
      //Redirect(routes.HomeController.index()).withSession(request.session + ("user" -> user.email))
    })
  }

}