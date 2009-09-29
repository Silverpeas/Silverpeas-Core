package com.silverpeas.thesaurus.ejb;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ThesaurusBmEJB implements SessionBean {

  private ThesaurusManager thesaurus = null;

  public ThesaurusManager getThesaurus() {
    if (thesaurus == null)
      thesaurus = new ThesaurusManager();
    return thesaurus;
  }

  public ThesaurusBmEJB() {
  }

  public List getSynonyms(long idTree, long idTerm, long idVoca) {
    List synonyms = null;
    try {
      synonyms = (List) getThesaurus().getSynonyms(idTree, idTerm, idVoca);
    } catch (Exception e) {
      throw new ThesaurusBmRuntimeException("ThesaurusBmEJB.getSynonyms",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
          "idTree = " + idTree + ", idTerm = " + idTerm + ", idVoca = "
              + idVoca, e);
    }
    return synonyms;
  }

  public List getSynonymsByTree(long idTree, long idVoca) {
    List synonyms = null;
    try {
      synonyms = (List) getThesaurus().getSynonymsByTree(idTree, idVoca);
    } catch (Exception e) {
      throw new ThesaurusBmRuntimeException("ThesaurusBmEJB.getSynonymsByTree",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
          "idTree = " + idTree + ", idVoca = " + idVoca, e);
    }
    return synonyms;
  }

  public void setSessionContext(SessionContext arg0) throws EJBException,
      RemoteException {
  }

  public void ejbCreate() {
  }

  public void ejbRemove() throws EJBException, RemoteException {
  }

  public void ejbActivate() throws EJBException, RemoteException {
  }

  public void ejbPassivate() throws EJBException, RemoteException {
  }
}