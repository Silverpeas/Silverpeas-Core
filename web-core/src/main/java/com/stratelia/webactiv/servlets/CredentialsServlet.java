package com.stratelia.webactiv.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Controller tier for credential management (Used by LoginFilter)
 * 
 * @author Ludovic Bertin
 *
 */
public class CredentialsServlet
	extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	private Map handlers = new HashMap();
	private Admin admin = new Admin();
	private ResourceBundle resources = null;
	private ResourceLocator m_Multilang = null;
	
	ResourceLocator general = new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");

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
		resources = ResourceBundle.getBundle("com.stratelia.silverpeas.peasCore.SessionManager", new Locale("", ""));
		String language = resources.getString("language");
		if ((language == null) || (language.length() <= 0))
		{
			language = "fr";
		}
		m_Multilang = new ResourceLocator("com.stratelia.silverpeas.peasCore.multilang.peasCoreBundle", language);
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
	}

	/**
	 * Information class containing absolute path and context. 
	 */
	private class UriInfo {
		protected String absolutePath = null;
		protected String context = null;
		
		/**
		 * @param absolutePath
		 * @param context
		 */
		public UriInfo(String absolutePath, String context) {
			this.absolutePath = absolutePath;
			this.context = context;
		}
	}
	
	/**
	 * Navigation case : force user to change his password.
	 */
	private FunctionHandler forcePasswordChangeHandler = new FunctionHandler() {
		String doAction(HttpServletRequest request) {
			
			/*
			 * Analyze URL
			 */
	        UriInfo uriInfo = getUriInfo(request);

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
	            SilverTrace.error("peasCore","forcePasswordChangeHandler.doAction()","peasCore.EX_USER_KEY_NOT_FOUND","key=" + key);
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
	            SilverTrace.error("peasCore","effectiveChangePasswordHandler.doAction()","peasCore.EX_USER_KEY_NOT_FOUND","key=" + key);
	            return forcePasswordChangeHandler.doAction(request);
			} catch (AuthenticationException e) {
	            SilverTrace.error("peasCore","effectiveChangePasswordHandler.doAction()","peasCore.EX_USER_KEY_NOT_FOUND","key=" + key);
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
				UriInfo uriInfo = getUriInfo(request);
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
				if (userDetail.getLoginQuestion().length() > 0) {
					request.setAttribute("userDetail", userDetail);
					return general.getString("userLoginQuestionPage");
				} else {
					// page d'erreur : veuillez contacter votre admin
					return "/Login.jsp";
				}
			} catch (AdminException e) {
				// Error : go back to login page
				UriInfo uriInfo = getUriInfo(request);
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
				UriInfo uriInfo = getUriInfo(request);
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
				UriInfo uriInfo = getUriInfo(request);
	            SilverTrace.error("peasCore", "changePasswordHandler.doAction()",
	            	"peasCore.EX_USER_KEY_NOT_FOUND", "login=" + login);
	            return "/Login.jsp";
			}
		}
	};
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String function = getFunction(request);
		
		FunctionHandler handler = (FunctionHandler) handlers.get(function);
		if (handler != null) {
			String destinationPage = handler.doAction(request);
			RequestDispatcher dispatcher = request.getRequestDispatcher(destinationPage);
			dispatcher.forward(request, response);
		}
		else
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "command not found : " + function);
	}

	/**
	 * Retrieves function from path info.
	 * 
	 * @param request	HTTP request
	 * 
	 * @return the function as a String
	 */
	private String getFunction(HttpServletRequest request) {
		String function = "Error";
		String pathInfo = request.getPathInfo();
        if (pathInfo != null)
        {
			pathInfo = pathInfo.substring(1); //remove first '/'
			function = pathInfo.substring(pathInfo.indexOf("/") + 1);
        }
        
        return function;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	/**
	 * Calculate absolute path and context from request.
	 * 
	 * @param request	the HTTP request
	 * 
	 * @return a UriInfo object containing absolute path and context
	 */
	private UriInfo getUriInfo(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String requestURL = request.getRequestURL().toString();
        String absolutePath = requestURL.substring(0, requestURL.length() - uri.length());

        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        if(pathInfo != null)
            uri = uri.substring(0,uri.lastIndexOf(pathInfo));
        String context = uri.substring(0,uri.lastIndexOf(servletPath));
        if(context.charAt(context.length()-1) == '/')
        {
            context = context.substring(0, context.length()-1);
        }
        
        return new UriInfo(absolutePath, context);
		
	}
	
}
