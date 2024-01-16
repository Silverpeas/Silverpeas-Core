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
package org.silverpeas.core.template;

import org.antlr.stringtemplate.AutoIndentWriter;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.silverpeas.core.template.renderer.DateRenderer;
import org.silverpeas.core.template.renderer.StringRenderer;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.apache.commons.io.FileUtils.copyDirectory;

public class SilverpeasStringTemplate implements SilverpeasTemplate {

  private static final Object MUTEX = new Object();
  private static final String MERGED_DIR_NAME = "__merged_delete_me_on_template_modification";
  private Map<String, Object> attributes = new HashMap<>();
  private Properties templateConfig;
  private List<String> paths = new ArrayList<>(3);

  public SilverpeasStringTemplate(Properties templateConfig) {
    this.templateConfig = templateConfig;
    paths.add(templateConfig.getProperty(TEMPLATE_CUSTOM_DIR));
    paths.add(templateConfig.getProperty(TEMPLATE_ROOT_DIR));
  }

  @Override
  public SilverpeasTemplate mergeRootWithCustom() {
    if (paths.size() == 2) {
      final File customPath = new File(paths.get(0));
      if (!customPath.exists()) {
        return this;
      }
      final File rootPath = new File(paths.get(1));
      final File mergedPath = new File(customPath, MERGED_DIR_NAME);
      synchronized (MUTEX) {
        if (!mergedPath.exists()) {
          if (mergedPath.mkdirs()) {
            try {
              copyDirectory(rootPath, mergedPath, true);
              copyDirectory(customPath, mergedPath, f -> !MERGED_DIR_NAME.equals(f.getName()), true);
              paths.add(0, mergedPath.getPath());
            } catch (IOException e) {
              SilverLogger.getLogger(this).error(e);
            }
          }
        } else {
          paths.add(0, mergedPath.getPath());
        }
      }
    }
    return this;
  }

  @Override
  public String applyFileTemplate(final String fileName) {
    return paths.stream()
        .map(d -> Pair.of(d, new StringTemplateGroup(fileName, d)))
        .map(p -> {
          final String physicalName = p.getSecond().getFileNameFromTemplateName(fileName);
          return Pair.of(new File(p.getFirst(), physicalName), p.getSecond());
        })
        .filter(p -> {
          final File file = p.getFirst();
          return file.exists() && file.isFile();
        })
        .findFirst()
        // In case the file is empty, StringTemplate is in error because the encoding can't be guessed ...
        .filter(p -> p.getFirst().length() > 0)
        .map(p -> {
          final StringTemplateGroup group = p.getSecond();
          group.setFileCharEncoding(Charsets.UTF_8.name());
          return group;
        })
        .map(g -> g.getInstanceOf(fileName))
        .map(this::applyAttributes)
        .orElse("");
  }

  @Override
  public String applyStringTemplate(String label) {
    StringTemplate template = new StringTemplate(label);
    return applyAttributes(template);
  }

  protected String applyAttributes(StringTemplate template) {
    template.registerRenderer(Date.class, new DateRenderer());
    template.registerRenderer(String.class, new StringRenderer());
    for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
      template.setAttribute(attribute.getKey(), attribute.getValue());
    }

    try(StringWriter writer = new StringWriter()) {
      AutoIndentWriter out = new AutoIndentWriter(writer, "\n");
      template.write(out);
      return writer.toString();
    } catch (IOException e) {
      return template.toString();
    }
  }

  @Override
  public void setAttribute(String name, Object value) {
    if (value instanceof String && StringUtil.isNotDefined((String) value)) {
      // It exists no reason to get true on conditional if performed on a not defined string.
      value = null;
    }
    attributes.put(name, value);
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String applyFileTemplateOnComponent(String componentName, String fileName) {
    return applyFileTemplate("/" + componentName.toLowerCase() + "/" + fileName);
  }

  @Override
  public boolean isCustomTemplateExists(String componentName, String fileName) {
    String filePath = "/" + componentName.toLowerCase() + "/" + fileName;
    String customersRootDir = templateConfig.getProperty(TEMPLATE_CUSTOM_DIR);
    StringTemplateGroup group = new StringTemplateGroup(filePath, customersRootDir);
    String physicalName = group.getFileNameFromTemplateName(filePath);
    File file = new File(customersRootDir, physicalName);
    return file.exists() && file.isFile();
  }
}
