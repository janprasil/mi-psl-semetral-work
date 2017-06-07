package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import services.UserService
import models.User
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import akka.actor.Props

import scala.concurrent.Future
import scala.util.Random

@Singleton
class UserController @Inject() (val service: UserService, val messagesApi: MessagesApi) extends Controller with I18nSupport {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  val randomKey = Random.alphanumeric

  val userForm = Form(
    mapping(
      "id" -> ignored[Option[Int]](None),
      "email" -> email.verifying("Maximum length is 512", _.size <= 512),
      "firstName" -> optional(text(maxLength = 64)),
      "lastName" -> optional(text(maxLength = 64)),
      "apiKey" -> ignored[Option[String]](Option(randomKey.take(128).mkString("")))
    )(User.apply)(User.unapply)
  )
  
  def getUsers = Action.async {
    service.findAll().map { users =>
      Ok(views.html.users(users)(userForm))
    }
  }

  def addUserForm = Action {
    Ok(views.html.addUser(userForm))
  }
  
  def addUser = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => Future { BadRequest(views.html.addUser(formWithErrors)) },
      user => {
        service.insert(user).map { user =>
          Ok(views.html.addUser(userForm, user))
        } recover {
          case _ => BadRequest(views.html.addUser(userForm
            .withGlobalError(s"Duplicate email address: ${user.email}")))
        }
      }
    )
  }
}
