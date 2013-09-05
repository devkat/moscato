package org.arriba.importer

import java.io.File
import org.arriba.model.Path
import java.io.FilenameFilter
import org.arriba.record._
import org.arriba.record.ArribaDb._
import org.apache.tika.Tika
import org.arriba.content.MediaType

object HtmlFileImporter extends FileImporter {

  lazy val tika = new Tika

  def baseName(name: String) = name.lastIndexOf(".") match {
    case -1 => name
    case index @ _ => name.substring(0, index)
  }

  def importContent(file: File) {
    importResourcesInDir()(file)

  }

  def importResourcesInDir(parent:Option[Long] = None)(f: File) {
    f.listFiles(new FilenameFilter {
      override def accept(f: File, n: String) = !n.startsWith(".")
    }) foreach importResources(parent) _
  }

  def importResources(folderId: Option[Long] = None)(f: File) {
    print("Importing " + f + " -> ")
    
    /*
    def setSlug(slug:String) {
      Resource.findBySlug(slug, parent) match {
        case None => r.slug.set(slug)
        case Some(resource) => setSlug(slug + "_")
      }
    }
    
    setSlug(slug)
    
    r.slug.set(f.getName)
     */
    val slug = baseName(f.getName)
    if (f.isDirectory()) {
      println
      val folder = Folder.createRecord
      folder.slug.set(slug)
      folder.parentId.set(folderId)
      folders.insert(folder)
      importResourcesInDir(Some(folder.id))(f)
    } else {
      MediaType.parse(tika.detect(f)) match {
        case Some(mediaType) => {
          println(mediaType)
          val r = Resource.createRecord
          r.folderId.set(folderId)
          r.slug.set(slug)
          r.name.set(f.getName)
          r.setMediaType(mediaType)
          r.newVersion(read(f))
          resources.insert(r)
        }
        case None => {
          println("Could not detect media type of file " + f)
        }
      }
    }
  }

}