/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.admin.component.parameter;

import com.stratelia.webactiv.beans.admin.ComponentInst;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.silverpeas.core.admin.component.constant.ComponentInstanceParameterName;
import org.silverpeas.core.admin.component.exception.ComponentFileFilterException;

import java.io.File;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * File structure :
 * /file.jpg
 * /file.odp
 * /rep1/file.pdf
 * /rep1/rep2/file.pptx
 * <p/>
 * User: Yohann Chastagnier
 * Date: 17/12/12
 */
public class ComponentFileFilterParameterTest {
  private static final String AUTHORIZED_GLOBALLY = "   *.doc    jpg,*.pptx";
  private static final String PARSED_AUTHORIZED_GLOBALLY = "doc, jpg, pptx";
  private static final String FORBIDDEN_GLOBALLY = " *  .xml; *.jpg, ppt";
  private static final String PARSED_FORBIDDEN_GLOBALLY = "xml, jpg, ppt";
  private static final String AUTHORIZED_COMPONENT = "  odp, pdf, ppt";
  private static final String PARSED_AUTHORIZED_COMPONENT = "odp, pdf, ppt";
  private static final String FORBIDDEN_COMPONENT = "*.gif, pdf, pptx,   *   .doc.xml";
  private static final String PARSED_FORBIDDEN_COMPONENT = "gif, pdf, pptx, doc.xml";
  private ComponentInst component = Mockito.mock(ComponentInst.class);

  @Before
  public void beforeTest() {
    ComponentFileFilterParameter.defaultAuthorizedFiles = "";
    ComponentFileFilterParameter.defaultForbiddenFiles = "";
  }

  @Test
  public void testIsFileAuthorizedWithNoFilter() throws Exception {
    // Test
    ComponentFileFilterParameter test = ComponentFileFilterParameter.from(component);
    assertThat(test.getFileFilters(), is(""));
    // Forbidden result
    assertThat(test.isFileAuthorized(null), is(false));
    // Authorized results
    for (String fileName : new String[]{"unexistingFile.xml", "file.jpg", "file.odp", "file.pdf",
        "file.pptx"}) {
      assertThat(test.isFileAuthorized(getFile(fileName)), is(true));
    }
  }

  @Test
  public void testIsFileAuthorizedWithGlobalForbiddenFilter() throws Exception {
    // Settings
    ComponentFileFilterParameter.defaultForbiddenFiles = FORBIDDEN_GLOBALLY;
    // Test
    ComponentFileFilterParameter test = ComponentFileFilterParameter.from(component);
    assertThat(test.isAuthorization(), is(false));
    assertThat(test.isFileFilterGloballySet(), is(true));
    assertThat(test.getFileFilters(), is(PARSED_FORBIDDEN_GLOBALLY));
    // Forbidden results
    assertThat(test.isFileAuthorized(null), is(false));
    for (String fileName : new String[]{"unexistingFile.xml", "file.jpg"}) {
      assertThat(test.isFileAuthorized(getFile(fileName)), is(false));
    }
    // Authorized results
    for (String fileName : new String[]{"file.odp", "file.pdf", "file.pptx"}) {
      assertThat(test.isFileAuthorized(getFile(fileName)), is(true));
    }
  }

  @Test
  public void testIsFileAuthorizedWithGlobalAuthorizedFilter() throws Exception {
    // Settings
    ComponentFileFilterParameter.defaultAuthorizedFiles = AUTHORIZED_GLOBALLY;
    ComponentFileFilterParameter.defaultForbiddenFiles = FORBIDDEN_GLOBALLY;
    // Test
    ComponentFileFilterParameter test = ComponentFileFilterParameter.from(component);
    assertThat(test.isAuthorization(), is(true));
    assertThat(test.isFileFilterGloballySet(), is(true));
    assertThat(test.getFileFilters(), is(PARSED_AUTHORIZED_GLOBALLY));
    // Forbidden results
    assertThat(test.isFileAuthorized(null), is(false));
    for (String fileName : new String[]{"unexistingFile.xml", "file.odp", "file.pdf"}) {
      assertThat(test.isFileAuthorized(getFile(fileName)), is(false));
    }
    // Authorized results
    for (String fileName : new String[]{"file.pptx", "file.jpg"}) {
      assertThat(test.isFileAuthorized(getFile(fileName)), is(true));
    }
  }

