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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

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
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ImageUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.FileServerUtils;

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

  public static final String CONTEXT_FORM_IMAGE = "XMLFormImages";


  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when : <UL> <LI>the fieldName is unknown by the
   * template. <LI>the field type is not a managed type. </UL>
   *
   * @param pageContext
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pageContext)
      throws java.io.IOException {
    String language = pageContext.getLanguage();
    String fieldName = template.getFieldName();
    if (template.isMandatory() && pageContext.useMandatory()) {
      out.println("	if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println(
          "		var " + fieldName + "Value = document.getElementById('" + fieldName
          + FileField.PARAM_ID_SUFFIX + "').value;");
      out.println("		if (" + fieldName + "Value=='' || " + fieldName
          + "Value.substring(0,7)==\"remove_\") {");
      out.println("			errorMsg+=\"  - '"
          + EncodeHelper.javaStringToJsString(template.getLabel(language)) + "' " + Util.
          getString(
          "GML.MustBeFilled", language) + "\\n \";");
      out.println("			errorNb++;");
      out.println("		}");
      out.println("	}");
    }

    Util.includeFileNameLengthChecker(template, pageContext, out);
    Util.getJavascriptChecker(template.getFieldName(), pageContext, out);
  }

  @Override
  public void display(PrintWriter out, FileField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    display(out, field, template, pagesContext, FileServerUtils.getApplicationContext());
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL> <LI>the field type is not a managed type. </UL>
   *
   * @param out
   * @param field
   * @param template
   * @param pagesContext
   * @param webContext
   * @throws FormException
   */
  public void display(PrintWriter out, FileField field, FieldTemplate template,
      PagesContext pagesContext, String webContext) throws FormException {
    SilverTrace.info("form", "ImageFieldDisplayer.display", "root.MSG_GEN_ENTER_METHOD",
        "fieldName = " + template.getFieldName() + ", value = " + field.getValue()
        + ", fieldType = " + field.getTypeName());
    String fieldName = template.getFieldName();
    String language = pagesContext.getLanguage();

    String componentId = pagesContext.getComponentId();
    String attachmentId = field.getValue();
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
      } else {
        attachment = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
            attachmentPk, language);
        if (attachment != null) {
          if (pagesContext.getRenderingContext() == RenderingContext.EXPORT) {
            imageURL = "file:" + attachment.getAttachmentPath();
          } else {
            imageURL = webContext + attachment.getAttachmentURL();
          }
        }
      }
    } else {
      attachmentId = "";
    }
    Map<String, String> parameters = template.getParameters(language);
    if (template.isReadOnly() && !template.isHidden()) {
      if (imageURL != null) {
        String height = (parameters.containsKey("height") ? parameters.get("height") : "");
        String width = (parameters.containsKey("width") ? parameters.get("width") : "");
        String paramHeight = "";
        String paramWidth = "";
        if (StringUtil.isDefined(width) && StringUtil.isDefined(height)) {
          paramWidth = " width=\"" + width + "\" ";
          paramHeight = " height=\"" + height + "\" ";
        } else {
          // un des 2 seulement est renseigné, calculer le second
          if (StringUtil.isDefined(width) && attachment != null) {
            String[] paramSize = ImageUtil.getWidthAndHeightByWidth(new File(attachment.
                getAttachmentPath()), Integer.parseInt(width));
            if (StringUtil.isDefined(paramSize[0])) {
              paramWidth = " width=\"" + paramSize[0] + "\" ";
            }
            if (StringUtil.isDefined(paramSize[1])) {
              paramHeight = " height=\"" + paramSize[1] + "\" ";
            }
          }
          if (StringUtil.isDefined(height) && attachment != null) {
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
          + "document." + pagesContext.getFormName() + "." + fieldName
          + Field.FILE_PARAM_NAME_SUFFIX + ".value='remove_" + attachmentId + "';"
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
      out.println("<input type=\"hidden\" name=\"" + fieldName + Field.FILE_PARAM_NAME_SUFFIX
          + "\" id=\"" + fieldName + FileField.PARAM_ID_SUFFIX + "\" value=\"" + attachmentId
          + "\"/>");

      // Adding "Galleries" listbox if needed
      boolean useGalleries = Util.getBooleanValue(parameters, "galleries");
      String fieldNameFunction = FileServerUtils.replaceAccentChars(fieldName.replace(' ', '_'));
      if (useGalleries) {
        List<ComponentInstLight> galleries = WysiwygController.getGalleries();
        if (galleries != null && !galleries.isEmpty()) {

          StringBuilder stringBuilder = new StringBuilder();
          stringBuilder.append(" ").append(Util.getString("GML.or", language)).append(" ");
          stringBuilder.append("<select id=\"galleryFile_").append(fieldName).
              append("\" name=\"componentId\" onchange=\"openGalleryFileManager").
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
        }
      }

      if (template.isMandatory() && pagesContext.useMandatory()) {
        out.println(Util.getMandatorySnippet());
      }

      out.println("</div>");

      out.println("<script type=\"text/javascript\">");
      GalleryHelper.getJavaScript(fieldNameFunction, fieldName, language, out);
      out.println("function choixImageInGallery" + fieldNameFunction + "(url){");
      out.println("$(\"#" + fieldName + "ThumbnailArea\").css(\"display\", \"block\");");
      out.println("$(\"#" + fieldName + "Thumbnail\").attr(\"src\", url);");
      out.println("$(\"#" + fieldName + "ThumbnailLink\").attr(\"href\", url);");
      out.println("$(\"#" + fieldName + FileField.PARAM_ID_SUFFIX + "\").attr(\"value\", url);");
      out.println("}");
      out.println("</script>");
    }
  }

  @Override
  public List<String> update(String attachmentId, FileField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    List<String> attachmentIds = new ArrayList<String>();
    if (Field.TYPE_FILE.equals(field.getTypeName())) {
      if (!StringUtil.isDefined(attachmentId)) {
        field.setNull();
      } else {
        field.setAttachmentId(attachmentId);
        attachmentIds.add(attachmentId);
      }
    } else {
      throw new FormException("ImageFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          Field.TYPE_FILE);
    }
    return attachmentIds;
  }

  @Override
  public List<String> update(List<FileItem> items, FileField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    List<String> attachmentIds = new ArrayList<String>();
    String itemName = template.getFieldName();
    try {
      String value = processUploadedImage(items, itemName, pageContext);
      String param = FileUploadUtil.getParameter(items, itemName + Field.FILE_PARAM_NAME_SUFFIX);
      if (param != null && !pageContext.isCreation()) {
        if (param.startsWith("remove_")) {
          // Il faut supprimer le fichier
          String attachmentId = field.getAttachmentId();
          if (!attachmentId.startsWith("/")) {
            deleteAttachment(attachmentId, pageContext);
          } else {
            value = null;
          }
        } else if (value != null && StringUtil.isInteger(param)) {
          // Y'avait-il un déjà un fichier ?
          // Il faut remplacer le fichier donc supprimer l'ancien
          deleteAttachment(param, pageContext);
        } else if (value == null) {
          if (param.startsWith("/")) {
            // image from a gallery
            value = param;
          } else {
            // pas de nouveau fichier, ni de suppression
            // le champ ne doit pas être mis à jour
            return attachmentIds;
          }
        }
      }
      if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES
          && !StringUtil.isDefined(value)) {
        return attachmentIds;
      }
      attachmentIds.addAll(update(value, field, template, pageContext));
    } catch (IOException e) {
      SilverTrace.error("form", "ImageFieldDisplayer.update", "form.EXP_UNKNOWN_FIELD", null, e);
    }
    return attachmentIds;
  }

  /**
   * Method declaration
   *
   * @return
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
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

  private String processUploadedImage(List<FileItem> items, String parameterName,
      PagesContext pagesContext) throws IOException {
    String attachmentId = null;
    FileItem item = FileUploadUtil.getFile(items, parameterName);
    if (item != null && !item.isFormField()) {
      String componentId = pagesContext.getComponentId();
      String userId = pagesContext.getUserId();
      String objectId = pagesContext.getObjectId();
      if (StringUtil.isDefined(item.getName())) {
        String fileName = FileUtil.getFilename(item.getName());
        long size = item.getSize();
        if (size > 0L) {
          SimpleDocument document = createSimpleDocument(objectId, componentId, item, fileName,
              userId);
          attachmentId = document.getId();
        }
      }
    }
    return attachmentId;
  }
}
