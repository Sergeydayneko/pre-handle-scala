package com.dayneko.crmValidation.utils

import java.io.File
import scala.reflect.runtime.{currentMirror, universe}
import scala.tools.reflect.ToolBox

object Eval {
  def apply[A](string: String): A = {
    val toolbox: ToolBox[universe.type] = currentMirror.mkToolBox()
    val tree: toolbox.u.Tree = toolbox.parse(string)
    toolbox.eval(tree).asInstanceOf[A]
  }

  def fromFile[A](file: File): A =
    apply(scala.io.Source.fromFile(file).mkString(""))

  def fromFileName[A](file: String): A =
    fromFile(new File(file))

}
