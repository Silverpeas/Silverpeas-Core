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

import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.jsp.JspWriter;

/**
 * @author ehugonnet
 */
public class MenuHelper {

  static String NEW_LINE = System.getProperty("line.separator");

  public static void displayActions(AttachmentDetail attachment, boolean useXMLForm, boolean useFileSharing, boolean useWebDAV, String userId, String language, ResourcesWrapper resources, String httpServerBase, JspWriter out) throws IOException {
    String attachmentId = attachment.getPK().getId();
    boolean webDavOK = useWebDAV && attachment.isOpenOfficeCompatible();
    StringBuilder builder = new StringBuilder();
    builder.append("<div id=\"basicmenu").append(attachmentId).append("\" class=\"yuimenu\">").append(NEW_LINE);
    builder.append("<div class=\"bd\">").append(NEW_LINE);
    builder.append("<ul class=\"first-of-type\">").append(NEW_LINE);
    builder.append("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkout(").append(attachmentId).append(',').append(webDavOK).append(")\">").append(resources.getString("checkOut")).append("</a></li>").append(NEW_LINE);
    builder.append("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkoutAndDownload(").append(attachmentId).append(',').append(webDavOK).append(")\">").append(resources.getString("attachment.checkOutAndDownload")).append("</a></li>").append(NEW_LINE);
    out.print(builder.toString());
    out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkoutAndEdit("
            + attachmentId + ")\">" + resources.getString("attachment.checkOutAndEditOnline") + "</a></li>");
    out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:checkin("
            + attachmentId
            + ","
            + attachment.isOpenOfficeCompatible()
            + ", false)\">"
            + resources.getString("checkIn")
            + "</a></li>");
    out.println("</ul>");
    out.println("<ul>");
    out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:updateAttachment('"
            + attachmentId + "')\">" + resources.getString("GML.modify") + "</a></li>");
    if (useXMLForm) {
      out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:EditXmlForm('"
              + attachmentId
              + "','"
              + language
              + "')\">"
              + resources.getString("attachment.xmlForm.Edit") + "</a></li>");
    }
    out.println(
            "<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:deleteAttachment('"
            + EncodeHelper.javaStringToJsString(attachment.getLogicalName(language))
            + "',"
            + attachmentId
            + ")\">"
            + resources.getString("GML.delete") + "</a></li>");
    out.println("</ul>");
    if (useFileSharing) {
      out.println("<ul>");
      out.println("<li class=\"yuimenuitem\"><a class=\"yuimenuitemlabel\" href=\"javascript:ShareAttachment('"
              + attachmentId + "')\">" + resources.getString("attachment.share") + "</a></li>");
      out.println("</ul>");
    }
    out.println("</div>");
    out.println("</div>");

    out.println("<script type=\"text/javascript\">");

    out.println("var oMenu" + attachmentId + ";");
    out.println("var webDav" + attachmentId + " = \""
            + URLEncoder.encode(httpServerBase + attachment.getWebdavUrl(language), "ISO-8859-1") + "\";");
    out.println("YAHOO.util.Event.onContentReady(\"basicmenu" + attachmentId + "\", function () {");
    out.println(
            "oMenu" + attachmentId + " = new YAHOO.widget.ContextMenu(\"basicmenu" + attachmentId
            + "\", { trigger: \"img_" + attachmentId
            + "\", hidedelay: 100, effect: {effect: YAHOO.widget.ContainerEffect.FADE, duration: 0.30}});");
    out.println("oMenu" + attachmentId + ".render();");
    if (attachment.isReadOnly()) {
      if (userId.equals(attachment.getWorkerId())) {
        out.println("oMenu" + attachmentId + ".getItem(3).cfg.setProperty(\"disabled\", false);");
      }
      out.println("oMenu" + attachmentId + ".getItem(0).cfg.setProperty(\"disabled\", true);");
      out.println("oMenu" + attachmentId + ".getItem(1).cfg.setProperty(\"disabled\", true);");
      out.println("oMenu" + attachmentId + ".getItem(2).cfg.setProperty(\"disabled\", true);");

      if (useWebDAV && attachment.isOpenOfficeCompatible()
              && userId.equals(attachment.getWorkerId())) {
        out.println("oMenu" + attachmentId + ".getItem(2).cfg.setProperty(\"disabled\", false);");
      }

      // disable delete
      if (useXMLForm) {
        out.println("oMenu" + attachmentId + ".getItem(2,1).cfg.setProperty(\"disabled\", true);");
      } else {
        out.println("oMenu" + attachmentId + ".getItem(1,1).cfg.setProperty(\"disabled\", true);");
      }

      if (!userId.equals(attachment.getWorkerId())) {
        // disable update
        out.println("oMenu" + attachmentId + ".getItem(0, 1).cfg.setProperty(\"disabled\", true);");

        // disable xmlForm
        if (useXMLForm) {
          out.println("oMenu" + attachmentId
                  + ".getItem(1,1).cfg.setProperty(\"disabled\", true);");
        }
      }
    } else {
      out.println("oMenu" + attachmentId + ".getItem(3).cfg.setProperty(\"disabled\", true);");
    }
    if (!useWebDAV || !attachment.isOpenOfficeCompatible()) {
      out.println("oMenu" + attachmentId + ".getItem(2).cfg.setProperty(\"disabled\", true);");
    }
    out.println(
            "YAHOO.util.Event.addListener(\"basicmenu" + attachmentId + "\", \"mouseover\", oMenu"
            + attachmentId + ".show);");
    out.println(
            "YAHOO.util.Event.addListener(\"basicmenu" + attachmentId + "\", \"mouseout\", oMenu"
            + attachmentId + ".hide);");
    out.println("});");
    out.println("</script>");
  }
}
