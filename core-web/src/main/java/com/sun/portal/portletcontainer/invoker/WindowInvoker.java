/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.invoker;

import com.sun.portal.container.*;
import com.sun.portal.portletcontainer.invoker.util.InvokerUtil;
import com.sun.portal.portletcontainer.invoker.util.PortletWindowRules;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class is responsible for rendering the portlet markup fragments. It retrieves the portlet
 * markup fragments by delegating the portlet execution to the a container implementation.
 */
public abstract class WindowInvoker implements WindowInvokerConstants {

  // -----------------------------------------------------------
  // Static String used for creating Error URL
  // ----------------------------------------------------------
  private static final String ERROR_CODE = "errorCode";
  // --------------------------------------------------------
  // Static String used for setting the isTarget flag
  // -----------------------------------------------------
  public static final List localParamKeyList = initParamKeyList();
  private String title = "";
  private HttpServletRequest origRequest;
  private HttpServletResponse origResponse;
  private PortletWindowContext portletWindowContext;
  private String portletWindowName;
  private ChannelMode portletWindowMode;
  private ChannelState portletWindowState;
  private ServletContext servletContext;
  private ResponseProperties responseProperties;
  // ---
  // Debug logger
  // ---
  private final static Logger logger = ContainerLogger.getLogger(WindowInvoker.class,
      "org.silverpeas.portlets.PCCTXLogMessages");

  // ------------------------------------------------------------------
  //
  // Abstract methods to be implemented by the sub-class
  //
  // -----------------------------------------------------------------
  abstract public List getRoleList(HttpServletRequest request)
      throws InvokerException;

  abstract public Map getUserInfoMap(HttpServletRequest request)
      throws InvokerException;

  abstract public EntityID getEntityID(HttpServletRequest request)
      throws InvokerException;

  abstract public WindowRequestReader getWindowRequestReader()
      throws InvokerException;

  abstract public Container getContainer();

  abstract public ChannelURLFactory getPortletWindowURLFactory(
      String desktopURLPrefix,
      HttpServletRequest request)
      throws InvokerException;

  abstract public boolean isMarkupSupported(String contentType,
      String locale,
      ChannelMode mode,
      ChannelState state)
      throws InvokerException;

  abstract public String getDefaultTitle()
      throws InvokerException;

  // ***************************************************************** //
  // MAIN METHODS FOR GETTING render
  //
  // ******************************************************************
  /**
   * Initializes the WindowInvoker.
   * <p>
   * @param servletContext The ServletContext object.
   * @param request The HTTP request object.
   * @param response The HTTP response object.
   * @throws com.sun.portal.portletcontainer.invoker.InvokerException
   */
  public void init(ServletContext servletContext,
      HttpServletRequest request,
      HttpServletResponse response)
      throws InvokerException {
    this.origRequest = request;
    this.origResponse = response;
    this.servletContext = servletContext;
    try {
      PortletWindowContextAbstractFactory afactory = new PortletWindowContextAbstractFactory();
      PortletWindowContextFactory factory = afactory.getPortletWindowContextFactory();
      this.portletWindowContext = factory.getPortletWindowContext(request);
    } catch (PortletWindowContextException pwce) {
      throw new InvokerException("Initialization of WindowInvoker failed",
          pwce);
    }
  }

  public String getPortletWindowName() {
    return this.portletWindowName;
  }

  public void setPortletWindowName(String portletWindowName) {
    this.portletWindowName = portletWindowName;
  }

  protected HttpServletRequest getOriginalRequest() {
    return this.origRequest;
  }

  protected HttpServletResponse getOriginalResponse() {
    return this.origResponse;
  }

  protected ServletContext getServletContext() {
    return this.servletContext;
  }

  public ChannelMode getPortletWindowMode() {
    return this.portletWindowMode;
  }

  public void setPortletWindowMode(ChannelMode portletWindowMode) {
    this.portletWindowMode = portletWindowMode;
  }

