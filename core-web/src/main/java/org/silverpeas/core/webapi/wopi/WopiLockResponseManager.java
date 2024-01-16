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

package org.silverpeas.core.webapi.wopi;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.wopi.WopiFile;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.webapi.wopi.WopiFileResource.WOPI_LOCK_HEADER;
import static org.silverpeas.core.wopi.WopiLogger.logger;
import static org.silverpeas.core.wopi.WopiSettings.isLockCapabilityEnabled;

/**
 * @author silveryocha
 */
@Service
public class WopiLockResponseManager {

  private static final String WOPI_OLD_LOCK_HEADER = "X-WOPI-OldLock";

  protected WopiLockResponseManager() {
  }

  /**
   * Indicates if lock manager is enabled.
   * @return true of enabled, false otherwise.
   */
  protected boolean isEnabled() {
    return isLockCapabilityEnabled();
  }

  /**
   * Manages a file lock.
   * @param request the current request.
   * @param lockAction the lock action.
   * @param file the current file to manage.
   * @return an optional HTTP response.
   */
  protected Optional<Response> manage(final HttpServletRequest request, final String lockAction,
      final WopiFile file) {
    final Response response;
    if (!isEnabled()) {
      response = null;
    } else if (currentLockIsNotWopiOne(file)) {
      logger().debug(() -> format("EXTERNAL LOCK CONFLICT on file {0}", file));
      response = Response.status(CONFLICT).header(WOPI_LOCK_HEADER, EMPTY).build();
    } else if ("LOCK".equals(lockAction)) {
      response = lock(request, file);
    } else if ("GET_LOCK".equals(lockAction)) {
      response = getLock(file);
    } else if ("REFRESH_LOCK".equals(lockAction)) {
      response = refreshLock(request, file);
    } else if ("UNLOCK".equals(lockAction)) {
      response = unlock(request, file);
    } else {
      response = null;
    }
    return Optional.ofNullable(response);
  }

  private Response lock(final HttpServletRequest request, final WopiFile file) {
    final String lockId = request.getHeader(WOPI_LOCK_HEADER);
    final String oldLockId = request.getHeader(WOPI_OLD_LOCK_HEADER);
    if (isDefined(oldLockId)) {
      // unlock and relock case
      if (!file.lock().exists() || !file.lock().id().equals(oldLockId)) {
        logger().debug(() -> format("RELOCK CONFLICT with old lock {0} on file {1}", oldLockId, file));
        return Response.status(CONFLICT)
            .header(WOPI_LOCK_HEADER, file.lock().id())
            .build();
      }
      logger().debug(() -> format("RELOCK with new lock {0} on file {1}", lockId, file));
    } else {
      // new lock case
      if (file.lock().exists() && !file.lock().id().equals(lockId)) {
        logger().debug(() -> format("LOCK CONFLICT with new lock {0} on file {1}", lockId, file));
        return Response.status(CONFLICT)
            .header(WOPI_LOCK_HEADER, file.lock().id())
            .build();
      }
      logger().debug(() -> format("LOCK with new lock {0} on file {1}", lockId, file));
    }
    file.lock().setId(lockId);
    return Response.ok().header(WOPI_LOCK_HEADER, file.lock().id()).build();
  }

  private Response getLock(final WopiFile file) {
    logger().debug(() -> format("GET LOCK on file {0}", file));
    return Response.ok().header(WOPI_LOCK_HEADER, file.lock().id()).build();
  }

  private Response refreshLock(final HttpServletRequest request, final WopiFile file) {
    final String lockId = request.getHeader(WOPI_LOCK_HEADER);
    if (!file.lock().exists() || !file.lock().id().equals(lockId)) {
      logger().debug(() -> format("REFRESH LOCK CONFLICT with new lock {0} on file {1}", lockId, file));
      return Response.status(CONFLICT)
          .header(WOPI_LOCK_HEADER, file.lock().id())
          .build();
    }
    logger().debug(() -> format("REFRESH LOCK with new lock {0} on file {1}", lockId, file));
    file.lock().setId(lockId);
    return Response.ok().header(WOPI_LOCK_HEADER, file.lock().id()).build();
  }

  private Response unlock(final HttpServletRequest request, final WopiFile file) {
    final String lockId = request.getHeader(WOPI_LOCK_HEADER);
    if (!file.lock().exists() || !file.lock().id().equals(lockId)) {
      logger().debug(() -> format("UNLOCK CONFLICT with lock {0} on file {1}", lockId, file));
      return Response.status(CONFLICT)
          .header(WOPI_LOCK_HEADER, file.lock().id())
          .build();
    }
    logger().debug(() -> format("UNLOCK on file {0}", file));
    file.lock().clear();
    return Response.ok().build();
  }

  private boolean currentLockIsNotWopiOne(final WopiFile file) {
    return file.lock().exists() && file.lock().id().length() > 1024;
  }
}
