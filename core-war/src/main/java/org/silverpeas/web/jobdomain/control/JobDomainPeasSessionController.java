/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.web.jobdomain.control;

import org.apache.commons.fileupload.FileItem;
import org.apache.ecs.xhtml.br;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.LocalizedWAComponent;
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
import org.silverpeas.core.admin.service.*;
import org.silverpeas.core.admin.service.AdminController.Result;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.GroupSelection;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.*;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.exception.SilverpeasException;
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
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.TokenGeneratorProvider;
import org.silverpeas.core.security.token.synchronizer.SynchronizerToken;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplates;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.*;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.util.csv.CSVReader;
import org.silverpeas.core.util.csv.Variant;
import org.silverpeas.core.web.authentication.LoginServlet;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.AbstractAdminComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.web.util.ListIndex;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.Level;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.web.directory.servlets.ImageProfile;
import org.silverpeas.web.jobdomain.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Collections.synchronizedList;
import static java.util.stream.Collectors.toList;
import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.admin.domain.DomainDriverManagerProvider.getCurrentDomainDriverManager;
import static org.silverpeas.core.personalization.service.PersonalizationServiceProvider.getPersonalizationService;
import static org.silverpeas.kernel.bundle.ResourceLocator.getSettingBundle;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

public class JobDomainPeasSessionController extends AbstractAdminComponentSessionController {

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
  private Exception errorOccurred = null;
  private String synchroReport = "";
  private Selection sel = null;
  private List<UserDetail> usersToImport = null;
  private Map<String, String> queryToImport = null;
  private AdminController adminCtrl = null;
  private List<String> listSelectedUsers = synchronizedList(new ArrayList<>());
  // pagination de la liste des résultats
  private int indexOfFirstItemToDisplay = 0;
  private boolean refreshDomain = true;
  private final ListIndex currentIndex = new ListIndex(0);
  private List<UserDetail> sessionUsers = synchronizedList(new ArrayList<>());

  private static final Properties templateConfiguration = new Properties();
  private static final String USER_ACCOUNT_TEMPLATE_FILE = "userAccount_email";
  private static final List<String> USERTYPES =
      Arrays.asList("Admin", "AdminPdc", "AdminDomain", "User", "Guest");
  private static final String BR_ELEMENT = new br().toString();

  private final Map<String, LocalizedWAComponent> localizedComponents = new HashMap<>();
  private Token token = SynchronizerToken.NoneToken;

  /**
   * Apply in a secure way the specified function. The function will be executed only if the
   * specified token matches the current one. Otherwise a
   *
   * @param token a security token.
   * @param function the function to apply.
   * @throws Exception if the administrative task fails.
   */
  public void securelyApply(String token, AdminTask function) throws Exception {
    if (token == null || !this.token.isDefined() || !token.equals(this.token.getValue())) {
      throwForbiddenError();
    }
    function.run();
  }

  /**
   * Standard Session Controller constructor.
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
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

  /**
   * Dedicated for tests
   */
  protected JobDomainPeasSessionController(final MainSessionController controller,
      final ComponentContext context, final String localizedMessagesBundleName,
      final String iconFileName, final String settingsFileName) {
    super(controller, context, localizedMessagesBundleName, iconFileName, settingsFileName);
  }

  public int getMinLengthLogin() {
    return JobDomainSettings.getLoginMinLength();
  }

  public boolean isUserAddingAllowedForGroupManager() {
    return JobDomainSettings.isUserAddingAllowedForGroupManagers();
  }

  @Override
  public boolean isAccessGranted() {
    return getUserDetail().isAccessAdmin() || getUserDetail().isAccessDomainManager() ||
        isOnlySpaceManager() || !getUserManageableGroupIds().isEmpty();
  }

  public void checkDomainAccessGranted(final String domainId) {
    checkDomainAccessGranted(domainId, true);
  }

  public void checkCurrentDomainAccessGranted(final boolean readOnly) {
    final String domainId = Optional.ofNullable(getTargetDomain()).map(Domain::getId).orElse(null);
    checkDomainAccessGranted(domainId, readOnly);
  }

  void checkDomainAccessGranted(final String domainId, final boolean readOnly) {
    checkAccessGranted(domainId, new DomainAccessContext(), readOnly);
  }

  public void checkUserAccessGranted(final String userId, final boolean readOnly) {
    final User user = getUserDetail(userId);
    if (user == null || getTargetDomain() == null ||
        Domain.MIXED_DOMAIN_ID.equals(user.getDomainId())) {
      throwForbiddenError();
    } else {
      if (getTargetDomain().getId().equals(Domain.MIXED_DOMAIN_ID)) {
        checkDomainAccessGranted(Domain.MIXED_DOMAIN_ID, readOnly);
      } else if (getTargetDomain().getId().equals(user.getDomainId())) {
        checkAccessGranted(user.getDomainId(), new UserAccessContext(user), readOnly);
      } else {
        throwForbiddenError();
      }
    }
  }

  public void checkGroupAccessGranted(final String groupId, final boolean readOnly) {
    final Group group = getOrganisationController().getGroup(groupId);
    if (group == null) {
      throwForbiddenError();
    } else {
      final String domainId = defaultStringIfNotDefined(group.getDomainId(),
          Domain.MIXED_DOMAIN_ID);
      if (getTargetDomain() == null || !getTargetDomain().getId().equals(domainId)) {
        throwForbiddenError();
      } else {
        checkAccessGranted(domainId, new GroupAccessContext(group), readOnly);
      }
    }
  }

  private <T> void checkAccessGranted(final String domainId, final AccessContext<T> accessContext,
      final boolean readOnly) {
    final UserDetail ud = getUserDetail();
    final boolean granted = ud.isAccessAdmin() || isAccessGrantedOnNotFullAdminAccess(
        defaultStringIfNotDefined(domainId, Domain.MIXED_DOMAIN_ID), accessContext, readOnly);
    if (!granted) {
      throwForbiddenError();
    }
  }

