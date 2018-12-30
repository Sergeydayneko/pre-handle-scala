package com.dayneko.crmValidation.model

sealed trait Result {
  def isBlock = false
}

case object Ack extends Result

case class Block(status: Int) extends Result {
  override def isBlock: Boolean = true
}

case class Rule(name: String, group: String,
                validation: (Map[String, String], Map[String, String]) => Result,
                postProccessing: (Result, Map[String, String], Map[String, String]) => (Int, String))

//case class Validation(validation: (Map[String, String], Map[String, String]) => Boolean)
