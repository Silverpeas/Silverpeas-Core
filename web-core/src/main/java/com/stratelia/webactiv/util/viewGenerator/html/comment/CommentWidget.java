/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.viewGenerator.html.comment;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.personalization.UserPreferences;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.script;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Arrays;

import static com.silverpeas.util.StringUtil.isDefined;

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
  private String userId;
  private String callback;

  /**
   * Gets a javascript function to call when an event occurs on a comment or on a list of comments.
   * @return the callback to invoke.
   */
  public String getCallback() {
    return callback;
  }

  /**
   * Sets the javascript function to call when an event occurs on a comment or on a list of
   * comments.
   * @param callback the callback to invoke.
   */
  public void setCallback(String callback) {
    this.callback = callback;
  }

  /**
   * Sets up the widget with all required information. It initializes the JQuery comment plugin with
   * and it parameterizes from Silverpeas settings and from the resource for which the comments
   * should be rendered.
   * @return a container of rendering elements.
   * @throws JspException if an error occurs while initializing the JQuery comment plugin.
   */
  public ElementContainer initWidget() throws JspException {
    String context = URLManager.getApplicationURL();
    ElementContainer xhtmlcontainer = new ElementContainer();
    div comments = new div();
    comments.setID(COMMENT_WIDGET_DIV_ID);
    comments.setClass(COMMENT_WIDGET_DIV_CLASS);
    script checkForm = new script().setType("text/javascript").
        setSrc(context + "/util/javaScript/checkForm.js");
    script initCommentPlugin = new script().setType("text/javascript").
        addElement(setUpJQueryCommentPlugin());
    script autoresizePlugin = new script().setType("text/javascript").
        setSrc(URLManager.getApplicationURL()
        + "/util/javaScript/jquery/autoresize.jquery.min.js");
    script commentJqueryScript = new script().setType("text/javascript").
        setSrc(URLManager.getApplicationURL() + "/util/javaScript/jquery/jquery-comment.js");

    xhtmlcontainer.addElement(checkForm).
        addElement(autoresizePlugin).
        addElement(commentJqueryScript).
        addElement(comments).
        addElement(initCommentPlugin);
    return xhtmlcontainer;
  }

  /**
   * Gets the unique identifier of the user that requested the page in which will be rendered the
   * widget.
   * @return the unique identifier of the user.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the unique identifier of the user that requested the page in which will be rendered the
   * widget.
   * @param userId the user identifier.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Sets the unique identifier of the Silverpeas component instance to which the commented resource
   * belongs.
   * @param componentId the unique identifier of the instance of a Silverpeas component.
   */
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  /**
   * Sets the unique identifier of the resource that is commented out.
   * @param resourceId the unique identifier of the commented resource.
   */
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * Gets the identifier of the Silverpeas component instance to which the resource belongs.
   * @return the component identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets the unique identifier of the resource in Silverpeas.
   * @return the resource identifier.
   */
  public String getResourceId() {
    return resourceId;
  }

  private UserPreferences getUserPreferences() throws JspTagException {
    return SilverpeasServiceProvider.getPersonalizationService().getUserSettings(getUserId());
  }

  private ResourcesWrapper getSettings() throws JspTagException {
    String language = getUserPreferences().getLanguage();
    ResourceLocator messages = new ResourceLocator(
        "com.stratelia.webactiv.util.comment.multilang.comment", language);
    ResourcesWrapper resources = new ResourcesWrapper(messages,
        new ResourceLocator("com.stratelia.webactiv.util.comment.icons", ""),
        new ResourceLocator("com.stratelia.webactiv.util.comment.Comment", ""), language);

    return resources;

  }

  private String getUpdateIconURL() {
    return URLManager.getApplicationURL() + "/util/icons/update.gif";
  }

  private String getDeletionIconURL() {
    return URLManager.getApplicationURL() + "/util/icons/delete.gif";
  }

  private String getMandatoryFieldSymbolURL() {
    return URLManager.getApplicationURL() + "/util/icons/mandatoryField.gif";
  }

  /**
   * This method generates the Javascript instructions to retrieve in AJAX the comments on the given
   * resource and to display them. The generated code is built upon the JQuery toolkit, so that it
   * is required to be included within the the XHTML header section.
   * @return the javascript code to handle a list of comments on a given resource.
   */
  private String setUpJQueryCommentPlugin() throws JspTagException {
    String context = URLManager.getApplicationURL();

    OrganizationController controller = new OrganizationController();
    ResourcesWrapper settings = getSettings();
    UserDetail currentUser = controller.getUserDetail(getUserId());
    String[] profiles = controller.getUserProfiles(getUserId(), getComponentId());
    boolean isAdmin = false;
    if (Arrays.asList(profiles).contains(SilverpeasRole.admin.name())) {
      isAdmin = true;
    }
    boolean canBeUpdated = settings.getSetting("AdminAllowedToUpdate", true) && isAdmin;

    String script = "$('#commentaires').comment({" + "uri: '" + context + "/services/comments/"
        + getComponentId() + "/" + getResourceId()
        + "', avatar: '" + URLManager.getApplicationURL() + currentUser.getAvatar()
        + "', update: { activated: function( comment ) {"
        + "if (" + canBeUpdated + "|| (comment.author.id === '" + getUserId() + "'))"
        + "return true; else return false;},icon: '" + getUpdateIconURL() + "'," + "altText: '"
        + settings.getString("GML.update") + "'},"
        + "deletion: {activated: function( comment ) {if (" + canBeUpdated
        + " || (comment.author.id === '" + getUserId() + "')) return true; else return false;},"
        + "confirmation: '" + settings.getString("comment.suppressionConfirmation") + "',"
        + "icon: '" + getDeletionIconURL() + "',altText: '" + settings.getString("GML.delete")
        + "'}, updateBox: { title: '" + settings.getString("comment.comment")
        + "'}, editionBox: { title: '" + settings.getString("comment.add") + "', ok: '"
        + settings.getString("GML.validate")
        + "'}, validate: function(text) { if (text == null || $.trim(text).length == 0) { "
        + "alert('"
        + settings.getString("comment.pleaseFill_single") + "');"
        + "} else if (!isValidTextArea(text)) { alert('" + settings.getString(
        "comment.champsTropLong") + "'); } else { return true; } return false; },"
        + "mandatory: '"
        + getMandatoryFieldSymbolURL() + "', mandatoryText: '" + settings.getString(
        "GML.requiredField") + "'";
    if (isDefined(getCallback())) {
      script += ",callback: " + getCallback() + "});";
    } else {
      script += "});";
    }

    return script;
  }
}
