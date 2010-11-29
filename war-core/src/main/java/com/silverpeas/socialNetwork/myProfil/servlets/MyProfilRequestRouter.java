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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.socialNetwork.myProfil.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.directory.servlets.ImageProfil;
import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.silverpeas.socialNetwork.myProfil.control.MyProfilSessionController;
import com.silverpeas.socialNetwork.user.model.SNContactUser;
import com.silverpeas.socialNetwork.user.model.SNFullUser;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.authentication.AuthenticationBadCredentialException;
import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.PeasCoreException;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 *
 * @author azzedine
 */
public class MyProfilRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = 1L;
  private static final String AVATAR_FOLDER = "avatar";
  private final int NUMBER_CONTACTS_TO_DISPLAY = 3;
  
  @Override
  public String getSessionControlBeanName() {
    return "myProfil";
  }

  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new MyProfilSessionController(mainSessionCtrl, componentContext);
  }
/**
 *
 * @param function
 * @param componentSC
 * @param request
 * @return String
 */
  @Override
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "#";

    MyProfilSessionController myProfilSC = (MyProfilSessionController) componentSC;
    SNFullUser snUserFull = new SNFullUser(myProfilSC.getUserId());
    
    try {
      if (function.equalsIgnoreCase("MyEvents")) {
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
      } else if (function.equalsIgnoreCase("validateChangePhoto")) {
        try {
          saveAvatar(request, snUserFull.getUserFull().getLogin());

          return getDestination("MyInfos", componentSC, request);
        } catch (Exception ex) {
          Logger.getLogger(MyProfilRequestRouter.class.getName()).log(Level.SEVERE, null, ex);
        }

      } else if (function.equalsIgnoreCase("Main") || function.equalsIgnoreCase("MyInfos")) {
       
        // Détermination du domaine du user
        boolean domainRW = myProfilSC.isUserDomainRW();
        if (domainRW) {
          request.setAttribute("Action", "userModify");
        } else {
          request.setAttribute("Action", "userMS");
        }

        boolean updateIsAllowed = domainRW
            || myProfilSC.isPasswordChangeAllowed()
            || (snUserFull.getUserFull().isPasswordValid() && snUserFull.getUserFull().isPasswordAvailable());

        request.setAttribute("userObject", snUserFull.getUserFull());
        request.setAttribute("UpdateIsAllowed", updateIsAllowed);
        request.setAttribute("minLengthPwd", myProfilSC.getMinLengthPwd());
        request.setAttribute("blanksAllowedInPwd", myProfilSC.isBlanksAllowedInPwd());
        request.setAttribute("View", "MyInfos");
        
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (function.equalsIgnoreCase("UpdateMyInfos")) {
        
        updateUserFull(request, myProfilSC);

        return getDestination("MyInfos", componentSC, request);
      } else if (function.equals("MySettings")) {
        
        request.setAttribute("View", function);
        setUserSettingsIntoRequest(request, myProfilSC);
        
        destination = "/socialNetwork/jsp/myProfil/myProfile.jsp";
      } else if (function.equals("UpdateMySettings")) {
        updateUserSettings(request, myProfilSC);
        
        destination = getDestination("MySettings", componentSC, request);
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }
    request.setAttribute("snUserFull", snUserFull);
    request.setAttribute("UserFull", snUserFull.getUserFull());
    List<String> contactIds = myProfilSC.getContactsIdsForUser(myProfilSC.getUserId());
    request.setAttribute("contacts", chooseContactsToDisplay(contactIds, myProfilSC));
    request.setAttribute("contactsNumber",contactIds.size());
    return destination;
  }
/**
 * method to change profile Photo
 * @param request
 * @param nameAvatar
 * @return String
 * @throws IOException
 * @throws UtilException
 */
  protected String saveAvatar(HttpServletRequest request, String nameAvatar)
      throws IOException, UtilException {
    List<FileItem> parameters = FileUploadUtil.parseRequest(request);
    FileItem file = FileUploadUtil.getFile(parameters, "WAIMGVAR0");
    String avatar = nameAvatar + ".jpg";
    ImageProfil img = new ImageProfil(avatar, AVATAR_FOLDER);
    img.saveImage(file.getInputStream());
    return avatar;
  }
  /**
   * methode to choose (x) contacts for display it in the page profil
   * x is the number of contacts 
   * the methode use Random rule
   * @param contactIds
   * @return List<SNContactUser>
   */
  private List<SNContactUser> chooseContactsToDisplay(List<String> contactIds, MyProfilSessionController sc) {
    int numberOfContactsTodisplay;
    List<SNContactUser> contacts = new ArrayList<SNContactUser>();
    try {
      numberOfContactsTodisplay = Integer.parseInt(sc.getSettings().getString(
          "numberOfContactsTodisplay"));
    } catch (NumberFormatException ex) {
      numberOfContactsTodisplay = NUMBER_CONTACTS_TO_DISPLAY;
    }
    if ( contactIds.size()<= numberOfContactsTodisplay) {
      for (int i = 0; i < contactIds.size(); i++) {
        contacts.add(new SNContactUser(contactIds.get(i)));
      }
    } else {
      Random random = new Random();
       int indexContactsChoosed = (random.nextInt(contactIds.size()) );
      for (int i = 0; i < numberOfContactsTodisplay; i++) {
        contacts.add(new SNContactUser(contactIds.get((indexContactsChoosed+i)%numberOfContactsTodisplay)));
      }
    }

    return contacts;
  }
  
  private void updateUserFull(HttpServletRequest request, MyProfilSessionController sc) {
    ResourceLocator rl = new ResourceLocator("com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings", "");
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
      String userEmail = updateEmailIsAllowed ? request
          .getParameter("userEMail") : currentUser.geteMail();
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
      userLoginAnswer = (userLoginAnswer != null
          ? EncodeHelper.htmlStringToJavaString(userLoginAnswer)
          : currentUser.getLoginAnswer());

      // process extra properties
      HashMap<String, String> properties = new HashMap<String, String>();
      Enumeration<String> parameters = request.getParameterNames();
      String parameterName = null;
      String property = null;
      while (parameters.hasMoreElements()) {
        parameterName = parameters.nextElement();
        if (parameterName.startsWith("prop_")) {
          property = parameterName.substring(5, parameterName.length()); // remove
          // "prop_"
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
      request.setAttribute("Message", sc.getString("myProfile.MessageOK"));
    } catch (AuthenticationBadCredentialException e) {
      request.setAttribute("Message", sc.getString("myProfile.Error_bad_credential"));
    } catch (AuthenticationException e) {
      request.setAttribute("Message", sc.getString("myProfile.Error_unknown"));
    }
  }
  
  private void setUserSettingsIntoRequest(HttpServletRequest request, MyProfilSessionController sc)
      throws PeasCoreException {
    request.setAttribute("selectedLanguage", sc.getFavoriteLanguage());
    request.setAttribute("thesaurusStatus", sc.getThesaurusStatus());
    request.setAttribute("dragDropStatus", sc.getDragAndDropStatus());
    request.setAttribute("webdavEditingStatus", sc.getWebdavEditingStatus());
    request.setAttribute("FavoriteSpace", sc.getFavoriteSpace());
    request.setAttribute("selectedLook", sc.getFavoriteLook());
    request.setAttribute("SpaceTreeview", sc.getSpaceTreeview());
    request.setAttribute("AllLanguages", sc.getAllLanguages());
  }
  
  private void updateUserSettings(HttpServletRequest request, MyProfilSessionController sc)
      throws PeasCoreException {

    List<String> languages = new ArrayList<String>();
    languages.add(request.getParameter("SelectedLanguage"));
    sc.setLanguages(languages);

    sc.setFavoriteLook(request.getParameter("SelectedLook"));
    sc.setThesaurusStatus(Boolean.valueOf(request.getParameter("opt_thesaurusStatus")));
    sc.setDragAndDropStatus(Boolean.valueOf(request.getParameter("opt_dragDropStatus")));
    sc.setWebdavEditingStatus(Boolean.valueOf(request.getParameter("opt_webdavEditingStatus")));

    String selectedWorkSpace = request.getParameter("SelectedWorkSpace");
    if (!StringUtil.isDefined(selectedWorkSpace)) {
      sc.setPersonalWorkSpace(null);
    } else {
      sc.setPersonalWorkSpace(selectedWorkSpace);
    }
  }
}
