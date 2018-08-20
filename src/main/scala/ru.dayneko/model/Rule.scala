package ru.dayneko.model

import java.util.concurrent.TimeUnit
import ru.dayneko.utils.KeysRepository._
import com.typesafe.config.Config
import scala.collection.JavaConversions._

case class Rule(name: String, keys: List[String], duration: Int, status: Int, counter: Int)
object Rule {
    def apply(config: Config) = new Rule(
    Some("name").filter(config.hasPath).map(config.getString).getOrElse("no_name"),
    Some("keys").filter(config.hasPath).map(config.getAnyRefList).map(_.toList.map(_.toString)).getOrElse(List("")),
    Some("duration").filter(config.hasPath).map(config.getDuration(_, TimeUnit.SECONDS)).getOrElse(standard_time).toInt,
    Some("status").filter(config.hasPath).map(config.getInt).getOrElse(50),
    Some("maxCount").filter(config.hasPath).map(config.getInt).getOrElse(standard_counter)
  )
}

