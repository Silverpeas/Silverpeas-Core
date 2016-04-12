/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.viewer.web.mock;

import static org.mockito.Mockito.mock;

import java.io.File;

import javax.inject.Named;

import org.silverpeas.viewer.Preview;
import org.silverpeas.viewer.PreviewService;

import com.silverpeas.util.Default;
import org.silverpeas.viewer.ViewerContext;

/**
 * @author Yohann Chastagnier
 */
@Named("previewService")
@Default
public class PreviewServiceMockWrapper implements PreviewService {

  private final PreviewService mock;

  public PreviewServiceMockWrapper() {
    mock = mock(PreviewService.class);
  }

  public PreviewService getMock() {
    return mock;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.service.PreviewService#isItPossibleGettingPreview(java.io.File)
   */
  @Override
  public boolean isPreviewable(final File file) {
    return mock.isPreviewable(file);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.service.PreviewService#getPreview(java.io.File)
   */
  @Override
  public Preview getPreview(final ViewerContext viewerContext) {
    return mock.getPreview(viewerContext);
  }
}
