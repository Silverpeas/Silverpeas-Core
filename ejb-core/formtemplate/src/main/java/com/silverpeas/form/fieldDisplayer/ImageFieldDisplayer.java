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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.form.fieldDisplayer;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ImageUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

/**
 * A ImageFieldDisplayer is an object which can display an image in HTML and can retrieve via HTTP
 * any file.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class ImageFieldDisplayer extends AbstractFieldDisplayer {

  public static final String CONTEXT_FORM_IMAGE = "XMLFormImages";

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    String[] s = new String[0];
    s[0] = Field.TYPE_FILE;
    return s;
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the fieldName is unknown by the template.
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template,
      PagesContext PagesContext) throws java.io.IOException {
    String language = PagesContext.getLanguage();

    String fieldName = template.getFieldName();

    if (!Field.TYPE_FILE.equals(template.getTypeName())) {
      SilverTrace.info("form", "ImageFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
          Field.TYPE_FILE);
    }
    if (template.isMandatory() && PagesContext.useMandatory()) {
      out.println("	if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println(
          "		var " + fieldName + "Value = document.getElementById('" + fieldName +
          Field.FILE_PARAM_NAME_SUFFIX + "').value;");
      out.println("		if (" + fieldName + "Value=='' || " + fieldName +
          "Value.substring(0,7)==\"remove_\") {");
      out.println("			errorMsg+=\"  - '" +
          EncodeHelper.javaStringToJsString(template.getLabel(language)) + "' " + Util.
          getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("			errorNb++;");
      out.println("		}");
      out.println("	}");
    }

    Util.getJavascriptChecker(template.getFieldName(), PagesContext, out);
  }

  @Override
  public void display(PrintWriter out, Field field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    display(out, field, template, pagesContext, FileServerUtils.getApplicationContext());
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the field type is not a managed type.
   * </UL>
   * @param out
   * @param field
   * @param template
   * @param pagesContext
   * @throws FormException
   */

  public void display(PrintWriter out, Field field, FieldTemplate template,
      PagesContext pagesContext, String webContext) throws FormException {
    SilverTrace.info("form", "ImageFieldDisplayer.display", "root.MSG_GEN_ENTER_METHOD",
        "fieldName = " + template.
        getFieldName() + ", value = " + field.getValue() + ", fieldType = " +
        field.getTypeName());

    String mandatoryImg = Util.getIcon("mandatoryField");

    String html = "";

    String fieldName = template.getFieldName();

    if (!field.getTypeName().equals(Field.TYPE_FILE)) {
      SilverTrace.info("form", "ImageFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          Field.TYPE_FILE);
    }

    String attachmentId = field.getValue();
    String componentId = pagesContext.getComponentId();
    AttachmentDetail attachment = null;
    if (attachmentId != null && attachmentId.length() > 0) {
      attachment =
          AttachmentController.searchAttachmentByPK(new AttachmentPK(attachmentId, componentId));
    } else {
      attachmentId = "";
    }

    if (template.isReadOnly() && !template.isHidden()) {
      if (attachment != null) {
        Map parameters = template.getParameters(pagesContext.getLanguage());
        String height =
            (parameters.containsKey("height") ? (String) parameters.get("height") : "");
        String width =
            (parameters.containsKey("width") ? (String) parameters.get("width") : "");

        String paramHeight = "";
        String paramWidth = "";
        if (StringUtil.isDefined(width) && StringUtil.isDefined(height)) {
          // les 2 paramètres sont renseignés : forcer la taille
          paramWidth = " width=\"" + width + "\" ";
          paramHeight = " height=\"" + height + "\" ";
        } else {
          // un des 2 seulement est renseigné, calculer le second
          if (StringUtil.isDefined(width)) {
            String[] paramSize =
                ImageUtil.getWidthAndHeightByWidth(getImagePath(attachment), Integer
                .parseInt(width));
            if (StringUtil.isDefined(paramSize[0])) {
              paramWidth = " width=\"" + paramSize[0] + "\" ";
            }
            if (StringUtil.isDefined(paramSize[1])) {
              paramHeight = " height=\"" + paramSize[1] + "\" ";
            }
          }
          if (StringUtil.isDefined(height)) {
            String[] paramSize =
                ImageUtil.getWidthAndHeightByHeight(getImagePath(attachment), Integer
                .parseInt(height));
            if (StringUtil.isDefined(paramSize[0])) {
              paramWidth = " width=\"" + paramSize[0] + "\" ";
            }
            if (StringUtil.isDefined(paramSize[1])) {
              paramHeight = " height=\"" + paramSize[1] + "\" ";
            }
          }
        }

        out.print("<IMG alt=\"\" src=\"");
        out.print(webContext);
        out.print(attachment.getAttachmentURL());
        out.print("\"");
        out.print(paramHeight);
        out.print(paramWidth);
        out.print("/>");
      }
    } else if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly()) {
      out.print("<INPUT type=\"file\" size=\"50\" id=\"");
      out.print(fieldName);
      out.print("\" name=\"");
      out.print(fieldName);
      out.print("\">");
      html +=
          "<INPUT type=\"hidden\" name=\"" + fieldName + Field.FILE_PARAM_NAME_SUFFIX +
          "\" value=\"" + attachmentId + "\">";

      if (attachment != null) {
        String deleteImg = Util.getIcon("delete");
        String deleteLab = Util.getString("removeImage", pagesContext.getLanguage());

        html += "&nbsp;<span id=\"div" + fieldName + "\">";
        html +=
            "<IMG alt=\"\" align=\"absmiddle\" src=\"" + attachment.getAttachmentIcon() +
            "\" width=20>&nbsp;";
        html +=
            "<A href=\"" + webContext + attachment.getAttachmentURL() + "\" target=\"_blank\">" +
            attachment.getLogicalName() + "</A>";

        html +=
            "&nbsp;<a href=\"#\" onclick=\"javascript:"
            + "document.getElementById('div" + fieldName + "').style.display='none';"
            + "document." + pagesContext.getFormName() + "." + fieldName +
            Field.FILE_PARAM_NAME_SUFFIX + ".value='remove_" + attachmentId + "';"
            + "\">";
        html += "<img src=\""
            + deleteImg
            + "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
            + deleteLab + "\" align=\"absmiddle\" title=\""
            + deleteLab + "\"></a>";
        html += "</span>";
      }

      if (template.isMandatory() && pagesContext.useMandatory()) {
        html += "&nbsp;<img src=\"" + mandatoryImg + "\" width=\"5\" height=\"5\" border=\"0\">";
      }
    }
    out.println(html);
  }

  @Override
  public List<String> update(String attachmentId, Field field,
      FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    List<String> attachmentIds = new ArrayList<String>();
    if (field.getTypeName().equals(Field.TYPE_FILE)) {
      if (!StringUtil.isDefined(attachmentId)) {
        field.setNull();
      } else {
        ((FileField) field).setAttachmentId(attachmentId);
        attachmentIds.add(attachmentId);
      }
    } else {
      throw new FormException("ImageFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          Field.TYPE_FILE);
    }
    return attachmentIds;
  }

  @Override
  public List<String> update(List<FileItem> items, Field field, FieldTemplate template,
      PagesContext pageContext) throws
      FormException {
    List<String> attachmentIds = new ArrayList<String>();
    String itemName = template.getFieldName();
    try {
      String value = processUploadedImage(items, itemName, pageContext);
      String param = FileUploadUtil.getParameter(items, itemName + Field.FILE_PARAM_NAME_SUFFIX);
      if (param != null) {
        if (param.startsWith("remove_")) {
          // Il faut supprimer le fichier
          String attachmentId = param.substring("remove_".length());
          deleteAttachment(attachmentId, pageContext);
        } else if (value != null && StringUtil.isInteger(param)) {
          // Y'avait-il un déjà un fichier ?
          // Il faut remplacer le fichier donc supprimer l'ancien
          deleteAttachment(param, pageContext);
        } else if (value == null) {
          // pas de nouveau fichier, ni de suppression
          // le champ ne doit pas être mis à jour
          return attachmentIds;
        }
      }
      if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES &&
          !StringUtil.isDefined(value)) {
        return attachmentIds;
      }
      attachmentIds.addAll(update(value, field, template, pageContext));
    } catch (Exception e) {
      SilverTrace.error("form", "ImageFieldDisplayer.update", "form.EXP_UNKNOWN_FIELD", null, e);
    }
    return attachmentIds;
  }

  /**
   * Method declaration
   * @return
   */
  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  /**
   * Method declaration
   * @return
   */
  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

  private String processUploadedImage(List items, String parameterName, PagesContext pagesContext)
      throws Exception {
    String attachmentId = null;
    FileItem item = FileUploadUtil.getFile(items, parameterName);
    if (!item.isFormField()) {
      String componentId = pagesContext.getComponentId();
      String userId = pagesContext.getUserId();
      String objectId = pagesContext.getObjectId();
      String logicalName = item.getName();
      String physicalName = null;
      String type = null;
      String mimeType = null;
      File dir = null;
      long size = 0;
      if (StringUtil.isDefined(logicalName)) {
        if (!FileUtil.isWindows()) {
          logicalName = logicalName.replace('\\', File.separatorChar);
          SilverTrace.info("form", "AbstractForm.processUploadedImage", "root.MSG_GEN_PARAM_VALUE",
              "fullFileName on Unix = " + logicalName);
        }

        logicalName =
            logicalName
            .substring(logicalName.lastIndexOf(File.separator) + 1, logicalName.length());
        type = FileRepositoryManager.getFileExtension(logicalName);
        mimeType = item.getContentType();

        physicalName = new Long(new Date().getTime()).toString() + "." + type;

        dir = getImagePath(componentId, physicalName);
        size = item.getSize();
        item.write(dir);

        // l'ajout du fichier joint ne se fait que si la taille du fichier (size) est >0
        // sinon cela indique que le fichier n'est pas valide (chemin non valide, fichier non
        // accessible)
        if (size > 0) {
          AttachmentDetail ad =
              createAttachmentDetail(objectId, componentId, physicalName, logicalName, mimeType,
              size,
              ImageFieldDisplayer.CONTEXT_FORM_IMAGE, userId);
          ad = AttachmentController.createAttachment(ad, true);
          attachmentId = ad.getPK().getId();
        } else {
          // le fichier à tout de même été créé sur le serveur avec une taille 0!, il faut le
          // supprimer
          if (dir != null) {
            FileFolderManager.deleteFolder(dir.getPath());
          }
        }
      }
    }
    return attachmentId;
  }

  private File getImagePath(AttachmentDetail attachment) {
    String path =
        AttachmentController.createPath(attachment.getInstanceId(), attachment.getContext());
    return new File(path + attachment.getPhysicalName());
  }

  private File getImagePath(String componentId, String physicalName) {
    String path =
        AttachmentController.createPath(componentId, ImageFieldDisplayer.CONTEXT_FORM_IMAGE);
    return new File(path + physicalName);
  }

  private AttachmentDetail createAttachmentDetail(String objectId, String componentId,
      String physicalName,
      String logicalName, String mimeType, long size, String context, String userId) {
    // create AttachmentPK with spaceId and componentId
    AttachmentPK atPK = new AttachmentPK(null, "useless", componentId);

    // create foreignKey with spaceId, componentId and id
    // use AttachmentPK to build the foreign key of customer object.
    AttachmentPK foreignKey = new AttachmentPK("-1", "useless", componentId);
    if (objectId != null) {
      foreignKey.setId(objectId);
    }

    // create AttachmentDetail Object
    AttachmentDetail ad =
        new AttachmentDetail(atPK, physicalName, logicalName, null, mimeType, size, context,
        new Date(), foreignKey);
    ad.setAuthor(userId);

    return ad;
  }

  private void deleteAttachment(String attachmentId, PagesContext pageContext) {
    SilverTrace.info("form", "AbstractForm.deleteAttachment", "root.MSG_GEN_ENTER_METHOD",
        "attachmentId = " + attachmentId + ", componentId = " + pageContext.getComponentId());
    AttachmentPK pk = new AttachmentPK(attachmentId, pageContext.getComponentId());
    AttachmentController.deleteAttachment(pk);
  }
}
