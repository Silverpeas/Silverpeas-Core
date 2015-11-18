/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.attachment.mock;

import org.mockito.Mockito;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.webdav.WebdavService;

/**
 * A wrapper of a mock of an {@code WebdavService} instance dedicated to the tests. This wrapper
 * decorates the mock and it is used to be managed by an IoC container as an
 * {@code WebdavService} instance.
 * @author: Yohann Chastagnier
 */
public class WebdavServiceMockWrapper implements WebdavService {

  private WebdavService mock = Mockito.mock(WebdavService.class);

  public WebdavService getMock() {
    return mock;
  }

  @Override
  public void updateDocumentContent(final SimpleDocument document) {
    mock.updateDocumentContent(document);
  }

  @Override
  public String getContentEditionLanguage(final SimpleDocument document) {
    return mock.getContentEditionLanguage(document);
  }

  @Override
  public long getContentEditionSize(final SimpleDocument document) {
    return mock.getContentEditionSize(document);
  }
}