  public ChannelState getPortletWindowState() {
    return this.portletWindowState;
  }

  public void setPortletWindowState(ChannelState portletWindowState) {
    this.portletWindowState = portletWindowState;
  }

  /**
   * Gets the content for the portletWindow based on mode from the underlying container. This method
   * is called to get content for VIEW, EDIT and and HELP mode . This method sets all the necessary
   * attributes in the ContainerRequest and Container Response and calls the configured container to
   * get the content for the portlet.
   * @param request An HttpServletRequest that contains information related to this request for
   * content.
   * @param response An HttpServletResponse that allows the portlet window context to influence the
   * overall response for the page (besides generating the content).
   * @return StringBuffer holding the content.
   * @exception InvokerException If there was an error generating the content.
   */
  public StringBuffer render(HttpServletRequest request,
      HttpServletResponse response)
      throws InvokerException {

    StringBuffer markupText = null;

    ErrorCode errorCode = readErrorCode(request);
    if (errorCode != null) {
      //
      // First, check if request is to report errors that processAction
      // might have ran in to, prior to render() request
      // Or due to explicit invocation of ErrorURL.
      logger.log(Level.FINE,
          "PSPL_PCCTXCSPPCI0001",
          errorCode);
      markupText = getErrorMessageContent(errorCode);
      // Since the error message is being sent, set the default title for this
      // portlet
      setTitle(getDefaultTitle());
    } else {
      //
      // Not an error case, get the normal content
      //

      try {
        markupText = getPortletContent(request,
            response);
      } catch (WindowException we) {
        logger.log(Level.SEVERE,
            "PSPL_PCCTXCSPPCI0006",
            we.getMessage());
        markupText = getErrorMessageContent(we.getErrorCode());
      } catch (InvokerException ie) {
        logger.log(Level.SEVERE,
            "PSPL_PCCTXCSPPCI0006",
            ie.getMessage());
        markupText = getErrorMessageContent(WindowErrorCode.CONTAINER_EXCEPTION);
      }
    }

    return markupText;
  }

