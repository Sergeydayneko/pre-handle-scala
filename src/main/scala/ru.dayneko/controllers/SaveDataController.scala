package ru.dayneko.controllers

import java.io.{BufferedReader, PrintWriter}
import java.util.stream.Collectors
import ru.dayneko.database.Redis._
import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory
import spray.json._
import ru.dayneko.utils.KeyVariables._
import ru.dayneko.utils.KeysRepository._

@WebServlet(Array("/saveData"))
class SaveDataController extends HttpServlet{
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val log                    = LoggerFactory.getLogger(this.getClass)
    var reader: BufferedReader = null
    var writer: PrintWriter    = null
    resp.setContentType("application/json")

    try {
      reader         = req.getReader
      writer         = resp.getWriter

      val bodyFields: Map[String, JsValue] = reader.lines().collect(Collectors.joining()).parseJson.asJsObject.fields
      log.trace("A new request has been received for SAVING in database user. The body of request = {}", bodyFields)

      configRules
        .filter(isProperRule(_, bodyFields))
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
        val body = s"""{"WEB_RESULT":$errorCode}"""
        resp.setContentLength(body.length)
        Some(writer).foreach(_.append(body))
    }
  }
}
