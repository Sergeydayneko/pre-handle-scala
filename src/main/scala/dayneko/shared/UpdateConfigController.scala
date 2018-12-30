package com.dayneko.shared

import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

@WebServlet(Array("/updateConfig"))
class UpdateConfigController extends HttpServlet{
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
      // черновик удален до прояснения части вопросов
  }
}
