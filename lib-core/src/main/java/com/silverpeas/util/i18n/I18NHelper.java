package com.silverpeas.util.i18n;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class I18NHelper {

  // "fr" - List of I18NLanguage : all available languages in french
  // "en" - List of I18NLanguage : all available languages in english
  public static Hashtable<String, List<I18NLanguage>> allLanguages = new Hashtable<String, List<I18NLanguage>>();

  private static int nbLanguages = 0;
  public static boolean isI18N = false;
  public static String defaultLanguage = null;
  private static List<String> allCodes = new ArrayList<String>();

  public static final String HTMLSelectObjectName = "I18NLanguage";
  public static final String HTMLHiddenRemovedTranslationMode = "TranslationRemoveIt";
  public static final String HTMLLink1 = "View";
  public static final String HTMLLink2 = "Translation?Code=";

  static {
    ResourceLocator rs = new ResourceLocator("com.silverpeas.util.i18n", "");

    String rsLanguages = rs.getString("languages");

    StringTokenizer tokenizer = new StringTokenizer(rsLanguages, ",");
    while (tokenizer.hasMoreTokens()) {
      String language = tokenizer.nextToken();
      allCodes.add(language);
      nbLanguages++;

      if (defaultLanguage == null)
        defaultLanguage = language;

      ResourceLocator rsLanguage = new ResourceLocator(
          "com.silverpeas.util.multilang.i18n", language);

      StringTokenizer tokenizer2 = new StringTokenizer(rsLanguages, ",");
      List<I18NLanguage> l = new ArrayList<I18NLanguage>();
      while (tokenizer2.hasMoreTokens()) {
        String language2 = tokenizer2.nextToken();
        I18NLanguage i18nLanguage = new I18NLanguage(language2, rsLanguage
            .getString("language_" + language2));
        l.add(i18nLanguage);
      }
      allLanguages.put(language, l);
    }
    isI18N = (nbLanguages > 1);
  }

  static public String getLanguageLabel(String code, String userLanguage) {
    List<I18NLanguage> labels = allLanguages.get(userLanguage);
    for (int l = 0; l < labels.size(); l++) {
      I18NLanguage language = (I18NLanguage) labels.get(l);
      if (language.getCode().equalsIgnoreCase(code))
        return language.getLabel();
    }
    return "";
  }

  static private List<I18NLanguage> getAllLanguages(String userLanguage) {
    return allLanguages.get(userLanguage);
  }

  static public Iterator<String> getLanguages() {
    return allLanguages.keySet().iterator();
  }

  static public int getNumberOfLanguages() {
    return allLanguages.size();
  }

  static public boolean isDefaultLanguage(String language) {
    if (StringUtil.isDefined(language))
      return defaultLanguage.equalsIgnoreCase(language);
    else
      return true;
  }

  static public String checkLanguage(String language) {
    if (!StringUtil.isDefined(language))
      language = defaultLanguage;

    return language;
  }

  static public String getHTMLLinks(String url, String currentLanguage) {
    if (!isI18N)
      return "";

    String links = "";
    String link = "";

    String begin = "";
    String end = "";

    Iterator<String> it = allCodes.iterator();
    int i = 0;
    while (it.hasNext()) {
      String code = (String) it.next();
      String className = "";
      if (url.indexOf("?") != -1)
        link = url + "&SwitchLanguage=" + code;
      else
        link = url + "?SwitchLanguage=" + code;
      if (i != 0)
        links += "&nbsp;";

      if (code.equals(currentLanguage))
        className = "ArrayNavigationOn";

      begin = "<a href=\"" + link + "\" class=\"" + className
          + "\" id=\"translation_" + code + "\">";
      end = "</a>";

      links += begin + code.toUpperCase() + end;
      i++;
    }

    return links;
  }

  static public String getHTMLLinks(List<String> languages, String currentLanguage) {
    if (!isI18N || languages == null)
      return "";

    String links = "";
    String link = "";

    String begin = "";
    String end = "";

    Iterator<String> it = allCodes.iterator();
    int i = 0;
    while (it.hasNext()) {
      String className = "";
      String code = (String) it.next();

      if (languages.contains(code)) {
        link = "javaScript:showTranslation('" + code + "');";
        if (i != 0)
          links += "&nbsp;";

        if (code.equals(currentLanguage) || languages.size() == 1)
          className = "ArrayNavigationOn";

        begin = "<a href=\"" + link + "\" class=\"" + className
            + "\" id=\"translation_" + code + "\">";
        end = "</a>";

        links += begin + code.toUpperCase() + end;
        i++;
      }
    }

    return links;
  }

  static public String getHTMLLinks(I18NBean bean, String currentLanguage) {
    if (!isI18N || bean == null)
      return "";

    if (bean.getTranslation(currentLanguage) == null) {
      Translation translation = bean.getNextTranslation();
      if (translation != null)
        currentLanguage = translation.getLanguage();
    }

    List<String> languages = new ArrayList<String>(bean.getTranslations().keySet());

    return getHTMLLinks(languages, currentLanguage);
  }

  static public String getFormLine(ResourcesWrapper resources) {
    return getFormLine(resources, null, null);
  }

  static public String getFormLine(ResourcesWrapper resources, I18NBean bean,
      String translation) {
    if (nbLanguages == 1)
      return "";

    StringBuffer tr = new StringBuffer(50);
    tr.append("<tr>\n");
    tr.append("<td class=\"txtlibform\">").append(
        resources.getString("GML.language")).append(" :</td>\n");
    tr.append("<td>").append(
        getHTMLSelectObject(resources.getLanguage(), bean, translation))
        .append("</td>");
    tr.append("</tr>\n");

    return tr.toString();
  }

  static private String getHTMLSelectObject(String userLanguage, I18NBean bean,
      String selectedTranslation) {
    List<I18NLanguage> languages = getAllLanguages(userLanguage);

    List<I18NLanguage> result = new ArrayList<I18NLanguage>();
    for (I18NLanguage lang : languages) {
      I18NLanguage newLang = new I18NLanguage(lang.getCode(), lang.getLabel());
      if (bean != null) {
        Translation translation = bean.getTranslation(newLang.getCode());
        if (translation != null) {
          newLang.setTranslationId(translation.getId());
        }
      }
      result.add(newLang);
    }
    return getHTMLSelectObject(result, bean, selectedTranslation, userLanguage);
  }

  static private String getHTMLSelectObject(List<I18NLanguage> toDisplay, I18NBean bean,
      String selectedTranslation, String userLanguage) {
    String list = "";

    String onChangeJavascript = "";
    if (bean != null) {
      onChangeJavascript = "onChange= \"javaScript:showTranslation(this.value.substring(0,2));\"";
      if (bean.getTranslation(selectedTranslation) == null) {
        Translation translation = bean.getNextTranslation();
        if (translation != null) {
          selectedTranslation = translation.getLanguage();
        }
      }
    }
    list += "<SELECT name=\"" + HTMLSelectObjectName + "\" "
        + onChangeJavascript + ">\n";
    for (int l = 0; l < toDisplay.size(); l++) {
      I18NLanguage language = toDisplay.get(l);

      String selected = "";
      if (language.getCode().equals(selectedTranslation))
        selected = "selected";

      list += "<option value=\"" + language.getCode() + "_"
          + language.getTranslationId() + "\" " + selected + ">"
          + language.getLabel() + "</option>\n";
    }
    list += "</SELECT>";

    if (bean != null) {
      String path = GeneralPropertiesManager.getGeneralResourceLocator()
          .getString("ApplicationURL");
      String text = GeneralPropertiesManager.getGeneralMultilang(userLanguage)
          .getString("GML.translationRemove");

      list += "&nbsp;<span id=\"delTranslationLink\">";
      if (bean.getTranslations().size() >= 2) // cannot remove last translation
        list += "<a href=\"javaScript:document.getElementById('"
            + HTMLHiddenRemovedTranslationMode
            + "').value='true';removeTranslation();\"><img src=\""
            + path
            + "/util/icons/delete.gif\" border=\"0\" valign=\"absmiddle\" title=\""
            + text + "\" alt=\"" + text + "\"></a>";
      list += "</span>";

      list += "<input type=\"hidden\" id=\"" + HTMLHiddenRemovedTranslationMode
          + "\" name=\"" + HTMLHiddenRemovedTranslationMode
          + "\" value=\"false\">\n";
    }
    return list;
  }

  static public String updateHTMLLinks(I18NBean bean) {
    String javaScript = "";

    Set<String> codes = bean.getTranslations().keySet();
    for (String lang : codes) {
      javaScript += "document.getElementById(\"translation_" + lang
          + "\").className = \"\";\n";
      javaScript += "if (lang == '" + lang + "')\n";
      javaScript += "{\n";
      javaScript += "document.getElementById(\"translation_" + lang
          + "\").className = \"ArrayNavigationOn\";\n";
      javaScript += "}\n";
    }
    return javaScript;
  }

  static public String[] getLanguageAndTranslationId(HttpServletRequest request) {
    String param = request.getParameter(HTMLSelectObjectName);
    return getLanguageAndTranslationId(param);
  }

  static private String[] getLanguageAndTranslationId(String param) {
    if (StringUtil.isDefined(param)) {
      StringTokenizer tokenizer = new StringTokenizer(param, "_");
      String language = tokenizer.nextToken();
      String translationId = tokenizer.nextToken();
      String[] result = { language, translationId };
      return result;
    }
    return null;
  }

  static public String getSelectedLanguage(HttpServletRequest request) {
    String[] param = getLanguageAndTranslationId(request);
    if (param != null) {
      return param[0];
    }
    return null;
  }

  static public void setI18NInfo(I18NBean bean, HttpServletRequest request) {
    String languageAndTranslationId = request
        .getParameter(HTMLSelectObjectName);
    String removeTranslation = request
        .getParameter(HTMLHiddenRemovedTranslationMode);

    setI18NInfo(bean, languageAndTranslationId, removeTranslation);
  }

  static public void setI18NInfo(I18NBean bean, List<FileItem> parameters) {
    String languageAndTranslationId = getParameterValue(parameters,
        HTMLSelectObjectName);
    String removeTranslation = getParameterValue(parameters,
        HTMLHiddenRemovedTranslationMode);

    setI18NInfo(bean, languageAndTranslationId, removeTranslation);
  }

  static private void setI18NInfo(I18NBean bean, String param,
      String removeParam) {
    String[] languageAndTranslationId = getLanguageAndTranslationId(param);

    if (languageAndTranslationId != null) {
      String language = languageAndTranslationId[0];
      String translationId = languageAndTranslationId[1];

      bean.setLanguage(language);
      bean.setTranslationId(translationId);

      String removeTranslation = removeParam;

      // check if translation must be removed
      bean.setRemoveTranslation("true".equalsIgnoreCase(removeTranslation));
    }
  }

  static private String getParameterValue(List<FileItem> items, String parameterName) {
    for (FileItem item : items) {
      if (item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item.getString();
      }
    }
    return null;
  }
}