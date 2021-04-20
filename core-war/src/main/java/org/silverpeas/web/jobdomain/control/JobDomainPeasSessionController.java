/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jobdomain.control;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.LocalizedComponent;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.DomainDriver.UserFilterManager;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.domain.DomainServiceProvider;
import org.silverpeas.core.admin.domain.DomainType;
import org.silverpeas.core.admin.domain.exception.DomainConflictException;
import org.silverpeas.core.admin.domain.exception.DomainCreationException;
import org.silverpeas.core.admin.domain.exception.DomainDeletionException;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.domain.quota.UserDomainQuotaKey;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminController.Result;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.service.GroupAlreadyExistsAdminException;
import org.silverpeas.core.admin.service.RightAssignationContext;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.GroupProfileInst;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.exception.UtilTrappedException;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.security.authentication.password.service.PasswordCheck;
import org.silverpeas.core.security.authentication.password.service.PasswordRulesServiceProvider;
import org.silverpeas.core.security.encryption.X509Factory;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.*;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.util.csv.CSVReader;
import org.silverpeas.core.util.csv.Variant;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionException;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.web.util.ListIndex;
import org.silverpeas.web.directory.servlets.ImageProfil;
import org.silverpeas.web.jobdomain.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.synchronizedList;
import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.admin.domain.DomainDriverManagerProvider.getCurrentDomainDriverManager;
import static org.silverpeas.core.personalization.service.PersonalizationServiceProvider.getPersonalizationService;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Class declaration
 *
 * @author
 */
public class JobDomainPeasSessionController extends AbstractComponentSessionController {

  public static final String REPLACE_RIGHTS = "1";
  public static final String ADD_RIGHTS = "2";

  private static final String IMPORT_CSV_USERS_OPERATION =
      "JobDomainPeasSessionController.importCsvUsers";
  private static final String ERROR_CSV_FILE = "jobDomainPeas.EX_CSV_FILE";
  private String targetUserId = null;
  private String targetDomainId = "";
  private DomainNavigationStock targetDomain = null;
  private List<GroupNavigationStock> groupsPath = synchronizedList(new ArrayList<>());
  private SynchroThread synchroThread = null;
  private Exception errorOccured = null;
  private String synchroReport = "";
  private Selection sel = null;
  private List<UserDetail> usersToImport = null;
  private Map<String, String> queryToImport = null;
  private AdminController adminCtrl = null;
  private List<String> listSelectedUsers = synchronizedList(new ArrayList<>());
  // pagination de la liste des résultats
  private int indexOfFirstItemToDisplay = 0;
  private boolean refreshDomain = true;
  private ListIndex currentIndex = new ListIndex(0);
  private List<UserDetail> sessionUsers = synchronizedList(new ArrayList<>());

  private static final Properties templateConfiguration = new Properties();
  private static final String USER_ACCOUNT_TEMPLATE_FILE = "userAccount_email";
  private static final List<String> USERTYPES =
      Arrays.asList("Admin", "AdminPdc", "AdminDomain", "User", "Guest");

  private Map<String, LocalizedComponent> localizedComponents = new HashMap<>();

