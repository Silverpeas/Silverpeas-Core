package com.silverpeas.jcrutil.model;

import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;

public class AbstractJcrService {
  private Repository repository;

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public Session getSession() throws LoginException, RepositoryException {
    return repository.login(new SilverpeasSystemCredentials());
  }
}
