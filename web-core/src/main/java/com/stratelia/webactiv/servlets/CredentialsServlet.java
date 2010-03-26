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
package com.stratelia.webactiv.servlets;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.mail.MessagingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.PasswordGenerator;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.authentication.password.ForgottenPasswordException;
import com.stratelia.silverpeas.authentication.password.ForgottenPasswordMailManager;
import com.stratelia.silverpeas.authentication.password.ForgottenPasswordMailParameters;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Controller tier for credential management (Used by LoginFilter)
 * @author Ludovic Bertin
 */
public class CredentialsServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private Map<String, FunctionHandler> handlers = new HashMap<String, FunctionHandler>();
  private static LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();
  private Admin admin = new Admin();
  private ForgottenPasswordMailManager forgottenPasswordMailManager =
      new ForgottenPasswordMailManager(admin);
  private PasswordGenerator passwordGenerator = new PasswordGenerator();
  private ResourceBundle resources = null;
  private ResourceLocator m_Multilang = null;

  ResourceLocator general =
      new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");

  private abstract class FunctionHandler {
    abstract String doAction(HttpServletRequest request);
  }

  /**
   * Default Constructor : init handlers.
   */
  public CredentialsServlet() {
    super();
    initHandlers();
    initRessources();
  }

  private void initRessources() {
    resources =
        FileUtil.loadBundle("com.stratelia.silverpeas.peasCore.SessionManager", new Locale("",
        ""));
    String language = resources.getString("language");
    if ((language == null) || (language.length() <= 0)) {
      language = "fr";
    }
    m_Multilang = new ResourceLocator("com.stratelia.silverpeas.peasCore.multilang.peasCoreBundle",
        language);
  }

  /**
   * Load mapping between functions and associated handlers
   */
  private void initHandlers() {
    /*
     * Password change management
     */
    handlers.put("ForcePasswordChange", forcePasswordChangeHandler);
    handlers.put("EffectiveChangePassword", effectiveChangePasswordHandler);

    handlers.put("ChangeQuestion", changeQuestionHandler);
    handlers.put("ValidateQuestion", validationQuestionHandler);
    handlers.put("LoginQuestion", loginQuestionHandler);
    handlers.put("ValidateAnswer", validationAnswerHandler);
    handlers.put("ChangePassword", changePasswordHandler);

    // Password reset management
    handlers.put("ForgotPassword", forgotPasswordHandler);
    handlers.put("ResetPassword", resetPasswordHandler);
    handlers.put("ResetLoginPassword", resetLoginPasswordHandler);
    handlers.put("SendMessage", sendMessageHandler);
  }

  /**
   * Navigation case : force user to change his password.
   */
  private FunctionHandler forcePasswordChangeHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {

      /*
       * Retrieves key
       */
      HttpSession session = request.getSession(true);
      String key = (String) session.getAttribute("svplogin_Key");

      try {
        /*
         * Retrieves user detail
         */
        String userId = admin.authenticate(key, session.getId(), false, false);
        UserDetail ud = admin.getUserDetail(userId);
        request.setAttribute("userDetail", ud);

        /*
         * forward to correct JSP page
         */
        String changePasswordPage = general.getString("userLoginForcePasswordChangePage");
        return changePasswordPage;

      } catch (AdminException e) {
        // Error : go back to login page
        SilverTrace.error("peasCore", "forcePasswordChangeHandler.doAction()",
            "peasCore.EX_USER_KEY_NOT_FOUND", "key=" + key);
        return "/Login.jsp";
      }
    }
  };

  /**
   * Navigation case : user has committed change password form.
   */
  private FunctionHandler effectiveChangePasswordHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {

      HttpSession session = request.getSession(true);
      String key = (String) session.getAttribute("svplogin_Key");

      try {
        /*
         * Retrieves user detail
         */
        String userId = admin.authenticate(key, session.getId(), false, false);
        UserDetail ud = admin.getUserDetail(userId);
        String login = ud.getLogin();
        String domainId = ud.getDomainId();
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");

        /*
         * Change password
         */
        LoginPasswordAuthentication auth = new LoginPasswordAuthentication();
        auth.changePassword(login, oldPassword, newPassword, domainId);

        /*
         * forward to correct JSP page
         */
        return "/LoginServlet";
      } catch (AdminException e) {
        SilverTrace.error("peasCore", "effectiveChangePasswordHandler.doAction()",
            "peasCore.EX_USER_KEY_NOT_FOUND", "key=" + key);
        return forcePasswordChangeHandler.doAction(request);
      } catch (AuthenticationException e) {
        SilverTrace.error("peasCore", "effectiveChangePasswordHandler.doAction()",
            "peasCore.EX_USER_KEY_NOT_FOUND", "key=" + key);
        request.setAttribute("message", m_Multilang.getString("badCredentials"));
        return forcePasswordChangeHandler.doAction(request);
      }
    }
  };

  /**
   * Navigation case : user arrives on the page which allows him to select his login question.
   */
  private FunctionHandler changeQuestionHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {
      return general.getString("userLoginQuestionSelectionPage");
    }
  };

  /**
   * Navigation case : user has validated login question form.
   */
  private FunctionHandler validationQuestionHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {

      HttpSession session = request.getSession(true);
      String key = (String) session.getAttribute("svplogin_Key");
      try {
        String userId = admin.authenticate(key, session.getId(), false, false);
        UserDetail userDetail = admin.getUserDetail(userId);

        String question = request.getParameter("question");
        String answer = request.getParameter("answer");
        userDetail.setLoginQuestion(question);
        userDetail.setLoginAnswer(answer);

        admin.updateUser(userDetail);

        if ("true".equals(general.getString("userLoginForcePasswordChange"))) {
          // User is forced to modify his password.
          return general.getString("userLoginForcePasswordChangePage");
        } else {
          // User can log himself.
          return "/LoginServlet";
        }
      } catch (AdminException e) {
        // Error : go back to login page
        SilverTrace.error("peasCore", "validationQuestionHandler.doAction()",
            "peasCore.EX_USER_KEY_NOT_FOUND", "key=" + key);
        return "/Login.jsp";
      }
    }
  };

  /**
   * Navigation case : user forgot his password and will be asked for his login question.
   */
  private FunctionHandler loginQuestionHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {
      String login = request.getParameter("Login");
      String domainId = request.getParameter("DomainId");
      try {
        String userId = admin.getUserIdByLoginAndDomain(login, domainId);
        UserDetail userDetail = admin.getUserDetail(userId);
        if (StringUtil.isDefined(userDetail.getLoginQuestion())) {
          request.setAttribute("userDetail", userDetail);
          return general.getString("userLoginQuestionPage");
        } else {
          // page d'erreur : veuillez contacter votre admin
          return "/Login.jsp";
        }
      } catch (AdminException e) {
        // Error : go back to login page
        SilverTrace.error("peasCore", "loginQuestionHandler.doAction()",
            "peasCore.EX_USER_KEY_NOT_FOUND", "login=" + login);
        return "/Login.jsp";
      }
    }
  };

  /**
   * Navigation case : user validates his answer to his login question.
   */
  private FunctionHandler validationAnswerHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {
      String login = request.getParameter("Login");
      String domainId = request.getParameter("DomainId");
      String answer = request.getParameter("answer");
      try {
        String userId = admin.getUserIdByLoginAndDomain(login, domainId);
        UserDetail userDetail = admin.getUserDetail(userId);
        request.setAttribute("userDetail", userDetail);

        if (answer.equals(userDetail.getLoginAnswer())) {
          return general.getString("userResetPasswordPage");
        } else {
          // Invalid answer.
          request.setAttribute("message", m_Multilang.getString("invalidAnswer"));
          return general.getString("userLoginQuestionPage");
        }
      } catch (AdminException e) {
        // Error : go back to login page
        SilverTrace.error("peasCore", "validationAnswerHandler.doAction()",
            "peasCore.EX_USER_KEY_NOT_FOUND", "login=" + login);
        return "/Login.jsp";
      }
    }
  };

  /**
   * Navigation case : user has changed his password.
   */
  private FunctionHandler changePasswordHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {
      String login = request.getParameter("Login");
      String domainId = request.getParameter("DomainId");
      String password = request.getParameter("password");
      try {
        // Change password.
        LoginPasswordAuthentication auth = new LoginPasswordAuthentication();
        auth.resetPassword(login, password, domainId);

        return "/AuthenticationServlet?Login=" + login + "&Password=" + password
            + "&DomainId=" + domainId;
      } catch (Exception e) {
        // Error : go back to login page
        SilverTrace.error("peasCore", "changePasswordHandler.doAction()",
            "peasCore.EX_USER_KEY_NOT_FOUND", "login=" + login);
        return "/Login.jsp";
      }
    }
  };

  private FunctionHandler forgotPasswordHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {
      String login = request.getParameter("Login");
      String domainId = request.getParameter("DomainId");
      String userId = null;
      try {
        userId = admin.getUserIdByLoginAndDomain(login, domainId);
      } catch (AdminException e) {
        // Login incorrect.
        request.setAttribute("login", login);

        Hashtable<String, String> domains = lpAuth.getAllDomains();
        String dId = null;
        String domain = "";
        for (Enumeration<String> en = domains.keys(); en.hasMoreElements();) {
          dId = en.nextElement();
          if (dId.equals(domainId)) {
            domain = domains.get(dId);
          }
        }
        request.setAttribute("domain", domain);
        return general.getString("forgottenPasswordInvalidLogin");
      }

      try {
        if (lpAuth.isPasswordChangeAllowed(domainId)) {
          String authenticationKey = null;
          try {
            authenticationKey = lpAuth.getAuthenticationKey(login, domainId);
          } catch (AuthenticationException e) {
            throw new ForgottenPasswordException(
                "CredentialsServlet.forgotPasswordHandler.doAction()",
                "forgottenPassword.EX_GET_USER_AUTHENTICATION_KEY",
                "login=" + login + " ; domainId=" + domainId, e);
          }

          // Envoi d'un mail contenant un lien permettant de lancer la réinitialisation
          // automatique du mot de passe.
          try {
            ForgottenPasswordMailParameters parameters = getMailParameters(userId);
            parameters.setLink(
                getContextPath(request) + "/ResetPassword?key=" + authenticationKey);
            forgottenPasswordMailManager.sendResetPasswordRequestMail(parameters);
            return general.getString("forgottenPasswordChangeAllowed");
          } catch (AdminException e) {
            throw new ForgottenPasswordException(
                "CredentialsServlet.forgotPasswordHandler.doAction()",
                "forgottenPassword.EX_GET_USER_DETAIL", "userId=" + userId, e);
          } catch (MessagingException e) {
            throw new ForgottenPasswordException(
                "CredentialsServlet.forgotPasswordHandler.doAction()",
                "forgottenPassword.EX_SEND_MAIL", "userId=" + userId, e);
          }
        } else {
          // Affichage d'un message d'information invitant à joindre l'administrateur système
          return general.getString("forgottenPasswordChangeNotAllowed");
        }
      } catch (ForgottenPasswordException fpe) {
        return forgottenPasswordError(request, fpe);
      }
    }
  };

  private FunctionHandler resetPasswordHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {
      try {
        String authenticationKey = request.getParameter("key");
        String userId = null;
        try {
          userId = admin.getUserIdByAuthenticationKey(authenticationKey);
        } catch (Exception e) {
          return general.getString("forgottenPasswordResetError");
        }
        if (userId != null) {
          String password = passwordGenerator.random();
          ForgottenPasswordMailParameters parameters = null;
          try {
            parameters = getMailParameters(userId);
          } catch (AdminException e) {
            throw new ForgottenPasswordException(
                "CredentialsServlet.resetPasswordHandler.doAction()",
                "forgottenPassword.EX_GET_USER_DETAIL", "userId=" + userId, e);
          }

          UserFull user;
          try {
            user = admin.getUserFull(userId);
          } catch (AdminException e) {
            throw new ForgottenPasswordException(
                "CredentialsServlet.resetPasswordHandler.doAction()",
                "forgottenPassword.EX_GET_FULL_USER_DETAIL", "userId=" + userId, e);
          }
          user.setPassword(password);

          try {
            admin.updateUserFull(user);
          } catch (AdminException e) {
            throw new ForgottenPasswordException(
                "CredentialsServlet.resetPasswordHandler.doAction()",
                "forgottenPassword.EX_UPDATE_USER_DETAIL", "userId=" + userId, e);
          }

          parameters.setPassword(password);
          parameters.setLink(getContextPath(request) + "/ResetLoginPassword"
              + "?login=" + user.getLogin()
              + "&domainId=" + user.getDomainId());
          try {
            forgottenPasswordMailManager.sendNewPasswordMail(parameters);
          } catch (MessagingException e) {
            throw new ForgottenPasswordException(
                "CredentialsServlet.resetPasswordHandler.doAction()",
                "forgottenPassword.EX_SEND_MAIL", "userId=" + userId, e);
          }

          return general.getString("forgottenPasswordReset");
        } else {
          return general.getString("forgottenPasswordResetError");
        }
      } catch (ForgottenPasswordException fpe) {
        return forgottenPasswordError(request, fpe);
      }
    }
  };

  private FunctionHandler resetLoginPasswordHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {
      StringBuffer url = new StringBuffer(general.getString("forgottenPasswordLoginUrl"));
      String login = request.getParameter("login");
      if (StringUtil.isDefined(login)) {
        request.getSession().setAttribute("specialLogin", login);
      }
      String domainId = request.getParameter("domainId");
      if (StringUtil.isDefined(domainId)) {
        request.getSession().setAttribute("specialDomainId", domainId);
      }
      return url.toString();
    }
  };

  private FunctionHandler sendMessageHandler = new FunctionHandler() {
    String doAction(HttpServletRequest request) {
      try {
        ForgottenPasswordMailParameters parameters = new ForgottenPasswordMailParameters();
        parameters.setUserName(
            request.getParameter("firstname") + " " + request.getParameter("lastname"));
        parameters.setEmail(request.getParameter("email"));
        parameters.setMessage(request.getParameter("message"));
        try {
          forgottenPasswordMailManager.sendAdminMail(parameters);
          return general.getString("forgottenPasswordSendMessage");
        } catch (MessagingException e) {
          throw new ForgottenPasswordException(
              "CredentialsServlet.sendMessageHandler.doAction()",
              "forgottenPassword.EX_SEND_MAIL", e);
        }
      } catch (ForgottenPasswordException fpe) {
        return forgottenPasswordError(request, fpe);
      }
    }
  };

  private String forgottenPasswordError(HttpServletRequest request, ForgottenPasswordException fpe) {
    String error = SilverTrace.getTraceMessage(fpe.getMessage()) + " - " + fpe.getExtraInfos();
    ForgottenPasswordMailParameters parameters = new ForgottenPasswordMailParameters();
    parameters.setError(error);
    try {
      forgottenPasswordMailManager.sendErrorMail(parameters);
    } catch (MessagingException e) {
      SilverTrace.error("peasCore",
          "CredentialsServlet.forgottenPasswordError()",
          "forgottenPassword.EX_SEND_MAIL", e);
    }
    request.setAttribute("error", error);
    return general.getString("forgottenPasswordError");
  }

  private ForgottenPasswordMailParameters getMailParameters(String userId) throws AdminException {
    ForgottenPasswordMailParameters parameters = new ForgottenPasswordMailParameters();
    UserDetail userDetail = admin.getUserDetail(userId);
    parameters.setUserName(userDetail.getDisplayedName());
    parameters.setLogin(userDetail.getLogin());
    parameters.setDomainId(userDetail.getDomainId());
    parameters.setToAddress(userDetail.geteMail());
    return parameters;
  }

  private String getContextPath(HttpServletRequest request) {
    String requestUrl = request.getRequestURL().toString();
    String servletPath = request.getServletPath();
    String contextPath = requestUrl.substring(
        0, requestUrl.indexOf(servletPath) + servletPath.length());
    return contextPath;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String function = getFunction(request);

    FunctionHandler handler = (FunctionHandler) handlers.get(function);
    if (handler != null) {
      String destinationPage = handler.doAction(request);
      RequestDispatcher dispatcher = request.getRequestDispatcher(destinationPage);
      dispatcher.forward(request, response);
    } else
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "command not found : " + function);
  }

  /**
   * Retrieves function from path info.
   * @param request HTTP request
   * @return the function as a String
   */
  private String getFunction(HttpServletRequest request) {
    String function = "Error";
    String pathInfo = request.getPathInfo();
    if (pathInfo != null) {
      pathInfo = pathInfo.substring(1); // remove first '/'
      function = pathInfo.substring(pathInfo.indexOf("/") + 1);
    }

    return function;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

}
