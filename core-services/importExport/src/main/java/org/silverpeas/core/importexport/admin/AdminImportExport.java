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

package org.silverpeas.core.importexport.admin;

import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.util.ServiceProvider;

/**
 * Classe de gestion des components dans le moteur d'importExport de silverpeas.
 * @author sdevolder
 */
public class AdminImportExport {

  /**
   * Méthode récupérant la liste des componentInsts des composants impliqués dans une exportation
   * donnée et destinées au marshalling.
   * @param listComponentId = liste des id des composants impliqués dans l'esxport en cours
   * @return l'objet ComponentsType complété, null si la liste passée en paramètre est vide
   */
  public ComponentsType getComponents(List<String> listComponentId) {
    ComponentsType componentsType = new ComponentsType();
    List<ComponentInst> listComponentInst = new ArrayList<ComponentInst>();
    for (String componentId : listComponentId) {
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
  private synchronized AdminController getAdminController() {
    return ServiceProvider.getService(AdminController.class);
  }

}
