/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.contentcontainer.container;

import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.util.URLUtil;

/**
 * This class represents one container descriptor in memory (read from the xml)
 */
public class ContainerPeas {

  // The container type (unique among all containers)
  String m_sType = null;
  // The class to call that implements the ContainerInterface
  String m_sContainerInterface = null;
  // The object (class.forName(m_sContainerInterface))
  ContainerInterface m_containerInterface = null;
  // User roles of the container
  List<String> m_asUserRoles = null;
  // URL to call when the content want to get back on the container
  String m_sReturnURL = null;
  // Name of the bean in the session
  String m_sSessionControlBeanName = null;
  // URLIcone on the classify JSP to call
  URLIcone m_ClassifyURLIcone = null;

  // for classifying a SilverContent
  public ContainerPeas(String sContainerDescriptorPath) {
    // -------------------------------------------------
    // We don't have enough time to do the parsing !!!
    // We hard coded for this time !!!!
    // -------------------------------------------------
    if (sContainerDescriptorPath.equals("containerPDC")) {
      this.setType("containerPDC");
      this.setContainerInterface("org.silverpeas.core.pdc.pdc.service.GlobalPdcManager");

      List<String> asUserRoles = new ArrayList<>();
      asUserRoles.add("containerPDC_admin");
      asUserRoles.add("containerPDC_user");
      this.setUserRoles(asUserRoles);
      this.setReturnURL("SearchView");
      this.setSessionControlBeanName("pdcSearch");

      URLIcone urlIcone = new URLIcone();
      urlIcone.setIconePath(
          URLUtil.getApplicationURL() + "/pdcPeas/jsp/icons/pdcPeas_classify_in_pdc.gif");
      urlIcone.setActionURL("/RpdcClassify/jsp/Main");
      this.setClassifyURLIcone(urlIcone);
    }
  }

  public void setType(String sType) {
    m_sType = sType;
  }

  public String getType() {
    return m_sType;
  }

  public void setContainerInterface(String sContainerInterface) {
    m_sContainerInterface = sContainerInterface;
  }

  public ContainerInterface getContainerInterface() throws Exception {
    if (m_containerInterface == null) {
      Class containerInterface = Class.forName(m_sContainerInterface);
      m_containerInterface = (ContainerInterface) containerInterface.newInstance();
    }

    return m_containerInterface;
  }

  public void setUserRoles(List<String> asUserRoles) {
    m_asUserRoles = asUserRoles;
  }

  public List<String> getUserRoles() {
    return m_asUserRoles;
  }

  public void setReturnURL(String sReturnURL) {
    m_sReturnURL = sReturnURL;
  }

  public String getReturnURL() {
    return m_sReturnURL;
  }

  public void setSessionControlBeanName(String sSessionControlBeanName) {
    m_sSessionControlBeanName = sSessionControlBeanName;
  }

  public String getSessionControlBeanName() {
    return m_sSessionControlBeanName;
  }

  public void setClassifyURLIcone(URLIcone ClassifyURLIcone) {
    m_ClassifyURLIcone = ClassifyURLIcone;
  }

  public URLIcone getClassifyURLIcone() {
    return m_ClassifyURLIcone;
  }
}