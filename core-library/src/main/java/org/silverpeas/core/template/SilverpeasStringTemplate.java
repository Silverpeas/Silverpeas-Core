/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.template;

import org.antlr.stringtemplate.AutoIndentWriter;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.template.renderer.DateRenderer;
import org.silverpeas.core.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SilverpeasStringTemplate implements SilverpeasTemplate {

  public Map<String, Object> attributes = new HashMap<String, Object>();
  public Properties templateConfig;

  public SilverpeasStringTemplate(Properties templateConfig) {
    this.templateConfig = templateConfig;
  }

  @Override
  public String applyFileTemplate(String fileName) {
    String customersRootDir = templateConfig.getProperty(TEMPLATE_CUSTOM_DIR);
    StringTemplateGroup group = new StringTemplateGroup(fileName, customersRootDir);
    String physicalName = group.getFileNameFromTemplateName(fileName);
    File file = new File(customersRootDir, physicalName);
    if (!file.exists() || !file.isFile()) {
      String rootRootDir = templateConfig.getProperty(TEMPLATE_ROOT_DIR);
      file = new File(rootRootDir, physicalName);
      group = new StringTemplateGroup(fileName, rootRootDir);
    }
    // In case the file is empty, StringTemplate is in error because the encoding can't be
    // guessed ...
    if (file.exists() && file.length() == 0) {
      return "";
    }
    group.setFileCharEncoding(CharEncoding.UTF_8);
    StringTemplate template = group.getInstanceOf(fileName);
    return applyAttributes(template);
  }

  @Override
  public String applyStringTemplate(String label) {
    StringTemplate template = new StringTemplate(label);
    return applyAttributes(template);
  }

  protected String applyAttributes(StringTemplate template) {
    template.registerRenderer(Date.class, new DateRenderer());
    for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
      template.setAttribute(attribute.getKey(), attribute.getValue());
    }
    StringWriter writer = new StringWriter();
    AutoIndentWriter out = new AutoIndentWriter(writer, "\n");
    try {
      template.write(out);
    } catch (IOException e) {
      return template.toString();
    } finally {
      IOUtils.closeQuietly(writer);
    }
    return writer.toString();
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
