/*
 * Created on 17 févr. 2005
 */
package com.silverpeas.admin.importExport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;

/**
 * Classe de gestion des components dans le moteur d'importExport de silverpeas.
 * 
 * @author sdevolder
 */
public class AdminImportExport {

  // Variables
  AdminController ac = null;

  // Méthodes
  /**
   * Méthode récupérant la liste des componentInsts des composants impliqués
   * dans une exportation donnée et destinées au marshalling.
   * 
   * @param listComponentId
   *          = liste des id des composants impliqués dans l'esxport en cours
   * @return l'objet ComponentsType complété, null si la liste passée en
   *         paramètre est vide
   */
  public ComponentsType getComponents(List listComponentId) {

    ComponentsType componentsType = null;
    ComponentInst componentInst = null;
    ArrayList listComponentInst = null;
    Iterator itListComponentId = listComponentId.iterator();
    while (itListComponentId.hasNext()) {
      String componentId = (String) itListComponentId.next();
      componentInst = getAdminController().getComponentInst(componentId);
      if (listComponentInst == null)
        listComponentInst = new ArrayList();
      listComponentInst.add(componentInst);
    }
    if (listComponentInst != null)
      componentsType = new ComponentsType();
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
