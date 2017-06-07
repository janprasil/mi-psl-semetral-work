package controllers

import javax.inject.Singleton
import play.api.mvc.Action
import play.api.mvc.Controller

@Singleton
class HealthController extends Controller {
  def check() = Action {
    Ok("OK")
  }
}