  private <T> boolean isAccessGrantedOnNotFullAdminAccess(final String domainId,
      final AccessContext<T> accessContext,
      final boolean readOnly) {
    final UserDetail ud = getUserDetail();
    final boolean equalsUserDomain = domainId.equals(ud.getDomainId());
    boolean granted = ud.isAccessDomainManager() && equalsUserDomain;
    if (!granted && (readOnly || accessContext.getType().isGroup())) {
      Stream<Pair<Group, String>> groupStream = getUserManageableGroups().stream()
          .map(g -> Pair.of(g, defaultStringIfNotDefined(g.getDomainId(), Domain.MIXED_DOMAIN_ID)))
          .filter(p -> !ud.isDomainAdminRestricted() ||
              p.getSecond().equals(Domain.MIXED_DOMAIN_ID) ||
              p.getSecond().equals(ud.getDomainId()));
      if (!readOnly && accessContext.getType().isGroup()) {
        final Group aimedGroup = (Group) accessContext.getResource();
        if (aimedGroup == null) {
          groupStream = groupStream.filter(g -> false);
        } else {
          final List<String> aimedGroupPath = Stream.concat(
                  Stream.of(aimedGroup.getId()),
                  Optional.of(aimedGroup)
                      .filter(g -> isDefined(g.getSuperGroupId()))
                      .stream()
                      .flatMap(g -> getOrganisationController().getPathToGroup(g.getId()).stream()))
              .collect(toList());
          groupStream = groupStream.filter(p -> aimedGroupPath.contains(p.getFirst().getId()));
        }
      }
      granted = groupStream.map(Pair::getSecond).anyMatch(domainId::equals);
      if (!granted && readOnly) {
        granted = (equalsUserDomain || Domain.MIXED_DOMAIN_ID.equals(domainId)) && isOnlySpaceManager();
      }
    }
    return granted;
  }

  public void setRefreshDomain(boolean refreshDomain) {
    this.refreshDomain = refreshDomain;
  }

