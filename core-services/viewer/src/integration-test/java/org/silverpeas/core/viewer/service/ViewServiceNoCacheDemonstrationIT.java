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
package org.silverpeas.core.viewer.service;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.SerializationUtil;
import org.silverpeas.core.viewer.model.DocumentView;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

public class ViewServiceNoCacheDemonstrationIT extends AbstractViewerIT {

  @Inject
  private ViewService viewService;

  @Before
  public void setup() {
    clearTemporaryPath();
    boolean isOk = getTemporaryPath().mkdirs();
    assertThat(isOk, is(true));

    setViewerSettings("org.silverpeas.viewer.viewer_without_cache");
  }

  @After
  public void tearDown() {
    clearTemporaryPath();
  }

  @Test
  public void demonstrateCache() throws Exception {
    firstStep();
    secondStep();
  }

  @Test
  public void firstStep() {
    final Thread thread = new Thread(() -> {
      try {
        if (canPerformViewConversionTest()) {
          SimpleDocument document = getSimpleDocumentNamed("file.odt");
          long start = System.currentTimeMillis();
          DocumentView documentView = viewService.getDocumentView(ViewerContext.from(document));
          assertDocumentView(documentView, 1);
          long end = System.currentTimeMillis();
          long conversionDuration = end - start;
          Logger.getAnonymousLogger().info("Conversion duration without cache in " +
              DurationFormatUtils.formatDurationHMS(conversionDuration));
          assertThat(conversionDuration, greaterThan(250L));
          saveInTemporaryPath(CONVERSION_DURATION_FILE_NAME, String.valueOf(conversionDuration));
          saveInTemporaryPath(DOCUMENT_VIEW_FILE_NAME,
              SerializationUtil.serializeAsString(documentView));
        }
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      }
    });
    thread.start();
    try {
      thread.join(60000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void secondStep() throws Exception {
    if (canPerformViewConversionTest()) {
      readAndRemoveFromTemporaryPathAsLong(CONVERSION_DURATION_FILE_NAME);
      DocumentView documentViewFromBefore = SerializationUtil
          .deserializeFromString(readAndRemoveFromTemporaryPath(DOCUMENT_VIEW_FILE_NAME));
      awaitUntil(1001, TimeUnit.MILLISECONDS);
      SimpleDocument document = getSimpleDocumentNamed("file.odt");
      long start = System.currentTimeMillis();
      final DocumentView view = viewService.getDocumentView(ViewerContext.from(document));
      assertDocumentView(view, 2);
      long end = System.currentTimeMillis();
      long fromCacheDuration = end - start;
      Logger.getAnonymousLogger().info("(Again) Conversion duration without cache in " +
          DurationFormatUtils.formatDurationHMS(fromCacheDuration));
      assertThat(fromCacheDuration, greaterThan(250L));

      assertThat(view, not(sameInstance(documentViewFromBefore)));

      assertThat(view.getPhysicalFile().getParentFile().getName(),
          not(is(documentViewFromBefore.getPhysicalFile().getParentFile().getName())));

      assertThat(view.getPhysicalFile().getParentFile().lastModified(),
          greaterThan(documentViewFromBefore.getPhysicalFile().getParentFile().lastModified()));

      assertThat(view.getPhysicalFile().lastModified(),
          greaterThan(documentViewFromBefore.getPhysicalFile().lastModified()));
    }
  }

  private void assertDocumentView(DocumentView view, final int nbAttemptedFiles) {
    assertThat(view, notNullValue());
    assertThat(getTemporaryPath().listFiles(), arrayWithSize(nbAttemptedFiles));
    assertThat(view.getPhysicalFile().getParentFile().listFiles(), arrayWithSize(1));
    assertThat(view.getPhysicalFile().getName(), endsWith("file.swf"));
    assertThat(view.getWidth(), is("595"));
    assertThat(view.getHeight(), is("842"));
  }
}
