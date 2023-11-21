/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.window;

import org.apache.ecs.html.Div;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.look.proxy.SpaceHomepageProxy;
import org.silverpeas.core.web.look.proxy.SpaceHomepageProxyManager;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPaneType;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author neysseri
 */
public abstract class AbstractWindow implements Window {

  private static final String DIV = "</div>";
  private BrowseBar browseBar = null;
  private OperationPane operationPane = null;
  private GraphicElementFactory gef = null;
  private String body = null;
  private String width = null;
  private boolean browserBarDisplayable = true;
  private boolean popup = false;
  String contextualDiv = null;

  /**
   * Constructor declaration
   *
   */
  public AbstractWindow() {
  }

  @Override
  public void init(GraphicElementFactory gef) {
    this.gef = gef;
  }

  public String getBody() {
    return this.body;
  }

  @Override
  public void addBody(String body) {
    this.body = body;
  }

  public GraphicElementFactory getGEF() {
    return this.gef;
  }

  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  @Override
  public void setWidth(String width) {
    this.width = width;
  }

  public String getWidth() {
    if (this.width == null) {
      this.width = "100%";
    }
    return this.width;
  }

  @Override
  public OperationPane getOperationPane() {
    if (this.operationPane == null) {
      this.operationPane = getGEF().getOperationPane();
      if (!isPopup() && StringUtil.isDefined(getGEF().getComponentIdOfCurrentRequest())) {
        if (ResourceLocator.getGeneralSettingBundle()
            .getBoolean("AdminFromComponentEnable", true)) {
          addOperationToSetupComponent();
        }
        addOperationToEditComponentIntro();
      }
    }
    return this.operationPane;
  }

  private void addOperationToSetupComponent() {
    MainSessionController msc = getGEF().getMainSessionController();
    OrganizationController organizationController =
        OrganizationControllerProvider.getOrganisationController();
    String componentId = getGEF().getComponentIdOfCurrentRequest();
    boolean isComponentInstanceIdDefinedShareable = StringUtil.isDefined(componentId);
    if (isComponentInstanceIdDefinedShareable) {
      isComponentInstanceIdDefinedShareable =
          !PersonalComponentInstance.from(componentId).isPresent();
    }
    if (isComponentInstanceIdDefinedShareable && organizationController
        .isComponentManageable(componentId, msc.getUserId()) && getGEF().isComponentMainPage()) {
      String label = ResourceLocator.getGeneralLocalizationBundle(
          getGEF().getMultilang().getLocale().getLanguage())
              .getString("GML.operations.setupComponent");
      String url = "javascript:spWindow.setupComponent('" +  componentId +"\')";
      this.operationPane.addOperation(null, label, url);
      this.operationPane.addLine();
    }
  }

  private void addOperationToSetupSpace() {
    MainSessionController msc = getGEF().getMainSessionController();
    String currentSpaceId = getGEF().getSpaceIdOfCurrentRequest();
    if (msc.getCurrentUserDetail().isAccessAdmin() || ArrayUtil
        .contains(msc.getUserManageableSpaceIds(), currentSpaceId)) {
      String label = ResourceLocator.getGeneralLocalizationBundle(
          getGEF().getMultilang().getLocale().getLanguage())
          .getString("GML.operations.setupSpace");
      String url = "javascript:spWindow.setupSpace('" + currentSpaceId +"\')";
      this.operationPane.addOperation(null, label, url);
      this.operationPane.addLine();
    }
  }

  private void addOperationToEditComponentIntro() {
    String currentComponentId = getGEF().getComponentIdOfCurrentRequest();
    if (!getGEF().isComponentMainPage() || currentComponentId.startsWith("kmelia")) {
      return;
    }
    if (isUserAllowedToEditComponentIntro(currentComponentId)) {
      String label = ResourceLocator.getGeneralLocalizationBundle(
          getGEF().getMultilang().getLocale().getLanguage())
          .getString("GML.operations.editComponentIntro");
      String url = "EditComponentInstanceIntro";
      this.operationPane.addOperation(null, label, url);
      this.operationPane.addLine();
    }
  }

