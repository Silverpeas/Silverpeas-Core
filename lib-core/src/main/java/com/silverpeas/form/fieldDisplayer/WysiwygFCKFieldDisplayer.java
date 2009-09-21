package com.silverpeas.form.fieldDisplayer;

import au.id.jericho.lib.html.Source;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;

/**
 * A WysiwygFieldDisplayer is an object which can display a TextFiel in HTML
 * the content of a TextFiel to a end user
 * and can retrieve via HTTP any updated value.
 * 
 * 
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class WysiwygFCKFieldDisplayer extends AbstractFieldDisplayer {

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
   * Prints the javascripts which will be used to control
   * the new value given to the named field.
   *
   * The error messages may be adapted to a local language.
   * The FieldTemplate gives the field type and constraints.
   * The FieldTemplate gives the local labeld too.
   *
   * Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI> the fieldName is unknown by the template.
   * <LI> the field type is not a managed type.
   * </UL>
   */
  public void displayScripts(PrintWriter out,
      FieldTemplate template,
      PagesContext PagesContext) throws java.io.IOException {

    String fieldName = template.getFieldName();
    String language = PagesContext.getLanguage();

    if (!template.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "WysiwygFCKFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
    }

    out.println("var oEditor;");

    out.println("oEditor = FCKeditorAPI.GetInstance('" + fieldName + "');");

    out.println("var thecode = oEditor.GetHTML();");

    if (template.isMandatory() && PagesContext.useMandatory()) {
      out.println("	if (isWhitespace(stripInitialWhitespace(thecode)) || thecode == \"<P>&nbsp;</P>\") {");
      out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' " + Util.getString("GML.MustBeFilled",
          language) + "\\n \";");
      out.println("		errorNb++;");
      out.println("	}");
    }

    Util.getJavascriptChecker(template.getFieldName(), PagesContext, out);
  }

  /**
   * Prints the HTML value of the field.
   * The displayed value must be updatable by the end user.
   *
   * The value format may be adapted to a local language.
   * The fieldName must be used to name the html form input.
   *
   * Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI> the field type is not a managed type.
   * </UL>
   */
  public void display(PrintWriter out,
      Field field,
      FieldTemplate template,
      PagesContext pageContext) throws FormException {

    String code = "";

    String fieldName = template.getFieldName();
    if (!field.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "WysiwygFCKFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
    }

    if (!field.isNull()) {
      code = field.getValue(pageContext.getLanguage());
    }

    String contentLanguage = I18NHelper.checkLanguage(pageContext.getContentLanguage());

    code = getContent(pageContext.getComponentId(), pageContext.getObjectId(), template.getFieldName(), code,
        contentLanguage);

    if (template.isDisabled() || template.isReadOnly()) {
      out.println(code);
    } else {
      out.println("<TABLE>");

      out.println("<TR>");

      out.println("<TD valign=top>");
      out.println("<textarea id=\"" + fieldName + "\" name=\"" + fieldName + "\">" + code + "</textarea>");
      out.println("<script language=\"JavaScript\">");
      out.println("var oFCKeditor = new FCKeditor('" + fieldName + "');");
      out.println("oFCKeditor.Width = \"500\";");
      out.println("oFCKeditor.Height = \"300\";");
      out.println("oFCKeditor.BasePath = \"" + Util.getPath() + "/wysiwyg/jsp/FCKeditor/\" ;");
      out.println("oFCKeditor.DisplayErrors = true;");
      out.println("oFCKeditor.Config[\"DefaultLanguage\"] = \"" + pageContext.getLanguage() + "\";");
      String configFile = SilverpeasSettings.readString(settings, "configFile",
          Util.getPath() + "/wysiwyg/jsp/javaScript/myconfig.js");
      out.println("oFCKeditor.Config[\"CustomConfigurationsPath\"] = \"" + configFile + "\";");
      out.println("oFCKeditor.ToolbarSet = 'XMLForm';");
      out.println("oFCKeditor.Config[\"ToolbarStartExpanded\"] = false;");
      out.println("oFCKeditor.ReplaceTextarea();");
      out.println("</script>");

      if (template.isMandatory() && pageContext.useMandatory()) {
        out.println(Util.getMandatorySnippet());
      }

      out.println("</TD>");
      out.println("</TR>");
      out.println("</TABLE>");

    }
  }

  /**
   * Updates the value of the field.
   *
   * The fieldName must be used to retrieve the HTTP parameter from the request.
   *
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  public void update(String newValue, Field field, FieldTemplate template, PagesContext pageContext) throws
      FormException {
    if (!field.getTypeName().equals(TextField.TYPE)) {
      throw new FormException("WysiwygFCKFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE", TextField.TYPE);
    }

    if (field.acceptValue(newValue, pageContext.getLanguage())) {
      //field.setValue(newValue, PagesContext.getLanguage());
      try {
        String contentLanguage = I18NHelper.checkLanguage(pageContext.getContentLanguage());

        String fileName = setContentIntoFile(pageContext.getComponentId(), pageContext.getObjectId(), template.
            getFieldName(), newValue, contentLanguage);

        field.setValue(dbKey + fileName, pageContext.getLanguage());
      } catch (Exception e) {
        throw new FormException("WysiwygFCKFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE", e);
      }
    } else {
      throw new FormException("WysiwygFCKFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE", TextField.TYPE);
    }

  }

  public boolean isDisplayedMandatory() {
    return true;
  }

  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 2;
  }

  @Override
  public void index(FullIndexEntry indexEntry, String key, String fieldName, Field field, String language) {
    String fieldValue = field.getValue();
    String fieldValueIndex = "";
    if (fieldValue != null && fieldValue.trim().length() > 0) {
      if (fieldValue.startsWith(dbKey)) {
        String file = WysiwygFCKFieldDisplayer.getFile(indexEntry.getComponent(), indexEntry.getObjectId(), fieldName,
            language);
        Source source = new Source(file);
        if (source != null) {
          fieldValueIndex = source.getTextExtractor().toString();
        }
        indexEntry.addTextContent(fieldValueIndex, language);
      } else {
        indexEntry.addTextContent(fieldValue.trim(), language);
        fieldValueIndex = fieldValue.trim().replaceAll("##", " ");
      }
      indexEntry.addField(key, fieldValueIndex, language);
    }
  }

  private String getContent(String componentId, String objectId, String fieldName, String code, String language) throws
      FormException {
    if (!code.startsWith(dbKey)) {
      //Reprise de données
      //Création d'un fichier contenant le texte
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

  private String setContentIntoFile(String componentId, String objectId, String fieldName, String code, String language) {
    //Création d'un fichier contenant le texte
    try {
      FileRepositoryManager.createAbsolutePath(componentId, dir);
    } catch (Exception e) {
      //do nothing
    }

    String[] dirs = new String[1];
    dirs[0] = dir;
    String path = FileRepositoryManager.getAbsolutePath(componentId, dirs);

    String fileName = getFileName(fieldName, objectId, language);

    try {
      FileFolderManager.createFile(path, fileName, code);
    } catch (UtilException e) {
      //do nothinf
    }
    return fileName;
  }

  public static String getContentFromFile(String componentId, String objectId, String fieldName) throws UtilException {
    return getContentFromFile(componentId, objectId, fieldName, null);
  }

  public static String getContentFromFile(String componentId, String objectId, String fieldName, String language) throws
      UtilException {
    String fileName = getFileName(fieldName, objectId, language);

    String[] dirs = new String[1];
    dirs[0] = dir;
    String path = FileRepositoryManager.getAbsolutePath(componentId, dirs);

    return FileFolderManager.getCode(path, fileName);
  }

  private static String getFileName(String fieldName, String objectId) {
    //return objectId+"_"+fieldName;
    return getFileName(fieldName, objectId, null);
  }

  private static String getFileName(String fieldName, String objectId, String language) {
    if (language == null || I18NHelper.isDefaultLanguage(language)) {
      return objectId + "_" + fieldName;
    } else {
      return objectId + "_" + language + "_" + fieldName;
    }
  }

  public void cloneContents(String componentIdFrom, String objectIdFrom, String componentIdTo, String objectIdTo) throws
      UtilException, IOException {
    String[] dirs = new String[1];
    dirs[0] = dir;
    String fromPath = FileRepositoryManager.getAbsolutePath(componentIdFrom, dirs);
    String toPath = FileRepositoryManager.getAbsolutePath(componentIdTo, dirs);

    File from = new File(fromPath);
    if (from != null && from.exists()) {
      //Verifie si le repertoire de destination existe
      try {
        FileRepositoryManager.createAbsolutePath(componentIdTo, dir);
      } catch (Exception e) {
        throw new IOException(e.getMessage());
      }

      //Copier/coller de tous les fichiers wysiwyg de objectIdFrom vers objectIdTo
      List files = (List) FileFolderManager.getAllFile(fromPath);
      for (int f = 0; f < files.size(); f++) {
        File file = (File) files.get(f);
        String fileName = file.getName();
        if (fileName.startsWith(objectIdFrom + "_")) {
          String fieldName = fileName.substring(objectIdFrom.length() + 1);
          FileRepositoryManager.copyFile(fromPath + file.getName(), toPath + getFileName(fieldName, objectIdTo));

          //paste translations
          Iterator languages = I18NHelper.getLanguages();
          while (languages.hasNext()) {
            String language = (String) languages.next();

            if (fieldName.startsWith(language + "_")) {
              fieldName = fieldName.substring(3); //skip en_
              FileRepositoryManager.copyFile(fromPath + file.getName(), toPath + getFileName(fieldName, objectIdTo,
                  language));
            }
          }
        }
      }
    }
  }

  public static String getFile(String componentId, String objectId, String fieldName, String language) {
    String[] dirs = new String[1];
    dirs[0] = dir;
    String path = FileRepositoryManager.getAbsolutePath(componentId, dirs);

    return path + getFileName(fieldName, objectId, language);
  }
}
