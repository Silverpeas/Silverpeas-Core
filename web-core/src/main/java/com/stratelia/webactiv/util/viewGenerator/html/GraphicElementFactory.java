/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * GraphicElementFactory.java
 *
 * Created on 10 octobre 2000, 16:26
 */

package com.stratelia.webactiv.util.viewGenerator.html;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane;
import com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPaneWA;
import com.stratelia.webactiv.util.viewGenerator.html.board.Board;
import com.stratelia.webactiv.util.viewGenerator.html.board.BoardSP;
import com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar;
import com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBarWA;
import com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane;
import com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPaneWA;
import com.stratelia.webactiv.util.viewGenerator.html.buttons.Button;
import com.stratelia.webactiv.util.viewGenerator.html.buttons.ButtonWA;
import com.stratelia.webactiv.util.viewGenerator.html.calendar.Calendar;
import com.stratelia.webactiv.util.viewGenerator.html.calendar.CalendarWA1;
import com.stratelia.webactiv.util.viewGenerator.html.formPanes.FormPane;
import com.stratelia.webactiv.util.viewGenerator.html.formPanes.FormPaneWA;
import com.stratelia.webactiv.util.viewGenerator.html.frame.Frame;
import com.stratelia.webactiv.util.viewGenerator.html.frame.FrameWA;
import com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane;
import com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPaneWA;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendar;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendarWA1;
import com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList;
import com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationListWA;
import com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane;
import com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPaneWA;
import com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination;
import com.stratelia.webactiv.util.viewGenerator.html.pagination.PaginationSP;
import com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane;
import com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPaneWA;
import com.stratelia.webactiv.util.viewGenerator.html.window.Window;
import com.stratelia.webactiv.util.viewGenerator.html.window.WindowWA;

/**
 * The GraphicElementFactory is the only class to instanciate in this package.
 * You should have one factory for each client (for future evolution). The
 * GraphicElementFactory is responsible from graphic component instanciation.
 * You should never directly instanciate a component without using this factory !
 * This class uses the "factory design pattern".
 */
public class GraphicElementFactory extends Object {
  public static final String GE_FACTORY_SESSION_ATT = "SessionGraphicElementFactory";
  private static ResourceLocator settings = null;
  private static String iconsPath = null;
  private ResourceLocator lookSettings = null;
  private ResourceLocator silverpeasLookSettings = null;
  private ResourceLocator favoriteLookSettings = null;
  private String defaultLook = "com.stratelia.webactiv.util.viewGenerator.settings.Initial";
  private static ResourceLocator generalSettings = null;

  private String currentLookName = null;
  private String externalStylesheet = null;

  /**
   * Creates new GraphicElementFactory
   */
  public GraphicElementFactory() {
  }

  /**
   * Constructor declaration
   *
   *
   * @param look
   *
   * @see
   */
  public GraphicElementFactory(String look) {
    setLook(look);
  }

  public static String getIconsPath() {
    if (iconsPath == null) {
      iconsPath = getGeneralSettings().getString("ApplicationURL")
          + getSettings().getString("IconsPath");
    }
    return iconsPath;
  }

  public static ResourceLocator getGeneralSettings() {
    if (generalSettings == null) {
      generalSettings = new ResourceLocator("com.stratelia.webactiv.general",
          "fr");
    }
    return generalSettings;
  }

  /**
   * Get the settings for the factory.
   *
   * @return The ResourceLocator returned contains all default environment
   *         settings necessary to know wich component to instanciate, but also
   *         to know how to generate html code.
   */
  public static ResourceLocator getSettings() {
    if (settings == null) {
      settings = new ResourceLocator("com.stratelia.webactiv.util.viewGenerator.settings.graphicElementFactorySettings", "");
    }
    return settings;
  }

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public ResourceLocator getLookSettings() {
    SilverTrace.info("viewgenerator", "GraphicElementFactory.getLookSettings()", "root.MSG_GEN_ENTER_METHOD");
    if (lookSettings == null) {
      SilverTrace.info("viewgenerator", "GraphicElementFactory.getLookSettings()", "root.MSG_GEN_EXIT_METHOD", "lookSettings == null");
      // get the customer lookSettings
      try {
        lookSettings = new ResourceLocator("com.stratelia.webactiv.util.viewGenerator.settings.lookSettings", "");
      } catch (java.util.MissingResourceException e) {
        // the customer lookSettings is undefined
        // get the default silverpeas looks
        lookSettings = getSilverpeasLookSettings();
      }
    }
    SilverTrace.info("viewgenerator", "GraphicElementFactory.getLookSettings()", "root.MSG_GEN_EXIT_METHOD");
    return lookSettings;
  }

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public ResourceLocator getSilverpeasLookSettings() {
    SilverTrace.info("viewgenerator", "GraphicElementFactory.getSilverpeasLookSettings()", "root.MSG_GEN_ENTER_METHOD");
    if (silverpeasLookSettings == null) {
      silverpeasLookSettings = new ResourceLocator("com.stratelia.webactiv.util.viewGenerator.settings.defaultLookSettings", "");
    }
    return silverpeasLookSettings;
  }

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public ResourceLocator getFavoriteLookSettings() {
    return this.favoriteLookSettings;
  }

