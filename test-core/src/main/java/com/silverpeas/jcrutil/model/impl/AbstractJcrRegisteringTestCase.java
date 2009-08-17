package com.silverpeas.jcrutil.model.impl;

import java.io.FileNotFoundException;

import javax.jcr.AccessDeniedException;
import javax.jcr.NamespaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
import org.apache.jackrabbit.core.nodetype.compact.ParseException;

import com.silverpeas.jcrutil.model.SilverpeasRegister;

public abstract class AbstractJcrRegisteringTestCase extends
    AbstractJcrTestCase {

  protected static boolean registred = false;

  protected Repository repository;

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public AbstractJcrRegisteringTestCase() {
    super();
  }

  public AbstractJcrRegisteringTestCase(String name) {
    super(name);
  }

  public void registerSilverpeasNodeTypes() throws NamespaceException,
      UnsupportedRepositoryOperationException, AccessDeniedException,
      RepositoryException, ParseException, FileNotFoundException,
      InvalidNodeTypeDefException {
    if (registred) {
      return;
    }
    String cndFileName = this.getClass().getClassLoader().getResource(
        "silverpeas-jcr.txt").getFile().toString().replaceAll("%20", " ");
    SilverpeasRegister.registerNodeTypes(cndFileName);
    registred = true;
  }

}
