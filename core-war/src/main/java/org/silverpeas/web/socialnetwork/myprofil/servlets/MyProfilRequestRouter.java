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
package org.silverpeas.web.socialnetwork.myprofil.servlets;

import org.silverpeas.web.directory.servlets.ImageProfil;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.web.socialnetwork.myprofil.control.MyProfilSessionController;
import org.silverpeas.core.web.socialnetwork.user.model.SNFullUser;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.security.encryption.cipher.CryptMD5;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.silverpeas.web.socialnetwork.myprofil.servlets.MyProfileRoutes.*;

/**
 * @author azzedine
 */
public class MyProfilRequestRouter extends ComponentRequestRouter<MyProfilSessionController> {

  private static final long serialVersionUID = -9194682447286602180L;
  private final int NUMBER_CONTACTS_TO_DISPLAY = 3;

  @Override
  public String getSessionControlBeanName() {
    return "myProfile";
  }

  @Override
  public MyProfilSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new MyProfilSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * @param function
   * @param myProfilSC
   * @param request
   * @return String
   */
  @Override
  public String getDestination(String function, MyProfilSessionController myProfilSC,
      HttpRequest request) {
    String destination = "#";
    SNFullUser snUserFull = new SNFullUser(myProfilSC.getUserId());
    MyProfileRoutes route = valueOf(function);
    SocialNetworkHelper socialNetworkHelper = ServiceProvider.getService(SocialNetworkHelper.class);

    try {
      if (route == MyInfos) {
        // Détermination du domaine du user
        boolean domainRW = myProfilSC.isUserDomainRW();

        boolean updateIsAllowed = domainRW && ((myProfilSC.isPasswordChangeAllowed() || (snUserFull.
            getUserFull().isPasswordValid() && snUserFull.getUserFull().isPasswordAvailable())
            || myProfilSC.updatablePropertyExists()));

        request.setAttribute("userObject", snUserFull.getUserFull());
        request.setAttribute("UpdateIsAllowed", updateIsAllowed);
        request.setAttribute("isPasswordChangeAllowed", myProfilSC.isPasswordChangeAllowed());
        request.setAttribute("View", "MyInfos");
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (route == MyProfileRoutes.UpdatePhoto) {
        saveAvatar(request, snUserFull.getUserFull().getAvatarFileName());

        return getDestination(MyInfos.toString(), myProfilSC, request);
      } else if (route == MyProfileRoutes.UpdateMyInfos) {
        updateUserFull(request, myProfilSC);
        return getDestination(MyInfos.toString(), myProfilSC, request);
      } else if (route == MySettings) {
        request.setAttribute("View", function);
        setUserSettingsIntoRequest(request, myProfilSC);

        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (route == MyProfileRoutes.UpdateMySettings) {
        updateUserSettings(request, myProfilSC);

        return getDestination(MySettings.toString(), myProfilSC, request);
      } else if (route == MyNetworks) {
        request.setAttribute("View", function);
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (route == UnlinkFromSVP) {
        socialNetworkHelper.unlinkFromSilverpeas(myProfilSC, request);
        request.setAttribute("View", MyNetworks.name());
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (route == LinkToSVP) {
        return socialNetworkHelper.buildAuthenticationURL(request, route);
      } else if (route == AddLinkToSVP) {
        socialNetworkHelper.linkToSilverpeas(myProfilSC, request);
        request.setAttribute("View", MyNetworks.name());
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (route == PublishStatus) {
        return socialNetworkHelper.buildAuthenticationURL(request, route);
      } else if (route == DoPublishStatus) {
        socialNetworkHelper.publishStatus(myProfilSC, request);
        request.setAttribute("View", MyNetworks.name());
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (route == MyInvitations) {
        MyInvitationsHelper helper = new MyInvitationsHelper();
        helper.getAllInvitationsReceived(myProfilSC, request);
        request.setAttribute("View", function);
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (route == MySentInvitations) {
        MyInvitationsHelper helper = new MyInvitationsHelper();
        helper.getAllInvitationsSent(myProfilSC, request);
        request.setAttribute("View", function);
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (route == MyProfileRoutes.MyWall) {
        request.setAttribute("View", function);
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (route == Main || route == MyProfileRoutes.MyFeed) {
        request.setAttribute("View", MyProfileRoutes.MyFeed.toString());
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if ("MyEvents".equalsIgnoreCase(function)) {
        try {
          request.setAttribute("type", SocialInformationType.EVENT);
        } catch (Exception ex) {
          SilverLogger.getLogger(this).error(ex.getMessage(), ex);
        }
        destination = "/socialNetwork/jsp/myProfil/myProfilTemplate.jsp";
      } else if ("ALL".equalsIgnoreCase(function)) {
        request.setAttribute("type", SocialInformationType.ALL);
        destination = "/socialNetwork/jsp/myProfil/myProfilTemplate.jsp";
      } else if ("MyPhotos".equalsIgnoreCase(function)) {
        request.setAttribute("type", SocialInformationType.MEDIA);
        destination = "/socialNetwork/jsp/myProfil/myProfilTemplate.jsp";
      } else if ("MyPubs".equalsIgnoreCase(function)) {
        request.setAttribute("type", SocialInformationType.PUBLICATION);
        destination = "/socialNetwork/jsp/myProfil/myProfilTemplate.jsp";
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    socialNetworkHelper.getAllMyNetworks(myProfilSC, request);
    socialNetworkHelper.setupJSAttributes(myProfilSC, request);

    request.setAttribute("UserFull", snUserFull.getUserFull());
    List<String> contactIds = myProfilSC.getContactsIdsForUser(myProfilSC.getUserId());
    request.setAttribute("Contacts", getContactsToDisplay(contactIds, myProfilSC));
    request.setAttribute("ContactsNumber", contactIds.size());
    return destination;
  }

  /**
   * method to change profile Photo
   * @param request
   * @param nameAvatar
   * @return String
   * @throws UtilException
   */
  protected String saveAvatar(HttpRequest request, String nameAvatar) throws UtilException {
    List<FileItem> parameters = request.getFileItems();
    String removeImageFile = FileUploadUtil.getParameter(parameters, "removeImageFile");
    FileItem file = FileUploadUtil.getFile(parameters, "WAIMGVAR0");
    ImageProfil img = new ImageProfil(nameAvatar);
    if (file != null && StringUtil.isDefined(file.getName())) {// Create or Update
      // extension
      String extension = FileRepositoryManager.getFileExtension(file.getName());
      if (extension != null && extension.equalsIgnoreCase("jpeg")) {
        extension = "jpg";
      }

      if (!"gif".equalsIgnoreCase(extension) && !"jpg".equalsIgnoreCase(extension) && !"png".
          equalsIgnoreCase(extension)) {
        throw new UtilException("MyProfilRequestRouter.saveAvatar()",
            SilverpeasRuntimeException.ERROR, "", "Bad extension, .gif or .jpg or .png expected.");
      }
      try (InputStream fis = file.getInputStream()) {
        img.saveImage(fis);
      } catch (IOException e) {
        throw new UtilException("MyProfilRequestRouter.saveAvatar()",
            SilverpeasRuntimeException.ERROR, "", "Problem while saving image.");
      }
    } else if ("yes".equals(removeImageFile)) {// Remove
      img.removeImage();
    }
    return nameAvatar;
  }

  /**
   * method to choose (x) contacts for display it in the page profil x is the number of contacts
   * the
   * methode use Random rule
   * @param contactIds
   * @return List<SNContactUser>
   */
  private List<UserDetail> getContactsToDisplay(List<String> contactIds,
      MyProfilSessionController sc) {
    int numberOfContactsTodisplay =
        sc.getSettings().getInteger("numberOfContactsTodisplay", NUMBER_CONTACTS_TO_DISPLAY);
    List<UserDetail> contacts = new ArrayList<>();
    if (contactIds.size() <= numberOfContactsTodisplay) {
      for (String userId : contactIds) {
        contacts.add(sc.getUserDetail(userId));
      }
    } else {
      Random random = new Random();
      int indexContactsChoosed = random.nextInt(contactIds.size());
      for (int i = 0; i < numberOfContactsTodisplay; i++) {
        contacts.add(sc.getUserDetail(
            contactIds.get((indexContactsChoosed + i) % numberOfContactsTodisplay)));
      }
    }

    return contacts;
  }

  private void updateUserFull(HttpServletRequest request, MyProfilSessionController sc) {
    SettingBundle rl = ResourceLocator.getSettingBundle(
        "org.silverpeas.personalization.settings.personalizationPeasSettings");
    SettingBundle authenticationSettings = ResourceLocator.getSettingBundle(
        "org.silverpeas.authentication.settings.authenticationSettings");
    UserDetail currentUser = sc.getUserDetail();
    // Update informations only if updateMode is allowed for each field
    try {
      boolean updateFirstNameIsAllowed = rl.getBoolean("updateFirstName", false);
      boolean updateLastNameIsAllowed = rl.getBoolean("updateLastName", false);
      boolean updateEmailIsAllowed = rl.getBoolean("updateEmail", false);
      String userFirstName = updateFirstNameIsAllowed ? request.getParameter("userFirstName") :
          currentUser.getFirstName();
      String userLastName = updateLastNameIsAllowed ? request.getParameter("userLastName") :
          currentUser.getLastName();
      String userEmail =
          updateEmailIsAllowed ? request.getParameter("userEMail") : currentUser.geteMail();
      String userLoginQuestion = request.getParameter("userLoginQuestion");
      userLoginQuestion =
          (userLoginQuestion != null ? EncodeHelper.htmlStringToJavaString(userLoginQuestion) :
              currentUser.getLoginQuestion());
      String userLoginAnswer = request.getParameter("userLoginAnswer");

      // user has filled a new login answer
      if (StringUtil.isDefined(userLoginAnswer)) {
        userLoginAnswer = EncodeHelper.htmlStringToJavaString(userLoginAnswer);
        // encrypt the answser if needed
        boolean answerCrypted = authenticationSettings.getBoolean("loginAnswerCrypted", false);
        if (answerCrypted) {
          userLoginAnswer = CryptMD5.encrypt(userLoginAnswer);
        }
      } else {
        userLoginAnswer = currentUser.getLoginAnswer();
      }

      // process extra properties
      Map<String, String> properties = new HashMap<>();
      Enumeration<String> parameters = request.getParameterNames();
      String parameterName;
      String property;
      while (parameters.hasMoreElements()) {
        parameterName = parameters.nextElement();
        if (parameterName.startsWith("prop_")) {
          property = parameterName.substring(5, parameterName.length()); // remove s"prop_"
          properties.put(property, request.getParameter(parameterName));
        }
      }

      sc.modifyUser(currentUser.getId(), EncodeHelper.htmlStringToJavaString(userLastName),
          EncodeHelper.htmlStringToJavaString(userFirstName),
          EncodeHelper.htmlStringToJavaString(userEmail),
          EncodeHelper.htmlStringToJavaString(request.getParameter("userAccessLevel")),
          EncodeHelper.htmlStringToJavaString(request.getParameter("OldPassword")),
          EncodeHelper.htmlStringToJavaString(request.getParameter("NewPassword")),
          userLoginQuestion, userLoginAnswer, properties);
      request.setAttribute("MessageOK", sc.getString("myProfile.MessageOK"));
    } catch (AuthenticationBadCredentialException e) {
      request.setAttribute("MessageNOK", sc.getString("myProfile.Error_bad_credential"));
    } catch (Exception e) {
      request.setAttribute("MessageNOK", sc.getString("myProfile.Error_unknown"));
    }
  }

  private void setUserSettingsIntoRequest(HttpServletRequest request,
      MyProfilSessionController sc) {
    request.setAttribute("preferences", sc.getPreferences());
    request.setAttribute("SpaceTreeview", sc.getSpaceTreeview());
    request.setAttribute("AllLanguages", DisplayI18NHelper.getLanguages());
    LookHelper lookHelper = getLookHelper(request);
    if (lookHelper != null) {
      request.setAttribute("MenuDisplay", lookHelper.isMenuPersonalisationEnabled());
      List<String> userMenuDisplayOptions = new ArrayList<>();
      for (UserMenuDisplay display : UserMenuDisplay.values()) {
        userMenuDisplayOptions.add(display.name());
      }
      request.setAttribute("MenuDisplayOptions", userMenuDisplayOptions);
    } else {
      request.setAttribute("MenuDisplay", false);
    }
  }

  private void updateUserSettings(HttpServletRequest request, MyProfilSessionController sc) {
    UserPreferences preferences = sc.getPreferences();
    preferences.setLanguage(request.getParameter("SelectedUserLanguage"));
    preferences.setLook(request.getParameter("SelectedLook"));
    preferences.enableThesaurus(Boolean.valueOf(request.getParameter("opt_thesaurusStatus")));
    preferences.enableDragAndDrop(Boolean.valueOf(request.getParameter("opt_dragDropStatus")));
    preferences
        .enableWebdavEdition(Boolean.valueOf(request.getParameter("opt_webdavEditingStatus")));
    LookHelper lookHelper = getLookHelper(request);
    if (lookHelper != null && lookHelper.isMenuPersonalisationEnabled() &&
        StringUtil.isDefined(request.getParameter("MenuDisplay"))) {
      preferences.setDisplay(UserMenuDisplay.valueOf(request.getParameter("MenuDisplay")));
      lookHelper.setDisplayUserMenu(preferences.getDisplay());
    }
    String selectedWorkSpace = request.getParameter("SelectedWorkSpace");
    if (!StringUtil.isDefined(selectedWorkSpace)) {
      preferences.setPersonalWorkSpaceId("");
    } else {
      preferences.setPersonalWorkSpaceId(selectedWorkSpace);
    }
    sc.savePreferences(preferences);
  }

  private LookHelper getLookHelper(HttpServletRequest request) {
    return LookHelper.getLookHelper(request.getSession());
  }
}