  /**
   * Method declaration
   *
   *
   * @param look
   *
   * @see
   */
  public void setLook(String look) {
    lookSettings = getLookSettings();
    String selectedLook = null;

    // get the customer lookSettings
    try {
      selectedLook = lookSettings.getString(look, null);
    } catch (java.util.MissingResourceException e) {
      // the customer lookSettings is undefined
      // get the default silverpeas looks
      // lookSettings = null;
      SilverTrace.info("viewgenerator", "GraphicElementFactory.setLook()", "root.MSG_GEN_PARAM_VALUE", " customer lookSettings is undefined !");
      lookSettings = getSilverpeasLookSettings();

      selectedLook = silverpeasLookSettings.getString(look, null);
      if (selectedLook == null) {
        // ce look n'existe plus, look par defaut
        selectedLook = defaultLook;
      }
    }

    SilverTrace.info("viewgenerator", "GraphicElementFactory.setLook()", "root.MSG_GEN_PARAM_VALUE", " look = " + look + " | corresponding settings = " + selectedLook);
    this.favoriteLookSettings = new ResourceLocator(selectedLook, "");

    currentLookName = look;
  }

  public String getCurrentLookName() {
    return currentLookName;
  }
  
  public void setExternalStylesheet(String externalStylesheet)
  {
	  this.externalStylesheet = externalStylesheet;
  }

  public boolean hasExternalStylesheet()
  {
	  return (externalStylesheet != null);
  }

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public String getLookFrame() {
    SilverTrace.info("viewgenerator", "GraphicElementFactory.getLookFrame()", "root.MSG_GEN_PARAM_VALUE", " FrameJSP = "+getFavoriteLookSettings().getString("FrameJSP"));
    return getFavoriteLookSettings().getString("FrameJSP");
  }

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public String getLookStyleSheet() {
    SilverTrace.info("viewgenerator", "GraphicElementFactory.getLookStyleSheet()", "root.MSG_GEN_ENTER_METHOD");
    String standardStyle = "/util/styleSheets/globalSP_SilverpeasV4.css";
    String v5Style = "/util/styleSheets/globalSP_SilverpeasV5.css";
    String lookStyle = getFavoriteLookSettings().getString("StyleSheet");
    String contextPath = getGeneralSettings().getString("ApplicationURL");
    String charset = getGeneralSettings().getString("charset", "ISO-8859-1");
    StringBuffer code = new StringBuffer();

    code.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset="+ charset + "\">\n");
    
    if (externalStylesheet == null)
    {
    	code.append("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"").append(contextPath).append(standardStyle).append("\">\n");
    	code.append("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"").append(contextPath).append(v5Style).append("\">\n");
    	if (lookStyle.length() > 0) {
    		code.append("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"").append(lookStyle).append("\">");
    	}
    }
    else
    {
    	code.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(externalStylesheet).append("\">\n");
    }

    if (getFavoriteLookSettings() != null
        && getFavoriteLookSettings().getString("OperationPane").toLowerCase()
            .endsWith("web20"))
      code.append(getYahooElements());

