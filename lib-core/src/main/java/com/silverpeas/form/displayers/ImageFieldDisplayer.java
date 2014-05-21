/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.form.displayers;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.GalleryHelper;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RenderingContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.util.ImageUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.FileServerUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.wysiwyg.control.WysiwygController;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * A ImageFieldDisplayer is an object which can display an image in HTML and can retrieve via HTTP
 * any file.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class ImageFieldDisplayer extends AbstractFileFieldDisplayer {

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL> <LI>the field type is not a managed type. </UL>
   *
   * @param out
   * @param field
   * @param template
   * @param pageContext
   * @throws FormException
   */
  @Override
  public void display(PrintWriter out, FileField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    SilverTrace.info("form", "ImageFieldDisplayer.display", "root.MSG_GEN_ENTER_METHOD",
        "fieldName = " + template.getFieldName() + ", value = " + field.getAttachmentId()
        + ", fieldType = " + field.getTypeName());
    String fieldName = Util.getFieldOccurrenceName(template.getFieldName(), field.getOccurrence());
    String language = pageContext.getLanguage();
    Operation originalOperation = Operation.ADD;
    String componentId = pageContext.getComponentId();
    String attachmentId = field.getAttachmentId();
    SimpleDocumentPK attachmentPk;
    if (StringUtil.isLong(attachmentId)) {
      attachmentPk = new SimpleDocumentPK(null, componentId);
      attachmentPk.setOldSilverpeasId(Long.parseLong(attachmentId));
    } else {
      attachmentPk = new SimpleDocumentPK(attachmentId, componentId);
    }
    SimpleDocument attachment = null;
    String imageURL = null;
    if (StringUtil.isDefined(attachmentId)) {
      if (attachmentId.startsWith("/")) {
        imageURL = attachmentId;
        originalOperation = Operation.UPDATE;
      } else {
        attachment = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
            attachmentPk, language);
        if (attachment != null) {
          originalOperation = Operation.UPDATE;
          if (pageContext.getRenderingContext() == RenderingContext.EXPORT) {
            imageURL = "file:" + attachment.getAttachmentPath();
          } else {
            imageURL = URLManager.getApplicationURL() + attachment.getAttachmentURL();
          }
        }
      }
    } else {
      attachmentId = "";
    }
    Map<String, String> parameters = template.getParameters(language);
    if (template.isReadOnly() && !template.isHidden()) {
      if (imageURL != null) {
        displayImage(parameters, attachment, imageURL, out);
      }
    } else if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly()) {

      String displayCSS = "display:none";
      if (imageURL != null) {
        displayCSS = "display:block";
      }

      String deleteImg = Util.getIcon("delete");
      String deleteLab = Util.getString("removeImage", language);

      out.println("<div id=\"" + fieldName + "ThumbnailArea\" style=\"" + displayCSS + "\">");
      out.println("<a id=\"" + fieldName + "ThumbnailLink\" href=\"" + imageURL
          + "\" target=\"_blank\">");
      out.println("<img alt=\"\" align=\"top\" src=\"" + imageURL
          + "\" height=\"50\" id=\"" + fieldName + "Thumbnail\"/>&nbsp;");
      out.println("</a>");
      out.println("&nbsp;<a href=\"#\" onclick=\"javascript:"
          + "document.getElementById('" + fieldName + "ThumbnailArea').style.display='none';"
          + "document." + pageContext.getFormName() + "." + fieldName
          + OPERATION_KEY + ".value='"+Operation.DELETION.name()+"';"
          + "\">");
      out.println("<img src=\""
          + deleteImg
          + "\" width=\"15\" height=\"15\" alt=\""
          + deleteLab + "\" align=\"top\" title=\""
          + deleteLab + "\"/></a>");
      out.println("</div>");

      out.println("<div id=\"" + fieldName + "SelectionArea\">");
      out.print("<input type=\"file\" size=\"50\" id=\"");
      out.print(fieldName);
      out.print("\" name=\"");
      out.print(fieldName);
      out.println("\"/>");
      out.println("<input type=\"hidden\" name=\"" + fieldName + Field.FILE_PARAM_NAME_SUFFIX +
          "\" id=\"" + fieldName + FileField.PARAM_ID_SUFFIX + "\" value=\"" + attachmentId +
          "\"/>");
      out.println("<input type=\"hidden\" id=\"" + fieldName + OPERATION_KEY + "\" name=\"" +
          fieldName + OPERATION_KEY + "\" value=\"" + originalOperation.name() + "\"/>");

      // Adding "Galleries" listbox if needed
      boolean useGalleries = Util.getBooleanValue(parameters, "galleries");
      if (useGalleries) {
        renderGalleries(originalOperation, fieldName, language, out);
      }

      if (template.isMandatory() && pageContext.useMandatory()) {
        out.println(Util.getMandatorySnippet());
      }

      out.println("</div>");
    }
  }
  
  private void displayImage(Map<String, String> parameters, SimpleDocument attachment, String imageURL, PrintWriter out) {
    String height = (parameters.containsKey("height") ? parameters.get("height") : "");
    String width = (parameters.containsKey("width") ? parameters.get("width") : "");
    String paramHeight = "";
    String paramWidth = "";
    if (StringUtil.isDefined(width) && StringUtil.isDefined(height)) {
      paramWidth = " width=\"" + width + "\" ";
      paramHeight = " height=\"" + height + "\" ";
    } else if (attachment != null) {
      // un des 2 seulement est renseigné, calculer le second
      if (StringUtil.isDefined(width)) {
        String[] paramSize = ImageUtil.getWidthAndHeightByWidth(new File(attachment.
            getAttachmentPath()), Integer.parseInt(width));
        if (StringUtil.isDefined(paramSize[0])) {
          paramWidth = " width=\"" + paramSize[0] + "\" ";
        }
        if (StringUtil.isDefined(paramSize[1])) {
          paramHeight = " height=\"" + paramSize[1] + "\" ";
        }
      }
      if (StringUtil.isDefined(height)) {
        String[] paramSize = ImageUtil.getWidthAndHeightByHeight(new File(attachment.
            getAttachmentPath()), Integer.parseInt(height));
        if (StringUtil.isDefined(paramSize[0])) {
          paramWidth = " width=\"" + paramSize[0] + "\" ";
        }
        if (StringUtil.isDefined(paramSize[1])) {
          paramHeight = " height=\"" + paramSize[1] + "\" ";
        }
      }
    }

    out.print("<img alt=\"\" src=\"");
    out.print(imageURL);
    out.print("\"");
    out.print(paramHeight);
    out.print(paramWidth);
    out.print("/>");
  }

  private void renderGalleries(final Operation originalOperation, String fieldName, String language,
      PrintWriter out) {
    String fieldNameFunction = FileServerUtils.replaceAccentChars(fieldName.replace(' ', '_'));
    List<ComponentInstLight> galleries = WysiwygController.getGalleries();
    if (galleries != null && !galleries.isEmpty()) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(" ").append(Util.getString("GML.or", language)).append(" ");
      stringBuilder.append("<select id=\"galleryFile_").append(fieldName).
          append("\" name=\"galleryFile\" onchange=\"openGalleryFileManager").
          append(fieldNameFunction).append("();this.selectedIndex=0\">");
      stringBuilder.append("<option value=\"\">");
      stringBuilder.append(Util.getString("GML.galleries", language));
      stringBuilder.append("</option>");
      for (ComponentInstLight component : galleries) {
        stringBuilder.append("<option value=\"").append(component.getId()).append("\">").append(
            component.getLabel(language)).append("</option>");
      }
      stringBuilder.append("</select>");
      out.println(stringBuilder.toString());
      
      out.println("<script type=\"text/javascript\">");
      GalleryHelper.getJavaScript(fieldNameFunction, fieldName, language, out);
      out.println("function choixImageInGallery" + fieldNameFunction + "(url){");
      out.println("$(\"#" + fieldName + "ThumbnailArea\").css(\"display\", \"block\");");
      out.println("$(\"#" + fieldName + "Thumbnail\").attr(\"src\", url);");
      out.println("$(\"#" + fieldName + "ThumbnailLink\").attr(\"href\", url);");
      out.println("$(\"#" + fieldName + FileField.PARAM_ID_SUFFIX + "\").attr(\"value\", url);");
      out.println("$(\"#" + fieldName + OPERATION_KEY + "\").attr(\"value\", \"" +
          originalOperation.name() + "\");");
      out.println("}");
      out.println("</script>");
    }
  }

  /**
   * Method declaration
   *
   * @return
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }
  
}
