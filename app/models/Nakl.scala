package models

import java.util.Date

import anorm.SqlParser._
import anorm._
import dto.NaklSearchData
import play.api.Play.current
import play.api.db.DB

case class Nakl(
                 id: Int, // n.unicum_num,
                 org: String, // n.brieforg
                 ppDate: Date, // n.date_p_por,
                 ppNum: String, // n.n_plat_por,
                 vozvrDate: Option[Date], // n.vozvrdate,
                 ctrlDate: Option[Date], // n.contrldate,
                 family: Option[String], // n.family,
                 corr: Option[String], // n.who_corr,
                 osn: Option[String], // n.osnovanie,
                 otgrDate: Option[Date], // n.otgrdate,
                 via: Option[String], // cherz_kogo,
                 sumPpor: Option[Double], // sum(n.sum_por),
                 sumOpl: Option[Double], // sum(m.sum_opl),
                 sumPredm: Option[Double], // sum(m.sum_predm),
                 sumNalog: Option[Double] // sum(m.nalogmoney)
                 )

object Nakl {

  val simple = {
    get[Int]("unicum_num") ~
      get[String]("brieforg") ~
      get[Date]("date_p_por") ~
      get[String]("n_plat_por") ~
      get[Option[Date]]("vozvrdate") ~
      get[Option[Date]]("contrldate") ~
      get[Option[String]]("family") ~
      get[Option[String]]("who_corr") ~
      get[Option[String]]("osnovanie") ~
      get[Option[Date]]("otgrdate") ~
      get[Option[String]]("cherz_kogo") ~
      get[Option[Double]]("sum_por") ~
      get[Option[Double]]("sum_opl") ~
      get[Option[Double]]("sum_predm") ~
      get[Option[Double]]("nalogmoney") map {
      case unum ~ org ~ date_p_por ~ n_plat_por ~ vozvrdate ~ contrldate ~
        family ~ who_corr ~ osnovanie ~ otgrdate ~ cherz_kogo ~
        sum_por ~ sum_opl ~ sum_predm ~ nalogmoney =>
        Nakl(unum, org, date_p_por, n_plat_por, vozvrdate, contrldate,
          family, who_corr, osnovanie, otgrdate, cherz_kogo,
          sum_por, sum_opl, sum_predm, nalogmoney)
    }
  }

  def list(filter: NaklSearchData): List[Nakl] = {
    val prefix = """select n.unicum_num, n.brieforg, n.date_p_por, n.n_plat_por, n.vozvrdate, n.contrldate, n.family,  n.who_corr, n.osnovanie, n.otgrdate, cherz_kogo,
                   | sum(n.sum_por), sum(m.sum_opl), sum(m.sum_predm), sum(m.nalogmoney)
                   | from SCL_NAKL N
                   | inner join SCL_MOVE M on N.UNICUM_NUM=M.UNICUM_NUM""".stripMargin
    val group = """ group by n.unicum_num, n.brieforg, n.date_p_por, n.n_plat_por, n.vozvrdate, n.contrldate, n.family, n.who_corr, n.osnovanie, n.otgrdate, cherz_kogo
                  | having sum(m.sum_opl) <> sum(m.sum_predm)""".stripMargin

    val where = " where " + {
      val exprs = scala.collection.mutable.ArrayBuffer.empty[String]
      exprs += "(n.brieforg={partner})"
      exprs += "(n.oplata_sch=0)"
      exprs += "(n.type_doc='Р')" // russian "�"
      exprs += "(n.vozvrat_pr=0)"
      exprs += "(m.id_sclad in ({ware}))"
      exprs += "(year(n.date_p_por)>={year})"
      if (models.SQLDialect == SqlDialect.SQLServer2000) {
        exprs += "(CAST(FLOOR(CAST(n.date_p_por AS FLOAT)) AS DATETIME) <= CAST(FLOOR(CAST({date} AS FLOAT)) AS DATETIME))"
      } else {
        exprs += "(trunc(n.date_p_por) <= trunc({date}))"
      }
      exprs.mkString(" and ")
    }

    DB.withConnection(CONNECTION_NAME) { implicit c =>
      SQL(prefix + where + group).on('partner -> filter.partner,
        'ware -> filter.ware,
        'year -> filter.year,
        'date -> filter.date
      ).as(simple *)
    }
  }

}
