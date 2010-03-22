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
package com.silverpeas.admin.importExport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;

/**
 * Classe de gestion des components dans le moteur d'importExport de silverpeas.
 * @author sdevolder
 */
public class AdminImportExport {

  // Variables
  AdminController ac = null;

  /**
   * Méthode récupérant la liste des componentInsts des composants impliqués dans une exportation
   * donnée et destinées au marshalling.
   * @param listComponentId = liste des id des composants impliqués dans l'esxport en cours
   * @return l'objet ComponentsType complété, null si la liste passée en paramètre est vide
   */
  public ComponentsType getComponents(List listComponentId) {
    ComponentsType componentsType = new ComponentsType();
    List listComponentInst = new ArrayList();
    Iterator itListComponentId = listComponentId.iterator();
    while (itListComponentId.hasNext()) {
      String componentId = (String) itListComponentId.next();
      ComponentInst componentInst = getAdminController().getComponentInst(
          componentId);
      listComponentInst.add(componentInst);
    }
    componentsType.setListComponentInst(listComponentInst);
    return componentsType;
  }

  /**
   * @return un objet AdminController
   */
  private AdminController getAdminController() {
    if (ac == null) {
      ac = new AdminController("unknown");
    }
    return ac;
  }

}
