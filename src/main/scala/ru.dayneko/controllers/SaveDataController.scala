package controllers

import java.io.{BufferedReader, PrintWriter}
import java.util.stream.Collectors
import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory
import spray.json._
import utils.KeyVariables.extractData
import utils.KeysRepository.{ws_key, web_key, phc_key, _}
import database.Redis._

/**
  * Created by s.dayneko 04.07.2018
  */
@WebServlet(Array("/dataSave"))
class SaveDataController extends HttpServlet{
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val log                    = LoggerFactory.getLogger(this.getClass)
    var reader: BufferedReader = null
    var writer: PrintWriter    = null
    resp.setContentType("application/json")

    try {
      reader         = req.getReader
      writer         = resp.getWriter
      val bodyFields = reader.lines().collect(Collectors.joining()).parseJson.asJsObject.fields
      log.trace("A new request has been received for SAVING in database user. The body of request = {}", bodyFields)

      saveData(extractData(bodyFields, phc_key), extractData(bodyFields, web_key), extractData(bodyFields, ws_key).getOrElse(""))

      log.debug("User has been successfully SAVED in black list")

      resp.setStatus(200)
      resp.setContentLength("{}".length)
      writer.append("{}")
    } catch {
      case e: Exception =>
        log.warn("Exceptional situation in initial handler", e)
        resp.setStatus(409)
        val body = "{\"WEB_RESULT\":" + errorCode + "}"
        resp.setContentLength(body.length)
        if (writer != null) writer.append(body)
    }
  }
}