    SilverTrace.info("viewgenerator", "GraphicElementFactory.getLookStyleSheet()", "root.MSG_GEN_EXIT_METHOD");
    return code.toString();
  }

  private String getYahooElements() {
    String contextPath = getGeneralSettings().getString("ApplicationURL");
    StringBuffer code = new StringBuffer();

    code.append("<!-- CSS for Menu -->");
    code.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""+getSettings().getString("YUIMenuCss", contextPath+"/util/yui/menu/assets/menu.css")+"\">");
    code.append("<!-- Page-specific styles -->");
    code.append("<style type=\"text/css\">");
    code.append("    div.yuimenu {");
    code.append("    position:dynamic;");
    code.append("    visibility:hidden;");
    code.append("    }");
    code.append("</style>");
    
    code.append("<script type=\"text/javascript\" src=\""+contextPath+"/util/yui/yahoo-dom-event/yahoo-dom-event.js\"></script>");
    code.append("<script type=\"text/javascript\" src=\""+contextPath+"/util/yui/container/container_core-min.js\"></script>");
    code.append("<script type=\"text/javascript\" src=\""+contextPath+"/util/yui/menu/menu-min.js\"></script>");

    return code.toString();
  }

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public String getIcon(String iconKey) {
    SilverTrace.info("viewgenerator", "GraphicElementFactory.getIcon()", "root.MSG_GEN_ENTER_METHOD", "iconKey = " + iconKey);
    String iconURL = getFavoriteLookSettings().getString(iconKey, null);
    return iconURL;
  }

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public Vector getAvailableLooks() {
    ResourceLocator lookSettings = getLookSettings();
    Enumeration keys = lookSettings.getKeys();
    Vector vector = new Vector();

    while (keys.hasMoreElements()) {
      vector.add((String) keys.nextElement());
    }
    return vector;
  }

  /**
   * Construct a new button.
   *
   * @param label
   *          The new button label
   * @param action
   *          The action associated exemple :
   *          "javascript:onClick=history.back()", or
   *          "http://www.stratelia.com/"
   * @param disabled
   *          Specify if the button is disabled or not. If disabled, no action
   *          will be possible.
   * @return returns an object implementing the FormButton interface. That's the
   *         new button to use.
   */
  public Button getFormButton(String label, String action, boolean disabled) {
    Button button = null;
    String buttonClassName = getFavoriteLookSettings().getString("Button");

    try {
      button = (Button) Class.forName(buttonClassName).newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator",
          "GraphicElementFactory.getFormButton()",
          "viewgenerator.EX_CANT_GET_BUTTON", "", e);
      button = (Button) new ButtonWA();
    } finally {
      button.init(label, action, disabled);
    }
    return button;
  }

  /**
   * Construct a new frame.
   *
   * @param title
   *          The new frame title
   * @return returns an object implementing the Frame interface. That's the new
   *         frame to use.
   */
  public Frame getFrame() {
    Frame frame = null;
    String frameClassName = getFavoriteLookSettings().getString("Frame");

    try {
      frame = (Frame) Class.forName(frameClassName).newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator", "GraphicElementFactory.getFrame()",
          "viewgenerator.EX_CANT_GET_FRAME", "", e);
      frame = (Frame) new FrameWA();
    }
    return frame;
  }

  /**
   * Construct a new board.
   *
   * @return returns an object implementing the Board interface. That's the new
   *         board to use.
   */
  public Board getBoard() {
    Board board = null;
    String boardClassName = getFavoriteLookSettings().getString("Board");

    try {
      board = (Board) Class.forName(boardClassName).newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator", "GraphicElementFactory.getBoard()",
          "viewgenerator.EX_CANT_GET_FRAME", "", e);
      board = (Board) new BoardSP();
    }
    return board;
  }

  /**
   * Construct a new navigation list.
   *
   * @return returns an object implementing the NavigationList interface.
   */
  public NavigationList getNavigationList() {
    NavigationList navigationList = null;
    String navigationListClassName = getFavoriteLookSettings().getString(
        "NavigationList");

    try {
      navigationList = (NavigationList) Class.forName(navigationListClassName)
          .newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator",
          "GraphicElementFactory.getNavigationList()",
          "viewgenerator.EX_CANT_GET_NAVIGATIONLIST", "", e);
      navigationList = (NavigationList) new NavigationListWA();
    }
    return navigationList;
  }

  /**
   * Construct a new button.
   *
   * @deprecated
   * @return returns an object implementing the FormButton interface. That's the
   *         new button to use.
   * @param label
   *          The new button label
   * @param action
   *          The action associated exemple : "javascript:history.back()", or
   *          "http://www.stratelia.com/"
   * @param disabled
   *          Specify if the button is disabled or not. If disabled, no action
   *          will be possible.
   * @param imagePath
   *          The path where the images needed to display buttons will be found.
   */
  public Button getFormButton(String label, String action, boolean disabled,
      String imagePath) {
    return getFormButton(label, action, disabled);
  }

  /**
   * Build a new TabbedPane.
   *
   * @return An object implementing the TabbedPane interface.
   */
  public TabbedPane getTabbedPane() {
    String tabbedPaneClassName = getFavoriteLookSettings().getString(
        "TabbedPane");
    TabbedPane tabbedPane = null;

    try {
      tabbedPane = (TabbedPane) Class.forName(tabbedPaneClassName)
          .newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator",
          "GraphicElementFactory.getTabbedPane()",
          "viewgenerator.EX_CANT_GET_TABBED_PANE", "", e);
      tabbedPane = new TabbedPaneWA();
    } finally {
      tabbedPane.init(1);
    }
    return tabbedPane;
  }

  /**
   * Build a new TabbedPane.
   *
   * @return An object implementing the TabbedPane interface.
   */
  public TabbedPane getTabbedPane(int nbLines) {
    String tabbedPaneClassName = getFavoriteLookSettings().getString(
        "TabbedPane");
    TabbedPane tabbedPane = null;

    try {
      tabbedPane = (TabbedPane) Class.forName(tabbedPaneClassName)
          .newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator",
          "GraphicElementFactory.getTabbedPane()",
          "viewgenerator.EX_CANT_GET_TABBED_PANE", " nbLines = " + nbLines, e);
      tabbedPane = new TabbedPaneWA();
    } finally {
      tabbedPane.init(nbLines);
    }
    return tabbedPane;
  }

  /**
   * Build a new ArrayPane.
   *
   * @deprecated
   * @param name
   *          The name from your array. This name has to be unique in the
   *          session. It will be used to put some information (including the
   *          sorted column), in the session. exemple : "MyToDoArrayPane"
   * @param pageContext
   *          The page context computed by the servlet or JSP. The PageContext
   *          is used to both get new request (sort on a new column), and keep
   *          the current state (via the session).
   * @return An object implementing the ArrayPane interface.
   *
   */
  public ArrayPane getArrayPane(String name,
      javax.servlet.jsp.PageContext pageContext) {
    String arrayPaneClassName = getFavoriteLookSettings()
        .getString("ArrayPane");
    ArrayPane arrayPane = null;

    try {
      arrayPane = (ArrayPane) Class.forName(arrayPaneClassName).newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator",
          "GraphicElementFactory.getArrayPane()",
          "viewgenerator.EX_CANT_GET_ARRAY_PANE", " name = " + name, e);
      arrayPane = new ArrayPaneWA();
    } finally {
      arrayPane.init(name, pageContext);
    }
    return arrayPane;
  }

  /**
   * Build a new ArrayPane.
   *
   * @deprecated
   * @param name
   *          The name from your array. This name has to be unique in the
   *          session. It will be used to put some information (including the
   *          sorted column), in the session. exemple : "MyToDoArrayPane"
   * @param request
   *          The http request (to get entering action, like sort operation)
   * @param session
   *          The client session (to get the old status, like on which column we
   *          are sorted)
   * @return An object implementing the ArrayPane interface.
   *
   */
  public ArrayPane getArrayPane(String name,
      javax.servlet.ServletRequest request,
      javax.servlet.http.HttpSession session) {
    String arrayPaneClassName = getFavoriteLookSettings()
        .getString("ArrayPane");
    ArrayPane arrayPane = null;

    try {
      arrayPane = (ArrayPane) Class.forName(arrayPaneClassName).newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator",
          "GraphicElementFactory.getArrayPane()",
          "viewgenerator.EX_CANT_GET_ARRAY_PANE", " name = " + name, e);
      arrayPane = new ArrayPaneWA();
    } finally {
      arrayPane.init(name, request, session);
    }
    return arrayPane;
  }

  /**
   * Build a new ArrayPane.
   *
   * @param name
   *          The name from your array. This name has to be unique in the
   *          session. It will be used to put some information (including the
   *          sorted column), in the session. exemple : "MyToDoArrayPane"
   * @param url
   *          The url to root sorting action. This url can contain parameters.
   *          exemple :
   *          http://localhost/webactiv/Rkmelia/topicManager?topicId=12
   * @param request
   *          The http request (to get entering action, like sort operation)
   * @param session
   *          The client session (to get the old status, like on which column we
   *          are sorted)
   * @return An object implementing the ArrayPane interface.
   *
   */
  public ArrayPane getArrayPane(String name, String url,
      javax.servlet.ServletRequest request,
      javax.servlet.http.HttpSession session) {
    String arrayPaneClassName = getFavoriteLookSettings()
        .getString("ArrayPane");
    ArrayPane arrayPane = null;

    try {
      arrayPane = (ArrayPane) Class.forName(arrayPaneClassName).newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator",
          "GraphicElementFactory.getArrayPane()",
          "viewgenerator.EX_CANT_GET_ARRAY_PANE", " name = " + name, e);
      arrayPane = new ArrayPaneWA();
    } finally {
      arrayPane.init(name, url, request, session);
    }
    return arrayPane;
  }

  /**
   * Build a new main Window using the object specified in the properties.
   *
   * @return An object implementing Window interface
   */
  public Window getWindow() {
    String windowClassName = getFavoriteLookSettings().getString("Window");
    Window window = null;

    try {
      window = (Window) Class.forName(windowClassName).newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator", "GraphicElementFactory.getWindow()",
          "viewgenerator.EX_CANT_GET_WINDOW", "", e);
      window = new WindowWA();
    } finally {
      window.init(this);
    }
    return window;
  }

  /**
   * Build a new ButtonPane.
   *
   * @return An object implementing the ButtonPane interface
   */
  public ButtonPane getButtonPane() {
    String buttonPaneClassName = getFavoriteLookSettings().getString(
        "ButtonPane");

    try {
      return (ButtonPane) Class.forName(buttonPaneClassName).newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator",
          "GraphicElementFactory.getButtonPane()",
          "viewgenerator.EX_CANT_GET_BUTTON_PANE", "", e);
      return new ButtonPaneWA();
    }
  }

  /**
   * Build a new IconPane.
   *
   * @return An object implementing the IconPane interface.
   */
  public IconPane getIconPane() {
    String iconPaneClassName = getFavoriteLookSettings().getString("IconPane");

    try {
      return (IconPane) Class.forName(iconPaneClassName).newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator", "GraphicElementFactory.getIconPane()",
          "viewgenerator.EX_CANT_GET_ICON_PANE", "", e);
      return new IconPaneWA();
    }
  }

  /**
   * Build a new FormPane.
   *
   * @param name
   * @param actionURL
   * @param pageContext
   * @return
   */
  public FormPane getFormPane(String name, String actionURL,
      javax.servlet.jsp.PageContext pageContext) {
    return new FormPaneWA(name, actionURL, pageContext);
  }

  /**
   * Build a new OperationPane.
   *
   * @return An object implementing the OperationPane interface.
   */
  public OperationPane getOperationPane() {
    String operationPaneClassName = getFavoriteLookSettings().getString(
        "OperationPane");

    try {
      return (OperationPane) Class.forName(operationPaneClassName)
          .newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator",
          "GraphicElementFactory.getOperationPane()",
          "viewgenerator.EX_CANT_GET_OPERATION_PANE", "", e);
      return new OperationPaneWA();
    }
  }

  /**
   * Build a new BrowseBar.
   *
   * @return An object implementing the BrowseBar interface.
   */
  public BrowseBar getBrowseBar() {
    String browseBarClassName = getFavoriteLookSettings()
        .getString("BrowseBar");

    try {
      return (BrowseBar) Class.forName(browseBarClassName).newInstance();
    } catch (Exception e) {
      SilverTrace.error("viewgenerator",
          "GraphicElementFactory.getBrowseBar()",
          "viewgenerator.EX_CANT_GET_BROWSE_BAR", "", e);
      return new BrowseBarWA();
    }
  }

  /**
   * Build a new monthCalendar.
   *
   * @param String:
   *          the language to use by the monthCalendar
   * @return an object implementing the monthCalendar interface
   */
  public MonthCalendar getMonthCalendar(String language) {
    return new MonthCalendarWA1(language);
  }

  /**
   * Build a new Calendar.
   *
   * @param String:
   *          the language to use by the monthCalendar
   * @return an object implementing the monthCalendar interface
   */
  public Calendar getCalendar(String context, String language, Date date) {
    return new CalendarWA1(context, language, date);
  }

  public Pagination getPagination(int nbItems, int nbItemsPerPage,
      int firstItemIndex) {
    String paginationClassName = getFavoriteLookSettings().getString(
        "Pagination");
    Pagination pagination = null;
    try {
      pagination = (Pagination) Class.forName(paginationClassName)
          .newInstance();
    } catch (Exception e) {
      SilverTrace.info("viewgenerator",
          "GraphicElementFactory.getPagination()",
          "viewgenerator.EX_CANT_GET_PAGINATION", "", e);
      pagination = new PaginationSP();
    }
    pagination.init(nbItems, nbItemsPerPage, firstItemIndex);
    return pagination;
  }
}