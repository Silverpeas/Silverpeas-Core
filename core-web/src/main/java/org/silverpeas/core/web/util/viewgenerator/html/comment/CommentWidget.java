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
package org.silverpeas.core.web.util.viewgenerator.html.comment;

import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion;
import org.silverpeas.core.util.MultiSilverpeasBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Arrays;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * It defines the base class of a widget for the rendering and handling of comments in Silverpeas.
 */
public abstract class CommentWidget extends TagSupport {

  private static final long serialVersionUID = 441573421690625472L;
  /**
   * The identifier of the XHTML div tag within which the comment widgets are displayed.
   */
  public static final String COMMENT_WIDGET_DIV_ID = "commentaires";
  /**
   * The CSS class name of the XHTML div tag within which the comment widgets are displayed.
   */
  public static final String COMMENT_WIDGET_DIV_CLASS = "commentaires";
  private String componentId;
  private String resourceId;
  private String resourceType;
  private String userId;
  private String callback;

  /**
   * Gets a javascript function to call when an event occurs on a comment or on a list of comments.
   *
   * @return the callback to invoke.
   */
  public String getCallback() {
    return callback;
  }

  /**
   * Sets the javascript function to call when an event occurs on a comment or on a list of
   * comments.
   *
   * @param callback the callback to invoke.
   */
  public void setCallback(String callback) {
    this.callback = callback;
  }

  /**
   * Sets up the widget with all required information. It initializes the JQuery comment plugin with
   * and it parameterizes from Silverpeas settings and from the resource for which the comments
   * should be rendered.
   *
   * @return a container of rendering elements.
   * @throws JspException if an error occurs while initializing the JQuery comment plugin.
   */
  public ElementContainer initWidget() throws JspException {
    String context = URLUtil.getApplicationURL();
    ElementContainer xhtmlcontainer = new ElementContainer();
    div comments = new div();
    comments.setID(COMMENT_WIDGET_DIV_ID);
    comments.setClass(COMMENT_WIDGET_DIV_CLASS);
    Element checkForm = JavascriptPluginInclusion.script(context + "/util/javaScript/checkForm.js");
    Element initCommentPlugin = new script().setType("text/javascript").
        addElement(setUpJQueryCommentPlugin());

    xhtmlcontainer.addElement(comments).addElement(checkForm);
    JavascriptPluginInclusion.includeComment(xhtmlcontainer);
    xhtmlcontainer.addElement(initCommentPlugin);
    return xhtmlcontainer;
  }

  /**
   * Gets the unique identifier of the user that requested the page in which will be rendered the
   * widget.
   *
   * @return the unique identifier of the user.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the unique identifier of the user that requested the page in which will be rendered the
   * widget.
   *
   * @param userId the user identifier.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Sets the unique identifier of the Silverpeas component instance to which the commented resource
   * belongs.
   *
   * @param componentId the unique identifier of the instance of a Silverpeas component.
   */
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  /**
   * Sets the unique identifier of the resource that is commented out.
   *
   * @param resourceId the unique identifier of the commented resource.
   */
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * Sets the type of the resource that is commented out.
   *
   * @param resourceType the type of the commented resource.
   */
  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  /**
   * Gets the identifier of the Silverpeas component instance to which the resource belongs.
   *
   * @return the component identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets the unique identifier of the resource in Silverpeas.
   *
   * @return the resource identifier.
   */
  public String getResourceId() {
    return resourceId;
  }

  /**
   * Gets the type of the commented resource.
   *
   * @return
   */
  public String getResourceType() {
    return resourceType;
  }

  private UserPreferences getUserPreferences() {
    return PersonalizationServiceProvider.getPersonalizationService().getUserSettings(getUserId());
  }

  private MultiSilverpeasBundle getResources() {
    String language = getUserPreferences().getLanguage();
    MultiSilverpeasBundle resources = new MultiSilverpeasBundle(
        ResourceLocator.getLocalizationBundle("org.silverpeas.util.comment.multilang.comment",
            language),
        ResourceLocator.getSettingBundle("org.silverpeas.util.comment.icons"),
        ResourceLocator.getSettingBundle("org.silverpeas.util.comment.Comment"), language);

    return resources;

  }

  private String getUpdateIconURL() {
    return URLUtil.getApplicationURL() + "/util/icons/update.gif";
  }

  private String getDeletionIconURL() {
    return URLUtil.getApplicationURL() + "/util/icons/delete.gif";
  }

  private String getMandatoryFieldSymbolURL() {
    return URLUtil.getApplicationURL() + "/util/icons/mandatoryField.gif";
  }

  /**
   * This method generates the Javascript instructions to retrieve in AJAX the comments on the given
   * resource and to display them. The generated code is built upon the JQuery toolkit, so that it
   * is required to be included within the the XHTML header section.
   *
   * @return the javascript code to handle a list of comments on a given resource.
   */
  private String setUpJQueryCommentPlugin() {
    String context = URLUtil.getApplicationURL();

    OrganizationController controller = OrganizationControllerProvider.getOrganisationController();
    MultiSilverpeasBundle resources = getResources();
    UserDetail currentUser = controller.getUserDetail(getUserId());
    String[] profiles = controller.getUserProfiles(getUserId(), getComponentId());
    final boolean isAdmin;
    if (profiles != null) {
      isAdmin = Arrays.asList(profiles).contains(SilverpeasRole.admin.name());
    } else {
      isAdmin = currentUser.isAccessAdmin();
    }
    boolean canBeUpdated = resources.getSetting("AdminAllowedToUpdate", true) && isAdmin;

    String script = "$('#commentaires').comment({" + "uri: '" + context + "/services/comments/"
        + getComponentId() + "/" + getResourceType() + "/" + getResourceId()
        + "', author: { avatar: '" + URLUtil.getApplicationURL() + currentUser.getSmallAvatar()
        + "', id: '" + getUserId() + "', anonymous: " + currentUser.isAnonymous() + "}, "
        + "update: { activated: function( comment ) {"
        + "if (" + canBeUpdated + "|| (comment.author.id === '" + getUserId() + "'))"
        + "return true; else return false;},icon: '" + getUpdateIconURL() + "'," + "altText: '"
        + resources.getString("GML.update") + "'},"
        + "deletion: {activated: function( comment ) {if (" + canBeUpdated
        + " || (comment.author.id === '" + getUserId() + "')) return true; else return false;},"
        + "confirmation: '" + resources.getString("comment.suppressionConfirmation") + "',"
        + "icon: '" + getDeletionIconURL() + "',altText: '" + resources.getString("GML.delete")
        + "'}, updateBox: { title: '" + resources.getString("comment.comment")
        + "'}, editionBox: { title: '" + resources.getString("comment.add") + "', ok: '"
        + resources.getString("GML.validate")
        + "'}, validate: function(text) { if (text == null || $.trim(text).length == 0) { "
        + "alert('"
        + resources.getString("comment.pleaseFill_single") + "');"
        + "} else if (!isValidTextArea(text)) { alert('" + resources.getString(
            "comment.champsTropLong") + "'); } else { return true; } return false; },"
        + "mandatory: '"
        + getMandatoryFieldSymbolURL() + "', mandatoryText: '" + resources.getString(
            "GML.requiredField") + "'";
    if (isDefined(getCallback())) {
      script += ",callback: " + getCallback() + "});";
    } else {
      script += "});";
    }

    return script;
  }
}
