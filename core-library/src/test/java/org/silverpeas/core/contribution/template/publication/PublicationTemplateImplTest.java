/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.contribution.template.publication;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordTemplate;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.admin.component.model.GlobalContext;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;

import static org.silverpeas.core.contribution.template.publication.Assertion.assertEquals;
import static java.io.File.separatorChar;
import static org.apache.commons.io.FileUtils.getFile;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
public class PublicationTemplateImplTest {
  private static final char SEPARATOR = separatorChar;

  public File MAPPINGS_PATH;
  public File TEMPLATES_PATH;

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Before
  public void setUpClass() throws Exception {
    final File targetDir = getFile(
        PublicationTemplateImplTest.class.getProtectionDomain().getCodeSource().getLocation()
            .getFile());
    MAPPINGS_PATH = getFile(targetDir, "templateRepository", "mapping");
    TEMPLATES_PATH = getFile(targetDir, "templateRepository");

    PublicationTemplateManager.templateDir = TEMPLATES_PATH.getPath();
  }

  @Test
  public void testGetRecordTemplateSimple() throws Exception {
    String xmlFileName = "template" + SEPARATOR + "data.xml";
    // Pay attention to not declare org.silverpeas.core.contribution.content.form.displayers.PdcPositionsFieldDisplayer
    // inside types.properties cause this class is not available inside silverpeas-core project
    RecordTemplate expectedTemplate = getExpectedRecordTemplate(xmlFileName);
    PublicationTemplateImpl instance = new PublicationTemplateImpl();
    instance.setDataFileName(xmlFileName);
    instance.setFileName("personne.xml");
    RecordTemplate result = instance.getRecordTemplate();
    FieldTemplate fieldTemplate = result.getFieldTemplate("civilite");
    Field field = fieldTemplate.getEmptyField();
    assertThat(field, is(notNullValue()));
    assertEquals(expectedTemplate, result);
  }

  /**
   * Gets the expected record template from the specified file in which it is serialized in XML.
   * @param xmlFileName the name of the file in which is serialized the record template.
   * @return the record template that is serialized in the specified XML file.
   * @throws Exception if the record template cannot be deserialized.
   */
  private RecordTemplate getExpectedRecordTemplate(final String xmlFileName) throws Exception {
    Mapping mapping = new Mapping();
    mapping.loadMapping(getFile(MAPPINGS_PATH, "templateMapping.xml").getPath());
    Unmarshaller unmarshaller = new Unmarshaller(mapping);
    RecordTemplate template = (GenericRecordTemplate) unmarshaller
        .unmarshal(new InputSource(new FileInputStream(getFile(TEMPLATES_PATH, xmlFileName))));
    return template;
  }

  @Test
  @Ignore
  public void testTemplateVisibilityOnApplications() throws Exception {
    // template.xml is only applicable to component kmelia
    GlobalContext globalContext = new GlobalContext("WA1");
    globalContext.setComponentName("kmelia");
    PublicationTemplateManager manager = PublicationTemplateManager.getInstance();

    // template is visible to all kmelia instances
    assertThat(manager.isPublicationTemplateVisible("template.xml", globalContext), is(true));

    globalContext.setComponentName("webPages");
    // template is not visible to other components
    assertThat(manager.isPublicationTemplateVisible("template.xml", globalContext), is(false));
  }

  @Test
  @Ignore
  public void testTemplateVisibilityOnInstances() throws Exception {
    // template.xml is only applicable to only both instances
    GlobalContext globalContext = new GlobalContext("WA1");
    globalContext.setComponentName("webPages");
    PublicationTemplateManager manager = PublicationTemplateManager.getInstance();

    // template is not visible to all webPages instances
    assertThat(manager.isPublicationTemplateVisible("sandbox.xml", globalContext), is(false));

    globalContext.setComponentName("kmelia");
    // template is not visible to other components
    assertThat(manager.isPublicationTemplateVisible("sandbox.xml", globalContext), is(false));

    globalContext.setComponentName(null);
    globalContext.setComponentId("kmelia123");
    assertThat(manager.isPublicationTemplateVisible("sandbox.xml", globalContext), is(false));

    globalContext.setComponentId("kmelia12");
    assertThat(manager.isPublicationTemplateVisible("sandbox.xml", globalContext), is(true));
  }
}
