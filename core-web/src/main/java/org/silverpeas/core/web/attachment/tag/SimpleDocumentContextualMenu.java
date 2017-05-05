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
package org.silverpeas.core.web.attachment.tag;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.lang3.StringEscapeUtils;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;
import static org.silverpeas.core.util.StringUtil.newline;

/**
 * @author ehugonnet
 */
public class SimpleDocumentContextualMenu extends TagSupport {

  private SimpleDocument attachment;
  private boolean useXMLForm;
  private boolean useWebDAV;
  private boolean showMenuNotif;

  private static final String TEMPLATE = "oMenu%s.getItem(%s).cfg.setProperty(\"disabled\", %s);";

  private static final String MENU_ITEM_TEMPLATE = "<li class=\"yuimenuitem\">"
      + "<a class=\"yuimenuitemlabel\" href=\"javascript:%1$s\">%2$s</a></li>%n";
  private static final long serialVersionUID = 1L;

  public void setAttachment(SimpleDocument attachment) {
    this.attachment = attachment;
  }

  public void setUseXMLForm(boolean useXMLForm) {
    this.useXMLForm = useXMLForm;
  }

  public void setUseWebDAV(boolean useWebDAV) {
    this.useWebDAV = useWebDAV;
  }

  public void setShowMenuNotif(boolean showMenuNotif) {
    this.showMenuNotif = showMenuNotif;
  }