  private boolean isUserAllowedToEditComponentIntro(String componentId) {
    OrganizationController organizationController =
        OrganizationControllerProvider.getOrganisationController();
    MainSessionController msc = getGEF().getMainSessionController();
    String[] profiles = organizationController.getUserProfiles(msc.getUserId(), componentId);
    return ArrayUtil.contains(profiles, SilverpeasRole.ADMIN.getName()) ||
        ArrayUtil.contains(profiles, SilverpeasRole.SUPERVISOR.getName()) ||
        ArrayUtil.contains(profiles, "Administrator") ||
        ArrayUtil.contains(profiles, "Admin");
  }

  @Override
  public BrowseBar getBrowseBar() {
    if (this.browseBar == null) {
      this.browseBar = getGEF().getBrowseBar();
    }
    return this.browseBar;
  }

  public String getContextualDiv() {
    final String componentId = getGEF().getComponentIdOfCurrentRequest();
    final OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    final String spaceIds = Optional.ofNullable(componentId)
        .filter(StringUtil::isDefined)
        .stream()
        .flatMap(i -> oc.getPathToComponent(i).stream())
        .map(s -> s.getId() + " ")
        .collect(Collectors.joining());
    if (!spaceIds.isEmpty()) {
      final ComponentInstLight component = oc.getComponentInstLight(componentId);
      // append all profiles of current user
      final String userId = User.getCurrentRequester().getId();
      final String cssProfiles = Optional.ofNullable(oc.getUserProfiles(userId, componentId))
          .stream()
          .flatMap(Stream::of)
          .map(p -> " profile_" + p)
          .collect(Collectors.joining());
      return "<div class=\"" + spaceIds + component.getName() + " " + componentId + cssProfiles + "\">";
    }
    return null;
  }

  @Override
  public boolean isBrowseBarVisible() {
    return this.browserBarDisplayable;
  }

  @Override
  public void setBrowseBarVisibility(boolean browseBarVisible) {
    this.browserBarDisplayable = browseBarVisible;
  }

  @Override
  public boolean isPopup() {
    return popup;
  }

  @Override
  public void setPopup(boolean popup) {
    this.popup = popup;
  }

  private String getWelcomeMessage(SilverpeasComponentInstance component, String language) {
    String message = null;
    String fileName;
    try {
      fileName = "welcome_" + language;
      message = getSilverpeasTemplate().applyFileTemplateOnComponent(component.getName(), fileName);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .silent(e)
          .info("App ''{0}'' has no welcome message yet !", component.getName());
    }

    if (!StringUtil.isDefined(message)) {
      return null;
    }
    StringBuilder sb = new StringBuilder(100);
    String title = getGEF().getMultilang().getString("GEF.welcome.title");
    sb.append("<div id=\"welcome-message\" title=\"").append(title)
        .append("\" style=\"display: none;\">\n");
    sb.append("<p>\n");
    sb.append(message);
    sb.append("</p>\n");
    sb.append("</div>\n");
    return sb.toString();
  }

