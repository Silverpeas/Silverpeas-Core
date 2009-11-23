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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.beans.admin.spaceTemplates;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameters;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;
import com.stratelia.webactiv.beans.admin.instance.control.WADOMUtil;

/**
 * Class declaration
 * @author
 */
public class SpaceTemplate {
  protected String filename;

  protected String templateName;
  protected String defaultName;
  protected String description;
  protected boolean readOnly;

  protected SpaceTemplateProfile[] spaceTemplateProfiles;
  protected ArrayList components = new ArrayList();

  /**
   * Constructor declaration
   * @param filename
   * @param allComponentsModels
   * @see
   */
  public SpaceTemplate(String filename, Hashtable allComponentsModels) {
    SpaceTemplateProfile stp;
    Hashtable templateProfiles = new Hashtable();

    this.filename = filename;
    try {
      SilverTrace.info("admin", "SpaceTemplate.SpaceTemplate",
          "root.MSG_GEN_PARAM_VALUE", "Reading File : " + filename);
      Document document = WADOMUtil.readDocument(filename);
      Node ntemplateName = WADOMUtil.findNode(document, "templateName");
      Node ndefaultName = WADOMUtil.findNode(document, "defaultName");
      Node ndescription = WADOMUtil.findNode(document, "description");
      Node nreadOnly = WADOMUtil.findNode(document, "readOnly");

      // Retrieves the general space datas
      this.templateName = ntemplateName.getFirstChild().getNodeValue();
      this.defaultName = ndefaultName.getFirstChild().getNodeValue();
      this.description = ndescription.getFirstChild().getNodeValue();
      this.readOnly = stringToBoolean(nreadOnly.getFirstChild().getNodeValue());

      // Retrieves the profiles
      Node nProfilesList = WADOMUtil.findNode(document, "Profiles");
      if (nProfilesList != null) {
        NodeList listProfiles = nProfilesList.getChildNodes();

        for (int nJ = 0; nJ < listProfiles.getLength(); nJ++) {
          if (listProfiles.item(nJ).getNodeType() == Node.ELEMENT_NODE) {
            String sProfileName = "";
            String sProfileLabel = "";

            sProfileLabel = listProfiles.item(nJ).getFirstChild()
                .getNodeValue();
            if (listProfiles.item(nJ).getAttributes() != null
                && listProfiles.item(nJ).getAttributes().getNamedItem("name") != null
                && listProfiles.item(nJ).getAttributes().getNamedItem("name")
                .getNodeValue() != null)
              sProfileName = listProfiles.item(nJ).getAttributes()
                  .getNamedItem("name").getNodeValue();
            if (sProfileName != null && sProfileName.length() > 0) {
              stp = new SpaceTemplateProfile();
              stp.setName(sProfileName);
              stp.setLabel(sProfileLabel);
              templateProfiles.put(sProfileName, stp);
            }
          }
        }
      }

      // Components
      Node nComponentsList = WADOMUtil.findNode(document, "Components");

      if (nComponentsList != null) {
        NodeList listComponents = nComponentsList.getChildNodes();

        for (int nI = 0; nI < listComponents.getLength(); nI++) {
          if (listComponents.item(nI).getNodeType() == Node.ELEMENT_NODE) {
            String sComponentName = "";
            String sComponentLabel = "";
            String sComponentDescription = "";
            ComponentInst ci;

            Node nComponentName = WADOMUtil.findNode(listComponents.item(nI),
                "ComponentName");
            Node nComponentLabel = WADOMUtil.findNode(listComponents.item(nI),
                "ComponentLabel");
            Node nComponentDescription = WADOMUtil.findNode(listComponents
                .item(nI), "ComponentDescription");
            Node nComponentParameters = WADOMUtil.findNode(listComponents
                .item(nI), "ComponentParameters");
            Node nComponentMatchingProfiles = WADOMUtil.findNode(listComponents
                .item(nI), "ComponentMatchingProfiles");

            if (nComponentName != null
                && nComponentName.getFirstChild() != null) {
              sComponentName = nComponentName.getFirstChild().getNodeValue();

            }
            if (nComponentLabel != null
                && nComponentLabel.getFirstChild() != null) {
              sComponentLabel = nComponentLabel.getFirstChild().getNodeValue();

            }
            if (nComponentDescription != null
                && nComponentDescription.getFirstChild() != null) {
              sComponentDescription = nComponentDescription.getFirstChild()
                  .getNodeValue();

            }
            SilverTrace.info("admin", "SpaceTemplate.SpaceTemplate",
                "root.MSG_GEN_PARAM_VALUE", "ComponentName: " + sComponentName
                + "   label: " + sComponentLabel + "   for space: "
                + this.templateName);

            WAComponent wac = (WAComponent) allComponentsModels
                .get(sComponentName);

            if (wac != null) {
              ci = new ComponentInst();
              ci.setName(sComponentName);
              ci.setLabel(sComponentLabel);
              ci.setDescription(sComponentDescription);
              ci.setParameters(wac.getParameters());

              // Retrieves the parameters' values
              if (nComponentParameters != null) {
                SPParameters parameters = (SPParameters) wac.getSPParameters()
                    .clone();

                NodeList listParameters = nComponentParameters.getChildNodes();

                for (int nJ = 0; nJ < listParameters.getLength(); nJ++) {
                  if (listParameters.item(nJ).getNodeType() == Node.ELEMENT_NODE) {
                    String sComponentParameterName = "";
                    String sComponentParameterValue = "";

                    sComponentParameterValue = listParameters.item(nJ)
                        .getFirstChild().getNodeValue();
                    if (listParameters.item(nJ).getAttributes() != null
                        && listParameters.item(nJ).getAttributes()
                        .getNamedItem("name") != null
                        && listParameters.item(nJ).getAttributes()
                        .getNamedItem("name").getNodeValue() != null) {
                      sComponentParameterName = listParameters.item(nJ)
                          .getAttributes().getNamedItem("name").getNodeValue();
                      parameters.setParameterValue(sComponentParameterName,
                          sComponentParameterValue);
                    }
                  }
                }
                ci.setSPParameters(parameters);
              }
              // Retrieves the mapped profiles' values
              if (nComponentMatchingProfiles != null) {
                NodeList listMatchingProfiles = nComponentMatchingProfiles
                    .getChildNodes();

                for (int nJ = 0; nJ < listMatchingProfiles.getLength(); nJ++) {
                  if (listMatchingProfiles.item(nJ).getNodeType() == Node.ELEMENT_NODE) {
                    String sTemplateName = "";
                    String sComponentProfileName = "";

                    sComponentProfileName = listMatchingProfiles.item(nJ)
                        .getFirstChild().getNodeValue();
                    if (listMatchingProfiles.item(nJ).getAttributes() != null
                        && listMatchingProfiles.item(nJ).getAttributes()
                        .getNamedItem("name") != null
                        && listMatchingProfiles.item(nJ).getAttributes()
                        .getNamedItem("name").getNodeValue() != null)
                      sTemplateName = listMatchingProfiles.item(nJ)
                          .getAttributes().getNamedItem("name").getNodeValue();
                    SilverTrace.info("admin", "SpaceTemplate.SpaceTemplate",
                        "root.MSG_GEN_PARAM_VALUE", "Compo label: -"
                        + sComponentLabel + "- Compo PRofile : -"
                        + sComponentProfileName + "- TemplateProfile : -"
                        + sTemplateName);
                    if (sTemplateName != null && sTemplateName.length() > 0
                        && sComponentProfileName != null
                        && sComponentProfileName.length() > 0) {
                      stp = (SpaceTemplateProfile) templateProfiles
                          .get(sTemplateName);
                      if (stp != null) {
                        SilverTrace.info("admin",
                            "SpaceTemplate.SpaceTemplate",
                            "root.MSG_GEN_PARAM_VALUE", "ADD");
                        stp.addMappedComponentProfile(sComponentLabel,
                            sComponentProfileName);
                      }
                    }
                  }
                }
              }

              // Finaly add the object
              components.add(ci);
            }
          }
        }
      }
      spaceTemplateProfiles = (SpaceTemplateProfile[]) templateProfiles
          .values().toArray(new SpaceTemplateProfile[0]);
    } catch (Exception e) {
      SilverTrace.error("admin", "SpaceTemplate.SpaceTemplate",
          "admin.EX_ERR_SpaceTemplate", "Reading File : " + filename, e);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getTemplateName() {
    return templateName;
  }

  /**
   * Method declaration
   * @param s
   * @see
   */
  public void setTemplateName(String s) {
    templateName = s;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getDefaultName() {
    return defaultName;
  }

  /**
   * Method declaration
   * @param s
   * @see
   */
  public void setDefaultName(String s) {
    defaultName = s;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getDescription() {
    return description;
  }

  /**
   * Method declaration
   * @param s
   * @see
   */
  public void setDescription(String s) {
    description = s;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Method declaration
   * @param b
   * @see
   */
  public void setReadOnly(boolean b) {
    readOnly = b;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public ArrayList getComponents() {
    return components;
  }

  /**
   * Method declaration
   * @param ar
   * @see
   */
  public void setComponents(ArrayList ar) {
    components = ar;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String toString() {
    return templateName + "|" + description + "|"
        + ((readOnly) ? " Read Only " : " Read/Write ");
  }

  /**
   * Method declaration
   * @param templateName
   * @return
   * @see
   */
  public SpaceInst makeSpaceInst() {
    SpaceInst valret = new SpaceInst();
    Iterator it = components.iterator();

    valret.setName(defaultName);
    valret.setDescription(description);
    while (it.hasNext()) {
      valret.addComponentInst((ComponentInst) ((ComponentInst) it.next())
          .clone());
    }
    SilverTrace.info("admin", "SpaceTemplate.makeSpaceInst",
        "root.MSG_GEN_PARAM_VALUE", "defaultSpaceName : " + defaultName
        + " NbCompo: " + valret.getNumComponentInst());
    return valret;
  }

  public SpaceTemplateProfile[] getTemplateProfiles() {
    return spaceTemplateProfiles;
  }

  /**
   * Method declaration
   * @param b
   * @return
   * @see
   */
  protected String booleanToString(boolean b) {
    if (b) {
      return "yes";
    } else {
      return "no";
    }
  }

  /**
   * Method declaration
   * @param s
   * @return
   * @see
   */
  protected boolean stringToBoolean(String s) {
    if ((s != null)
        && ((s.equalsIgnoreCase("no")) || (s.equalsIgnoreCase("n"))
        || (s.equalsIgnoreCase("non")) || (s.equalsIgnoreCase("0")) || (s
        .equalsIgnoreCase("false")))) {
      return false;
    } else {
      return true;
    }
  }

}
