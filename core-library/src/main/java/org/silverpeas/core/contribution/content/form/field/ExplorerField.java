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
package org.silverpeas.core.contribution.content.form.field;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.form.AbstractField;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * An ExplorerField stores a node reference.
 * @see Field
 * @see FieldDisplayer
 */
public class ExplorerField extends AbstractField {

  private static final long serialVersionUID = -4982574221213514901L;
  /**
   * The text field type name.
   */
  public static final String TYPE = "explorer";

  @Override
  public String getTypeName() {
    return TYPE;
  }

  /**
   * Gets the node id referenced by this field (ex: kmelia1-138)
   * @return a node identifier.
   */
  public String getNodePK() {
    return pk;
  }

  /**
   * Sets the node id to be referenced by this field.
   * @param pk a node identifier.
   */
  public void setNodePK(String pk) {
    this.pk = pk;
  }

  /**
   * Is this field is read only?
   * @return true if the value is read only.
   */
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public String getValue() {
    return getValue(I18NHelper.DEFAULT_LANGUAGE);
  }

  @Override
  public String getValue(String language) {
    ResourceReference ref = (ResourceReference) getObjectValue();
    if (ref == null) {
      return "";
    }


    return getPath(ref.getInstanceId(), ref.getId(), language);
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   */
  @Override
  public void setValue(String value) throws FormException {
    // nothing to do
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   */
  @Override
  public void setValue(String value, String language) throws FormException {
    // nothing to do
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  @Override
  public boolean acceptValue(String value) {
    return false;
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  @Override
  public boolean acceptValue(String value, String language) {
    return false;
  }

  /**
   * Returns the User referenced by this field.
   */
  @Override
  public Object getObjectValue() {
    if (!StringUtil.isDefined(getNodePK())) {
      return null;
    }
    String[] ids = getNodePK().split("-");
    return new ResourceReference(ids[1], ids[0]);
  }

  /**
   * Set node referenced by this field.
   */
  @Override
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof ResourceReference) {
      ResourceReference ref = (ResourceReference) value;
      setNodePK(ref.getInstanceId() + "-" + ref.getId());
    } else if (value == null) {
      setNodePK(null);
    } else {
      throw new FormException("ExplorerField.setObjectValue",
          "form.EXP_NOT_A_NODE");
    }
  }

  @Override
  public boolean acceptObjectValue(Object value) {
    return value instanceof UserDetail && !isReadOnly();
  }

  /**
   * Returns this field value as a normalized String: a user id
   */
  @Override
  public String getStringValue() {
    return getNodePK();
  }

  /**
   * Set this field value from a normalized String: a user id
   */
  @Override
  public void setStringValue(String value) {
    setNodePK(value);
  }

  /**
   * Returns true if this field isn't read only.
   */
  @Override
  public boolean acceptStringValue(String value) {
    return !isReadOnly();
  }

  /**
   * Returns true if this field is not set.
   */
  @Override
  public boolean isNull() {
    return (getNodePK() == null);
  }

  @Override
  public void setNull() throws FormException {
    setNodePK(null);
  }

  @Override
  public boolean equals(Object o) {
    String s = getNodePK();

    if (o instanceof ExplorerField) {
      String t = ((ExplorerField) o).getNodePK();
      return (s == null || s.equals(t));
    } else {
      return false;
    }
  }

  @Override
  public int compareTo(@Nonnull Field o) {
    String s = getValue();
    if (s == null) {
      s = "";
    }
    if (o instanceof ExplorerField) {
      String t = o.getValue();
      if (t == null) {
        t = "";
      }

      if (s.equals(t)) {
        s = getNodePK();
        if (s == null) {
          s = "";
        }
        t = ((ExplorerField) o).getNodePK();
        if (t == null) {
          t = "";
        }
      }
      return s.compareTo(t);
    } else {
      return -1;
    }
  }

  public int hashCode() {
    String s = getNodePK();
    return ("" + s).hashCode();
  }

  /**
   * The referenced node.
   */
  private String pk = null;

  /**
   * Returns the access path of the object.
   */
  private String getPath(String componentId, String nodeId, String language) {
    StringBuilder path = new StringBuilder();

    if (componentId != null && !"useless".equals(componentId)) {
      // Space > SubSpace
      setSpacePath(componentId, language, path);

      // Service
      path.append(OrganizationControllerProvider.getOrganisationController()
          .getComponentInstLight(componentId)
          .getLabel(language));

      // Theme > SubTheme
      setNodePath(componentId, nodeId, language, path);
    }

    return path.toString();
  }

  private static void setNodePath(final String componentId, final String nodeId,
      final String language, final StringBuilder path) {
    StringBuilder pathString = new StringBuilder();
    if (nodeId == null) {
      return;
    }

    NodeService nodeService = NodeService.get();
    NodePK nodePk = new NodePK(nodeId, componentId);
    NodePath listPath = nodeService.getPath(nodePk);
    if (listPath != null) {
      Collections.reverse(listPath);
      String nodeName;
      for (NodeDetail nodeInPath : listPath) {
        if (!nodeInPath.getNodePK().getId().equals("0")) {
          if (language != null) {
            nodeName = nodeInPath.getName(language);
          } else {
            nodeName = nodeInPath.getName();
          }
          pathString.append(nodeName).append(" > ");
        }
      }

      if (pathString.length() > 0) {
        // remove last ' > '
        pathString.delete(pathString.length() - 3, pathString.length());
      }
    }

    if (pathString.length() > 0) {
      path.append(" > ").append(pathString);
    }
  }

  private static void setSpacePath(final String componentId, final String language,
      final StringBuilder path) {
    List<SpaceInstLight> listSpaces = OrganizationControllerProvider.getOrganisationController()
        .getPathToComponent(componentId);
    for (SpaceInstLight space : listSpaces) {
      path.append(space.getName(language)).append(" > ");
    }
  }
}
