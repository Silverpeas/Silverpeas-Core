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

package com.stratelia.silverpeas.personalizationPeas.servlets;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.authentication.AuthenticationBadCredentialException;
import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.PeasCoreException;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.personalizationPeas.control.PersonalizationSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 * @author
 */
public class PersoPeasRequestRouter extends ComponentRequestRouter {

  /**
	 *
	 */
  private static final long serialVersionUID = 1L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new PersonalizationSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for notificationUser, returns
   * "notificationUser"
   */
  public String getSessionControlBeanName() {
    return "personalizationPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    SilverTrace.info(getSessionControlBeanName(),
        "PersoPeasRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "function = " + function);
    PersonalizationSessionController personalizationScc =
        (PersonalizationSessionController) componentSC;

    ResourceLocator rl = new ResourceLocator(
        "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings",
        "");

    String destination = "";
    String selectedLanguage = null;
    String thesaurusStatus = null;
    String dragDropStatus = null;
    String webdavEditingStatus = null;
    String selectedWorkSpace = null;
    String selectedLook = null;

    UserDetail currentUser = personalizationScc.getUserDetail();
    try {
      if (function.startsWith("Main")) {
        // language parameter
        selectedLanguage = personalizationScc.getFavoriteLanguage();

        // thesaurus parameter
        thesaurusStatus = new Boolean(personalizationScc.getThesaurusStatus())
            .toString();

        // drag and drop parameter
        dragDropStatus = new Boolean(personalizationScc.getDragAndDropStatus())
            .toString();

        // online editing parameter
        webdavEditingStatus = new Boolean(personalizationScc
            .getWebdavEditingStatus()).toString();

        // favorite look
        selectedLook = personalizationScc.getFavoriteLook();
        request.setAttribute("selectedLanguage", selectedLanguage);
        request.setAttribute("thesaurusStatus", thesaurusStatus);
        request.setAttribute("dragDropStatus", dragDropStatus);
        request.setAttribute("webdavEditingStatus", webdavEditingStatus);
        request.setAttribute("FavoriteSpace", personalizationScc
            .getFavoriteSpace());
        request.setAttribute("selectedLook", selectedLook);
        request.setAttribute("SpaceTreeview", personalizationScc
            .getSpaceTreeview());
        request.setAttribute("AllLanguages", personalizationScc
            .getAllLanguages());

        destination = "/personalizationPeas/jsp/personalization_Language.jsp";
      } else if (function.equals("SavePreferences")) {
        selectedLanguage = request.getParameter("SelectedLanguage");
        thesaurusStatus = request.getParameter("opt_thesaurusStatus");
        dragDropStatus = request.getParameter("opt_dragDropStatus");
        webdavEditingStatus = request.getParameter("opt_webdavEditingStatus");
        selectedWorkSpace = request.getParameter("SelectedWorkSpace");
        selectedLook = request.getParameter("SelectedLook");

        Boolean mustBeReloaded = isFramesetMustBeReloaded(selectedLanguage,
            selectedLook, personalizationScc);

        Vector<String> languages = new Vector<String>();
        languages.add(selectedLanguage);
        personalizationScc.setLanguages(languages);

        personalizationScc.setFavoriteLook(selectedLook);
        personalizationScc.setThesaurusStatus(new Boolean(thesaurusStatus)
            .booleanValue());
        personalizationScc.setDragAndDropStatus(new Boolean(dragDropStatus)
            .booleanValue());
        personalizationScc.setWebdavEditingStatus(new Boolean(
            webdavEditingStatus).booleanValue());

        if (selectedWorkSpace == null || selectedWorkSpace.equals("null"))
          personalizationScc.setPersonalWorkSpace(null);
        else
          personalizationScc.setPersonalWorkSpace(selectedWorkSpace);

        request.setAttribute("FramesetMustBeReloaded", mustBeReloaded);

        destination = getDestination("Main", componentSC, request);
      } else if (function.startsWith("personalization_Language")) {
        SilverTrace.info(getSessionControlBeanName(),
            "PersoPeasRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE");
        destination = getDestination("Main", componentSC, request);
      } else if (function.startsWith("ChangePassword")) {
        // Détermination du domaine du user
        boolean domainRW = personalizationScc.isUserDomainRW();
        if (domainRW)
          request.setAttribute("action", "userModify");
        else
          request.setAttribute("action", "userMS");

        UserFull uf = personalizationScc.getTargetUserFull();

        boolean updateIsAllowed = domainRW
            || personalizationScc.isPasswordChangeAllowed()
            || (uf.isPasswordValid() && uf.isPasswordAvailable());

        request.setAttribute("userObject", uf);
        request.setAttribute("UpdateIsAllowed", new Boolean(updateIsAllowed));
        request.setAttribute("minLengthPwd", new Integer(personalizationScc
            .getMinLengthPwd()));
        request.setAttribute("blanksAllowedInPwd", new Boolean(
            personalizationScc.isBlanksAllowedInPwd()));
        destination = "/personalizationPeas/jsp/changePassword.jsp";
      } else if (function.startsWith("EffectiveChangePassword")) {
        // Update informations only if updateMode is allowed for each field
        try {
          boolean updateFirstNameIsAllowed = rl.getBoolean("updateFirstName",
              false);
          boolean updateLastNameIsAllowed = rl.getBoolean("updateLastName",
              false);
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

          personalizationScc.modifyUser(
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
          request.setAttribute("Message", personalizationScc
              .getString("MessageOK"));
        } catch (AuthenticationBadCredentialException e) {
          request.setAttribute("Message", personalizationScc
              .getString("Error_bad_credential"));
        } catch (AuthenticationException e) {
          request.setAttribute("Message", personalizationScc
              .getString("Error_unknown"));
        }
        destination = getDestination("ChangePassword", componentSC, request);
      } else {
        destination = "/personalizationPeas/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info(getSessionControlBeanName(),
        "PersoPeasRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "destination = " + destination);
    return destination;
  }

  private Boolean isFramesetMustBeReloaded(String newLanguage, String newLook,
      PersonalizationSessionController personalizationScc)
      throws PeasCoreException {
    String actualLanguage = personalizationScc.getFavoriteLanguage();
    String actualLook = personalizationScc.getFavoriteLook();

    if (!newLanguage.equals(actualLanguage) || !newLook.equals(actualLook)) {
      return new Boolean(true);
    } else {
      return new Boolean(false);
    }
  }
}