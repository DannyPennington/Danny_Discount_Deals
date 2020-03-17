package controllers

import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import scala.concurrent.{ExecutionContext, Future}
import collection._
import models.{Game, User}
import models.JsonFormats._
import play.api.libs.json.{JsValue, Json}
import reactivemongo.api.Cursor

import play.modules.reactivemongo.{
  MongoController, ReactiveMongoComponents, ReactiveMongoApi
}

class JsonReadersWriters @Inject()(
                                                    components: ControllerComponents,
                                                    val reactiveMongoApi: ReactiveMongoApi
                                                  ) extends AbstractController(components)
  with MongoController with ReactiveMongoComponents {

  implicit def ec: ExecutionContext = components.executionContext

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("users"))

  def create: Action[AnyContent] = Action.async {
    val user = User("Test","User","test@user.com","password",List.empty[Game],List.empty[Game])
    val futureResult = collection.flatMap(_.insert.one(user))
    futureResult.map(_ => Ok(views.html.index("User added")))
  }

}