/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.SilverpeasBundle;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.silverpeas.core.util.WebEncodeHelper.javaStringToJsString;

/**
 * This tool permits the creates dynamically javascript bundles that javascript plugins can used
 * easily.<br>
 * The generated javascript bundle will be an instance of SilverpeasPluginBundle defined into
 * silverpeas.js
 * @author Yohann Chastagnier
 */
public class JavascriptBundleProducer {

  private String jsBundleVariableName = "Bundle";
  private Map<String, String> keyMessages = new LinkedHashMap<>();

  /**
   * Hidden constructor.
   */
  private JavascriptBundleProducer() {
  }

  /**
   * Gets the javascript bundle content from the string template repository and according to
   * given parameters.
   * @param pathSuffix the path suffix behind the root path "core" of string template repository.
   * @param templateName the template name (so the filename without the language ("_fr" for
   * example) and without extension.
   * @param language the requested language content.
   * @return the initialized producer instance.
   */
  static String fromCoreTemplate(final String pathSuffix, final String templateName,
      final String language) {
    SilverpeasTemplate bundle =
        SilverpeasTemplateFactory.createSilverpeasTemplateOnCore(pathSuffix);
    return bundle
        .applyFileTemplate(templateName + "_" + DisplayI18NHelper.verifyLanguage(language));
  }

  /**
   * Initializes the bundle producer by specifying the name of the javascript variable that
   * represents the bundle.
   * @param jsBundleVariableName the javascript variable name of the bundle.
   * @return the initialized producer instance.
   */
  public static JavascriptBundleProducer bundleVariableName(final String jsBundleVariableName) {
    return new JavascriptBundleProducer().withBundleVariableName(jsBundleVariableName);
  }

  /**
   * Initializes the bundle producer by specifying the name of the javascript variable that
   * represents the bundle.
   * @param jsBundleVariableName the javascript variable name of the bundle.
   * @return itself.
   */
  private JavascriptBundleProducer withBundleVariableName(final String jsBundleVariableName) {
    this.jsBundleVariableName = jsBundleVariableName;
    return this;
  }

  /**
   * Adds given keys from the given bundle.
   * @param bundle the bundle from which the messages must be extracted.
   * @param keys the requested message keys.
   * @return itself.
   */
  public JavascriptBundleProducer add(final SilverpeasBundle bundle, final String... keys) {
    for (String key : keys) {
      add(key, bundle.getString(key));
    }
    return this;
  }

  /**
   * Adds given key / value.
   * @param key the key to add.
   * @param value the boolean value associated to the key.
   * @return itself.
   */
  public JavascriptBundleProducer add(final String key, final String value) {
    keyMessages.put(key, "\"" + javaStringToJsString(value) + "\"");
    return this;
  }

  /**
   * The name of the javascript variable that represents the instance of the bundle.<br>
   * Please ensure that it is unique.
   * @return the javascript as string.
   */
  @SuppressWarnings("Duplicates")
  public String produce() {
    StringBuilder js = new StringBuilder();
    js.append("window.").append(jsBundleVariableName).append("=new SilverpeasPluginBundle({");
    Iterator<Map.Entry<String, String>> keyMessageIt = keyMessages.entrySet().iterator();
    while (keyMessageIt.hasNext()) {
      Map.Entry<String, String> keyMessage = keyMessageIt.next();
      js.append("\"").append(keyMessage.getKey()).append("\":");
      js.append(keyMessage.getValue());
      if (keyMessageIt.hasNext()) {
        js.append(",");
      }
    }
    js.append("});");
    return js.toString();
  }
}
