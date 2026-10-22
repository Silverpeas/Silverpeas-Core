/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.wysiwyg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Unit tests on the Javascript instructions printed out to display a WYSIWYG editor.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class WysiwygEditorTest {

  private static final String DD_UPLOAD = "configureCkEditorDdUpload";

  @TestManagedMock
  private OrganizationController organizationController;

  @BeforeEach
  void setUp() {
    URLUtil.setSilverpeasVersion("6.5");
  }

  @Test
  void theDragAndDropUploadIsConfiguredForAnExistingResource() {
    String js = editorFor("42").print();

    assertThat(js, containsString(DD_UPLOAD));
    assertThat(js, containsString("resourceId : '42'"));
  }

  /**
   * The files dropped into the editor are attached to the edited resource: no file can be
   * uploaded for a resource that doesn't exist yet, and the upload configuration reports an error
   * when the resource identifier is missing.
   */
  @Test
  void noDragAndDropUploadIsConfiguredForAResourceNotYetCreated() {
    assertThat(editorFor(null).print(), not(containsString(DD_UPLOAD)));
    assertThat(editorFor("").print(), not(containsString(DD_UPLOAD)));
  }

  private static WysiwygEditor editorFor(final String resourceId) {
    WysiwygEditor editor = new WysiwygEditor("suggestionBox5", "", resourceId, false);
    editor.setReplace("editorContent");
    return editor;
  }
}
