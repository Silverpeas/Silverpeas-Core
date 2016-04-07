/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.jobdomain.control;

import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionException;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.DomainServiceProvider;
import org.silverpeas.core.admin.domain.DomainType;
import org.silverpeas.core.admin.domain.exception.DomainConflictException;
import org.silverpeas.core.admin.domain.exception.DomainCreationException;
import org.silverpeas.core.admin.domain.exception.DomainDeletionException;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.domain.quota.UserDomainQuotaKey;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.exception.UtilTrappedException;
import org.silverpeas.core.security.authentication.password.service.PasswordCheck;
import org.silverpeas.core.security.authentication.password.service.PasswordRulesServiceProvider;
import org.silverpeas.core.security.encryption.X509Factory;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.csv.CSVReader;
import org.silverpeas.core.util.csv.Variant;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.web.jobdomain.DomainNavigationStock;
import org.silverpeas.web.jobdomain.GroupNavigationStock;
import org.silverpeas.web.jobdomain.JobDomainPeasDAO;
import org.silverpeas.web.jobdomain.JobDomainPeasException;
import org.silverpeas.web.jobdomain.JobDomainPeasTrappedException;
import org.silverpeas.web.jobdomain.JobDomainSettings;
import org.silverpeas.web.jobdomain.SynchroUserWebServiceItf;
import org.silverpeas.web.jobdomain.UserRequestData;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.*;

import static org.silverpeas.core.personalization.service.PersonalizationServiceProvider
    .getPersonalizationService;

/**
 * Class declaration
 *
 * @author
 */
public class JobDomainPeasSessionController extends AbstractComponentSessionController {

  String m_TargetUserId = null;
  String targetDomainId = "";
  DomainNavigationStock m_TargetDomain = null;
  Vector<GroupNavigationStock> m_GroupsPath = new Vector<GroupNavigationStock>();
  SynchroThread m_theThread = null;
  Exception m_ErrorOccured = null;
  String m_SynchroReport = "";
  Selection sel = null;
  List<UserDetail> usersToImport = null;
  Hashtable<String, String> queryToImport = null;
  AdminController m_AdminCtrl = null;
  private List<String> listSelectedUsers = new ArrayList<String>();
  // pagination de la liste des résultats
  private int indexOfFirstItemToDisplay = 0;
  boolean refreshDomain = true;

  private static final Properties templateConfiguration = new Properties();
  private static final String USER_ACCOUNT_TEMPLATE_FILE = "userAccount_email";

  /**
   * Standard Session Controller Constructeur
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public JobDomainPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle",
        "org.silverpeas.jobDomainPeas.settings.jobDomainPeasIcons",
        "org.silverpeas.jobDomainPeas.settings.jobDomainPeasSettings");
    setComponentRootName(URLUtil.CMP_JOBDOMAINPEAS);
    m_AdminCtrl = ServiceProvider.getService(AdminController.class);
    sel = getSelection();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, getSettings()
        .getString("templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, getSettings()
        .getString("customersTemplatePath"));
  }

  public int getMinLengthLogin() {
    return JobDomainSettings.m_MinLengthLogin;
  }

  public boolean isUserAddingAllowedForGroupManager() {
    return JobDomainSettings.m_UserAddingAllowedForGroupManagers;
  }

  public boolean isAccessGranted() {
    return !getUserManageableGroupIds().isEmpty() || getUserDetail().isAccessAdmin()
        || getUserDetail().
        isAccessDomainManager();
  }

  public void setRefreshDomain(boolean refreshDomain) {
    this.refreshDomain = refreshDomain;
  }

  /*
   * USER functions
   */
  public void setTargetUser(String userId) {
    m_TargetUserId = userId;
  }

  public UserDetail getTargetUserDetail() throws JobDomainPeasException {
    UserDetail valret = null;

    if ((m_TargetUserId != null) && (m_TargetUserId.length() > 0)) {
      valret = getOrganisationController().getUserDetail(m_TargetUserId);
      if (valret == null) {
        throw new JobDomainPeasException("JobDomainPeasSessionController.getTargetUserDetail()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_USER_NOT_AVAILABLE",
            "UserId=" + m_TargetUserId);
      }
    }
    return valret;
  }

  public UserFull getTargetUserFull() throws JobDomainPeasException {
    UserFull valret = null;

    if ((m_TargetUserId != null) && (m_TargetUserId.length() > 0)) {
      valret = getOrganisationController().getUserFull(m_TargetUserId);
      if (valret == null) {
        throw new JobDomainPeasException("JobDomainPeasSessionController.getTargetUserFull()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_USER_NOT_AVAILABLE", "UserId="
            + m_TargetUserId);
      }
    }
    return valret;
  }

  /**
   * Create a user
   *
   * @param userRequestData the data of the user from the request.
   * @param properties the user extra data.
   * @param req the current HttpServletRequest
   * @return
   * @throws JobDomainPeasException
   * @throws JobDomainPeasTrappedException
   */
  public String createUser(UserRequestData userRequestData, HashMap<String, String> properties,
      HttpServletRequest req) throws JobDomainPeasException, JobDomainPeasTrappedException {
    UserDetail theNewUser = new UserDetail();



    String existingUser =
        m_AdminCtrl.getUserIdByLoginAndDomain(userRequestData.getLogin(), targetDomainId);
    if ((existingUser != null) && (existingUser.length() > 0)) {
      JobDomainPeasTrappedException te = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.createUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_LOGIN_ALREADY_USED");
      te.setGoBackPage("displayUserCreate");
      throw te;
    }

    theNewUser.setId("-1");
    if ((targetDomainId != null) && (!targetDomainId.equals("-1")) && (targetDomainId.length() > 0)) {
      theNewUser.setDomainId(targetDomainId);
    }
    userRequestData.applyDataOnNewUser(theNewUser);
    String idRet = m_AdminCtrl.addUser(theNewUser);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.createUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_USER");
    }
    refresh();
    setTargetUser(idRet);
    theNewUser.setId(idRet);

    // Registering the preferred user language if any and if it is different from the default one
    if (StringUtil.isDefined(userRequestData.getLanguage()) &&
        !userRequestData.getLanguage().equals(DisplayI18NHelper.getDefaultLanguage())) {
      UserPreferences userPreferences = theNewUser.getUserPreferences();
      userPreferences.setLanguage(userRequestData.getLanguage());
      getPersonalizationService().saveUserSettings(userPreferences);
    }

    // Send an email to alert this user
    notifyUserAccount(userRequestData, theNewUser, req, true);

    // Update UserFull informations
    UserFull uf = getTargetUserFull();
    if (uf != null) {
      if (uf.isPasswordAvailable()) {
        uf.setPasswordValid(userRequestData.isPasswordValid());
        uf.setPassword(userRequestData.getPassword());
      }

      // process extra properties
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        uf.setValue(entry.getKey(), entry.getValue());
      }

      try {
        idRet = m_AdminCtrl.updateUserFull(uf);
      } catch (AdminException e) {
        throw new JobDomainPeasException("JobDomainPeasSessionController.createUser()",
            SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", e);
      }
    }
    // regroupement de l'utilisateur dans un groupe
    regroupInGroup(properties, null);
    // If group is provided, add newly created user to it
    if (StringUtil.isDefined(userRequestData.getGroupId())) {
      m_AdminCtrl.addUserInGroup(idRet, userRequestData.getGroupId());
    }

