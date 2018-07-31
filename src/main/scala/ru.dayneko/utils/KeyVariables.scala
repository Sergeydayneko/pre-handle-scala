package utils

import java.util.Date
import java.util.concurrent.TimeUnit
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

/**
  * Created by s.dayneko 02.07.2018
  */
object KeyVariables {
  val phcPrefix: String = "phc"
  val webPrefix: String = "web"

  /**
    * @param objectQualifier identifier of object
    * @param prefix          depending on what type ID is
    * @param postfix         is a ws value
    * @return summary string key of parameters
    */
  def key(objectQualifier: String, prefix: String, postfix: String): String = List(prefix, objectQualifier, postfix).mkString("_")

  /**
    * @return current date value
    */
  def curDate: String = new Date().getTime.toString

  /**
    * @param bodyFields - body parameters of request
    * @param key - sought-for key
    * @return Option[String] of corresponding key value otherwise None
    */
  def extractData(bodyFields: Map[String, JsValue], key: String): Option[String] = bodyFields.get(key).map(_.prettyPrint)
}

object KeysRepository {
  val config: Config      = ConfigFactory.load()
  val keys: Config        = config.getConfig("keys")
  val statuses: Config    = config.getConfig("statuses")
  val expirations: Config = config.getConfig("expirations")

  val web_key: String = Some("web_c_id").filter(keys.hasPath).map(keys.getString).getOrElse("web_c_id")
  val phc_key: String = Some("ph_c_id").filter(keys.hasPath).map(keys.getString).getOrElse("ph_c_id")
  val ws_key: String  = Some("ws_c_list").filter(keys.hasPath).map(keys.getString).getOrElse("ws_c_list")

  val errorCode: Int        = if (statuses.hasPath("error_code")) statuses.getInt("error_code") else 47
  val web_conflict: Int     = if (statuses.hasPath("web_conflict")) statuses.getInt("web_conflict") else 37
  val phc_conflict: Int     = if (statuses.hasPath("phc_conflict")) statuses.getInt("phc_conflict") else 32

  val standard_time: Long = Some("expiration_time").filter(expirations.hasPath).map(expirations.getDuration(_, TimeUnit.SECONDS)).getOrElse(86400)
  val exp_time_web: Long  = Some("exp_time_web").filter(expirations.hasPath).map(expirations.getDuration(_, TimeUnit.SECONDS)).getOrElse(standard_time)
  val exp_time_phc: Long  = Some("exp_time_phc").filter(expirations.hasPath).map(expirations.getDuration(_, TimeUnit.SECONDS)).getOrElse(standard_time)
}

