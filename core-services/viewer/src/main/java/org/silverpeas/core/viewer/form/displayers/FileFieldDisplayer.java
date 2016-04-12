/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.viewer.form.displayers;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.displayers.AbstractFileFieldDisplayer;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.viewer.service.ViewerProvider;

import java.io.File;
import java.io.PrintWriter;

/**
 * A FileFieldDisplayer is an object which can display a link to a file (attachment) in HTML and can
 * retrieve via HTTP any file.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class FileFieldDisplayer extends AbstractFileFieldDisplayer {

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the field type is not a managed type.</li>
   * </ul>
   * @throws FormException
   */
  public void display(PrintWriter out, FileField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String contentLanguage = I18NHelper.checkLanguage(pageContext.getContentLanguage());
    StringBuilder html = new StringBuilder(1024);
    Operation defaultOperation = Operation.ADD;
    String fieldName = Util.getFieldOccurrenceName(template.getFieldName(), field.getOccurrence());

    String attachmentId = field.getAttachmentId();
    String componentId = pageContext.getComponentId();
    String webContext = URLUtil.getApplicationURL();

    SimpleDocument attachment = null;
    if (StringUtil.isDefined(attachmentId)) {
      attachment = AttachmentServiceProvider.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(attachmentId, componentId), contentLanguage);
      if (attachment != null) {
        defaultOperation = Operation.UPDATE;
      }
    } else {
      attachmentId = "";
    }

    if (!template.isHidden()) {
      if (template.isReadOnly()) {
        if (attachment != null) {
          html.append("<img alt=\"\" src=\"")
              .append(attachment.getDisplayIcon())
              .append("\"/>&nbsp;");
          String url = webContext + attachment.getAttachmentURL();
          if (pageContext.isSharingContext()) {
            url = pageContext.getSharingContext().getSharedUriOf(attachment).toString();
          }
          html.append("<a href=\"")
              .append(url)
              .append("\" target=\"_blank\">")
              .append(attachment.getFilename())
              .append("</a>");
          if (!pageContext.isSharingContext()) {
            File attachmentFile = new File(attachment.getAttachmentPath());
            if (ViewerProvider.isPreviewable(attachmentFile)) {
              html.append("<img onclick=\"javascript:previewFormFile(this, '")
                  .append(attachment.getId())
                  .append("');\" class=\"preview-file\" src=\"")
                  .append(webContext)
                  .append("/util/icons/preview.png\" alt=\"")
                  .append(Util.getString("GML.preview", contentLanguage))
                  .append("\" title=\"")
                  .append(Util.getString("GML.preview", contentLanguage))
                  .append("\"/>");
            }
            if (ViewerProvider.isViewable(attachmentFile)) {
              html.append("<img onclick=\"javascript:viewFormFile(this, '")
                  .append(attachment.getId())
                  .append("');\" class=\"view-file\" src=\"")
                  .append(webContext)
                  .append("/util/icons/view.png\" alt=\"")
                  .append(Util.getString("GML.view", contentLanguage))
                  .append("\" title=\"")
                  .append(Util.getString("GML.view", contentLanguage))
                  .append("\"/>");
            }
            if (attachment.isSharingAllowedForRolesFrom(
                UserDetail.getById(pageContext.getUserId()))) {
              html.append("<img onclick=\"javascript:createSharingTicketPopup({ componentId : '")
                  .append(attachment.getInstanceId())
                  .append("',type : 'Attachment', id: '")
                  .append(attachment.getOldSilverpeasId())
                  .append("', name : '")
                  .append(EncodeHelper.javaStringToJsString(attachment.getFilename()))
                  .append("'});\" class=\"share-file\" src=\"")
                  .append(webContext)
                  .append("/util/icons/share.png\" alt=\"")
                  .append(Util.getString("GML.share.file", contentLanguage))
                  .append("\" title=\"")
                  .append(Util.getString("GML.share.file", contentLanguage))
                  .append("\"/>");
            }
          }
        }
      } else if (!template.isDisabled()) {
        html.append("<input type=\"file\" size=\"50\" id=\"")
            .append(fieldName)
            .append("\" name=\"")
            .append(fieldName)
            .append("\"/>")
            .append("<input type=\"hidden\" id=\"")
            .append(fieldName + FileField.PARAM_ID_SUFFIX)
            .append("\" name=\"")
            .append(fieldName + Field.FILE_PARAM_NAME_SUFFIX)
            .append("\" value=\"")
            .append(attachmentId)
            .append("\"/>")
            .append("<input type=\"hidden\" id=\"")
            .append(fieldName)
            .append(OPERATION_KEY + "\" name=\"")
            .append(fieldName)
            .append(OPERATION_KEY + "\" value=\"")
            .append(defaultOperation.name())
            .append("\"/>");

        if (attachment != null) {
          String deleteImg = Util.getIcon("delete");
          String deleteLab = Util.getString("removeFile", contentLanguage);
          html.append("&nbsp;<span id=\"div")
              .append(fieldName)
              .append("\">")
              .append("<img alt=\"\" align=\"top\" src=\"")
              .append(attachment.getDisplayIcon())
              .append("\"/>&nbsp;")
              .append("<a href=\"")
              .append(webContext)
              .append(attachment.getAttachmentURL())
              .append("\" target=\"_blank\">")
              .append(attachment.getFilename())
              .append("</a>")
              .append("&nbsp;<a href=\"#\" onclick=\"javascript:" + "document.getElementById('div")
              .append(fieldName)
              .append("').style.display='none';" + "document.")
              .append(pageContext.getFormName())
              .append(".")
              .append(fieldName)
              .append(OPERATION_KEY + ".value='")
              .append(Operation.DELETION.name())
              .append("';" + "\">")
              .append("<img src=\"")
              .append(deleteImg)
              .append("\" width=\"15\" height=\"15\" border=\"0\" alt=\"")
              .append(deleteLab)
              .append("\" align=\"top\" title=\"")
              .append(deleteLab)
              .append("\"/></a>")
              .append("</span>");
        }
        if (template.isMandatory() && pageContext.useMandatory()) {
          html.append(Util.getMandatorySnippet());
        }
      }
    }

    html.append(displayPreviewJavascript(pageContext));
    html.append(displayViewJavascript(pageContext));

    out.println(html.toString());
  }

  private String displayPreviewJavascript(PagesContext context) {
    StringBuilder sb = new StringBuilder(50);
    sb.append("<script type=\"text/javascript\">\n");
    sb.append("function previewFormFile(target, attachmentId) {\n");
    sb.append("$(target).preview(\"previewAttachment\", {\n");
    sb.append("componentInstanceId: \"").append(context.getComponentId()).append("\",\n");
    sb.append("attachmentId: attachmentId,\n");
    sb.append("lang: '").append(context.getContentLanguage()).append("'\n");
    sb.append("});\n");
    sb.append("return false;");
    sb.append("}\n");
    sb.append("</script>\n");
    return sb.toString();
  }

  private String displayViewJavascript(PagesContext context) {
    StringBuilder sb = new StringBuilder(50);
    sb.append("<script type=\"text/javascript\">\n");
    sb.append("function viewFormFile(target, attachmentId) {\n");
    sb.append("$(target).view(\"viewAttachment\", {\n");
    sb.append("componentInstanceId: \"").append(context.getComponentId()).append("\",\n");
    sb.append("attachmentId: attachmentId,\n");
    sb.append("lang: '").append(context.getContentLanguage()).append("'\n");
    sb.append("});\n");
    sb.append("return false;");
    sb.append("}\n");
    sb.append("</script>\n");
    return sb.toString();
  }

  /**
   * Method declaration
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

}