    return idRet;
  }

  /**
   * notifyUserAccount send an email to the user only if userPasswordValid, sendEmail are true, and
   * if userEMail and userPassword are defined
   *
   * @param userRequestData the data of the user from the request.
   * @param user the userDetail
   * @param req the current HttpServletRequest
   * @param isNewUser boolean true if it's a created user, false else if
   */
  private void notifyUserAccount(UserRequestData userRequestData, UserDetail user,
      HttpServletRequest req, boolean isNewUser) {

    // Add code here in order to send an email notification
    if (userRequestData.isPasswordValid() && userRequestData.isSendEmail() &&
        StringUtil.isDefined(user.geteMail()) &&
        StringUtil.isDefined(userRequestData.getPassword())) {

      // Send an email notification
      Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();

      NotificationMetaData notifMetaData =
          new NotificationMetaData(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, "", templates,
              USER_ACCOUNT_TEMPLATE_FILE);

      String loginUrl = getLoginUrl(user, req);

      for (String lang : DisplayI18NHelper.getLanguages()) {
        LocalizationBundle notifBundle = ResourceLocator.getLocalizationBundle(
            "org.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle", lang);
        notifMetaData.addLanguage(lang, notifBundle.getString("JDP.createAccountNotifTitle"), "");
        templates.put(lang, getTemplate(user, loginUrl, userRequestData.getPassword(), isNewUser));
      }

      notifMetaData.addUserRecipient(new UserRecipient(user.getId()));
      NotificationSender sender = new NotificationSender(null);
      try {
        sender.notifyUser(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, notifMetaData);
      } catch (NotificationManagerException e) {
        SilverTrace.error("JobDomainPeasSessionController", "notifyNewUserAccount",
            "admin.MSG_ERR_NOTIFY_USER", e);
      }
    }
  }

  /**
   * Retrieve the login URL
   *
   * @param user the user detail (UserDetail)
   * @param req the current HttpServletRequest
   * @return the login URL string representation
   */
  private String getLoginUrl(UserDetail user, HttpServletRequest req) {
    SettingBundle general =
        ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
    String loginPage = general.getString("loginPage");
    if (!StringUtil.isDefined(loginPage)) {
      loginPage = "/defaultLogin.jsp";
      String domainId = user.getDomainId();
      if (StringUtil.isDefined(domainId) && !"-1".equals(domainId)) {
        loginPage += "?DomainId=" + domainId;
      }
    }
    return URLUtil.getFullApplicationURL(req) + loginPage;
  }

  /**
   * Return the silverpeas template email configuration
   *
   * @param userDetail the current user detail
   * @param loginURL the login URL String
   * @param userPassword the current user password we have to send to new/modified user
   * @param isNew true if it's a created user, false else if
   * @return a SilverpeasTemplate
   */
  private SilverpeasTemplate getTemplate(UserDetail userDetail, String loginURL,
      String userPassword, boolean isNew) {
    Properties configuration = new Properties(templateConfiguration);
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplate(configuration);
    template.setAttribute("userDetail", userDetail);
    template.setAttribute("loginURL", loginURL);
    template.setAttribute("pwd", userPassword);
    if (isNew) {
      template.setAttribute("createdUser", "true");
    }
    return template;
  }

  /**
   * Regroupement éventuel de l'utilisateur dans un groupe (pour les domaines SQL)
   *
   * @throws JobDomainPeasException
   */
  private void regroupInGroup(HashMap<String, String> properties, String lastGroupId)
      throws JobDomainPeasException {

    // Traitement du domaine SQL
    if (!"-1".equals(getTargetDomain().getId())
        && !"0".equals(getTargetDomain().getId())
        && getTargetDomain().getDriverClassName().equals(
            "org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver")) {

      SettingBundle specificRs =
          ResourceLocator.getSettingBundle(getTargetDomain().getPropFileName());
      int numPropertyRegroup = specificRs.getInteger("property.Grouping", -1);
      String nomRegroup = null;
      String theUserIdToRegroup = m_TargetUserId;
      String[] newUserIds;
      List<String> lUserIds;
      List<String> lNewUserIds;
      if (numPropertyRegroup > -1) {
        String nomPropertyRegroupement = specificRs.getString("property_" + numPropertyRegroup
            + ".Name", null);

        if (nomPropertyRegroupement != null) {
          // Suppression de l'appartenance de l'utilisateur au groupe auquel il
          // appartenait
          if (lastGroupId != null) {
            Group lastGroup = m_AdminCtrl.getGroupById(lastGroupId);
            lUserIds = Arrays.asList(lastGroup.getUserIds());
            lNewUserIds = new ArrayList<String>(lUserIds);
            lNewUserIds.remove(theUserIdToRegroup);
            newUserIds = lNewUserIds.toArray(new String[lNewUserIds.size()]);
            updateGroupSubUsers(lastGroupId, newUserIds);
          }

          // Recherche du nom du regroupement (nom du groupe)
          String value = null;
          boolean trouve = false;
          for (Map.Entry<String, String> entry : properties.entrySet()) {
            value = entry.getValue();
            if (entry.getKey().equals(nomPropertyRegroupement)) {
              trouve = true;
              break;
            }
          }

          if (trouve) {
            nomRegroup = value;
          }
        }
      }

      if (nomRegroup != null && nomRegroup.length() > 0) {
        // Recherche le groupe dans le domaine
        Group group = m_AdminCtrl.getGroupByNameInDomain(nomRegroup, targetDomainId);
        if (group == null) {
          // le groupe n'existe pas, on le crée
          group = new Group();
          group.setId("-1");
          group.setDomainId(targetDomainId);
          group.setSuperGroupId(null); // groupe à la racine
          group.setName(nomRegroup);
          group.setDescription("");
          String groupId = m_AdminCtrl.addGroup(group);
          group = m_AdminCtrl.getGroupById(groupId);
        }

        lUserIds = Arrays.asList(group.getUserIds());
        lNewUserIds = new ArrayList<String>(lUserIds);
        lNewUserIds.add(theUserIdToRegroup);
        newUserIds = lNewUserIds.toArray(new String[lNewUserIds.size()]);

        // Ajout de l'appartenance de l'utilisateur au groupe
        updateGroupSubUsers(group.getId(), newUserIds);
      }
    }
  }

  /**
   * Parse the CSV file.
   *
   * @param filePart
   * @param req the current HttpServletRequest
   * @throws UtilTrappedException
   * @throws JobDomainPeasTrappedException
   * @throws JobDomainPeasException
   */
  public void importCsvUsers(FileItem filePart, boolean sendEmail, HttpServletRequest req)
      throws UtilTrappedException, JobDomainPeasTrappedException, JobDomainPeasException {
    InputStream is;
    try {
      is = filePart.getInputStream();
    } catch (IOException e) {
      JobDomainPeasTrappedException jdpe = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.importCsvUsers",
          SilverpeasException.ERROR, "jobDomainPeas.EX_CSV_FILE", e);
      jdpe.setGoBackPage("displayUsersCsvImport");
      throw jdpe;
    }
    CSVReader csvReader = new CSVReader(getLanguage());
    csvReader.initCSVFormat("org.silverpeas.jobDomainPeas.settings.usersCSVFormat", "User", ";",
        getTargetDomain().getPropFileName(), "property");

    // spécifique domaine Silverpeas (2 colonnes en moins (password et
    // passwordValid)
    if ("-1".equals(getTargetDomain().getId())
        || "0".equals(getTargetDomain().getId())) {// domaine Silverpeas
      csvReader.setSpecificNbCols(csvReader.getSpecificNbCols() - 2);
    }

    Variant[][] csvValues;
    try {
      csvValues = csvReader.parseStream(is);
    } catch (UtilTrappedException ute) {
      ute.setGoBackPage("displayUsersCsvImport");
      throw ute;
    }

    StringBuilder listErrors = new StringBuilder("");
    String nom;
    String prenom;
    String login;
    String existingLogin;
    String email;
    String droits;
    UserAccessLevel userAccessLevel;
    String motDePasse;

    String title;
    String company;
    String position;
    String boss;
    String phone;
    String homePhone;
    String fax;
    String cellularPhone;
    String address;

    String informationSpecifiqueString;
    boolean informationSpecifiqueBoolean;

    for (int i = 0; i < csvValues.length; i++) {
      // Nom
      nom = csvValues[i][0].getValueString();
      if (nom.length() == 0) {// champ obligatoire
        listErrors.append(getErrorMessage(i + 1, 1, nom));
        listErrors.append(getString("JDP.obligatoire")).append("<br/>");
      } else if (nom.length() > 100) {// verifier 100 char max
        listErrors.append(getErrorMessage(i + 1, 1, nom));
        listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").
            append(getString("JDP.caracteres")).append("<br/>");
      }

      // Prenom
      prenom = csvValues[i][1].getValueString(); // verifier 100 char max
      if (prenom.length() > 100) {
        listErrors.append(getErrorMessage(i + 1, 2, prenom));
        listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(
            getString("JDP.caracteres")).append("<br/>");
      }

      // Login
      login = csvValues[i][2].getValueString();
      if (login.length() == 0) {// champ obligatoire
        listErrors.append(getErrorMessage(i + 1, 3, login));
        listErrors.append(getString("JDP.obligatoire")).append("<br/>");
      } else if (login.length() < JobDomainSettings.m_MinLengthLogin) {// verifier
        listErrors.append(getErrorMessage(i + 1, 3, login));
        listErrors.append(getString("JDP.nbCarMin")).append(" ").append(
            JobDomainSettings.m_MinLengthLogin).append(" ").append(getString("JDP.caracteres")).
            append("<br/>");
      } else if (login.length() > 50) {// verifier 50 char max
        listErrors.append(getErrorMessage(i + 1, 3, login));
        listErrors.append(getString("JDP.nbCarMax")).append(" 50 ").append(getString(
            "JDP.caracteres")).append("<br/>");
      } else {// verif login unique
        existingLogin = m_AdminCtrl.getUserIdByLoginAndDomain(login,
            targetDomainId);
        if (existingLogin != null) {
          listErrors.append(getErrorMessage(i + 1, 3, login));
          listErrors.append(getString("JDP.existingLogin")).append("<br/>");
        }
      }

      // Email
      email = csvValues[i][3].getValueString(); // verifier 100 char max
      if (email.length() > 100) {
        listErrors.append(getErrorMessage(i + 1, 4, email));
        listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(getString(
            "JDP.caracteres")).append("<br/>");
      }

      // Droits
      droits = csvValues[i][4].getValueString();
      if (!"".equals(droits) && !"Admin".equals(droits)
          && !"AdminPdc".equals(droits) && !"AdminDomain".equals(droits)
          && !"User".equals(droits) && !"Guest".equals(droits)) {
        listErrors.append(getErrorMessage(i + 1, 5, droits));
        listErrors.append(getString("JDP.valeursPossibles")).append("<br/>");
      }

      // MotDePasse
      motDePasse = csvValues[i][5].getValueString();
      // password is not mandatory
      if (StringUtil.isDefined(motDePasse)) {
        // Cheking password
        PasswordCheck passwordCheck = PasswordRulesServiceProvider.getPasswordRulesService().check(motDePasse);
        if (!passwordCheck.isCorrect()) {
          listErrors.append(getErrorMessage(i + 1, 6, motDePasse))
              .append(passwordCheck.getFormattedErrorMessage(getLanguage()));
          listErrors.append("<br/>");
        } else if (motDePasse.length() > 32) {// verifier 32 char max
          listErrors.append(getErrorMessage(i + 1, 6, motDePasse));
          listErrors.append(getString("JDP.nbCarMax")).append(" 32 ").append(getString(
              "JDP.caracteres")).append("<br/>");
        }
      }

      if (csvReader.getSpecificNbCols() > 0) {
        if ("-1".equals(getTargetDomain().getId())
            || "0".equals(getTargetDomain().getId())) {// domaine Silverpeas

          // title
          title = csvValues[i][6].getValueString(); // verifier 100 char max
          if (title.length() > 100) {
            listErrors.append(getErrorMessage(i + 1, 7, title));
            listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(getString(
                "JDP.caracteres")).append("<br/>");
          }

          // company
          company = csvValues[i][7].getValueString(); // verifier 100 char max
          if (company.length() > 100) {
            listErrors.append(getErrorMessage(i + 1, 8, company));
            listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(getString(
                "JDP.caracteres")).append("<br/>");
          }

          // position
          position = csvValues[i][8].getValueString(); // verifier 100 char max
          if (position.length() > 100) {
            listErrors.append(getErrorMessage(i + 1, 9, position));
            listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(getString(
                "JDP.caracteres")).append("<br/>");
          }

          // boss
          boss = csvValues[i][9].getValueString(); // verifier 100 char max
          if (boss.length() > 100) {
            listErrors.append(getErrorMessage(i + 1, 10, boss));
            listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(getString(
                "JDP.caracteres")).append("<br/>");
          }

          // phone
          phone = csvValues[i][10].getValueString(); // verifier 20 char max
          if (phone.length() > 20) {
            listErrors.append(getErrorMessage(i + 1, 11, phone));
            listErrors.append(getString("JDP.nbCarMax")).append(" 20 ").append(getString(
                "JDP.caracteres")).append("<br/>");
          }

          // homePhone
          homePhone = csvValues[i][11].getValueString(); // verifier 20 char max
          if (homePhone.length() > 20) {
            listErrors.append(getErrorMessage(i + 1, 12, homePhone));
            listErrors.append(getString("JDP.nbCarMax")).append(" 20 ").append(getString(
                "JDP.caracteres")).append("<br/>");
          }

          // fax
          fax = csvValues[i][12].getValueString(); // verifier 20 char max
          if (fax.length() > 20) {
            listErrors.append(getErrorMessage(i + 1, 13, fax));
            listErrors.append(getString("JDP.nbCarMax")).append(" 20 ").append(getString(
                "JDP.caracteres")).append("<br/>");
          }

          // cellularPhone
          cellularPhone = csvValues[i][13].getValueString(); // verifier 20 char
          // max
          if (cellularPhone.length() > 20) {
            listErrors.append(getErrorMessage(i + 1, 14, cellularPhone));
            listErrors.append(getString("JDP.nbCarMax")).append(" 20 ").append(getString(
                "JDP.caracteres")).append("<br/>");
          }

          // address
          address = csvValues[i][14].getValueString(); // verifier 500 char max
          if (address.length() > 500) {
            listErrors.append(getErrorMessage(i + 1, 15, address));
            listErrors.append(getString("JDP.nbCarMax")).append(" 500 ").append(getString(
                "JDP.caracteres")).append("<br/>");
          }
        } else {// domaine SQL

          for (int j = 0; j < csvReader.getSpecificNbCols(); j++) {
            if (Variant.TYPE_STRING.equals(csvReader.getSpecificColType(j))) {
              informationSpecifiqueString = csvValues[i][j + 6].getValueString();
              // verify the length
              if (informationSpecifiqueString.length() > csvReader.getSpecificColMaxLength(j)) {
                listErrors.append(getErrorMessage(i + 1, j + 6, informationSpecifiqueString));
                listErrors.append(getString("JDP.nbCarMax")).append(" ")
                    .append(csvReader.getSpecificColMaxLength(j)).append(" ")
                    .append(getString("JDP.caracteres")).append("<br/>");
              }
            }
          }
        }
      }
    }

    if (listErrors.length() > 0) {
      JobDomainPeasTrappedException jdpe = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.importCsvUsers",
          SilverpeasException.ERROR, "jobDomainPeas.EX_CSV_FILE", listErrors.toString());
      jdpe.setGoBackPage("displayUsersCsvImport");
      throw jdpe;
    }

    // pas d'erreur, on importe les utilisateurs
    HashMap<String, String> properties;
    for (Variant[] csvValue : csvValues) {
      // Nom
      nom = csvValue[0].getValueString();

      // Prenom
      prenom = csvValue[1].getValueString();

      // Login
      login = csvValue[2].getValueString();

      // Email
      email = csvValue[3].getValueString();

      // Droits
      droits = csvValue[4].getValueString();
      if ("Admin".equals(droits)) {
        userAccessLevel = UserAccessLevel.ADMINISTRATOR;
      } else if ("AdminPdc".equals(droits)) {
        userAccessLevel = UserAccessLevel.PDC_MANAGER;
      } else if ("AdminDomain".equals(droits)) {
        userAccessLevel = UserAccessLevel.DOMAIN_ADMINISTRATOR;
      } else if ("User".equals(droits)) {
        userAccessLevel = UserAccessLevel.USER;
      } else if ("Guest".equals(droits)) {
        userAccessLevel = UserAccessLevel.GUEST;
      } else {
        userAccessLevel = UserAccessLevel.USER;
      }

      // MotDePasse
      motDePasse = csvValue[5].getValueString();

      // données spécifiques
      properties = new HashMap<String, String>();
      if (csvReader.getSpecificNbCols() > 0) {
        if ("-1".equals(getTargetDomain().getId())
            || "0".equals(getTargetDomain().getId())) {// domaine Silverpeas

          // title
          title = csvValue[6].getValueString();
          properties.put(csvReader.getSpecificParameterName(0), title);

          // company
          company = csvValue[7].getValueString();
          properties.put(csvReader.getSpecificParameterName(1), company);

          // position
          position = csvValue[8].getValueString();
          properties.put(csvReader.getSpecificParameterName(2), position);

          // boss
          boss = csvValue[9].getValueString();
          properties.put(csvReader.getSpecificParameterName(3), boss);

          // phone
          phone = csvValue[10].getValueString();
          properties.put(csvReader.getSpecificParameterName(4), phone);

          // homePhone
          homePhone = csvValue[11].getValueString();
          properties.put(csvReader.getSpecificParameterName(5), homePhone);

          // fax
          fax = csvValue[12].getValueString();
          properties.put(csvReader.getSpecificParameterName(6), fax);

          // cellularPhone
          cellularPhone = csvValue[13].getValueString();
          properties.put(csvReader.getSpecificParameterName(7), cellularPhone);

          // address
          address = csvValue[14].getValueString();
          properties.put(csvReader.getSpecificParameterName(8), address);

        } else {// domaine SQL

          // informations spécifiques
          for (int j = 0; j < csvReader.getSpecificNbCols(); j++) {
            if (Variant.TYPE_STRING.equals(csvReader.getSpecificColType(j))) {
              informationSpecifiqueString = csvValue[j + 6].getValueString();
              properties.put(csvReader.getSpecificParameterName(j),
                  informationSpecifiqueString);
            } else if (Variant.TYPE_BOOLEAN.equals(csvReader.getSpecificColType(j))) {
              informationSpecifiqueBoolean = csvValue[j + 6].getValueBoolean();
              if (informationSpecifiqueBoolean) {
                properties.put(csvReader.getSpecificParameterName(j), "1");
              } else {
                properties.put(csvReader.getSpecificParameterName(j), "0");
              }
            }
          }
        }
      }

      boolean passwordValid = StringUtil.isDefined(motDePasse); // password is not mandatory
      UserRequestData userRequestData = new UserRequestData();
      userRequestData.setLogin(login);
      userRequestData.setLastName(nom);
      userRequestData.setFirstName(prenom);
      userRequestData.setEmail(email);
      userRequestData.setAccessLevel(userAccessLevel);
      userRequestData.setPasswordValid(passwordValid);
      userRequestData.setPassword(motDePasse);
      userRequestData.setSendEmail(sendEmail);
      userRequestData.setUserManualNotifReceiverLimitEnabled(true);
      createUser(userRequestData, properties, req);
    }
  }

  private String getErrorMessage(int line, int column, String value) {
    StringBuilder str = new StringBuilder();
    str.append(getString("JDP.ligne")).append(" = ").append(line).append(", ");
    str.append(getString("JDP.colonne")).append(" = ").append(column).append(", ");
    str.append(getString("JDP.valeur")).append(" = ").append(StringUtil.truncate(value, 100))
        .append(", ");
    return str.toString();
  }

  private String getLastGroupId(UserFull theUser) {
    // Traitement du domaine SQL
    if (!"-1".equals(getTargetDomain().getId())
        && !"0".equals(getTargetDomain().getId())
        && getTargetDomain().getDriverClassName().equals(
            "org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver")) {
      SettingBundle specificRs =
          ResourceLocator.getSettingBundle(getTargetDomain().getPropFileName());
      int numPropertyRegroup = specificRs.getInteger("property.Grouping", -1);
      String nomLastGroup = null;
      if (numPropertyRegroup > -1) {
        String nomPropertyRegroupement = specificRs.getString("property_" + numPropertyRegroup
            + ".Name", null);
        if (nomPropertyRegroupement != null) {
          // Recherche du nom du regroupement (nom du groupe)
          String value = null;
          boolean trouve = false;
          for (String key : theUser.getPropertiesNames()) {
            value = theUser.getValue(key);

            if (key.equals(nomPropertyRegroupement)) {
              trouve = true;
              break;
            }
          }

          if (trouve) {
            nomLastGroup = value;
          }
        }
      }

      if (nomLastGroup != null && nomLastGroup.length() > 0) {
        // Recherche le groupe dans le domaine
        Group group = m_AdminCtrl.getGroupByNameInDomain(nomLastGroup,
            targetDomainId);
        if (group != null) {
          return group.getId();
        }
      }
    }

    return null;
  }

  /**
   * Modify user account information
   *
   * @param userRequestData the data of the user from the request.
   * @param properties the user extra data.
   * @param req the current HttpServletRequest
   * @throws JobDomainPeasException
   */
  public void modifyUser(UserRequestData userRequestData, HashMap<String, String> properties,
      HttpServletRequest req)
      throws JobDomainPeasException {


    UserFull theModifiedUser = m_AdminCtrl.getUserFull(userRequestData.getId());
    if (theModifiedUser == null) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER");
    }

    // nom du groupe auquel était rattaché l'utilisateur
    String lastGroupId = getLastGroupId(theModifiedUser);

    userRequestData.applyDataOnExistingUser(theModifiedUser);

    notifyUserAccount(userRequestData, theModifiedUser, req, false);

    // process extra properties
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      theModifiedUser.setValue(entry.getKey(), entry.getValue());
    }

    String idRet;
    try {
      idRet = m_AdminCtrl.updateUserFull(theModifiedUser);
    } catch (AdminException e) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER",
          "UserId=" + userRequestData.getId(), e);
    }
    refresh();
    setTargetUser(idRet);

    // regroupement de l'utilisateur dans un groupe
    regroupInGroup(properties, lastGroupId);
  }

  public void modifySynchronizedUser(UserRequestData userRequestData)
      throws JobDomainPeasException {


    UserDetail theModifiedUser = m_AdminCtrl.getUserDetail(userRequestData.getId());
    if (theModifiedUser == null) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifySynchronizedUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER");
    }
    theModifiedUser.setAccessLevel(userRequestData.getAccessLevel());
    theModifiedUser.setUserManualNotificationUserReceiverLimit(
        userRequestData.getUserManualNotifReceiverLimitValue());
    String idRet = m_AdminCtrl.updateSynchronizedUser(theModifiedUser);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifySynchronizedUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "UserId="
          + userRequestData.getId());
    }
    refresh();
    setTargetUser(idRet);
  }

  public void modifyUserFull(UserRequestData userRequestData,
      HashMap<String, String> properties)
      throws JobDomainPeasException {
    UserFull theModifiedUser = m_AdminCtrl.getUserFull(userRequestData.getId());
    if (theModifiedUser == null) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifyUserFull()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER");
    }

    theModifiedUser.setAccessLevel(userRequestData.getAccessLevel());
    theModifiedUser.setUserManualNotificationUserReceiverLimit(
        userRequestData.getUserManualNotifReceiverLimitValue());

    // process extra properties
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      theModifiedUser.setValue(entry.getKey(), entry.getValue());
    }

    String idRet;
    try {
      idRet = m_AdminCtrl.updateUserFull(theModifiedUser);
    } catch (AdminException e) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.modifyUserFull()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER",
          "UserId=" + userRequestData.getId(), e);
    }
    refresh();
    setTargetUser(idRet);
  }

  public void blockUser(String userId) throws JobDomainPeasException {

    m_AdminCtrl.blockUser(userId);
  }

  public void unblockUser(String userId) throws JobDomainPeasException {
    m_AdminCtrl.unblockUser(userId);
  }

  public void deactivateUser(String userId) throws JobDomainPeasException {

    m_AdminCtrl.deactivateUser(userId);
  }

  public void activateUser(String userId) throws JobDomainPeasException {
    m_AdminCtrl.activateUser(userId);
  }

  public void deleteUser(String idUser) throws JobDomainPeasException {

    UserDetail user = getUserDetail(idUser);

    boolean deleteUser = true;

    // TODO : Manage deleting case for group manager
    if (!UserAccessLevel.ADMINISTRATOR.equals(getUserAccessLevel())
        && !UserAccessLevel.DOMAIN_ADMINISTRATOR.equals(getUserAccessLevel()) && isGroupManager()) {
      List<String> directGroupIds = Arrays.asList(getOrganisationController().
          getDirectGroupIdsOfUser(idUser));
      List<String> manageableGroupIds = getUserManageableGroupIds();

      String directGroupId;
      String rootGroupId;
      List<String> groupIdLinksToRemove = new ArrayList<String>();
      for (String directGroupId1 : directGroupIds) {
        directGroupId = directGroupId1;

        // get root group of each directGroup
        List<String> groupPath = m_AdminCtrl.getPathToGroup(directGroupId);
        if (groupPath != null && groupPath.size() > 0) {
          rootGroupId = groupPath.get(0);
        } else {
          rootGroupId = directGroupId;
        }

        // if root group is not one of manageable group, avoid deletion
        // user belongs to another community
        if (!manageableGroupIds.contains(rootGroupId)) {
          deleteUser = false;
        } else {
          groupIdLinksToRemove.add(directGroupId);
        }
      }

      if (!deleteUser) {
        // removes only links between user and manageable groups
        for (String groupIdLinkToRemove : groupIdLinksToRemove) {
          m_AdminCtrl.removeUserFromGroup(idUser, groupIdLinkToRemove);
        }

        refresh();
      }
    }

    if (deleteUser) {
      String idRet = m_AdminCtrl.deleteUser(idUser);
      if (!StringUtil.isDefined(idRet)) {
        throw new JobDomainPeasException("JobDomainPeasSessionController.deleteUser()",
            SilverpeasException.ERROR, "admin.EX_ERR_DELETE_USER", "UserId="
            + idUser);
      }
      if (m_TargetUserId.equals(idUser)) {
        m_TargetUserId = null;
      }

      if ((getDomainActions() & DomainDriver.ACTION_X509_USER) != 0) {
        // revocate user's certificate
        revocateCertificate(user);
      }

      refresh();
    }
  }

  public Iterator<DomainProperty> getPropertiesToImport() throws JobDomainPeasException {
    return m_AdminCtrl.getSpecificPropertiesToImportUsers(targetDomainId,
        getLanguage()).iterator();
  }

  public void importUser(String userLogin) throws JobDomainPeasException {



    String idRet = m_AdminCtrl.synchronizeImportUser(targetDomainId, userLogin);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.importUser()",
          SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_USER",
          "userLogin=" + userLogin);
    }
    refresh();
    setTargetUser(idRet);
  }

  public void importUsers(String[] specificIds) throws JobDomainPeasException {
    for (int i = 0; specificIds != null && i < specificIds.length; i++) {
      m_AdminCtrl.synchronizeImportUser(targetDomainId, specificIds[i]);
    }
    refresh();
  }

  public List<UserDetail> searchUsers(Hashtable<String, String> query)
      throws JobDomainPeasException {

    queryToImport = query;
    usersToImport = m_AdminCtrl.searchUsers(targetDomainId, query);
    return usersToImport;
  }

  public List<UserDetail> getUsersToImport() {
    return usersToImport;
  }

  public Hashtable<String, String> getQueryToImport() {
    return queryToImport;
  }

  public UserFull getUser(String specificId) {
    return m_AdminCtrl.getUserFull(targetDomainId, specificId);
  }

  public void synchroUser(String idUser) throws JobDomainPeasException {

    String idRet = m_AdminCtrl.synchronizeUser(idUser);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.synchroUser()",
          SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_USER");
    }
    refresh();
    setTargetUser(idRet);
  }

  public void unsynchroUser(String idUser) throws JobDomainPeasException {


    String idRet = m_AdminCtrl.synchronizeRemoveUser(idUser);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.unsynchroUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_USER");
    }
    if (m_TargetUserId.equals(idUser)) {
      m_TargetUserId = null;
    }
    refresh();
  }

  /*
   * GROUP functions
   */
  public void returnIntoGroup(String groupId) throws JobDomainPeasException {
    if (!StringUtil.isDefined(groupId)) {
      m_GroupsPath.clear();
    } else {
      int i = m_GroupsPath.size() - 1;
      while ((i >= 0)
          && (!m_GroupsPath.get(i).isThisGroup(groupId))) {
        m_GroupsPath.removeElementAt(i);
        i--;
      }
    }
    setTargetUser(null);
  }

  public void removeGroupFromPath(String groupId) throws JobDomainPeasException {
    if (StringUtil.isDefined(groupId)) {
      int i = 0;
      while ((i < m_GroupsPath.size())
          && (!m_GroupsPath.get(i).isThisGroup(groupId))) {
        i++;
      }
      if (i < m_GroupsPath.size()) {
        m_GroupsPath.setSize(i); // Tunc the vector
      }
    }
  }

  /**
   * @param groupId
   * @throws JobDomainPeasException
   */
  public void goIntoGroup(String groupId) throws JobDomainPeasException {
    if (StringUtil.isDefined(groupId)) {
      if (getTargetGroup() == null
          || (getTargetGroup() != null && !getTargetGroup().getId().equals(
              groupId))) {
        Group targetGroup = m_AdminCtrl.getGroupById(groupId);
        // Add user access control for security purpose
        if (UserAccessLevel.ADMINISTRATOR.equals(getUserAccessLevel()) ||
            m_AdminCtrl.isDomainManagerUser(getUserId(), targetGroup.getDomainId()) ||
            isGroupManagerOnGroup(groupId)) {
          if (GroupNavigationStock.isGroupValid(targetGroup)) {
            List<String> manageableGroupIds = null;
            if (isOnlyGroupManager() && !isGroupManagerOnGroup(groupId)) {
              manageableGroupIds = getUserManageableGroupIds();
            }
            GroupNavigationStock newSubGroup = new GroupNavigationStock(groupId,
                m_AdminCtrl, manageableGroupIds);
            m_GroupsPath.add(newSubGroup);
          }
        } else {
          SilverTrace.warn("jobDomainPeas", "JobDomainPeasSessionController.goIntoGroup()",
              "Security alert", "user id=" + getUserId() + " is trying to access group " + groupId);
        }
      }
    } else {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.setTargetGroup()",
          SilverpeasException.ERROR, "jobDomainPeas.EX_GROUP_NOT_AVAILABLE",
          "GroupId=" + groupId);
    }
    setTargetUser(null);
  }

  public Group getTargetGroup() throws JobDomainPeasException {
    if (m_GroupsPath.size() <= 0) {
      return null;
    }
    return m_GroupsPath.lastElement().getThisGroup();
  }

  /**
   * @return a List with 2 elements. First one, a List of UserDetail. Last one, a List of Group.
   * @throws JobDomainPeasException
   */
  public List<List> getGroupManagers() throws JobDomainPeasException {
    List<List> usersAndGroups = new ArrayList<List>();
    List<UserDetail> users = new ArrayList<UserDetail>();
    List<Group> groups = new ArrayList<Group>();

    GroupProfileInst profile = m_AdminCtrl.getGroupProfile(getTargetGroup().getId());
    if (profile != null) {
      for (String groupId : profile.getAllGroups()) {
        groups.add(m_AdminCtrl.getGroupById(groupId));
      }
      for (String userId : profile.getAllUsers()) {
        users.add(getUserDetail(userId));
      }
    }
    usersAndGroups.add(users);
    usersAndGroups.add(groups);
    return usersAndGroups;
  }

  // user panel de selection de n groupes et n users
  public void initUserPanelForGroupManagers(String compoURL, List<String> userIds,
      List<String> groupIds) throws SelectionException, JobDomainPeasException {
    sel.resetAll();
    sel.setHostSpaceName(getMultilang().getString("JDP.jobDomain"));
    sel.setHostComponentName(new Pair<>(getTargetGroup().getName(), null));
    LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(getLanguage());
    Pair<String, String>[] hostPath =
        new Pair[]{new Pair<>(getMultilang().getString("JDP.roleManager") + " > " +
            generalMessage.getString("GML.selection"), null)};
    sel.setHostPath(hostPath);
    //sel.setGoBackURL(compoURL + "groupManagersUpdate");
    //sel.setCancelURL(compoURL + "groupManagersCancel");
    setDomainIdOnSelection(sel);
    sel.setPopupMode(true);
    sel.setHtmlFormElementId("roleItems");
    sel.setHtmlFormName("dummy");
    sel.setSelectedElements(userIds);
    sel.setSelectedSets(groupIds);
  }

  public void updateGroupProfile(List<String> userIds, List<String> groupIds)
      throws JobDomainPeasException {
    GroupProfileInst profile = m_AdminCtrl.getGroupProfile(getTargetGroup().getId());
    profile.setUsers(userIds);
    profile.setGroups(groupIds);
    m_AdminCtrl.updateGroupProfile(profile);
  }

  public boolean isGroupRoot(String groupId) throws JobDomainPeasException {
    Group gr = m_AdminCtrl.getGroupById(groupId);
    return GroupNavigationStock.isGroupValid(gr) && this.refreshDomain && (!StringUtil.isDefined(gr.
        getSuperGroupId()) || "-1".equals(gr.getSuperGroupId()));
  }

  public Group[] getSubGroups(boolean isParentGroup)
      throws JobDomainPeasException {
    Group[] groups;

    if (isParentGroup) {
      if (m_GroupsPath.size() <= 0) {
        throw new JobDomainPeasException("JobDomainPeasSessionController.getTargetGroup()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_GROUP_NOT_AVAILABLE");
      }
      groups = m_GroupsPath.lastElement().getGroupPage();
    } else { // Domain case
      groups = m_TargetDomain.getGroupPage();
    }
    if (isOnlyGroupManager() && !isGroupManagerOnCurrentGroup()) {
      groups = filterGroupsToGroupManager(groups);
    }
    return groups;
  }

  public List<UserDetail> getSubUsers(boolean isParentGroup)
      throws JobDomainPeasException {
    final UserDetail[] usDetails;
    if (isParentGroup) {
      if (m_GroupsPath.isEmpty()) {
        throw new JobDomainPeasException("JobDomainPeasSessionController.getTargetGroup()",
            SilverpeasException.ERROR, "jobDomainPeas.EX_GROUP_NOT_AVAILABLE");
      }
      usDetails = m_GroupsPath.lastElement().getUserPage();
    } else { // Domain case
      usDetails = m_TargetDomain.getUserPage();
    }
    return Arrays.asList(usDetails);
  }

  public String getPath(String baseURL, String toAppendAtEnd)
      throws JobDomainPeasException {
    StringBuilder strPath = new StringBuilder("");

    for (int i = 0; i < m_GroupsPath.size(); i++) {
      Group theGroup = m_GroupsPath.get(i).getThisGroup();
      if (strPath.length() > 0) {
        strPath.append(" &gt ");
      }
      if (((i + 1) < m_GroupsPath.size()) || (m_TargetUserId != null)
          || (toAppendAtEnd != null)) {
        strPath.append("<a href=\"").append(baseURL).append("groupReturn?Idgroup=").
            append(theGroup.getId()).append("\">").
            append(EncodeHelper.javaStringToHtmlString(theGroup.getName())).append("</a>");
      } else {
        strPath.append(EncodeHelper.javaStringToHtmlString(theGroup.getName()));
      }
    }
    if (m_TargetUserId != null) {
      if (strPath.length() > 0) {
        strPath.append(" &gt ");
      }
      if (toAppendAtEnd != null) {
        strPath.append("<a href=\"").append(baseURL).append("userContent?Iduser=").
            append(m_TargetUserId).append("\">").
            append(EncodeHelper.javaStringToHtmlString(getTargetUserDetail().getDisplayedName())).
            append("</a>");
      } else {
        strPath.append(EncodeHelper
            .javaStringToHtmlString(getTargetUserDetail().getDisplayedName()));
      }
    }
    if (toAppendAtEnd != null) {
      if (strPath.length() > 0) {
        strPath.append(" &gt ");
      }
      strPath.append(EncodeHelper.javaStringToHtmlString(toAppendAtEnd));
    }
    return strPath.toString();
  }

  public boolean createGroup(String idParent, String groupName,
      String groupDescription, String groupRule) throws JobDomainPeasException {
    Group theNewGroup = new Group();
    theNewGroup.setId("-1");
    if (StringUtil.isDefined(targetDomainId)
        && !"-1".equals(targetDomainId)) {
      theNewGroup.setDomainId(targetDomainId);
    }
    theNewGroup.setSuperGroupId(idParent);
    theNewGroup.setName(groupName);
    theNewGroup.setDescription(groupDescription);
    theNewGroup.setRule(groupRule);
    String idRet = m_AdminCtrl.addGroup(theNewGroup);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.createGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_GROUP");
    }
    refresh();
    return isGroupRoot(idRet);
  }

  public boolean modifyGroup(String idGroup, String groupName,
      String groupDescription, String groupRule) throws JobDomainPeasException {


    Group theModifiedGroup = m_AdminCtrl.getGroupById(idGroup);
    if (theModifiedGroup == null) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.modifyGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_GROUP");
    }
    theModifiedGroup.setName(groupName);
    theModifiedGroup.setDescription(groupDescription);
    theModifiedGroup.setRule(groupRule);

    String idRet = m_AdminCtrl.updateGroup(theModifiedGroup);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.modifyGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_GROUP");
    }
    refresh();
    return isGroupRoot(idRet);
  }

  public boolean updateGroupSubUsers(String idGroup, String[] userIds)
      throws JobDomainPeasException {


    Group theModifiedGroup = m_AdminCtrl.getGroupById(idGroup);
    if (theModifiedGroup == null) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.updateGroupSubUsers()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_GROUP");
    }
    theModifiedGroup.setUserIds(userIds);
    String idRet = m_AdminCtrl.updateGroup(theModifiedGroup);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.updateGroupSubUsers()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_GROUP");
    }
    refresh();
    return false;
  }

  public boolean deleteGroup(String idGroup) throws JobDomainPeasException {
    boolean haveToRefreshDomain = isGroupRoot(idGroup);



    String idRet = m_AdminCtrl.deleteGroupById(idGroup);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.deleteGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_GROUP");
    }
    removeGroupFromPath(idGroup);
    refresh();
    return haveToRefreshDomain;
  }

  public boolean synchroGroup(String idGroup) throws JobDomainPeasException {


    String idRet = m_AdminCtrl.synchronizeGroup(idGroup);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.synchroGroup()",
          SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_GROUP");
    }
    refresh();
    return isGroupRoot(idRet);
  }

  public boolean unsynchroGroup(String idGroup) throws JobDomainPeasException {
    boolean haveToRefreshDomain = isGroupRoot(idGroup);



    String idRet = m_AdminCtrl.synchronizeRemoveGroup(idGroup);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.unsynchroGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_GROUP");
    }
    removeGroupFromPath(idGroup);
    refresh();
    return haveToRefreshDomain;
  }

  public boolean importGroup(String groupName) throws JobDomainPeasException {


    String idRet = m_AdminCtrl.synchronizeImportGroup(targetDomainId,
        groupName);
    if (!StringUtil.isDefined(idRet)) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.importGroup()",
          SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_GROUP");
    }
    refresh();
    return isGroupRoot(idRet);
  }

  /*
   * DOMAIN functions
   */
  public void setDefaultTargetDomain() {
    UserDetail ud = getUserDetail();

    if (ud.isDomainAdminRestricted()) {
      setTargetDomain(ud.getDomainId());
    }
  }

  public void setTargetDomain(String domainId) {
    if (!StringUtil.isDefined(domainId)) {
      m_TargetDomain = null;
      targetDomainId = "";
    } else {
      List<String> manageableGroupIds = null;
      if (isOnlyGroupManager()) {
        manageableGroupIds = getUserManageableGroupIds();
      }
      m_TargetDomain = new DomainNavigationStock(domainId, m_AdminCtrl, manageableGroupIds);
      targetDomainId = domainId;
    }
  }

  public Domain getTargetDomain() {
    if (m_TargetDomain == null) {
      return null;
    }
    return m_TargetDomain.getThisDomain();
  }

  public long getDomainActions() {
    if (targetDomainId.length() > 0) {
      return m_AdminCtrl.getDomainActions(targetDomainId);
    }
    return 0;
  }

  public List<Domain> getAllDomains() {
    List<Domain> domains = new ArrayList<Domain>();
    UserDetail ud = getUserDetail();

    if (ud.isAccessDomainManager()) {
      // return only domain of user
      domains.add(m_AdminCtrl.getDomain(ud.getDomainId()));
    } else if (ud.isAccessAdmin()) {
      // return mixed domain...
      domains.add(m_AdminCtrl.getDomain(Domain.MIXED_DOMAIN_ID));

      // and all classic domains
      domains.addAll(Arrays.asList(m_AdminCtrl.getAllDomains()));
    } else if (isCommunityManager()) {
      // return mixed domain...
      domains.add(m_AdminCtrl.getDomain(Domain.MIXED_DOMAIN_ID));

      // domain of user...
      domains.add(m_AdminCtrl.getDomain(ud.getDomainId()));

      // and default domain
      domains.add(m_AdminCtrl.getDomain("0"));
    } else if (isOnlyGroupManager()) {
      // return mixed domain...
      domains.add(m_AdminCtrl.getDomain(Domain.MIXED_DOMAIN_ID));

      // and domain of user
      domains.add(m_AdminCtrl.getDomain(ud.getDomainId()));
    }
    return domains;
  }

  public boolean isOnlyGroupManager() {
    return isGroupManager() && !getUserDetail().isAccessAdmin()
        && !getUserDetail().isAccessDomainManager();
  }

  public boolean isCommunityManager() {
    if (!JobDomainSettings.m_UseCommunityManagement) {
      return false;
    }

    // check if user is able to manage at least one space and its corresponding group
    List<Group> groups = getUserManageableGroups();
    List<String> spaceIds = Arrays.asList(getUserManageableSpaceIds());
    for (String spaceId : spaceIds) {
      SpaceInstLight space = getOrganisationController().getSpaceInstLightById(spaceId);
      for (Group group : groups) {
        if (space.getName().equalsIgnoreCase(group.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isGroupManagerOnCurrentGroup() throws JobDomainPeasException {
    return getTargetGroup() != null && isGroupManagerOnGroup(getTargetGroup().getId());
  }

  public boolean isGroupManagerOnGroup(String groupId)
      throws JobDomainPeasException {
    List<String> manageableGroupIds = getUserManageableGroupIds();
    if (manageableGroupIds.contains(groupId)) {
      // Current user is directly manager of group
      return true;
    } else {
      List<String> groupPath = m_AdminCtrl.getPathToGroup(groupId);

      groupPath.retainAll(manageableGroupIds);

      if (groupPath.size() > 0) {
        // Current user is at least manager of one super group of group
        return true;
      }
    }
    return false;
  }

  public boolean isGroupManagerDirectlyOnCurrentGroup()
      throws JobDomainPeasException {
    List<String> manageableGroupIds = getUserManageableGroupIds();
    return manageableGroupIds.contains(getTargetGroup().getId());
  }

  public Group[] getAllRootGroups() {
    if (targetDomainId.length() <= 0) {
      return new Group[0];
    }
    Group[] selGroupsArray = m_TargetDomain.getAllGroupPage();

    if (isOnlyGroupManager()) {
      selGroupsArray = filterGroupsToGroupManager(selGroupsArray);
    }
    JobDomainSettings.sortGroups(selGroupsArray);
    return selGroupsArray;
  }

  private Group[] filterGroupsToGroupManager(Group[] groups) {
    // get all manageable groups by current user
    List<String> manageableGroupIds = getUserManageableGroupIds();
    List<Group> temp = new ArrayList<Group>();
    // filter groups
    for (Group group : groups) {
      if (manageableGroupIds.contains(group.getId())) {
        temp.add(group);
      } else {
        // get all subGroups of group
        List<String> subGroupIds = Arrays.asList(m_AdminCtrl.getAllSubGroupIdsRecursively(group.
            getId()));
        // check if at least one manageable group is part of subGroupIds
        Iterator<String> itManageableGroupsIds = manageableGroupIds.iterator();

        String manageableGroupId;
        boolean find = false;
        while (!find && itManageableGroupsIds.hasNext()) {
          manageableGroupId = itManageableGroupsIds.next();
          if (subGroupIds.contains(manageableGroupId)) {
            find = true;
          }
        }

        if (find) {
          temp.add(group);
        }
      }
    }
    return temp.toArray(new Group[temp.size()]);
  }

  public String createDomain(String domainName, String domainDescription, String domainDriver,
      String domainProperties, String domainAuthentication, String silverpeasServerURL,
      String domainTimeStamp) throws JobDomainPeasException, JobDomainPeasTrappedException {



    String newDomainId = null;

    try {
      Domain theNewDomain = new Domain();
      theNewDomain.setId("-1");
      theNewDomain.setName(domainName);
      theNewDomain.setDescription(domainDescription);
      theNewDomain.setDriverClassName(domainDriver);
      theNewDomain.setPropFileName(domainProperties);
      theNewDomain.setAuthenticationServer(domainAuthentication);
      theNewDomain.setSilverpeasServerURL(silverpeasServerURL);
      theNewDomain.setTheTimeStamp(domainTimeStamp);

      DomainServiceProvider.getDomainService(DomainType.EXTERNAL).createDomain(theNewDomain);
      refresh();
    } catch (DomainCreationException e) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.createDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e);
    } catch (DomainConflictException e) {
      JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.createDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DOMAIN_ALREADY_EXIST_DATABASE", e);
      trappedException.setGoBackPage("displayDomainCreate");
      throw trappedException;
    }

    return newDomainId;
  }

  public String createSQLDomain(String domainName, String domainDescription,
      String silverpeasServerURL, String usersInDomainQuotaMaxCount) throws JobDomainPeasException,
      JobDomainPeasTrappedException {

    // build Domain object
    Domain domainToCreate = new Domain();
    domainToCreate.setName(domainName);
    domainToCreate.setDescription(domainDescription);
    domainToCreate.setSilverpeasServerURL(silverpeasServerURL);

    // launch domain creation process
    String domainId;
    try {

      // Getting quota filled
      if (JobDomainSettings.usersInDomainQuotaActivated) {
        domainToCreate.setUserDomainQuotaMaxCount(usersInDomainQuotaMaxCount);
      }

      domainId = DomainServiceProvider.getDomainService(DomainType.SQL).createDomain(domainToCreate);
      domainToCreate.setId(domainId);

      if (JobDomainSettings.usersInDomainQuotaActivated) {
        // Registering "users in domain" quota
        DomainServiceProvider.getUserDomainQuotaService().initialize(
            UserDomainQuotaKey.from(domainToCreate),
            domainToCreate.getUserDomainQuota().getMaxCount());
      }

    } catch (QuotaException qe) {
      JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN",
          getString("JDP.userDomainQuotaMaxCountError"), qe);
      trappedException.setGoBackPage("displayDomainSQLCreate");
      throw trappedException;
    } catch (DomainCreationException e) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN", e);
    } catch (DomainConflictException e) {
      JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DOMAIN_ALREADY_EXIST", e);
      trappedException.setGoBackPage("displayDomainSQLCreate");
      throw trappedException;
    }

    return domainId;
  }

  public String modifyDomain(String domainName, String domainDescription,
      String domainDriver, String domainProperties,
      String domainAuthentication, String silverpeasServerURL,
      String domainTimeStamp) throws JobDomainPeasException,
      JobDomainPeasTrappedException {
    Domain theNewDomain = getTargetDomain();

    // Vérif domainName unique dans la table ST_Domain
    JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
        "JobDomainPeasSessionController", SilverpeasException.WARNING,
        "jobDomainPeas.WARN_DOMAIN_SQL_NAME");
    trappedException.setGoBackPage("domainContent");
    Domain[] tabDomain = m_AdminCtrl.getAllDomains();
    Domain domain;
    for (Domain aTabDomain : tabDomain) {
      domain = aTabDomain;
      if (!domain.getId().equals(theNewDomain.getId())
          && domain.getName().toLowerCase().equals(domainName.toLowerCase())) {
        throw trappedException;
      }
    }

    if (!StringUtil.isDefined(targetDomainId)
        || targetDomainId.equals("-1")) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifyDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_DOMAIN");
    }
    theNewDomain.setName(domainName);
    theNewDomain.setDescription(domainDescription);
    theNewDomain.setDriverClassName(domainDriver);
    theNewDomain.setPropFileName(domainProperties);
    theNewDomain.setAuthenticationServer(domainAuthentication);
    theNewDomain.setSilverpeasServerURL(silverpeasServerURL);
    theNewDomain.setTheTimeStamp(domainTimeStamp);
    String idRet = m_AdminCtrl.updateDomain(theNewDomain);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifyDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_DOMAIN");
    }
    refresh();
    return idRet;
  }

  public String modifySQLDomain(String domainName, String domainDescription,
      String silverpeasServerURL, String usersInDomainQuotaMaxCount) throws JobDomainPeasException,
      JobDomainPeasTrappedException {
    Domain theNewDomain = getTargetDomain();

    // Vérif domainName unique dans la table ST_Domain
    JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
        "JobDomainPeasSessionController", SilverpeasException.WARNING,
        "jobDomainPeas.WARN_DOMAIN_SQL_NAME");
    trappedException.setGoBackPage("domainContent");
    Domain[] tabDomain = m_AdminCtrl.getAllDomains();
    Domain domain;
    for (Domain aTabDomain : tabDomain) {
      domain = aTabDomain;
      if (!domain.getId().equals(theNewDomain.getId())
          && domain.getName().toLowerCase().equals(domainName.toLowerCase())) {
        throw trappedException;
      }
    }

    String idRet;

    if ((targetDomainId == null) || (targetDomainId.equals("-1"))
        || (targetDomainId.equals("0")) || (targetDomainId.length() <= 0)) {
      throw new JobDomainPeasException(
          "JobDomainPeasSessionController.modifySQLDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_DOMAIN");
    }
    theNewDomain.setName(domainName);
    theNewDomain.setDescription(domainDescription);
    theNewDomain.setSilverpeasServerURL(silverpeasServerURL);

    try {

      if (JobDomainSettings.usersInDomainQuotaActivated) {
        // Getting quota filled
        theNewDomain.setUserDomainQuotaMaxCount(usersInDomainQuotaMaxCount);
      }

      idRet = m_AdminCtrl.updateDomain(theNewDomain);
      if ((idRet == null) || (idRet.length() <= 0)) {
        throw new JobDomainPeasException("JobDomainPeasSessionController.modifySQLDomain()",
            SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_DOMAIN");
      }

      if (JobDomainSettings.usersInDomainQuotaActivated) {
        // Registering "users in domain" quota
        DomainServiceProvider.getUserDomainQuotaService().initialize(
            UserDomainQuotaKey.from(theNewDomain), theNewDomain.getUserDomainQuota().getMaxCount());
      }

    } catch (QuotaException qe) {
      trappedException = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.modifySQLDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_DOMAIN",
          getString("JDP.userDomainQuotaMaxCountError"), qe);
      trappedException.setGoBackPage("displayDomainSQLCreate");
      throw trappedException;
    }

    refresh();
    return idRet;
  }

  public void deleteDomain() throws JobDomainPeasException {
    try {
      DomainServiceProvider.getDomainService(DomainType.EXTERNAL).deleteDomain(getTargetDomain());
    } catch (DomainDeletionException e) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.deleteDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DELETE_DOMAIN", e);
    }
  }

  public void deleteSQLDomain() throws JobDomainPeasException {
    try {
      DomainServiceProvider.getDomainService(DomainType.SQL).deleteDomain(getTargetDomain());
      DomainServiceProvider.getUserDomainQuotaService().remove(
          UserDomainQuotaKey.from(getTargetDomain()));
    } catch (DomainDeletionException e) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.deleteSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DELETE_DOMAIN", e);
    }
  }

  protected String getSureString(String s) {
    if (s == null) {
      return "";
    } else {
      return s;
    }
  }

  public void refresh() {
    if (m_TargetDomain != null) {
      m_TargetDomain.refresh();
    }
    for (GroupNavigationStock aM_GroupsPath : m_GroupsPath) {
      aM_GroupsPath.refresh();
    }
    setTargetUser(null);
  }

  /*
   * Selection Peas functions
   */
  public String initSelectionPeasForGroups(String compoURL) throws JobDomainPeasException {
    String hostSpaceName = getString("JDP.userPanelGroup");
    Pair<String, String> hostComponentName = new Pair<>(getTargetGroup().getName(),
        compoURL + "groupContent");
    Pair<String, String>[] hostPath = new Pair[0];
    String hostUrl = compoURL + "groupAddRemoveUsers";
    String cancelUrl = compoURL + "groupContent";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setFilterOnDeactivatedState(false);
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostPath(hostPath);
    sel.setHostComponentName(hostComponentName);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    setDomainIdOnSelection(sel);

    sel.setSelectedElements(SelectionUsersGroups.getUserIds(m_GroupsPath.lastElement().
        getAllUserPage()));

    // Contraintes
    sel.setSetSelectable(false);
    sel.setPopupMode(false);
    sel.setFirstPage(Selection.FIRST_PAGE_CART);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  private void setDomainIdOnSelection(Selection selection) {
    if (StringUtil.isDefined(targetDomainId) && !Domain.MIXED_DOMAIN_ID.equals(targetDomainId)) {
      // Add extra params
      SelectionUsersGroups sug = new SelectionUsersGroups();
      sug.setDomainId(targetDomainId);
      selection.setExtraParams(sug);
    }
  }

  /*
   * Appel UserPannel pour récup du user sélectionné :
   */
  public String[] getSelectedUsersIds() {
    return getSelection().getSelectedElements();
  }

  // Throws Specific Exception
  public String initSelectionPeasForOneGroupOrUser(String compoURL)
      throws JobDomainPeasException {
    String hostSpaceName = getString("JDP.userPanelDomain");
    Pair<String, String> hostComponentName = new Pair<>(getTargetDomain().getName(),
        compoURL + "domainContent");
    Pair<String, String>[] hostPath = new Pair[0];
    String hostUrl = compoURL + "selectUserOrGroup";
    String cancelUrl = compoURL + "domainContent";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setFilterOnDeactivatedState(false);
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostPath(hostPath);
    sel.setHostComponentName(hostComponentName);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    if (!StringUtil.isDefined(targetDomainId) || "-1".equals(targetDomainId)) {
      sel.setElementSelectable(false);
    }

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setDomainId(targetDomainId);
    sel.setExtraParams(sug);

    // Contraintes
    sel.setMultiSelect(false);
    sel.setPopupMode(false);
    sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String getSelectedUserId() {
    return getSelection().getFirstSelectedElement();
  }

  public String getSelectedGroupId() {
    return getSelection().getFirstSelectedSet();
  }

  // Synchro Management
  // ------------------
  public void synchroSQLDomain() {
    if (m_theThread == null) {
      SynchroDomainReport.setReportLevel(Level.INFO);
      SynchroDomainReport.waitForStart();
      m_theThread = new SynchroWebServiceThread(this);
      m_ErrorOccured = null;
      m_SynchroReport = "";
      m_theThread.startTheThread();
    }
  }

  protected String synchronizeSilverpeasViaWebService() {
    StringBuilder sReport = new StringBuilder();
    SynchroUserWebServiceItf synchroUserWebService = null;
    try {
      sReport.append("Démarrage de la synchronisation...\n\n");
      // Démarrage de la synchro avec la Popup d'affichage
      SynchroDomainReport.startSynchro();
      Domain theDomain = getTargetDomain();

      SynchroDomainReport.warn("jobDomainPeas.synchronizeSilverpeasViaWebService",
          "Domaine : " + theDomain.getName() + " (id : " + theDomain.getId() + ")");

      // 1- Récupère la liste des groupes à synchroniser (en insert et update)
      Collection<Group> listGroupToInsertUpdate;
      try {
        listGroupToInsertUpdate = JobDomainPeasDAO.selectGroupSynchroInsertUpdateTableDomain_Group(
            theDomain);
      } catch (SQLException e1) {
        throw new JobDomainPeasException("JobDomainPeasSessionController.synchroSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e1);
      }

      // 2- Traitement Domaine, appel aux webServices
      String propDomainFileName = theDomain.getPropFileName();

      SettingBundle propDomainSql = ResourceLocator.getSettingBundle(propDomainFileName);
      String nomClasseWebService = propDomainSql.getString("ExternalSynchroClass");
      try {
        synchroUserWebService = (SynchroUserWebServiceItf) Class.forName(nomClasseWebService).
            newInstance();
      } catch (Exception e) {
        throw new JobDomainPeasException("JobDomainPeasSessionController.synchroSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e);
      }

      synchroUserWebService.startConnection();

      // Insertion / Update de la société
      sReport.append(synchroUserWebService.insertUpdateDomainWebService(theDomain.getId(),
          theDomain.getName()));

      // 3- Traitement groupes, appel aux webServices
      if (listGroupToInsertUpdate != null && listGroupToInsertUpdate.size() > 0) {

        // Insertion / Update des groupes
        sReport.append(synchroUserWebService.insertUpdateListGroupWebService(theDomain.getId(),
            theDomain.getName(), listGroupToInsertUpdate));
      }

      // 4- Récupère la liste des users à synchroniser (en insert et update)
      Collection<UserFull> listUserToInsertUpdate;
      try {
        listUserToInsertUpdate = JobDomainPeasDAO.selectUserSynchroInsertUpdateTableDomain_User(
            theDomain);
      } catch (SQLException e1) {
        throw new JobDomainPeasException("JobDomainPeasSessionController.synchroSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e1);
      }

      // 5- Récupère la liste des users à synchroniser (en delete)
      Collection<UserDetail> listUserToDelete;
      try {
        listUserToDelete = JobDomainPeasDAO.selectUserSynchroDeleteTableDomain_User(theDomain);
      } catch (SQLException e1) {
        throw new JobDomainPeasException(
            "JobDomainPeasSessionController.synchroSQLDomain()",
            SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e1);
      }

      // 6-Traitement users, appel aux webServices
      if (listUserToDelete != null && !listUserToDelete.isEmpty()) {
        // Suppression des users
        sReport.append(synchroUserWebService.deleteListUserWebService(theDomain.getId(),
            listUserToDelete));
      }

      // Insertion / Update des users
      if (listUserToInsertUpdate != null && !listUserToInsertUpdate.isEmpty()) {
        sReport.append(synchroUserWebService.insertUpdateListUserWebService(theDomain.getId(),
            listUserToInsertUpdate, listGroupToInsertUpdate));
      }

      sReport.append("\n\nFin de la synchronisation...");

    } catch (JobDomainPeasException e) {
      SilverTrace.error("JobDomainPeasSessionController",
          "JobDomainPeasSessionController.synchronizeSilverpeasViaWebService",
          "admin.MSG_ERR_SYNCHRONIZE_DOMAIN", e);
      SynchroDomainReport.error(
          "JobDomainPeasSessionController.synchronizeSilverpeasViaWebService",
          "Problème lors de la synchronisation : " + e.getMessage(), null);
      sReport.append("Erreurs lors de la synchronisation : \n").append(e.getMessage());
    } finally {
      // Fin de synchro avec la Popup d'affichage
      SynchroDomainReport.stopSynchro();
      if (synchroUserWebService != null) {
        synchroUserWebService.endConnection();
      }
    }
    return sReport.toString();
  }

  public void synchroDomain(Level level) {
    if (m_theThread == null) {
      SynchroDomainReport.setReportLevel(level);
      SynchroDomainReport.waitForStart();
      m_theThread = new SynchroLdapThread(this, m_AdminCtrl, targetDomainId);
      m_ErrorOccured = null;
      m_SynchroReport = "";
      m_theThread.startTheThread();
    }
  }

  public boolean isEnCours() {
    return m_theThread != null && m_theThread.isEnCours();
  }

  public String getSynchroReport() {
    if (m_ErrorOccured != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);

      m_ErrorOccured.printStackTrace(pw);
      return m_ErrorOccured.toString() + "\n" + sw.getBuffer().toString();
    }
    return m_SynchroReport;
  }

  public void threadFinished() {
    m_ErrorOccured = m_theThread.getErrorOccured();
    m_SynchroReport = m_theThread.getSynchroReport();
    m_theThread = null;
  }

  public void getP12(String userId) throws JobDomainPeasException {
    UserDetail user = getUserDetail(userId);

    try {
      X509Factory.buildP12(user.getId(), user.getLogin(), user.getLastName(), user.getFirstName(),
          user.getDomainId());
    } catch (UtilException e) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.getP12()",
          SilverpeasException.ERROR, "admin.MSG_ERR_CANT_GET_P12", e);
    }
  }

  private void revocateCertificate(UserDetail user)
      throws JobDomainPeasException {
    try {
      X509Factory.revocateUserCertificate(user.getId());
    } catch (UtilException e) {
      throw new JobDomainPeasException("JobDomainPeasSessionController.revocateCertificate()",
          SilverpeasException.ERROR, "admin.MSG_ERR_CANT_REVOCATE_CERTIFICATE", e);
    }
  }

  /**
   * PAGINATION *
   */
  /**
   * Get list of selected users Ids
   */
  public List<String> getListSelectedUsers() {
    return listSelectedUsers;
  }

  public void clearListSelectedUsers() {
    listSelectedUsers.clear();
  }

  public void setListSelectedUsers(List<String> list) {
    listSelectedUsers = list;
  }

  public void setIndexOfFirstItemToDisplay(String index) {
    this.indexOfFirstItemToDisplay = Integer.parseInt(index);
  }

  public int getIndexOfFirstItemToDisplay() {
    return indexOfFirstItemToDisplay;
  }

  public List<Group> getUserManageableGroups() {
    List<String> groupIds = getUserManageableGroupIds();
    Group[] aGroups = getOrganisationController().getGroups(groupIds.toArray(new String[groupIds.
        size()]));
    return Arrays.asList(aGroups);
  }

  public UserDetail checkUser(UserDetail userToCheck) {
    UserDetail[] existingUsers = m_TargetDomain.getAllUserPage();
    for (UserDetail existingUser : existingUsers) {
      if (userToCheck.getLastName().equalsIgnoreCase(existingUser.getLastName())
          && userToCheck.getFirstName().equalsIgnoreCase(existingUser.getFirstName())
          && userToCheck.geteMail().equalsIgnoreCase(existingUser.geteMail())) {
        return existingUser;
      }
    }
    return null;
  }

  /**
   * @return true if community management is activated and target user belongs to one group
   * manageable by current user
   */
  public boolean isUserInAtLeastOneGroupManageableByCurrentUser() {
    if (!JobDomainSettings.m_UseCommunityManagement) {
      return false;
    }
    List<String> groupIds = getUserManageableGroupIds();
    for (String groupId : groupIds) {
      UserDetail[] users = getOrganisationController().getAllUsersOfGroup(groupId);
      UserDetail user = getUser(m_TargetUserId, users);

      if (user != null) {
        return true;
      }
    }
    return false;
  }

  private UserDetail getUser(String userId, UserDetail[] users) {
    for (UserDetail userDetail : users) {
      if (userId.equals(userDetail.getId())) {
        return userDetail;
      }
    }
    return null;
  }
}