  private SilverpeasTemplate getSilverpeasTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents();
  }

  private String getWelcomeMessageScript(SilverpeasComponentInstance component) {
    StringBuilder sb = new StringBuilder(100);
    sb.append("<script type=\"text/javascript\">\n");
    sb.append("var welcomeMessageAlreadyShown = false;\n");
    sb.append("$(function() {\n");
    sb.append("var welcomeMessageCookieName = \"Silverpeas_").append(component.getName())
        .append("_WelcomeMessage\";\n");
    sb
        .append("if (!welcomeMessageAlreadyShown && \"IKnowIt\" != $.cookie(welcomeMessageCookieName)) {\n");
    sb.append("if (!welcomeMessageAlreadyShown) {\n");
    sb.append("welcomeMessageAlreadyShown = true;\n");
    sb.append("$('#welcome-message').dialog({\n");
    sb.append("modal: true,\n");
    sb.append("resizable: false,\n");
    sb.append("width: 400,\n");
    sb.append("dialogClass: 'help-modal-message',\n");
    sb.append("buttons: {\n");
    sb.append("\"").append(getGEF().getMultilang().getString("GEF.welcome.button.ok"))
        .append("\": function() {\n");
    sb.append("$.cookie(welcomeMessageCookieName, \"IKnowIt\", { expires: 3650, path: '/'");
    sb.append(", secure: ").append(HttpRequest.isCurrentRequestSecure()).append(" });\n");
    sb.append("$(this).dialog(\"close\");\n");
    sb.append(" }, \n");
    sb.append("\"").append(getGEF().getMultilang().getString("GEF.welcome.button.reminder"))
        .append("\": function() {\n");
    sb.append(" $(this).dialog(\"close\");\n");
    sb.append(" }\n");
    sb.append("}\n");
    sb.append("});\n");
    sb.append("}\n");
    sb.append("}\n");
    sb.append("}\n");
    sb.append(");\n");
    sb.append("</script>");
    return sb.toString();
  }

  @SuppressWarnings("StatementWithEmptyBody")
  public String displayWelcomeMessage() {
    if (getGEF().isComponentMainPage()) {
      String componentId = getGEF().getComponentIdOfCurrentRequest();
      if (StringUtil.isDefined(componentId)) {
        StringBuilder sb = new StringBuilder(300);
        OrganizationControllerProvider.getOrganisationController().getComponentInstance(componentId)
            .ifPresent(component -> {
              String language = getGEF().getMainSessionController().getFavoriteLanguage();
              String message = getWelcomeMessage(component, language);
              if (message != null) {
                sb.append(message);
                sb.append(getWelcomeMessageScript(component));
              } else {
                // Welcome message is not yet defined for this application
                // So, display nothing at all !
              }
            });
        return sb.toString();
      }
    }
    return "";
  }

  /**
   * Add an operation that permits to
   * <ul>
   * <li>display space or application manager</li>
   * <li>add space or application to user bookmarks</li>
   * </ul>
   */
  protected void addSpaceOrComponentOperations() {
    String currentSpaceId = getGEF().getSpaceIdOfCurrentRequest();
    String currentComponentId = getGEF().getComponentIdOfCurrentRequest();
    boolean isComponentInstanceIdDefinedShareable = StringUtil.isDefined(currentComponentId);
    if (isComponentInstanceIdDefinedShareable) {
      isComponentInstanceIdDefinedShareable =
          !PersonalComponentInstance.from(currentComponentId).isPresent();
    }
    if ((OperationPaneType.space.equals(getOperationPane().getType()) &&
        StringUtil.isDefined(currentSpaceId)) || isComponentInstanceIdDefinedShareable) {
      if (getOperationPane().nbOperations() > 0) {
        getOperationPane().addLine();
      }

      // verify if current component is used as space homepage
      if (getGEF().isCurrentRequestFromSpaceHomepage() && StringUtil.isDefined(currentSpaceId) &&
          StringUtil.isDefined(currentComponentId)) {
        final SpaceHomepageProxy space = SpaceHomepageProxyManager.get()
            .getProxyOf(OrganizationController.get().getSpaceInstById(currentSpaceId));
        if (space.getFirstPageType() == SpaceInst.FP_TYPE_COMPONENT_INST &&
            currentComponentId.equals(space.getFirstPageExtraParam())) {
          getOperationPane().setType(OperationPaneType.space);
        }
      }

      final String viewMgrLabel;
      final String addFavLabel;
      final String viewMgrAction;
      final String addFavAction;
      boolean addFavOperation = true;
      String currentUserId = getGEF().getMainSessionController().getUserId();
      LocalizationBundle bundle = ResourceLocator.getGeneralLocalizationBundle(
          getGEF().getMultilang().getLocale().getLanguage());
      if (OperationPaneType.space.equals(getOperationPane().getType())) {
        viewMgrLabel = bundle.getString("GML.space.responsibles").replaceAll("''", "'");
        viewMgrAction =
            "displaySpaceResponsibles('" + currentUserId + "','" + currentSpaceId + "')";
        addFavLabel = bundle.getString("GML.favorite.space.add");
        addFavAction = "addFavoriteSpace('" + currentSpaceId + "')";

        addOperationToSetupSpace();

      } else {
        viewMgrLabel = bundle.getString("GML.component.responsibles").replaceAll("''", "'");
        viewMgrAction =
            "displayComponentResponsibles('" + currentUserId + "','" + currentComponentId + "')";
        addFavLabel = bundle.getString("GML.favorite.application.add");
        addFavAction = "addFavoriteApp('" + currentComponentId + "')";
        addFavOperation = getGEF().isComponentMainPage();
      }
      if (addFavOperation) {
        getOperationPane().addOperation("", addFavLabel, "javascript:" + addFavAction + ";",
            "space-or-application-favorites-operation");
      }
      getOperationPane().addOperation("", viewMgrLabel, "javascript:" + viewMgrAction + ";",
          "space-or-component-responsibles-operation");
    }
  }

  @Override
  public String printBefore() {
    StringBuilder result = new StringBuilder(200);
    if (!isPopup() && !User.getCurrentRequester().isAnonymous() &&
        !User.getCurrentRequester().isAccessGuest() &&
        !OperationPaneType.personalSpace.equals(getOperationPane().getType())) {
      addSpaceOrComponentOperations();
    }
    String browseBarClass = "cellBrowseBar";
    boolean someOperations = getOperationPane().nbOperations() > 0;
    if (!someOperations) {
      browseBarClass += " cellBrowseBarWithoutOperation";
    }

    contextualDiv = getContextualDiv();
    if (StringUtil.isDefined(contextualDiv)) {
      result.append(contextualDiv);
    }

    result.append("<div id=\"topPage\">");
    if (isBrowseBarVisible()) {
      if (isPopup()) {
        getBrowseBar().setClickable(false);
      }
      Div div = new Div(getBrowseBar().print());
      div.setClass(browseBarClass);
      result.append(div.toString());
      if (someOperations) {
        result.append("<div class=\"cellOperation\" >");
        result.append(getOperationPane().print());
        result.append(DIV);
      }
    }
    result.append("<div class=\"cellBodyWindows\">");
    return result.toString();
  }

  @Override
  public String printAfter() {
    StringBuilder result = new StringBuilder(200);
    String iconsPath = getIconsPath();
    result.append(DIV);

    if (!isPopup()) {
      result.append("<div class=\"sp_goToTop\"><a href=\"#topPage\"><img src=\"").append(iconsPath).append(
          "/goTop.gif\" border=\"0\" alt=\"\"/></a></div>");
    } else {
      SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
      Object includePopupResizeJsDone = cache.get("@includePopupResizeJsDone@");
      if (includePopupResizeJsDone == null) {
        StringBuilder popupResizeJs = new StringBuilder();
        popupResizeJs.append("jQuery(document.body).ready(");
        popupResizeJs.append("function(){");
        popupResizeJs.append("currentPopupResize();");
        popupResizeJs.append("});");
        result.append(new script().setType("text/javascript").addElement(popupResizeJs.toString())
            .toString());
        cache.put("@includePopupResizeJsDone@", true);
      }
    }
    if (StringUtil.isDefined(contextualDiv)) {
      result.append(DIV);
    }

    result.append(displayWelcomeMessage());

    return result.toString();
  }

  @Override
  public String print() {
    StringBuilder result = new StringBuilder(500);
    result.append(printBefore());
    result.append(getBody());
    result.append(printAfter());
    return result.toString();
  }
}
