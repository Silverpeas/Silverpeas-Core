/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.jcrutil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.silverpeas.jcrutil.converter.ConverterUtil;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.util.FileUtil;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * He
 * 
 * @author Emmanuel Hugonnet
 * 
 */
public class BasicDaoFactory implements ApplicationContextAware {

  public static final String JRC_REPOSITORY = "repository";

  public static final String COUNTER_SEPARATOR = "__-__";

  private ApplicationContext context;
  private static BasicDaoFactory instance;

  private BasicDaoFactory() {
  }

  public void setApplicationContext(ApplicationContext context)
      throws BeansException {
    this.context = context;
  }

  public static BasicDaoFactory getInstance() {
    synchronized (BasicDaoFactory.class) {
      if (BasicDaoFactory.instance == null) {
        BasicDaoFactory.instance = new BasicDaoFactory();
      }
    }
    return BasicDaoFactory.instance;
  }

  public static Object getBean(String name) {
    return getInstance().context.getBean(name);
  }

  /**
   * Create a system JCR session
   * 
   * @return a jcrSession with System rights.
   * @throws LoginException
   * @throws RepositoryException
   */
  public static Session getSystemSession() throws LoginException,
      RepositoryException {
    return ((Repository) getInstance().context.getBean(JRC_REPOSITORY))
        .login(new SilverpeasSystemCredentials());
  }

  /**
   * Create a system JCR session
   * 
   * @param login
   *          the user's login.
   * @param password
   *          the user's password.
   * @return a jcrSession with user's rights.
   * @throws LoginException
   * @throws RepositoryException
   */
  public static Session getAuthentifiedSession(String login, String password)
      throws LoginException, RepositoryException {
    return ((Repository) getInstance().context.getBean(JRC_REPOSITORY))
        .login(new SimpleCredentials(login, password.toCharArray()));
  }

  /**
   * Logout of the JCR session
   * 
   * @param session
   *          the session to be closed.
   */
  public static void logout(Session session) {
    if (session != null) {
      session.logout();
    }
  }

  /**
   * Compute the componentId corresponding to the specified node by checking the
   * name of the first Ancestor.
   * 
   * @param node
   *          the node whose componentId is required.
   * @return the componentId of the specified node.
   * @throws ItemNotFoundException
   * @throws AccessDeniedException
   * @throws RepositoryException
   */
  public static String getComponentId(Node node) throws ItemNotFoundException,
      AccessDeniedException, RepositoryException {
    return ConverterUtil.convertFromJcrPath(node.getAncestor(1).getName());
  }

  /**
   * Defines the value of a JCR Node's property. If the specified value is null
   * then the corresponding property is removed from the Node.
   * 
   * @param node
   *          the node whose property is being set.
   * @param propertyName
   *          the name of the property being set.
   * @param value
   *          the value being set. If it is null then the property is removed.
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public static void addStringProperty(Node node, String propertyName,
      String value) throws VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    if (value == null) {
      try {
        node.getProperty(propertyName).remove();
      } catch (PathNotFoundException pnfex) {
      }
    } else {
      node.setProperty(propertyName, value);
    }
  }

  /**
   * Defines the Calendar value of a JCR Node's property. If the specified value
   * is null then the corresponding property is removed from the Node.
   * 
   * @param node
   *          the node whose property is being set.
   * @param propertyName
   *          the name of the property being set.
   * @param value
   *          the value being set. If it is null then the property is removed.
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public static void addDateProperty(Node node, String propertyName, Date value)
      throws VersionException, LockException, ConstraintViolationException,
      RepositoryException {
    if (value == null) {
      try {
        node.getProperty(propertyName).remove();
      } catch (PathNotFoundException pnfex) {
      }
    } else {
      Calendar calend = Calendar.getInstance();
      calend.setTime(value);
      node.setProperty(propertyName, calend);
    }
  }

  /**
   * Defines the Calendar value of a JCR Node's property. If the specified value
   * is null then the corresponding property is removed from the Node.
   * 
   * @param node
   *          the node whose property is being set.
   * @param propertyName
   *          the name of the property being set.
   * @param value
   *          the value being set. If it is null then the property is removed.
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public static void addCalendarProperty(Node node, String propertyName,
      Calendar value) throws VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    if (value == null) {
      try {
        node.getProperty(propertyName).remove();
      } catch (PathNotFoundException pnfex) {
      }
    } else {
      node.setProperty(propertyName, value);
    }
  }

  /**
   * Return the property value as String for a JCR Node. If the property doesn't
   * exist return null.
   * 
   * @param node
   *          the node whose property is required.
   * @param propertyName
   *          the name of the property required.
   * @return the String value of the property - null if the property doesn't
   *         exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public static String getStringProperty(Node node, String propertyName)
      throws ValueFormatException, RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getString();
    }
    // property may not exist
    return null;
  }

  /**
   * Return the property value as Calendar for a JCR Node. If the property
   * doesn't exist return null.
   * 
   * @param node
   *          the node whose property is required.
   * @param propertyName
   *          the name of the property required.
   * @return the Calendar value of the property - null if the property doesn't
   *         exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public static Calendar getCalendarProperty(Node node, String propertyName)
      throws ValueFormatException, RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getDate();
    }
    // property may not exist
    return null;
  }

  /**
   * Return the property value as java.util.Date for a JCR Node. If the property
   * doesn't exist return null.
   * 
   * @param node
   *          the node whose property is required.
   * @param propertyName
   *          the name of the property required.
   * @return the java.util.Date value of the property - null if the property
   *         doesn't exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public static Date getDateProperty(Node node, String propertyName)
      throws ValueFormatException, RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getDate().getTime();
    }
    // property may not exist
    return null;
  }

  /**
   * Return the property value as an int for a JCR Node. If the property doesn't
   * exist return 0.
   * 
   * @param node
   *          the node whose property is required.
   * @param propertyName
   *          the name of the property required.
   * @return the int value of the property - 0 if the property doesn't exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public static int getIntProperty(Node node, String propertyName)
      throws ValueFormatException, RepositoryException {
    if (node.hasProperty(propertyName)) {
      return Long.valueOf(node.getProperty(propertyName).getLong()).intValue();
    }
    // property may not exist
    return 0;
  }

  /**
   * Return the property value as an long for a JCR Node. If the property
   * doesn't exist return 0.
   * 
   * @param node
   *          the node whose property is required.
   * @param propertyName
   *          the name of the property required.
   * @return the long value of the property - 0 if the property doesn't exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public static long getLongProperty(Node node, String propertyName)
      throws ValueFormatException, RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getLong();
    }
    // property may not exist
    return 0;
  }

  /**
   * Remove a reference from an array of javax.jcr.Value. If the reference is
   * not found no change is done to the array.
   * 
   * @param values
   *          the array of references
   * @param uuid
   *          the reference to be removed
   * @return the updated arry of references.
   * @throws ValueFormatException
   * @throws IllegalStateException
   * @throws RepositoryException
   */
  public static Value[] removeReference(Value[] values, String uuid)
      throws ValueFormatException, IllegalStateException, RepositoryException {
    List<Value> references = new ArrayList<Value>(Arrays.asList(values));
    Iterator<Value> iter = references.iterator();
    while (iter.hasNext()) {
      Value value = (Value) iter.next();
      if (uuid.equals(value.getString())) {
        iter.remove();
        return (Value[]) references.toArray(new Value[values.length - 1]);
      }
    }
    return values;
  }

