package com.dayneko.predialValidation.model

import java.util.concurrent.TimeUnit
import com.dayneko.shared.ConfigVariables._
import com.typesafe.config.Config
import scala.collection.JavaConverters._

case class Rule(name: String, keys: List[String], duration: Int, status: Int, counter: Int)
object Rule {
    def apply(config: Config): Rule = {
        new Rule(
            Some("name").filter(config.hasPath).map(config.getString).get,
            Some("keys").filter(config.hasPath).map(config.getAnyRefList).map(_.asScala.toList.map(_.toString)).get,
            Some("expiration").filter(config.hasPath).map(config.getDuration(_, TimeUnit.SECONDS)).getOrElse(standard_time).toInt,
            Some("status").filter(config.hasPath).map(config.getInt).getOrElse(standard_code),
            Some("maxCount").filter(config.hasPath).map(config.getInt).getOrElse(standard_counter)
        )
    }
}

