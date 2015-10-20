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
package org.silverpeas.viewer;

import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.rule.MockByReflectionRule;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.File;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-viewer.xml")
public class ViewServiceNoCacheDemonstrationTestBefore extends AbstractViewerTest {

  static DocumentView documentView;
  static long conversionDuration = 0;

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  @Inject
  private ViewService viewService;

  @Before
  public void setup() throws Exception {
    FileUtils.deleteQuietly(new File(FileRepositoryManager.getTemporaryPath()));
    (new File(FileRepositoryManager.getTemporaryPath())).mkdirs();
    final ResourceLocator mockedSettings =
        reflectionRule.mockField(ViewerSettings.class, ResourceLocator.class, "settings");
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
      long start = System.currentTimeMillis();
      documentView =
          viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.odt")));
      assertDocumentView(documentView);
      long end = System.currentTimeMillis();
      conversionDuration = end - start;
      Logger.getAnonymousLogger().info("Convertion duration without cache in " +
          DurationFormatUtils.formatDurationHMS(conversionDuration));
      assertThat(conversionDuration, greaterThan(250l));
    }
  }

  private void assertDocumentView(DocumentView view) {
    assertThat(view, notNullValue());
    assertThat(new File(FileRepositoryManager.getTemporaryPath()).listFiles(), arrayWithSize(1));
    assertThat(view.getPhysicalFile().getParentFile().listFiles(), arrayWithSize(1));
    assertThat(view.getPhysicalFile().getName(), endsWith("file.swf"));
    assertThat(view.getWidth(), is("595"));
    assertThat(view.getHeight(), is("842"));
  }
}
