package com.dayneko.predialValidation.whitelist.controllers

import java.io.{BufferedReader, PrintWriter}
import java.util.stream.Collectors
import com.dayneko.predialValidation.whitelist.database.WhitelistRedis.{postProcessAct, preProcessCheck}
import com.dayneko.predialValidation.model.Rule
import com.dayneko.shared.ConfigVariables._
import com.dayneko.predialValidation.utils.AdditionalMethods._
import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.slf4j.{Logger, LoggerFactory}
import spray.json._
import scala.collection.JavaConverters._

@WebServlet(Array("/checkCompany"))
class CheckDataController extends HttpServlet{
  val log: Logger  = LoggerFactory.getLogger(this.getClass)

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    import DefaultJsonProtocol._
    var reader: BufferedReader = null
    var writer: PrintWriter    = null

    resp.setContentType("application/json")

    try {
      reader                               = req.getReader
      writer                               = resp.getWriter
      val bodyFields: Map[String, JsValue] = reader.lines().collect(Collectors.joining()).parseJson.asJsObject.fields
      val requestRules: List[String]       = bodyFields("rules").convertTo[List[String]]
      val configRules: List[Rule]          = rules.asScala.map(Rule(_)).toList

      log.trace("A new request has been received for CHECKING for being existing in database. The body of request = {}", bodyFields)

      val (status, text) = postProcessAct(configRules.filter(isProperRules(_, requestRules))
                                                     .map(r => preProcessCheck(getKey(bodyFields, r.keys, r.name), r)))

      log.debug("User has been successfully CHECKED, status = {}, response text = {}", status, text)
      resp.setStatus(status)
      resp.setContentLength(text.length)
      writer.append(text)
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
    * Find proper rules when using check request
    *
    * @param rule current rule
    * @param requestRules request body rules
    * @return
    */
  def isProperRules(rule: Rule, requestRules: List[String]): Boolean = requestRules.contains(rule.name)
}
