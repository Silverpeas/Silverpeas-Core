/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import net.htmlparser.jericho.Source;
import org.apache.commons.io.FileUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformer;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.text.MessageFormat.format;

/**
 * A WysiwygFieldDisplayer is an object which can display a TextField in HTML the content of a
 * TextField to a end user and can retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class WysiwygFCKFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  public static final String DB_KEY = "xmlWysiwygField_";
  private static final String DIRECTORYNAME = "xmlWysiwyg";
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.wysiwyg.settings.wysiwygSettings");

  private static final int DEFAULT_WIDTH = 600;
  private static final int DEFAULT_HEIGHT = 300;

  private static final String DD_UPLOAD_TEMPLATE_SCRIPT =
      "whenSilverpeasReady(function() '{'" +
        "configureCkEditorDdUpload('{'" +
          "componentInstanceId : ''{0}''," +
          "resourceId : ''{1}'', " +
          "indexIt : {2}" +
        "'}');" +
      "'}');\n";
  private static final String WYSIWYG_FCKFIELD_DISPLAYER_UPDATE = "WysiwygFCKFieldDisplayer.update";

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
   * log a message and writes an empty string when :
   * <UL>
   * <LI>the fieldName is unknown by the template.
   * <LI>the field type is not a managed type.
   * </UL>
   * @param out
   * @param template
   * @param pageContext
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pageContext) {
    String fieldName = template.getFieldName();
    String language = pageContext.getLanguage();
    String label = WebEncodeHelper.javaStringToJsString(template.getLabel(language));
    if (!template.isReadOnly()) {
      out.println("var oEditor = CKEDITOR.instances." + fieldName + ";");
      out.println("var thecode = oEditor.getData();");
      if (template.isMandatory() && pageContext.useMandatory()) {
        out.println(
            " if (!ignoreMandatory && isWhitespace(stripInitialWhitespace(thecode))) {");
        out.println(" errorMsg+=\" - '" + label + "' "
            + Util.getString("GML.MustBeFilled", language) + "\\n\";");
        out.println(" errorNb++;");
        out.println(" }");
      }

      Util.getJavascriptChecker(template.getFieldName(), pageContext, out);
    }
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a message and writes an empty string when :
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
    String fieldValue = field.getValue();
    if (StringUtil.isDefined(fieldValue) && fieldValue.startsWith(DB_KEY)) {
      String fileName = fieldValue.substring(DB_KEY.length());
      code = getContentFromFile(pageContext.getComponentId(), fileName);
    } else {
      // Case of field initialized with a content
      code = fieldValue;
    }
    if (code == null) {
      code = "";
    }

    if (pageContext.isSharingContext()) {
      code = pageContext.getSharingContext().applyOn(code);
    }

    if (template.isDisabled() || template.isReadOnly()) {
      displayContent(out, code);
    } else {
      displayEditor(out, code, template, pageContext);
    }
  }

  private void displayContent(PrintWriter out, String code) {
    final WysiwygContentTransformer wysiwygContentTransformer =
        WysiwygContentTransformer.on(code)
            .modifyImageUrlAccordingToHtmlSizeDirective()
            .resolveVariablesDirective()
            .applySilverpeasLinkCssDirective();
    out.println(wysiwygContentTransformer.transform());
  }

  private void displayEditor(PrintWriter out, String code, FieldTemplate template,
      PagesContext pageContext) {
    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());

    out.println("<table>");
    out.println("<tr>");

    // looks for size parameters
    int editorWidth = DEFAULT_WIDTH;
    int editorHeight = DEFAULT_HEIGHT;
    if (parameters.containsKey("width")) {
      editorWidth = Integer.parseInt(parameters.get("width"));
    }
    if (parameters.containsKey("height")) {
      editorHeight = Integer.parseInt(parameters.get("height"));
    }

    boolean showFileStorages = true;
    if (parameters.containsKey("fileStorages")) {
      showFileStorages = StringUtil.getBooleanValue(parameters.get("fileStorages"));
    }

    boolean showGalleries = true;
    if (parameters.containsKey("galleries")) {
      showGalleries = StringUtil.getBooleanValue(parameters.get("galleries"));
    }

    String toolbarStartupExpanded = Util.getSetting("form.field.wysiwyg.toolbar.startupExpanded");

    out.println("<td valign=\"top\">");
    out.println("<textarea id=\"" + fieldName + "\" name=\"" + fieldName
        + "\" rows=\"10\" cols=\"10\">" + code + "</textarea>");
    out.println("<script type=\"text/javascript\">");

    StringBuilder stringBuilder = new StringBuilder();
    String configFile = getWysiwygConfigFile();

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
    stringBuilder.append("imageUploadUrl : '").append("activated").append("',\n");
    stringBuilder.append("toolbarStartupExpanded : ").append(toolbarStartupExpanded).append(",\n");
    stringBuilder.append("customConfig : '").append(configFile).append("',\n");
    stringBuilder.append("toolbar : '").append("XMLForm").append("',\n");
    String skin = settings.getString("skin", "");
    if (StringUtil.isDefined(skin)) {
      stringBuilder.append("skin : '").append(skin).append("',\n");
    }
    stringBuilder.append("filebank : ").append(showFileStorages).append(",\n");
    stringBuilder.append("imagebank : ").append(showGalleries).append(",\n");
    stringBuilder.append("silverpeasObjectId : '").append(pageContext.getObjectId()).append("',\n");
    stringBuilder.append("silverpeasComponentId : '").append(pageContext.getComponentId()).append("'\n");
    stringBuilder.append("});\n");

    stringBuilder.append(format(DD_UPLOAD_TEMPLATE_SCRIPT, pageContext.getComponentId(), pageContext.getObjectId(), false));

    out.println(stringBuilder.toString());

    out.println("</script>");

    if (template.isMandatory() && pageContext.useMandatory()) {
      out.println(Util.getMandatorySnippet());
    }

    out.println("</td>");
    out.println("</tr>");
    out.println("</table>");
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
      throw new FormException(WYSIWYG_FCKFIELD_DISPLAYER_UPDATE, "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (field.acceptValue(newValue, pageContext.getLanguage())) {
      try {
        String fieldValue = field.getValue();
        if (StringUtil.isDefined(fieldValue) && fieldValue.startsWith(DB_KEY)) {
          String fileName = fieldValue.substring(DB_KEY.length());
          setContentIntoFile(pageContext.getComponentId(), fileName, newValue);
        } else {
          String contentLanguage = I18NHelper.checkLanguage(pageContext.getContentLanguage());
          String fileName =
              setContentIntoFile(pageContext.getComponentId(), pageContext.getObjectId(),
                  template.getFieldName(), newValue, contentLanguage);
          field.setValue(DB_KEY + fileName, contentLanguage);
        }
      } catch (FormException e) {
        throw new FormException(WYSIWYG_FCKFIELD_DISPLAYER_UPDATE, "form.EX_NOT_CORRECT_VALUE", e);
      }
    } else {
      throw new FormException(WYSIWYG_FCKFIELD_DISPLAYER_UPDATE, "form.EX_NOT_CORRECT_VALUE",
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
      if (fieldValue.startsWith(DB_KEY)) {
        String file = getFile(indexEntry.getComponent(), indexEntry.getObjectId(), fieldName,
            language);
        try {
          Source source = new Source(new FileInputStream(file));
          fieldValueIndex = source.getTextExtractor().toString();
        } catch (IOException ioex) {
          SilverLogger.getLogger(this).warn(ioex);
        }
        indexEntry.addTextContent(fieldValueIndex, language);
      } else {
        indexEntry.addTextContent(fieldValue.trim(), language);
        fieldValueIndex = fieldValue.trim().replace("##", " ");
      }
      indexEntry.addField(key, fieldValueIndex, language, false);

      // index embedded linked attachment (links presents in wysiwyg content)
      String content = getContentFromFile(indexEntry.getComponent(), indexEntry.getObjectId(),
          fieldName, language);
      List<String> embeddedAttachmentIds = WysiwygController.getEmbeddedAttachmentIds(content);
      WysiwygController.indexEmbeddedLinkedFiles(indexEntry, embeddedAttachmentIds);
    }
  }

  public String duplicateContent(FieldTemplate template, ResourceReference from, ResourceReference to,
      String language) {
    String code = getContent(from.getInstanceId(), from.getId(), template.getFieldName(), language);
    String fileName = setContentIntoFile(to.getInstanceId(), to.getId(), template.
        getFieldName(), code, language);
    return DB_KEY + fileName;
  }

  public void duplicateContent(Field field, FieldTemplate template,
      PagesContext pageContext, String newObjectId) throws FormException {

    String contentLanguage = I18NHelper.checkLanguage(pageContext.getContentLanguage());

    String code = getContent(pageContext.getComponentId(), pageContext.getObjectId(),
        template.getFieldName(), contentLanguage);
    String fileName = setContentIntoFile(pageContext.getComponentId(), newObjectId, template.
        getFieldName(), code, contentLanguage);
    field.setValue(DB_KEY + fileName, pageContext.getLanguage());
  }

  private String getContent(String componentId, String objectId, String fieldName,
      String language) {
    return getContentFromFile(componentId, objectId, fieldName, language);
  }

  private String setContentIntoFile(String componentId, String objectId, String fieldName,
      String code, String language) {
    String fileName = getFileName(fieldName, objectId, language);
    setContentIntoFile(componentId, fileName, code);
    return fileName;
  }

  private void setContentIntoFile(String componentId, String fileName, String code) {
    FileRepositoryManager.createAbsolutePath(componentId, DIRECTORYNAME);
    String path = getPath(componentId);
    FileFolderManager.createFile(path, fileName, code);
  }

  public static String getContentFromFile(String componentId, String objectId, String fieldName) {
    return getContentFromFile(componentId, objectId, fieldName, null);
  }

  public static String getContentFromFile(String componentId, String objectId, String fieldName,
      String language) {
    String fileName = getFileName(fieldName, objectId, language);
    return getContentFromFile(componentId, fileName);
  }

  public static String getContentFromFile(String componentId, String fileName) {
    if (StringUtil.isDefined(fileName) && isDirectoryExists(componentId)) {
      String path = getPath(componentId);
      Optional<String> content = FileFolderManager.getFileContent(path, fileName);
      if (content.isPresent()) {
        return content.get();
      }
    }
    return "";
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

  public void move(ResourceReference fromPK, ResourceReference toPK) throws IOException {
    moveOrCopy(fromPK, toPK, false, null);
  }

  private void moveOrCopy(ResourceReference fromPK, ResourceReference toPK, boolean copy,
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
          moveOrCopyFile(fromPK, toPK, srcFile, destFile, copy, oldAndNewFileIds);

          Collection<String> languages = I18NHelper.getLanguages();
          for (final String language: languages) {
            if (fieldName.startsWith(language + "_")) {
              // skip en_
              fieldName = fieldName.substring(3);
              srcFile = new File(fromPath, file.getName());
              destFile = new File(toPath, getFileName(fieldName, toPK.getId(), language));
              moveOrCopyFile(fromPK, toPK, srcFile, destFile, copy, oldAndNewFileIds);
            }
          }
        }
      }
    }
  }

  private void moveOrCopyFile(ResourceReference fromPK, ResourceReference toPK, File srcFile,
      File destFile, boolean copy, Map<String, String> oldAndNewFileIds) throws IOException {
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

  private void changeInstanceId(File file, String from, String to) throws IOException {
    String content = FileUtils.readFileToString(file, Charsets.UTF_8);
    String changed = content.replaceAll("/" + from + "/", "/" + to + "/");
    FileUtils.writeStringToFile(file, changed, Charsets.UTF_8);
  }

  private void changeImagePath(File file, String from, String to,
      Map<String, String> oldAndNewFileIds) throws IOException {
    String content = FileUtils.readFileToString(file, Charsets.UTF_8);
    ResourceReference fromPK = new ResourceReference(ResourceReference.UNKNOWN_ID, from);
    ResourceReference toPK = new ResourceReference(ResourceReference.UNKNOWN_ID, to);
    for (Map.Entry<String, String> fileIds : oldAndNewFileIds.entrySet()) {
      fromPK.setId(fileIds.getKey());
      toPK.setId(fileIds.getValue());
      content = replaceInternalImageId(content, fromPK, toPK);
    }
    FileUtils.writeStringToFile(file, content, Charsets.UTF_8);
  }

  private String replaceInternalImageId(String content, ResourceReference oldPK, ResourceReference newPK) {
    String from = "/componentId/" + oldPK.getInstanceId() + "/attachmentId/" + oldPK.getId() + "/";
    String to = "/componentId/" + newPK.getInstanceId() + "/attachmentId/" + newPK.getId() + "/";
    return content.replaceAll(from, to);
  }

  public void cloneContents(ResourceReference fromPK, ResourceReference toPK, Map<String, String> oldAndNewFileIds)
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
      String objectIdTo) {
    String fromPath = getPath(componentIdFrom);

    File from = new File(fromPath);
    if (from.exists()) {
      // Verifie si le repertoire de destination existe
      FileRepositoryManager.createAbsolutePath(componentIdTo, DIRECTORYNAME);

      // Copier/coller de tous les fichiers wysiwyg de objectIdFrom vers objectIdTo
      List<File> files = (List<File>) FileFolderManager.getAllFile(fromPath);
      for (File file : files) {
        String fileName = file.getName();
        if (fileName.startsWith(objectIdFrom + "_")) {
          String fieldName = fileName.substring(objectIdFrom.length() + 1);
          String fieldContent = getContentFromFile(componentIdFrom, objectIdFrom, fieldName);

          setContentIntoFile(componentIdTo, objectIdTo, fieldName, fieldContent, null);

          // paste translations
          Collection<String> languages = I18NHelper.getLanguages();
          for (final String language: languages) {
            if (fieldName.startsWith(language + "_")) {
              // skip en_
              fieldName = fieldName.substring(3);
              fieldContent = getContentFromFile(componentIdFrom, objectIdFrom, fieldName, language);
              setContentIntoFile(componentIdTo, objectIdTo, fieldName, fieldContent, language);
            }
          }

          FileUtils.deleteQuietly(file);
        }
      }
    }
  }

  public static String getFile(String componentId, String objectId, String fieldName,
      String language) {
    return getPath(componentId) + getFileName(fieldName, objectId, language);
  }

  /*
  * Remove content on disk of WYSIWYG fields in given language
  * @param pk the PK of contribution
  * @param fieldNames list of name of fields to delete
  * @param language the language to delete
  */
  public static void removeContents(ResourceReference pk, List<String> fieldNames, String language) {
    String fromPath = getPath(pk.getInstanceId());
    File directory = new File(fromPath);
    if (directory.exists()) {
      for (String fieldName : fieldNames) {
        String filePath = getFile(pk.getInstanceId(), pk.getId(), fieldName, language);
        File file = new File(filePath);
        if (file.exists()) {
          FileUtils.deleteQuietly(file);
        }
      }
    }
  }

  private static String getPath(String componentId) {
    String[] dirs = {DIRECTORYNAME};
    return FileRepositoryManager.getAbsolutePath(componentId, dirs);
  }

  private String getWysiwygConfigFile() {
    String configFile = settings.getString("configFile");
    if (!configFile.startsWith("/") && !configFile.toLowerCase().startsWith("http")) {
      configFile = URLUtil.getApplicationURL() + "/" + configFile;
    }
    return configFile;
  }

  private static boolean isDirectoryExists(String componentId) {
    String path = getPath(componentId);
    File directory = new File(path);
    return directory.exists();
  }
}