  @Override
  public int doStartTag() throws JspException {
    try {
      MainSessionController mainSessionController = (MainSessionController) pageContext.getSession()
          .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      String favoriteLanguage = mainSessionController.getFavoriteLanguage();
      LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.util.attachment.multilang.attachment", favoriteLanguage);
      UserDetail user = mainSessionController.getCurrentUserDetail();
      if (attachment.canBeModifiedBy(user)) {
        pageContext.getOut().print(
            prepareActions(attachment, useXMLForm, useWebDAV, user,
                favoriteLanguage, messages, showMenuNotif));
      } else {
        pageContext.getOut()
            .print(prepareReadOnlyActions(attachment, user, messages, showMenuNotif));
      }
      return EVAL_BODY_INCLUDE;
    } catch (IOException ioex) {
      throw new JspException(ioex);
    }
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  boolean isAdmin(User user) {
    return user.isAccessAdmin();
  }

  boolean isWorker(String userId, SimpleDocument attachment) {
    return userId.equals(attachment.getEditedBy());
  }

  boolean isEditable(String userId, SimpleDocument attachment, boolean useWebDAV) {
    return useWebDAV && attachment.isOpenOfficeCompatible() && isWorker(userId, attachment) &&
        StringUtil.defaultStringIfNotDefined(attachment.getWebdavContentEditionLanguage(),
            attachment.getLanguage()).equals(attachment.getLanguage());
  }

  String prepareActions(SimpleDocument attachment, boolean useXMLForm, boolean useWebDAV,
      UserDetail user, final String userLanguage, LocalizationBundle resources,
      boolean showMenuNotif) throws UnsupportedEncodingException {
    String userId = user.getId();
    String attachmentId = String.valueOf(attachment.getOldSilverpeasId());
    boolean webDavOK = useWebDAV && attachment.isOpenOfficeCompatible();
    StringBuilder builder = new StringBuilder(2048);

    builder.append("<ul class=\"first-of-type\">").append(newline);
    prepareMenuItem(builder, "checkout('" + attachment.getId() + "'," + attachmentId + ','
        + webDavOK + ");", resources.getString("checkOut"));
    prepareMenuItem(builder, "checkoutAndDownload('" + attachment.getId() + "'," + attachmentId
        + ',' + webDavOK + ");", resources.getString("attachment.checkOutAndDownload"));
    String checkoutAndEditLabel = resources.getString("attachment.checkOutAndEditOnline");
    String webdavContentEditionLanguageLabel = "";
    if (I18NHelper.isI18nContentEnabled()) {
      webdavContentEditionLanguageLabel = I18NHelper.getLanguageLabel(StringUtil
          .defaultStringIfNotDefined(attachment.getWebdavContentEditionLanguage(),
              attachment.getLanguage()), userLanguage);
      checkoutAndEditLabel += " (" + webdavContentEditionLanguageLabel + ")";
    }
    prepareMenuItem(builder, "checkoutAndEdit('" + attachment.getId() + "'," + attachmentId + ",'" +
        StringUtil.defaultStringIfNotDefined(attachment.getWebdavContentEditionLanguage(),
            attachment.getLanguage()) + "');", checkoutAndEditLabel);
    prepareMenuItem(builder, "checkin('" + attachment.getId() + "'," + attachmentId + "," +
        attachment.isOpenOfficeCompatible() + ", false, " + attachment.isVersioned() + ", '" +
        webdavContentEditionLanguageLabel + "');", resources.getString("checkIn"));
    builder.append("</ul>").append(newline);
    builder.append("<ul>").append(newline);
    prepareMenuItem(builder,
        "updateAttachment('" + attachment.getId() + "','" + attachment.getLanguage() + "');",
        resources.getString("GML.modify"));
    prepareMenuItem(builder,
        "EditXmlForm('" + attachment.getId() + "','" + attachment.getLanguage() + "');",
        resources.getString("attachment.xmlForm.Edit"));
    String message = resources.getString("attachment.switchState.toVersioned");
    if (attachment.isVersioned()) {
      message = resources.getString("attachment.switchState.toSimple");
    }
    final boolean isLastPublicVersion = attachment.getLastPublicVersion() != null;
    prepareMenuItem(builder,
        "switchState('" + attachment.getId() + "', " + attachment.isVersioned() + ", " +
            isLastPublicVersion + ");", message);
    prepareMenuItem(builder, "deleteAttachment('" + attachment.getId() + "','" + StringEscapeUtils
        .escapeEcmaScript(attachment.getFilename()) + "');", resources.getString("GML.delete"));
    message = resources.getString("attachment.download.allowReaders");
    boolean isDownloadAllowedForReaders = attachment.isDownloadAllowedForReaders();
    if (isDownloadAllowedForReaders) {
      message = resources.getString("attachment.download.forbidReaders");
    }
    prepareMenuItem(builder, "switchDownloadAllowedForReaders('" + attachment.getId() + "', " +
        !isDownloadAllowedForReaders + ");", message);
    builder.append("</ul>").append(newline);
    builder.append("<ul>").append(newline);
    prepareMenuItem(builder, "ShareAttachment('" + attachmentId + "');", resources.getString(
        "GML.share.file"));

    builder.append("</ul>").append(newline);
    builder.append("<ul>").append(newline);
    prepareMenuItem(builder, "notifyAttachment('" + attachmentId + "');", resources.getString(
        "GML.notify"));
    builder.append("</ul>").append(newline);

    String menuItems = builder.toString();

    builder = new StringBuilder();
    if (attachment.isReadOnly()) {
      configureCheckout(builder, attachmentId, true);
      builder.append(configureCheckoutAndDownload(attachmentId, !isWorker(userId, attachment)));
      builder.append(configureCheckoutAndEdit(attachmentId,
          !isEditable(userId, attachment, useWebDAV)));
      builder.append(configureCheckin(attachmentId,
          !isWorker(userId, attachment) && !isAdmin(user)));
      builder.append(configureUpdate(attachmentId, !isWorker(userId, attachment)));
      builder.append(configureDelete(attachmentId, true));
      builder.append(configureForbidDownloadForReaders(attachmentId, true));
      if (!userId.equals(attachment.getEditedBy())) {
        builder.append(configureXmlForm(attachmentId, true));
      }
    } else {
      builder.append(configureXmlForm(attachmentId, !useXMLForm));
      builder.append(configureCheckin(attachmentId, true));
      builder.append(configureCheckoutAndEdit(attachmentId, !useWebDAV || !attachment.
          isOpenOfficeCompatible()));
    }
    builder.append(configureFileSharing(attachmentId, !attachment.isSharingAllowedForRolesFrom(user)));
    builder.append(configureSwitchState(attachmentId, (!attachment.isVersioned() &&
        isComponentPublicationAlwaysVisible(attachment.getInstanceId())) ||
        attachment.isReadOnly()));
    builder.append(configureNotify(attachmentId, !showMenuNotif));

    String itemsConfig = builder.toString();

    return getMenu(attachmentId, menuItems, itemsConfig);
  }

  String prepareReadOnlyActions(SimpleDocument attachment, UserDetail user,
      LocalizationBundle resources, boolean showMenuNotif) throws UnsupportedEncodingException {
    String attachmentId = String.valueOf(attachment.getOldSilverpeasId());
    StringBuilder itemsBuilder = new StringBuilder(2048);
    itemsBuilder.append("<ul>").append(newline);

    boolean sharingAllowed = attachment.isSharingAllowedForRolesFrom(user);

    if (sharingAllowed) {
      prepareMenuItem(itemsBuilder, "ShareAttachment('" + attachmentId + "');",
          resources.getString("GML.share.file"));
    }
    prepareMenuItem(itemsBuilder, "notifyAttachment('" + attachmentId + "');", resources.getString(
        "GML.notify"));
    itemsBuilder.append("</ul>").append(newline);

    StringBuilder configBuilder = new StringBuilder();
    configBuilder.append(String.format(TEMPLATE, attachmentId, "0", !showMenuNotif));
    if (sharingAllowed) {
      configBuilder.append(String.format(TEMPLATE, attachmentId, "1", false));
    }

    return getMenu(attachmentId, itemsBuilder.toString(), configBuilder.toString());
  }

  private String getMenu(String attachmentId, String items, String config) {
    StringBuilder builder = new StringBuilder(2048);
    String oMenuId = "oMenu" + attachmentId;
    String basicMenuId = "basicmenu" + attachmentId;
    builder.append("<div id=\"").append(basicMenuId).append("\" class=\"yuimenu\">").
        append(newline);
    builder.append("<div class=\"bd\">").append(newline);

    // adding menu items
    builder.append(items);

    builder.append("</div>").append(newline);
    builder.append("</div>").append(newline);
    builder.append("<script type=\"text/javascript\">");
    builder.append("var ").append(oMenuId).append(";");
    builder.append("YAHOO.util.Event.onContentReady(\"").append(basicMenuId).append(
        "\", function () {");
    builder.append(oMenuId).append(" = new YAHOO.widget.Menu(\"").append(basicMenuId).
        append("\"").append(", {");
    builder.append("hidedelay: 100, ");
    builder.append("effect: {effect: YAHOO.widget.ContainerEffect.FADE, duration: 0.30}});");
    builder.append(oMenuId).append(".render();");

    // adding configuration of menu items
    builder.append(config);

    builder.append("YAHOO.util.Event.addListener(\"").append(basicMenuId);
    builder.append("\", \"mouseover\", ").append(oMenuId).append(".show);");
    builder.append("YAHOO.util.Event.addListener(\"").append(basicMenuId);
    builder.append("\", \"mouseout\", ").append(oMenuId).append(".hide);");

    builder.append("YAHOO.util.Event.on(\"edit_").append(attachmentId);
    builder.append("\", \"click\", function (event) {");
    builder.append("var xy = YAHOO.util.Event.getXY(event);");
    builder.append(oMenuId).append(".cfg.setProperty(\"x\", xy[0]);");
    builder.append(oMenuId).append(".cfg.setProperty(\"y\", xy[1]+10);");
    builder.append(oMenuId).append(".show();");
    builder.append("  })");

    builder.append("});");
    builder.append("</script>");

    return builder.toString();
  }

  /**
   * Indicates if the publication are always visible for the component instance represented by the
   * given identifier.
   * @param componentInstanceId the component instance identifier that must be verified.
   * @return true if publication are always visible.
   */
  public boolean isComponentPublicationAlwaysVisible(String componentInstanceId) {
    return StringUtil.getBooleanValue(
        getAdminService().getComponentParameterValue(componentInstanceId,
            "publicationAlwaysVisible"));
  }

  StringBuilder prepareMenuItem(StringBuilder buffer, String javascript, String label) {
    return buffer.append(String.format(MENU_ITEM_TEMPLATE, javascript, label));
  }

  StringBuilder configureCheckout(StringBuilder buffer, String attachmentId, boolean disable) {
    return buffer.append(String.format(TEMPLATE, attachmentId, "0", disable));
  }

  String configureCheckoutAndDownload(String attachmentId, boolean disable) {
    return String.format(TEMPLATE, attachmentId, "1", disable);
  }

  String configureCheckoutAndEdit(String attachmentId, boolean disable) {
    return String.format(TEMPLATE, attachmentId, "2", disable);
  }

  String configureCheckin(String attachmentId, boolean disable) {
    return String.format(TEMPLATE, attachmentId, "3", disable);
  }

  String configureUpdate(String attachmentId, boolean disable) {
    return String.format(TEMPLATE, attachmentId, "0, 1", disable);
  }

  String configureDelete(String attachmentId, boolean disable) {
    return String.format(TEMPLATE, attachmentId, "3, 1", disable);
  }

  String configureForbidDownloadForReaders(String attachmentId, boolean disable) {
    return String.format(TEMPLATE, attachmentId, "4, 1", disable);
  }

  String configureXmlForm(String attachmentId, boolean disable) {
    return String.format(TEMPLATE, attachmentId, "1, 1", disable);
  }

  String configureFileSharing(String attachmentId, boolean disable) {
    return String.format(TEMPLATE, attachmentId, "0, 2", disable);
  }

  String configureSwitchState(String attachmentId, boolean disable) {
    return String.format(TEMPLATE, attachmentId, "2, 1", disable);
  }

  String configureNotify(String attachmentId, boolean disable) {
    return String.format(TEMPLATE, attachmentId, "0, 3", disable);
  }
}