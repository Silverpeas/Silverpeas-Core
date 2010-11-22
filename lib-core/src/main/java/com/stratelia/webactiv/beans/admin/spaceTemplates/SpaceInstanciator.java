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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.webactiv.beans.admin.spaceTemplates;

import java.io.File;
import java.util.MissingResourceException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;
import com.stratelia.webactiv.util.ResourceLocator;
import java.util.HashMap;
import java.util.Map;

/**
 * An instanciator of Silverpeas workspaces.
 * A Silverpeas workspace is an area providing specified Silverpeas components and within which some
 * collaborative works can be performed by well authorized users.
 * The Silverpeas collaborative portal is made up of one or more workspaces. By default, each user
 * has its own workspace from which it access its data and collaborative and shared resources.
 */
public class SpaceInstanciator extends Object {

  protected static String xmlPackage = "";
  private Map<String, SpaceTemplate> spaceTemplates = new HashMap<String, SpaceTemplate>();

  // Init Function
  static {
    try {
      xmlPackage = (new ResourceLocator(
          "com.stratelia.webactiv.beans.admin.admin", "")).getString("xmlSpaceTemplate").trim();
    } catch (MissingResourceException mre) {
      SilverTrace.fatal("admin", "Instanciateur.static",
          "admin.MSG_INSTANCIATEUR_RESOURCES_NOT_FOUND", mre);
    }
  }

  /**
   * Constructs a new SpaceInstanciator instance with the specified component models.
   * @param allComponentsModels a map of component models each of them identified by their name.
   */
  public SpaceInstanciator(Map<String, WAComponent> allComponentsModels) {
    File file = new File(xmlPackage);
    String[] list = file.list();
    if (list != null) {
      for (String fileName : list) {
        if (fileName.toLowerCase().endsWith(".xml")) {
          String spaceName = fileName.substring(0, fileName.length() - 4);
          String fullPath = xmlPackage + File.separator + fileName;
          SilverTrace.info("admin", "SpaceInstanciateur.SpaceInstanciateur",
              "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "space name: '" + spaceName
              + "', full path: '" + fullPath + "'");
          spaceTemplates.put(spaceName, new SpaceTemplate(fullPath, allComponentsModels));
        }
      }
    }
  }

  /**
   * Gets all of the templates on workspace.
   * A template is provided by an XML file that defines the Silverpeas components a workspace
   * can contain and for each of them the user profiles.
   * @return a map between the workspace template name and its definition. If no templates are
   * found, then an empty Map instance is returned.
   */
  public Map<String, SpaceTemplate> getAllSpaceTemplates() {
    return spaceTemplates;
  }

  /**
   * Gets the user profiles of the specified workspace, identified by its name.
   * @param templateName the name of the space templace.
   * @return an array of the profiles in the specified template.
   */
  public SpaceTemplateProfile[] getTemplateProfiles(String templateName) {
    SpaceTemplate st = spaceTemplates.get(templateName);

    if (st == null) {
      SilverTrace.info("admin",
          "SpaceInstanciateur.getTemplateMappedComponentProfile",
          "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "template Name : '"
          + templateName + "' NOT FOUND !!!!!!!!!");
      return new SpaceTemplateProfile[0];
    } else {
      SilverTrace.info("admin",
          "SpaceInstanciateur.getTemplateMappedComponentProfile",
          "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "template Name : '"
          + templateName);
      return st.getTemplateProfiles();
    }
  }

  /**
   * Gets an instance of a workspace defined by the specified template, identified by its name.
   * @param templateName the name of the template from which the workspace has to be instanciated.
   * @return a workspace instance.
   */
  public SpaceInst getSpaceToInstanciate(String templateName) {
    SpaceTemplate st = spaceTemplates.get(templateName);

    if (st == null) {
      SilverTrace.info("admin", "SpaceInstanciateur.getSpaceToInstanciate",
          "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "template Name : '"
          + templateName + "' NOT FOUND !!!!!!!!!");
      return null;
    } else {
      SilverTrace.info("admin", "SpaceInstanciateur.getSpaceToInstanciate",
          "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "template Name : '"
          + templateName);
      return st.makeSpaceInst();
    }
  }
} // class
