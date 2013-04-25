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

package com.silverpeas.form.fieldType;

import java.util.Collection;
import java.util.List;

import org.silverpeas.core.admin.OrganisationControllerFactory;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * An AccessPathField stores the current access path of the form
 * <p/>
 * @see Field
 * @see FieldDisplayer
 */
public class AccessPathField extends TextField {

  private static final long serialVersionUID = 9112703938534783673L;
  /**
   * The text field type name.
   */
  static public final String TYPE = "accessPath";
  private String value = "";

  /**
   * Returns the type name.
   */
  public String getTypeName() {
    return TYPE;
  }

  /**
   * The no parameters constructor
   */
  public AccessPathField() {
  }

  /**
   * Returns the string value of this field.
   */
  public String getStringValue() {
    return value;
  }

  /**
   * Set the string value of this field.
   */
  public void setStringValue(String value) {
    this.value = value;
  }

  /**
   * Returns true if the value is read only.
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns the access path of the object.
   */
  public String getAccessPath(String componentId, String nodeId,
      String contentLanguage) {
    String currentAccessPath = "";

    // Space > SubSpace
    if (componentId != null && !"useless".equals(componentId)) {
      List<SpaceInst> listSpaces =  OrganisationControllerFactory
          .getOrganisationController().getSpacePathToComponent(componentId);
      for (SpaceInst space : listSpaces) {
        currentAccessPath += space.getName() + " > ";
      }

      // Service
      currentAccessPath +=  OrganisationControllerFactory.getOrganisationController()
          .getComponentInstLight(componentId).getLabel();

      // Theme > SubTheme
      String pathString = "";
      if (nodeId != null) {
        NodeBm nodeBm = null;
        try {
          nodeBm = EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class);
        } catch (Exception e) {
          SilverTrace.error("form", "AccessPathFieldDisplayer.display",
              "form.EX_CANT_CREATE_NODEBM_HOME");
        }

        if (nodeBm != null) {
          NodePK nodePk = new NodePK(nodeId, componentId);
          Collection<NodeDetail> listPath = nodeBm.getPath(nodePk);
          if (listPath != null) {
            String nodeName;
            for (NodeDetail nodeInPath : listPath) {
              if (!nodeInPath.getNodePK().getId().equals("0")) {
                if (contentLanguage != null) {
                  nodeName = nodeInPath.getName(contentLanguage);
                } else {
                  nodeName = nodeInPath.getName();
                }
                pathString += EncodeHelper.javaStringToHtmlString(nodeName) + " > ";
              }
            }

            if (StringUtil.isDefined(pathString)) {
              pathString = pathString.substring(0, pathString.length() - 3); // remove
            } // last
            // '>'
          }
        }
      }

      if (pathString.length() > 0) {
        currentAccessPath += " > " + pathString;
      }
    }

    return currentAccessPath;
  }
}