  private StringBuffer getPortletContent(HttpServletRequest request,
      HttpServletResponse response)
      throws InvokerException,
      WindowException {

    //
    // We need to know if it is a authless user, because
    // parameters are stored in client properties or
    // in session properties based on whether this use is authless or
    // not.

    boolean authless = getPortletWindowContext().
        isAuthless(request);

    //
    // Abstract method to be implemented by the derived class
    //

    EntityID portletEntityId = getEntityID(request);

    //
    // Get current portlet mode
    //

    ChannelMode currentPortletWindowMode = getCurrentPortletWindowMode(request);

    //
    // Get current window state

    ChannelState currentWindowState = getCurrentWindowState(request);

    //
    // Get list of allowed window states
    //
    List allowableWindowStates = getAllowableWindowStates(request,
        currentPortletWindowMode);

    //
    // Get list of allowed portletWindow modes
    //
    List allowablePortletWindowModes = PortletWindowRules.getAllowablePortletWindowModes(
        currentPortletWindowMode,
        authless);

    String processURL = getActionURL(request,
        currentPortletWindowMode,
        currentWindowState);

    // Request
    GetMarkupRequest getMarkupRequest = getContainer().
        createGetMarkUpRequest(
        request,
        portletEntityId,
        currentWindowState,
        currentPortletWindowMode,
        portletWindowContext,
        getPortletWindowURLFactory(processURL,
        request));
    populateContainerRequest(getMarkupRequest,
        request,
        allowableWindowStates,
        allowablePortletWindowModes);

    // Response
    GetMarkupResponse getMarkupResponse = getContainer().
        createGetMarkUpResponse(response);

    //
    // Call the container interface
    //
    try {
      getContainer().
          getMarkup(getMarkupRequest,
          getMarkupResponse);
    } catch (ContainerException ce) {
      // If exception set the default title
      setTitle(getDefaultTitle());
      if (logger.isLoggable(Level.WARNING)) {
        LogRecord logRecord = new LogRecord(Level.WARNING,
            "PSPL_PCCTXCSPPCI0006");
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new String[] { getPortletWindowName() });
        logRecord.setThrown(ce);
        logger.log(logRecord);
      }
      throw new InvokerException("Container exception",
          ce);
    } catch (ContentException cte) {
      // If exception set the default title
      setTitle(getDefaultTitle());
      if (logger.isLoggable(Level.WARNING)) {
        LogRecord logRecord = new LogRecord(Level.WARNING,
            "PSPL_PCCTXCSPPCI0006");
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new String[] { getPortletWindowName() });
        logRecord.setThrown(cte);
        logger.log(logRecord);
      }
      throw new WindowException(getErrorCode(cte),
          "Content Exception",
          cte);
    }

    //
    // save the title so getTitle can return it
    //

    setTitle(getMarkupResponse.getTitle());

    //
    // Process the markup based on the mode.
    //

    if (getMarkupResponse.getMarkup() == null) {
      logger.info("PSPL_PCCTXCSPPCI0007");
    }

    // If headers, cookie have been set by the Portlet add it to the
    // ResponseProperties
    if (getMarkupResponse.getCookieProperties()
        != null
        || getMarkupResponse.getStringProperties()
        != null
        || getMarkupResponse.getElementProperties()
        != null) {
      this.responseProperties = new ResponseProperties();
      this.responseProperties.setCookies(getMarkupResponse.getCookieProperties());
      this.responseProperties.setResponseHeaders(getMarkupResponse.getStringProperties());
      this.responseProperties.setMarkupHeaders(getMarkupResponse.getElementProperties());
    }

    return getMarkupResponse.getMarkup();

  }

  // ****************************************************************
  //
  // MAIN METHODS FOR processAction
  //
  // ***************************************************************
  /**
   * Invokes the processAction of the portlet. This method sets all the necessary attributes in the
   * ContainerRequest and Container Response and calls the configured container to invoke the
   * processAction of the portlet.
   * @param request An HttpServletRequest that contains information related to this request for
   * content.
   * @param response An HttpServletResponse that allows the portlet window context to influence the
   * overall response for the page (besides generating the content).
   * @return URL redirect URL
   * @exception InvokerException If there was an error generating the content.
   */
  public URL processAction(HttpServletRequest request,
      HttpServletResponse response)
      throws InvokerException {

    try {
      return processActionInternal(request,
          response);
    } catch (WindowException we) {
      logger.log(Level.SEVERE,
          "PSPL_PCCTXCSPPCI0008",
          we.getMessage());
      return getErrorCodeURL(we.getErrorCode(),
          request);
    }
  }

  public URL processActionInternal(HttpServletRequest request,
      HttpServletResponse response)
      throws InvokerException,
      WindowException {

    URL returnURL = null;
    ChannelMode currentPortletWindowMode = null;
    ChannelMode newPortletWindowMode = null;
    ChannelState currentWindowState = null;
    ChannelState newWindowState = null;

    //
    // We need to know if it is a authless user, because
    // parameters are stored in client properties or
    // in session properties based on whether this use is authless or
    // not.

    boolean authless = getPortletWindowContext().
        isAuthless(request);

    //
    // Abstract method to be implemented by the derived class
    //
    EntityID portletEntityId = getEntityID(request);

    //
    // Get current portletWindow mode and window state
    //
    currentPortletWindowMode = getCurrentPortletWindowMode(request);

    currentWindowState = getCurrentWindowState(request);

    //
    // get new portletWindow mode and window state set on the url.
    //
    newWindowState = getWindowRequestReader().
        readNewWindowState(request);

    newPortletWindowMode = getWindowRequestReader().
        readNewPortletWindowMode(
        request);

    //
    // Process only the window state.
    // Won't process new ChannelMode, just check validity,
    // as an optimization
    // since it might change again after the processAction is called

    if (newPortletWindowMode != null) {
      validateModeChange(currentPortletWindowMode,
          newPortletWindowMode,
          authless);
      currentPortletWindowMode = newPortletWindowMode;
    }
    if (newWindowState != null) {
      currentWindowState = processWindowStateChange(request,
          newWindowState,
          currentPortletWindowMode,
          authless);
    }

    //
    // See what kind of URL is it

    ChannelURLType urlType = getWindowRequestReader().
        readURLType(request);
    Container c = ContainerFactory.getContainer(ContainerType.PORTLET_CONTAINER);
    String processURL = getActionURL(request,
        currentPortletWindowMode,
        currentWindowState);
    // Request
    ExecuteActionRequest executeActionRequest = getContainer().
        createExecuteActionRequest(request,
        portletEntityId,
        currentWindowState,
        currentPortletWindowMode,
        portletWindowContext,
        getPortletWindowURLFactory(processURL,
        request),
        getWindowRequestReader());

    List allowableWindowStates = getAllowableWindowStates(request,
        currentPortletWindowMode);
    List allowablePortletWindowModes = PortletWindowRules.getAllowablePortletWindowModes(
        currentPortletWindowMode,
        authless);
    populateContainerRequest(executeActionRequest,
        request,
        allowableWindowStates,
        allowablePortletWindowModes);

    // Response
    ExecuteActionResponse executeActionResponse = getContainer().
        createExecuteActionResponse(response);

    //
    // Call the container implementation to executeAction
    //
    try {
      getContainer().
          executeAction(executeActionRequest,
          executeActionResponse,
          urlType);
    } catch (ContainerException ce) {
      if (logger.isLoggable(Level.WARNING)) {
        LogRecord logRecord = new LogRecord(Level.WARNING,
            "PSPL_PCCTXCSPPCI0008");
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new String[] { getPortletWindowName() });
        logRecord.setThrown(ce);
        logger.log(logRecord);
      }
      throw new InvokerException(
          "WindowInvoker.processAction():container exception",
          ce);
    } catch (ContentException cte) {
      if (logger.isLoggable(Level.WARNING)) {
        LogRecord logRecord = new LogRecord(Level.WARNING,
            "PSPL_PCCTXCSPPCI0008");
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new String[] { getPortletWindowName() });
        logRecord.setThrown(cte);
        logger.log(logRecord);
      }
      throw new WindowException(getErrorCode(cte),
          "Content Exception",
          cte);
    }

    //
    // container.executeAction can either return a redirectURL or
    // change in mode,windowstate and new renderparams.
    // Both cases are mutually exclusive.
    //

    returnURL = executeActionResponse.getRedirectURL();

    if (returnURL == null) {

      //
      // process mode changes.

      newPortletWindowMode = executeActionResponse.getNewChannelMode();
      if (newPortletWindowMode != null) {
        validateModeChange(currentPortletWindowMode,
            newPortletWindowMode,
            authless);
        currentPortletWindowMode = newPortletWindowMode;
      }

      //
      // process state changes.
      //

      newWindowState = executeActionResponse.getNewWindowState();
      if (newWindowState != null) {
        currentWindowState = processWindowStateChange(request,
            newWindowState,
            currentPortletWindowMode,
            authless);
      }

    }

    //
    // Now we have new renderParams, new portletWindow mode
    // new window state
    //

    if (returnURL == null) {
      if (currentPortletWindowMode != null) {
        returnURL = processModeChange(request,
            currentPortletWindowMode,
            currentWindowState);
      }

    }

    return returnURL;
  }

  /**
   * Invokes the serveResource of the portlet. This method sets all the necessary attributes in the
   * ContainerRequest and Container Response and calls the configured container to invoke the
   * serveResource of the portlet.
   * @param request An HttpServletRequest that contains information related to this request for
   * content.
   * @param response An HttpServletResponse that allows the portlet window context to influence the
   * overall response for the page (besides generating the content).
   * @exception InvokerException If there was an error generating the content.
   */
  public void getResources(HttpServletRequest request,
      HttpServletResponse response)
      throws InvokerException {

    try {
      getResourcesInternal(request,
          response);
    } catch (WindowException we) {
      logger.log(Level.SEVERE,
          "PSPL_PCCTXCSPPCI0019",
          we.getMessage());
    }
  }

  public void getResourcesInternal(HttpServletRequest request,
      HttpServletResponse response)
      throws InvokerException,
      WindowException {

    boolean authless = getPortletWindowContext().
        isAuthless(request);

    //
    // Abstract method to be implemented by the derived class
    //

    EntityID portletEntityId = getEntityID(request);

    //
    // Get current portlet mode
    //

    ChannelMode currentPortletWindowMode = getCurrentPortletWindowMode(request);

    //
    // Get current window state

    ChannelState currentWindowState = getCurrentWindowState(request);

    //
    // Get list of allowed window states
    //
    List allowableWindowStates = getAllowableWindowStates(request,
        currentPortletWindowMode);

    //
    // Get list of allowed portletWindow modes
    //
    List allowablePortletWindowModes = PortletWindowRules.getAllowablePortletWindowModes(
        currentPortletWindowMode,
        authless);

    String processURL = getActionURL(request,
        currentPortletWindowMode,
        currentWindowState);

    // Request
    GetResourceRequest getResourceRequest = getContainer().
        createGetResourceRequest(request,
        portletEntityId,
        currentWindowState,
        currentPortletWindowMode,
        portletWindowContext,
        getPortletWindowURLFactory(processURL,
        request),
        getWindowRequestReader());
    populateContainerRequest(getResourceRequest,
        request,
        allowableWindowStates,
        allowablePortletWindowModes);

    // Response
    GetResourceResponse getResourceResponse = getContainer().
        createGetResourceResponse(response);

    //
    // Call the container interface
    //
    try {
      getContainer().
          getResources(getResourceRequest,
          getResourceResponse);
    } catch (ContainerException ce) {
      // If exception set the default title
      setTitle(getDefaultTitle());
      if (logger.isLoggable(Level.WARNING)) {
        LogRecord logRecord = new LogRecord(Level.WARNING,
            "PSPL_PCCTXCSPPCI0019");
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new String[] { getPortletWindowName() });
        logRecord.setThrown(ce);
        logger.log(logRecord);
      }
      throw new InvokerException("Container exception",
          ce);
    } catch (ContentException cte) {
      // If exception set the default title
      setTitle(getDefaultTitle());
      if (logger.isLoggable(Level.WARNING)) {
        LogRecord logRecord = new LogRecord(Level.WARNING,
            "PSPL_PCCTXCSPPCI0019");
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new String[] { getPortletWindowName() });
        logRecord.setThrown(cte);
        logger.log(logRecord);
      }
      throw new InvokerException("Container exception");
    }

    // If headers, cookie have been set by the Portlet add it to the
    // ResponseProperties
    if (getResourceResponse.getCookieProperties()
        != null
        || getResourceResponse.getStringProperties()
        != null
        || getResourceResponse.getElementProperties()
        != null) {
      this.responseProperties = new ResponseProperties();
      this.responseProperties.setCookies(getResourceResponse.getCookieProperties());
      this.responseProperties.setResponseHeaders(getResourceResponse.getStringProperties());
      this.responseProperties.setMarkupHeaders(getResourceResponse.getElementProperties());
    }

    InvokerUtil.setResponseProperties(request,
        response,
        this.responseProperties);

    try {
      StringBuffer buff = getResourceResponse.getContentAsBuffer();
      byte[] bytes = getResourceResponse.getContentAsBytes();
      if (buff != null) {
        response.getWriter().
            print(buff);

      } else if (bytes
          != null
          && bytes.length
          > 0) {
        response.getOutputStream().
            write(bytes);

      } else {
        response.getWriter().
            print("");
      }

      response.flushBuffer();
      InvokerUtil.clearResponseProperties(this.responseProperties);
    } catch (IOException e) {
      throw new InvokerException("Exception in Writing Response",
          e);
    }

  }

  /**
   * Populates the ContainerRequest object
   */
  protected void populateContainerRequest(ContainerRequest containerRequest,
      HttpServletRequest request,
      List allowableWindowStates,
      List allowablePortletWindowModes)
      throws InvokerException {

    //
    // allowable window state and mode determines the set of window state
    // and portlet mode the portlet can switch to programmatically
    //

    containerRequest.setAllowableWindowStates(allowableWindowStates);
    containerRequest.setAllowableChannelModes(allowablePortletWindowModes);

    //
    // set allowable states, modes and contentTypes.
    //

    String contentType = getPortletWindowContext().
        getContentType();
    List allowableContentTypes = new ArrayList();
    allowableContentTypes.add(contentType);
    containerRequest.setAllowableContentTypes(allowableContentTypes);

    containerRequest.setRoles(getRoleList(request));

    containerRequest.setUserInfo(getUserInfoMap(request));
  }

  /**
   * Gets the title for the portletWindow. This method returns the title from the portlet. Portlet
   * uses javax.portlet.title namespace for its title.
   * @return A string title.
   * @exception InvokerException if error occurs when getting the title for the portletWindow.
   */
  public String getTitle()
      throws InvokerException {
    if (title != null && title.trim().length() != 0) {
      return title;
    } else {
      return getDefaultTitle();
    }
  }

  /**
   * Gets the response properties for the portletWindow. This method returns the cookies, headers
   * that were set by the portlet.
   * @return ResponseProperties
   */
  public ResponseProperties getResponseProperties() {
    return this.responseProperties;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isEditable()
      throws InvokerException {

    //
    // Get handle to portlet description
    //
    //
    if (getPortletWindowContext().
        isAuthless(origRequest)) {
      return false;
    }

    return isMarkupSupported(getPortletWindowContext().
        getContentType(),
        getPortletWindowContext().
        getLocaleString(),
        ChannelMode.EDIT,
        ChannelState.MAXIMIZED);
  }

  /**
   * Process window state changes.
   */
  protected ChannelState processWindowStateChange(HttpServletRequest request,
      ChannelState newWindowState,
      ChannelMode portletWindowMode,
      boolean authless)
      throws InvokerException,
      WindowException {

    ChannelState windowState = newWindowState;
    boolean validState = PortletWindowRules.validateWindowStateChange(
        portletWindowMode,
        newWindowState);

    if (!validState
        || newWindowState
        == null) {

      windowState = PortletWindowRules.getDefaultWindowState(portletWindowMode);
      logger.log(Level.FINER,
          "PSPL_PCCTXCSPPCI0002",
          new Object[] {
          windowState,
          portletWindowMode });
    }

    return windowState;
  }

  /**
   * Assemble the URL to cause the desktop to be rendered with the new mode and window state
   */
  protected URL processModeChange(HttpServletRequest request,
      ChannelMode portletWindowMode,
      ChannelState portletWindowState)
      throws InvokerException {
    URL redirectURL = null;
    try {
      redirectURL = new URL(getRenderURL(request,
          portletWindowMode,
          portletWindowState));
    } catch (MalformedURLException mue) {
      throw new InvokerException("WindowInvoker.processModeChange():"
          + " couldn't generate redirect URL to page for mode "
          + portletWindowMode.toString(),
          mue);
    }
    return redirectURL;
  }

  private static List<String> initParamKeyList() {
    ArrayList<String> paramKeyList = new ArrayList();
    paramKeyList.add(PORTLET_WINDOW_KEY);
    paramKeyList.add(PORTLET_WINDOW_MODE_KEY);
    return paramKeyList;

  }

  /**
   * Used by subclasses to find out if key in the request is reserved by the window invoker
   */
  public static boolean isWindowInvokerKey(String key) {
    if (key != null) {
      if (localParamKeyList.contains(key)) {
        return true;
      }
    }
    return false;
  }

  // -----------------------------------------------------------------
  // Error Handling Methods
  // -----------------------------------------------------------------
  /**
   * Derived implementations can use this method to generate a error url if needed We return the URL
   * based on the current mode
   */
  public URL getErrorCodeURL(ErrorCode errorCode,
      HttpServletRequest request)
      throws InvokerException {

    try {
      //
      // Get the URL for the existing mode
      //
      String startURL = getPortletWindowContext().
          getDesktopURL(request)
          + "?";

      //
      // Append the error code to it
      //
      return new URL(startURL
          + getErrorCodeParameter()
          + "="
          + errorCode.toString());

    } catch (MalformedURLException mue) {
      throw new InvokerException(
          "WindowInvoker.getErrorCodeURL():couldn't build errorURL",
          mue);
    }
  }

  private String getErrorCodeParameter() {
    StringBuilder builder = new StringBuilder();
    builder.append(KEYWORD_PREFIX);
    builder.append(getPortletWindowName());
    builder.append(ERROR_CODE);
    return builder.toString();
  }

  //
  // Content rendered for error
  protected StringBuffer getErrorMessageContent(ErrorCode errorCode) {
    ResourceBundle bundle = null;
    StringBuffer buffer = new StringBuffer();
    try {
      bundle = getResourceBundle("org.silverpeas.portlets.multilang.WindowInvoker");
      buffer.append(bundle.getString(errorCode.toString()));
    } catch (MissingResourceException ex) {
      logger.log(Level.FINE,
          "PSPL_PCCTXCSPPCI0003",
          ex);
      if (bundle != null) {
        buffer.append(bundle.getString(WindowErrorCode.GENERIC_ERROR.toString()));
        buffer.append(" ");
        buffer.append(errorCode);
      } else {
        buffer.append(WindowErrorCode.GENERIC_ERROR.toString());
        buffer.append(" ");
        buffer.append(errorCode);
      }
    }
    return buffer;
  }

  //
  // This method can be overwritten by the
  // derived classes based on different container implementation
  // to spit out more fine grained error codes.
  // Overwriting this method IMPLIES overwriting getErrorMessageContent
  // too. In that case, derived classes should create resource bundle
  // with messages for the each new error code they might return from
  // getErrorCode method, and use the super class method for the rest.
  //
  protected ErrorCode getErrorCode(ContentException ex) {
    ErrorCode code = ex.getErrorCode();
    if (code == null) {
      return WindowErrorCode.CONTENT_EXCEPTION;
    } else {
      return code;
    }
  }

  //
  // Read error code from the request params
  //
  protected ErrorCode readErrorCode(HttpServletRequest request) {
    String errorCodeStr = request.getParameter(getErrorCodeParameter());

    if (errorCodeStr
        != null
        && errorCodeStr.length()
        > 0) {
      return new ErrorCode(errorCodeStr);
    } else {
      return null;
    }
  }

  protected ChannelMode getCurrentPortletWindowMode(HttpServletRequest request) {

    ChannelMode currentPortletWindowMode = getPortletWindowMode();

    if (currentPortletWindowMode != null) {
      return currentPortletWindowMode;
    }
    return ChannelMode.VIEW;
  }

  protected ChannelState getCurrentWindowState(HttpServletRequest request)
      throws InvokerException {

    ChannelState currentWindowState = getPortletWindowState();
    if (currentWindowState == null) {
      return PortletWindowRules.getDefaultWindowState(getCurrentPortletWindowMode(request));
    }

    return currentWindowState;
  }

  protected List getAllowableWindowStates(HttpServletRequest request,
      ChannelMode mode) {
    List allowableWindowStates = null;

    allowableWindowStates = PortletWindowRules.getDefaultAllowableWindowStates(mode);
    return allowableWindowStates;
  }

  public String getActionURL(HttpServletRequest request,
      ChannelMode portletWindowMode,
      ChannelState portletWindowState) {
    return computeURL(request, ACTION, portletWindowState, portletWindowMode);
  }

  public String getRenderURL(HttpServletRequest request,
      ChannelMode portletWindowMode,
      ChannelState portletWindowState) {
    return computeURL(request, RENDER, portletWindowState, portletWindowMode);
  }

  /**
   * Computes the URL corresponding to the specified action, window state, and window mode of the
   * portlet.
   * @param request the incoming HTTP request.
   * @param driverAction the next portlet action state.
   * @param portletWindowState the next portlet window state.
   * @param portletWindowMode the next portlet window mode.
   * @return the URL corresponding to the passed parameters.
   */
  private String computeURL(final HttpServletRequest request, String driverAction,
      final ChannelState portletWindowState, final ChannelMode portletWindowMode) {
    String portletWindowStateName = getPortletWindowState().toString();
    if (portletWindowState != null) {
      portletWindowStateName = portletWindowState.toString();
    }
    String portletWindowModeName = getPortletWindowMode().toString();
    if (portletWindowMode != null) {
      portletWindowModeName = portletWindowMode.toString();
    }

    StringBuilder processURL = new StringBuilder(getPortletWindowContext().
        getDesktopURL(request));

    processURL.append("?").
        append(DRIVER_ACTION).
        append("=").
        append(driverAction).
        append("&").
        append(PORTLET_WINDOW_MODE_KEY).
        append("=").
        append(portletWindowModeName).
        append("&").
        append(PORTLET_WINDOW_STATE_KEY).
        append("=").
        append(portletWindowStateName).
        append("&").
        append(PORTLET_WINDOW_KEY).
        append("=").
        append(getPortletWindowName());

    String spaceId = (String) request.getAttribute("SpaceId");
    if (StringUtil.isDefined(spaceId)) {
      processURL.append("&").
          append(WindowInvokerConstants.DRIVER_SPACEID).
          append("=").
          append(spaceId);
      processURL.append("&").
          append(WindowInvokerConstants.DRIVER_ROLE).
          append(
          "=").
          append("admin");
    }

    return processURL.toString();
  }

  /**
   * Throws a WindowException exception if attempt is made to change to a new mode that is not
   * allowed by the portal
   */
  private void validateModeChange(ChannelMode currentMode,
      ChannelMode newMode,
      boolean authless)
      throws WindowException {

    List allowedList = PortletWindowRules.getAllowablePortletWindowModes(
        currentMode,
        authless);
    if (!allowedList.contains(newMode)) {
      throw new WindowException(WindowErrorCode.INVALID_MODE_CHANGE_REQUEST,
          "Portal doesn't allow changing mode "
          + " from "
          + currentMode
          + " to "
          + newMode);
    }
    return;

  }

  /**
   * Gets the <code>PortletWindowContext</code> for the PortletiIndow.
   * @return <code>PortletWindowContext</code>.
   */
  public PortletWindowContext getPortletWindowContext() {
    return this.portletWindowContext;
  }

  /**
   * Gets a specified ResourceBundle file for the provider based on User's locale.
   * <p>
   * A provider can specify on-screen strings to be localized in a resource bundle file, as
   * described in the Java <code>ResourceBundle</code> class.
   * @param base a specified <code>ResourceBundle</code> name.
   * @see java.util.ResourceBundle
   * @return <code>ResourceBundle</code>.
   */
  public ResourceBundle getResourceBundle(String base) {
    return ResourceLocator.getLocalizationBundle(base, getPortletWindowContext().getLocaleString());
  }
}
