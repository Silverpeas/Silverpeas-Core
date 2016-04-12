/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.jcr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.jackrabbit.JcrConstants;

import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.file.FileUtil;

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.UtilException;

import static org.silverpeas.core.persistence.jcr.util.JcrConstants.NT_FOLDER;
import static org.silverpeas.core.persistence.jcr.util.JcrConstants.SLV_PROPERTY_NAME;
import static javax.jcr.Property.*;

/**
 *
 * @author ehugonnet
 */
public abstract class AbstractJcrConverter {

  /**
   * Return the property value as String for a JCR Node. If the property doesn't exist return null.
   *
   * @param node the node whose property is required.
   * @param propertyName the name of the property required.
   * @return the String value of the property - null if the property doesn't exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   */
  protected String getStringProperty(Node node, String propertyName)
      throws ValueFormatException, RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getString();
    }
    return null;
  }

  /**
   * Compute the componentId corresponding to the specified node by checking the name of the first
   * Ancestor.
   *
   * @param node the node whose componentId is required.
   * @return the componentId of the specified node.
   * @throws ItemNotFoundException
   * @throws AccessDeniedException
   * @throws RepositoryException
   */
  protected String getComponentId(Node node) throws ItemNotFoundException,
      AccessDeniedException, RepositoryException {
    return JcrDataConverter.convertFromJcrPath(node.getAncestor(1).getName());
  }

  /**
   * Defines the value of a JCR Node's property. If the specified value is null then the
   * corresponding property is removed from the Node.
   *
   * @param node the node whose property is being set.
   * @param propertyName the name of the property being set.
   * @param value the value being set. If it is null then the property is removed.
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public void addStringProperty(Node node, String propertyName,
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
   * Defines the Calendar value of a JCR Node's property. If the specified value is null then the
   * corresponding property is removed from the Node.
   *
   * @param node the node whose property is being set.
   * @param propertyName the name of the property being set.
   * @param value the value being set. If it is null then the property is removed.
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public void addDateProperty(Node node, String propertyName, Date value)
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
      Value propertyValue = node.getSession().getValueFactory().createValue(calend);
      node.setProperty(propertyName, propertyValue);
    }
  }

  /**
   * Defines the Calendar value of a JCR Node's property. If the specified value is null then the
   * corresponding property is removed from the Node.
   *
   * @param node the node whose property is being set.
   * @param propertyName the name of the property being set.
   * @param value the value being set. If it is null then the property is removed.
   * @throws VersionException
   * @throws LockException
   * @throws ConstraintViolationException
   * @throws RepositoryException
   */
  public void addCalendarProperty(Node node, String propertyName,
      Calendar value) throws VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    if (value == null) {
      try {
        node.getProperty(propertyName).remove();
      } catch (PathNotFoundException pnfex) {
      }
    } else {
      Value propertyValue = node.getSession().getValueFactory().createValue(value);
      node.setProperty(propertyName, propertyValue);
    }
  }

  /**
   * Return the property value as Calendar for a JCR Node. If the property doesn't exist return
   * null.
   *
   * @param node the node whose property is required.
   * @param propertyName the name of the property required.
   * @return the Calendar value of the property - null if the property doesn't exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   */
  protected Calendar getCalendarProperty(Node node, String propertyName)
      throws ValueFormatException, RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getDate();
    }
    return null;
  }

  /**
   * Return the property value as java.util.Date for a JCR Node. If the property doesn't exist
   * return null.
   *
   * @param node the node whose property is required.
   * @param propertyName the name of the property required.
   * @return the java.util.Date value of the property - null if the property doesn't exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   */
  protected Date getDateProperty(Node node, String propertyName)
      throws ValueFormatException, RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getDate().getTime();
    }
    return null;
  }

  /**
   * Return the property value as an int for a JCR Node. If the property doesn't exist return 0.
   *
   * @param node the node whose property is required.
   * @param propertyName the name of the property required.
   * @return the int value of the property - 0 if the property doesn't exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   */
  protected int getIntProperty(Node node, String propertyName) throws ValueFormatException,
      RepositoryException {
    if (node.hasProperty(propertyName)) {
      return Long.valueOf(node.getProperty(propertyName).getLong()).intValue();
    }
    return 0;
  }

  /**
   * Return the property value as a boolean for a JCR Node. If the property doesn't exist return
   * false.
   *
   * @param node the node whose property is required.
   * @param propertyName the name of the property required.
   * @return the boolean value of the property - false if the property doesn't exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   */
  protected boolean getBooleanProperty(Node node, String propertyName) throws ValueFormatException,
      RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getBoolean();
    }
    return false;
  }

  /**
   * Return the property value as an long for a JCR Node. If the property doesn't exist return 0.
   *
   * @param node the node whose property is required.
   * @param propertyName the name of the property required.
   * @return the long value of the property - 0 if the property doesn't exist.
   * @throws RepositoryException
   * @throws ValueFormatException
   */
  protected long getLongProperty(Node node, String propertyName)
      throws ValueFormatException, RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getLong();
    }
    return 0;
  }

  /**
   * Remove a reference from an array of javax.jcr.Value. If the reference is not found no change is
   * done to the array.
   *
   * @param values the array of references
   * @param uuid the reference to be removed
   * @return the updated arry of references.
   * @throws ValueFormatException
   * @throws IllegalStateException
   * @throws RepositoryException
   */
  protected Value[] removeReference(Value[] values, String uuid) throws ValueFormatException,
      IllegalStateException, RepositoryException {
    List<Value> references = CollectionUtil.asList(values);
    Iterator<Value> iter = references.iterator();
    while (iter.hasNext()) {
      Value value = iter.next();
      if (uuid.equals(value.getString())) {
        iter.remove();
        return references.toArray(new Value[values.length - 1]);
      }
    }
    return values;
  }

  /**
   * Compute a unique node name if a node with the same name already exists under the same parent
   * node.
   *
   * @param prefix
   * @param tableName the name of the column used to stored the id.
   * @return the name of the node.
   * @throws UtilException
   */
  protected String computeUniqueName(String prefix, String tableName) throws SQLException {
    return prefix + DBUtil.getNextId(tableName, null);
  }

  /**
   *
   * @param fileNode
   * @param content
   * @param mimeType
   * @throws RepositoryException
   */
  public void setContent(Node fileNode, InputStream content, String mimeType) throws
      RepositoryException {
    Node contentNode;
    if (fileNode.hasNode(JCR_CONTENT)) {
      contentNode = fileNode.getNode(JCR_CONTENT);
    } else {
      contentNode = fileNode.addNode(JCR_CONTENT, JcrConstants.NT_RESOURCE);
    }
    Binary binaryContent = fileNode.getSession().getValueFactory().createBinary(content);
    contentNode.setProperty(JCR_DATA, binaryContent);
    binaryContent.dispose();
    String fileMimeType = mimeType;
    if (fileMimeType == null) {
      fileMimeType = FileUtil.getMimeType(fileNode.getProperty(SLV_PROPERTY_NAME).getString());
    }
    contentNode.setProperty(JCR_ENCODING, CharEncoding.UTF_8);
    contentNode.setProperty(JCR_MIMETYPE, fileMimeType);
    Calendar lastModified = Calendar.getInstance();
    contentNode.setProperty(JCR_LAST_MODIFIED, lastModified);
  }

  /**
   * Returns the mime-type of the jcr:content node stored in the fileNode.
   *
   * @param fileNode
   * @return the mime-type of the jcr:content node stored in the fileNode.
   * @throws RepositoryException
   */
  public String getContentMimeType(Node fileNode) throws RepositoryException {
    if (fileNode.hasNode(JCR_CONTENT)) {
      Node contentNode = fileNode.getNode(JCR_CONTENT);
      return getStringProperty(contentNode, JCR_MIMETYPE);
    }
    return null;
  }

  /**
   * Return the size of the file in the jcr:content node which is a child node of the specified
   * node.
   *
   * @param fileNode
   * @return the size of the content.
   * @throws RepositoryException
   */
  public long getContentSize(Node fileNode) throws RepositoryException {
    if (fileNode.hasNode(JCR_CONTENT)) {
      Node contentNode = fileNode.getNode(JCR_CONTENT);
      return getSize(contentNode);
    }
    return 0L;
  }

  /**
   *
   * @param fileNode
   * @param file
   * @param mimeType
   * @throws RepositoryException
   */
  public void setContent(Node fileNode, File file, String mimeType) throws RepositoryException {
    InputStream in = null;
    try {
      in = new FileInputStream(file);
      setContent(fileNode, in, mimeType);
    } catch (FileNotFoundException ex) {
      throw new RepositoryException(ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Add binary content to the specified node.
   *
   * @param fileNode the node.
   * @param content the binary content.
   * @param mimeType the mime type of the content.
   * @throws RepositoryException
   */
  public void setContent(Node fileNode, byte[] content, String mimeType) throws RepositoryException {
    ByteArrayInputStream in = new ByteArrayInputStream(content);
    try {
      setContent(fileNode, in, mimeType);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   *
   * @param fileNode
   * @return
   * @throws RepositoryException
   */
  public byte[] getContent(Node fileNode) throws RepositoryException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Node contentNode;
    if (fileNode.hasNode(JCR_CONTENT)) {
      contentNode = fileNode.getNode(JCR_CONTENT);
      InputStream in = contentNode.getProperty(JCR_DATA).getBinary().getStream();
      try {
        IOUtils.copy(in, out);
      } catch (IOException ioex) {
        throw new RepositoryException(ioex);
      } finally {
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
      }
      return out.toByteArray();
    }
    return ArrayUtil.EMPTY_BYTE_ARRAY;
  }

  /**
   *
   * @param fileNode
   * @return
   * @throws RepositoryException
   */
  public Binary getBinaryContent(Node fileNode) throws RepositoryException {
    Node contentNode;
    if (fileNode.hasNode(JCR_CONTENT)) {
      contentNode = fileNode.getNode(JCR_CONTENT);
      if (contentNode.hasProperty(JCR_DATA)) {
        return contentNode.getProperty(JCR_DATA).getBinary();
      }
    }
    return null;
  }

  /**
   *
   * @param fileNode
   * @param out
   * @throws RepositoryException
   */
  public void getContent(Node fileNode, OutputStream out) throws RepositoryException, IOException {
    if (fileNode.hasNode(JCR_CONTENT)) {
      Node contentNode = fileNode.getNode(JCR_CONTENT);
      Binary content = contentNode.getProperty(JCR_DATA).getBinary();
      InputStream in = content.getStream();
      try {
        IOUtils.copy(in, out);
      } finally {
        IOUtils.closeQuietly(in);
        content.dispose();
      }
    }

  }

  /**
   *
   * @param contentNode
   * @return
   * @throws ValueFormatException
   * @throws PathNotFoundException
   * @throws RepositoryException
   */
  private long getSize(Node contentNode) throws ValueFormatException, PathNotFoundException,
      RepositoryException {
    if (contentNode.hasProperty(JCR_DATA)) {
      return contentNode.getProperty(JCR_DATA).getBinary().getSize();
    }
    return 0L;
  }

  /**
   * Return the node whith the specified parent and name. Create a nt:folder with the specified
   * parent and name if the node doesn't exist.
   *
   * @param parent parent node of the folder.
   * @param name name of the folder.
   * @return the node whith the specified parent and name.
   * @throws RepositoryException
   */
  public Node getFolder(Node parent, String name) throws RepositoryException {
    if (parent.hasNode(name)) {
      return parent.getNode(name);
    }
    return parent.addNode(name, NT_FOLDER);
  }

  /**
   * Return true if the specified mixin type is explicitly assigned to the node. It does not include
   * mixin types inherited through the addition of supertypes to the primary type hierarchy or
   * through the addition of supertypes to the type hierarchy of any of the declared mixin types.
   *
   * @param node the node on which we are looking for the specified mixin.
   * @param mixin the name of the mixin.
   * @return rue if the specified mixin type is explicitly assigned to the node false otherwise.
   * @throws RepositoryException
   */
  public boolean isMixinApplied(Node node, String mixin) throws RepositoryException {
    for (NodeType type : node.getMixinNodeTypes()) {
      if (type.isNodeType(mixin)) {
        return true;
      }
    }
    return false;
  }
}
