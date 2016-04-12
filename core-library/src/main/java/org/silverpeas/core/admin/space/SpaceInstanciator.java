/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.space;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.space.model.SpaceComponent;
import org.silverpeas.core.admin.space.model.SpaceComponentParameter;
import org.silverpeas.core.admin.space.model.SpaceTemplate;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An instanciator of Silverpeas workspaces. A Silverpeas workspace is an area providing specified
 * Silverpeas components and within which some collaborative works can be performed by well
 * authorized users. The Silverpeas collaborative portal is made up of one or more workspaces. By
 * default, each user has its own workspace from which it access its data and collaborative and
 * shared resources.
 */
public class SpaceInstanciator {

  protected static SettingBundle configuration =
      ResourceLocator.getSettingBundle("org.silverpeas.admin.admin");
  private Map<String, SpaceTemplate> spaceTemplates = new HashMap<String, SpaceTemplate>();
  private final Map<String, WAComponent> allComponentsModels;

  /**
   * For tests purpose only
   * @param allComponentsModels
   * @param xmlPackage the path to where space descriptors are stored.
   */
  SpaceInstanciator(Map<String, WAComponent> allComponentsModels, String xmlPackage) {
    this.allComponentsModels = allComponentsModels;
    File file = new File(xmlPackage);
    String[] list = file.list();
    if (list != null) {
      try {
        JAXBContext context = JAXBContext.newInstance("org.silverpeas.core.admin.space.model");
        XMLInputFactory factory = XMLInputFactory.newFactory();
        Unmarshaller unmarshaller = context.createUnmarshaller();
        for (String fileName : list) {
          if (fileName.toLowerCase().endsWith(".xml")) {
            String spaceName = fileName.substring(0, fileName.length() - 4);
            String fullPath = xmlPackage + File.separator + fileName;
            SpaceTemplate template = (unmarshaller.unmarshal(factory.createXMLStreamReader(
                new FileInputStream(fullPath)), SpaceTemplate.class)).getValue();
            spaceTemplates.put(spaceName, template);
          }
        }
      } catch (JAXBException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      } catch (XMLStreamException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      } catch (FileNotFoundException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
  }

  /**
   * Constructs a new SpaceInstanciator instance with the specified component models.
   * @param allComponentsModels a map of component models each of them identified by their name.
   */
  public SpaceInstanciator(Map<String, WAComponent> allComponentsModels) {
    this(allComponentsModels, configuration.getString("xmlSpaceTemplate").trim());
  }

  /**
   * Gets all of the templates on workspace. A template is provided by an XML file that defines the
   * Silverpeas components a workspace can contain and for each of them the user profiles.
   * @return a map between the workspace template name and its definition. If no templates are
   * found, then an empty Map instance is returned.
   */
  public Map<String, SpaceTemplate> getAllSpaceTemplates() {
    return Collections.unmodifiableMap(spaceTemplates);
  }

  /**
   * Gets an instance of a workspace defined by the specified template, identified by its name.
   * @param templateName the name of the template from which the workspace has to be instanciated.
   * @return a workspace instance.
   */
  public SpaceInst getSpaceToInstanciate(String templateName) {
    SpaceTemplate st = spaceTemplates.get(templateName);
    if (st == null) {
      return null;
    }
    return makeSpaceInst(st);
  }

  /**
   * Method declaration
   * @param st
   * @return
   * @see
   */
  public SpaceInst makeSpaceInst(SpaceTemplate st) {
    SpaceInst space = new SpaceInst();
    space.setName(st.getDefaultName());
    space.setDescription(st.getDescription());
    for (SpaceComponent component : st.getComponents()) {
      WAComponent wacomponent = allComponentsModels.get(component.getType());
      if (wacomponent != null) {
        ComponentInst ci = new ComponentInst();
        ci.setOrderNum(space.getNumComponentInst());
        ci.setName(component.getType());
        ci.setLabel(component.getLabel());
        ci.setDescription(component.getDescription());
        ci.setParameters(wacomponent.getParameters());
        for (SpaceComponentParameter param : component.getParameters()) {
          Parameter parameter = ci.getParameter(param.getName());
          if (parameter != null) {
            parameter.setValue(param.getValue());
          }
        }
        space.addComponentInst(ci);
      }
    }
    return space;
  }
}
