/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.form.fieldType;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FormException;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * An ExplorerField stores a node reference.
 * @see Field
 * @see FieldDisplayer
 */
public class ExplorerField implements Field {

  private static final long serialVersionUID = -4982574221213514901L;

  /**
   * The text field type name.
   */
  static public final String TYPE = "explorer";

  /**
   * Returns the type name.
   */
  public String getTypeName() {
    return TYPE;
  }

  /**
   * The no parameters constructor
   */
  public ExplorerField() {
  }

  /**
   * Returns the node id referenced by this field (ex : kmelia1-138)
   */
  public String getNodePK() {
    return pk;
  }

  /**
   * Set the node id referenced by this field.
   */
  public void setNodePK(String pk) {
    SilverTrace.info("form", "ExplorerField.setNodePK",
        "root.MSG_GEN_ENTER_METHOD", "pk = " + pk);
    this.pk = pk;
  }

  /**
   * Returns true if the value is read only.
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns the string value of this field : aka the node path.
   */
  public String getValue() {
    return getValue(I18NHelper.defaultLanguage);
  }

  /**
   * Returns the local value of this field. There is no local format for a user field, so the
   * language parameter is unused.
   */
  public String getValue(String language) {
    ForeignPK pk = (ForeignPK) getObjectValue();
    if (pk == null) {
      return "";
    }
    SilverTrace.info("form", "ExplorerField.getValue", "root.MSG_GEN_PARAM_VALUE",
        "pk = " + pk.toString());

    return getPath(pk.getInstanceId(), pk.getId(), language);
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   */
  public void setValue(String value) throws FormException {
  }

  /**
   * Does nothing since a user reference can't be computed from a user name.
   */
  public void setValue(String value, String language) throws FormException {
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  public boolean acceptValue(String value) {
    return false;
  }

  /**
   * Always returns false since a user reference can't be computed from a user name.
   */
  public boolean acceptValue(String value, String language) {
    return false;
  }

  /**
   * Returns the User referenced by this field.
   */
  public Object getObjectValue() {
    if (!StringUtil.isDefined(getNodePK())) {
      return null;
    }
    String[] ids = getNodePK().split("-");
    return new ForeignPK(ids[1], ids[0]);
  }

  /**
   * Set node referenced by this field.
   */
  public void setObjectValue(Object value) throws FormException {
    if (value instanceof ForeignPK) {
      ForeignPK pk = (ForeignPK) value;
      setNodePK(pk.getInstanceId() + "-" + pk.getId());
    } else if (value == null) {
      setNodePK(null);
    } else {
      throw new FormException("ExplorerField.setObjectValue",
          "form.EXP_NOT_A_NODE");
    }
  }

  /**
   * Returns true if the value is a String and this field isn't read only.
   */
  public boolean acceptObjectValue(Object value) {
    if (value instanceof UserDetail) {
      return !isReadOnly();
    } else {
      return false;
    }
  }

  /**
   * Returns this field value as a normalized String : a user id
   */
  public String getStringValue() {
    return getNodePK();
  }

  /**
   * Set this field value from a normalized String : a user id
   */
  public void setStringValue(String value) {
    SilverTrace.info("form", "ExplorerField.setStringValue",
        "root.MSG_GEN_ENTER_METHOD", "value = " + value);
    setNodePK(value);
  }

  /**
   * Returns true if this field isn't read only.
   */
  public boolean acceptStringValue(String value) {
    return !isReadOnly();
  }

  /**
   * Returns true if this field is not set.
   */
  public boolean isNull() {
    return (getNodePK() == null);
  }

  /**
   * Set to null this field.
   * @throw FormException when the field is mandatory.
   * @throw FormException when the field is read only.
   */
  public void setNull() throws FormException {
    setNodePK(null);
  }

  /**
   * Tests equality beetwen this field and the specified field.
   */
  public boolean equals(Object o) {
    String s = getNodePK();

    if (o instanceof ExplorerField) {
      String t = ((ExplorerField) o).getNodePK();
      return ((s == null && t == null) || s.equals(t));
    } else {
      return false;
    }
  }

  /**
   * Compares this field with the specified field.
   */
  public int compareTo(Object o) {
    String s = getValue();
    if (s == null) {
      s = "";
    }
    if (o instanceof ExplorerField) {
      String t = ((ExplorerField) o).getValue();
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
   * The main access to the users set.
   */
  private static OrganizationController organizationController = new OrganizationController();

  /**
   * Returns the access path of the object.
   */
  private String getPath(String componentId, String nodeId, String language) {
    String path = "";

    // Space > SubSpace
    if (componentId != null && !"useless".equals(componentId)) {
      List<SpaceInst> listSpaces = organizationController.getSpacePathToComponent(componentId);
      for (SpaceInst space : listSpaces) {
        path += space.getName(language) + " > ";
      }

      // Service
      path += organizationController.getComponentInstLight(componentId).getLabel(language);

      // Theme > SubTheme
      String pathString = "";
      if (nodeId != null) {
        NodeBm nodeBm = null;
        try {
          NodeBmHome nodeBmHome =
              EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
          nodeBm = nodeBmHome.create();
        } catch (Exception e) {
          SilverTrace.error("form", "ExplorerFieldDisplayer.display",
              "form.EX_CANT_CREATE_NODEBM_HOME", e);
        }

        if (nodeBm != null) {
          NodePK nodePk = new NodePK(nodeId, componentId);
          Collection<NodeDetail> listPath = null;
          try {
            listPath = nodeBm.getPath(nodePk);
          } catch (RemoteException e) {
            SilverTrace.error("form", "ExplorerFieldDisplayer.display",
                "form.EX_CANT_GET_PATH_NODE", nodeId);
          }

          if (listPath != null) {
            Collections.reverse((List<NodeDetail>) listPath);
            String nodeName;
            for (NodeDetail nodeInPath : listPath) {
              if (!nodeInPath.getNodePK().getId().equals("0")) {
                if (language != null) {
                  nodeName = nodeInPath.getName(language);
                } else {
                  nodeName = nodeInPath.getName();
                }
                pathString += nodeName + " > ";
              }
            }

            if (StringUtil.isDefined(pathString)) {
              pathString = pathString.substring(0, pathString.length() - 3); // remove last '>'
            }
          }
        }
      }

      if (pathString.length() > 0) {
        path += " > " + pathString;
      }
    }

    return path;
  }

}
