package controllers

import javax.inject.{Inject, Singleton}

import models.{Post, User}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.{PostService, UserService}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@Singleton
class PostController @Inject() (val service: PostService, val userService: UserService, val messagesApi: MessagesApi) extends Controller with I18nSupport  {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  val postMapForm = Form(
    mapping(
      "id" -> ignored[Option[Int]](None),
      "title" -> nonEmptyText,
      "description" -> optional(text(maxLength = 500)),
      "text" -> nonEmptyText,
      "userId" -> tuple(
        "apiKey" -> text,
        "userId" -> optional(number)
      ).transform(
        { case (apiKey, userId) => {
          val result = userService.findByApiKey(apiKey)
          val response = Await.result(result, 2 seconds)
          if (response.isEmpty) {
            Option(0)
          } else {
            response.get.id
          }
        } },
        (userId: Option[Int]) => ("", userId)
      )
    )(Post.apply)(Post.unapply)
  )

  def postList = Action.async {
    service.findAll().map { posts =>
      Ok(views.html.posts(posts))
    }
  }

  def postDetail(id: Int) = Action.async {
    Logger.info(s"${id}")
    service.find(id).map { post =>
      post.fold({ NotFound("404 Not found") })(post => Ok(views.html.post(post)))
    }
  }

  def postForm = Action {
    Ok(views.html.postForm(postMapForm))
  }

  def postPost = Action.async { implicit request =>
    postMapForm.bindFromRequest.fold(
      formWithErrors => Future { BadRequest(views.html.postForm(formWithErrors)) },
      post => {
        service.insert(post).map { post =>
          Redirect(routes.PostController.postList)
        } recover {
          case _ => BadRequest(views.html.postForm(postMapForm
            .withGlobalError(s"Check if your API key is right!")))
        }
      }
    )
  }

  def removePost(id: Int) = Action {
    Ok(views.html.removePost(id, false))
  }

  def removePostConfirm = Action.async { implicit request =>
    request.body.asFormUrlEncoded.fold(
      Future { Redirect(routes.PostController.postList) }
    )(
      values => {
        val apiKey = values.get("apiKey").get.head
        val id = values.get("id").get.head.toInt
        val res = service.remove(id, apiKey)
        val result = Await.result(res, 10 seconds)

        if (result)
          Future { Redirect(routes.PostController.postList) }
        else
          Future { BadRequest(views.html.removePost(id, true)) }
      }
    )
  }
}
