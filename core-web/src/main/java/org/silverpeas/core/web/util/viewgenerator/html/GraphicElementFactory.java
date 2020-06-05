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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPaneSilverpeasV5;
import org.silverpeas.core.web.util.viewgenerator.html.board.Board;
import org.silverpeas.core.web.util.viewgenerator.html.board.BoardSilverpeasV5;
import org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar;
import org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBarComplete;
import org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane;
import org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPaneWA2;
import org.silverpeas.core.web.util.viewgenerator.html.buttons.Button;
import org.silverpeas.core.web.util.viewgenerator.html.buttons.ButtonSilverpeasV5;
import org.silverpeas.core.web.util.viewgenerator.html.calendar.Calendar;
import org.silverpeas.core.web.util.viewgenerator.html.calendar.CalendarWA1;
import org.silverpeas.core.web.util.viewgenerator.html.frame.Frame;
import org.silverpeas.core.web.util.viewgenerator.html.frame.FrameSilverpeasV5;
import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane;
import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPaneWA;
import org.silverpeas.core.web.util.viewgenerator.html.navigationlist.NavigationList;
import org.silverpeas.core.web.util.viewgenerator.html.navigationlist.NavigationListSilverpeasV5;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPaneSilverpeasV5Web20;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.PaginationSP;
import org.silverpeas.core.web.util.viewgenerator.html.progressmessage.ProgressMessage;
import org.silverpeas.core.web.util.viewgenerator.html.progressmessage.ProgressMessageSilverpeasV5;
import org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane;
import org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPaneSilverpeasV5;
import org.silverpeas.core.web.util.viewgenerator.html.window.Window;
import org.silverpeas.core.web.util.viewgenerator.html.window.WindowWeb20V5;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import static org.silverpeas.core.web.mvc.controller.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

/**
 * The GraphicElementFactory is the only class to instanciate in this package. You should have one
 * factory for each client (for future evolution). The GraphicElementFactory is responsible from
 * graphic component instanciation. You should never directly instanciate a component without using
 * this factory ! This class uses the "factory design pattern".
 */
public class GraphicElementFactory {

  /**
   * The key with which is associated the resources wrapper used by a Silverpeas component instance
   * and that is carried in each request.
   */
  public static final String RESOURCES_KEY = "resources";
  public static final String GE_FACTORY_SESSION_ATT = "SessionGraphicElementFactory";
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.util.viewGenerator.settings.graphicElementFactorySettings");
  private static final String ARRAY_PANE = "ArrayPane";
  private static SettingBundle lookSettings = null;
  private SettingBundle favoriteLookSettings = null;
  private static final String REQUEST_SPACE_ID = GraphicElementFactory.class + "_REQUEST_SPACE_ID";
  private static final String REQUEST_COMPONENT_ID =
      GraphicElementFactory.class + "_REQUEST_COMPONENT_ID";
  private static final String REQUEST_IS_COMPONENT_MAIN_PAGE =
      GraphicElementFactory.class + "_REQUEST_IS_COMPONENT_MAIN_PAGE";
  private static final String REQUEST_EXTERNAL_STYLESHEET =
      GraphicElementFactory.class + "_REQUEST_EXTERNAL_STYLESHEET";
  private static final String ICONS_PATH =
      (URLUtil.getApplicationURL() + settings.getString("IconsPath")).replaceAll("/$", "");
  private LocalizationBundle multilang = null;
  private String currentLookName = null;
  private MainSessionController mainSessionController = null;
  public static final String defaultLookName = "Initial";
  static final String MOMENT_JS = "moment-with-locales.min.js";
  static final String MOMENT_TIMEZONE_JS = "moment-timezone-with-data.min.js";
  public static final String STANDARD_CSS = "/util/styleSheets/silverpeas-main.css";

  static {
    lookSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.util.viewGenerator.settings.lookSettings");
    try {
      lookSettings.getString("dummy", null);
    } catch (MissingResourceException e) {
      SilverLogger.getLogger(GraphicElementFactory.class)
          .warn("lookSettings bundle not found. Load then defaultLookSettings bundle");
      lookSettings = ResourceLocator.getSettingBundle(
          "org.silverpeas.util.viewGenerator.settings.defaultLookSettings");
    }
  }

