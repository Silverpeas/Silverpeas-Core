/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.core.util.SilverpeasBundle;
import org.silverpeas.core.util.StringUtil;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static org.silverpeas.core.util.WebEncodeHelper.javaStringToJsString;

/**
 * This tool permits the creates dynamically javascript settings that javascript plugins can used
 * easily.<br>
 * The generated javascript settings will be an instance of SilverpeasPluginSettings defined into
 * silverpeas.js
 * @author Yohann Chastagnier
 */
public class JavascriptSettingProducer {

  private String jsSettingVariableName = "Settings";
  private Map<String, String> keySettings = new LinkedHashMap<>();

  /**
   * Hidden constructor.
   */
  private JavascriptSettingProducer() {
  }

  /**
   * Initializes the setting producer by specifying the name of the javascript variable that
   * represents the settings.
   * @param jsSettingVariableName the javascript variable name of the bundle.
   * @return the initialized producer instance.
   */
  public static JavascriptSettingProducer settingVariableName(final String jsSettingVariableName) {
    return new JavascriptSettingProducer().withSettingVariableName(jsSettingVariableName);
  }

  /**
   * Initializes the setting producer by specifying the name of the javascript variable that
   * represents the settings.
   * @param jsSettingVariableName the javascript variable name of the bundle.
   * @return itself.
   */
  private JavascriptSettingProducer withSettingVariableName(final String jsSettingVariableName) {
    this.jsSettingVariableName = jsSettingVariableName;
    return this;
  }

  /**
   * Adds given keys from the given bundle.
   * @param bundle the bundle from which the messages must be extracted.
   * @param keys the requested message keys.
   * @return itself.
   */
  public JavascriptSettingProducer add(final SilverpeasBundle bundle, final String... keys) {
    for (String key : keys) {
      add(key, bundle.getString(key));
    }
    return this;
  }

  /**
   * Adds given key / value which will be an array of values.
   * @param key the key to add.
   * @param values the values to set.
   * @param valuesAreStrings indicates if the values must be registered as string.
   * @return itself.
   */
  protected <T> JavascriptSettingProducer add(String key, Stream<T> values,
      boolean valuesAreStrings) {
    Stream<String> decodedValues = values.map(String::valueOf)
        .filter(StringUtil::isDefined)
        .flatMap(v -> stream(v.split(",")));
    if (valuesAreStrings) {
      decodedValues = decodedValues.map(JavascriptSettingProducer::stringValue);
    }
    keySettings.put(key, decodedValues.collect(Collectors.joining(",", "[", "]")));
    return this;
  }

  /**
   * Adds given key / value.
   * @param key the key to add.
   * @param value the string value associated to the key.
   * @return itself.
   */
  public JavascriptSettingProducer add(final String key, final String value) {
    keySettings.put(key, stringValue(value));
    return this;
  }

  /**
   * Adds given key / value.
   * @param key the key to add.
   * @param value the boolean value associated to the key.
   * @return itself.
   */
  public JavascriptSettingProducer add(final String key, final boolean value) {
    keySettings.put(key, String.valueOf(value));
    return this;
  }

  /**
   * Adds given key / value.
   * @param key the key to add.
   * @param value the number value associated to the key.
   * @return itself.
   */
  public JavascriptSettingProducer add(final String key, final Number value) {
    keySettings.put(key, String.valueOf(value));
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
    js.append("window.").append(jsSettingVariableName).append("=new SilverpeasPluginSettings({");
    Iterator<Map.Entry<String, String>> keyMessageIt = keySettings.entrySet().iterator();
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

  private static String stringValue(final String value) {
    return "'" + javaStringToJsString(value) + "'";
  }
}
