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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.attachment;

import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.jsp.JspWriter;

/**
 * @author ehugonnet
 */
public class MenuHelper {

  static final String NEW_LINE = System.getProperty("line.separator");
  private final static String template = "oMenu%s.getItem(%s).cfg.setProperty(\"disabled\", %s);";
  private static OrganizationController organisation = new OrganizationController();

  public static boolean isAdmin(String userId) {
    return organisation.getUserDetail(userId).isAccessAdmin();
  }

  public static boolean isWorker(String userId, AttachmentDetail attachment) {
    return userId.equals(attachment.getWorkerId());
  }

  public static boolean isEditable(String userId, AttachmentDetail attachment, boolean useWebDAV) {
    return useWebDAV && attachment.isOpenOfficeCompatible() && isWorker(userId, attachment);
  }

  public static void displayActions(AttachmentDetail attachment, boolean useXMLForm,
      boolean useFileSharing, boolean useWebDAV, String userId, String language,
      ResourcesWrapper resources, String httpServerBase, boolean showMenuNotif, JspWriter out)
      throws IOException {

    String attachmentId = attachment.getPK().getId();
    boolean webDavOK = useWebDAV && attachment.isOpenOfficeCompatible();
    StringBuilder builder = new StringBuilder(1024);
    builder.append("<div id=\"basicmenu").append(attachmentId).append("\" class=\"yuimenu\">").
        append(NEW_LINE);
    builder.append("<div class=\"bd\">").append(NEW_LINE);
    builder.append("<ul class=\"first-of-type\">").append(NEW_LINE);
    builder.append(
        "<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkout(").
        append(attachmentId).append(',').append(webDavOK).append(")\">").append(
        resources.getString("checkOut")).append("</a></li>").append(NEW_LINE);
    builder.append(
        "<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkoutAndDownload(").
        append(attachmentId).append(',').append(webDavOK).append(")\">").append(
        resources.getString("attachment.checkOutAndDownload")).append("</a></li>").append(
        NEW_LINE);
    builder.append(
        "<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkoutAndEdit(").
        append(attachmentId).append(")\">").
        append(resources.getString("attachment.checkOutAndEditOnline")).append("</a></li>");
    builder.append("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkin(").
        append(attachmentId).append(",").append(attachment.isOpenOfficeCompatible()).
        append(", false)\">").append(resources.getString("checkIn")).append("</a></li>");
    builder.append("</ul>");
    builder.append("<ul>");
    builder.append("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:updateAttachment('").
        append(attachmentId).append("')\">").append(resources.getString("GML.modify")).append("</a></li>");
    if (useXMLForm) {
      builder.append("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:EditXmlForm('"
          + attachmentId
          + "','"
          + language
          + "')\">"
          + resources.getString("attachment.xmlForm.Edit") + "</a></li>");
    }
    builder.append("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:deleteAttachment(").
        append(attachmentId).append(")\">").append(resources.getString("GML.delete")).append("</a></li>");
    builder.append("</ul>");
      builder.append("<ul>");
      builder.append(
          "<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:ShareAttachment('").
          append(attachmentId).append("')\">").append(resources.getString("attachment.share")).
          append("</a></li>");
      builder.append("</ul>");
      builder.append("<ul>");
      builder.append("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:notifyAttachment('"
          + attachmentId + "')\">" + resources.getString("GML.notify") + "</a></li>");
      builder.append("</ul>");
    builder.append("</div>");
    builder.append("</div>");

    builder.append("<script type=\"text/javascript\">");

    builder.append("var oMenu").append(attachmentId).append(";");
    builder.append("var webDav" + attachmentId + " = \""
        + URLEncoder.encode(httpServerBase + attachment.getWebdavUrl(language), "UTF-8")
        + "\";");
    builder.append("YAHOO.util.Event.onContentReady(\"basicmenu").append(attachmentId).append(
        "\", function () {");
    builder.append(
        "oMenu" + attachmentId + " = new YAHOO.widget.ContextMenu(\"basicmenu" + attachmentId
        + "\", { trigger: \"img_" + attachmentId
        + "\", hidedelay: 100, effect: {effect: YAHOO.widget.ContainerEffect.FADE, duration: 0.30}});");
    builder.append("oMenu" + attachmentId + ".render();");
    if (attachment.isReadOnly()) {
      builder.append(configureCheckout(attachmentId, true));
      builder.append(configureCheckoutAndDownload(attachmentId, !isWorker(userId, attachment)));
      builder.append(configureCheckoutAndEdit(attachmentId, !isEditable(userId, attachment,
          useWebDAV)));
      builder.append(configureCheckin(attachmentId,
          !isWorker(userId, attachment) && !isAdmin(userId)));
      builder.append(configureUpdate(attachmentId, !isWorker(userId, attachment)));
      builder.append(configureDelete(attachmentId, useXMLForm, true));      
      if (!userId.equals(attachment.getWorkerId())) {
        // disable xmlForm
        if (useXMLForm) {
          builder.append(configureXmlForm(attachmentId, true));
        }
      }
    } else {
      builder.append(configureCheckin(attachmentId, true));
      builder.append(configureCheckoutAndEdit(attachmentId, !useWebDAV || !attachment.
          isOpenOfficeCompatible()));     
    }
    builder.append(configureFileSharing(attachmentId, !useFileSharing));
    builder.append(configureNotify(attachmentId, !showMenuNotif));
    builder.append("YAHOO.util.Event.addListener(\"basicmenu" + attachmentId + "\", \"mouseover\", oMenu"
        + attachmentId + ".show);");
    builder.append("YAHOO.util.Event.addListener(\"basicmenu" + attachmentId + "\", \"mouseout\", oMenu"
        + attachmentId + ".hide);");
    builder.append("});");
    builder.append("</script>");
    out.print(builder.toString());
  }

  public static String configureCheckout(String attachmentId, boolean disable) {
    return String.format(template, attachmentId, "0", disable);
  }

  public static String configureCheckoutAndDownload(String attachmentId, boolean disable) {
    return String.format(template, attachmentId, "1", disable);
  }

  public static String configureCheckoutAndEdit(String attachmentId, boolean disable) {
    return String.format(template, attachmentId, "2", disable);
  }

  public static String configureCheckin(String attachmentId, boolean disable) {
    return String.format(template, attachmentId, "3", disable);
  }

  public static String configureUpdate(String attachmentId, boolean disable) {
    return String.format(template, attachmentId, "0, 1", disable);
  }

  public static String configureDelete(String attachmentId, boolean useXmlForm, boolean disable) {
    if (useXmlForm) {
      return String.format(template, attachmentId, "2, 1", disable);
    }
    return String.format(template, attachmentId, "1, 1", disable);
  }
  
   public static String configureXmlForm(String attachmentId, boolean disable) {
    return String.format(template, attachmentId, "1, 1", disable);
  }
   
   public static String configureFileSharing(String attachmentId, boolean disable) {
    return String.format(template, attachmentId, "0, 2", disable);
  }

  public static String configureNotify(String attachmentId, boolean disable) {
    return String.format(template, attachmentId, "0, 3", disable);
  }
}
