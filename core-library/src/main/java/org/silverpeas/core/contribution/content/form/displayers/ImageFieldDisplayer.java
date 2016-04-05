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
package org.silverpeas.core.contribution.content.form.displayers;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.GalleryHelper;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RenderingContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

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

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL> <LI>the field type is not a managed type. </UL>
   * @throws FormException
   */
  @Override
  public void display(PrintWriter out, FileField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
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
    SimpleDocument attachment;
    String imageURL = null;
    if (StringUtil.isDefined(attachmentId)) {
      if (attachmentId.startsWith("/")) {
        imageURL = attachmentId;
        originalOperation = Operation.UPDATE;
      } else {
        attachment = AttachmentServiceProvider.getAttachmentService().searchDocumentById(
            attachmentPk, language);
        if (attachment != null) {
          originalOperation = Operation.UPDATE;
          if (pageContext.getRenderingContext() == RenderingContext.EXPORT) {
            imageURL = "file:" + attachment.getAttachmentPath();
          } else if (pageContext.isSharingContext()) {
            imageURL = pageContext.getSharingContext().getSharedUriOf(attachment).toString();
          } else {
            imageURL = URLUtil.getApplicationURL() + attachment.getAttachmentURL();
          }
        }
      }
    } else {
      attachmentId = "";
    }
    Map<String, String> parameters = template.getParameters(language);
    if (!template.isHidden()) {
      if (template.isReadOnly()) {
        if (imageURL != null) {
          displayImage(parameters, imageURL, out, pageContext.isSharingContext());
        }
      } else if (!template.isDisabled()) {

        String displayCSS = "display:none";
        if (imageURL != null) {
          displayCSS = "display:block";
        }

        String deleteImg = Util.getIcon("delete");
        String deleteLab = Util.getString("removeImage", language);
        String size = settings.getString("image.size.xmlform.thumbnail", null);
        if (!StringUtil.isDefined(size)) {
          size = "x50";
        }
        String thumbnailURL = imageURL;
        if (imageURL != null) {
          thumbnailURL = FileServerUtils.getImageURL(imageURL, size);
        }

        out.println("<div id=\"" + fieldName + "ThumbnailArea\" style=\"" + displayCSS + "\">");
        out.println("<a id=\"" + fieldName + "ThumbnailLink\" href=\"" + imageURL +
            "\" target=\"_blank\">");
        out.println("<img alt=\"\" align=\"top\" src=\"" + thumbnailURL + "\" id=\"" + fieldName +
            "Thumbnail\"/>&nbsp;");
        out.println("</a>");
        out.println(
            "&nbsp;<a href=\"#\" onclick=\"javascript:" + "document.getElementById('" + fieldName +
                "ThumbnailArea').style.display='none';" + "document." + pageContext.getFormName() +
                "." + fieldName + OPERATION_KEY + ".value='" + Operation.DELETION.name() + "';" +
                "\">");
        out.println("<img src=\"" + deleteImg + "\" width=\"15\" height=\"15\" alt=\"" + deleteLab +
            "\" align=\"top\" title=\"" + deleteLab + "\"/></a>");
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
  }

  private void displayImage(Map<String, String> parameters, String imageURL, PrintWriter out, boolean useOriginalDimension) {
    if (!useOriginalDimension) {
      String height = (parameters.containsKey("height") ? parameters.get("height") : "");
      String width = (parameters.containsKey("width") ? parameters.get("width") : "");
      String size = width + "x" + height;
      if (size.length() <= 1) {
        size = settings.getString("image.size.xmlform", null);
      }
      if (StringUtil.isDefined(size)) {
        imageURL = FileServerUtils.getImageURL(imageURL, size);
      }
    }

    out.print("<img alt=\"\" src=\"");
    out.print(imageURL);
    out.print("\"");
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
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

}
