/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.i18n;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class permits to manage the activated and displayed content languages.<br>
 * Be careful, this class handles possible content languages and not possible user languages.<br>
 * The different user languages are managed by {@link DisplayI18NHelper}.
 */
public class I18NHelper implements I18n {

  // "fr" - List of I18NLanguage : all available languages in french
  // "en" - List of I18NLanguage : all available languages in english
  private static final Map<String, List<I18NLanguage>> allContentLanguages = new LinkedHashMap<>();

  // The languages set for content writing are not necessarily the same set for user languages.
  // This fallback container permits to store the content language labels translated into a
  // language that is not managed by content language mechanism.
  // By this way, even if the user language does not correspond to a managed content language,
  // the labels are displayed according to the user language.
  private static final Map<String, List<I18NLanguage>> fallbackContentLanguages =
      new LinkedHashMap<>();

  private static int nbContentLanguages = 0;
  public static final boolean isI18nContentActivated;
  public static String defaultLanguage;
  public static Locale defaultLocale = Locale.getDefault();
  private static final List<String> allContentLanguageCodes = new ArrayList<>();

  public static final String HTMLSelectObjectName = "I18NLanguage";
  public static final String HTMLHiddenRemovedTranslationMode = "TranslationRemoveIt";

  static {
    SettingBundle rs = ResourceLocator.getSettingBundle("org.silverpeas.util.i18n");
    String[] rsLanguages = rs.getString("languages").split(",");
    for (String contentLanguageCode : rsLanguages) {
      contentLanguageCode = contentLanguageCode.trim();
      if (!contentLanguageCode.isEmpty()) {
        allContentLanguageCodes.add(contentLanguageCode);
        nbContentLanguages++;
        if (defaultLanguage == null) {
          defaultLanguage = contentLanguageCode;
          defaultLocale = new Locale(contentLanguageCode);
        }
        LocalizationBundle rsLanguage =
            ResourceLocator.getLocalizationBundle("org.silverpeas.util.multilang.i18n",
                contentLanguageCode);

        List<I18NLanguage> contentLanguageLabels = new ArrayList<>();
        for (String language : rsLanguages) {
          language = language.trim();
          if (!language.isEmpty()) {
            I18NLanguage i18nLanguage =
                new I18NLanguage(language, rsLanguage.getString("language_" + language));
            contentLanguageLabels.add(i18nLanguage);
          }
        }
        allContentLanguages.put(contentLanguageCode, contentLanguageLabels);
      }
    }

    isI18nContentActivated = (nbContentLanguages > 1);

    // Fallback languages
    List<String> fallbackLanguageCodes = new ArrayList<>(DisplayI18NHelper.getLanguages());
    fallbackLanguageCodes.removeAll(allContentLanguageCodes);
    for (String fallbackLanguageCode : fallbackLanguageCodes) {
      LocalizationBundle rsLanguage =
          ResourceLocator.getLocalizationBundle("org.silverpeas.util.multilang.i18n",
              fallbackLanguageCode);

      List<I18NLanguage> fallbackLanguageLabels = new ArrayList<>();
      for (String contentLanguageCode : allContentLanguageCodes) {
        I18NLanguage i18nLanguage = new I18NLanguage(contentLanguageCode,
            rsLanguage.getString("language_" + contentLanguageCode));
        fallbackLanguageLabels.add(i18nLanguage);
      }
      fallbackContentLanguages.put(fallbackLanguageCode, fallbackLanguageLabels);
    }
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

  /**
   * Gets all translations of enabled content languages according to the specified user language.
   * @param userLanguage the favorite language of a user.
   * @return the language labels with their code translated into the user favorite language.
   */
  public static List<I18NLanguage> getAllUserTranslationsOfContentLanguages(String userLanguage) {
    List<I18NLanguage> allContentLanguageUserTranslations = allContentLanguages.get(userLanguage);
    if (allContentLanguageUserTranslations == null) {
      // The user language is not one of the handled content languages. The labels to display are
      // retrieved from the fallback container.
      allContentLanguageUserTranslations = fallbackContentLanguages.get(userLanguage);
    }
    return allContentLanguageUserTranslations;
  }

  public static Collection<String> getLanguages() {
    return Collections.unmodifiableSet(allContentLanguages.keySet());
  }

  @Override
  public String getDefaultLanguage() {
    return I18NHelper.defaultLanguage;
  }

  @Override
  public Set<String> getSupportedLanguages() {
    return I18NHelper.getAllSupportedLanguages();
  }

  public static Set<String> getAllSupportedLanguages() {
    return allContentLanguages.keySet();
  }

  public static int getNumberOfLanguages() {
    return allContentLanguages.size();
  }

  public static boolean isDefaultLanguage(String language) {
    if (StringUtil.isDefined(language)) {
      return defaultLanguage.equalsIgnoreCase(language);
    }
    return true;
  }

  public static String checkLanguage(String language) {
    String lang = language;
    if (!StringUtil.isDefined(language) || !allContentLanguageCodes.contains(language)) {
      lang = defaultLanguage;
    }
    return lang;
  }

  public static String getHTMLLinks(String url, String currentLanguage) {
    if (!isI18nContentActivated) {
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
    for (String code : allContentLanguageCodes) {
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
    if (!isI18nContentActivated || languages == null) {
      return "";
    }

    StringBuilder links = new StringBuilder(512);
    String link;
    boolean first = true;
    for (String code : allContentLanguageCodes) {
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
    if (!isI18nContentActivated || bean == null) {
      return "";
    }

    if (bean.getTranslation(lang) == null) {
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

  public static String getFormLine(MultiSilverpeasBundle resources, I18NBean bean,
      String translation) {
    if (nbContentLanguages == 1) {
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
    List<I18NLanguage> languages = getAllUserTranslationsOfContentLanguages(userLanguage);

    List<I18NLanguage> result = new ArrayList<>();
    for (I18NLanguage lang : languages) {
      I18NLanguage newLang = new I18NLanguage(lang.getCode(), lang.getLabel());
      if (bean != null) {
        BeanTranslation translation = (BeanTranslation) bean.getTranslation(newLang.getCode());
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
     StringBuilder list = new StringBuilder();
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
     list.append("<SELECT name=\"")
         .append(HTMLSelectObjectName)
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
            .append(HTMLHiddenRemovedTranslationMode)
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
          .append(HTMLHiddenRemovedTranslationMode)
          .append("\" name=\"")
          .append(HTMLHiddenRemovedTranslationMode)
          .append("\" value=\"false\">\n");
    }
     return list.toString();
  }

  public static String updateHTMLLinks(I18NBean bean) {
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
    String param = request.getParameter(HTMLSelectObjectName);
    return getLanguageAndTranslationId(param);
  }

  private static String[] getLanguageAndTranslationId(String param) {
    if (StringUtil.isDefined(param)) {
      StringTokenizer tokenizer = new StringTokenizer(param, "_");
      String language = tokenizer.nextToken();
      String translationId = tokenizer.nextToken();
      return new String[] {language, translationId};
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

  public static boolean isI18nContentEnabled() {
    return isI18nContentActivated;
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

  private static void setI18NInfo(I18NBean bean, String param,
      String removeParam) {
    String[] languageAndTranslationId = getLanguageAndTranslationId(param);
    if (languageAndTranslationId.length > 0) {
      String language = languageAndTranslationId[0];
      String translationId = languageAndTranslationId[1];

      bean.setLanguage(language);
      bean.setTranslationId(translationId);

      String removeTranslation = removeParam;

      // check if translation must be removed
      bean.setRemoveTranslation("true".equalsIgnoreCase(removeTranslation));
    }
  }

  private static String getParameterValue(List<FileItem> items, String parameterName) {
    for (FileItem item : items) {
      if (item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item.getString();
      }
    }
    return null;
  }
}