  /*
   * USER functions
   */
  public void setTargetUser(String userId) {
    if (isDefined(userId)) {
      checkUserAccessGranted(userId, true);
    }
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
   * @throws JobDomainPeasException if an error occurs.
   * @throws JobDomainPeasTrappedException if a trapped error occurs.
   */
  public void createUser(UserRequestData userRequestData, Map<String, String> properties,
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
    saveUserSettings(userRequestData, theNewUser);

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
      if (!userRequestData.isFromCSV()) {
        processDataOfExtraTemplate(uf.getId(), req);
      } else {
        processDataOfExtraTemplate(uf.getId(), properties);
      }

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
  }

  private void saveUserSettings(UserRequestData userRequestData, UserDetail theNewUser) {
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
        isDefined(user.getEmailAddress()) &&
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
    SettingBundle general = getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
    String loginPage = general.getString("loginPage", "/defaultLogin.jsp");

    UriBuilder uriBuilder = UriBuilder.fromPath(URLUtil.getFullApplicationURL(req) + loginPage);

    String domainId = user.getDomainId();
    if (isDefined(domainId) && !Domain.MIXED_DOMAIN_ID.equals(domainId) &&
        !loginPage.contains(LoginServlet.PARAM_DOMAINID)) {
      uriBuilder.queryParam(LoginServlet.PARAM_DOMAINID, domainId);
    }
    return uriBuilder.toTemplate();
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
    SilverpeasTemplate template = SilverpeasTemplates.createSilverpeasTemplate(configuration);
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
   * Regroups the targeted user into the specified group (only for SQL domain).
   *
   * @param properties properties of the regrouping
   * @param lastGroupId the identifier of the group
   * @throws JobDomainPeasException if an error occurs
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
            newUserIds = lNewUserIds.toArray(new String[0]);
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
        newUserIds = lNewUserIds.toArray(new String[0]);

        // Ajout de l'appartenance de l'utilisateur au groupe
        updateGroupSubUsers(group.getId(), newUserIds);
      }
    }
  }

  /**
   * Parse the CSV file.
   *
   * @param filePart the CSV file
   * @param data additional information
   * @param req the current HttpServletRequest
   * @throws JobDomainPeasTrappedException if an error occurs
   * @throws PublicationTemplateException if an error occurs
   */
  public void importCsvUsers(FileItem filePart, UserRequestData data, HttpRequest req)
      throws JobDomainPeasTrappedException, PublicationTemplateException {
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
    final CSVReader csvReader = new CSVReader(getLanguage());
    final SettingBundle domainSettings = getSettingBundle(getTargetDomain().getPropFileName());
    final String propertyPrefix = "property";
    csvReader.initCSVFormat("org.silverpeas.jobDomainPeas.settings.usersCSVFormat", "User", ";",
        domainSettings, propertyPrefix, l -> {
          int numberOfSpecificCols = domainSettings.getInteger(propertyPrefix + ".Number", -1);
          Stream<String> specificFieldStream = l.stream();
          if ("0".equals(getTargetDomain().getId())) {
            // specific password fields of Silverpeas's domain are ignored
            final List<String> passwordFilteredList = specificFieldStream
                .filter(p -> !p.startsWith("password"))
                .collect(toList());
            specificFieldStream = passwordFilteredList.stream();
            numberOfSpecificCols -= (l.size() - passwordFilteredList.size());
          }
          if (numberOfSpecificCols > -1) {
            specificFieldStream = specificFieldStream.limit(numberOfSpecificCols);
          }
          final List<String> finalSpecificFieldList = specificFieldStream.collect(toList());
          if (numberOfSpecificCols > finalSpecificFieldList.size()) {
            throw new SilverpeasRuntimeException(String.format(
                "The domain property file %s.properties is specifying %s specific(s) column(s), " +
                    "but only %s are defined",
                domainSettings.getBaseBundleName(), numberOfSpecificCols,
                finalSpecificFieldList.size()));
          }
          return finalSpecificFieldList;
        });
    final boolean importExtraFormData = req.getParameterAsBoolean("importExtraForm");
    if (importExtraFormData) {
      PublicationTemplate template = getDirectoryExtraForm();
      if (template != null) {
        String[] extraFields = template.getRecordTemplate().getFieldNames();
        for (String extraField : extraFields) {
          csvReader.addSpecificCol(extraField, 2000, Variant.TYPE_STRING, "false");
        }
      }
    }

    boolean ignoreFirstLine = req.getParameterAsBoolean("ignoreFirstLine");
    Variant[][] csvValues;
    try {
      csvValues = csvReader.parseStream(is, ignoreFirstLine);
    } catch (UtilTrappedException ute) {
      JobDomainPeasTrappedException e = new JobDomainPeasTrappedException(
          IMPORT_CSV_USERS_OPERATION,
          SilverpeasException.ERROR, ERROR_CSV_FILE, ute.getExtraInfos(), ute);
      e.setGoBackPage("displayUsersCsvImport");
      throw e;
    }

    checkCSVFileContent(csvValues, csvReader, ignoreFirstLine);

    // no error, importing users...
    HashMap<String, String> properties;
    int nbCreatedUsers = 0;
    for (Variant[] csvValue : csvValues) {

      UserRequestData userRequestData = new UserRequestData();
      userRequestData.setFromCSV(true);
      userRequestData.setLogin(csvValue[2].getValueString());
      userRequestData.setLastName(csvValue[0].getValueString());
      userRequestData.setFirstName(csvValue[1].getValueString());
      userRequestData.setEmail(csvValue[3].getValueString());

      // Droits
      userRequestData.setAccessLevel(getAccessLevelFromCSV(csvValue[4].getValueString()));

      String motDePasse = csvValue[5].getValueString();
      // password is not mandatory
      userRequestData.setPasswordValid(isDefined(motDePasse));
      userRequestData.setPassword(motDePasse);

      // données spécifiques
      properties = new HashMap<>();
      if (csvReader.getSpecificNbCols() > 0) {
        for (int j = 0; j < csvReader.getSpecificNbCols(); j++) {
          String paramName = csvReader.getSpecificParameterName(j);
          if (Variant.TYPE_STRING.equals(csvReader.getSpecificColType(j))) {
            String informationSpecifiqueString = csvValue[j + 6].getValueString();
            properties.put(paramName, informationSpecifiqueString);
          } else if (Variant.TYPE_BOOLEAN.equals(csvReader.getSpecificColType(j))) {
            boolean informationSpecifiqueBoolean = csvValue[j + 6].getValueBoolean();
            if (informationSpecifiqueBoolean) {
              properties.put(paramName, "1");
            } else {
              properties.put(paramName, "0");
            }
          }
        }
      }

      userRequestData.setSendEmail(data.isSendEmail());
      userRequestData.setExtraMessage(data.getExtraMessage());
      userRequestData.setUserManualNotifReceiverLimitEnabled(true);
      try {
        createUser(userRequestData, properties, req);
        nbCreatedUsers++;
      } catch (JobDomainPeasException e) {
        throw new JobDomainPeasTrappedException(IMPORT_CSV_USERS_OPERATION,
            SilverpeasException.ERROR, ERROR_CSV_FILE, e);
      }
    }
    MessageNotifier.addSuccess(getString("JDP.csvImport.nbUsers"), nbCreatedUsers);
  }

  private void checkCSVFileContent(Variant[][] csvValues, CSVReader csvReader,
      boolean ignoreFirstLine) throws JobDomainPeasTrappedException {
    StringBuilder listErrors = new StringBuilder();
    for (int i = 0; i < csvValues.length; i++) {

      int lineNumber = i + 1;
      if (ignoreFirstLine) {
        lineNumber = i + 2;
      }

      // Nom
      String nom = csvValues[i][0].getValueString();
      listErrors.append(checkCSVData(nom, lineNumber, 1, true, 100));

      // Prenom
      String prenom = csvValues[i][1].getValueString();
      listErrors.append(checkCSVData(prenom, lineNumber, 2, false, 100));

      // Login
      String login = csvValues[i][2].getValueString();
      checkLoginDuringCSVImport(login, lineNumber, listErrors);

      // Email
      String email = csvValues[i][3].getValueString();
      listErrors.append(checkCSVData(email, lineNumber, 4, false, 100));

      // Droits
      String droits = csvValues[i][4].getValueString();
      checkUserTypeDuringCSVImport(droits, lineNumber, listErrors);

      // MotDePasse
      String motDePasse = csvValues[i][5].getValueString();
      checkPasswordDuringCSVImport(motDePasse, lineNumber, listErrors);

      if (csvReader.getSpecificNbCols() > 0) {
        for (int j = 0; j < csvReader.getSpecificNbCols(); j++) {
          if (Variant.TYPE_STRING.equals(csvReader.getSpecificColType(j))) {
            String informationSpecifiqueString = csvValues[i][j + 6].getValueString();

            // verify the length
            listErrors.append(checkCSVData(informationSpecifiqueString, lineNumber, j + 6, false,
                csvReader.getSpecificColMaxLength(j)));
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
  }

  private void checkLoginDuringCSVImport(String login, int lineNumber, StringBuilder listErrors) {
    String loginError = checkCSVData(login, lineNumber, 3, true, 50);
    if (StringUtil.isDefined(loginError)) {
      listErrors.append(loginError);
    } else if (login.length() < JobDomainSettings.getLoginMinLength()) {// verifier
      listErrors.append(getErrorMessage(lineNumber, 3, login));
      listErrors.append(getString("JDP.nbCarMin")).append(" ").append(
              JobDomainSettings.getLoginMinLength()).append(" ").append(getString("JDP.caracteres")).
          append(BR_ELEMENT);
    } else {
      // verif login unique
      String existingLogin = adminCtrl.getUserIdByLoginAndDomain(login, targetDomainId);
      if (existingLogin != null) {
        listErrors.append(getErrorMessage(lineNumber, 3, login));
        listErrors.append(getString("JDP.existingLogin")).append(BR_ELEMENT);
      }
    }
  }

  private void checkPasswordDuringCSVImport(String pwd, int lineNumber, StringBuilder listErrors) {
    // password is not mandatory
    if (isDefined(pwd)) {
      // Cheking password
      String passwordError = checkCSVData(pwd, lineNumber, 6, false, 32);
      if (StringUtil.isDefined(passwordError)) {
        listErrors.append(passwordError);
      } else {
        PasswordCheck passwordCheck =
            PasswordRulesServiceProvider.getPasswordRulesService().check(pwd);
        if (!passwordCheck.isCorrect()) {
          listErrors.append(getErrorMessage(lineNumber, 6, pwd))
              .append(passwordCheck.getFormattedErrorMessage(getLanguage()));
          listErrors.append(BR_ELEMENT);
        }
      }
    }
  }

  private void checkUserTypeDuringCSVImport(String type, int lineNumber, StringBuilder listErrors) {
    if (!"".equals(type) && !USERTYPES.contains(type)) {
      listErrors.append(getErrorMessage(lineNumber, 5, type));
      listErrors.append(getString("JDP.valeursPossibles")).append(BR_ELEMENT);
    }
  }

  private String checkCSVData(String data, int lineNumber, int column, boolean mandatory,
      int maxLength) {
    String result = "";
    if (mandatory && data.isEmpty()) {
      // champ obligatoire
      result +=
          getErrorMessage(lineNumber, column, data) + getString("JDP.obligatoire") + BR_ELEMENT;
    } else if (maxLength > -1 && data.length() > maxLength) {
      result +=
          getErrorMessage(lineNumber, column, data) + getString("JDP.nbCarMax") + " " + maxLength +
              " " + getString("JDP.caracteres") + BR_ELEMENT;
    }
    return result;
  }

  public Map<String, String> getFieldLabelsOfCSVToImport()
      throws AdminException, PublicationTemplateException, FormException {
    final Map<String, String> fieldsByCategory = new LinkedHashMap<>();
    fieldsByCategory.put("MANDATORY", String.join(";",
        getString("GML.lastName"),
        getString("GML.firstName"),
        getString("GML.login"),
        getString("GML.eMail"),
        getString("GML.type") + "<span class='user-type-list'>(Admin, AdminPdc, AdminDomain, " +
            "User, Guest)</span>",
        getString("GML.password")));
    // add complementary fields
    final DomainDriver driver =
        getCurrentDomainDriverManager().getDomainDriver(getTargetDomain().getId());
    final Map<String, String> propertiesLabels = driver.getPropertiesLabels(getLanguage());
    if (!propertiesLabels.isEmpty()) {
      fieldsByCategory.put("COMPLEMENTARY", Stream.of(driver.getPropertiesNames())
          .filter(p -> !p.startsWith("password"))
          .map(propertiesLabels::get)
          .collect(Collectors.joining(";")));
    }
    // add personalized fields
    final PublicationTemplate extraForm = getDirectoryExtraForm();
    if (extraForm != null) {
      final FieldTemplate[] extraFields = extraForm.getRecordTemplate().getFieldTemplates();
      fieldsByCategory.put("PERSONALIZED", Stream.of(extraFields)
          .map(f -> f.getLabel(getLanguage()))
          .collect(Collectors.joining(";")));
    }
    return fieldsByCategory;
  }

  private UserAccessLevel getAccessLevelFromCSV(String droits) {
    if ("Admin".equals(droits)) {
      return UserAccessLevel.ADMINISTRATOR;
    } else if ("AdminPdc".equals(droits)) {
      return UserAccessLevel.PDC_MANAGER;
    } else if ("AdminDomain".equals(droits)) {
      return UserAccessLevel.DOMAIN_ADMINISTRATOR;
    } else if ("User".equals(droits)) {
      return UserAccessLevel.USER;
    } else if ("Guest".equals(droits)) {
      return UserAccessLevel.GUEST;
    }
    return UserAccessLevel.USER;
  }

  private String getErrorMessage(int line, int column, String value) {
    return getString("JDP.ligne") + " = " + line + ", " +
        getString("JDP.colonne") + " = " + column + ", " +
        getString("JDP.valeur") + " = " + StringUtil.truncate(value, 100) + ", ";
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
          String value;
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
   * @throws JobDomainPeasException if an error occurs
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
    notifyUserAccount(userRequestData, theModifiedUser, req, false);
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

    String idRet;
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

  public void blockUser(String userId) {

    adminCtrl.blockUser(userId);
  }

  public void unblockUser(String userId) {
    adminCtrl.unblockUser(userId);
  }

  public void deactivateUser(String userId) {
    adminCtrl.deactivateUser(userId);
  }

  public void activateUser(String userId) {
    adminCtrl.activateUser(userId);
  }

  public void hideUserSensitiveData(String userId) {
    adminCtrl.setUserSensitiveData(userId, true);
  }

  public void showUserSensitiveData(String userId) {
    adminCtrl.setUserSensitiveData(userId, false);
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
        revokeCertificate(user);
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

  public Iterator<DomainProperty> getPropertiesToImport() {
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

  public void importUsers(String[] specificIds) {
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

  public void synchroUser(String userId) throws JobDomainPeasException {
    String idRet = adminCtrl.synchronizeUser(userId);
    if (!isDefined(idRet)) {
      throw new JobDomainPeasException(failureOnAdding("synchronize user", userId));
    }
    refresh();
    final User user = adminCtrl.getUserDetail(userId);
    if (!user.isRemovedState()) {
      setTargetUser(idRet);
    } else {
      MessageNotifier.addWarning(getString("JDP.userSynchro.removed"));
    }
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
  public void returnIntoGroup(String groupId) {
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
    return groupsPath.get(groupsPath.size() - 1).getThisGroup();
  }

  /**
   * @return a List with 2 elements. First one, a List of UserDetail. Last one, a List of Group.
   */
  public List<List<?>> getGroupManagers() {
    List<List<?>> usersAndGroups = new ArrayList<>();
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
  public void initUserPanelForGroupManagers(List<String> userIds, List<String> groupIds) {
    sel.resetAll();
    sel.setHostSpaceName(getMultilang().getString("JDP.jobDomain"));
    sel.setHostComponentName(new Pair<>(getTargetGroup().getName(), null));
    LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(getLanguage());
    //noinspection unchecked
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

  public void updateGroupProfile(List<String> userIds, List<String> groupIds) {
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

  /**
   * Gets all the root groups of the current user domain, split in two according their type: the
   * first ones are those managed by the domain whereas the second ones are the application managed
   * groups. Later are defined only in the mixed domain so for any other user domains the second
   * returned array in the pair is empty.
   *
   * @return a pair of two arrays: the first one with the groups managed by the current user domain
   * and the second one the groups managed by applications. Latter is empty if the user domain isn't
   * the mixed one.
   * @throws JobDomainPeasException if an error occurs while getting the root groups of the current
   * domain.
   */
  public Pair<Group[], Group[]> getRootGroupsPerType() throws JobDomainPeasException {
    var rootGroups = getSubGroups(false);
    if (!getTargetDomain().isMixedOne()) {
      return Pair.of(rootGroups, new Group[0]);
    }
    var allRootGroups = splitGroupsByType(rootGroups);
    return Pair.of(allRootGroups.getFirst().toArray(new Group[0]),
        allRootGroups.getSecond().toArray(new Group[0]));
  }

  public Group[] getSubGroups(boolean isParentGroup) throws JobDomainPeasException {
    Group[] groups;

    if (isParentGroup) {
      if (groupsPath.isEmpty()) {
        throw new JobDomainPeasException(failureOnGetting("subgroups", ""));
      }
      groups = groupsPath.get(groupsPath.size() - 1).getGroupPage();
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
      usDetails = groupsPath.get(groupsPath.size() - 1).getUserPage();
    } else {
      // Domain case
      usDetails = targetDomain.getUserPage();
    }
    setSessionUsers(Arrays.asList(usDetails));
    return getSessionUsers();
  }

  public String getPath(String baseURL, String toAppendAtEnd) throws JobDomainPeasException {
    StringBuilder strPath = new StringBuilder();

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
    GroupDetail parent = adminCtrl.getGroupById(idParent);
    if (parent != null && parent.isCommunityGroup()) {
      throw new JobDomainPeasException(idParent + " is an application managed group");
    }

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

    return isSynchronizationToPerform ? synchroGroup(idRet).isPresent() : isGroupRoot(idRet);
  }

  public boolean modifyGroup(String idGroup, String groupName,
      String groupDescription, String groupRule) throws JobDomainPeasException {
    GroupDetail theModifiedGroup = adminCtrl.getGroupById(idGroup);
    if (theModifiedGroup == null) {
      throw new JobDomainPeasException(unknown("group", idGroup));
    }
    if (theModifiedGroup.isCommunityGroup()) {
      throw new JobDomainPeasException(idGroup + " is an application managed group");
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
    return isSynchronizationToPerform ? synchroGroup(idRet).isPresent() : isGroupRoot(idRet);
  }

  public boolean updateGroupSubUsers(String idGroup, String[] userIds)
      throws JobDomainPeasException {
    GroupDetail theModifiedGroup = adminCtrl.getGroupById(idGroup);
    if (theModifiedGroup == null) {
      throw new JobDomainPeasException(unknown("group", idGroup));
    }
    theModifiedGroup.setUserIds(userIds);
    String idRet = adminCtrl.updateGroup(theModifiedGroup);
    if ((idRet == null) || (idRet.isEmpty())) {
      throw new JobDomainPeasException(failureOnUpdate("group", idGroup));
    }
    refresh();
    return true;
  }

  public List<GroupDetail> getRemovedGroups() throws AdminException {
    final List<GroupDetail> removedGroups = adminCtrl.getRemovedGroupsInDomain(this.targetDomainId);
    removedGroups.sort(Comparator
        .comparing(GroupDetail::getStateSaveDate)
        .thenComparing(GroupDetail::getName)
        .thenComparing(GroupDetail::getId));
    return removedGroups;
  }

  public boolean restoreGroup(String groupId) {
    if (adminCtrl.restoreGroupById(groupId).isEmpty()) {
      SilverLogger.getLogger(this).warn(format("Group {0} is already restored", groupId));
    }
    refresh();
    return true;
  }

  public boolean removeGroup(String groupId) throws JobDomainPeasException {
    var group =  adminCtrl.getGroupById(groupId);
    if (group != null && group.isCommunityGroup()) {
      throw new JobDomainPeasException(groupId + " is an application managed group");
    }
    if (adminCtrl.removeGroupById(groupId).isEmpty()) {
      SilverLogger.getLogger(this).warn(format("Group {0} is already removed", groupId));
    }
    removeGroupFromPath(groupId);
    refresh();
    return true;
  }

  public boolean deleteGroup(String groupId) throws JobDomainPeasException {
    boolean deleted = false;
    var group = adminCtrl.getGroupById(groupId);
    if (group != null) {
      if (group.isCommunityGroup()) {
        throw new JobDomainPeasException(groupId + " is an application managed group");
      }
      deleted = !adminCtrl.deleteGroupById(groupId).isEmpty();
    }
    if (!deleted) {
      SilverLogger.getLogger(this).warn(format("Group {0} is already deleted", groupId));
    }
    removeGroupFromPath(groupId);
    refresh();
    return true;
  }

  public Optional<Group> synchroGroup(String groupId) throws JobDomainPeasException {
    String synchronizationResult = adminCtrl.synchronizeGroup(groupId);
    if (!isDefined(synchronizationResult)) {
      throw new JobDomainPeasException(failureOnAdding("synchronized group", groupId));
    }
    if (StringUtil.isLong(synchronizationResult)) {
      final GroupDetail group = adminCtrl.getGroupById(synchronizationResult);
      if (group.isRemovedState()) {
        MessageNotifier.addWarning(getString("JDP.groupSynchro.removed"));
        removeGroupFromPath(groupId);
      }
      refresh();
      return Optional.of(group);
    }
    if (synchronizationResult.startsWith("expression.")) {
      if (synchronizationResult.startsWith("expression.groundrule.unknown")) {
        final String[] keyRule = synchronizationResult.split("[|]");
        String msgKey = keyRule[0];
        String groundRule = "<b>" + keyRule[1] + "</b>";
        MessageNotifier.addError(
            format(getString("JDP.groupSynchroRule." + msgKey), groundRule));
      } else {
        MessageNotifier.addError(getString("JDP.groupSynchroRule." + synchronizationResult));
      }
    } else {
      MessageNotifier.addError(synchronizationResult);
    }
    return Optional.empty();
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
      checkDomainAccessGranted(domainId);
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
    if (!targetDomainId.isEmpty()) {
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
    } else if (isOnlySpaceManager()) {
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
    return isGroupManager() && isNotManagerOfCurrentDomain();
  }

  private boolean isNotManagerOfCurrentDomain() {
    if (getUserDetail().isAccessAdmin()) {
      return false;
    }
    if (getUserDetail().isAccessDomainManager()) {
      return !getUserDetail().getDomainId().equals(targetDomainId);
    }
    return true;
  }

  public boolean isGroupManagerOnCurrentGroup() {
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

      // Current user is at least manager of one super group of group
      return !groupPath.isEmpty();
    }
  }

  public boolean isGroupManagerDirectlyOnCurrentGroup() {
    List<String> manageableGroupIds = getUserManageableGroupIds();
    return manageableGroupIds.contains(getTargetGroup().getId());
  }

  /**
   * Gets all the root groups of the current user domain. The groups are splitted in two according
   * to their type. The first ones are the domain groups whereas the second ones are the application
   * groups. Application groups are only defined in the mixed domain; in any other domain, there is
   * no application groups.
   *
   * @return a pair with the domain's user groups and with the application groups.
   */
  public Pair<Group[], Group[]> getAllRootGroups() {
    if (targetDomainId.isEmpty()) {
      return Pair.of(new Group[0], new Group[0]);
    }
    Group[] selGroupsArray = targetDomain.getGroupPage();

    if (isOnlyGroupManager()) {
      selGroupsArray = filterGroupsToGroupManager(selGroupsArray);
    }

    var allRootGroups = splitGroupsByType(selGroupsArray);
    allRootGroups.getFirst().sort(Group::compareTo);
    allRootGroups.getSecond().sort(Group::compareTo);
    return Pair.of(allRootGroups.getFirst().toArray(new Group[0]),
        allRootGroups.getSecond().toArray(new Group[0]));
  }

  private Pair<List<Group>, List<Group>> splitGroupsByType(Group[] groups) {
    List<Group> domainGroups = new ArrayList<>();
    List<Group> applicationGroups = new ArrayList<>();
    for (Group group : groups) {
      if (group.isCommunityGroup()) {
        applicationGroups.add(group);
      } else {
        domainGroups.add(group);
      }
    }
    return Pair.of(domainGroups, applicationGroups);
  }

  private Group[] filterGroupsToGroupManager(Group[] groups) {
    return NavigationStock.filterGroupsToGroupManager(getUserManageableGroupIds(), groups);
  }

  public String createDomain(Domain theNewDomain, final DomainType domainType)
      throws JobDomainPeasException, JobDomainPeasTrappedException {
    String newDomainId;
    try {
      newDomainId = Objects.requireNonNull(
          DomainServiceProvider.getDomainService(domainType)).createDomain(theNewDomain);
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
      if (JobDomainSettings.isUsersInDomainQuotaEnabled()) {
        domainToCreate.setUserDomainQuotaMaxCount(usersInDomainQuotaMaxCount);
      }

      domainId = Objects.requireNonNull(
          DomainServiceProvider.getDomainService(DomainType.SQL)).createDomain(domainToCreate);
      domainToCreate.setId(domainId);

      if (JobDomainSettings.isUsersInDomainQuotaEnabled()) {
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
      if (JobDomainSettings.isUsersInDomainQuotaEnabled() && quotaDefined) {
        // Getting quota filled
        domain.setUserDomainQuotaMaxCount(usersInDomainQuotaMaxCount);
      }

      String idRet = adminCtrl.updateDomain(domain);
      if (StringUtil.isNotDefined(idRet)) {
        throw new JobDomainPeasException(failureOnUpdate("domain", domain.getName()));
      }

      if (JobDomainSettings.isUsersInDomainQuotaEnabled() && quotaDefined) {
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
      Objects.requireNonNull(
          DomainServiceProvider.getDomainService(domainType)).deleteDomain(getTargetDomain());
    } catch (DomainDeletionException e) {
      throw new JobDomainPeasException(e);
    }
  }

  public void deleteSQLDomain() throws JobDomainPeasException {
    try {
      Objects.requireNonNull(
          DomainServiceProvider.getDomainService(DomainType.SQL)).deleteDomain(getTargetDomain());
      DomainServiceProvider.getUserDomainQuotaService().remove(
          UserDomainQuotaKey.from(getTargetDomain()));
    } catch (DomainDeletionException e) {
      throw new JobDomainPeasException(e);
    }
  }

  public void refresh() {
    if (targetDomain != null) {
      targetDomain.refresh();
    }
    for (GroupNavigationStock groupPath : new ArrayList<>(groupsPath)) {
      groupPath.refresh();
    }
    setTargetUser(null);
  }

  public void setFilterOnUserState(final String userState) {
    if (targetDomain != null) {
      UserState state = UserState.from(userState);
      targetDomain.filterOnUserState(state);
    }
  }

  /*
   * Selection Peas functions
   */
  public String initSelectionPeasForGroups(String compoURL) {
    String hostSpaceName = getString("JDP.userPanelGroup");
    Pair<String, String> hostComponentName = new Pair<>(getTargetGroup().getName(),
        compoURL + "groupContent");
    //noinspection unchecked
    Pair<String, String>[] hostPath = new Pair[0];
    String hostUrl = compoURL + "groupAddRemoveUsers";
    String cancelUrl = compoURL + "groupContent";

    Selection selection = setupSelection(hostSpaceName, hostComponentName, hostPath, hostUrl,
        cancelUrl);

    setDomainIdOnSelection(selection);

    selection.setSelectedElements(
        SelectionUsersGroups.getUserIds(groupsPath.get(groupsPath.size() - 1).getUserPage()));

    // Contraintes
    selection.setSetSelectable(false);
    selection.setPopupMode(false);
    return Selection.getSelectionURL();
  }

  private Selection setupSelection(String hostSpaceName, Pair<String, String> hostComponentName,
      Pair<String, String>[] hostPath, String hostUrl, String cancelUrl) {
    Selection selection = getSelection();
    selection.resetAll();
    selection.setFilterOnDeactivatedState(false);
    selection.setHostSpaceName(hostSpaceName);
    selection.setHostPath(hostPath);
    selection.setHostComponentName(hostComponentName);

    selection.setGoBackURL(hostUrl);
    selection.setCancelURL(cancelUrl);
    return selection;
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
  public String initSelectionPeasForOneGroupOrUser(String compoURL) {
    String hostSpaceName = getString("JDP.userPanelDomain");
    Pair<String, String> hostComponentName = new Pair<>(getTargetDomain().getName(),
        compoURL + "domainContent");
    //noinspection unchecked
    Pair<String, String>[] hostPath = new Pair[0];
    String hostUrl = compoURL + "selectUserOrGroup";
    String cancelUrl = compoURL + "domainContent";

    Selection selection = setupSelection(hostSpaceName, hostComponentName, hostPath, hostUrl,
        cancelUrl);

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
      errorOccurred = null;
      synchroReport = "";
      synchroThread.startTheThread();
    }
  }

  @SuppressWarnings("unchecked")
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
      Constructor<SynchroUserWebServiceItf> constructor = (Constructor<SynchroUserWebServiceItf>)
          Class.forName(nomClasseWebService).getConstructor();
      synchroUserWebService = constructor.newInstance();

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
      if (!listUserToDelete.isEmpty()) {
        // Suppression des users
        sReport.append(synchroUserWebService.deleteListUserWebService(theDomain.getId(),
            listUserToDelete));
      }

      // Insertion / Update des users
      if (!listUserToInsertUpdate.isEmpty()) {
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
      errorOccurred = null;
      synchroReport = "";
      synchroThread.startTheThread();
    }
  }

  public boolean isEnCours() {
    return synchroThread != null && synchroThread.isRunning();
  }

  public String getSynchroReport() {
    if (errorOccurred != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);

      errorOccurred.printStackTrace(pw);
      return errorOccurred.toString() + "\n" + sw.getBuffer();
    }
    return synchroReport;
  }

  public void threadFinished() {
    errorOccurred = synchroThread.getErrorOccurred();
    synchroReport = synchroThread.getSynchroReport();
    synchroThread = null;
  }

  public void getP12(String userId) throws JobDomainPeasException {
    UserDetail user = getUserDetail(userId);

    try {
      X509Factory.getFactory().buildP12(user);
    } catch (SilverpeasRuntimeException e) {
      throw new JobDomainPeasException(e);
    }
  }

  private void revokeCertificate(UserDetail user)
      throws JobDomainPeasException {
    try {
      X509Factory.getFactory().revokeUserCertificate(user.getId());
    } catch (SilverpeasRuntimeException e) {
      throw new JobDomainPeasException(e);
    }
  }

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
    Group[] aGroups = getOrganisationController().getGroups(groupIds.toArray(new String[0]));
    return Arrays.asList(aGroups);
  }

  public UserDetail checkUser(UserDetail userToCheck) {
    UserDetail[] existingUsers = targetDomain.getUserPage();
    for (UserDetail existingUser : existingUsers) {
      if (userToCheck.getLastName().equalsIgnoreCase(existingUser.getLastName())
          && userToCheck.getFirstName().equalsIgnoreCase(existingUser.getFirstName())
          && userToCheck.getEmailAddress().equalsIgnoreCase(existingUser.getEmailAddress())) {
        return existingUser;
      }
    }
    return null;
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

  private void processDataOfExtraTemplate(String userId, Map<String, String> properties) {
    PublicationTemplate template = getDirectoryExtraForm();
    if (template != null) {
      try {
        RecordSet recordSet = template.getRecordSet();
        DataRecord data = recordSet.getEmptyRecord();
        data.setId(userId);

        FieldTemplate[] fieldTemplates = template.getRecordTemplate().getFieldTemplates();
        for (FieldTemplate fieldTemplate : fieldTemplates) {
          String fieldName = fieldTemplate.getFieldName();
          data.getField(fieldName).setObjectValue(properties.get(fieldName));
        }

        recordSet.save(data);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
        MessageNotifier.addError("Les données du formulaire n'ont pas été enregistrées !");
      }
    }
  }

  public PublicationTemplate getDirectoryExtraForm() {
    return PublicationTemplateManager.getInstance().getDirectoryTemplate();
  }

  private PagesContext getTemplateContext(String userId) {
    return PagesContext.getDirectoryContext(userId, getUserId(), getLanguage());
  }

  public void deleteUserAvatar(String userId) {
    ImageProfile img = new ImageProfile(UserDetail.getById(userId).getAvatarFileName());
    img.removeImage();
  }

  public List<? extends Group> getCurrentUserGroups() {
    return adminCtrl.getDirectGroupsOfUser(targetUserId);
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
  public LocalizedComponentInstProfilesList getCurrentProfiles() {
    LocalizedComponentInstProfilesList allProfiles = new LocalizedComponentInstProfilesList();
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
        .map(adminCtrl::getProfileInst)
        .forEach(p -> {
          Objects.requireNonNull(p);
          LocalizedComponentInstProfiles componentProfiles =
              allProfiles.getByLocalComponentInstanceId(p.getComponentFatherId());
          if (componentProfiles == null) {
            ComponentInstLight currentComponent =
                adminCtrl.getComponentInstLight(String.valueOf(p.getComponentFatherId()));
            if (currentComponent.getStatus() == null && !currentComponent.isPersonal()) {
              LocalizedWAComponent localizedWAComponent =
                  getLocalizedComponent(currentComponent.getName());
              componentProfiles = new LocalizedComponentInstProfiles(currentComponent,
                  localizedWAComponent, getLanguage());
              SpaceInstLight space = adminCtrl.getSpaceInstLight(currentComponent.getSpaceId());
              componentProfiles.setSpace(space);
              allProfiles.add(componentProfiles);
            }
          }
          if (componentProfiles != null) {
            componentProfiles.addProfile(p);
          }
        });
    allProfiles.sort(new AbstractComplexComparator<>() {
      private static final long serialVersionUID = 6776408278128213038L;

      @Override
      protected ValueBuffer getValuesToCompare(final LocalizedComponentInstProfiles object) {
        return new ValueBuffer().append(object.getSpace().getName(getLanguage()))
            .append(object.getComponent().getName(getLanguage()));
      }
    });
    return allProfiles;
  }

  private LocalizedWAComponent getLocalizedComponent(String name) {
    LocalizedWAComponent localizedWAComponent = localizedComponents.get(name);
    if (localizedWAComponent == null) {
      try {
        Optional<WAComponent> optionalComponent = WAComponent.getByName(name);
        if (optionalComponent.isPresent()) {
          WAComponent component = optionalComponent.get();
          localizedWAComponent = new LocalizedWAComponent(component, getLanguage());
          localizedComponents.put(name, localizedWAComponent);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }
    return localizedWAComponent;
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
                .assignRightsFromUserToGroup(operationMode, sourceRightsId,
                    getTargetGroup().getId(), nodeAssignRights, getUserId());
          }
        } else if (Selection.TYPE_SELECTED_SET.equals(sourceRightsType)) {
          if (isDefined(targetUserId)) {
            adminCtrl
                .assignRightsFromGroupToUser(operationMode, sourceRightsId, targetUserId,
                    nodeAssignRights, getUserId());
          } else if (getTargetGroup() != null) {
            adminCtrl
                .assignRightsFromGroupToGroup(operationMode, sourceRightsId,
                    getTargetGroup().getId(), nodeAssignRights, getUserId());
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

  public List<UserDetail> getUsersWithSensitiveData() {
    try {
      return adminCtrl.getUsersWithSensitiveData(this.targetDomainId);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
      return List.of();
    }
  }

  public void blankDeletedUsers(final List<String> userIds) throws AdminException {
    adminCtrl.blankDeletedUsers(targetDomainId, userIds);
  }

  public void disableUsersSensitivityData(final List<String> userIds) throws AdminException {
    adminCtrl.disableDataSensitivity(targetDomainId, userIds);
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
        !isOnlyGroupManager() && isNotManagerOfCurrentDomain() &&
        ArrayUtil.isNotEmpty(getUserManageableSpaceIds());
  }

  /**
   * Adds the specified group into the clipboard for further copy as a root group or as a subgroup
   * of another group in the same domain.
   *
   * @param groupId the unique identifier of an existing group to copy.
   * @throws AdminException if an error occurs while copying the given group of users.
   */
  public void copyGroup(String groupId) throws AdminException {
    copyOrCutGroup(groupId, false);
  }

  /**
   * Adds the specified group into the clipboard for further move as a root group or as subgroup of
   * another group in the same domain.
   *
   * @param groupId the unique identifier of an existing group to cut.
   * @throws AdminException if an error occurs while copying the given group of users.
   */
  public void cutGroup(String groupId) throws AdminException {
    copyOrCutGroup(groupId, true);
  }

  private void copyOrCutGroup(String groupId, boolean cut) throws AdminException {
    try {
      Group group = adminCtrl.getGroupById(groupId);
      if (group.isCommunityGroup()) {
        throw new AdminException(groupId + " is an application managed group");
      }
      GroupSelection groupSelection = new GroupSelection(group);
      groupSelection.setCut(cut);
      addClipboardSelection(groupSelection);
    } catch (ClipboardException e) {
      String op = cut ? " cut " : " copy ";
      throw new AdminException("Fail to" + op + "group " + groupId, e);
    }
  }

  /**
   * Pastes the selected groups of users in the clipboard into the current group of as a root group
   * of the current domain if there is no current group. The group to paste has to belong to the
   * same current domain and there shouldn't be any group having the same name in the current group
   * or among the root groups of the domain (in the case there is no current group). Any selected
   * group which don't belong to the same domain will be ignored whereas an exception will be thrown
   * if there is already a group with the same name.
   *
   * @throws AdminException if an error occurs while pasting the given group of users.
   */
  public void pasteGroup() throws AdminException {
    String parentGroupId = getTargetGroup() == null ? null : getTargetGroup().getId();
    String domainId = getTargetDomain() == null || "-1".equals(getTargetDomain().getId()) ?
        null : getTargetDomain().getId();
    if (parentGroupId != null) {
      checkGroupAccessGranted(parentGroupId, false);
    } else {
      checkCurrentDomainAccessGranted(false);
    }
    try {
      Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
      for (ClipboardSelection clipObject : clipObjects) {
        if (clipObject != null && clipObject.isDataFlavorSupported(GroupSelection.GROUP_FLAVOR)) {
          GroupDetail group =
              (GroupDetail) clipObject.getTransferData(GroupSelection.GROUP_FLAVOR);
          if (Objects.equals(group.getDomainId(), domainId) && !group.isCommunityGroup()) {
            if (clipObject.isCut()) {
              adminCtrl.moveGroup(group, parentGroupId);
              refresh();
            } else {
              adminCtrl.copyGroup(group, parentGroupId);
              refresh();
            }
          } else {
            SilverLogger.getLogger(this).warn("The group in clipboard " + group.getId() +
                " doesn't belong to the domain " + domainId + ": it isn't then pasted");
          }
        }
      }

      clipboardPasteDone();
    } catch (Exception e) {
      throw new AdminException(e);
    }
  }

  /**
   * Is there at least one selected group in the clipboard belonging to the same domain as the
   * current one and for which there is no actual groups with the same name in the domain (for root
   * groups) or in the current group of the domain.
   *
   * @return true if there is at least one selected group matching the expected constrains of group
   * pasting.
   * @throws ClipboardException if an error occurs while accessing the clipboard.
   * @throws JobDomainPeasException if an error occurs while accessing the root groups of the domain
   * or the subgroups of the current group.
   */
  public boolean isThereAMatchingGroupInClipboard() throws ClipboardException,
      JobDomainPeasException {
    final Function<ClipboardSelection, Group> toGroup = s -> {
      try {
        return (Group) s.getTransferData(GroupSelection.GROUP_FLAVOR);
      } catch (UnsupportedFlavorException e) {
        throw new SilverpeasRuntimeException(e);
      }
    };

    if (getTargetDomain() == null) {
      return false;
    }
    String domainId = "-1".equals(getTargetDomain().getId()) ? null : getTargetDomain().getId();
    Set<String> subgroups = Stream.of(getSubGroups(getTargetGroup() != null))
        .map(Group::getName)
        .collect(Collectors.toSet());
    Set<String> selectedGroups = getClipboardSelectedObjects().stream()
        .filter(Objects::nonNull)
        .filter(o -> o.isDataFlavorSupported(GroupSelection.GROUP_FLAVOR))
        .map(toGroup)
        .filter(g -> Objects.equals(g.getDomainId(), domainId))
        .map(Group::getName)
        .collect(Collectors.toSet());
    return !selectedGroups.isEmpty() && selectedGroups.stream().noneMatch(subgroups::contains);
  }

  /**
   * Generate a new synchronization token and register it in this controller. This token is to be
   * used to enforce the security between the client and this controller. For doing, use the method
   * {@link JobDomainPeasSessionController#securelyApply(String, AdminTask)} to execute securely
   * some administrative tasks with side effects. Once the token generated, any invocation of the
   * above method is valid only if the token passed by the client matches current one.
   *
   * @return the new current token value.
   */
  public String generateToken() {
    var generator = TokenGeneratorProvider.getTokenGenerator(SynchronizerToken.class);
    this.token = generator.generate();
    return token.getValue();
  }

  @FunctionalInterface
  public interface AdminTask {
    void run() throws Exception;
  }

  /**
   * In order to check the user granted access to domain services, the context of use MUST be set.
   * <p>
   * This context is an implementation of this abstraction.
   * </p>
   *
   * @param <T>
   */
  private static abstract class AccessContext<T> {
    private final AccessContextType type;
    private final T resource;

    private AccessContext(final AccessContextType accessContext, final T resource) {
      this.type = accessContext;
      this.resource = resource;
    }

    public AccessContextType getType() {
      return type;
    }

    public T getResource() {
      return resource;
    }

    enum AccessContextType {
      DOMAIN, GROUP, USER;

      boolean isGroup() {
        return this == GROUP;
      }
    }
  }

  /**
   * Context of a domain access.
   */
  private static class DomainAccessContext extends AccessContext<Void> {
    private DomainAccessContext() {
      super(AccessContextType.DOMAIN, null);
    }
  }

  /**
   * Context of user services access.
   */
  private static class UserAccessContext extends AccessContext<User> {
    private UserAccessContext(final User user) {
      super(AccessContextType.USER, user);
    }
  }

  /**
   * Context of group services access.
   */
  private static class GroupAccessContext extends AccessContext<Group> {
    private GroupAccessContext(final Group group) {
      super(AccessContextType.GROUP, group);
    }
  }
}