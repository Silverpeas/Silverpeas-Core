/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.viewer.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.viewer.model.DocumentView;
import org.silverpeas.core.viewer.model.ViewerSettings;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.util.SerializationUtil;
import org.silverpeas.core.util.SettingBundle;

import javax.inject.Inject;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(Arquillian.class)
public class ViewServiceNoCacheDemonstrationTestBefore extends AbstractViewerTest {

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  @Inject
  private ViewService viewService;

  @Before
  public void setup() throws Exception {
    FileUtils.deleteQuietly(getTemporaryPath());
    getTemporaryPath().mkdirs();
    final SettingBundle mockedSettings =
        reflectionRule.mockField(ViewerSettings.class, SettingBundle.class, "settings");
    when(mockedSettings.getInteger(eq("preview.width.max"), anyInt())).thenReturn(1000);
    when(mockedSettings.getInteger(eq("preview.height.max"), anyInt())).thenReturn(1000);
    when(mockedSettings.getBoolean(eq("viewer.cache.enabled"), anyBoolean())).thenReturn(false);
    when(mockedSettings.getBoolean(eq("viewer.cache.conversion.silent.enabled"), anyBoolean()))
        .thenReturn(false);
    when(mockedSettings.getBoolean(eq("viewer.conversion.strategy.split.enabled"), anyBoolean()))
        .thenReturn(false);
  }

  @Test
  public void demonstrateNoCache() throws Exception {
    if (canPerformViewConversionTest()) {
      SimpleDocument document = getSimpleDocumentNamed("file.odt");
      long start = System.currentTimeMillis();
      DocumentView documentView = viewService.getDocumentView(ViewerContext.from(document));
      assertDocumentView(documentView);
      long end = System.currentTimeMillis();
      long conversionDuration = end - start;
      Logger.getAnonymousLogger().info("Conversion duration without cache in " +
          DurationFormatUtils.formatDurationHMS(conversionDuration));
      assertThat(conversionDuration, greaterThan(250l));
      saveInTemporaryPath(ViewServiceNoCacheDemonstrationTestSuite.CONVERSION_DURATION_FILE_NAME, String.valueOf(conversionDuration));
      saveInTemporaryPath(ViewServiceNoCacheDemonstrationTestSuite.DOCUMENT_VIEW_FILE_NAME,
          SerializationUtil.serializeAsString(documentView));
    }
  }

  private void assertDocumentView(DocumentView view) {
    assertThat(view, notNullValue());
    assertThat(getTemporaryPath().listFiles(), arrayWithSize(1));
    assertThat(view.getPhysicalFile().getParentFile().listFiles(), arrayWithSize(1));
    assertThat(view.getPhysicalFile().getName(), endsWith("file.swf"));
    assertThat(view.getWidth(), is("595"));
    assertThat(view.getHeight(), is("842"));
  }
}
