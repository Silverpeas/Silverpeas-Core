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
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue.control.DynamicValueReplacement;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import net.htmlparser.jericho.Source;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformer;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A WysiwygFieldDisplayer is an object which can display a TextField in HTML the content of a
 * TextField to a end user and can retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class WysiwygFCKFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  public static final String dbKey = "xmlWysiwygField_";
  public static final String dir = "xmlWysiwyg";
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.wysiwyg.settings.wysiwygSettings");

  /**
   * Constructeur
   */
  public WysiwygFCKFieldDisplayer() {
  }

  /**
   * Returns the name of the managed types.
   * @return the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[] { TextField.TYPE };
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
   * @param out
   * @param template
   * @param PagesContext
   * @throws java.io.IOException
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext PagesContext)
      throws IOException {
    String fieldName = template.getFieldName();
    String language = PagesContext.getLanguage();

    if (!TextField.TYPE.equals(template.getTypeName())) {

    }

    if (!template.isReadOnly()) {
      out.println("var oEditor = CKEDITOR.instances." + fieldName + ";");
      out.println("var thecode = oEditor.getData();");
      if (template.isMandatory() && PagesContext.useMandatory()) {
        out.println(
            " if (isWhitespace(stripInitialWhitespace(thecode)) || thecode == \"<P>&nbsp;</P>\") {");
        out.println(" errorMsg+=\" - '" + template.getLabel(language) + "' "
            + Util.getString("GML.MustBeFilled", language) + "\\n\";");
        out.println(" errorNb++;");
        out.println(" }");
      }

      Util.getJavascriptChecker(template.getFieldName(), PagesContext, out);
    }
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
   * @param pageContext
   * @throws FormException
   */
  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String code = "";
    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
    if (!field.getTypeName().equals(TextField.TYPE)) {

    }
    if (!field.isNull()) {
      code = field.getValue(pageContext.getLanguage());
    }

    String contentLanguage = I18NHelper.checkLanguage(pageContext.getContentLanguage());

    code = getContent(pageContext.getComponentId(), pageContext.getObjectId(), template.
        getFieldName(), code, contentLanguage);

    if (pageContext.isSharingContext()) {
      code = pageContext.getSharingContext().applyOn(code);
    }

    if (template.isDisabled() || template.isReadOnly()) {
      code = WysiwygContentTransformer.on(code).modifyImageUrlAccordingToHtmlSizeDirective()
          .transform();

      // dynamic value functionality
      if (DynamicValueReplacement.isActivate()) {
        DynamicValueReplacement replacement = new DynamicValueReplacement();
        code = replacement.replaceKeyByValue(code);
      }
      out.println(code);

    } else {
      out.println("<table>");
      // Dynamic value functionality
      if (DynamicValueReplacement.isActivate()) {
        out.println("<tr class=\"TB_Expand\"><td class=\"TB_Expand\" align=\"center\">");
        out.println(DynamicValueReplacement.buildHTMLSelect(pageContext.getLanguage(), fieldName,
            fieldName));
        out.println("</td></tr>");
      }

      String fieldNameFunction = FileServerUtils.replaceAccentChars(fieldName.replace(' ', '_'));

      LocalizationBundle resources = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.wysiwyg.multilang.wysiwygBundle", contentLanguage);

      StringBuilder stringBuilder = new StringBuilder();

      // Storage file : HTML select building
      List<ComponentInstLight> fileStorage = null;
      boolean showFileStorages = true;
      if (parameters.containsKey("fileStorages")) {
        showFileStorages = StringUtil.getBooleanValue(parameters.get("fileStorages"));
      }
      if (showFileStorages) {
        fileStorage = WysiwygController.getStorageFile();
        if (!fileStorage.isEmpty()) {
          out.println("<tr class=\"TB_Expand\"><td class=\"TB_Expand\">");
          stringBuilder = new StringBuilder();
          stringBuilder.append("<select id=\"storageFile_").append(fieldName).append(
              "\" name=\"storageFile\" onchange=\"openStorageFileManager").append(
              FileServerUtils.replaceAccentChars(fieldName.replace(' ', '_'))).append(
              "();this.selectedIndex=0\">");
          stringBuilder.append("<option value=\"\">").append(
              resources.getString("storageFile.select.title")).append("</option>");
          for (ComponentInstLight component : fileStorage) {
            stringBuilder.append("<option value=\"").append(component.getId()).append("\">")
                .append(
                    component.getLabel(contentLanguage)).append("</option>");
          }
          stringBuilder.append("</select>");
          out.println(stringBuilder.toString());
        }
      }

      // Images uploaded : HTML select building
      List<SimpleDocument> listImages = null;
      if(pageContext.getObjectId() != null &&
          !"useless".equals(pageContext.getComponentId())) {
        listImages = WysiwygController.getImages(pageContext.getObjectId(), pageContext.getComponentId());
        if (!listImages.isEmpty()) {
          if (fileStorage == null || fileStorage.isEmpty()) {
            out.println("<tr class=\"TB_Expand\"><td class=\"TB_Expand\">");
          }
          stringBuilder = new StringBuilder();
          stringBuilder.append("<select id=\"images_").append(fieldName).append(
              "\" name=\"images\" onchange=\"choixImage").append(
              FileServerUtils.replaceAccentChars(fieldName.replace(' ', '_'))).append(
              "();this.selectedIndex=0\">");
          stringBuilder.append("<option value=\"\">").append(
              resources.getString("Image")).append("</option>");
          for (SimpleDocument image : listImages) {
            stringBuilder.append("<option value=\"").append(URLUtil.getApplicationURL()+image.getAttachmentURL()).append("\">")
                .append(image.getFilename()).append("</option>");
          }
          stringBuilder.append("</select>");
          out.println(stringBuilder.toString());
        }
      }

      // Gallery file : HTML select building
      List<ComponentInstLight> galleries = null;
      boolean showGalleries = true;
      if (parameters.containsKey("galleries")) {
        showGalleries = StringUtil.getBooleanValue(parameters.get("galleries"));
      }
      if (showGalleries) {
        galleries = WysiwygController.getGalleries();
        if (!galleries.isEmpty()) {
          if ((fileStorage == null || fileStorage.isEmpty()) &&
              (listImages == null || listImages.isEmpty())) {
            out.println("<tr class=\"TB_Expand\"><td class=\"TB_Expand\">");
          }
          stringBuilder = new StringBuilder();
          stringBuilder.append("<select id=\"galleryFile_").append(fieldName).append(
              "\" name=\"galleryFile\" onchange=\"openGalleryFileManager").append(
              fieldNameFunction).append("();this.selectedIndex=0\">");
          stringBuilder.append("<option value=\"\">").append(
              Util.getString("GML.galleries", contentLanguage)).append("</option>");
          for (ComponentInstLight component : galleries) {
            stringBuilder.append("<option value=\"").append(component.getId()).append("\">")
                .append(
                    component.getLabel(contentLanguage)).append("</option>");
          }
          stringBuilder.append("</select>");
          out.println(stringBuilder.toString());
        }
      }

      if ((fileStorage != null && !fileStorage.isEmpty()) ||
          (listImages != null && !listImages.isEmpty()) ||
          (galleries != null && !galleries.
          isEmpty())) {
        out.println("</td></tr>");
      }

      out.println("<tr>");

      // looks for size parameters
      int editorWidth = 600;
      int editorHeight = 300;
      if (parameters.containsKey("width")) {
        editorWidth = Integer.parseInt(parameters.get("width"));
      }
      if (parameters.containsKey("height")) {
        editorHeight = Integer.parseInt(parameters.get("height"));
      }

      out.println("<td valign=\"top\">");
      out.println("<textarea id=\"" + fieldName + "\" name=\"" + fieldName
          + "\" rows=\"10\" cols=\"10\">" + code + "</textarea>");
      out.println("<script type=\"text/javascript\">");

      stringBuilder = new StringBuilder();
      String configFile = settings.getString("configFile", URLUtil.getApplicationURL()
          + "/wysiwyg/jsp/ckeditor/silverconfig.js");

      stringBuilder.append("CKEDITOR.replace('").append(fieldName).append("', {\n");
      stringBuilder.append("width : '").append(editorWidth).append("',\n");
      stringBuilder.append("height : ").append(editorHeight).append(",\n");
      stringBuilder.append("language : '").append(pageContext.getLanguage()).append("',\n");
      String basehref = settings.getString("baseHref", pageContext.getServerURL());
      if (StringUtil.isDefined(basehref)) {
        stringBuilder.append("baseHref : '").append(basehref).append("',\n");
      }
      String fileBrowserUrl =
          Util.getPath() + "/wysiwyg/jsp/uploadFile.jsp?ComponentId=" +
              pageContext.getComponentId() + "&ObjectId=" + pageContext.getObjectId() +
              "&Context=" + fieldName;
      stringBuilder.append("filebrowserImageBrowseUrl : '").append(fileBrowserUrl).append("',\n");
      stringBuilder.append("filebrowserFlashBrowseUrl : '").append(fileBrowserUrl).append("',\n");
      stringBuilder.append("filebrowserBrowseUrl : '").append(fileBrowserUrl).append("',\n");
      stringBuilder.append("toolbarStartupExpanded : ").append("false").append(",\n");
      stringBuilder.append("customConfig : '").append(configFile).append("',\n");
      stringBuilder.append("toolbar : '").append("XMLForm").append("',\n");
      String skin = settings.getString("skin", "");
      if (StringUtil.isDefined(skin)) {
        stringBuilder.append("skin : '").append(skin).append("'\n");
      }
      stringBuilder.append("});");

      out.println(stringBuilder.toString());

      // field name used to generate a javascript function name
      // dynamic value functionality
      if (DynamicValueReplacement.isActivate()) {

        out.println("function chooseDynamicValues"
            + fieldNameFunction + "(){");
        out.println(" var oEditor = CKEDITOR.instances['" + fieldName + "'];");
        out.println(" var focusManager = new CKEDITOR.focusManager( oEditor );");
        out.println(" focusManager.focus();");
        out.println("index = document.getElementById(\"dynamicValues_" + fieldName
            + "\").selectedIndex;");
        out.println("var str = document.getElementById(\"dynamicValues_" + fieldName
            + "\").options[index].value;");
        out.println("if (index != 0 && str != null){");
        out.println("oEditor.insertHtml('(%'+str+'%)');");
        out.println("} }");
      }

      // Storage file exists : javascript functions
      if (fileStorage != null && !fileStorage.isEmpty()) {
        out.println("var storageFileWindow=window;");
        out.println("function openStorageFileManager" + fieldNameFunction + "(){");
        out.println("index = document.getElementById(\"storageFile_" + fieldName
            + "\").selectedIndex;");
        out.println("var componentId = document.getElementById(\"storageFile_" + fieldName
            + "\").options[index].value;");
        out.println("if (index != 0){ ");
        out.println("url = \""
            + URLUtil.getApplicationURL()
            +
            "/kmelia/jsp/attachmentLinkManagement.jsp?key=\"+componentId+\"&ntype=COMPONENT&fieldname="
            + fieldNameFunction + "\";");
        out.println("windowName = \"StorageFileWindow\";");
        out.println("width = \"750\";");
        out.println("height = \"580\";");
        out.
            println("windowParams = \"scrollbars=1,directories=0,menubar=0,toolbar=0, alwaysRaised\";");
        out.println("if (!storageFileWindow.closed && storageFileWindow.name==windowName)");
        out.println("storageFileWindow.close();");
        out.
            println("storageFileWindow = SP_openWindow(url, windowName, width, height, windowParams);");
        out.println("}}");

        out.println("function insertAttachmentLink" + fieldNameFunction + "(url,img,label){");
        out.println(" var oEditor = CKEDITOR.instances['" + fieldName + "'];");
        out.println(" var focusManager = new CKEDITOR.focusManager( oEditor );");
        out.println(" focusManager.focus();");
        out.
            println(
            "oEditor.insertHtml('<a href=\"'+url+'\"> <img src=\"'+img+'\" width=\"20\" border=\"0\" alt=\"\"/> '+label+'</a> ');");
        out.println("}");
      }

      // Images uploaded : javascript functions
      if (listImages != null && !listImages.isEmpty()) {
        out.println("function choixImage" + fieldNameFunction +"() {");
        out.println(" var oEditor = CKEDITOR.instances['" + fieldName + "'];");
        out.println(" var focusManager = new CKEDITOR.focusManager( oEditor );");
        out.println(" focusManager.focus();");
        out.println(" var index = document.getElementById(\"images_" + fieldName
            + "\").selectedIndex;");
        out.println(" var str = document.getElementById(\"images_" + fieldName
            + "\").options[index].value;");
        out.println(" if (index != 0 && str != null) {");
        out.println("   oEditor.insertHtml('<img border=\"0\" src=\"'+str+'\" alt=\"\"/>');");
        out.println(" }");
        out.println("}");
      }

      // Gallery files exists : javascript functions
      if (galleries != null && !galleries.isEmpty()) {
        GalleryHelper.getJavaScript(fieldNameFunction, fieldName, contentLanguage, out);

        out.println("function choixImageInGallery" + fieldNameFunction + "(url){");
        out.println(" var oEditor = CKEDITOR.instances['" + fieldName + "'];");
        out.println(" var focusManager = new CKEDITOR.focusManager( oEditor );");
        out.println(" focusManager.focus();");
        out.println("oEditor.insertHtml('<img src=\"'+url+'\" border=\"0\" alt=\"\"/>');");
        out.println("}");
      }

      out.println("</script>");

      if (template.isMandatory() && pageContext.useMandatory()) {
        out.println(Util.getMandatorySnippet());
      }

      out.println("</td>");
      out.println("</tr>");
      out.println("</table>");

    }
  }

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @param newValue
   * @param pageContext
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  @Override
  public List<String> update(String newValue, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    if (!field.getTypeName().equals(TextField.TYPE)) {
      throw new FormException("WysiwygFCKFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (field.acceptValue(newValue, pageContext.getLanguage())) {
      try {
        String contentLanguage = I18NHelper.checkLanguage(pageContext.getContentLanguage());

        String fileName =
            setContentIntoFile(pageContext.getComponentId(), pageContext.getObjectId(),
                template.getFieldName(), newValue, contentLanguage);

        field.setValue(dbKey + fileName, contentLanguage);
      } catch (FormException e) {
        throw new FormException("WysiwygFCKFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE", e);
      }
    } else {
      throw new FormException("WysiwygFCKFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          TextField.TYPE);
    }
    return new ArrayList<>();
  }

  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

  @Override
  public void index(FullIndexEntry indexEntry, String key, String fieldName, TextField field,
      String language, boolean store) {
    String fieldValue = field.getValue();
    String fieldValueIndex = "";
    if (StringUtil.isDefined(fieldValue)) {
      if (fieldValue.startsWith(dbKey)) {
        String file = getFile(indexEntry.getComponent(), indexEntry.getObjectId(), fieldName,
            language);
        try {
          Source source = new Source(new FileInputStream(file));
          fieldValueIndex = source.getTextExtractor().toString();
        } catch (IOException ioex) {
          SilverTrace.warn("form", "WysiwygFCKFieldDisplayer.index", "form.incorrect_data",
              "File not found " + file + " " + ioex.getMessage(), ioex);
        }
        indexEntry.addTextContent(fieldValueIndex, language);
      } else {
        indexEntry.addTextContent(fieldValue.trim(), language);
        fieldValueIndex = fieldValue.trim().replaceAll("##", " ");
      }
      indexEntry.addField(key, fieldValueIndex, language, false);

      // index embedded linked attachment (links presents in wysiwyg content)
      try {
        String content = getContentFromFile(indexEntry.getComponent(), indexEntry.getObjectId(),
            fieldName, language);
        List<String> embeddedAttachmentIds = WysiwygController.getEmbeddedAttachmentIds(content);
        WysiwygController.indexEmbeddedLinkedFiles(indexEntry, embeddedAttachmentIds);
      } catch (UtilException e) {
        SilverTrace.warn("form", "WysiwygFCKFieldDisplayer.index", "form.incorrect_data",
            "Unable to extract linkes files from object" + indexEntry.getObjectId(), e);
      }
    }
  }

  public void duplicateContent(Field field, FieldTemplate template,
      PagesContext pageContext, String newObjectId) throws FormException {

    String contentLanguage = I18NHelper.checkLanguage(pageContext.getContentLanguage());

    String code = field.getStringValue();
    code = getContent(pageContext.getComponentId(), pageContext.getObjectId(),
        template.getFieldName(), code, contentLanguage);
    String fileName = setContentIntoFile(pageContext.getComponentId(), newObjectId, template.
        getFieldName(), code, contentLanguage);
    field.setValue(dbKey + fileName, pageContext.getLanguage());
  }

  private String getContent(String componentId, String objectId, String fieldName, String code,
      String language) throws
      FormException {
    if (!code.startsWith(dbKey)) {
      setContentIntoFile(componentId, objectId, fieldName, code, language);
    } else {
      try {
        code = getContentFromFile(componentId, objectId, fieldName, language);
      } catch (UtilException e) {
        throw new FormException("WysiwygFCKFieldDisplayer.getContent", e.getMessage(), e);
      }
    }
    return code;
  }

  private String setContentIntoFile(String componentId, String objectId, String fieldName,
      String code, String language) {
    try {
      FileRepositoryManager.createAbsolutePath(componentId, dir);
    } catch (Exception ignored) {
    }
    String path = getPath(componentId);
    String fileName = getFileName(fieldName, objectId, language);

    try {
      FileFolderManager.createFile(path, fileName, code);
    } catch (UtilException e) {
      // do nothing
    }
    return fileName;
  }

  public static String getContentFromFile(String componentId, String objectId, String fieldName)
      throws UtilException {
    return getContentFromFile(componentId, objectId, fieldName, null);
  }

  public static String getContentFromFile(String componentId, String objectId, String fieldName,
      String language) throws UtilException {
    String fileName = getFileName(fieldName, objectId, language);
    String path = getPath(componentId);

    return FileFolderManager.getCode(path, fileName);
  }

  private static String getFileName(String fieldName, String objectId) {
    return getFileName(fieldName, objectId, null);
  }

  private static String getFileName(String fieldName, String objectId, String language) {
    if (language == null || I18NHelper.isDefaultLanguage(language)) {
      return objectId + "_" + fieldName;
    } else {
      return objectId + "_" + language + "_" + fieldName;
    }
  }

  public void move(ForeignPK fromPK, ForeignPK toPK) throws IOException {
    moveOrCopy(fromPK, toPK, false, null);
  }

  private void moveOrCopy(ForeignPK fromPK, ForeignPK toPK, boolean copy,
      Map<String, String> oldAndNewFileIds) throws IOException {
    String fromPath = getPath(fromPK.getInstanceId());
    String toPath = getPath(toPK.getInstanceId());

    File from = new File(fromPath);
    if (from.exists()) {
      List<File> files = (List<File>) FileFolderManager.getAllFile(fromPath);
      for (File file : files) {
        String fileName = file.getName();
        if (fileName.startsWith(fromPK.getId() + "_")) {
          String fieldName = fileName.substring(fromPK.getId().length() + 1);
          File srcFile = new File(fromPath, file.getName());
          File destFile = new File(toPath, getFileName(fieldName, toPK.getId()));
          if (copy) {
            // copy file and change images path (instanceId and imageId) inside
            FileUtils.copyFile(srcFile, destFile);
            changeImagePath(destFile, fromPK.getInstanceId(), toPK.getInstanceId(),
                oldAndNewFileIds);
          } else {
            // move file and change images path (instanceId only) inside
            FileUtils.moveFile(srcFile, destFile);
            changeInstanceId(destFile, fromPK.getInstanceId(), toPK.getInstanceId());
          }
          Iterator<String> languages = I18NHelper.getLanguages();
          while (languages.hasNext()) {
            String language = languages.next();

            if (fieldName.startsWith(language + "_")) {
              fieldName = fieldName.substring(3); // skip en_
              srcFile = new File(fromPath, file.getName());
              destFile = new File(toPath, getFileName(fieldName, toPK.getId(), language));
              if (copy) {
                // copy file and change images path (instanceId and imageId) inside
                FileUtils.copyFile(srcFile, destFile);
                changeImagePath(destFile, fromPK.getInstanceId(), toPK.getInstanceId(),
                    oldAndNewFileIds);
              } else {
                // move file and change images path (instanceId only) inside
                FileUtils.moveFile(srcFile, destFile);
                changeInstanceId(destFile, fromPK.getInstanceId(), toPK.getInstanceId());
              }
            }
          }
        }
      }
    }
  }

  private void changeInstanceId(File file, String from, String to) throws IOException {
    String content = FileUtils.readFileToString(file, Charsets.UTF_8);
    String changed = content.replaceAll("/" + from + "/", "/" + to + "/");
    FileUtils.writeStringToFile(file, changed, Charsets.UTF_8);
  }

  private void changeImagePath(File file, String from, String to,
      Map<String, String> oldAndNewFileIds) throws IOException {
    String content = FileUtils.readFileToString(file, Charsets.UTF_8);
    ForeignPK fromPK = new ForeignPK("unknown", from);
    ForeignPK toPK = new ForeignPK("unknown", to);
    for (String oldId : oldAndNewFileIds.keySet()) {
      fromPK.setId(oldId);
      toPK.setId(oldAndNewFileIds.get(oldId));
      content = replaceInternalImageId(content, fromPK, toPK);
    }
    FileUtils.writeStringToFile(file, content, Charsets.UTF_8);
  }

  private String replaceInternalImageId(String content, ForeignPK oldPK, ForeignPK newPK) {
    String from = "/componentId/" + oldPK.getInstanceId() + "/attachmentId/" + oldPK.getId() + "/";
    String to = "/componentId/" + newPK.getInstanceId() + "/attachmentId/" + newPK.getId() + "/";
    return content.replaceAll(from, to);
  }

  public void cloneContents(ForeignPK fromPK, ForeignPK toPK, Map<String, String> oldAndNewFileIds)
      throws IOException {
    if (oldAndNewFileIds == null) {
      oldAndNewFileIds = new HashMap<>();
    }

    List<SimpleDocument> images =
        AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKeyAndType(fromPK,
            DocumentType.image, null);
    for (SimpleDocument image : images) {
      SimpleDocumentPK imageCopyPk =
          AttachmentServiceProvider.getAttachmentService().copyDocument(image, toPK);
      oldAndNewFileIds.put(image.getId(), imageCopyPk.getId());
    }

    moveOrCopy(fromPK, toPK, true, oldAndNewFileIds);
  }

  public void mergeContents(String componentIdFrom, String objectIdFrom, String componentIdTo,
      String objectIdTo) throws UtilException, IOException {
    String fromPath = getPath(componentIdFrom);

    File from = new File(fromPath);
    if (from.exists()) {
      // Verifie si le repertoire de destination existe
      try {
        FileRepositoryManager.createAbsolutePath(componentIdTo, dir);
      } catch (Exception e) {
        throw new IOException(e.getMessage());
      }

      // Copier/coller de tous les fichiers wysiwyg de objectIdFrom vers objectIdTo
      List<File> files = (List<File>) FileFolderManager.getAllFile(fromPath);
      for (File file : files) {
        String fileName = file.getName();
        if (fileName.startsWith(objectIdFrom + "_")) {
          String fieldName = fileName.substring(objectIdFrom.length() + 1);
          String fieldContent = getContentFromFile(componentIdFrom, objectIdFrom, fieldName);

          setContentIntoFile(componentIdTo, objectIdTo, fieldName, fieldContent, null);

          // paste translations
          Iterator<String> languages = I18NHelper.getLanguages();
          while (languages.hasNext()) {
            String language = languages.next();

            if (fieldName.startsWith(language + "_")) {
              fieldName = fieldName.substring(3); // skip en_
              fieldContent = getContentFromFile(componentIdFrom, objectIdFrom, fieldName, language);
              setContentIntoFile(componentIdTo, objectIdTo, fieldName, fieldContent, language);
            }
          }
        }
      }

      // Delete merged files
      for (File file : files) {
        String fileName = file.getName();
        if (fileName.startsWith(objectIdFrom + "_")) {
          file.delete();
        }
      }
    }
  }

  public static String getFile(String componentId, String objectId, String fieldName,
      String language) {
    return getPath(componentId) + getFileName(fieldName, objectId, language);
  }

  public static void removeContents(ForeignPK pk) {
    String fromPath = getPath(pk.getInstanceId());
    File directory = new File(fromPath);
    if (directory.exists()) {
      Collection<File> files =
          FileUtils.listFiles(directory, new PrefixFileFilter(pk.getId() + "_"), null);
      for (File file : files) {
        FileUtils.deleteQuietly(file);
      }
    }
  }

  private static String getPath(String componentId) {
    String[] dirs = { dir };
    return FileRepositoryManager.getAbsolutePath(componentId, dirs);
  }
}