  /**
   * Constructor declaration
   * @param look
   * @see
   */
  public GraphicElementFactory(String look) {
    setLook(look);
  }

  public GraphicElementFactory(MainSessionController mainSessionController) {
    this.mainSessionController = mainSessionController;
    String userLook = mainSessionController.getFavoriteLook();
    String effectiveLook = setLook(userLook);
    if (!userLook.equals(effectiveLook)) {
      mainSessionController.getPersonalization().setLook(effectiveLook);
    }
  }

  public static String getIconsPath() {
    return ICONS_PATH;
  }

  public LocalizationBundle getMultilang() {
    if (multilang == null) {
      String language = getLanguage();
      multilang = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.util.viewGenerator.multilang.graphicElementFactoryBundle", language);
    }
    return multilang;
  }

  private String getLanguage() {
    String language = I18NHelper.defaultLanguage;
    if (mainSessionController != null) {
      language = mainSessionController.getFavoriteLanguage();
    }
    return language;
  }

  /**
   * Get the settings for the factory.
   * @return The ResourceLocator returned contains all default environment settings necessary to
   * know wich component to instanciate, but also to know how to generate html code.
   */
  public static SettingBundle getSettings() {
    return settings;
  }

  /**
   * Method declaration
   * @return Customer specific look settings if defined, default look settings otherwise
   * @see
   */
  public SettingBundle getLookSettings() {
    return lookSettings;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public SettingBundle getFavoriteLookSettings() {
    return this.favoriteLookSettings;
  }

  /**
   * Method declaration
   * @param lookName
   * @see
   */
  public final String setLook(String lookName) {
    // get the customer lookSettings
    String aLookName = lookName;
    String look = lookSettings.getString(aLookName, null);
    if (!isLookAllowed(aLookName)) {
      // this look no more exists or is not allowed, set an existing look
      aLookName = getAvailableLooksForUser().get(0);
      look = lookSettings.getString(aLookName, null);
    }
    this.favoriteLookSettings = ResourceLocator.getSettingBundle(look);
    this.currentLookName = aLookName;
    return this.currentLookName;
  }

  private static String getExistingLook() {
    Set<String> lookNames = lookSettings.keySet();
    for (String aLookName : lookNames) {
      if (isLookExist(aLookName)) {
        return aLookName;
      }
    }
    return defaultLookName;
  }

  private static boolean isLookExist(String lookName) {
    try {
      String selectedLook = lookSettings.getString(lookName);
      SettingBundle look = ResourceLocator.getSettingBundle(selectedLook);
      return look.exists();
    } catch (MissingResourceException e) {
      SilverLogger.getLogger(GraphicElementFactory.class)
          .error("Look named '" + lookName + "' not found", e);
      return false;
    }
  }

  private boolean isLookAllowed(String lookName) {
    return getAvailableLooksForUser().contains(lookName) && isLookExist(lookName);
  }

  public String getCurrentLookName() {
    return currentLookName;
  }

  public static String getCSSOfLook(String lookName) {
    return getLookSettings(lookName).getString("StyleSheet", null);
  }

  public static SettingBundle getLookSettings(String lookName) {
    String look = lookSettings.getString(lookName, null);
    if (!isLookExist(lookName)) {
      String existingLookName = getExistingLook();
      look = lookSettings.getString(existingLookName, null);
    }
    return ResourceLocator.getSettingBundle(look);
  }

  public void setExternalStylesheet(String externalStylesheet) {
    CacheServiceProvider.getRequestCacheService().getCache()
        .put(REQUEST_EXTERNAL_STYLESHEET, externalStylesheet);
  }

  public String getExternalStylesheet() {
    return CacheServiceProvider.getRequestCacheService().getCache()
        .get(REQUEST_EXTERNAL_STYLESHEET, String.class);
  }

  public boolean hasExternalStylesheet() {
    return (getExternalStylesheet() != null);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getLookFrame() {
    return getFavoriteLookSettings().getString("FrameJSP");
  }

  /**
   * Use {@link LookAndStyleTag} instead
   * @return HTML header fragment
   * @deprecated
   */
  @Deprecated
  public String getLookStyleSheet(final HttpRequest request) {
    return WebCommonLookAndFeel.getInstance().getCommonHeader(request);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIcon(String iconKey) {
    return getFavoriteLookSettings().getString(iconKey, null);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public List<String> getAvailableLooks() {
    SettingBundle theLookSettings = getLookSettings();
    return new ArrayList<>(theLookSettings.keySet());
  }

  public List<String> getAvailableLooksForUser() {
    if (getMainSessionController() != null) {
      String domainId = User.getById(getMainSessionController().getUserId()).getDomainId();
      OrganizationController controller = OrganizationController.get();
      Domain domain = controller.getDomain(domainId);
      List<String> domainLooks = domain.getLooks();
      if (!domainLooks.isEmpty()) {
        return domainLooks;
      }
    }
    return getAvailableLooks();
  }

  /**
   * Construct a new button.
   * @param label The new button label
   * @param action The action associated exemple : "javascript:onClick=history.back()", or
   * "http://www.stratelia.com/"
   * @param disabled Specify if the button is disabled or not. If disabled, no action will be
   * possible.
   * @return returns an object implementing the FormButton interface. That's the new button to use.
   */
  public Button getFormButton(String label, String action, boolean disabled) {
    Button button = null;
    String buttonClassName = getFavoriteLookSettings().getString("Button");
    try {
      button = (Button) Class.forName(buttonClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      button = new ButtonSilverpeasV5();
    } finally {
      if (button != null) {
        button.init(label, action, disabled);
      }
    }
    return button;
  }

  /**
   * Construct a new frame.
   * @return returns an object implementing the Frame interface. That's the new frame to use.
   */
  public Frame getFrame() {
    Frame frame;
    String frameClassName = getFavoriteLookSettings().getString("Frame");

    try {
      frame = (Frame) Class.forName(frameClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      frame = new FrameSilverpeasV5();
    }
    return frame;
  }

  /**
   * Construct a new board.
   * @return returns an object implementing the Board interface. That's the new board to use.
   */
  public Board getBoard() {
    Board board;
    String boardClassName = getFavoriteLookSettings().getString("Board");

    try {
      board = (Board) Class.forName(boardClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      board = new BoardSilverpeasV5();
    }
    return board;
  }

  /**
   * Construct a new navigation list.
   * @return returns an object implementing the NavigationList interface.
   */
  public NavigationList getNavigationList() {
    NavigationList navigationList;
    String navigationListClassName = getFavoriteLookSettings().getString("NavigationList");

    try {
      navigationList = (NavigationList) Class.forName(navigationListClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      navigationList = new NavigationListSilverpeasV5();
    }
    return navigationList;
  }

  /**
   * Construct a new button.
   * @param label The new button label
   * @param action The action associated exemple : "javascript:history.back()", or
   * "http://www.stratelia.com/"
   * @param disabled Specify if the button is disabled or not. If disabled, no action will be
   * possible.
   * @param imagePath The path where the images needed to display buttons will be found.
   * @return returns an object implementing the FormButton interface. That's the new button to use.
   * @deprecated
   */
  @Deprecated
  public Button getFormButton(String label, String action, boolean disabled, String imagePath) {
    return getFormButton(label, action, disabled);
  }

  /**
   * Build a new TabbedPane.
   * @return An object implementing the TabbedPane interface.
   */
  public TabbedPane getTabbedPane() {
    String tabbedPaneClassName = getFavoriteLookSettings().getString("TabbedPane");
    TabbedPane tabbedPane = null;
    try {
      tabbedPane = (TabbedPane) Class.forName(tabbedPaneClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      tabbedPane = new TabbedPaneSilverpeasV5();
    }
    return tabbedPane;
  }

  /**
   * Build a new ArrayPane.
   * @param name The name from your array. This name has to be unique in the session. It will be
   * used to put some information (including the sorted column), in the session. exemple :
   * "MyToDoArrayPane"
   * @param pageContext The page context computed by the servlet or JSP. The PageContext is used to
   * both get new request (sort on a new column), and keep the current state (via the session).
   * @return An object implementing the ArrayPane interface.
   * @deprecated
   */
  @Deprecated
  public ArrayPane getArrayPane(String name, javax.servlet.jsp.PageContext pageContext) {
    String arrayPaneClassName = getFavoriteLookSettings().getString(ARRAY_PANE);
    ArrayPane arrayPane = null;

    try {
      arrayPane = (ArrayPane) Class.forName(arrayPaneClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      arrayPane = new ArrayPaneSilverpeasV5();
    } finally {
      if (arrayPane != null) {
        arrayPane.init(name, pageContext);
      }
    }
    return arrayPane;
  }

  /**
   * Build a new ArrayPane.
   * @param name The name from your array. This name has to be unique in the session. It will be
   * used to put some information (including the sorted column), in the session. exemple :
   * "MyToDoArrayPane"
   * @param request The http request (to get entering action, like sort operation)
   * @param session The client session (to get the old status, like on which column we are sorted)
   * @return An object implementing the ArrayPane interface.
   * @deprecated
   */
  @Deprecated
  public ArrayPane getArrayPane(String name, ServletRequest request, HttpSession session) {
    String arrayPaneClassName = getFavoriteLookSettings().getString(ARRAY_PANE);
    ArrayPane arrayPane = null;
    try {
      arrayPane = (ArrayPane) Class.forName(arrayPaneClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      arrayPane = new ArrayPaneSilverpeasV5();
    } finally {
      if (arrayPane != null) {
        arrayPane.init(name, request, session);
      }
    }
    return arrayPane;
  }

  /**
   * Build a new ArrayPane.
   * @param name The name from your array. This name has to be unique in the session. It will be
   * used to put some information (including the sorted column), in the session. exemple :
   * "MyToDoArrayPane"
   * @param url The url to root sorting action. This url can contain parameters. exemple :
   * http://localhost/webactiv/Rkmelia/topicManager?topicId=12
   * @param request The http request (to get entering action, like sort operation)
   * @param session The client session (to get the old status, like on which column we are sorted)
   * @return An object implementing the ArrayPane interface.
   */
  public ArrayPane getArrayPane(String name, String url, ServletRequest request,
      HttpSession session) {
    String arrayPaneClassName = getFavoriteLookSettings().getString(ARRAY_PANE);
    ArrayPane arrayPane = null;
    try {
      arrayPane = (ArrayPane) Class.forName(arrayPaneClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      arrayPane = new ArrayPaneSilverpeasV5();
    } finally {
      if (arrayPane != null) {
        arrayPane.init(name, url, request, session);
      }
    }
    return arrayPane;
  }

  /**
   * Build a new main Window using the object specified in the properties.
   * @return An object implementing Window interface
   */
  public Window getWindow() {
    String windowClassName = getFavoriteLookSettings().getString("Window");
    Window window = null;
    try {
      window = (Window) Class.forName(windowClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      window = new WindowWeb20V5();
    } finally {
      if (window != null) {
        window.init(this);
      }
    }
    return window;
  }

  /**
   * Build a new ButtonPane.
   * @return An object implementing the ButtonPane interface
   */
  public ButtonPane getButtonPane() {
    String buttonPaneClassName = getFavoriteLookSettings().getString("ButtonPane");

    try {
      return (ButtonPane) Class.forName(buttonPaneClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new ButtonPaneWA2();
    }
  }

  /**
   * Build a new IconPane.
   * @return An object implementing the IconPane interface.
   */
  public IconPane getIconPane() {
    String iconPaneClassName = getFavoriteLookSettings().getString("IconPane");
    try {
      return (IconPane) Class.forName(iconPaneClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return new IconPaneWA();
    }
  }

  /**
   * Build a new OperationPane.
   * @return An object implementing the OperationPane interface.
   */
  public OperationPane getOperationPane() {
    String operationPaneClassName = getFavoriteLookSettings().getString("OperationPane");
    OperationPane operationPane;
    try {
      operationPane = (OperationPane) Class.forName(operationPaneClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      operationPane = new OperationPaneSilverpeasV5Web20();
    }
    operationPane.setMultilang(getMultilang());
    return operationPane;
  }

  /**
   * Build a new BrowseBar.
   * @return An object implementing the BrowseBar interface.
   */
  public BrowseBar getBrowseBar() {
    String browseBarClassName = getFavoriteLookSettings().getString("BrowseBar");
    try {
      BrowseBar browseBar = (BrowseBar) Class.forName(browseBarClassName).newInstance();
      browseBar.setComponentId(getComponentIdOfCurrentRequest());
      browseBar.setMainSessionController(mainSessionController);
      return browseBar;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      BrowseBar browseBar = new BrowseBarComplete();
      browseBar.setComponentId(getComponentIdOfCurrentRequest());
      browseBar.setMainSessionController(mainSessionController);
      return browseBar;
    }
  }

  /**
   * Build a new SilverpeasCalendar.
   * @param language : the language to use by the monthCalendar
   * @return an object implementing the monthCalendar interface
   */
  public Calendar getCalendar(String context, String language, Date date) {
    return new CalendarWA1(context, language, date);
  }

  public Pagination getPagination(int nbItems, int nbItemsPerPage, int firstItemIndex) {
    Pagination pagination = getPagination();
    pagination.init(nbItems, nbItemsPerPage, firstItemIndex);
    return pagination;
  }

  public Pagination getPagination() {
    String paginationClassName = getFavoriteLookSettings().getString("Pagination", null);
    Pagination pagination;
    if (paginationClassName == null) {
      paginationClassName = "org.silverpeas.core.web.util.viewgenerator.html.pagination.PaginationSP";
    }
    try {
      pagination = (Pagination) Class.forName(paginationClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      pagination = new PaginationSP();
    }
    pagination.setMultilang(getMultilang());
    return pagination;
  }

  public ProgressMessage getProgressMessage(List<String> messages) {
    String progressClassName = getFavoriteLookSettings().getString("Progress", null);
    ProgressMessage progress;
    if (progressClassName == null) {
      progressClassName =
          "org.silverpeas.core.web.util.viewgenerator.html.progressmessage.ProgressMessageSilverpeasV5";
    }
    try {
      progress = (ProgressMessage) Class.forName(progressClassName).newInstance();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      progress = new ProgressMessageSilverpeasV5();
    }
    progress.init(messages);
    progress.setMultilang(getMultilang());
    return progress;
  }

  public void setComponentIdForCurrentRequest(String componentId) {
    CacheServiceProvider.getRequestCacheService().getCache().put(REQUEST_COMPONENT_ID, componentId);
  }

  public String getComponentIdOfCurrentRequest() {
    return CacheServiceProvider.getRequestCacheService()
        .getCache()
        .get(REQUEST_COMPONENT_ID, String.class);
  }

  public MainSessionController getMainSessionController() {
    return mainSessionController;
  }

  public void setHttpRequest(HttpRequest request) {
    mainSessionController = null;
    HttpSession session = request.getSession(false);
    if (session != null) {
      mainSessionController =
          (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
    }
    boolean isComponentMainPage =
        request.getRequestURI().endsWith("/Main") && !request.getRequestURI().
            endsWith("/jsp/Main");
    CacheServiceProvider.getRequestCacheService().getCache()
        .put(REQUEST_IS_COMPONENT_MAIN_PAGE, isComponentMainPage);
  }

  public boolean isComponentMainPage() {
    Boolean isComponentMainPage = CacheServiceProvider.getRequestCacheService().getCache()
        .get(REQUEST_IS_COMPONENT_MAIN_PAGE, Boolean.class);
    return isComponentMainPage != null && isComponentMainPage;
  }

  /**
   * @return the space identifier
   */
  public String getSpaceIdOfCurrentRequest() {
    return CacheServiceProvider.getRequestCacheService()
        .getCache()
        .get(REQUEST_SPACE_ID, String.class);
  }

  /**
   * @param spaceId the space identifier to set (full identifier with WA + number)
   */
  public void setSpaceIdForCurrentRequest(String spaceId) {
    CacheServiceProvider.getRequestCacheService().getCache().put(REQUEST_SPACE_ID, spaceId);
  }

  /**
   * Retrieve default look name
   * @return user personal look settings if defined, default look settings otherwise
   */
  public String getDefaultLookName() {
    // Retrieve user personal look settings
    String userLookStyle;
    try {
      userLookStyle = mainSessionController.getPersonalization().getLook();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      userLookStyle = defaultLookName;
    }
    return userLookStyle;
  }
}