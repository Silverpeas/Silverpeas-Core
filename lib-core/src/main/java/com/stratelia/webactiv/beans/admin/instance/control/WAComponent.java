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
package com.stratelia.webactiv.beans.admin.instance.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class WAComponent {
  /**
   * ResourceLocator object to retrieve messages in a properties file
   */
  private static ResourceLocator settings = new ResourceLocator(
      "com.stratelia.webactiv.beans.admin.instance.control.castorSettings",
      "fr");

  protected String filename;
  protected String name;
  protected String requestRouter;
  protected String label;
  protected String description;
  protected String suite;
  protected boolean visible;
  protected boolean portlet;
  protected String instanceClassName;
  protected String[] tableList = new String[0];
  protected List<SPProfile> profiles = new ArrayList<SPProfile>();
  protected SPParameters parameters = null;

  public WAComponent(String filename) {
    this.filename = filename;

    try {
      SilverTrace.info("admin", "WAComponent.WAComponent",
          "root.MSG_GEN_PARAM_VALUE", "Reading File : " + filename);
      Document document = WADOMUtil.readDocument(filename);
      Node nname = WADOMUtil.findNode(document, "name");
      Node nrequestRouter = WADOMUtil.findNode(document, "requestRouter");
      Node nlabel = WADOMUtil.findNode(document, "label");
      Node ndescription = WADOMUtil.findNode(document, "description");
      Node nsuite = WADOMUtil.findNode(document, "suite");
      Node nvisible = WADOMUtil.findNode(document, "visible");
      Node nportlet = WADOMUtil.findNode(document, "portlet");
      Node ninstanceClassName = WADOMUtil.findNode(document,
          "instanceClassName");
      Node ntableList = WADOMUtil.findNode(document, "tableList");
      Node nprofilList = WADOMUtil.findNode(document, "Profiles");

      this.name = nname.getFirstChild().getNodeValue();
      this.label = nlabel.getFirstChild().getNodeValue();
      this.description = ndescription.getFirstChild().getNodeValue();
      this.suite = nsuite.getFirstChild().getNodeValue();
      this.visible = stringToBoolean(nvisible.getFirstChild().getNodeValue());
      this.portlet = stringToBoolean(nportlet.getFirstChild().getNodeValue());
      this.instanceClassName = ninstanceClassName.getFirstChild()
          .getNodeValue();
      if (nrequestRouter == null)
        this.requestRouter = "R" + this.name;
      else
        this.requestRouter = nrequestRouter.getFirstChild().getNodeValue();

      if ((ntableList != null) && (ntableList.hasChildNodes())) {
        NodeList list = ntableList.getChildNodes();
        int size = list.getLength();
        String[] myList = new String[size];
        int j = 0;
        for (int i = 1; i < size; i = i + 2) {
          if (list.item(i).hasChildNodes()) {
            myList[j++] = list.item(i).getFirstChild().getNodeValue();
          }
          this.tableList = myList;
        }
      }
      if ((nprofilList != null) && (nprofilList.hasChildNodes())) {
        NodeList list = nprofilList.getChildNodes();
        String strLabel = ""; // profile label as seen by user
        String strProfile; // profile name as stored in database

        int size = list.getLength();
        for (int i = 1; i < size; i = i + 2)
          if (list.item(i).hasChildNodes()) {
            if (list.item(i).getFirstChild().getNodeValue() != null)
              strLabel = list.item(i).getFirstChild().getNodeValue();

            // Get name of component used in Silverpeas (if none, get label
            // instead)
            if (list.item(i).getAttributes() != null
                && list.item(i).getAttributes().getNamedItem("name") != null)
              strProfile = list.item(i).getAttributes().getNamedItem("name")
                  .getNodeValue();
            else
              strProfile = list.item(i).getFirstChild().getNodeValue();

            profiles.add(new SPProfile(strProfile, strLabel));
          }

        this.parameters = new SPParameters(document);
      }
    } catch (Exception e) {
      SilverTrace.error("admin", "WAComponent.WAComponent",
          "admin.EX_ERR_WACOMPONENT", "Reading File : " + filename, e);
    }
  }

  public WAComponent(String strName, String strLabel, String strDescription,
      String strSuite, boolean fIsVisible, boolean fIsPortlet,
      String strInstanceClass, String[] astrTableList, List<SPProfile> listProfiles) {
    setName(strName);
    setLabel(strLabel);
    setDescription(strDescription);
    setSuite(strSuite);
    setVisible(fIsVisible);
    setPortlet(fIsPortlet);
    setInstanceClassName(strInstanceClass);
    setTableList(astrTableList);
    setSPProfiles(listProfiles);
  }

  public String getName() {
    return name;
  }

  public void setName(String s) {
    name = s;
  }

  public String getRequestRouter() {
    return requestRouter;
  }

  public void setRequestRouter(String s) {
    requestRouter = s;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String s) {
    label = s;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String s) {
    description = s;
  }

  public String getSuite() {
    return suite;
  }

  public void setSuite(String s) {
    suite = s;
  }

  public boolean isVisible() {
    return visible;
  }

  public String getVisibleAsString() {
    return booleanToString(visible);
  }

  public void setVisible(boolean b) {
    visible = b;
  }

  public void setVisible(String strB) {
    visible = stringToBoolean(strB);
  }

  public boolean isPortlet() {
    return portlet;
  }

  public String getPortletAsString() {
    return booleanToString(portlet);
  }

  public void setPortlet(boolean b) {
    portlet = b;
  }

  public void setPortlet(String strB) {
    portlet = stringToBoolean(strB);
  }

  public String getInstanceClassName() {
    return instanceClassName;
  }

  public void setInstanceClassName(String s) {
    instanceClassName = s;
  }

  public String[] getTableList() {
    return tableList;
  }

  public void setTableList(String[] s) {
    tableList = s;
  }

  public List<SPProfile> getSPProfiles() {
    return profiles;
  }

  public void setSPProfiles(List<SPProfile> listProfiles) {
    if (listProfiles != null)
      profiles = listProfiles;
  }

  public String[] getProfilList() {
    Iterator<SPProfile> iterProfile = profiles.iterator();
    List<String> listNames = new ArrayList<String>();

    while (iterProfile.hasNext())
      listNames.add(iterProfile.next().getName());

    return (String[]) listNames.toArray(new String[0]);
  }

  public String[] getProfilLabelList() {
    Iterator<SPProfile> iterProfile = profiles.iterator();
    List<String> listLabels = new ArrayList<String>();

    while (iterProfile.hasNext())
      listLabels.add(iterProfile.next().getLabel());

    return (String[]) listLabels.toArray(new String[0]);
  }

  public SPParameters getSPParameters() {
    return this.parameters;
  }

  public void setSPParameters(SPParameters spParameters) {
    parameters = spParameters;
  }

  public List<SPParameter> getParameters() {
    return this.parameters.getParameters();
  }

  public List<SPParameter> getSortedParameters() {
    return this.parameters.getSortedParameters();
  }

  public String toString() {
    String s = name + "|" + description + "|" + suite + "|"
        + ((visible) ? " Visible " : " NOT Visible ") + "|" + instanceClassName
        + "|";
    Iterator<SPProfile> iterProfile = profiles.iterator();
    int i = 0;
    if (tableList != null) {
      while ((i < tableList.length) && (tableList[i] != null))
        s = s + "," + tableList[i++];
    }
    s = s + "|";
    i = 0;

    while (iterProfile.hasNext())
      s += "," + iterProfile.next().getName();

    s = s + "|";
    return s;
  }

  public void writeToXml() throws InstanciationException {

    String strDescriptorFileName = Instanciateur.getXMLPackage()
        + File.separatorChar + name.trim() + ".xml";
    Mapping mapping = new Mapping();
    String strMappingFileName = settings.getString("CastorXMLMappingFileURL");
    // String strSchemaFileName =
    // settings.getString("ComponentDescriptorSchemaFileURL");
    String strDescriptorFileEncoding = settings
        .getString("ComponentDescriptorFileEncoding");
    Marshaller mar;
    ResourceLocator resourceSettings = new ResourceLocator(
        "com.stratelia.webactiv.util.attachment.Attachment", "");
    boolean runOnUnix = SilverpeasSettings.readBoolean(resourceSettings,
        "runOnSolaris", false);

    try {
      // Format these urls
      //
      if (runOnUnix)
        strMappingFileName = strMappingFileName.replace('\\', '/');
      else
        strMappingFileName = "file:///" + strMappingFileName.replace('\\', '/');

      strDescriptorFileName = strDescriptorFileName.replace('\\', '/');

      mapping.loadMapping(strMappingFileName);

      mar = new Marshaller(new OutputStreamWriter(new FileOutputStream(
          strDescriptorFileName), strDescriptorFileEncoding));
      mar.setMapping(mapping);
      // mar.setNoNamespaceSchemaLocation( strSchemaFileName );
      mar.setSuppressNamespaces(true);
      mar.setSuppressXSIType(true);
      mar.setValidation(false);
      mar.setEncoding(strDescriptorFileEncoding);
      mar.marshal(this);
    } catch (MappingException me) {
      throw new InstanciationException("WAComponent.writeToXml",
          SilverpeasException.ERROR, "admin.EX_ERR_CASTOR_LOAD_XML_MAPPING",
          "Mapping file name : "
          + (strMappingFileName == null ? "<null>" : strMappingFileName),
          me);
    } catch (MarshalException me) {
      throw new InstanciationException("WAComponent.writeToXml",
          SilverpeasException.ERROR,
          "admin.EX_ERR_CASTOR_MARSHALL_COMPONENT_DESCRIPTOR",
          "Component descriptor file name : "
          + (strDescriptorFileName == null ? "<null>"
          : strDescriptorFileName), me);
    } catch (ValidationException ve) {
      throw new InstanciationException("WAComponent.writeToXml",
          SilverpeasException.ERROR,
          "admin.EX_ERR_CASTOR_INVALID_XML_COMPONENT_DESCRIPTOR",
          "Component descriptor file name : "
          + (strDescriptorFileName == null ? "<null>"
          : strDescriptorFileName), ve);
    } catch (IOException ioe) {
      throw new InstanciationException("WAComponent.writeToXml",
          SilverpeasException.ERROR,
          "admin.EX_ERR_CASTOR_SAVE_COMPONENT_DESCRIPTOR",
          "Component descriptor file name : "
          + (strDescriptorFileName == null ? "<null>"
          : strDescriptorFileName), ioe);
    }
  }

  protected String booleanToString(boolean b) {
    if (b) {
      return "yes";
    } else {
      return "no";
    }
  }

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