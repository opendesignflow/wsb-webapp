package com.idyria.osi.wsb.webapp.db

import com.idyria.osi.ooxoo.db.store.DocumentStore
import com.idyria.osi.wsb.webapp.db.Database
import com.idyria.osi.ooxoo.db.store.DocumentContainer
import com.idyria.osi.wsb.webapp.view.WWWView

/**
 * Wrapper for OOXOO Db to be added in application DB
 */
class OOXOODatabase(var documentStore: DocumentStore) extends Database with DocumentStore {

  // Init
  //----------

  //-- Make sure this class is available for views
  WWWView.addCompileImport(getClass)

  def container(name: String): DocumentContainer = documentStore.container(name)

  def containers: Iterable[DocumentContainer] = documentStore.containers

}
