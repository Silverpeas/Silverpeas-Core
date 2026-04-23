/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.i18n;

import jakarta.servlet.http.HttpServletRequest;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileItem;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A helper class about i18n properties as defined with the {@link I18n} bean. It allows unmanaged
 * beans to statically access some of the {@link I18n} methods.
 */
public final class I18NHelper {

  public static final String HTML_SELECT_OBJECT_NAME = "I18NLanguage";
  public static final String HTML_HIDDEN_REMOVED_TRANSLATION_MODE = "TranslationRemoveIt";

  private final I18n i18n = I18n.get();

  private static I18NHelper instance;

  private static I18NHelper getInstance() {
    if (instance == null) {
      instance = new I18NHelper();
    }
    return instance;
  }

  public static int getNumberOfLanguages() {
    return getInstance().i18n.getSupportedLanguageCodes().size();
  }

  public static String checkLanguage(String language) {
    return getInstance().i18n.checkLanguage(language);
  }

  public static boolean isDefaultLanguage(String language) {
    return getInstance().i18n.isDefaultLanguage(language);
  }

  public static boolean isI18nContentActivated() {
    return getInstance().i18n.isEnabled();
  }

  private List<String> getSupportedLanguageCodes() {
    return i18n.getSupportedLanguageCodes();
  }

  private boolean isNotEnabled() {
    return !i18n.isEnabled();
  }

  public static String getLanguageLabel(String code, String userLanguage) {
    List<I18NLanguage> labels = getAllUserTranslationsOfContentLanguages(userLanguage);
    for (I18NLanguage language : labels) {
      if (language.getCode().equalsIgnoreCase(code)) {
        return language.getLabel();
      }
    }
    return "";
  }

  public static Locale getDefaultLocale() {
    return new Locale(getDefaultLanguage());
  }

  public static String getDefaultLanguage() {
    return getInstance().i18n.getDefaultLanguage();
  }

  public static List<String> getAllSupportedLanguages() {
    return getInstance().getSupportedLanguageCodes();
  }

  public static String getHTMLLinks(String url, String currentLanguage) {
    I18NHelper helper = getInstance();
    if (helper.isNotEnabled()) {
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
    for (String code : helper.getSupportedLanguageCodes()) {
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
          .append(code.toUpperCase(getDefaultLocale())).append("</a>");
      first = false;
    }

    return links.toString();
  }

  public static String getHTMLLinks(List<String> languages, String currentLanguage) {
    I18NHelper helper = getInstance();
    if (helper.isNotEnabled() || languages == null) {
      return "";
    }

    StringBuilder links = new StringBuilder(512);
    String link;
    boolean first = true;
    for (String code : helper.getSupportedLanguageCodes()) {
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
            .append(code.toUpperCase(getDefaultLocale())).append("</a>");
        first = false;
      }
    }
    return links.toString();
  }

  public static String getHTMLLinks(I18NBean<?> bean, String currentLanguage) {
    String lang = currentLanguage;
    I18NHelper helper = getInstance();
    if (helper.isNotEnabled() || bean == null) {
      return "";
    }

    if (bean.getTranslations().get(lang) == null) {
      Translation translation = bean.getNextTranslation();
      if (translation != null) {
        lang = translation.getLanguage();
      }
    }
    List<String> languages = new ArrayList<>(bean.getTranslations().keySet());
    return getHTMLLinks(languages, lang);
  }

  public static String getFormLine(MultiSilverpeasBundle resources) {
    return getFormLine(resources, null, null);
  }

  public static String getFormLine(MultiSilverpeasBundle resources, I18NBean<?> bean,
      String translation) {
    I18NHelper helper = getInstance();
    if (helper.getSupportedLanguageCodes().size() == 1) {
      return "";
    }
    return "<tr>\n" +
        "<td class=\"txtlibform\">" +
        resources.getString("GML.language") + " :</td>\n" +
        "<td>" + getHTMLSelectObject(resources.getLanguage(), bean, translation) +
        "</td>" +
        "</tr>\n";
  }

  public static String getHTMLSelectObject(String userLanguage, I18NBean<?> bean,
      String selectedTranslation) {
    List<I18NLanguage> languages = getAllUserTranslationsOfContentLanguages(userLanguage);

    List<I18NLanguage> result = new ArrayList<>();
    for (I18NLanguage lang : languages) {
      I18NLanguage newLang = new I18NLanguage(lang.getCode(), lang.getLabel());
      if (bean != null) {
        BeanTranslation translation =
            (BeanTranslation) bean.getTranslations().get(newLang.getCode());
        if (translation != null) {
          newLang.setTranslationId(translation.getId());
        }
      }
      result.add(newLang);
    }
    return getHTMLSelectObject(result, bean, selectedTranslation, userLanguage);
  }

  public static List<I18NLanguage> getAllUserTranslationsOfContentLanguages(String userLanguage) {
    return getInstance().i18n.getSupportedLanguages(userLanguage).stream()
        .map(l -> new I18NLanguage(l.getCode(), l.getName()))
        .collect(Collectors.toList());
  }