  /**
   * Standard Session Controller Constructeur
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   *
   */
  public JobDomainPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle",
        "org.silverpeas.jobDomainPeas.settings.jobDomainPeasIcons",
        "org.silverpeas.jobDomainPeas.settings.jobDomainPeasSettings");
    setComponentRootName(URLUtil.CMP_JOBDOMAINPEAS);
    adminCtrl = ServiceProvider.getService(AdminController.class);
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
        || getUserDetail().isAccessDomainManager() || isOnlySpaceManager();
  }

  public void setRefreshDomain(boolean refreshDomain) {
    this.refreshDomain = refreshDomain;
  }

  /*
   * USER functions
   */
  public void setTargetUser(String userId) {
    targetUserId = userId;
    processIndex(targetUserId);
  }

  public UserDetail getTargetUserDetail() throws JobDomainPeasException {
    UserDetail valret = null;

    if (isDefined(targetUserId)) {
      valret = UserDetail.getById(targetUserId);
      if (valret == null) {
        throw new JobDomainPeasException(unknown("user", targetUserId));
      }
    }
    return valret;
  }

  public UserFull getTargetUserFull() throws JobDomainPeasException {
    UserFull valret = null;

    if (isDefined(targetUserId)) {
      valret = UserFull.getById(targetUserId);
      if (valret == null) {
        throw new JobDomainPeasException(unknown("user", targetUserId));
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
  public String createUser(UserRequestData userRequestData, Map<String, String> properties,
      HttpRequest req) throws JobDomainPeasException, JobDomainPeasTrappedException {
    UserDetail theNewUser = new UserDetail();
    if (adminCtrl.isUserByLoginAndDomainExist(userRequestData.getLogin(), targetDomainId)) {
      JobDomainPeasTrappedException te = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.createUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_LOGIN_ALREADY_USED");
      te.setGoBackPage("displayUserCreate");
      throw te;
    }

    theNewUser.setId("-1");
    if (isDefined(targetDomainId) && !targetDomainId.equals(Domain.MIXED_DOMAIN_ID)) {
      theNewUser.setDomainId(targetDomainId);
    }
    userRequestData.applyDataOnNewUser(theNewUser);
    String idRet = adminCtrl.addUser(theNewUser);
    if (StringUtil.isNotDefined(idRet)) {
      throw new JobDomainPeasException(failureOnAdding("user", theNewUser.getLogin()));
    }
    refresh();
    setTargetUser(idRet);
    theNewUser.setId(idRet);

    // Registering the preferred user language if any and if it is different from the default one
    final boolean isPreferredLanguageFilled =
        !userRequestData.getLanguage().equals(DisplayI18NHelper.getDefaultLanguage());
    final boolean isPreferredZoneIdFilled =
        !userRequestData.getZoneId().equals(DisplayI18NHelper.getDefaultZoneId());
    if (isPreferredLanguageFilled || isPreferredZoneIdFilled) {
      UserPreferences userPreferences = theNewUser.getUserPreferences();
      userPreferences.setLanguage(userRequestData.getLanguage());
      userPreferences.setZoneId(userRequestData.getZoneId());
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

      // process data of extra template
      processDataOfExtraTemplate(uf.getId(), req);

      try {
        idRet = adminCtrl.updateUserFull(uf);
      } catch (AdminException e) {
        throw new JobDomainPeasException(failureOnUpdate("user", uf.getId()), e);
      }
    }

    // regroupement de l'utilisateur dans un groupe
    regroupInGroup(properties, null);
    // If group is provided, add newly created user to it
    if (isDefined(userRequestData.getGroupId())) {
      adminCtrl.addUserInGroup(idRet, userRequestData.getGroupId());
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
        isDefined(user.geteMail()) &&
        isDefined(userRequestData.getPassword())) {

      // Send an email notification
      Map<String, SilverpeasTemplate> templates = new HashMap<>();

      NotificationMetaData notifMetaData =
          new NotificationMetaData(BuiltInNotifAddress.BASIC_SMTP.getId(), "", templates,
              USER_ACCOUNT_TEMPLATE_FILE);

      String loginUrl = getLoginUrl(user, req);

      for (String lang : DisplayI18NHelper.getLanguages()) {
        LocalizationBundle notifBundle = ResourceLocator.getLocalizationBundle(
            "org.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle", lang);
        notifMetaData.addLanguage(lang, notifBundle.getString("JDP.createAccountNotifTitle"), "");
        templates.put(lang, getTemplate(user, loginUrl, userRequestData, isNewUser));
      }

      notifMetaData.addUserRecipient(new UserRecipient(user.getId()));
      NotificationSender sender = new NotificationSender(null);
      try {
        sender.notifyUser(BuiltInNotifAddress.BASIC_SMTP.getId(), notifMetaData);
      } catch (NotificationException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
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
    if (!isDefined(loginPage)) {
      loginPage = "/defaultLogin.jsp";
      String domainId = user.getDomainId();
      if (isDefined(domainId) && !Domain.MIXED_DOMAIN_ID.equals(domainId)) {
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
   * @param userRequestData the current user data
   * @param isNew true if it's a created user, false else if
   * @return a SilverpeasTemplate
   */
  private SilverpeasTemplate getTemplate(UserDetail userDetail, String loginURL,
      UserRequestData userRequestData, boolean isNew) {
    Properties configuration = new Properties(templateConfiguration);
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplate(configuration);
    template.setAttribute("userDetail", userDetail);
    template.setAttribute("loginURL", loginURL);
    template.setAttribute("pwd", userRequestData.getPassword());
    template.setAttribute("extraMessage", userRequestData.getExtraMessage());
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
  private void regroupInGroup(Map<String, String> properties, String lastGroupId)
      throws JobDomainPeasException {

    // Traitement du domaine SQL
    if (!getTargetDomain().isMixedOne()
        && !"0".equals(getTargetDomain().getId()) &&
        "org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver"
            .equals(getTargetDomain().getDriverClassName())) {

      SettingBundle specificRs = getTargetDomain().getSettings();
      int numPropertyRegroup = specificRs.getInteger("property.Grouping", -1);
      String nomRegroup = null;
      String theUserIdToRegroup = targetUserId;
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
            Group lastGroup = adminCtrl.getGroupById(lastGroupId);
            lUserIds = Arrays.asList(lastGroup.getUserIds());
            lNewUserIds = new ArrayList<>(lUserIds);
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

      if (isDefined(nomRegroup)) {
        // Recherche le groupe dans le domaine
        GroupDetail group = adminCtrl.getGroupByNameInDomain(nomRegroup, targetDomainId);
        if (group == null) {
          // le groupe n'existe pas, on le crée
          group = new GroupDetail();
          group.setId("-1");
          group.setDomainId(targetDomainId);
          // groupe à la racine
          group.setSuperGroupId(null);
          group.setName(nomRegroup);
          group.setDescription("");
          String groupId = adminCtrl.addGroup(group);
          group = adminCtrl.getGroupById(groupId);
        }

        lUserIds = Arrays.asList(group.getUserIds());
        lNewUserIds = new ArrayList<>(lUserIds);
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
   * @throws JobDomainPeasTrappedException
   */
  public void importCsvUsers(FileItem filePart, UserRequestData data, HttpRequest req)
      throws JobDomainPeasTrappedException {
    InputStream is;
    try {
      is = filePart.getInputStream();
    } catch (IOException e) {
      JobDomainPeasTrappedException jdpe = new JobDomainPeasTrappedException(
          IMPORT_CSV_USERS_OPERATION,
          SilverpeasException.ERROR, ERROR_CSV_FILE, e);
      jdpe.setGoBackPage("displayUsersCsvImport");
      throw jdpe;
    }
    CSVReader csvReader = new CSVReader(getLanguage());
    csvReader.initCSVFormat("org.silverpeas.jobDomainPeas.settings.usersCSVFormat", "User", ";",
        getTargetDomain().getPropFileName(), "property");

    // spécifique domaine Silverpeas (2 colonnes en moins (password et
    // passwordValid)
    if (Domain.MIXED_DOMAIN_ID.equals(getTargetDomain().getId())
        || "0".equals(getTargetDomain().getId())) {
      // domaine Silverpeas
      csvReader.setSpecificNbCols(csvReader.getSpecificNbCols() - 2);
    }

    Variant[][] csvValues;
    try {
      csvValues = csvReader.parseStream(is);
    } catch (UtilTrappedException ute) {
      JobDomainPeasTrappedException e = new JobDomainPeasTrappedException(
          IMPORT_CSV_USERS_OPERATION,
          SilverpeasException.ERROR, ERROR_CSV_FILE, ute);
      e.setGoBackPage("displayUsersCsvImport");
      throw e;
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
      if (nom.length() == 0) {
        // champ obligatoire
        listErrors.append(getErrorMessage(i + 1, 1, nom));
        listErrors.append(getString("JDP.obligatoire")).append("<br>");
      } else if (nom.length() > 100) {
        listErrors.append(getErrorMessage(i + 1, 1, nom));
        listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").
            append(getString("JDP.caracteres")).append("<br>");
      }

      // Prenom
      prenom = csvValues[i][1].getValueString();
      if (prenom.length() > 100) {
        listErrors.append(getErrorMessage(i + 1, 2, prenom));
        listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(
            getString("JDP.caracteres")).append("<br>");
      }

      // Login
      login = csvValues[i][2].getValueString();
      if (login.length() == 0) {
        // champ obligatoire
        listErrors.append(getErrorMessage(i + 1, 3, login));
        listErrors.append(getString("JDP.obligatoire")).append("<br>");
      } else if (login.length() < JobDomainSettings.m_MinLengthLogin) {// verifier
        listErrors.append(getErrorMessage(i + 1, 3, login));
        listErrors.append(getString("JDP.nbCarMin")).append(" ").append(
            JobDomainSettings.m_MinLengthLogin).append(" ").append(getString("JDP.caracteres")).
            append("<br>");
      } else if (login.length() > 50) {
        listErrors.append(getErrorMessage(i + 1, 3, login));
        listErrors.append(getString("JDP.nbCarMax")).append(" 50 ").append(getString(
            "JDP.caracteres")).append("<br>");
      } else {
        // verif login unique
        existingLogin = adminCtrl.getUserIdByLoginAndDomain(login, targetDomainId);
        if (existingLogin != null) {
          listErrors.append(getErrorMessage(i + 1, 3, login));
          listErrors.append(getString("JDP.existingLogin")).append("<br>");
        }
      }

      // Email
      email = csvValues[i][3].getValueString();
      if (email.length() > 100) {
        listErrors.append(getErrorMessage(i + 1, 4, email));
        listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(getString(
            "JDP.caracteres")).append("<br>");
      }

      // Droits
      droits = csvValues[i][4].getValueString();
      if (!"".equals(droits) && !USERTYPES.contains(droits)) {
        listErrors.append(getErrorMessage(i + 1, 5, droits));
        listErrors.append(getString("JDP.valeursPossibles")).append("<br>");
      }

      // MotDePasse
      motDePasse = csvValues[i][5].getValueString();
      // password is not mandatory
      if (isDefined(motDePasse)) {
        // Cheking password
        PasswordCheck passwordCheck = PasswordRulesServiceProvider.getPasswordRulesService().check(motDePasse);
        if (!passwordCheck.isCorrect()) {
          listErrors.append(getErrorMessage(i + 1, 6, motDePasse))
              .append(passwordCheck.getFormattedErrorMessage(getLanguage()));
          listErrors.append("<br>");
        } else if (motDePasse.length() > 32) {
          listErrors.append(getErrorMessage(i + 1, 6, motDePasse));
          listErrors.append(getString("JDP.nbCarMax")).append(" 32 ").append(getString(
              "JDP.caracteres")).append("<br>");
        }
      }

      if (csvReader.getSpecificNbCols() > 0) {
        if (getTargetDomain().isMixedOne() || "0".equals(getTargetDomain().getId())) {
          // domaine Silverpeas

          // title
          title = csvValues[i][6].getValueString();
          if (title.length() > 100) {
            listErrors.append(getErrorMessage(i + 1, 7, title));
            listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(getString(
                "JDP.caracteres")).append("<br>");
          }

          // company
          company = csvValues[i][7].getValueString();
          if (company.length() > 100) {
            listErrors.append(getErrorMessage(i + 1, 8, company));
            listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(getString(
                "JDP.caracteres")).append("<br>");
          }

          // position
          position = csvValues[i][8].getValueString();
          if (position.length() > 100) {
            listErrors.append(getErrorMessage(i + 1, 9, position));
            listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(getString(
                "JDP.caracteres")).append("<br>");
          }

          // boss
          boss = csvValues[i][9].getValueString();
          if (boss.length() > 100) {
            listErrors.append(getErrorMessage(i + 1, 10, boss));
            listErrors.append(getString("JDP.nbCarMax")).append(" 100 ").append(getString(
                "JDP.caracteres")).append("<br>");
          }

          // phone
          phone = csvValues[i][10].getValueString();
          if (phone.length() > 20) {
            listErrors.append(getErrorMessage(i + 1, 11, phone));
            listErrors.append(getString("JDP.nbCarMax")).append(" 20 ").append(getString(
                "JDP.caracteres")).append("<br>");
          }

          // homePhone
          homePhone = csvValues[i][11].getValueString();
          if (homePhone.length() > 20) {
            listErrors.append(getErrorMessage(i + 1, 12, homePhone));
            listErrors.append(getString("JDP.nbCarMax")).append(" 20 ").append(getString(
                "JDP.caracteres")).append("<br>");
          }

          // fax
          fax = csvValues[i][12].getValueString();
          if (fax.length() > 20) {
            listErrors.append(getErrorMessage(i + 1, 13, fax));
            listErrors.append(getString("JDP.nbCarMax")).append(" 20 ").append(getString(
                "JDP.caracteres")).append("<br>");
          }

          // cellularPhone
          cellularPhone = csvValues[i][13].getValueString();

          // max
          if (cellularPhone.length() > 20) {
            listErrors.append(getErrorMessage(i + 1, 14, cellularPhone));
            listErrors.append(getString("JDP.nbCarMax")).append(" 20 ").append(getString(
                "JDP.caracteres")).append("<br>");
          }

          // address
          address = csvValues[i][14].getValueString();
          if (address.length() > 500) {
            listErrors.append(getErrorMessage(i + 1, 15, address));
            listErrors.append(getString("JDP.nbCarMax")).append(" 500 ").append(getString(
                "JDP.caracteres")).append("<br>");
          }
        } else {
          // domaine SQL
          for (int j = 0; j < csvReader.getSpecificNbCols(); j++) {
            if (Variant.TYPE_STRING.equals(csvReader.getSpecificColType(j))) {
              informationSpecifiqueString = csvValues[i][j + 6].getValueString();
              // verify the length
              if (informationSpecifiqueString.length() > csvReader.getSpecificColMaxLength(j)) {
                listErrors.append(getErrorMessage(i + 1, j + 6, informationSpecifiqueString));
                listErrors.append(getString("JDP.nbCarMax")).append(" ")
                    .append(csvReader.getSpecificColMaxLength(j)).append(" ")
                    .append(getString("JDP.caracteres")).append("<br>");
              }
            }
          }
        }
      }
    }

    if (listErrors.length() > 0) {
      JobDomainPeasTrappedException jdpe = new JobDomainPeasTrappedException(
          IMPORT_CSV_USERS_OPERATION,
          SilverpeasException.ERROR, ERROR_CSV_FILE, listErrors.toString());
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
      properties = new HashMap<>();
      if (csvReader.getSpecificNbCols() > 0) {
        if (getTargetDomain().isMixedOne() || "0".equals(getTargetDomain().getId())) {
          // domaine Silverpeas

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

        } else {
          // domaine SQL

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

      // password is not mandatory
      boolean passwordValid = isDefined(motDePasse);
      UserRequestData userRequestData = new UserRequestData();
      userRequestData.setLogin(login);
      userRequestData.setLastName(nom);
      userRequestData.setFirstName(prenom);
      userRequestData.setEmail(email);
      userRequestData.setAccessLevel(userAccessLevel);
      userRequestData.setPasswordValid(passwordValid);
      userRequestData.setPassword(motDePasse);
      userRequestData.setSendEmail(data.isSendEmail());
      userRequestData.setExtraMessage(data.getExtraMessage());
      userRequestData.setUserManualNotifReceiverLimitEnabled(true);
      try {
        createUser(userRequestData, properties, req);
      } catch (JobDomainPeasException e) {
        throw new JobDomainPeasTrappedException(IMPORT_CSV_USERS_OPERATION,
            SilverpeasException.ERROR, ERROR_CSV_FILE, e);
      }
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
    if (!getTargetDomain().isMixedOne() && !"0".equals(getTargetDomain().getId())
        && "org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver".equals(
        getTargetDomain().getDriverClassName())) {
      SettingBundle specificRs = getTargetDomain().getSettings();
      int numPropertyRegroup = specificRs.getInteger("property.Grouping", -1);
      String nomLastGroup = null;
      if (numPropertyRegroup > -1) {
        String nomPropertyRegroupement = specificRs.getString("property_" + numPropertyRegroup
            + ".Name", null);
        if (nomPropertyRegroupement != null) {
          // Recherche du nom du regroupement (nom du groupe)
          String value = null;
          for (String key : theUser.getPropertiesNames()) {
            value = theUser.getValue(key);
            if (key.equals(nomPropertyRegroupement)) {
              nomLastGroup = value;
              break;
            }
          }
        }
      }

      if (isDefined(nomLastGroup)) {
        // Recherche le groupe dans le domaine
        Group group = adminCtrl.getGroupByNameInDomain(nomLastGroup, targetDomainId);
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
  public void modifyUser(UserRequestData userRequestData, Map<String, String> properties,
      HttpRequest req) throws JobDomainPeasException {

    UserFull theModifiedUser = adminCtrl.getUserFull(userRequestData.getId());
    if (theModifiedUser == null) {
      throw new JobDomainPeasException(unknown("user", userRequestData.getId()));
    }

    // nom du groupe auquel était rattaché l'utilisateur
    String lastGroupId = getLastGroupId(theModifiedUser);

    userRequestData.applyDataOnExistingUser(theModifiedUser);

    notifyUserAccount(userRequestData, theModifiedUser, req, false);

    // process extra properties
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      theModifiedUser.setValue(entry.getKey(), entry.getValue());
    }

    // process data of extra template
    processDataOfExtraTemplate(theModifiedUser.getId(), req);

    String idRet;
    try {
      idRet = adminCtrl.updateUserFull(theModifiedUser);
    } catch (AdminException e) {
      throw new JobDomainPeasException(failureOnUpdate("user", userRequestData.getId()), e);
    }
    refresh();
    setTargetUser(idRet);

    // regroupement de l'utilisateur dans un groupe
    regroupInGroup(properties, lastGroupId);
  }

  public void modifySynchronizedUser(UserRequestData userRequestData,
      Map<String, String> properties, HttpRequest req) throws JobDomainPeasException {

    UserFull theModifiedUser = adminCtrl.getUserFull(userRequestData.getId());
    if (theModifiedUser == null) {
      throw new JobDomainPeasException(unknown("synchronized user", userRequestData.getId()));
    }
    theModifiedUser.setAccessLevel(userRequestData.getAccessLevel());
    theModifiedUser.setUserManualNotificationUserReceiverLimit(
        userRequestData.getUserManualNotifReceiverLimitValue());

    // process data of extra template
    processDataOfExtraTemplate(theModifiedUser.getId(), req);

    String idRet = "";
    if (theModifiedUser.isAtLeastOnePropertyUpdatableByAdmin()) {
      // process extra properties
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        if (theModifiedUser.isPropertyUpdatableByAdmin(entry.getKey())) {
          theModifiedUser.setValue(entry.getKey(), entry.getValue());
        }
      }
      try {
        idRet = adminCtrl.updateUserFull(theModifiedUser);
      } catch (AdminException e) {
        throw new JobDomainPeasException(failureOnUpdate("user", userRequestData.getId()), e);
      }
    } else {
      idRet = adminCtrl.updateSynchronizedUser(theModifiedUser);
    }

    if (!isDefined(idRet)) {
      throw new JobDomainPeasException(
          failureOnUpdate("synchronized user", userRequestData.getId()));
    }
    refresh();
    setTargetUser(idRet);
  }

  public void blockUser(String userId) throws JobDomainPeasException {

    adminCtrl.blockUser(userId);
  }

  public void unblockUser(String userId) throws JobDomainPeasException {
    adminCtrl.unblockUser(userId);
  }

  public void deactivateUser(String userId) throws JobDomainPeasException {

    adminCtrl.deactivateUser(userId);
  }

  public void activateUser(String userId) throws JobDomainPeasException {
    adminCtrl.activateUser(userId);
  }

  public void restoreUser(String idUser) throws JobDomainPeasException {
    final String restoreUserId = adminCtrl.restoreUser(idUser);
    if (!isDefined(restoreUserId)) {
      throw new JobDomainPeasException(failureOnRestoring("user", idUser));
    }
    refresh();
  }

  public void removeUser(String idUser) throws JobDomainPeasException {
    final String removedUserId = adminCtrl.removeUser(idUser);
    if (!isDefined(removedUserId)) {
      throw new JobDomainPeasException(failureOnRemoving("user", idUser));
    }
    if (targetUserId.equals(idUser)) {
      targetUserId = null;
    }
    refresh();
  }

  public void deleteUser(String idUser) throws JobDomainPeasException {

    UserDetail user = getUserDetail(idUser);

    boolean deleteUser = true;

    if (!UserAccessLevel.ADMINISTRATOR.equals(getUserAccessLevel())
        && !UserAccessLevel.DOMAIN_ADMINISTRATOR.equals(getUserAccessLevel()) && isGroupManager()) {
      deleteUser = deleteUserByGroupManager(idUser);
    }

    if (deleteUser) {
      String idRet = adminCtrl.deleteUser(idUser);
      if (!isDefined(idRet)) {
        throw new JobDomainPeasException(failureOnDeleting("user", idUser));
      }
      if (idUser.equals(targetUserId)) {
        targetUserId = null;
      }

      if ((getDomainActions() & DomainDriver.ActionConstants.ACTION_X509_USER) != 0) {
        // revocate user's certificate
        revocateCertificate(user);
      }
      refresh();
    }
  }

  private boolean deleteUserByGroupManager(String userId) {
    boolean deleteUser = true;
    List<GroupDetail> directGroups = getOrganisationController().getDirectGroupsOfUser(userId);
    List<String> manageableGroupIds = getUserManageableGroupIds();

    String directGroupId;
    String rootGroupId;
    List<String> groupIdLinksToRemove = new ArrayList<>();
    for (GroupDetail directGroup : directGroups) {
      directGroupId = directGroup.getId();
      // get root group of each directGroup
      List<String> groupPath = adminCtrl.getPathToGroup(directGroupId);
      if (CollectionUtil.isNotEmpty(groupPath)) {
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
        adminCtrl.removeUserFromGroup(userId, groupIdLinkToRemove);
      }
      refresh();
    }
    return deleteUser;
  }

  public Iterator<DomainProperty> getPropertiesToImport() throws JobDomainPeasException {
    return adminCtrl.getSpecificPropertiesToImportUsers(targetDomainId,
        getLanguage()).iterator();
  }

  public void importUser(String userLogin) throws JobDomainPeasException {
    String idRet = adminCtrl.synchronizeImportUser(targetDomainId, userLogin);
    if (!isDefined(idRet)) {
      throw new JobDomainPeasException(failureOnAdding("synchronized user", userLogin));
    }
    refresh();
    setTargetUser(idRet);
  }

  public void importUsers(String[] specificIds) throws JobDomainPeasException {
    for (int i = 0; specificIds != null && i < specificIds.length; i++) {
      adminCtrl.synchronizeImportUser(targetDomainId, specificIds[i]);
    }
    refresh();
  }

  public List<UserDetail> searchUsers(Map<String, String> query) {
    queryToImport = query;
    usersToImport = adminCtrl.searchUsers(targetDomainId, query);
    return usersToImport;
  }

  public List<UserDetail> getUsersToImport() {
    return usersToImport;
  }

  public Map<String, String> getQueryToImport() {
    return queryToImport;
  }

  public UserFull getUser(String specificId) {
    return adminCtrl.getUserFull(targetDomainId, specificId);
  }

  public void synchroUser(String idUser) throws JobDomainPeasException {

    String idRet = adminCtrl.synchronizeUser(idUser);
    if (!isDefined(idRet)) {
      throw new JobDomainPeasException(failureOnAdding("synchronize user", idUser));
    }
    refresh();
    setTargetUser(idRet);
  }

  public void unsynchroUser(String idUser) throws JobDomainPeasException {
    String idRet = adminCtrl.synchronizeRemoveUser(idUser);
    if (!isDefined(idRet)) {
      throw new JobDomainPeasException(failureOnDeleting("synchronized user", idUser));
    }
    if (targetUserId.equals(idUser)) {
      targetUserId = null;
    }
    refresh();
  }

  /*
   * GROUP functions
   */
  public void returnIntoGroup(String groupId) throws JobDomainPeasException {
    if (!isDefined(groupId)) {
      groupsPath.clear();
    } else {
      int i = groupsPath.size() - 1;
      while (i >= 0 && !groupsPath.get(i).isThisGroup(groupId)) {
        groupsPath.remove(i);
        i--;
      }
    }
    setTargetUser(null);
  }

  private void removeGroupFromPath(String groupId) {
    if (isDefined(groupId)) {
      int i = 0;
      while (i < groupsPath.size() && !groupsPath.get(i).isThisGroup(groupId)) {
        i++;
      }
      if (i < groupsPath.size()) {
        groupsPath = groupsPath.subList(0, i);
      }
    }
  }

  /**
   * @param groupId
   * @throws JobDomainPeasException
   */
  public void goIntoGroup(String groupId) throws JobDomainPeasException {
    if (isDefined(groupId)) {
      if (getTargetGroup() == null
          || (getTargetGroup() != null && !getTargetGroup().getId().equals(
              groupId))) {
        Group targetGroup = adminCtrl.getGroupById(groupId);
        // Add user access control for security purpose
        if (isOnlySpaceManager() || isUserAuthorizedToManageGroup(targetGroup)) {
          if (GroupNavigationStock.isGroupValid(targetGroup)) {
            List<String> manageableGroupIds = null;
            if (isOnlyGroupManager() && !isGroupManagerOnGroup(groupId)) {
              manageableGroupIds = getUserManageableGroupIds();
            }
            GroupNavigationStock newSubGroup = new GroupNavigationStock(groupId, adminCtrl,
                manageableGroupIds);
            groupsPath.add(newSubGroup);
          }
        } else {
          SilverLogger.getLogger(this)
              .warn("Security Alert: the user id {0} is attempting to access group id {1}",
                  getUserId(), groupId);
        }
      }
    } else {
      throw new JobDomainPeasException(undefined("group"));
    }
    setTargetUser(null);
  }

  private boolean isUserAuthorizedToManageGroup(Group group) {
    if (getUserDetail().isAccessAdmin() ||
        adminCtrl.isDomainManagerUser(getUserId(), group.getDomainId())) {
      return true;
    }

    // check if current user is manager of this group or one of its descendants
    Group[] groups = new Group[1];
    groups[0] = group;
    Group[] allowedGroups = filterGroupsToGroupManager(groups);
    if (!ArrayUtil.isEmpty(allowedGroups)) {
      return true;
    }

    // check if current user is manager of at least one parent group
    return isGroupManagerOnGroup(group.getId());
  }

  public Group getTargetGroup() {
    if (groupsPath.isEmpty()) {
      return null;
    }
    return groupsPath.get(groupsPath.size()-1).getThisGroup();
  }

  /**
   * @return a List with 2 elements. First one, a List of UserDetail. Last one, a List of Group.
   * @throws JobDomainPeasException
   */
  public List<List> getGroupManagers() throws JobDomainPeasException {
    List<List> usersAndGroups = new ArrayList<>();
    List<UserDetail> users = new ArrayList<>();
    List<Group> groups = new ArrayList<>();

    GroupProfileInst profile = adminCtrl.getGroupProfile(getTargetGroup().getId());
    if (profile != null) {
      for (String groupId : profile.getAllGroups()) {
        groups.add(adminCtrl.getGroupById(groupId));
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
  public void initUserPanelForGroupManagers(List<String> userIds,
      List<String> groupIds) throws SelectionException {
    sel.resetAll();
    sel.setHostSpaceName(getMultilang().getString("JDP.jobDomain"));
    sel.setHostComponentName(new Pair<>(getTargetGroup().getName(), null));
    LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(getLanguage());
    Pair<String, String>[] hostPath =
        new Pair[]{new Pair<>(getMultilang().getString("JDP.roleManager") + " > " +
            generalMessage.getString("GML.selection"), null)};
    sel.setHostPath(hostPath);
    setDomainIdOnSelection(sel);
    sel.setPopupMode(true);
    sel.setHtmlFormElementId("roleItems");
    sel.setHtmlFormName("dummy");
    sel.setSelectedElements(userIds);
    sel.setSelectedSets(groupIds);
  }

  public void updateGroupProfile(List<String> userIds, List<String> groupIds)
      throws JobDomainPeasException {
    GroupProfileInst profile = adminCtrl.getGroupProfile(getTargetGroup().getId());
    profile.setUsers(userIds);
    profile.setGroups(groupIds);
    adminCtrl.updateGroupProfile(profile);
  }

  public boolean isGroupRoot(String groupId) {
    Group gr = adminCtrl.getGroupById(groupId);
    return GroupNavigationStock.isGroupValid(gr) && this.refreshDomain && (!isDefined(gr.
        getSuperGroupId()) || "-1".equals(gr.getSuperGroupId()));
  }

  public Group[] getSubGroups(boolean isParentGroup) throws JobDomainPeasException {
    Group[] groups;

    if (isParentGroup) {
      if (groupsPath.isEmpty()) {
        throw new JobDomainPeasException(failureOnGetting("subgroups", ""));
      }
      groups = groupsPath.get(groupsPath.size()-1).getGroupPage();
    } else {
      // Domain case
      groups = targetDomain.getGroupPage();
    }
    if (isOnlyGroupManager() && !isGroupManagerOnCurrentGroup()) {
      groups = filterGroupsToGroupManager(groups);
    }
    return groups;
  }

  public List<UserDetail> getSubUsers(boolean isParentGroup) throws JobDomainPeasException {
    final UserDetail[] usDetails;
    if (isParentGroup) {
      if (groupsPath.isEmpty()) {
        throw new JobDomainPeasException(failureOnGetting("users of subgroups", ""));
      }
      usDetails = groupsPath.get(groupsPath.size()-1).getUserPage();
    } else {
      // Domain case
      usDetails = targetDomain.getUserPage();
    }
    setSessionUsers(Arrays.asList(usDetails));
    return getSessionUsers();
  }

  public String getPath(String baseURL, String toAppendAtEnd) throws JobDomainPeasException {
    StringBuilder strPath = new StringBuilder("");

    for (int i = 0; i < groupsPath.size(); i++) {
      Group theGroup = groupsPath.get(i).getThisGroup();
      appendSeparator(strPath);
      if (((i + 1) < groupsPath.size()) || (targetUserId != null)
          || (toAppendAtEnd != null)) {
        strPath.append("<a href=\"").append(baseURL).append("groupReturn?Idgroup=").
            append(theGroup.getId()).append("\">").
            append(WebEncodeHelper.javaStringToHtmlString(theGroup.getName())).append("</a>");
      } else {
        strPath.append(WebEncodeHelper.javaStringToHtmlString(theGroup.getName()));
      }
    }
    if (targetUserId != null) {
      appendSeparator(strPath);
      if (toAppendAtEnd != null) {
        strPath.append("<a href=\"").append(baseURL).append("userContent?Iduser=").
            append(targetUserId).append("\">").
            append(WebEncodeHelper.javaStringToHtmlString(getTargetUserDetail().getDisplayedName())).
            append("</a>");
      } else {
        strPath.append(WebEncodeHelper
            .javaStringToHtmlString(getTargetUserDetail().getDisplayedName()));
      }
    }
    if (toAppendAtEnd != null) {
      appendSeparator(strPath);
      strPath.append(WebEncodeHelper.javaStringToHtmlString(toAppendAtEnd));
    }
    return strPath.toString();
  }

  private void appendSeparator(StringBuilder sb) {
    if (sb.length() > 0) {
      sb.append(" &gt ");
    }
  }

  public boolean createGroup(String idParent, String groupName,
      String groupDescription, String groupRule) throws JobDomainPeasException {
    GroupDetail theNewGroup = new GroupDetail();

    String rule = groupRule;
    boolean isSynchronizationToPerform = isDefined(groupRule);
    if (isSynchronizationToPerform) {
      rule = groupRule.trim();
    }

    theNewGroup.setId("-1");
    if (isDefined(targetDomainId) && !Domain.MIXED_DOMAIN_ID.equals(targetDomainId)) {
      theNewGroup.setDomainId(targetDomainId);
    }
    theNewGroup.setSuperGroupId(idParent);
    theNewGroup.setName(groupName);
    theNewGroup.setDescription(groupDescription);
    theNewGroup.setRule(rule);
    String idRet = adminCtrl.addGroup(theNewGroup);
    if (!isDefined(idRet)) {
      throw new JobDomainPeasException(failureOnAdding("group", groupName));
    }
    refresh();

    goIntoGroup(idRet);

    return isSynchronizationToPerform ? synchroGroup(idRet) : isGroupRoot(idRet);
  }

  public boolean modifyGroup(String idGroup, String groupName,
      String groupDescription, String groupRule) throws JobDomainPeasException {
    GroupDetail theModifiedGroup = adminCtrl.getGroupById(idGroup);
    if (theModifiedGroup == null) {
      throw new JobDomainPeasException(unknown("group", idGroup));
    }
    boolean isSynchronizationToPerform =
        isDefined(groupRule) && !groupRule.equalsIgnoreCase(theModifiedGroup.getRule());
    String rule = groupRule;
    if (isSynchronizationToPerform) {
      rule = rule.trim();
    }
    theModifiedGroup.setName(groupName);
    theModifiedGroup.setDescription(groupDescription);
    theModifiedGroup.setRule(rule);

    String idRet = adminCtrl.updateGroup(theModifiedGroup);
    if (!isDefined(idRet)) {
      throw new JobDomainPeasException(failureOnUpdate("group", idGroup));
    }
    refresh();
    return isSynchronizationToPerform ? synchroGroup(idRet) : isGroupRoot(idRet);
  }

  public boolean updateGroupSubUsers(String idGroup, String[] userIds)
      throws JobDomainPeasException {
    GroupDetail theModifiedGroup = adminCtrl.getGroupById(idGroup);
    if (theModifiedGroup == null) {
      throw new JobDomainPeasException(unknown("group", idGroup));
    }
    theModifiedGroup.setUserIds(userIds);
    String idRet = adminCtrl.updateGroup(theModifiedGroup);
    if ((idRet == null) || (idRet.length() <= 0)) {
      throw new JobDomainPeasException(failureOnUpdate("group", idGroup));
    }
    refresh();
    return true;
  }

  public boolean deleteGroup(String idGroup) throws JobDomainPeasException {
    String idRet = adminCtrl.deleteGroupById(idGroup);
    if (!isDefined(idRet)) {
      throw new JobDomainPeasException(failureOnDeleting("group", idGroup));
    }
    removeGroupFromPath(idGroup);
    refresh();
    return true;
  }

  public boolean synchroGroup(String idGroup) throws JobDomainPeasException {
    String synchronizationResult = adminCtrl.synchronizeGroup(idGroup);
    if (!isDefined(synchronizationResult)) {
      throw new JobDomainPeasException(failureOnAdding("synchronized group", idGroup));
    }
    if (StringUtil.isLong(synchronizationResult)) {
      refresh();
      return true;
    }
    if (synchronizationResult.startsWith("expression.")) {
      if (synchronizationResult.startsWith("expression.groundrule.unknown")) {
        final String[] keyRule = synchronizationResult.split("[|]");
        String msgKey = keyRule[0];
        String groundRule = "<b>" + keyRule[1] + "</b>";
        MessageNotifier.addError(
            MessageFormat.format(getString("JDP.groupSynchroRule." + msgKey), groundRule));
      } else {
        MessageNotifier.addError(getString("JDP.groupSynchroRule." + synchronizationResult));
      }
    } else {
      MessageNotifier.addError(synchronizationResult);
    }
    return false;
  }

  public boolean unsynchroGroup(String idGroup) throws JobDomainPeasException {
    final String idRet = adminCtrl.synchronizeRemoveGroup(idGroup);
    if (!isDefined(idRet)) {
      throw new JobDomainPeasException(failureOnDeleting("synchronized group", idGroup));
    }
    removeGroupFromPath(idGroup);
    refresh();
    return true;
  }

  public boolean importGroup(String groupName) {
    final Result<String> result = adminCtrl.synchronizeImportGroup(targetDomainId, groupName);
    if (result.getValue().filter(v -> !isDefined(v)).isPresent()) {
      final String errorMessage = result.getException()
          .filter(e -> e instanceof GroupAlreadyExistsAdminException)
          .map(e -> getString("JDP.error.import.group.already.exists"))
          .orElseGet(() -> getString("JDP.error.import.group.technical"));
      WebMessager.getInstance().addError(errorMessage);
      return false;
    }
    refresh();
    return true;
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
    if (!isDefined(domainId)) {
      targetDomain = null;
      targetDomainId = "";
    } else {
      List<String> manageableGroupIds = null;
      targetDomainId = domainId;
      if (isOnlyGroupManager()) {
        manageableGroupIds = getUserManageableGroupIds();
      }
      targetDomain = new DomainNavigationStock(domainId, adminCtrl, manageableGroupIds);
    }
  }

  public Domain getTargetDomain() {
    if (targetDomain == null) {
      return null;
    }
    return targetDomain.getThisDomain();
  }

  public long getDomainActions() {
    if (targetDomainId.length() > 0) {
      return adminCtrl.getDomainActions(targetDomainId);
    }
    return 0;
  }

  public List<Domain> getAllDomains() {
    List<Domain> domains = new ArrayList<>();
    UserDetail ud = getUserDetail();

    if (ud.isAccessDomainManager() || isOnlyGroupManager()) {
      if (ud.isAccessDomainManager()) {
        // return domain of user
        domains.add(adminCtrl.getDomain(ud.getDomainId()));
      }

      // and other domains of manageable groups
      List<Group> groups = getUserManageableGroups();
      for (Group group : groups) {
        Domain domain = adminCtrl.getDomain(group.getDomainId());
        if (!domains.contains(domain)) {
          if (domain.isMixedOne()) {
            domains.add(0, domain);
          } else {
            domains.add(domain);
          }
        }
      }
    } else if (ud.isAccessAdmin()) {
      // return mixed domain...
      domains.add(adminCtrl.getDomain(Domain.MIXED_DOMAIN_ID));

      // and all classic domains
      domains.addAll(Arrays.asList(adminCtrl.getAllDomains()));
    } else if (isOnlySpaceManager() || isCommunityManager()) {
      // return mixed domain...
      domains.add(adminCtrl.getDomain(Domain.MIXED_DOMAIN_ID));

      if (ud.isDomainRestricted()) {
        // domain of current user only...
        domains.add(adminCtrl.getDomain(ud.getDomainId()));
      } else {
        // all classic domains
        domains.addAll(Arrays.asList(adminCtrl.getAllDomains()));
      }
    }
    return domains;
  }

  public boolean isOnlyGroupManager() {
    return isGroupManager() && !isManagerOfCurrentDomain();
  }

  private boolean isManagerOfCurrentDomain() {
    if (getUserDetail().isAccessAdmin()) {
      return true;
    }
    if (getUserDetail().isAccessDomainManager()) {
      return getUserDetail().getDomainId().equals(targetDomainId);
    }
    return false;
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

  public boolean isGroupManagerOnGroup(String groupId) {
    List<String> manageableGroupIds = getUserManageableGroupIds();
    if (manageableGroupIds.contains(groupId)) {
      // Current user is directly manager of group
      return true;
    } else {
      List<String> groupPath = adminCtrl.getPathToGroup(groupId);

      groupPath.retainAll(manageableGroupIds);

      if (!groupPath.isEmpty()) {
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
    Group[] selGroupsArray = targetDomain.getAllGroupPage();

    if (isOnlyGroupManager()) {
      selGroupsArray = filterGroupsToGroupManager(selGroupsArray);
    }
    JobDomainSettings.sortGroups(selGroupsArray);
    return selGroupsArray;
  }

  private Group[] filterGroupsToGroupManager(Group[] groups) {
    return NavigationStock.filterGroupsToGroupManager(getUserManageableGroupIds(), groups);
  }

  public String createDomain(Domain theNewDomain, final DomainType domainType)
      throws JobDomainPeasException, JobDomainPeasTrappedException {
    String newDomainId;
    try {
      newDomainId = DomainServiceProvider.getDomainService(domainType).createDomain(theNewDomain);
      refresh();
    } catch (DomainCreationException e) {
      throw new JobDomainPeasException(e);
    } catch (DomainConflictException e) {
      JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.createDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DOMAIN_ALREADY_EXIST_DATABASE", e);
      final String goBackPage;
      if (DomainType.LDAP == domainType) {
        goBackPage = "displayDomainCreate";
      } else if (DomainType.GOOGLE == domainType) {
        goBackPage = "displayDomainGoogleCreate";
      } else {
        goBackPage = "displayDomain" + domainType + "Create";
      }
      trappedException.setGoBackPage(goBackPage);
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
      throw new JobDomainPeasException(e);
    } catch (DomainConflictException e) {
      JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.createSQLDomain()",
          SilverpeasException.ERROR, "admin.MSG_ERR_DOMAIN_ALREADY_EXIST", e);
      trappedException.setGoBackPage("displayDomainSQLCreate");
      throw trappedException;
    }

    return domainId;
  }

  public String modifyDomain(Domain domain, String usersInDomainQuotaMaxCount) throws JobDomainPeasException,
      JobDomainPeasTrappedException {
    Domain theNewDomain = getTargetDomain();

    checkDomainUnicityOnUpdate(theNewDomain.getId(), domain.getName());

    if (!isDefined(targetDomainId) || targetDomainId.equals(Domain.MIXED_DOMAIN_ID)) {
      throw new JobDomainPeasException(unknown("domain", domain.getName()));
    }
    theNewDomain.setName(domain.getName());
    theNewDomain.setDescription(domain.getDescription());
    theNewDomain.setDriverClassName(domain.getDriverClassName());
    theNewDomain.setPropFileName(domain.getPropFileName());
    theNewDomain.setAuthenticationServer(domain.getAuthenticationServer());
    theNewDomain.setSilverpeasServerURL(domain.getSilverpeasServerURL());

    String idRet = reallyUpdateDomain(theNewDomain, usersInDomainQuotaMaxCount);

    refresh();
    return idRet;
  }

  public String modifySQLDomain(String domainName, String domainDescription,
      String silverpeasServerURL, String usersInDomainQuotaMaxCount) throws JobDomainPeasException,
      JobDomainPeasTrappedException {
    Domain theNewDomain = getTargetDomain();

    checkDomainUnicityOnUpdate(theNewDomain.getId(), domainName);

    if (StringUtil.isNotDefined(targetDomainId) || targetDomainId.equals(Domain.MIXED_DOMAIN_ID)
        || "0".equals(targetDomainId)) {
      throw new JobDomainPeasException(unknown("domain", domainName));
    }
    theNewDomain.setName(domainName);
    theNewDomain.setDescription(domainDescription);
    theNewDomain.setSilverpeasServerURL(silverpeasServerURL);

    String idRet = reallyUpdateDomain(theNewDomain, usersInDomainQuotaMaxCount);

    refresh();
    return idRet;
  }

  private void checkDomainUnicityOnUpdate(String domainId, String domainName)
      throws JobDomainPeasTrappedException {
    // Vérif domainName unique dans la table ST_Domain
    JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
        "JobDomainPeasSessionController", SilverpeasException.WARNING,
        "jobDomainPeas.WARN_DOMAIN_SQL_NAME");
    trappedException.setGoBackPage("domainContent");
    Domain[] allDomains = adminCtrl.getAllDomains();
    for (Domain domain : allDomains) {
      if (!domain.getId().equals(domainId) && domain.getName().equalsIgnoreCase(domainName)) {
        throw trappedException;
      }
    }
  }

  private String reallyUpdateDomain(Domain domain, String usersInDomainQuotaMaxCount)
      throws JobDomainPeasException, JobDomainPeasTrappedException {
    try {

      boolean quotaDefined = isDefined(usersInDomainQuotaMaxCount);
      if (JobDomainSettings.usersInDomainQuotaActivated && quotaDefined) {
        // Getting quota filled
        domain.setUserDomainQuotaMaxCount(usersInDomainQuotaMaxCount);
      }

      String idRet = adminCtrl.updateDomain(domain);
      if (StringUtil.isNotDefined(idRet)) {
        throw new JobDomainPeasException(failureOnUpdate("domain", domain.getName()));
      }

      if (JobDomainSettings.usersInDomainQuotaActivated && quotaDefined) {
        // Registering "users in domain" quota
        DomainServiceProvider.getUserDomainQuotaService().initialize(
            UserDomainQuotaKey.from(domain), domain.getUserDomainQuota().getMaxCount());
      }

      return idRet;

    } catch (QuotaException qe) {
      JobDomainPeasTrappedException trappedException = new JobDomainPeasTrappedException(
          "JobDomainPeasSessionController.reallyUpdateDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_DOMAIN",
          getString("JDP.userDomainQuotaMaxCountError"), qe);
      trappedException.setGoBackPage("displayDomainSQLCreate");
      throw trappedException;
    }
  }

  public void deleteDomain(final DomainType domainType) throws JobDomainPeasException {
    try {
      DomainServiceProvider.getDomainService(domainType).deleteDomain(getTargetDomain());
    } catch (DomainDeletionException e) {
      throw new JobDomainPeasException(e);
    }
  }

  public void deleteSQLDomain() throws JobDomainPeasException {
    try {
      DomainServiceProvider.getDomainService(DomainType.SQL).deleteDomain(getTargetDomain());
      DomainServiceProvider.getUserDomainQuotaService().remove(
          UserDomainQuotaKey.from(getTargetDomain()));
    } catch (DomainDeletionException e) {
      throw new JobDomainPeasException(e);
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
    if (targetDomain != null) {
      targetDomain.refresh();
    }
    for (GroupNavigationStock aM_GroupsPath : groupsPath) {
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

    Selection selection = getSelection();
    selection.resetAll();
    selection.setFilterOnDeactivatedState(false);
    selection.setHostSpaceName(hostSpaceName);
    selection.setHostPath(hostPath);
    selection.setHostComponentName(hostComponentName);

    selection.setGoBackURL(hostUrl);
    selection.setCancelURL(cancelUrl);

    setDomainIdOnSelection(selection);

    selection.setSelectedElements(
        SelectionUsersGroups.getUserIds(groupsPath.get(groupsPath.size() - 1).getAllUserPage()));

    // Contraintes
    selection.setSetSelectable(false);
    selection.setPopupMode(false);
    return Selection.getSelectionURL();
  }

  private void setDomainIdOnSelection(Selection selection) {
    if (isDefined(targetDomainId) && !Domain.MIXED_DOMAIN_ID.equals(targetDomainId)) {
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

    Selection selection = getSelection();
    selection.resetAll();
    selection.setFilterOnDeactivatedState(false);
    selection.setHostSpaceName(hostSpaceName);
    selection.setHostPath(hostPath);
    selection.setHostComponentName(hostComponentName);

    selection.setGoBackURL(hostUrl);
    selection.setCancelURL(cancelUrl);

    if (!isDefined(targetDomainId) || Domain.MIXED_DOMAIN_ID.equals(targetDomainId)) {
      selection.setElementSelectable(false);
    }

    if (getTargetDomain() != null &&
        ("autDomainSCIM".equals(getTargetDomain().getAuthenticationServer())
        || "autDomainGoogle".equals(getTargetDomain().getAuthenticationServer()))) {
      selection.setSetSelectable(false);
    }

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setDomainId(targetDomainId);
    selection.setExtraParams(sug);

    // Contraintes
    selection.setMultiSelect(false);
    selection.setPopupMode(false);
    return Selection.getSelectionURL();
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
    if (synchroThread == null) {
      SynchroDomainReport.setReportLevel(Level.INFO);
      SynchroDomainReport.waitForStart();
      synchroThread = new SynchroWebServiceThread(this);
      errorOccured = null;
      synchroReport = "";
      synchroThread.startTheThread();
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

      Collection<Group> listGroupToInsertUpdate;
      SettingBundle propDomainSql = theDomain.getSettings();
      String nomClasseWebService = propDomainSql.getString("ExternalSynchroClass");

      // 1- Récupère la liste des groupes à synchroniser (en insert et update)
      listGroupToInsertUpdate = JobDomainPeasDAO.selectGroupSynchroInsertUpdateTableDomain_Group(
          theDomain);
      // 2- Traitement Domaine, appel aux webServices
      synchroUserWebService = (SynchroUserWebServiceItf) Class.forName(nomClasseWebService).
          newInstance();

      synchroUserWebService.startConnection();

      // Insertion / Update de la société
      sReport.append(synchroUserWebService.insertUpdateDomainWebService(theDomain.getId(),
          theDomain.getName()));

      // 3- Traitement groupes, appel aux webServices
      if (CollectionUtil.isNotEmpty(listGroupToInsertUpdate)) {
        // Insertion / Update des groupes
        sReport.append(synchroUserWebService.insertUpdateListGroupWebService(theDomain.getId(),
            theDomain.getName(), listGroupToInsertUpdate));
      }

      Collection<UserFull> listUserToInsertUpdate;
      Collection<UserDetail> listUserToDelete;

      // 4- Récupère la liste des users à synchroniser (en insert et update)
      listUserToInsertUpdate = JobDomainPeasDAO.selectUserSynchroInsertUpdateTableDomain_User(
          theDomain);
      // 5- Récupère la liste des users à synchroniser (en delete)
      listUserToDelete = JobDomainPeasDAO.selectUserSynchroDeleteTableDomain_User(theDomain);

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

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
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
    if (synchroThread == null) {
      SynchroDomainReport.setReportLevel(level);
      SynchroDomainReport.waitForStart();
      synchroThread = new SynchroLdapThread(this, adminCtrl, targetDomainId);
      errorOccured = null;
      synchroReport = "";
      synchroThread.startTheThread();
    }
  }

  public boolean isEnCours() {
    return synchroThread != null && synchroThread.isEnCours();
  }

  public String getSynchroReport() {
    if (errorOccured != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);

      errorOccured.printStackTrace(pw);
      return errorOccured.toString() + "\n" + sw.getBuffer().toString();
    }
    return synchroReport;
  }

  public void threadFinished() {
    errorOccured = synchroThread.getErrorOccured();
    synchroReport = synchroThread.getSynchroReport();
    synchroThread = null;
  }

  public void getP12(String userId) throws JobDomainPeasException {
    UserDetail user = getUserDetail(userId);

    try {
      X509Factory.buildP12(user.getId(), user.getLogin(), user.getLastName(), user.getFirstName(),
          user.getDomainId());
    } catch (UtilException e) {
      throw new JobDomainPeasException(e);
    }
  }

  private void revocateCertificate(UserDetail user)
      throws JobDomainPeasException {
    try {
      X509Factory.revocateUserCertificate(user.getId());
    } catch (UtilException e) {
      throw new JobDomainPeasException(e);
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
    UserDetail[] existingUsers = targetDomain.getAllUserPage();
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
      UserDetail user = getUser(targetUserId, users);

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

  private void processDataOfExtraTemplate(String userId, HttpRequest request) {
    PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
    PublicationTemplate template = templateManager.getDirectoryTemplate();
    if (template != null) {
      try {
        PagesContext context = getTemplateContext(userId);
        templateManager.saveData(template.getFileName(), context, request.getFileItems());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
        MessageNotifier.addError("Les données du formulaire n'ont pas été enregistrées !");
      }
    }
  }

  private PagesContext getTemplateContext(String userId) {
    return PagesContext.getDirectoryContext(userId, getUserId(), getLanguage());
  }

  public void deleteUserAvatar(String userId) {
    ImageProfil img = new ImageProfil(UserDetail.getById(userId).getAvatarFileName());
    img.removeImage();
  }

  @SuppressWarnings("unchecked")
  public List<Group> getCurrentUserGroups() {
    return (List) adminCtrl.getDirectGroupsOfUser(targetUserId);
  }

  public List<SpaceInstLight> getManageablesSpaces() {
    String[] spaceIds = new String[0];
    if (isDefined(targetUserId)) {
      if (User.getById(targetUserId).isAccessAdmin()) {
        return Collections.emptyList();
      }
      spaceIds = adminCtrl.getUserManageableSpaceIds(targetUserId);
    } else if (getTargetGroup() != null) {
      spaceIds = adminCtrl.getGroupManageableSpaceIds(getTargetGroup().getId());
    }

    if (ArrayUtil.isEmpty(spaceIds)) {
      return Collections.emptyList();
    }

    List<SpaceInstLight> spaces = new ArrayList<>();
    for (String spaceId : spaceIds) {
      spaces.add(adminCtrl.getSpaceInstLight(spaceId));
    }

    return spaces;
  }

  public List<Group> getManageablesGroups() {
    if (User.getById(targetUserId).isAccessAdmin()) {
      return Collections.emptyList();
    }

    List<Group> groups = new ArrayList<>();
    try {
      List<String> ids =
          AdministrationServiceProvider.getAdminService().getUserManageableGroupIds(targetUserId);
      for (String id : ids) {
        groups.add(Group.getById(id));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return groups;
  }

  /**
   * @return list of (array[space name, component id, component label, component name, profile
   * name])
   */
  public ComponentProfilesList getCurrentProfiles() {
    ComponentProfilesList allProfiles = new ComponentProfilesList();
    Stream<String> profileIds = Stream.empty();
    if (isDefined(targetUserId)) {
      profileIds = Stream.concat(profileIds, Stream.of(adminCtrl.getProfileIds(targetUserId)));
    } else if (getTargetGroup() != null) {
      // get profiles associated to group and its parents
      Optional<Group> group = Optional.of(getTargetGroup());
      while (group.isPresent()) {
        profileIds = Stream
            .concat(profileIds, Stream.of(adminCtrl.getProfileIdsOfGroup(group.get().getId())));
        group = group
            .filter(g -> !g.isRoot())
            .flatMap(g -> Optional.ofNullable(Group.getById(g.getSuperGroupId())));
      }
    }
    profileIds
        .map(i -> adminCtrl.getProfileInst(i))
        .forEach(p -> {
      Objects.requireNonNull(p);
      ComponentProfiles componentProfiles = allProfiles.getByLocalComponentInstanceId(p.getComponentFatherId());
      if (componentProfiles == null) {
        ComponentInstLight currentComponent =
            adminCtrl.getComponentInstLight(String.valueOf(p.getComponentFatherId()));
        if (currentComponent.getStatus() == null && !currentComponent.isPersonal()) {
          LocalizedComponent localizedComponent = getLocalizedComponent(currentComponent.getName());
          componentProfiles = new ComponentProfiles(currentComponent, localizedComponent);
          SpaceInstLight space = adminCtrl.getSpaceInstLight(currentComponent.getSpaceId());
          componentProfiles.setSpace(space);
          allProfiles.add(componentProfiles);
        }
      }
      if (componentProfiles != null) {
        componentProfiles.addProfile(p);
      }
    });
    allProfiles.sort(new AbstractComplexComparator<ComponentProfiles>() {
      private static final long serialVersionUID = 6776408278128213038L;
      @Override
      protected ValueBuffer getValuesToCompare(final ComponentProfiles object) {
        return new ValueBuffer().append(object.getSpace().getName(getLanguage()))
            .append(object.getComponent().getName(getLanguage()));
      }
    });
    return allProfiles;
  }

  private LocalizedComponent getLocalizedComponent(String name) {
    LocalizedComponent localizedComponent = localizedComponents.get(name);
    if (localizedComponent == null) {
      try {
        Optional<WAComponent> optionalComponent = WAComponent.getByName(name);
        if (optionalComponent.isPresent()) {
          WAComponent component = WAComponent.getByName(name).get();
          localizedComponent = new LocalizedComponent(component, getLanguage());
          localizedComponents.put(name, localizedComponent);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }
    return localizedComponent;
  }

  public boolean isRightCopyReplaceEnabled() {
    return getUserDetail().isAccessAdmin() &&
        getSettings().getBoolean("admin.profile.rights.copyReplace.activated", false);
  }

  /*
   * UserPanel initialization : a user or (exclusive) a group
   */
  public String initSelectionRightsUserOrGroup() {
    Selection selection = getSelection();
    selection.resetAll();
    selection.setFilterOnDeactivatedState(false);
    selection.setHostPath(null);

    selection.setHtmlFormName("rightsForm");
    selection.setHtmlFormElementName("sourceRightsName");
    selection.setHtmlFormElementId("sourceRightsId");
    selection.setHtmlFormElementType("sourceRightsType");

    selection.setMultiSelect(false);
    selection.setPopupMode(true);
    return Selection.getSelectionURL();
  }

  public void assignRights(String choiceAssignRights, String sourceRightsId,
      String sourceRightsType, boolean nodeAssignRights) {

    try {
      if (REPLACE_RIGHTS.equals(choiceAssignRights) || ADD_RIGHTS.equals(choiceAssignRights)) {
        RightAssignationContext.MODE operationMode =
            REPLACE_RIGHTS.equals(choiceAssignRights) ?
                RightAssignationContext.MODE.REPLACE : RightAssignationContext.MODE.COPY;

        if (Selection.TYPE_SELECTED_ELEMENT.equals(sourceRightsType)) {
          if (isDefined(targetUserId)) {
            adminCtrl
                .assignRightsFromUserToUser(operationMode, sourceRightsId, targetUserId,
                    nodeAssignRights, getUserId());
          } else if (getTargetGroup() != null) {
            adminCtrl
                .assignRightsFromUserToGroup(operationMode, sourceRightsId, getTargetGroup().getId(),
                    nodeAssignRights, getUserId());
          }
        } else if (Selection.TYPE_SELECTED_SET.equals(sourceRightsType)) {
          if (isDefined(targetUserId)) {
            adminCtrl
                .assignRightsFromGroupToUser(operationMode, sourceRightsId, targetUserId,
                    nodeAssignRights, getUserId());
          } else if (getTargetGroup() != null) {
            adminCtrl
                .assignRightsFromGroupToGroup(operationMode, sourceRightsId, getTargetGroup().getId(),
                    nodeAssignRights, getUserId());
          }
        }
        MessageNotifier.addSuccess(getString("JDP.rights.assign.MessageOk"));
      }
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
      MessageNotifier.addError(getString("JDP.rights.assign.MessageNOk"));
    }
  }

  private List<UserDetail> getSessionUsers() {
    return sessionUsers;
  }

  private void setSessionUsers(List<UserDetail> users) {
    sessionUsers = users;
  }

  public ListIndex getIndex() {
    return currentIndex;
  }

  private void processIndex(String userId) {
    UserDetail user = UserDetail.getById(userId);
    currentIndex.setCurrentIndex(getSessionUsers().indexOf(user));
    currentIndex.setNbItems(getSessionUsers().size());
  }

  public UserDetail getPrevious() {
    return getSessionUsers().get(currentIndex.getPreviousIndex());
  }

  public UserDetail getNext() {
    return getSessionUsers().get(currentIndex.getNextIndex());
  }

  public List<UserDetail> getRemovedUsers() throws AdminException {
    final List<UserDetail> removedUsers = adminCtrl.getRemovedUsersInDomain(this.targetDomainId);
    removedUsers.sort(Comparator
        .comparing(UserDetail::getStateSaveDate)
        .thenComparing(UserDetail::getLastName)
        .thenComparing(UserDetail::getFirstName)
        .thenComparing(UserDetail::getId));
    return removedUsers;
  }

  public List<UserDetail> getDeletedUsers() throws AdminException {
    return adminCtrl.getDeletedUsersInDomain(this.targetDomainId);
  }

  public void blankDeletedUsers(final List<String> userIds) throws AdminException {
    adminCtrl.blankDeletedUsers(targetDomainId, userIds);
  }

  public Optional<UserFilterManager> getUserFilterManager() throws AdminException {
    final DomainDriverManager driverManager = getCurrentDomainDriverManager();
    final DomainDriver driver = driverManager.getDomainDriver(getTargetDomain().getId());
    return driver.getUserFilterManager();
  }

  public User[] verifyUserFilterRule(final String rule) throws AdminException {
    final Optional<UserFilterManager> manager = getUserFilterManager();
    if (manager.isPresent()) {
      return manager.get().validateRule(rule);
    }
    return new User[0];
  }

  public void saveUserFilterRule(final String rule) throws AdminException {
    final Optional<UserFilterManager> manager = getUserFilterManager();
    if (manager.isPresent()) {
      manager.get().saveRule(rule);
    }
  }

  public boolean isOnlySpaceManager() {
    return !getUserDetail().isAccessAdmin() && !getUserDetail().isAccessDomainManager() &&
        !isOnlyGroupManager() && !isManagerOfCurrentDomain() &&
        ArrayUtil.isNotEmpty(getUserManageableSpaceIds());
  }
}