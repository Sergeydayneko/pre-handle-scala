package ru.dayneko.controllers

import java.io.{BufferedReader, PrintWriter}
import java.util.stream.Collectors
import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory
import spray.json._
import ru.dayneko.utils.KeysRepository._
import ru.dayneko.utils.KeyVariables._
import ru.dayneko.database.Redis._
import ru.dayneko.database.{Lock, NotLock, LockServerError}

@WebServlet(Array("/checkData"))
class CheckDataController extends HttpServlet{
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val log                    = LoggerFactory.getLogger(this.getClass)
    var reader: BufferedReader = null
    var writer: PrintWriter    = null
    resp.setContentType("application/json")

    try {
      reader                               = req.getReader
      writer                               = resp.getWriter

      val bodyFields: Map[String, JsValue] = reader.lines().collect(Collectors.joining()).parseJson.asJsObject.fields
      log.trace("A new request has been received for CHECKING for being existing in database user. The body of request = {}", bodyFields)

      val (status, text) = configRules.filter(isProperRule(_, bodyFields)).map(r => {
        checkData(getKey(bodyFields, r.keys, r.name), r) match {
          case Lock(statusCode) => (409, s"""{"WEB_RESULT":$statusCode}""")
          case NotLock() => (200, "{}")
          case LockServerError(statusCode) => (409, s"""{"WEB_RESULT":$statusCode}""")
        }}).head

      log.debug("User has been successfully CHECKED, status = {}, response text = {}", status, text)
      resp.setStatus(status)
      resp.setContentLength(text.length)
      writer.append(text)
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
