/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.socialnetwork.myProfil.servlets;

import com.silverpeas.directory.servlets.ImageProfil;
import com.silverpeas.look.LookHelper;
import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.socialnetwork.model.SocialInformationType;
import com.silverpeas.socialnetwork.myProfil.control.MyProfilSessionController;
import com.silverpeas.socialnetwork.user.model.SNFullUser;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.util.crypto.CryptMD5;

import static com.silverpeas.socialnetwork.myProfil.servlets.MyProfileRoutes.*;

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
      HttpServletRequest request) {
    String destination = "#";
    SNFullUser snUserFull = new SNFullUser(myProfilSC.getUserId());
    MyProfileRoutes route = valueOf(function);
    SocialNetworkHelper socialNetworkHelper = new SocialNetworkHelper();

    try {
      if (route == MyInfos) {
        // Détermination du domaine du user
        boolean domainRW = myProfilSC.isUserDomainRW();

        boolean updateIsAllowed = domainRW && (myProfilSC.isPasswordChangeAllowed() || (snUserFull.
            getUserFull().isPasswordValid() && snUserFull.getUserFull().isPasswordAvailable())
            || myProfilSC.updatablePropertyExists());

        if (updateIsAllowed) {
          request.setAttribute("Action", "userModify");
        } else {
          request.setAttribute("Action", "userMS");
        }
        request.setAttribute("userObject", snUserFull.getUserFull());
        request.setAttribute("UpdateIsAllowed", updateIsAllowed);
        request.setAttribute("isAdmin", myProfilSC.isAdmin());
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
      } else if (route == CreateLinkToSVP) {
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
      } else if (function.equalsIgnoreCase("MyEvents")) {
        try {
          request.setAttribute("type", SocialInformationType.EVENT);
        } catch (Exception ex) {
          Logger.getLogger(MyProfilRequestRouter.class.getName()).log(Level.SEVERE, null, ex);
        }
        destination = "/socialNetwork/jsp/myProfil/myProfilTemplate.jsp";
      } else if (function.equalsIgnoreCase("ALL")) {
        request.setAttribute("type", SocialInformationType.ALL);
        destination = "/socialNetwork/jsp/myProfil/myProfilTemplate.jsp";
      } else if (function.equalsIgnoreCase("MyPhotos")) {
        request.setAttribute("type", SocialInformationType.PHOTO);
        destination = "/socialNetwork/jsp/myProfil/myProfilTemplate.jsp";
      } else if (function.equalsIgnoreCase("MyPubs")) {
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
   *
   * @param request
   * @param nameAvatar
   * @return String
   * @throws UtilException
   */
  protected String saveAvatar(HttpServletRequest request, String nameAvatar)
      throws UtilException {
    List<FileItem> parameters = FileUploadUtil.parseRequest(request);
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
            SilverpeasRuntimeException.ERROR,
            "", "Bad extension, .gif or .jpg or .png expected.");
      }
      try {
        img.saveImage(file.getInputStream());
      } catch (IOException e) {
        throw new UtilException("MyProfilRequestRouter.saveAvatar()",
            SilverpeasRuntimeException.ERROR,
            "", "Problem while saving image.");
      }
    } else if ("yes".equals(removeImageFile)) {// Remove
      img.removeImage();
    }
    return nameAvatar;
  }

  /**
   * method to choose (x) contacts for display it in the page profil x is the number of contacts the
   * methode use Random rule
   *
   * @param contactIds
   * @return List<SNContactUser>
   */
  private List<UserDetail> getContactsToDisplay(List<String> contactIds,
      MyProfilSessionController sc) {
    int numberOfContactsTodisplay = sc.getSettings().getInteger("numberOfContactsTodisplay",
        NUMBER_CONTACTS_TO_DISPLAY);
    List<UserDetail> contacts = new ArrayList<UserDetail>();
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
    ResourceLocator rl = new ResourceLocator(
        "org.silverpeas.personalizationPeas.settings.personalizationPeasSettings", "");
    ResourceLocator authenticationSettings = new ResourceLocator(
        "org.silverpeas.authentication.settings.authenticationSettings", "");
    UserDetail currentUser = sc.getUserDetail();
    // Update informations only if updateMode is allowed for each field
    try {
      boolean updateFirstNameIsAllowed = rl.getBoolean("updateFirstName", false);
      boolean updateLastNameIsAllowed = rl.getBoolean("updateLastName", false);
      boolean updateEmailIsAllowed = rl.getBoolean("updateEmail", false);
      String userFirstName = updateFirstNameIsAllowed ? request
          .getParameter("userFirstName") : currentUser.getFirstName();
      String userLastName = updateLastNameIsAllowed ? request
          .getParameter("userLastName") : currentUser.getLastName();
      String userEmail = updateEmailIsAllowed ? request.getParameter(
          "userEMail") : currentUser.geteMail();
      SilverTrace.info(getSessionControlBeanName(),
          "PersoPeasRequestRouter.getDestination()",
          "root.MSG_GEN_PARAM_VALUE", "userFirstName=" + userFirstName
          + " - userLastName=" + userLastName + " userEmail="
          + userEmail);

      String userLoginQuestion = request.getParameter("userLoginQuestion");
      userLoginQuestion = (userLoginQuestion != null
          ? EncodeHelper.htmlStringToJavaString(userLoginQuestion)
          : currentUser.getLoginQuestion());
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
      Map<String, String> properties = new HashMap<String, String>();
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

      sc.modifyUser(
          currentUser.getId(),
          EncodeHelper.htmlStringToJavaString(userLastName),
          EncodeHelper.htmlStringToJavaString(userFirstName),
          EncodeHelper.htmlStringToJavaString(userEmail),
          EncodeHelper.htmlStringToJavaString(request.getParameter("userAccessLevel")),
          EncodeHelper.htmlStringToJavaString(request.getParameter("OldPassword")),
          EncodeHelper.htmlStringToJavaString(request.getParameter("NewPassword")),
          userLoginQuestion,
          userLoginAnswer,
          properties);
      request.setAttribute("MessageOK", sc.getString("myProfile.MessageOK"));
    } catch (AuthenticationBadCredentialException e) {
      request.setAttribute("MessageNOK", sc.getString("myProfile.Error_bad_credential"));
    } catch (Exception e) {
      request.setAttribute("MessageNOK", sc.getString("myProfile.Error_unknown"));
    }
  }

  private void setUserSettingsIntoRequest(HttpServletRequest request, MyProfilSessionController sc) {
    request.setAttribute("preferences", sc.getPreferences());
    request.setAttribute("SpaceTreeview", sc.getSpaceTreeview());
    request.setAttribute("AllLanguages", DisplayI18NHelper.getLanguages());
    LookHelper lookHelper = getLookHelper(request);
    if (lookHelper != null) {
      request.setAttribute("MenuDisplay", lookHelper.isMenuPersonalisationEnabled());
      List<String> userMenuDisplayOptions = new ArrayList<String>();
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
    preferences.setLanguage(request.getParameter("SelectedLanguage"));
    preferences.setLook(request.getParameter("SelectedLook"));
    preferences.enableThesaurus(Boolean.valueOf(request.getParameter("opt_thesaurusStatus")));
    preferences.enableDragAndDrop(Boolean.valueOf(request.getParameter("opt_dragDropStatus")));
    preferences.enableWebdavEdition(
        Boolean.valueOf(request.getParameter("opt_webdavEditingStatus")));
    LookHelper lookHelper = getLookHelper(request);
    if (lookHelper != null && lookHelper.isMenuPersonalisationEnabled() && StringUtil.isDefined(
        request.getParameter("MenuDisplay"))) {
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
    return (LookHelper) request.getSession().getAttribute(LookHelper.SESSION_ATT);
  }
}
