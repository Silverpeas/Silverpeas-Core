/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.form.displayers;

import au.id.jericho.lib.html.Source;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.GalleryHelper;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.wysiwyg.dynamicvalue.control.DynamicValueReplacement;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A WysiwygFieldDisplayer is an object which can display a TextFiel in HTML the content of a
 * TextFiel to a end user and can retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class WysiwygFCKFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  public static final String dbKey = "xmlWysiwygField_";
  public static final String dir = "xmlWysiwyg";
  private static final ResourceLocator settings = new ResourceLocator(
      "com.stratelia.silverpeas.wysiwyg.settings.wysiwygSettings", "");

  /**
   * Constructeur
   */
  public WysiwygFCKFieldDisplayer() {
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    String[] s = new String[0];
    s[0] = TextField.TYPE;
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
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext PagesContext)
      throws java.io.IOException {
    String fieldName = template.getFieldName();
    String language = PagesContext.getLanguage();

    if (!TextField.TYPE.equals(template.getTypeName())) {
      SilverTrace.info("form", "WysiwygFCKFieldDisplayer.displayScripts",
          "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
    }

    if (!template.isReadOnly()) {
      out.println("var oEditor = FCKeditorAPI.GetInstance('" + fieldName + "');");
      out.println("var thecode = oEditor.GetHTML();");
      if (template.isMandatory() && PagesContext.useMandatory()) {
        out
            .println(
            "	if (isWhitespace(stripInitialWhitespace(thecode)) || thecode == \"<P>&nbsp;</P>\") {");
        out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' "
            + Util.getString("GML.MustBeFilled", language) + "\\n \";");
        out.println("		errorNb++;");
        out.println("	}");
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
   */
  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {

    String code = "";

    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());

    if (!field.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "WysiwygFCKFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (!field.isNull()) {
      code = field.getValue(pageContext.getLanguage());
    }

    String contentLanguage = I18NHelper.checkLanguage(pageContext.getContentLanguage());

    code =
        getContent(pageContext.getComponentId(), pageContext.getObjectId(),
        template.getFieldName(), code,
        contentLanguage);

    if (template.isDisabled() || template.isReadOnly()) {
      // dynamic value functionality
      if (DynamicValueReplacement.isActivate()) {
        DynamicValueReplacement replacement = new DynamicValueReplacement();
        code = replacement.replaceKeyByValue(code);
      }
      out.println(code);

    } else {
      out.println("<table>");
      // dynamic value functionality
      if (DynamicValueReplacement.isActivate()) {
        out.println("<tr class=\"TB_Expand\"> <td class=\"TB_Expand\" align=\"center\">");
        out.println(DynamicValueReplacement.buildHTMLSelect(pageContext.getLanguage(), fieldName));
        out.println("</td></tr>");
      }

      ResourceLocator resources = new ResourceLocator(
          "com.stratelia.silverpeas.wysiwyg.multilang.wysiwygBundle", contentLanguage);

      // storage file : HTML select building
      List<ComponentInstLight> fileStorage =
          WysiwygController.getStorageFile(pageContext.getUserId());
      if (!fileStorage.isEmpty()) {
        out.println("<tr class=\"TB_Expand\"><td class=\"TB_Expand\">");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<select id=\"storageFile_").append(fieldName).append(
            "\" name=\"componentId\" onchange=\"openStorageFilemanager").append(
            FileServerUtils.replaceAccentChars(fieldName.replace(' ', '_'))).append(
            "();this.selectedIndex=0\">");
        stringBuilder.append("<option value=\"\">").append(
            resources.getString("storageFile.select.title")).append("</option>");
        for (ComponentInstLight component : fileStorage) {
          stringBuilder.append("<option value=\"").append(component.getId()).append("\">").append(
              component.getLabel(contentLanguage)).append("</option>");
        }
        stringBuilder.append("</select>");
        out.println(stringBuilder.toString());
      }

      // Gallery file : HTML select building
      List<ComponentInstLight> galleries = WysiwygController.getGalleries();
      String fieldNameFunction = FileServerUtils.replaceAccentChars(fieldName.replace(' ', '_'));

      if (galleries != null && !galleries.isEmpty()) {
        if (fileStorage.isEmpty()) {
          out.println("<tr class=\"TB_Expand\"><td class=\"TB_Expand\">");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<select id=\"galleryFile_").append(fieldName).append(
            "\" name=\"componentId\" onchange=\"openGalleryFileManager").append(
            fieldNameFunction).append("();this.selectedIndex=0\">");
        stringBuilder.append("<option value=\"\">").append(
            Util.getString("GML.galleries", contentLanguage)).append("</option>");
        for (ComponentInstLight component : galleries) {
          stringBuilder.append("<option value=\"").append(component.getId()).append("\">").append(
              component.getLabel(contentLanguage)).append("</option>");
        }
        stringBuilder.append("</select>");
        out.println(stringBuilder.toString());
      }

      if (!fileStorage.isEmpty() || (galleries != null && !galleries.isEmpty())) {
        out.println("</td></tr>");
      }

      out.println("<tr>");

      // looks for size parameters
      int editorWitdh = 500;
      int editorHeight = 300;
      if (parameters.containsKey("width")) {
        editorWitdh = Integer.parseInt(parameters.get("width"));
      }
      if (parameters.containsKey("height")) {
        editorHeight = Integer.parseInt(parameters.get("height"));
      }

      out.println("<td valign=\"top\">");
      out.println("<textarea id=\"" + fieldName + "\" name=\"" + fieldName
          + "\" rows=\"10\" cols=\"10\">" + code + "</textarea>");
      out.println("<script type=\"text/javascript\">");
      out.println("var oFCKeditor = new FCKeditor('" + fieldName + "');");
      out.println("oFCKeditor.Width = \"" + editorWitdh + "\";");
      out.println("oFCKeditor.Height = \"" + editorHeight + "\";");
      out.println("oFCKeditor.BasePath = \"" + Util.getPath() + "/wysiwyg/jsp/FCKeditor/\" ;");
      out.println("oFCKeditor.DisplayErrors = true;");
      out.println("oFCKeditor.Config[\"DefaultLanguage\"] = \"" + pageContext.getLanguage()
          + "\";");
      String configFile = settings.getString("configFile",
          Util.getPath() + "/wysiwyg/jsp/javaScript/myconfig.js");
      out.println("oFCKeditor.Config[\"CustomConfigurationsPath\"] = \"" + configFile + "\";");
      out.println("oFCKeditor.ToolbarSet = 'XMLForm';");
      out.println("oFCKeditor.Config[\"ToolbarStartExpanded\"] = false;");
      out.println("oFCKeditor.Config[\"ImageBrowserURL\"] = '" + Util.getPath()
          + "/wysiwyg/jsp/uploadFile.jsp?ComponentId=" + pageContext.getComponentId() +
          "&ObjectId="
          + pageContext.getObjectId() + "&Context=" + fieldName + "';");
      out.println("oFCKeditor.ReplaceTextarea();");

      // field name used to generate a javascript function name
      // dynamic value functionality
      if (DynamicValueReplacement.isActivate()) {

        out.println("function chooseDynamicValues"
            + fieldNameFunction + "(){");
        out.println(" var oEditor = FCKeditorAPI.GetInstance('" + fieldName + "');");
        out.println("oEditor.Focus();");
        out.println("index = document.getElementById(\"dynamicValues_" + fieldName
            + "\").selectedIndex;");
        out.println("var str = document.getElementById(\"dynamicValues_" + fieldName
            + "\").options[index].value;");
        out.println("if (index != 0 && str != null){");
        out.println("oEditor.InsertHtml('(%'+str+'%)');");
        out.println("} }");
      }

      // storage file : javascript functions
      out.println("var storageFileWindow=window;");
      out.println("function openStorageFilemanager" + fieldNameFunction + "(){");
      out.println("index = document.getElementById(\"storageFile_" + fieldName
          + "\").selectedIndex;");
      out.println("var componentId = document.getElementById(\"storageFile_" + fieldName
          + "\").options[index].value;");
      out.println("if (index != 0){  ");
      out
          .println("url = \""
              +
              URLManager.getApplicationURL()
              +
              "/kmelia/jsp/attachmentLinkManagement.jsp?key=\"+componentId+\"&ntype=COMPONENT&fieldname="
              + fieldNameFunction + "\";");
      out.println("windowName = \"StorageFileWindow\";");
      out.println("width = \"750\";");
      out.println("height = \"580\";");
      out
          .println("windowParams = \"scrollbars=1,directories=0,menubar=0,toolbar=0, alwaysRaised\";");
      out.println("if (!storageFileWindow.closed && storageFileWindow.name==windowName)");
      out.println("storageFileWindow.close();");
      out
          .println("storageFileWindow = SP_openWindow(url, windowName, width, height, windowParams);");
      out.println("}}");

      out.println("function insertAttachmentLink" + fieldNameFunction + "(url,img,label){");
      out.println(" var oEditor = FCKeditorAPI.GetInstance('" + fieldName + "');");
      out.println("oEditor.Focus();");
      out
          .println(
          "oEditor.InsertHtml('<a href=\"'+url+'\"> <img src=\"'+img+'\" width=\"20\" border=\"0\" alt=\"\"/> '+label+'</a> ');");
      out.println("}");

      // Gallery files exists; javascript functions
      if (galleries != null && !galleries.isEmpty()) {
        GalleryHelper.getJavaScript(fieldNameFunction, fieldName, contentLanguage, out);

        out.println("function choixImageInGallery" + fieldNameFunction + "(url){");
        out.println(" var oEditor = FCKeditorAPI.GetInstance('" + fieldName + "');");
        out.println("oEditor.Focus();");
        out.println("oEditor.InsertHtml('<img src=\"'+url+'\" border=\"0\" alt=\"\"/>');");
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
   * the request. @throw FormException if the field type is not a managed type. @throw FormException
   * if the field doesn't accept the new value.
   */
  @Override
  public List<String> update(String newValue, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    if (!field.getTypeName().equals(TextField.TYPE)) {
      throw new FormException("WysiwygFCKFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (field.acceptValue(newValue, pageContext.getLanguage())) {
      // field.setValue(newValue, PagesContext.getLanguage());
      try {
        String contentLanguage = I18NHelper.checkLanguage(pageContext.getContentLanguage());

        String fileName =
            setContentIntoFile(pageContext.getComponentId(), pageContext.getObjectId(),
            template.getFieldName(), newValue, contentLanguage);

        field.setValue(dbKey + fileName, pageContext.getLanguage());
      } catch (Exception e) {
        throw new FormException("WysiwygFCKFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE", e);
      }
    } else {
      throw new FormException("WysiwygFCKFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          TextField.TYPE);
    }
    return new ArrayList<String>();

  }

  public boolean isDisplayedMandatory() {
    return true;
  }

  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

  @Override
  public void index(FullIndexEntry indexEntry, String key, String fieldName, TextField field,
      String language) {
    String fieldValue = field.getValue();
    String fieldValueIndex = "";
    if (fieldValue != null && fieldValue.trim().length() > 0) {
      if (fieldValue.startsWith(dbKey)) {
        String file =
            WysiwygFCKFieldDisplayer.getFile(indexEntry.getComponent(), indexEntry.getObjectId(),
            fieldName,
            language);
        try {
          Source source = new Source(new FileInputStream(file));
          if (source != null) {
            fieldValueIndex = source.getTextExtractor().toString();
          }
        } catch (IOException ioex) {
          SilverTrace.warn("form", "WysiwygFCKFieldDisplayer.index", "form.incorrect_data",
              "File not found " + file + " " + ioex.getMessage(), ioex);
        }
        indexEntry.addTextContent(fieldValueIndex, language);
      } else {
        indexEntry.addTextContent(fieldValue.trim(), language);
        fieldValueIndex = fieldValue.trim().replaceAll("##", " ");
      }
      indexEntry.addField(key, fieldValueIndex, language);

      // index embedded linked attachment (links presents in wysiwyg content)
      try {
        String content = WysiwygFCKFieldDisplayer.getContentFromFile(indexEntry.getComponent(),
            indexEntry.getObjectId(), fieldName, language);
        List<String> embeddedAttachmentIds = WysiwygController.getEmbeddedAttachmentIds(content);
        WysiwygController.indexEmbeddedLinkedFiles(indexEntry, embeddedAttachmentIds);
      } catch (UtilException e) {
        SilverTrace.warn("form", "WysiwygFCKFieldDisplayer.index", "form.incorrect_data",
            "Unable to extract linkes files from object" + indexEntry.getObjectId(), e);
      } catch (WysiwygException e) {
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
    } catch (Exception e) {
    }

    String[] dirs = new String[1];
    dirs[0] = dir;
    String path = FileRepositoryManager.getAbsolutePath(componentId, dirs);

    String fileName = getFileName(fieldName, objectId, language);

    try {
      FileFolderManager.createFile(path, fileName, code);
    } catch (UtilException e) {
      // do nothinf
    }
    return fileName;
  }

  public static String getContentFromFile(String componentId, String objectId, String fieldName)
      throws UtilException {
    return getContentFromFile(componentId, objectId, fieldName, null);
  }

  public static String getContentFromFile(String componentId, String objectId, String fieldName,
      String language) throws
      UtilException {
    String fileName = getFileName(fieldName, objectId, language);

    String[] dirs = new String[1];
    dirs[0] = dir;
    String path = FileRepositoryManager.getAbsolutePath(componentId, dirs);

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

  public void cloneContents(String componentIdFrom, String objectIdFrom, String componentIdTo,
      String objectIdTo) throws
      UtilException, IOException {
    String[] dirs = new String[1];
    dirs[0] = dir;
    String fromPath = FileRepositoryManager.getAbsolutePath(componentIdFrom, dirs);
    String toPath = FileRepositoryManager.getAbsolutePath(componentIdTo, dirs);

    File from = new File(fromPath);
    if (from != null && from.exists()) {
      try {
        FileRepositoryManager.createAbsolutePath(componentIdTo, dir);
      } catch (Exception e) {
        throw new IOException(e.getMessage());
      }
      List<File> files = (List<File>) FileFolderManager.getAllFile(fromPath);
      for (File file : files) {
        String fileName = file.getName();
        if (fileName.startsWith(objectIdFrom + "_")) {
          String fieldName = fileName.substring(objectIdFrom.length() + 1);
          FileRepositoryManager.copyFile(fromPath + file.getName(), toPath
              + getFileName(fieldName, objectIdTo));
          Iterator<String> languages = I18NHelper.getLanguages();
          while (languages.hasNext()) {
            String language = languages.next();

            if (fieldName.startsWith(language + "_")) {
              fieldName = fieldName.substring(3); // skip en_
              FileRepositoryManager.copyFile(fromPath + file.getName(), toPath
                  + getFileName(fieldName, objectIdTo,
                  language));
            }
          }
        }
      }
    }
  }

  public void mergeContents(String componentIdFrom, String objectIdFrom, String componentIdTo,
      String objectIdTo) throws UtilException, IOException {
    String[] dirs = new String[1];
    dirs[0] = dir;
    String fromPath = FileRepositoryManager.getAbsolutePath(componentIdFrom, dirs);

    File from = new File(fromPath);
    if (from != null && from.exists()) {
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
    String[] dirs = new String[1];
    dirs[0] = dir;
    String path = FileRepositoryManager.getAbsolutePath(componentId, dirs);

    return path + getFileName(fieldName, objectId, language);
  }
}
