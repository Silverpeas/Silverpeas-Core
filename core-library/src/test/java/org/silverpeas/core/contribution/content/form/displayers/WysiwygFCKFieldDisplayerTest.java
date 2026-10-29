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
package org.silverpeas.core.contribution.content.form.displayers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.SettingBundleStub;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the editor displayed by the WYSIWYG field of a form.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class WysiwygFCKFieldDisplayerTest {

  private static final String DD_UPLOAD = "configureCkEditorDdUpload";

  @RegisterExtension
  SettingBundleStub wysiwygSettings =
      new SettingBundleStub("org.silverpeas.wysiwyg.settings.wysiwygSettings")
          .put("configFile", "wysiwyg/jsp/ckeditor/silverconfig.js");

  @RegisterExtension
  SettingBundleStub formSettings = new SettingBundleStub("org.silverpeas.form.settings.form")
      .put("form.field.wysiwyg.toolbar.startupExpanded", "true");

  private final FieldTemplate template = mock(FieldTemplate.class);
  private final TextField field = mock(TextField.class);

  @BeforeEach
  void setUp() {
    URLUtil.setSilverpeasVersion("6.5");
    when(template.getFieldName()).thenReturn("description");
    when(template.getParameters(any())).thenReturn(Map.of());
    when(field.getValue()).thenReturn("");
  }

  @Test
  void theEditorOfTheFieldOfAContributionKnowsItAndAcceptsDroppedFiles() throws FormException {
    String editor = display(context("kmelia1", "42"));

    assertThat(editor, containsString("silverpeasComponentId : 'kmelia1'"));
    assertThat(editor, containsString("silverpeasObjectId : '42'"));
    assertThat(editor, containsString(DD_UPLOAD));
  }

  /**
   * In the preview of a form in the template designer, there is no contribution: the component
   * instance is 'useless' and there is no object. The editor must then neither look for the media
   * of a contribution (with the 'null' identifier) nor configure the upload of dropped files.
   */
  @Test
  void theEditorOfAFieldWithoutContributionAsksNothingAboutIt() throws FormException {
    String editor = display(context("useless", null));

    assertThat(editor, not(containsString("silverpeasObjectId")));
    assertThat(editor, not(containsString("silverpeasComponentId")));
    assertThat(editor, not(containsString(DD_UPLOAD)));
  }

  @Test
  void theEditorOfAFieldOfAContributionNotYetCreatedAcceptsNoDroppedFiles() throws FormException {
    String editor = display(context("kmelia1", null));

    assertThat(editor, containsString("silverpeasComponentId : 'kmelia1'"));
    assertThat(editor, not(containsString("silverpeasObjectId")));
    assertThat(editor, not(containsString(DD_UPLOAD)));
  }

  /**
   * The image and file banks are given with the configuration of the editor, otherwise the
   * plugins of CKEditor request them by a synchronous XMLHttpRequest.
   */
  @Test
  void theEditorIsGivenTheImageAndFileBanks() throws FormException {
    String editor = display(context("kmelia1", "42"));

    assertThat(editor, containsString("filebankApps : ["));
    assertThat(editor, containsString("imagebankApps : ["));
  }

  private String display(final PagesContext context) throws FormException {
    StringWriter html = new StringWriter();
    new WysiwygFCKFieldDisplayer().display(new PrintWriter(html), field, template, context);
    return html.toString();
  }

  private static PagesContext context(final String componentId, final String objectId) {
    PagesContext context = mock(PagesContext.class);
    when(context.getComponentId()).thenReturn(componentId);
    when(context.getObjectId()).thenReturn(objectId);
    when(context.getLanguage()).thenReturn("fr");
    when(context.getServerURL()).thenReturn("https://intranet.silverpeas.org");
    return context;
  }
}