  /**
   * Compute a unique node name if a node with the same name already exists
   * under the same parent node.
   * 
   * @param tableName
   *          the name of the column used to stored the id.
   * @return the name of the node.
   * @throws RepositoryException
   * @throws UtilException
   */
  public static String computeUniqueName(String prefix, String tableName)
      throws UtilException {
    return prefix + DBUtil.getNextId(tableName, null);
  }

  public static void setContent(Node fileNode, InputStream content,
      String mimeType) throws RepositoryException, IOException {
    Node contentNode;
    if (fileNode.hasNode(JcrConstants.JCR_CONTENT)) {
      contentNode = fileNode.getNode(JcrConstants.JCR_CONTENT);
    } else {
      contentNode = fileNode.addNode(JcrConstants.JCR_CONTENT,
          JcrConstants.NT_RESOURCE);
    }
    contentNode.setProperty(JcrConstants.JCR_DATA, content);
    String fileMimeType = mimeType;
    if (fileMimeType == null) {
      fileMimeType = FileUtil.getMimeType(fileNode.getProperty(
          JcrConstants.SLV_PROPERTY_NAME).getString());
    }
    contentNode.setProperty(JcrConstants.JCR_MIMETYPE, fileMimeType);
    Calendar lastModified = Calendar.getInstance();
    contentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, lastModified);
  }

  public static void setContent(Node fileNode, File file, String mimeType)
      throws RepositoryException, IOException {
    InputStream in = null;
    try {
      in = new FileInputStream(file);
      setContent(fileNode, in, mimeType);
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

  public static void setContent(Node fileNode, byte[] content, String mimeType)
      throws RepositoryException, IOException {
    ByteArrayInputStream in = null;
    try {
      in = new ByteArrayInputStream(content);
      setContent(fileNode, in, mimeType);
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

  public static byte[] getContent(Node fileNode) throws RepositoryException,
      IOException {
    ByteArrayOutputStream out = null;
    InputStream in = null;
    try {
      out = new ByteArrayOutputStream();
      Node contentNode;
      if (fileNode.hasNode(JcrConstants.JCR_CONTENT)) {
        contentNode = fileNode.getNode(JcrConstants.JCR_CONTENT);
        in = contentNode.getProperty(JcrConstants.JCR_DATA).getStream();
        byte[] buffer = new byte[8];
        int c = 0;
        while ((c = in.read(buffer)) >= 0) {
          out.write(buffer, 0, c);
        }
        out.close();
        return out.toByteArray();
      } else {
        return new byte[0];
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

  public static void getContent(Node fileNode, OutputStream out)
      throws RepositoryException, IOException {
    InputStream in = null;
    try {
      Node contentNode;
      if (fileNode.hasNode(JcrConstants.JCR_CONTENT)) {
        contentNode = fileNode.getNode(JcrConstants.JCR_CONTENT);
        in = contentNode.getProperty(JcrConstants.JCR_DATA).getStream();
        byte[] buffer = new byte[8];
        int c = 0;
        while ((c = in.read(buffer)) >= 0) {
          out.write(buffer, 0, c);
        }
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

  public static long getSize(Node contentNode) throws ValueFormatException,
      PathNotFoundException, RepositoryException {
    if (contentNode.hasProperty(JcrConstants.JCR_DATA)) {
      return contentNode.getProperty(JcrConstants.JCR_DATA).getLength();
    }
    return 0;
  }
}
