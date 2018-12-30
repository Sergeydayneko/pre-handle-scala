package com.dayneko.predialValidation.blacklist.controllers

import java.io.{BufferedReader, PrintWriter}
import java.util.stream.Collectors

import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.slf4j.{Logger, LoggerFactory}
import spray.json._
import com.dayneko.predialValidation.blacklist.database.BlacklistRedis._
import com.dayneko.predialValidation.model.Rule
import com.dayneko.shared.ConfigVariables._
import com.dayneko.predialValidation.utils.AdditionalMethods._
import scala.collection.JavaConverters._

@WebServlet(Array("/saveData"))
class SaveDataController extends HttpServlet{
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val log: Logger            = LoggerFactory.getLogger(this.getClass)
    var reader: BufferedReader = null
    var writer: PrintWriter    = null
    resp.setContentType("application/json")

    try {
      reader                               = req.getReader
      writer                               = resp.getWriter
      val bodyFields: Map[String, JsValue] = reader.lines().collect(Collectors.joining()).parseJson.asJsObject.fields
      val configRules: List[Rule]          = rules.asScala.map(Rule(_)).toList
      log.trace("A new request has been received for SAVING in database. The body of request = {}", bodyFields)

      configRules.filter(isProperRule(_, bodyFields))
                 .foreach(r => saveData(getKey(bodyFields, r.keys, r.name), r.duration, r.counter.toString))

      log.debug("User has been successfully SAVED in black list")

      resp.setStatus(200)
      resp.setContentLength("{}".length)
      writer.append("{}")
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
    * Find proper rule by name when saving request
    *
    * @param rule current rule
    * @param bodyFields request body
    * @return
    */
  def isProperRule(rule: Rule, bodyFields: Map[String, JsValue]): Boolean = {
    import DefaultJsonProtocol._
    bodyFields("rule_name").convertTo[String].equals(rule.name)
  }
}
