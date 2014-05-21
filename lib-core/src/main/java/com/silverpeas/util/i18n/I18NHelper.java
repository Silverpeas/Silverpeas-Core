/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.util.i18n;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class I18NHelper {

  // "fr" - List of I18NLanguage : all available languages in french
  // "en" - List of I18NLanguage : all available languages in english
  public final static Map<String, List<I18NLanguage>> allLanguages =
      new LinkedHashMap<String, List<I18NLanguage>>();

  private static int nbLanguages = 0;
  public static boolean isI18N = false;
  public static String defaultLanguage = null;
  public static Locale defaultLocale = Locale.getDefault();
  private final static List<String> allCodes = new ArrayList<String>();

  public static final String HTMLSelectObjectName = "I18NLanguage";
  public static final String HTMLHiddenRemovedTranslationMode = "TranslationRemoveIt";

  static {
    ResourceLocator rs = new ResourceLocator("org.silverpeas.util.i18n", "");
    String rsLanguages = rs.getString("languages");
    StringTokenizer tokenizer = new StringTokenizer(rsLanguages, ",");
    while (tokenizer.hasMoreTokens()) {
      String language = tokenizer.nextToken();
      allCodes.add(language);
      nbLanguages++;
      if (defaultLanguage == null) {
        defaultLanguage = language;
        defaultLocale = new Locale(language);
      }
      ResourceLocator rsLanguage = new ResourceLocator("org.silverpeas.util.multilang.i18n",
          language);

      StringTokenizer tokenizer2 = new StringTokenizer(rsLanguages, ",");
      List<I18NLanguage> l = new ArrayList<I18NLanguage>();
      while (tokenizer2.hasMoreTokens()) {
        String language2 = tokenizer2.nextToken();
        I18NLanguage i18nLanguage = new I18NLanguage(language2, rsLanguage.getString("language_"
            + language2));
        l.add(i18nLanguage);
      }
      allLanguages.put(language, l);
    }
    isI18N = (nbLanguages > 1);
  }

  public static String getLanguageLabel(String code, String userLanguage) {
    List<I18NLanguage> labels = allLanguages.get(userLanguage);
    for (I18NLanguage language : labels) {
      if (language.getCode().equalsIgnoreCase(code)) {
        return language.getLabel();
      }
    }
    return "";
  }

  public static List<I18NLanguage> getAllLanguages(String userLanguage) {
    return allLanguages.get(userLanguage);
  }

  public static Iterator<String> getLanguages() {
    return allLanguages.keySet().iterator();
  }

  public static Set<String> getAllSupportedLanguages() {
    return allLanguages.keySet();
  }

  public static int getNumberOfLanguages() {
    return allLanguages.size();
  }

  public static boolean isDefaultLanguage(String language) {
    if (StringUtil.isDefined(language)) {
      return defaultLanguage.equalsIgnoreCase(language);
    }
    return true;
  }

  public static String checkLanguage(String language) {
    String lang = language;
    if (!StringUtil.isDefined(language) || ! allCodes.contains(language)) {
      lang = defaultLanguage;
    }
    return lang;
  }

  public static String getHTMLLinks(String url, String currentLanguage) {
    if (!isI18N) {
      return "";
    }
    String baseUrl = url;
    if (url.contains("?")) {
      baseUrl = baseUrl + "&SwitchLanguage=";
    } else {
      baseUrl = baseUrl + "?SwitchLanguage=";
    }


    StringBuilder links = new StringBuilder(512);
    boolean first = true;
    for(String code : allCodes) {
      String className = "";
      String link = baseUrl + code;
      if (!first) {
        links.append("&nbsp;");
      }
      if (code.equals(currentLanguage)) {
        className = "ArrayNavigationOn";
      }
      links.append("<a href=\"").append(link).append("\" class=\"").append(className)
          .append("\" id=\"translation_").append(code).append("\">")
          .append(code.toUpperCase(defaultLocale)).append("</a>");
      first = false;
    }

    return links.toString();
  }

  public static String getHTMLLinks(List<String> languages, String currentLanguage) {
    if (!isI18N || languages == null) {
      return "";
    }

    StringBuilder links = new StringBuilder(512);
    String link = "";

    String begin = "";
    String end = "";

    boolean first = true;
    for(String code : allCodes) {
      String className = "";

      if (languages.contains(code)) {
        link = "javaScript:showTranslation('" + code + "');";
        if (!first) {
          links.append("&nbsp;");
        }

        if (code.equals(currentLanguage) || languages.size() == 1) {
          className = "ArrayNavigationOn";
        }

        links.append("<a href=\"").append(link).append("\" class=\"").append(className)
            .append("\" id=\"translation_").append(code).append("\">")
            .append(code.toUpperCase(defaultLocale)).append("</a>");
        first = false;
      }
    }
    return links.toString();
  }

  public static String getHTMLLinks(I18NBean bean, String currentLanguage) {
    String lang = currentLanguage;
    if (!isI18N || bean == null) {
      return "";
    }

    if (bean.getTranslation(lang) == null) {
      Translation translation = bean.getNextTranslation();
      if (translation != null) {
        lang = translation.getLanguage();
      }
    }
    List<String> languages = new ArrayList<String>(bean.getTranslations().keySet());
    return getHTMLLinks(languages, lang);
  }

  public static String getFormLine(ResourcesWrapper resources) {
    return getFormLine(resources, null, null);
  }

  public static String getFormLine(ResourcesWrapper resources, I18NBean bean, String translation) {
    if (nbLanguages == 1) {
      return "";
    }
    StringBuilder tr = new StringBuilder(50);
    tr.append("<tr>\n");
    tr.append("<td class=\"txtlibform\">").append(
        resources.getString("GML.language")).append(" :</td>\n");
    tr.append("<td>").append(getHTMLSelectObject(resources.getLanguage(), bean, translation)).
        append("</td>");
    tr.append("</tr>\n");
    return tr.toString();
  }

  public static String getHTMLSelectObject(String userLanguage, I18NBean bean,
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

   private static String getHTMLSelectObject(List<I18NLanguage> toDisplay, I18NBean bean,
      String selectedTranslation, String userLanguage) {
    String list = "";
    String currentTranslation = selectedTranslation;

    String onChangeJavascript = "";
    if (bean != null) {
      onChangeJavascript = "onChange= \"javaScript:showTranslation(this.value.substring(0,2));\"";
      if (bean.getTranslation(currentTranslation) == null) {
        Translation translation = bean.getNextTranslation();
        if (translation != null) {
          currentTranslation = translation.getLanguage();
        }
      }
    }
    list += "<SELECT name=\"" + HTMLSelectObjectName + "\" "
        + onChangeJavascript + ">\n";
    for (I18NLanguage language : toDisplay) {
      String selected = "";
      if (language.getCode().equals(currentTranslation)) {
        selected = "selected";
      }

      list += "<option value=\"" + language.getCode() + '_'
          + language.getTranslationId() + "\" " + selected + '>'
          + language.getLabel() + "</option>\n";
    }
    list += "</SELECT>";

    if (bean != null) {
      String path = URLManager.getApplicationURL();
      String text = GeneralPropertiesManager.getGeneralMultilang(userLanguage).getString(
          "GML.translationRemove");

      list += "&nbsp;<span id=\"delTranslationLink\">";
      if (bean.getTranslations().size() >= 2) {
        list += "<a href=\"javaScript:document.getElementById('"
            + HTMLHiddenRemovedTranslationMode
            + "').value='true';removeTranslation();\"><img src=\""
            + path
            + "/util/icons/delete.gif\" border=\"0\" valign=\"absmiddle\" title=\""
            + text + "\" alt=\"" + text + "\"></a>";
      }
      list += "</span>";

      list += "<input type=\"hidden\" id=\"" + HTMLHiddenRemovedTranslationMode
          + "\" name=\"" + HTMLHiddenRemovedTranslationMode
          + "\" value=\"false\">\n";
    }
    return list;
  }

  public static String updateHTMLLinks(I18NBean bean) {
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

  public static String[] getLanguageAndTranslationId(HttpServletRequest request) {
    String param = request.getParameter(HTMLSelectObjectName);
    return getLanguageAndTranslationId(param);
  }

  static private String[] getLanguageAndTranslationId(String param) {
    if (StringUtil.isDefined(param)) {
      StringTokenizer tokenizer = new StringTokenizer(param, "_");
      String language = tokenizer.nextToken();
      String translationId = tokenizer.nextToken();
      String[] result = {language, translationId};
      return result;
    }
    return null;
  }

  public static String getSelectedLanguage(HttpServletRequest request) {
    String[] param = getLanguageAndTranslationId(request);
    if (param != null) {
      return param[0];
    }
    return null;
  }

  public static boolean isI18nActivated() {
    return isI18N;
  }

  public static void setI18NInfo(I18NBean bean, HttpServletRequest request) {
    String languageAndTranslationId = request.getParameter(HTMLSelectObjectName);
    String removeTranslation = request.getParameter(HTMLHiddenRemovedTranslationMode);

    setI18NInfo(bean, languageAndTranslationId, removeTranslation);
  }

  public static void setI18NInfo(I18NBean bean, List<FileItem> parameters) {
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