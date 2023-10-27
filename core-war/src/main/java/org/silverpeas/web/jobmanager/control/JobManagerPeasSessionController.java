/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jobmanager.control;

import org.silverpeas.core.documenttemplate.DocumentTemplateSettings;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.wbe.WbeSettings;
import org.silverpeas.core.web.mvc.controller.AbstractAdminComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.web.jobmanager.JobManagerService;
import org.silverpeas.web.jobmanager.JobManagerSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.util.URLUtil.*;
import static org.silverpeas.web.jobmanager.JobManagerService.LEVEL_OPERATION;
import static org.silverpeas.web.jobmanager.JobManagerService.LEVEL_SERVICE;

/**
 * Web Controller for all administrative tasks in the backoffice
 * @author Emmanuel Hugonnet
 */
public class JobManagerPeasSessionController extends AbstractAdminComponentSessionController {

  private static final String DEFAULT_SERVICE_ID = "1";
  private static final String MAIN_OWN_BODY_LAYOUT = "Main?ownBodyLayout=true";
  private transient Map<String, JobManagerService> services;
  private String idCurrentServiceActif = null;
  private String idCurrentOperationActif = null;
  private boolean isManager = false;
  private String directAccessToSpaceId;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public JobManagerPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.jobManagerPeas.multilang.jobManagerPeasBundle",
        "org.silverpeas.jobManagerPeas.settings.jobManagerPeasIcons",
        "org.silverpeas.jobManagerPeas.settings.jobManagerPeasSettings");
    setComponentRootName(CMP_JOBMANAGERPEAS);

    String[] ids = mainSessionCtrl.getUserManageableSpaceIds();
    if (ArrayUtil.isNotEmpty(ids)) {
      isManager = true;
    }
    if (services == null) {
      initServices();
    }
  }

  private void initServices() {
    services = new HashMap<>(100);
    String webContext = getApplicationURL();
    boolean pdcServicesAdded = false;
    int jDesignerServicesCounter = 0;

    JobManagerSettings jobManagerSettings = JobManagerSettings.get();
    if (getUserDetail().isAccessAdmin()) {
      pdcServicesAdded = setAdminLevelServices(webContext, jobManagerSettings);
    } else if (isManager) {
      pdcServicesAdded = setManagerLevelServices(webContext, jobManagerSettings);
      jDesignerServicesCounter++;
    } else if (getUserDetail().isAccessPdcManager() && jobManagerSettings.isKMVisible()) {
      JobManagerService jKM1 = new JobManagerService("21", "JKM1", LEVEL_OPERATION, webContext
          + getURL(CMP_PDC, null, null) + "Main", null, false);
      JobManagerService jKM2 = new JobManagerService("22", "JKM2", LEVEL_OPERATION, webContext
          + getURL(CMP_THESAURUS, null, null) + "Main", null, false);
      pdcServicesAdded = setJKMServices(DEFAULT_SERVICE_ID, jKM1, jKM2);
    } else if (getUserDetail().isAccessDomainManager() || !getUserManageableGroupIds().isEmpty()) {
      JobManagerService jdp = new JobManagerService("11", "JDP", LEVEL_OPERATION,
          webContext + getURL(CMP_JOBDOMAINPEAS, null, null) + MAIN_OWN_BODY_LAYOUT, null,
          false);
      String[] id1 = {jdp.getId()};
      JobManagerService jDesigner =
          new JobManagerService(DEFAULT_SERVICE_ID, "JD", LEVEL_SERVICE, null, id1, false);
      setServices(jDesigner);
      jDesignerServicesCounter++;

      setServices(jdp);
    }

    boolean isPDCManager = isPDCManager();

    if (!pdcServicesAdded && isPDCManager) {
      JobManagerService jKM1 = new JobManagerService("21", "JKM1", LEVEL_OPERATION, webContext
          + getURL(CMP_PDC, null, null) + "Main", null, false);
      String[] id1 = {jKM1.getId()};
      JobManagerService jKM = new JobManagerService(Integer.toString(jDesignerServicesCounter + 1), "JKM",
          LEVEL_SERVICE, null, id1, false);
      setServices(jKM, jKM1);
    }
  }

  private boolean setManagerLevelServices(final String webContext,
      final JobManagerSettings jobManagerSettings) {
    boolean pdcServicesAdded = false;

    JobManagerService jdp = new JobManagerService("11", "JDP", LEVEL_OPERATION,
        webContext + getURL(CMP_JOBDOMAINPEAS, null, null) + MAIN_OWN_BODY_LAYOUT, null,
        false);
    JobManagerService jspp = new JobManagerService("12", "JSPP", LEVEL_OPERATION,
        webContext + getURL(CMP_JOBSTARTPAGEPEAS, null, null) + MAIN_OWN_BODY_LAYOUT, null,
        false);

    // initialisation des opérations du service jKM
    JobManagerService jKM1 = new JobManagerService("21", "JKM1", LEVEL_OPERATION, webContext
        + getURL(CMP_PDC, null, null) + "Main", null, false);
    JobManagerService jKM2 = new JobManagerService("22", "JKM2", LEVEL_OPERATION, webContext
        + getURL(CMP_THESAURUS, null, null) + "Main", null, false);

    // initialisation des opérations du service jSTAT
    JobManagerService jSTAT2 = new JobManagerService("32", "JSTAT2", LEVEL_OPERATION, webContext
        + getURL(CMP_SILVERSTATISTICSPEAS, null, null) + "ViewAccess", null, false);
    JobManagerService jSTAT3 = new JobManagerService("33", "JSTAT3", LEVEL_OPERATION, webContext
        + getURL(CMP_SILVERSTATISTICSPEAS, null, null) + "ViewVolumeServices", null, false);

    String[] functionIds = {jspp.getId(), jdp.getId()};
    JobManagerService jDesigner = new JobManagerService(DEFAULT_SERVICE_ID, "JD", LEVEL_SERVICE, null, functionIds,
        false);

    setServices(jDesigner);


    setServices(jdp, jspp);

    String[] id2 = {jSTAT2.getId(), jSTAT3.getId()};
    JobManagerService jSTAT = new JobManagerService("3", "JSTAT", LEVEL_SERVICE, null, id2, false);

    if (getUserDetail().isAccessPdcManager() && jobManagerSettings.isKMVisible()) {
      pdcServicesAdded = setJKMServices("2", jKM1, jKM2);
    }

    setServices(jSTAT, jSTAT2, jSTAT3);
    return pdcServicesAdded;
  }

  private boolean setAdminLevelServices(final String webContext,
      final JobManagerSettings jobManagerSettings) {
    JobManagerService jTools;
    JobManagerService jDesigner;
    JobManagerService jSTAT;
    boolean pdcServicesAdded = false;

    JobManagerService jdp = new JobManagerService("11", "JDP", LEVEL_OPERATION,
        webContext + getURL(CMP_JOBDOMAINPEAS, null, null) + MAIN_OWN_BODY_LAYOUT, null,
        false);
    JobManagerService jspp = new JobManagerService("12", "JSPP", LEVEL_OPERATION,
        webContext + getURL(CMP_JOBSTARTPAGEPEAS, null, null) + MAIN_OWN_BODY_LAYOUT, null,
        false);
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
        + "/admin/jsp/SilverLoggerAdmin.jsp", null, false);
    JobManagerService jindex =
        new JobManagerService("48", "reindexation", LEVEL_OPERATION, webContext
            + "/applicationIndexer/jsp/applicationIndexer.jsp", null, false);
    JobManagerService jcipher = new JobManagerService("49", "JCIPHER", LEVEL_OPERATION, webContext
        + "/admin/jsp/cipherkey.jsp", null, false);
    JobManagerService jabout = new JobManagerService("50", "JAB", LEVEL_OPERATION, webContext
        + "/silverpeasinfos.jsp", null, false);
    JobManagerService variables = new JobManagerService("51", "JDV", LEVEL_OPERATION, webContext
        + "/Rvariables/jsp/Main", null, false);
    JobManagerService wbe = new JobManagerService("53", "JWBE", LEVEL_OPERATION, webContext
        + "/Rwbe/jsp/Main", null, false);
    JobManagerService documentTemplate = new JobManagerService("54", "DOCUMENTTEMPLATE", LEVEL_OPERATION, webContext
        + "/RdocumentTemplates/jsp/Main", null, false);

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

    // l'administrateur à accès au tout
    String[] jDesignerFunctions = {jspp.getId(), jdp.getId(), jsp.getId()};
    jDesigner =
        new JobManagerService(DEFAULT_SERVICE_ID, "JD", LEVEL_SERVICE, null, jDesignerFunctions,
            false);

    String[] jSTATFunctions = {jSTAT1.getId(), jSTAT2.getId(), jSTAT3.getId(), jSTAT4.getId()};
    jSTAT = new JobManagerService("3", "JSTAT", LEVEL_SERVICE, null, jSTATFunctions, false);

    if (jobManagerSettings.isKMVisible()) {
      pdcServicesAdded = setJKMServices("2", jKM1, jKM2);
    }

    if (jobManagerSettings.isToolSpecificAuthentVisible() ||
        jobManagerSettings.isToolWorkflowDesignerVisible()) {

      List<String> ids = new ArrayList<>(10);
      if (jobManagerSettings.isToolSpecificAuthentVisible()) {
        setServicesWithIds(ids, jSpecificAuthent);
      }
      if (jobManagerSettings.isTemplateDesignerVisible()) {
        setServicesWithIds(ids, jtd);
      }
      if (jobManagerSettings.isToolWorkflowDesignerVisible()) {
        setServicesWithIds(ids, jWorkflowDesigner);
      }
      setServicesWithIds(ids, variables);
      setServicesWithIds(ids, jImportExport);
      if (jobManagerSettings.isPortletDeployerVisible()) {
        setServicesWithIds(ids, portletDeployer);
      }
      if (WbeSettings.isEnabled()) {
        setServicesWithIds(ids, wbe);
      }
      if (DocumentTemplateSettings.isEnabled()) {
        setServicesWithIds(ids, documentTemplate);
      }
      setServicesWithIds(ids, jst);
      setServicesWithIds(ids, jindex);
      setServicesWithIds(ids, jcipher);
      setServicesWithIds(ids, jabout);

      jTools = new JobManagerService("4", "JTOOLS", LEVEL_SERVICE, null,
          ids.toArray(new String[0]), false);
      setServices(jTools);
    }

    setServices(jDesigner, jdp, jspp, jsp, jSTAT, jSTAT1, jSTAT2, jSTAT3, jSTAT4);
    return pdcServicesAdded;
  }

  private boolean setJKMServices(final String id, final JobManagerService jKM1,
      final JobManagerService jKM2) {
    JobManagerService jKM;
    String[] jKMFunctions = {jKM1.getId(), jKM2.getId()};
    jKM = new JobManagerService(id, "JKM", LEVEL_SERVICE, null, jKMFunctions, false);
    setServices(jKM, jKM1, jKM2);
    return true;
  }

  private void setServices(JobManagerService... jobManagerServices) {
    for (JobManagerService service : jobManagerServices) {
      services.put(service.getId(), service);
    }
  }

  private void setServicesWithIds(final List<String> ids, JobManagerService... jobManagerServices) {
    for (JobManagerService service : jobManagerServices) {
      ids.add(service.getId());
      services.put(service.getId(), service);
    }
  }
  @Override
  public boolean isAccessGranted() {
    boolean accessGranted =
        getUserDetail().isAccessAdmin() || getUserManageableSpaceIds().length != 0;
    if (!accessGranted) {
      accessGranted =
          getUserDetail().isAccessDomainManager() || !getUserManageableGroupIds().isEmpty();
    }
    if (!accessGranted) {
      JobManagerSettings jobManagerSettings = JobManagerSettings.get();
      accessGranted = jobManagerSettings.isKMVisible() &&
          (getUserDetail().isAccessPdcManager() || isPDCManager());
    }
    return accessGranted;
  }

  private boolean isPDCManager() {
    boolean isPDCManager = false;

    try {
      isPDCManager = PdcManager.get().isUserManager(getUserId());
    } catch (PdcException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return isPDCManager;
  }

  // retourne les services de niveau level
  // 0 => niveau Service
  // 1 => opération
  // 2 => action
  // 3 => acteur
  public JobManagerService[] getServices(int level) {
    List<JobManagerService> listServices = new ArrayList<>(services.size());
    for (JobManagerService jms : services.values()) {
      if (jms.getLevel() == level) {
        listServices.add(jms);
      }
    }
    return listServices.toArray(new JobManagerService[0]);
  }

  public JobManagerService[] getSubServices(JobManagerService jmsParent) {
    String[] idSubServices = jmsParent.getIdSubServices();
    List<JobManagerService> listChild = new ArrayList<>(idSubServices.length);
    for (String idSubService : idSubServices) {
      JobManagerService jmsChild = services.get(idSubService);
      if (jmsChild != null) {
        listChild.add(jmsChild);
      }
    }
    return listChild.toArray(new JobManagerService[0]);
  }

  public JobManagerService[] getSubServices(String idService) {
    return this.getSubServices(services.get(idService));
  }

  public JobManagerService getService(String key) {
    return this.services.get(key);
  }

  public void changeServiceActif(String idNewService) {
    // mise a jour de l'id actif et de l'objet correspondant
    if (this.idCurrentServiceActif != null) {
      // si = null, nous sommes dans la
      // première initialisation de idCurrentServiceActif reset du flag actif du service courant
      services.get(this.idCurrentServiceActif).setActive(false);
    }
    //
    JobManagerService newService = services.get(idNewService);
    newService.setActive(true);
    this.idCurrentServiceActif = idNewService;

    // initialisation du idCurrentOperationActif avec l'opération active
    // correspondant au nouveau service
    JobManagerService[] jms = this.getSubServices(newService);
    idCurrentOperationActif = null;
    for (JobManagerService jm : jms) {
      if (jm.isActive()) {
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
      JobManagerService currentOperation = services.get(newService.getDefaultIdSubService());
      currentOperation.setActive(true);
      this.idCurrentOperationActif = newService.getDefaultIdSubService();
    }
  }

  public String getIdServiceActif() {
    return this.idCurrentServiceActif;
  }

  public String getIdOperationActif() {
    return this.idCurrentOperationActif;
  }

  public void changeOperationActif(String idNewOperation) {
    services.get(idCurrentOperationActif).setActive(false);
    services.get(idNewOperation).setActive(true);
    this.idCurrentOperationActif = idNewOperation;
  }

  public String getIdDefaultService() {
    return DEFAULT_SERVICE_ID;
  }

  public String getDirectAccessToSpaceId() {
    return directAccessToSpaceId;
  }

  public void setDirectAccessToSpaceId(final String directAccessToSpaceId) {
    this.directAccessToSpaceId = directAccessToSpaceId;
  }
}
