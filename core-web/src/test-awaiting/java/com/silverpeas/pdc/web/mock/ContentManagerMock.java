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

package com.silverpeas.pdc.web.mock;

import javax.inject.Named;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.silvertrace.SilverTrace;
import static com.silverpeas.pdc.web.TestConstants.*;

/**
 * A Mock of the content manager. It mocks some of its method.
 */
@Named("contentManager")
public class ContentManagerMock extends ContentManager {

  private static final long serialVersionUID = 4852764880230256011L;

  public ContentManagerMock() throws ContentManagerException {
    super();
  }

  @Override
  public int getSilverContentId(String sInternalContentId, String sComponentId) throws
          ContentManagerException {
    if (!CONTENT_ID.equals(sInternalContentId) || !COMPONENT_INSTANCE_ID.equals(sComponentId)) {
      throw new ContentManagerException(getClass().getSimpleName(), SilverTrace.TRACE_LEVEL_ERROR,
              "root.EX_NO_MESSAGE");
    }
    return Integer.valueOf(sInternalContentId);
  }
}
