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

package com.silverpeas.jobManagerPeas.control;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.silverpeas.jobManagerPeas.JobManagerService;
import com.silverpeas.jobManagerPeas.JobManagerSettings;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class JobManagerPeasSessionController extends
    AbstractComponentSessionController {
  // variables gloabes
  private Hashtable services = null;

  private String idCurrentServiceActif = null;
  private String idCurrentOperationActif = null;
  private boolean isManager = false;

  /**
   * Standard Session Controller Constructeur
   * 
   * 
   * @param mainSessionCtrl
   *          The user's profile
   * @param componentContext
   *          The component's profile
   * 
   * @see
   */
  public JobManagerPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.jobManagerPeas.multilang.jobManagerPeasBundle",
        "com.silverpeas.jobManagerPeas.settings.jobManagerPeasIcons",
        "com.silverpeas.jobManagerPeas.settings.jobManagerPeasSettings");
    setComponentRootName(URLManager.CMP_JOBMANAGERPEAS);

    String[] ids = mainSessionCtrl.getUserManageableSpaceIds();
    if (ids != null && ids.length > 0)
      isManager = true;

    if (services == null)
      initServices();
  }

  private void initServices() {
    services = new Hashtable();
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");

    // inititialisation de tous les services disponibles
    // initialisation des services
    JobManagerService jDesigner = null;
    JobManagerService jKM = null;
    JobManagerService jSTAT = null;
    JobManagerService jTools = null;

    // initialisation des opérations du services jDesigner
    JobManagerService jdp = new JobManagerService("11", "JDP",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL(URLManager.CMP_JOBDOMAINPEAS) + "Main", null,
        false);
    JobManagerService jspp = new JobManagerService("12", "JSPP",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL(URLManager.CMP_JOBSTARTPAGEPEAS) + "Main",
        null, false);
    JobManagerService jrp = new JobManagerService("13", "JRP",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL(URLManager.CMP_JOBORGANIZATIONPEAS) + "Main",
        null, false);

    // tools
    // JobManagerService jRepositoryImport = new JobManagerService("41", "JRI",
    // JobManagerService.LEVEL_OPERATION, m_context +
    // URLManager.getURL(URLManager.CMP_JOBTOOLSPEAS)+"RepositoryImport", null,
    // false);
    JobManagerService jImportExport = new JobManagerService("42", "JIE",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL("importExportPeas") + "Main", null, false);
    JobManagerService jSpecificAuthent = new JobManagerService("43", "JSA",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL("specificAuthent") + "Main", null, false);
    JobManagerService jtd = new JobManagerService("44", "JTD",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL(URLManager.CMP_TEMPLATEDESIGNER) + "Main",
        null, false);
    JobManagerService jWorkflowDesigner = new JobManagerService("45", "JWD",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL("workflowDesigner") + "Main", null, false);
    JobManagerService portletDeployer = new JobManagerService("46", "portlets",
        JobManagerService.LEVEL_OPERATION, m_context + "/portletDeployer",
        null, false);
    JobManagerService jst = new JobManagerService("47", "JST",
        JobManagerService.LEVEL_OPERATION, m_context
            + "/admin/jsp/ExploitationSilverTrace.jsp", null, false);

    // initialisation des opérations du service jKM
    JobManagerService jKM1 = new JobManagerService("21", "JKM1",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL(URLManager.CMP_PDC) + "Main", null, false);
    JobManagerService jKM2 = new JobManagerService("22", "JKM2",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL(URLManager.CMP_THESAURUS) + "Main", null, false);

    // initialisation des opérations du service jSTAT
    JobManagerService jSTAT1 = new JobManagerService("31", "JSTAT1",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL(URLManager.CMP_SILVERSTATISTICSPEAS) + "Main",
        null, false); // ViewConnection
    JobManagerService jSTAT2 = new JobManagerService("32", "JSTAT2",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL(URLManager.CMP_SILVERSTATISTICSPEAS)
            + "ViewAccess", null, false);
    JobManagerService jSTAT3 = new JobManagerService("33", "JSTAT3",
        JobManagerService.LEVEL_OPERATION, m_context
            + URLManager.getURL(URLManager.CMP_SILVERSTATISTICSPEAS)
            + "ViewVolumeServices", null, false);

    boolean kmServiceAllowed = false;
    int nbServices = 0;

    if (getUserDetail().isAccessAdmin()) {
      // l'administrateur à accès au tout
      String[] id = { "11", "13", "12" };
      jDesigner = new JobManagerService("1", "JD",
          JobManagerService.LEVEL_SERVICE, null, id, false);

      String[] id2 = { "31", "32", "33" };
      jSTAT = new JobManagerService("3", "JSTAT",
          JobManagerService.LEVEL_SERVICE, null, id2, false);

      if (JobManagerSettings.m_IsKMVisible) {
        String[] id1 = { "21", "22" };
        jKM = new JobManagerService("2", "JKM",
            JobManagerService.LEVEL_SERVICE, null, id1, false);

        services.put(jKM.getId(), jKM);
        services.put(jKM1.getId(), jKM1);
        services.put(jKM2.getId(), jKM2);

        kmServiceAllowed = true;
      }

      if (JobManagerSettings.m_IsToolsVisible) {
        if (isImportExportLicenseOK()
            || JobManagerSettings.m_IsToolSpecificAuthentVisible
            || JobManagerSettings.m_IsToolWorkflowDesignerVisible) {

          List ids = new ArrayList();
          if (isImportExportLicenseOK()) {
            ids.add("42");
            services.put(jImportExport.getId(), jImportExport);
          }
          if (JobManagerSettings.m_IsToolSpecificAuthentVisible) {
            ids.add("43");
            services.put(jSpecificAuthent.getId(), jSpecificAuthent);
          }
          if (JobManagerSettings.m_IsTemplateDesignerVisible) {
            ids.add("44");
            services.put(jtd.getId(), jtd);
          }
          if (JobManagerSettings.m_IsToolWorkflowDesignerVisible) {
            ids.add("45");
            services.put(jWorkflowDesigner.getId(), jWorkflowDesigner);
          }
          if (JobManagerSettings.m_IsPortletDeployerVisible) {
            ids.add("46");
            services.put(portletDeployer.getId(), portletDeployer);
          }

          ids.add("47");
          services.put(jst.getId(), jst);

          jTools = new JobManagerService("4", "JTOOLS",
              JobManagerService.LEVEL_SERVICE, null, (String[]) ids
                  .toArray(new String[0]), false);
          services.put(jTools.getId(), jTools);
        }
      }

      services.put(jDesigner.getId(), jDesigner);
      services.put(jdp.getId(), jdp);
      services.put(jrp.getId(), jrp);
      services.put(jspp.getId(), jspp);
      // services.put(jst.getId(), jst);

      services.put(jSTAT.getId(), jSTAT);
      services.put(jSTAT1.getId(), jSTAT1);
      services.put(jSTAT2.getId(), jSTAT2);
      services.put(jSTAT3.getId(), jSTAT3);

      // services.put(jRepositoryImport.getId(), jRepositoryImport);
      // services.put(jImportExport.getId(), jImportExport);
    } else if (isManager) {
      if (getUserDetail().isAccessDomainManager()) {
        // l'administrateur du composant à accès seulement à certaine
        // fonction
        String[] id3 = { "11", "13", "12" };
        jDesigner = new JobManagerService("1", "JD",
            JobManagerService.LEVEL_SERVICE, null, id3, false);

        services.put(jDesigner.getId(), jDesigner);
        nbServices++;

        services.put(jdp.getId(), jdp);
        services.put(jrp.getId(), jrp);
        services.put(jspp.getId(), jspp);
      } else {
        // l'administrateur d'espace à accès seulement à certaine fonction
        if (getUserManageableGroupIds().size() > 0) {
          // Il est également gestionnaire de groupe, il a acces au
          // référentiel
          // (jobDomain)
          String[] id3 = { "11", "13", "12" };
          jDesigner = new JobManagerService("1", "JD",
              JobManagerService.LEVEL_SERVICE, null, id3, false);

          services.put(jDesigner.getId(), jDesigner);
          nbServices++;

          services.put(jdp.getId(), jdp);
          services.put(jrp.getId(), jrp);
          services.put(jspp.getId(), jspp);
        } else {
          // Il n'est pas gestionnaire de groupe
          String[] id3 = { "13", "12" };
          jDesigner = new JobManagerService("1", "JD",
              JobManagerService.LEVEL_SERVICE, null, id3, false);

          services.put(jDesigner.getId(), jDesigner);
          nbServices++;

          services.put(jrp.getId(), jrp);
          services.put(jspp.getId(), jspp);
        }
      }

      String[] id2 = { "32", "33" };
      jSTAT = new JobManagerService("3", "JSTAT",
          JobManagerService.LEVEL_SERVICE, null, id2, false);

      if (getUserDetail().isAccessKMManager()
          && JobManagerSettings.m_IsKMVisible) {
        String[] id1 = { "21", "22" };
        jKM = new JobManagerService("2", "JKM",
            JobManagerService.LEVEL_SERVICE, null, id1, false);

        services.put(jKM.getId(), jKM);
        services.put(jKM1.getId(), jKM1);
        services.put(jKM2.getId(), jKM2);

        kmServiceAllowed = true;
      }

      services.put(jSTAT.getId(), jSTAT);
      services.put(jSTAT2.getId(), jSTAT2);
      services.put(jSTAT3.getId(), jSTAT3);
    } else if (getUserDetail().isAccessKMManager()
        && JobManagerSettings.m_IsKMVisible) {
      String[] id1 = { "21", "22" };
      jKM = new JobManagerService("1", "JKM", JobManagerService.LEVEL_SERVICE,
          null, id1, false);

      services.put(jKM.getId(), jKM);
      services.put(jKM1.getId(), jKM1);
      services.put(jKM2.getId(), jKM2);

      kmServiceAllowed = true;
    } else if (getUserDetail().isAccessDomainManager()
        || getUserManageableGroupIds().size() > 0) {
      String[] id1 = { "11" };
      jDesigner = new JobManagerService("1", "JD",
          JobManagerService.LEVEL_SERVICE, null, id1, false);

      services.put(jDesigner.getId(), jDesigner);
      nbServices++;

      services.put(jdp.getId(), jdp);
    }

    boolean isPDCManager = false;

    try {
      isPDCManager = new PdcBmImpl().isUserManager(getUserId());
    } catch (PdcException e) {
      SilverTrace.error("jobManagerPeas",
          "jobManagerPeasSessionController.initServices()",
          "root.MSG_GEN_PARAM_VALUE", e);
    }

    if (!kmServiceAllowed && isPDCManager) {
      String[] id1 = { "21" };
      jKM = new JobManagerService(Integer.toString(nbServices + 1), "JKM",
          JobManagerService.LEVEL_SERVICE, null, id1, false);

      services.put(jKM.getId(), jKM);
      services.put(jKM1.getId(), jKM1);
    }

    // set du service par defaut=> à récupérer d'un properties
    // this.idCurrentServiceActif = "1";
    // set de l'opération par defaut correspondant au service

  }

  // retourne les services de niveau level
  // 0 => niveau Service
  // 1 => opération
  // 2 => action
  // 3 => acteur
  public JobManagerService[] getServices(int level) {
    SilverTrace.debug("jobManagerPeas",
        "jobManagerPeasSessionController.getServices()",
        "root.MSG_GEN_PARAM_VALUE", "ENTER level=" + level);
    ArrayList listServices = new ArrayList();
    Enumeration enumer = services.elements();
    while (enumer.hasMoreElements()) {
      JobManagerService jms = (JobManagerService) enumer.nextElement();
      if (jms.getLevel() == level) {
        SilverTrace.debug("jobManagerPeas",
            "jobManagerPeasSessionController.getServices()",
            "root.MSG_GEN_PARAM_VALUE", " jms id=" + jms.getId()
                + "  jms label=" + jms.getLabel());
        listServices.add(jms);
      }
    }
    return (JobManagerService[]) listServices.toArray(new JobManagerService[0]);
  }

  public JobManagerService[] getSubServices(JobManagerService jmsParent) {
    SilverTrace
        .debug(
            "jobManagerPeas",
            "jobManagerPeasSessionController.getSubServices(JobManagerService jmsParent)",
            "root.MSG_GEN_PARAM_VALUE", "ENTER jmsParent id="
                + jmsParent.getId() + " jmsParent label="
                + jmsParent.getLabel());
    ArrayList listChild = new ArrayList();
    String[] idSubServices = jmsParent.getIdSubServices();
    for (int i = 0; i < idSubServices.length; i++) {
      JobManagerService jmsChild = (JobManagerService) services
          .get(idSubServices[i]);
      if (jmsChild != null) {
        SilverTrace
            .debug(
                "jobManagerPeas",
                "jobManagerPeasSessionController.getSubServices(JobManagerService jmsParent)",
                "root.MSG_GEN_PARAM_VALUE", "Add services child jmsChild id="
                    + jmsChild.getId() + " jmsChild label="
                    + jmsChild.getLabel());
        listChild.add(jmsChild);
      }
    }
    return (JobManagerService[]) listChild.toArray(new JobManagerService[0]);
  }

  public JobManagerService[] getSubServices(String idService) {
    return this.getSubServices((JobManagerService) services.get(idService));
  }

  public JobManagerService getService(String key) {
    return (JobManagerService) this.services.get(key);
  }

  public void changeServiceActif(String idNewService) {
    // mise a jour de l'id actif et de l'objet correspondant
    SilverTrace
        .debug(
            "jobManagerPeas",
            "jobManagerPeasSessionController.changeServiceActif(String idNewService)",
            "root.MSG_GEN_PARAM_VALUE", "ENTER idCurrentServiceActif="
                + idCurrentServiceActif + " idNewService=" + idNewService);
    if (this.idCurrentServiceActif != null) {// si = null, nous sommes dans la
      // première initialisation de
      // idCurrentServiceActif
      // resset du flag actif du service courant
      ((JobManagerService) services.get(this.idCurrentServiceActif))
          .setActif(false);
    }
    //
    JobManagerService newService = (JobManagerService) services
        .get(idNewService);
    newService.setActif(true);
    this.idCurrentServiceActif = idNewService;

    // initialisation du idCurrentOperationActif avec l'opération active
    // correspondant au nouveau service
    JobManagerService[] jms = this.getSubServices(newService);
    idCurrentOperationActif = null;
    for (int i = 0; i < jms.length; i++) {
      if (jms[i].isActif())
        idCurrentOperationActif = jms[i].getId();
    }

    // contrôle si l'attribut idCurrentServiceActif à bien une valeur
    // si oui=> le service courant à déjà été consulté et il existe bien
    // une
    // opération active
    // si non => le service courant n'a pas encore été consulté et
    // l'opération
    // active est la valeur par defaut
    if (idCurrentOperationActif == null) {
      JobManagerService currentOperation = (JobManagerService) services
          .get(newService.getDefautIdSubService());
      currentOperation.setActif(true);
      this.idCurrentOperationActif = newService.getDefautIdSubService();
    }

    SilverTrace
        .debug(
            "jobManagerPeas",
            "jobManagerPeasSessionController.changeServiceActif(String idNewService)",
            "root.MSG_GEN_PARAM_VALUE", "END idCurrentServiceActif="
                + idCurrentServiceActif + " idNewService=" + idNewService);

  }

  public String getIdServiceActif() {
    return this.idCurrentServiceActif;
  }

  public String getIdOperationActif() {
    return this.idCurrentOperationActif;
  }

  public void changeOperationActif(String idNewOperation) {
    ((JobManagerService) services.get(idCurrentOperationActif)).setActif(false);
    SilverTrace
        .debug(
            "jobManagerPeas",
            "jobManagerPeasSessionController.changeOperationActif(String idNewOperation)",
            "root.MSG_GEN_PARAM_VALUE", "END idNewOperation=" + idNewOperation);

    ((JobManagerService) services.get(idNewOperation)).setActif(true);
    this.idCurrentOperationActif = idNewOperation;
  }

  public String getIdDefaultService() {
    return "1";
  }

  private boolean isImportExportLicenseOK() {
    ResourceLocator resource = new ResourceLocator("license.license", "");
    String code = resource.getString("import");

    boolean validSequence = true;
    String serial = "373957568";
    try {
      for (int i = 0; i < 9 && validSequence; i++) {
        String groupe = code.substring(i * 3, i * 3 + 3);
        int total = 0;
        for (int j = 0; j < groupe.length(); j++) {
          String valeur = groupe.substring(j, j + 1);
          total += new Integer(valeur).intValue();
        }
        if (total != new Integer(serial.substring(i * 1, i * 1 + 1)).intValue())
          validSequence = false;
      }
    } catch (Exception e) {
      validSequence = false;
    }
    return validSequence;
  }
}