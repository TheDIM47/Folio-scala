package models

import anorm.SqlParser._
import anorm._
import dto.PartnerSearchData
import play.api.Play.current
import play.api.cache.Cache
import play.api.db.DB
import utils.Page

case class Partner(
                    id: String, //  N_USER "Имя орг"
                    name: Option[String], //  NAME_USER "Организация"
                    cp1: Option[String], //  CP_1 "Вид деятельности"
                    cp2: Option[String], //  CP_2 "Тип контактов"
                    skidka: Option[Double], //  SKIDKAPRCNT "Скидка" 
                    delay: Option[Double], //  REGIS_USER "Отсрочка"
                    kredit: Option[Double], //  KREDIT "Кредит" (Лимит)
                    prim: Option[String], // PRIMECH
                    holding: Option[Int], // holding: OrgPropValues (PropCode=3, Prop_Org=client_id, -> VAL_INT)
                    dolg: Option[Double], // DOLG
                    tnad: Option[Double], // TNAD
                    usumm: Option[Double] // USUMM
                    )

object Partner {
  val EXPIRATION_SEC = 600

  val simple = {
    get[String]("n_user") ~
      get[Option[String]]("name_user") ~
      get[Option[String]]("cp_1") ~
      get[Option[String]]("cp_2") ~
      get[Option[Double]]("skidkaprcnt") ~
      get[Option[Double]]("regis_user") ~
      get[Option[Double]]("kredit") ~
      get[Option[String]]("primech") ~
      get[Option[Int]]("val_int") ~
      get[Option[Double]]("dolg") ~
      get[Option[Double]]("tnad") ~
      get[Option[Double]]("usumm") map {
      case id ~ name ~ cp1 ~ cp2 ~ skidka ~ delay ~ kredit ~ prim ~ holding ~ dolg ~ tnad ~ usumm =>
        Partner(id, name, cp1, cp2, skidka, delay, kredit, prim, holding, dolg, tnad, usumm)
    }
  }

  /*
  SELECT TOP ((PageNumber - 1) * items-per-page) * FROM Table
  WHERE ID NOT IN (SELECT TOP (PageNumber * items-per-page) ID FROM Table ORDER BY OrderingColumn)
  ORDER BY OrderingColumn
  ---
  SELECT TOP 100 * FROM MyTable
  WHERE id NOT IN (SELECT TOP 100 id FROM MyTable ORDER BY id) ORDER BY id
  ---
  SELECT TOP (@batch_size) *
  FROM
    (SELECT TOP (@offset + @batch_size) O.*
       FROM orders O INNER JOIN customers C ON O.customer_code = C.customer_code
       WHERE C.country_code = 'IT'
       ORDER BY O.product_code DESC, O.customer_code DESC, O.order_type DESC, O.qty_date DESC
    ) AS T1
  ORDER BY product_code ASC, customer_code ASC, order_type ASC, qty_date ASC
  */
//  val rowNumExpr = if (models.SQLDialect == SqlDialect.SQLServer2000) ",row_number() over (order by p.name_user) as rowNum" else ""
  val baseSql =
    """ p.n_user, p.name_user, p.cp_1, p.cp_2, p.skidkaprcnt, p.regis_user, p.kredit, p.primech, v.val_int
      | ,sum(m.sum_predm) - sum(m.sum_opl) as dolg, sum(sum_predm)-sum(sum_uchet) as tnad, sum(sum_uchet) as usumm
      | from _partner p
      | inner join scl_nakl n on p.n_user=n.brieforg
      | inner join scl_move m on m.unicum_num=n.unicum_num
      | left join orgpropvalues v on v.prop_org=p.n_user and v.propcode=3 and v.val_int is not null
    """.stripMargin

  val groupSql = "group by p.n_user, p.name_user, p.cp_1, p.cp_2, p.skidkaprcnt, p.regis_user, p.kredit, p.primech, v.val_int"

  def count(base: String, filter: PartnerSearchData): Int = {
    DB.withConnection(CONNECTION_NAME) { implicit c =>
      SQL(s"select count(*) from (select ${base}) x").on('manager -> {
        if (filter.manager.isDefined) Some(filter.manager.get) else filter.manager
      },
        'ware -> filter.ware,
        'year -> filter.year,
        'date -> { if (filter.date.isDefined) filter.date.get else models.NOW }
      ).as(scalar[Int].single)
    }
  }

  def list(filter: PartnerSearchData, page: Int = 1): Page[Partner] = {
    val offset = filter.psize * (page - 1)
    val rowsToFetch = filter.psize * page
    val exprs = scala.collection.mutable.ArrayBuffer.empty[String]
    if (filter.manager.isDefined) {
      exprs += "(cp_2={manager})"
    }
    exprs += "(n.oplata_sch=0)"
    exprs += "(n.type_doc='Р')" // russian "Р"
    exprs += "(n.vozvrat_pr=0)"
    exprs += "(m.id_sclad in ({ware}))"
    exprs += "(year(n.date_p_por)>={year})"
    if (filter.date.isDefined) {
      if (models.SQLDialect == SqlDialect.SQLServer2000) {
        exprs += "(CAST(FLOOR(CAST(n.date_p_por AS FLOAT)) AS DATETIME) <= CAST(FLOOR(CAST({date} AS FLOAT)) AS DATETIME))"
      } else {
        exprs += "(trunc(n.date_p_por) <= trunc({date}))"
      }
    }
    val where = if (exprs.isEmpty) ""
    else {
      "\nwhere " + exprs.mkString(" and ")
    }
    val recordCount = count(s"${baseSql} ${where} ${groupSql}", filter)
    if (recordCount == 0) {
      Page(Seq.empty, page, offset, recordCount)
    } else {
      val pageSql = models.SQLDialect match {
        case SqlDialect.H2 => s"${baseSql} ${where} ${groupSql} order by p.name_user limit ${filter.psize} offset ${offset}"
        case SqlDialect.SQLServer2000 => s"select top ${filter.psize} * from (select top ${filter.psize} * from (" +
          s"select top ${filter.psize * page} ${baseSql} ${where} ${groupSql} order by name_user" +
          s") x order by name_user desc) y order by name_user"
        case _ => ""
      }
      DB.withConnection(CONNECTION_NAME) { implicit c =>
        //        val sql = s"${baseSql} ${where} ${groupSql} order by p.name_user ${postfix}"
        val result = SQL(pageSql).on('manager -> {
          if (filter.manager.isDefined) Some(filter.manager.get) else filter.manager
        },
          'ware -> filter.ware,
          'year -> filter.year,
          'date -> { if (filter.date.isDefined) filter.date.get else models.NOW }
        ).as(simple *)
        Page(result, page, offset, recordCount)
      }
    }
  }

  def find(nuser: String): Option[Partner] = {
    Cache.getOrElse(nuser) {
      val result = DB.withConnection(CONNECTION_NAME) { implicit c =>
        val sql = baseSql + "\nwhere upper(p.n_user)=upper({nuser})"
        SQL(sql).on('nuser -> nuser).as(simple.singleOpt)
      }
      Cache.set(nuser, result, EXPIRATION_SEC)
      result
    }
  }

}
