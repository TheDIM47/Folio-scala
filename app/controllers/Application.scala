package controllers

import dto.{NaklSearchData, PartnerSearchData}
import models._
import org.joda.time.DateTime
import play.api.data.Form
import play.api.mvc._
import utils.Page

object Application extends Controller {

  val Home = Redirect(routes.Application.partners)

  def index = Action { Home }

  def partners = partnersPaged(PartnerSearchData(None, None), 1)

  def partnersSearch(page: Int = 1) = Action { implicit request =>
    val form = PartnerSearchData.form.bindFromRequest
    form.fold(
      formWithErrors => {
        BadRequest(views.html.partners(Page(Seq.empty[Partner], 0, 0, 0), formWithErrors, Manager.list, page))
//        BadRequest(views.html.partners( Partner.list(PartnerSearchData(None, models.DEFAULT_PAGE_SIZE), 1), formWithErrors, Manager.list, page))
      },
      filter => {
        val taskList = Partner.list(filter, page)
        Ok(views.html.partners(Partner.list(filter, page), PartnerSearchData.form.fill(filter), Manager.list, page))
      }
    )
  }

  def partnersPaged(filter: PartnerSearchData, page: Int = 0) = Action {
    Ok(views.html.partners(Partner.list(filter, page), PartnerSearchData.form.fill(filter), Manager.list, page))
  }

  def partnersOf(manager: String) = Action {
    val filter = PartnerSearchData(Some(manager), None)
    Ok(views.html.partners(Partner.list(filter, 1), PartnerSearchData.form.fill(filter), Manager.list, 1))
  }

  def managers = Action {
    Ok(views.html.managers(Manager.list))
  }

  def nakls(filter: NaklSearchData) = Action {
    Ok(views.html.nakls(Nakl.list(filter), NaklSearchData.form.fill(filter)))
  }

}
