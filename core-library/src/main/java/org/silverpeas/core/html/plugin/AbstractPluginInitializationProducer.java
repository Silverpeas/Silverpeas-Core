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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.html.plugin;

import org.apache.ecs.xhtml.script;
import org.silverpeas.core.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.WebEncodeHelper.javaStringToJsString;

/**
 * This abstraction defines the produce method which provides the HTML code to initialize a plugin
 * on the WEB browser page.
 * @author silveryocha
 */
public abstract class AbstractPluginInitializationProducer {

  private String language;
  private String jsInstanceVar;
  private Map<String, String> options = new HashMap<>();

  protected static String stringValue(final String value) {
    return "'" + javaStringToJsString(value) + "'";
  }

  protected String booleanValue(final boolean value) {
    return String.valueOf(value);
  }

  protected String numericValue(final Number value) {
    return String.valueOf(value);
  }

  /**
   * Sets the user language for UI rendered by the plugin.
   * @param language the user language.
   * @param <T> the type of the producer.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends AbstractPluginInitializationProducer> T withUserLanguage(String language) {
    this.language = language;
    return (T) this;
  }

  /**
   * Sets the name of the js instance.
   * @param jsInstanceVar the name of the js instance.
   * @param <T> the type of the producer.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends AbstractPluginInitializationProducer> T withJsInstanceVar(
      String jsInstanceVar) {
    this.jsInstanceVar = jsInstanceVar;
    return (T) this;
  }

  /**
   * Adds given name / value.
   * @param name the name to add.
   * @param value the boolean value associated to the key.
   * @param <T> the type of the producer.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  protected <T extends AbstractPluginInitializationProducer> T addOption(String name,
      String value) {
    options.put(name, stringValue(value));
    return (T) this;
  }

  /**
   * Adds given name / value which will be an array of values.
   * @param name the name to add.
   * @param values the values to set.
   * @param valuesAreStrings indicates if the values must be registered as string.
   * @param <T> the type of the producer.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  protected <T extends AbstractPluginInitializationProducer> T addOption(String name,
      Stream<Object> values, boolean valuesAreStrings) {
    Stream<String> decodedValues = values.map(String::valueOf)
          .filter(StringUtil::isDefined)
          .flatMap(v -> stream(v.split(",")));
    if (valuesAreStrings) {
      decodedValues = decodedValues.map(AbstractPluginInitializationProducer::stringValue);
    }
    options.put(name, decodedValues.collect(Collectors.joining(",", "[", "]")));
    return (T) this;
  }

  /**
   * Adds given name / value.
   * @param name the name to add.
   * @param value the boolean value associated to the key.
   * @param <T> the type of the producer.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends AbstractPluginInitializationProducer> T addOption(final String name,
      final boolean value) {
    options.put(name, booleanValue(value));
    return (T) this;
  }

  /**
   * Adds given name / value.
   * @param name the name to add.
   * @param value the number value associated to the key.
   * @param <T> the type of the producer.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends AbstractPluginInitializationProducer> T addOption(final String name,
      final Number value) {
    options.put(name, numericValue(value));
    return (T) this;
  }

  /**
   * @return the HTML code which initializes the plugin.
   */
  public String produce() {
    StringBuilder pluginInit = new StringBuilder();
    pluginInit.append("whenSilverpeasReady(function() {");
    pluginInit.append("var instance = new ")
        .append(getClass().getSimpleName().replace("Producer", "")).append("(");
    if (!options.isEmpty()) {
      pluginInit.append(
          options.entrySet().stream().map(e -> "\"" + e.getKey() + "\":" + e.getValue())
              .collect(Collectors.joining(",", "{", "}")));
    }
    pluginInit.append(");");
    if (isDefined(jsInstanceVar)) {
      pluginInit.append(jsInstanceVar).append("=").append("instance;");
    }
    pluginInit.append("});");
    return getDependencies() +
        new script().setType("text/javascript").addElement(pluginInit.toString());
  }

  protected String getUserLanguage() {
    return language;
  }

  protected abstract String getDependencies();
}
