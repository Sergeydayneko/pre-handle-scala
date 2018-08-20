package ru.dayneko.utils

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import ru.dayneko.model.Rule
import spray.json._

import scala.collection.JavaConversions._

object KeyVariables {
  /**
    * Find proper rule by name
    *
    * @param rule current rule
    * @param bodyFields request body
    * @return
    */
  def isProperRule(rule: Rule, bodyFields: Map[String, JsValue]): Boolean = {
    import DefaultJsonProtocol._
    bodyFields("rule_name").convertTo[String].equals(rule.name)
  }

  /**
    * makes key from proper rule
    *
    * @param bodyFields body of request
    * @param keys list of rule
    * @return
    */
  def getKey(bodyFields: Map[String, JsValue] , keys: List[String], ruleName: String): String = {
    def joinParts(keys: List[String], key: List[String] = List(ruleName)): List[String] = {
      if (keys.isEmpty) key
      else joinParts(keys.tail, key ++ bodyFields.get(keys.head).map(_.prettyPrint))
    }
    joinParts(keys).mkString("_")
  }
}

object KeysRepository {

  val config: Config      = ConfigFactory.load()
  val statuses: Config    = config.getConfig("statuses")
  val expirations: Config = config.getConfig("expirations")

  val errorCode: Int        = if (statuses.hasPath("error_code")) statuses.getInt("error_code") else 47
  val standard_time: Long   = Some("expiration_time").filter(expirations.hasPath).map(expirations.getDuration(_, TimeUnit.SECONDS)).getOrElse(86400)
  val standard_counter: Int = Some("standard_counter").filter(config.hasPath).map(config.getInt).getOrElse(3)

  /**
    * Тестовый основной конфиг
    */
  val configRules: List[Rule] = config.getConfigList("fields").map(Rule(_)).toList
}

