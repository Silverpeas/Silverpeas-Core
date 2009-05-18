package com.silverpeas.jcrutil.model;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import com.silverpeas.jcrutil.BasicDaoFactory;

public class AbstractJcrDao {
  public static void addProperty(Node node, String propertyName, String value)
      throws VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    BasicDaoFactory.addStringProperty(node, propertyName, value);
  }
}
