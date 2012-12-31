/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.jobManagerPeas.control;

import com.silverpeas.jobManagerPeas.JobManagerService;
import com.silverpeas.jobManagerPeas.JobManagerSettings;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.silverpeas.jobManagerPeas.JobManagerService.*;
import static com.stratelia.silverpeas.peasCore.URLManager.*;

/**
 * Class declaration
 *
 * @author
 */
public class JobManagerPeasSessionController extends AbstractComponentSessionController {
  // variables gloabes

  private Map<String, JobManagerService> services = null;
  private String idCurrentServiceActif = null;
  private String idCurrentOperationActif = null;
  private boolean isManager = false;

  /**
   * Standard Session Controller Constructeur
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public JobManagerPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.jobManagerPeas.multilang.jobManagerPeasBundle",
        "org.silverpeas.jobManagerPeas.settings.jobManagerPeasIcons",
        "org.silverpeas.jobManagerPeas.settings.jobManagerPeasSettings");
    setComponentRootName(CMP_JOBMANAGERPEAS);

    String[] ids = mainSessionCtrl.getUserManageableSpaceIds();
    if (ids != null && ids.length > 0) {
      isManager = true;
    }
    if (services == null) {
      initServices();
    }
  }

  private void initServices() {
    services = new HashMap<String, JobManagerService>(100);
    String webContext = getApplicationURL();

    // inititialisation de tous les services disponibles
    // initialisation des services
    JobManagerService jDesigner;
    JobManagerService jKM;
    JobManagerService jSTAT;
    JobManagerService jTools;

    // initialisation des opérations du services jDesigner
    JobManagerService jdp = new JobManagerService("11", "JDP", LEVEL_OPERATION, webContext
        + getURL(CMP_JOBDOMAINPEAS, null, null) + "Main", null, false);
    JobManagerService jspp = new JobManagerService("12", "JSPP", LEVEL_OPERATION, webContext
        + getURL(CMP_JOBSTARTPAGEPEAS, null, null) + "Main", null, false);
    JobManagerService jrp = new JobManagerService("13", "JRP", LEVEL_OPERATION, webContext
        + getURL(CMP_JOBORGANIZATIONPEAS, null, null) + "Main", null, false);
    JobManagerService jsp = new JobManagerService("14", "JSP", LEVEL_OPERATION, webContext
        + getURL(CMP_JOBSEARCHPEAS, null, null) + "Main", null, false);
    // tools
    JobManagerService jImportExport =
        new JobManagerService("42", "JIE", LEVEL_OPERATION, webContext
        + getURL("importExportPeas", null, null) + "Main", null, false);
    JobManagerService jSpecificAuthent = new JobManagerService("43", "JSA", LEVEL_OPERATION,
        webContext + getURL("specificAuthent", null, null) + "Main", null, false);
    JobManagerService jtd = new JobManagerService("44", "JTD", LEVEL_OPERATION, webContext
        + getURL(CMP_TEMPLATEDESIGNER, null, null) + "Main", null, false);
    JobManagerService jWorkflowDesigner = new JobManagerService("45", "JWD", LEVEL_OPERATION,
        webContext + getURL("workflowDesigner", null, null) + "Main", null, false);
    JobManagerService portletDeployer = new JobManagerService("46", "portlets", LEVEL_OPERATION,
        webContext + "/portletDeployer", null, false);
    JobManagerService jst = new JobManagerService("47", "JST", LEVEL_OPERATION, webContext
        + "/admin/jsp/ExploitationSilverTrace.jsp", null, false);
    JobManagerService jindex =
        new JobManagerService("48", "reindexation", LEVEL_OPERATION, webContext
        + "/applicationIndexer/jsp/applicationIndexer.jsp", null, false);
    JobManagerService jabout = new JobManagerService("49", "JAB", LEVEL_OPERATION, webContext
        + "/silverpeasinfos.jsp", null, false);

    // initialisation des opérations du service jKM
    JobManagerService jKM1 = new JobManagerService("21", "JKM1", LEVEL_OPERATION, webContext
        + getURL(CMP_PDC, null, null) + "Main", null, false);
    JobManagerService jKM2 = new JobManagerService("22", "JKM2", LEVEL_OPERATION, webContext
        + getURL(CMP_THESAURUS, null, null) + "Main", null, false);

    // initialisation des opérations du service jSTAT
    JobManagerService jSTAT1 = new JobManagerService("31", "JSTAT1", LEVEL_OPERATION, webContext
        + getURL(CMP_SILVERSTATISTICSPEAS, null, null) + "Main", null, false); // ViewConnection
    JobManagerService jSTAT2 = new JobManagerService("32", "JSTAT2", LEVEL_OPERATION, webContext
        + getURL(CMP_SILVERSTATISTICSPEAS, null, null) + "ViewAccess", null, false);
    JobManagerService jSTAT3 = new JobManagerService("33", "JSTAT3", LEVEL_OPERATION, webContext
        + getURL(CMP_SILVERSTATISTICSPEAS, null, null) + "ViewVolumeServices", null, false);
    JobManagerService jSTAT4 = new JobManagerService("34", "JSTAT4", LEVEL_OPERATION, webContext
        + getURL(CMP_SILVERSTATISTICSPEAS, null, null) + "ViewPDCAccess", null, false);

    boolean kmServiceAllowed = false;
    int nbServices = 0;

    if (getUserDetail().isAccessAdmin()) {
      // l'administrateur à accès au tout
      jDesigner = new JobManagerService("1", "JD", LEVEL_SERVICE, null, new String[]{"11", "13",
        "12", "14"}, false);
      jSTAT =
          new JobManagerService("3", "JSTAT", LEVEL_SERVICE, null, new String[]{"31", "32", "33",
        "34"}, false);

      if (JobManagerSettings.m_IsKMVisible) {
        jKM =
            new JobManagerService("2", "JKM", LEVEL_SERVICE, null, new String[]{"21", "22"},
            false);
        services.put(jKM.getId(), jKM);
        services.put(jKM1.getId(), jKM1);
        services.put(jKM2.getId(), jKM2);
        kmServiceAllowed = true;
      }

      if (JobManagerSettings.m_IsToolsVisible) {
        if (isImportExportLicenseOK() || JobManagerSettings.m_IsToolSpecificAuthentVisible
            || JobManagerSettings.m_IsToolWorkflowDesignerVisible) {

          List<String> ids = new ArrayList<String>(10);
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
          ids.add("48");
          services.put(jindex.getId(), jindex);
          ids.add("49");
          services.put(jabout.getId(), jabout);

          jTools = new JobManagerService("4", "JTOOLS", LEVEL_SERVICE, null,
              ids.toArray(new String[ids.size()]), false);
          services.put(jTools.getId(), jTools);
        }
      }

      services.put(jDesigner.getId(), jDesigner);
      services.put(jdp.getId(), jdp);
      services.put(jrp.getId(), jrp);
      services.put(jspp.getId(), jspp);
      services.put(jsp.getId(), jsp);

      services.put(jSTAT.getId(), jSTAT);
      services.put(jSTAT1.getId(), jSTAT1);
      services.put(jSTAT2.getId(), jSTAT2);
      services.put(jSTAT3.getId(), jSTAT3);
      services.put(jSTAT4.getId(), jSTAT4);

      // services.put(jRepositoryImport.getId(), jRepositoryImport);
      // services.put(jImportExport.getId(), jImportExport);
    } else if (isManager) {
      if (getUserDetail().isAccessDomainManager()) {
        // l'administrateur du composant à accès seulement à certaines fonctions
        jDesigner =
            new JobManagerService("1", "JD", LEVEL_SERVICE, null, new String[]{"11", "13",
          "12"}, false);

        services.put(jDesigner.getId(), jDesigner);
        nbServices++;

        services.put(jdp.getId(), jdp);
        services.put(jrp.getId(), jrp);
        services.put(jspp.getId(), jspp);
      } else {
        // l'administrateur d'espace à accès seulement à certaines fonctions
        if (getUserManageableGroupIds().size() > 0) {
          // Il est également gestionnaire de groupe, il a acces au référentiel (jobDomain)
          jDesigner =
              new JobManagerService("1", "JD", LEVEL_SERVICE, null, new String[]{"11", "13",
            "12"}, false);
          services.put(jDesigner.getId(), jDesigner);
          nbServices++;

          services.put(jdp.getId(), jdp);
          services.put(jrp.getId(), jrp);
          services.put(jspp.getId(), jspp);
        } else {
          // Il n'est pas gestionnaire de groupe
          jDesigner =
              new JobManagerService("1", "JD", LEVEL_SERVICE, null, new String[]{"13", "12"},
              false);
          services.put(jDesigner.getId(), jDesigner);
          nbServices++;
          services.put(jrp.getId(), jrp);
          services.put(jspp.getId(), jspp);
        }
      }

      String[] id2 = {"32", "33"};
      jSTAT = new JobManagerService("3", "JSTAT", LEVEL_SERVICE, null, id2, false);

      if (getUserDetail().isAccessKMManager()
          && JobManagerSettings.m_IsKMVisible) {
        String[] id1 = {"21", "22"};
        jKM = new JobManagerService("2", "JKM", LEVEL_SERVICE, null, id1, false);

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
      String[] id1 = {"21", "22"};
      jKM = new JobManagerService("1", "JKM", LEVEL_SERVICE, null, id1, false);

      services.put(jKM.getId(), jKM);
      services.put(jKM1.getId(), jKM1);
      services.put(jKM2.getId(), jKM2);

      kmServiceAllowed = true;
    } else if (getUserDetail().isAccessDomainManager() || !getUserManageableGroupIds().isEmpty()) {
      String[] id1 = {"11"};
      jDesigner = new JobManagerService("1", "JD", LEVEL_SERVICE, null, id1, false);
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
      String[] id1 = {"21"};
      jKM = new JobManagerService(Integer.toString(nbServices + 1), "JKM",
          LEVEL_SERVICE, null, id1, false);

      services.put(jKM.getId(), jKM);
      services.put(jKM1.getId(), jKM1);
    }
  }

  // retourne les services de niveau level
  // 0 => niveau Service
  // 1 => opération
  // 2 => action
  // 3 => acteur
  public JobManagerService[] getServices(int level) {
    SilverTrace.debug("jobManagerPeas", "jobManagerPeasSessionController.getServices()",
        "root.MSG_GEN_PARAM_VALUE", "ENTER level=" + level);
    List<JobManagerService> listServices = new ArrayList<JobManagerService>(services.size());
    for (JobManagerService jms : services.values()) {
      if (jms.getLevel() == level) {
        SilverTrace.debug("jobManagerPeas", "jobManagerPeasSessionController.getServices()",
            "root.MSG_GEN_PARAM_VALUE",
            " jms id=" + jms.getId() + "  jms label=" + jms.getLabel());
        listServices.add(jms);
      }
    }
    return listServices.toArray(new JobManagerService[listServices.size()]);
  }

  public JobManagerService[] getSubServices(JobManagerService jmsParent) {
    SilverTrace.debug("jobManagerPeas",
        "jobManagerPeasSessionController.getSubServices(JobManagerService jmsParent)",
        "root.MSG_GEN_PARAM_VALUE", "ENTER jmsParent id=" + jmsParent.getId() + " jmsParent label="
        + jmsParent.getLabel());
    String[] idSubServices = jmsParent.getIdSubServices();    
    List<JobManagerService> listChild = new ArrayList<JobManagerService>(idSubServices.length);
    for (String idSubService : idSubServices) {
      JobManagerService jmsChild = services.get(idSubService);
      if (jmsChild != null) {
        SilverTrace.debug("jobManagerPeas", 
            "jobManagerPeasSessionController.getSubServices(JobManagerService jmsParent)",
            "root.MSG_GEN_PARAM_VALUE", "Add services child jmsChild id=" + jmsChild.getId() 
            + " jmsChild label=" + jmsChild.getLabel());
        listChild.add(jmsChild);
      }
    }
    return listChild.toArray(new JobManagerService[listChild.size()]);
  }

  public JobManagerService[] getSubServices(String idService) {
    return this.getSubServices(services.get(idService));
  }

  public JobManagerService getService(String key) {
    return this.services.get(key);
  }

  public void changeServiceActif(String idNewService) {
    // mise a jour de l'id actif et de l'objet correspondant
    SilverTrace.debug("jobManagerPeas",
        "jobManagerPeasSessionController.changeServiceActif(String idNewService)",
        "root.MSG_GEN_PARAM_VALUE", "ENTER idCurrentServiceActif="
        + idCurrentServiceActif + " idNewService=" + idNewService);
    if (this.idCurrentServiceActif != null) {// si = null, nous sommes dans la
      // première initialisation de idCurrentServiceActif reset du flag actif du service courant
      services.get(this.idCurrentServiceActif).setActif(false);
    }
    //
    JobManagerService newService = services.get(idNewService);
    newService.setActif(true);
    this.idCurrentServiceActif = idNewService;

    // initialisation du idCurrentOperationActif avec l'opération active
    // correspondant au nouveau service
    JobManagerService[] jms = this.getSubServices(newService);
    idCurrentOperationActif = null;
    for (JobManagerService jm : jms) {
      if (jm.isActif()) {
        idCurrentOperationActif = jm.getId();
      }
    }

    // contrôle si l'attribut idCurrentServiceActif à bien une valeur
    // si oui=> le service courant à déjà été consulté et il existe bien
    // une
    // opération active
    // si non => le service courant n'a pas encore été consulté et
    // l'opération
    // active est la valeur par defaut
    if (idCurrentOperationActif == null) {
      JobManagerService currentOperation = services.get(newService.getDefautIdSubService());
      currentOperation.setActif(true);
      this.idCurrentOperationActif = newService.getDefautIdSubService();
    }

    SilverTrace.debug(
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
    services.get(idCurrentOperationActif).setActif(false);
    SilverTrace.debug(
        "jobManagerPeas",
        "jobManagerPeasSessionController.changeOperationActif(String idNewOperation)",
        "root.MSG_GEN_PARAM_VALUE", "END idNewOperation=" + idNewOperation);

    services.get(idNewOperation).setActif(true);
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
          total = total + Integer.parseInt(valeur);
        }
        if (total != Integer.parseInt(serial.substring(i, i + 1))) {
          validSequence = false;
        }
      }
    } catch (NumberFormatException e) {
      validSequence = false;
    }
    return validSequence;
  }
}
