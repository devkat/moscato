package org.arriba.snippet

import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.http.S
import org.arriba.model.ArribaDb._
import org.arriba.model.Resource
import org.arriba.model.Version
import java.util.UUID
import scala.xml.Text
import net.liftweb.common._
import org.arriba.util.Path
import org.arriba.util.Path._
import net.liftweb.http.SHtml
import scala.xml.NodeSeq
import org.arriba.content.DefaultTemplates
import net.liftweb.http.FieldBinding
import net.liftweb.util.FieldContainer
import net.liftweb.http.FieldBinding._
import org.arriba.content.Template
import net.liftweb.util.Html5
import net.devkat.lift.http.CssBoundBootstrapScreen

class EditResource extends CssBoundBootstrapScreen {
  
  def formName = "edit"

  object version extends ScreenVar[Box[Version]](Empty)
  
  def editableContent() =
    version.flatMap(_.html).map(_ \ "body" \ "_").getOrElse(<p>No content</p>)
      
  val content = textarea("", editableContent.toString, FieldBinding("content"))

  override def localSetup() {
    super.localSetup()
    val resource:Option[Resource] = S.param("path").map(Path.unapply(_)) match {
      case Full(path) => { Resource.findByPath(path) match {
        case None => {
          S.notice("Parent resource " + path + " not found.")
          None
        }
        case r => r
      }}
      case Empty => None
      case Failure(msg, _, _) => {
        S.error(msg)
        None
      }
    }
    
    val v = for (r <- resource; v <- r.currentVersion) yield v
    version.set(v)
    
  }
  
  def finish() {
    version foreach { v =>
      Html5.parse(content) foreach { html =>
        v.content.set(html.toString.getBytes("utf-8"))
      }
      versions.update(v)
    }
  }
  
  override protected def redirectBack() = S.redirectTo(version.get.get.resource.href)

}