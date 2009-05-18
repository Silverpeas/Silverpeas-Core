package com.stratelia.silverpeas.versioning.ejb;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.stratelia.silverpeas.versioning.jcr.JcrDocumentService;

public class RepositoryHelper {
  public static final String JRC_DOCUMENT_SERVICE = "jcrDocumentManager";

  public static JcrDocumentService getJcrDocumentService() {
    return (JcrDocumentService) BasicDaoFactory.getBean(JRC_DOCUMENT_SERVICE);
  }

}