  private static String getHTMLSelectObject(List<I18NLanguage> toDisplay, I18NBean<?> bean,
      String selectedTranslation, String userLanguage) {
    StringBuilder list = new StringBuilder();
    String currentTranslation = selectedTranslation;

    String onChangeJavascript = "";
    if (bean != null) {
      onChangeJavascript = "onChange= \"javaScript:showTranslation(this.value.substring(0,2));\"";
      if (currentTranslation == null || bean.getTranslations().get(currentTranslation) == null) {
        Translation translation = bean.getNextTranslation();
        if (translation != null) {
          currentTranslation = translation.getLanguage();
        }
      }
    }
    list.append("<SELECT name=\"")
        .append(HTML_SELECT_OBJECT_NAME)
        .append("\" ")
        .append(onChangeJavascript)
        .append(">\n");
    for (I18NLanguage language : toDisplay) {
      String selected = "";
      if (language.getCode().equals(currentTranslation)) {
        selected = "selected";
      }

      list.append("<option value=\"")
          .append(language.getCode())
          .append('_')
          .append(language.getTranslationId())
          .append("\" ")
          .append(selected)
          .append('>')
          .append(language.getLabel())
          .append("</option>\n");
    }
    list.append("</SELECT>");

    if (bean != null) {
      String path = URLUtil.getApplicationURL();
      String text = ResourceLocator.getGeneralLocalizationBundle(userLanguage).getString(
          "GML.translationRemove");

      list.append("&nbsp;<span id=\"delTranslationLink\">");
      if (bean.getTranslations().size() >= 2) {
        list.append("<a href=\"javaScript:document.getElementById('")
            .append(HTML_HIDDEN_REMOVED_TRANSLATION_MODE)
            .append("').value='true';removeTranslation();\"><img src=\"")
            .append(path)
            .append("/util/icons/delete.gif\" border=\"0\" valign=\"absmiddle\" title=\"")
            .append(text)
            .append("\" alt=\"")
            .append(text)
            .append("\"></a>");
      }
      list.append("</span>");

      list.append("<input type=\"hidden\" id=\"")
          .append(HTML_HIDDEN_REMOVED_TRANSLATION_MODE)
          .append("\" name=\"")
          .append(HTML_HIDDEN_REMOVED_TRANSLATION_MODE)
          .append("\" value=\"false\">\n");
    }
    return list.toString();
  }

  public static String updateHTMLLinks(I18NBean<?> bean) {
    StringBuilder javaScript = new StringBuilder();
    Set<String> codes = bean.getTranslations().keySet();
    for (String lang : codes) {
      javaScript.append("document.getElementById(\"translation_")
          .append(lang)
          .append("\").className = \"\";\n");
      javaScript.append("if (lang == '").append(lang).append("')\n");
      javaScript.append("{\n");
      javaScript.append("document.getElementById(\"translation_")
          .append(lang)
          .append("\").className = \"ArrayNavigationOn\";\n");
      javaScript.append("}\n");
    }
    return javaScript.toString();
  }

  public static String[] getLanguageAndTranslationId(HttpServletRequest request) {
    String param = request.getParameter(HTML_SELECT_OBJECT_NAME);
    return getLanguageAndTranslationId(param);
  }

  private static String[] getLanguageAndTranslationId(String param) {
    if (StringUtil.isDefined(param)) {
      StringTokenizer tokenizer = new StringTokenizer(param, "_");
      String language = tokenizer.nextToken();
      String translationId = tokenizer.nextToken();
      return new String[]{language, translationId};
    }
    return new String[0];
  }

  public static String getSelectedContentLanguage(HttpServletRequest request) {
    String[] param = getLanguageAndTranslationId(request);
    if (param.length > 0) {
      return param[0];
    }
    return null;
  }

  public static void setI18NInfo(I18NBean<?> bean, HttpServletRequest request) {
    String languageAndTranslationId = request.getParameter(HTML_SELECT_OBJECT_NAME);
    String removeTranslation = request.getParameter(HTML_HIDDEN_REMOVED_TRANSLATION_MODE);

    setI18NInfo(bean, languageAndTranslationId, removeTranslation);
  }

  public static void setI18NInfo(I18NBean<?> bean, List<FileItem> parameters) {
    String languageAndTranslationId = FileUploadUtil.getParameter(parameters,
        HTML_SELECT_OBJECT_NAME);
    String removeTranslation = FileUploadUtil.getParameter(parameters,
        HTML_HIDDEN_REMOVED_TRANSLATION_MODE);

    setI18NInfo(bean, languageAndTranslationId, removeTranslation);
  }

  private static void setI18NInfo(I18NBean<?> bean, String param,
      String removeParam) {
    String[] languageAndTranslationId = getLanguageAndTranslationId(param);
    if (languageAndTranslationId.length > 0) {
      String language = languageAndTranslationId[0];
      String translationId = languageAndTranslationId[1];

      bean.setLanguage(language);
      bean.setTranslationId(translationId);

      // check if translation must be removed
      bean.setRemoveTranslation("true".equalsIgnoreCase(removeParam));
    }
  }
}