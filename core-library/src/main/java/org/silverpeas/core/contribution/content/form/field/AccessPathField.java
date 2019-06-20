/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.form.field;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Collection;
import java.util.List;

/**
 * An AccessPathField stores the current access path of the form
 * <p>
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
    StringBuilder currentAccessPath = new StringBuilder();

    // Space > SubSpace
    if (componentId != null && !"useless".equals(componentId)) {
      List<SpaceInstLight> listSpaces =  OrganizationControllerProvider
          .getOrganisationController().getPathToComponent(componentId);
      for (SpaceInstLight space : listSpaces) {
        currentAccessPath.append(space.getName()).append(" > ");
      }

      // Service
      currentAccessPath.append(OrganizationControllerProvider.getOrganisationController()
          .getComponentInstLight(componentId).getLabel());

      // Theme > SubTheme
      StringBuilder pathString = new StringBuilder();
      if (nodeId != null) {
        NodeService nodeService = null;
        try {
          nodeService = NodeService.get();
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
        }

        if (nodeService != null) {
          NodePK nodePk = new NodePK(nodeId, componentId);
          Collection<NodeDetail> listPath = nodeService.getPath(nodePk);
          if (listPath != null) {
            String nodeName;
            for (NodeDetail nodeInPath : listPath) {
              if (!nodeInPath.getNodePK().getId().equals("0")) {
                if (contentLanguage != null) {
                  nodeName = nodeInPath.getName(contentLanguage);
                } else {
                  nodeName = nodeInPath.getName();
                }
                pathString.append(WebEncodeHelper.javaStringToHtmlString(nodeName)).append(" > ");
              }
            }

            if (pathString.length() > 0) {
              // remove the last ' > ' characters
              pathString.delete(pathString.length() - 3, pathString.length());
            }
          }
        }
      }

      if (pathString.length() > 0) {
        currentAccessPath.append(" > ").append(pathString);
      }
    }

    return currentAccessPath.toString();
  }
}
