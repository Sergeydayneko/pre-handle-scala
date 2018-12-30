package com.dayneko.crmValidation.utils

import java.util.concurrent.TimeUnit

import com.dayneko.predialValidation.model.Rule
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.JsValue

import scala.collection.JavaConverters._

object KeysRepository {
  /**
    * Creating from config parameters and methods from string
    * @param config current config
    * @return completed rule with methods and parameters
    */
  def createRule(config: Config): Rule = {
    val name = config.getString("name")
    val group = config.getString("group")
    val validation = config.getString("validation")
    val postprocessing = config.getString("postprocessing")
    Eval[Rule](s"import com.dayneko.evaltest._\n" +
      "Rule(\"" + name + "\",\"" + group + "\",(systemAttributes,businessAttributes) => {" + validation + "}, (result, systemAttributes,businessAttributes) => {" + postprocessing + "})")
  }

}



