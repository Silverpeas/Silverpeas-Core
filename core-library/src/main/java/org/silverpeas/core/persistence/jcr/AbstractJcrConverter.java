/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.jcr;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static javax.jcr.Property.*;
import static org.silverpeas.core.persistence.jcr.util.JcrConstants.NT_FOLDER;
import static org.silverpeas.core.persistence.jcr.util.JcrConstants.SLV_PROPERTY_NAME;

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
   * @throws RepositoryException on error
   */
  protected String getStringProperty(Node node, String propertyName)
      throws RepositoryException {
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
   * @throws RepositoryException on error
   */
  protected String getComponentId(Node node) throws RepositoryException {
    return JcrDataConverter.convertFromJcrPath(node.getAncestor(1).getName());
  }

  /**
   * Defines the value of a JCR Node's property. If the specified value is null then the
   * corresponding property is removed from the Node.
   *
   * @param node the node whose property is being set.
   * @param propertyName the name of the property being set.
   * @param value the value being set. If it is null then the property is removed.
   * @throws RepositoryException on error
   */
  public void addStringProperty(Node node, String propertyName,
      String value) throws RepositoryException {
    if (value == null) {
      try {
        node.getProperty(propertyName).remove();
      } catch (PathNotFoundException e) {
        SilverLogger.getLogger(this).silent(e);
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
   * @throws RepositoryException on error
   */
  public void addDateProperty(Node node, String propertyName, Date value)
      throws RepositoryException {
    if (value == null) {
      try {
        node.getProperty(propertyName).remove();
      } catch (PathNotFoundException e) {
        SilverLogger.getLogger(this).silent(e);
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
   * @throws RepositoryException on error
   */
  public void addCalendarProperty(Node node, String propertyName,
      Calendar value) throws RepositoryException {
    if (value == null) {
      try {
        node.getProperty(propertyName).remove();
      } catch (PathNotFoundException e) {
        SilverLogger.getLogger(this).silent(e);
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
   * @throws RepositoryException on error
   */
  protected Calendar getCalendarProperty(Node node, String propertyName)
      throws RepositoryException {
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
   * @throws RepositoryException on error
   */
  protected Date getDateProperty(Node node, String propertyName) throws RepositoryException {
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
   * @throws RepositoryException on error
   */
  protected int getIntProperty(Node node, String propertyName) throws RepositoryException {
    if (node.hasProperty(propertyName)) {
      return ((Long)(node.getProperty(propertyName).getLong())).intValue();
    }
    return 0;
  }

  /**
   * Return the property value as a boolean for a JCR Node. If the property doesn't exist return
   * false.
   *
   * @param node the node whose property is required.
   * @param propertyName the name of the property required.
   * @param defaultValueIfNull the default value of the value does not exist
   * @return the boolean value of the property - false if the property doesn't exist.
   * @throws RepositoryException on error
   */
  protected Boolean getBooleanProperty(Node node, String propertyName,
      final Boolean defaultValueIfNull) throws RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getBoolean();
    }
    return defaultValueIfNull;
  }

  /**
   * Return the property value as an long for a JCR Node. If the property doesn't exist return 0.
   *
   * @param node the node whose property is required.
   * @param propertyName the name of the property required.
   * @return the long value of the property - 0 if the property doesn't exist.
   * @throws RepositoryException on error
   */
  protected long getLongProperty(Node node, String propertyName) throws RepositoryException {
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
   * @throws RepositoryException on error
   */
  protected Value[] removeReference(Value[] values, String uuid) throws RepositoryException {
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
   * @param prefix a prefix to add to the node name
   * @param tableName the name of the column used to stored the id.
   * @return the name of the node.
   */
  protected String computeUniqueName(String prefix, String tableName) {
    return prefix + DBUtil.getNextId(tableName, null);
  }

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
    contentNode.setProperty(JCR_ENCODING, Charsets.UTF_8.name());
    contentNode.setProperty(JCR_MIMETYPE, fileMimeType);
    Calendar lastModified = Calendar.getInstance();
    contentNode.setProperty(JCR_LAST_MODIFIED, lastModified);
  }

  /**
   * Returns the mime-type of the jcr:content node stored in the fileNode.
   *
   * @param fileNode the file node
   * @return the mime-type of the jcr:content node stored in the fileNode.
   * @throws RepositoryException on error
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
   * @param fileNode the file node
   * @return the size of the content.
   * @throws RepositoryException on error
   */
  public long getContentSize(Node fileNode) throws RepositoryException {
    if (fileNode.hasNode(JCR_CONTENT)) {
      Node contentNode = fileNode.getNode(JCR_CONTENT);
      return getSize(contentNode);
    }
    return 0L;
  }

  public void setContent(Node fileNode, File file, String mimeType) throws RepositoryException {
    try(InputStream in = new FileInputStream(file)) {
      setContent(fileNode, in, mimeType);
    } catch (IOException ex) {
      throw new RepositoryException(ex);
    }
  }

  /**
   * Add binary content to the specified node.
   *
   * @param fileNode the node.
   * @param content the binary content.
   * @param mimeType the mime type of the content.
   * @throws RepositoryException on error
   */
  public void setContent(Node fileNode, byte[] content, String mimeType) throws RepositoryException {
    try(ByteArrayInputStream in = new ByteArrayInputStream(content)) {
      setContent(fileNode, in, mimeType);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  public byte[] getContent(Node fileNode) throws RepositoryException {
    Node contentNode;
    if (fileNode.hasNode(JCR_CONTENT)) {
      contentNode = fileNode.getNode(JCR_CONTENT);
      try(InputStream in = contentNode.getProperty(JCR_DATA).getBinary().getStream();
          ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        IOUtils.copy(in, out);
        return out.toByteArray();
      } catch (IOException ioex) {
        throw new RepositoryException(ioex);
      }
    }
    return ArrayUtil.emptyByteArray();
  }

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

  public void getContent(Node fileNode, OutputStream out) throws RepositoryException, IOException {
    if (fileNode.hasNode(JCR_CONTENT)) {
      Node contentNode = fileNode.getNode(JCR_CONTENT);
      Binary content = contentNode.getProperty(JCR_DATA).getBinary();
      try(InputStream in = content.getStream()) {
        IOUtils.copy(in, out);
      } finally {
        content.dispose();
      }
    }

  }

  private long getSize(Node contentNode) throws RepositoryException {
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
   * @throws RepositoryException on error
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
   * @throws RepositoryException on error
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
