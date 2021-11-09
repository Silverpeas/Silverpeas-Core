/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.ddwe;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.wbe.WbeEdition;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeUser;
import org.silverpeas.core.webapi.wbe.WbeFileEdition;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.Optional.of;
import static org.silverpeas.core.wbe.WbeLogger.logger;
import static org.silverpeas.core.webapi.wbe.WbeFileEdition.ACCESS_TOKEN_PARAM;

/**
 * @author silveryocha
 */
@Service
public class DragAndDropWebEditorRequestDispatcher implements WbeFileEdition.ClientRequestDispatcher {

  @Override
  public boolean canHandle(final WbeEdition edition) {
    return edition instanceof DragAndDropWebEditorEdition;
  }

  @Override
  public Optional<String> dispatch(final HttpServletRequest request, final WbeEdition edition) {
    return of(edition)
        .map(DragAndDropWebEditorEdition.class::cast)
        .map(e -> {
          final WbeUser editionUser = e.getUser();
          final WbeFile editionFile = e.getFile();
          logger().debug(() -> format("from {0} initializing DDWE edition for {1} and for user {2}",
              editionUser.getSilverpeasSessionId(), editionFile, editionUser));
          request.setAttribute("wbe_edition", edition);
          request.setAttribute(ACCESS_TOKEN_PARAM, editionUser.getAccessToken());
          request.setAttribute("file_id", editionFile.id());
          return "/Rddwe/jsp/Main";
        });
  }
}
