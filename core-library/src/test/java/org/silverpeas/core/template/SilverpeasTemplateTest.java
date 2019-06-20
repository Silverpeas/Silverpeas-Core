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
package org.silverpeas.core.template;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.util.MavenTestEnv;

import java.io.File;
import java.util.Properties;

import static java.io.File.separator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.silverpeas.core.template.SilverpeasTemplate.TEMPLATE_CUSTOM_DIR;
import static org.silverpeas.core.template.SilverpeasTemplate.TEMPLATE_ROOT_DIR;

@EnableSilverTestEnv
public class SilverpeasTemplateTest {

  private Properties configuration;

  @BeforeEach
  public void setUp(MavenTestEnv mavenTestEnv) {
    final File rootDir = new File(mavenTestEnv.getResourceTestDirFile(), "templates");
    configuration = new Properties();
    configuration.setProperty(TEMPLATE_ROOT_DIR, rootDir.getPath() + separator);
    configuration.setProperty(TEMPLATE_CUSTOM_DIR, rootDir.getPath() + separator);
  }

  @Test
  public void applyFileTemplateWithSimpleAttribute() throws Exception {
    SilverpeasTemplate template =
        SilverpeasTemplateFactory.createSilverpeasTemplate(configuration);
    String attributeString = "single";
    template.setAttribute("element", attributeString);
    String result = template.applyFileTemplate("testString");
    assertEquals("la valeur donnée est = single", result);
  }

  @Test
  public void applyStringTemplateWithSimpleAttribute() throws Exception {
    SilverpeasTemplate template =
        SilverpeasTemplateFactory.createSilverpeasTemplate(configuration);
    String attributeString = "single";
    template.setAttribute("element", attributeString);
    String result = template.applyStringTemplate("la valeur est = $element$");
    assertEquals("la valeur est = single", result);
  }

  @Test
  public void applyStringTemplateWithArrayAttribute() throws Exception {
    SilverpeasTemplate template =
        SilverpeasTemplateFactory.createSilverpeasTemplate(configuration);
    String[] attributeList = new String[2];
    attributeList[0] = "un";
    attributeList[1] = "deux";
    template.setAttribute("list", attributeList);
    String result = template.applyStringTemplate("la liste est = $list; separator=\", \"$");
    assertEquals("la liste est = un, deux", result);
  }

  @Test
  public void applyFileTemplateWithArrayAttribute() throws Exception {
    SilverpeasTemplate template =
        SilverpeasTemplateFactory.createSilverpeasTemplate(configuration);
    String[] attributeList = new String[2];
    attributeList[0] = "un";
    attributeList[1] = "deux";
    template.setAttribute("list", attributeList);
    String result = template.applyFileTemplate("testList");
    assertEquals("la liste donnée est = un, deux", result);
  }

}