  @Test
  public void testIsFileAuthorizedWithComponentForbiddenFilter() throws Exception {
    // Settings
    ComponentFileFilterParameter.defaultAuthorizedFiles = AUTHORIZED_GLOBALLY;
    ComponentFileFilterParameter.defaultForbiddenFiles = FORBIDDEN_GLOBALLY;
    when(component.getParameterValue(ComponentInstanceParameterName.forbiddenFileExtension))
        .thenReturn(FORBIDDEN_COMPONENT);
    // Test
    ComponentFileFilterParameter test = ComponentFileFilterParameter.from(component);
    assertThat(test.isAuthorization(), is(false));
    assertThat(test.isFileFilterGloballySet(), is(false));
    assertThat(test.getFileFilters(), is(PARSED_FORBIDDEN_COMPONENT));
    // Forbidden results
    assertThat(test.isFileAuthorized(null), is(false));
    for (String fileName : new String[]{"unexistingFile.xml", "file.pptx", "file.pdf"}) {
      assertThat(test.isFileAuthorized(getFile(fileName)), is(false));
    }
    // Authorized results
    for (String fileName : new String[]{"file.odp", "file.jpg"}) {
      assertThat(test.isFileAuthorized(getFile(fileName)), is(true));
    }
  }

  @Test
  public void testIsFileAuthorizedWithComponentAuthorizedFilter() throws Exception {
    // Settings
    ComponentFileFilterParameter.defaultAuthorizedFiles = AUTHORIZED_GLOBALLY;
    ComponentFileFilterParameter.defaultForbiddenFiles = FORBIDDEN_GLOBALLY;
    when(component.getParameterValue(ComponentInstanceParameterName.forbiddenFileExtension))
        .thenReturn(FORBIDDEN_COMPONENT);
    when(component.getParameterValue(ComponentInstanceParameterName.authorizedFileExtension))
        .thenReturn(AUTHORIZED_COMPONENT);
    // Test
    ComponentFileFilterParameter test = ComponentFileFilterParameter.from(component);
    assertThat(test.isAuthorization(), is(true));
    assertThat(test.isFileFilterGloballySet(), is(false));
    assertThat(test.getFileFilters(), is(PARSED_AUTHORIZED_COMPONENT));
    // Forbidden results
    assertThat(test.isFileAuthorized(null), is(false));
    for (String fileName : new String[]{"unexistingFile.xml", "file.pptx", "file.jpg"}) {
      assertThat(test.isFileAuthorized(getFile(fileName)), is(false));
    }
    // Authorized results
    for (String fileName : new String[]{"file.odp", "file.pdf"}) {
      assertThat(test.isFileAuthorized(getFile(fileName)), is(true));
    }
  }

  @Test(expected = ComponentFileFilterException.class)
  public void testVerifyFileAuthorizedWithComponentAuthorizedFilterAndForbiddenFile()
      throws Exception {
    // Settings
    ComponentFileFilterParameter.defaultAuthorizedFiles = AUTHORIZED_GLOBALLY;
    ComponentFileFilterParameter.defaultForbiddenFiles = FORBIDDEN_GLOBALLY;
    when(component.getParameterValue(ComponentInstanceParameterName.forbiddenFileExtension))
        .thenReturn(FORBIDDEN_COMPONENT);
    when(component.getParameterValue(ComponentInstanceParameterName.authorizedFileExtension))
        .thenReturn(AUTHORIZED_COMPONENT);
    // Test
    ComponentFileFilterParameter.from(component).verifyFileAuthorized(getFile("file.pptx"));
  }

  @Test
  public void testVerifyFileAuthorizedWithComponentAuthorizedFilterAndAuthorizedFile()
      throws Exception {
    // Settings
    ComponentFileFilterParameter.defaultAuthorizedFiles = AUTHORIZED_GLOBALLY;
    ComponentFileFilterParameter.defaultForbiddenFiles = FORBIDDEN_GLOBALLY;
    when(component.getParameterValue(ComponentInstanceParameterName.forbiddenFileExtension))
        .thenReturn(FORBIDDEN_COMPONENT);
    when(component.getParameterValue(ComponentInstanceParameterName.authorizedFileExtension))
        .thenReturn(AUTHORIZED_COMPONENT);
    // Test
    ComponentFileFilterParameter.from(component).verifyFileAuthorized(getFile("file.pdf"));
  }

  /**
   * Gets file from test resources.
   * @param name
   * @return
   * @throws Exception
   */
  private File getFile(String name) throws Exception {
    URL documentLocation = getClass().getResource(name);
    if (documentLocation == null) {
      return new File(name);
    }
    return new File(documentLocation.toURI());
  }
}
