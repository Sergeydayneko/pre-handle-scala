package com.dayneko.crmValidation.controller

import java.io.{BufferedReader, PrintWriter}
import java.util.stream.Collectors
import com.dayneko.crmValidation.model.Rule
import com.dayneko.shared.ConfigVariables._
import com.dayneko.crmValidation.model._
import com.dayneko.crmValidation.utils.Eval
import com.typesafe.config.Config
import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.slf4j.{Logger, LoggerFactory}
import spray.json._
import scala.collection.JavaConverters._


@WebServlet(Array("/checkAttrs"))
class CheckAttributes extends HttpServlet{
  val log: Logger  = LoggerFactory.getLogger(this.getClass)

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    var reader: BufferedReader = null
    var writer: PrintWriter    = null

    resp.setContentType("application/json")

    try {
      reader                               = req.getReader
      writer                               = resp.getWriter
      val bodyFields: Map[String, JsValue] = reader.lines().collect(Collectors.joining()).parseJson.asJsObject.fields
      val configRules: List[Rule]  = rules.asScala.map(createRule).toList

      val (businessAttributes, systemAttributes) = parseJson(bodyFields)

      val result: Result = configRules.map(_.validation(systemAttributes, businessAttributes)).find(_.isBlock).getOrElse(Ack)
      val response = configRules.foreach(_.postProccessing(result, systemAttributes, businessAttributes ))


      log.trace("A new request has been received for CHECKING for being existing in database. The body of request = {}", bodyFields)


      log.debug("User has been successfully CHECKED, status = {}, response text = {}")
      resp.setStatus(404)
      resp.setContentLength("drools income".length)
      writer.append("drools income")
    } catch {
      case e: Exception =>
        e.printStackTrace()
        log.warn("Exceptional situation in initial handler", e)
        resp.setStatus(409)
        val body = s"""{"GSW_CALL_RESULT":$error_code}"""
        resp.setContentLength(body.length)
        Some(writer).foreach(_.append(body))
    }
  }

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
    Eval[Rule](s"import com.dayneko.crmValidation.controller._\n" +
      "import com.dayneko.crmValidation.model._\n" +
      "import com.dayneko.crmValidation.database._\n" +
      "Rule(\"" + name + "\",\"" + group + "\",(systemAttributes,businessAttributes) => {" + validation + "}, (result, systemAttributes,businessAttributes) => {" + postprocessing + "})")
  }

  /**
    * Retrieval key values from body request
    * @param requestBody is body from http request
    * @return tuple with values of business and system attributes
    */
  def parseJson(requestBody: Map[String, JsValue]): (Map[String, String], Map[String, String]) = {
    def getAttrsValue(attrs: List[String], keyValueJson: Map[String, String] = Map()): Map[String, String] = {
      if (attrs.isEmpty) keyValueJson
      else getAttrsValue(attrs.tail, keyValueJson ++ Map(attrs.head -> requestBody.get(attrs.head).map(_.prettyPrint).get))
    }
    log.info("Recovering data from request(checkAttrs controller)")
    (getAttrsValue(businessAttrsKeys), getAttrsValue(systemAttrsKeys))
  }

}
