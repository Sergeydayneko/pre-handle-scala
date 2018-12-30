package com.dayneko.predialValidation.utils

import spray.json.JsValue

object AdditionalMethods {
  implicit class extraStreamMethods[A](val s: A) extends AnyVal {
    def updAndRes[B](func: A => B): (Int, String) = { func(s); (200, "{}")}
  }

  /**
    * Makes key from proper rule
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
