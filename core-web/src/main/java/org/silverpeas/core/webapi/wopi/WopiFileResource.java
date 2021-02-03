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

package org.silverpeas.core.webapi.wopi;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.base.annotation.Authenticated;
import org.silverpeas.core.wopi.WopiFile;
import org.silverpeas.core.wopi.WopiUser;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.time.OffsetDateTime.parse;
import static java.util.Optional.*;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static org.silverpeas.core.date.TemporalFormatter.toIso8601;
import static org.silverpeas.core.util.StringUtil.*;
import static org.silverpeas.core.util.URLUtil.getFullApplicationURL;
import static org.silverpeas.core.util.URLUtil.getServerURL;
import static org.silverpeas.core.util.file.FileServerUtils.getImageURL;
import static org.silverpeas.core.webapi.wopi.WopiResourceURIs.WOPI_FILE_BASE_URI;
import static org.silverpeas.core.wopi.WopiLogger.logger;
import static org.silverpeas.core.wopi.WopiSettings.*;

/**
 * @author silveryocha
 */
@WebService
@Path(WOPI_FILE_BASE_URI)
@Authenticated
public class WopiFileResource extends AbstractWopiFileResource {

  private static final String WOPI_OVERRIDE_HEADER = "X-WOPI-Override";
  private static final String WOPI_USER_IDS_HEADER = "X-WOPI-ViewUserIds";
  static final String WOPI_LOCK_HEADER = "X-WOPI-Lock";

  private static final String LAST_MODIFIED_TIME_FIELD = "LastModifiedTime";

  @Inject
  private WopiLockResponseManager lockManager;

  /**
   * @see
   * <a href="https://wopi.readthedocs.io/projects/wopirest/en/latest/endpoints.html#files-endpoint"> WOPI spec,
   * files endpoint</a>
   */
  @GET
  public Response sendFileData() {
    return process(() -> {
      final WopiFileEditionContext context = getEditionContext();
      final WopiFile file = context.getFile();
      final WopiUser user = context.getUser();
      final String json = JSONCodec.encodeObject(o -> {
        final User spUser = user.asSilverpeas();
        final boolean canBeModifiedBy = file.canBeModifiedBy(spUser);
        final boolean webViewOnly = false;
        final HttpServletRequest request = getSilverpeasContext().getRequest();
        return o
            // File
            .put("OwnerId", getWopiUserIdPrefix() + file.owner())
            .put("BaseFileName", file.name())
            .put("Size", file.size())
            .put("UserId", user.getId())
            .put("Version", file.version())
            .put(LAST_MODIFIED_TIME_FIELD, toIso8601(file.lastModificationDate(), true))
            // Host capabilities
            .put("SupportsContainers", false)
            .put("SupportsDeleteFile", false)
            .put("SupportsEcosystem", false)
            .put("SupportsExtendedLockLength", false)
            .put("SupportsFolders", false)
            .put("SupportsGetLock", lockManager.isEnabled())
            .put("SupportsLocks", lockManager.isEnabled())
            .put("SupportsRename", false)
            .put("SupportsUpdate", false)
            .put("SupportsUserInfo", false)
            // UI
            .put("PostMessageOrigin", getServerURL(request))
            .put("ClosePostMessage", true)
            // User
            .put("UserFriendlyName", spUser.getDisplayedName())
            .putJSONObject("UserExtraInfo", e ->
                e.put("avatar", getFullApplicationURL(request) + getImageURL(spUser.getAvatar(), "30x")))
            // User Permissions
            .put("ReadOnly", !canBeModifiedBy)
            .put("DisablePrint", webViewOnly)
            .put("DisableExport", webViewOnly)
            .put("RestrictedWebViewOnly", webViewOnly)
            .put("UserCanAttend", false)
            .put("UserCanNotWriteRelative", true)
            .put("UserCanPresent", false)
            .put("UserCanRename", false)
            .put("UserCanWrite", canBeModifiedBy);
        }
      );
      return Response.ok().type(MediaType.APPLICATION_JSON).entity(json).build();
    });
  }

  /**
   * @see
   * <a href="https://wopi.readthedocs.io/projects/wopirest/en/latest/endpoints.html#files-endpoint"> WOPI spec,
   * files endpoint</a>
   */
  @POST
  public Response receiveFileData() {
    return process(() -> {
      final HttpServletRequest request = getSilverpeasContext().getRequest();
      final String action = defaultStringIfNotDefined(request.getHeader(WOPI_OVERRIDE_HEADER));
      final WopiFile file = getEditionContext().getFile();
      final Optional<Response> response;
      if ("SP_CURRENT_USERS".equals(action)) {
        final Set<String> userIds = Stream
            .of(ofNullable(request.getHeader(WOPI_USER_IDS_HEADER)).orElse("").split("[, ;]"))
            .filter(StringUtil::isDefined)
            .map(String::trim)
            .collect(Collectors.toSet());
        getEditionManager().notifyEditionWith(file, userIds);
        response = of(Response.ok().build());
      } else if (action.contains("LOCK")) {
        response = lockManager.manage(request, action, file);
      } else {
        response = empty();
      }
      return response.orElseGet(() -> Response.status(Response.Status.NOT_IMPLEMENTED).build());
    });
  }

  /**
   * @see
   * <a href="https://wopi.readthedocs.io/projects/wopirest/en/latest/endpoints.html#file-contents-endpoint"> WOPI spec,
   * file contents endpoint</a>
   */
  @GET
  @Path("contents")
  public Response sendFileContentData() {
    return process(() -> {
      final WopiFileEditionContext context = getEditionContext();
      final StreamingOutput streamingOutput = o -> context.getFile().loadInto(o);
      return Response.ok(streamingOutput, APPLICATION_OCTET_STREAM).build();
    });
  }

  /**
   * @see
   * <a href="https://wopi.readthedocs.io/projects/wopirest/en/latest/endpoints.html#file-contents-endpoint"> WOPI spec,
   * file contents endpoint</a>
   */
  @POST
  @Path("contents")
  public Response receiveFileContentData() {
    return process(() -> {
      final HttpServletRequest request = getSilverpeasContext().getRequest();
      final WopiFileEditionContext context = getEditionContext();
      final WopiFile file = context.getFile();
      of(isLockCapabilityEnabled())
          .filter(b -> b)
          .map(b -> request.getHeader(WOPI_LOCK_HEADER))
          .filter(l -> (isDefined(l) && !file.lock().exists()) || !file.lock().id().equals(l))
          .ifPresent(l -> {
            logger().debug(() -> format("WRITE CONFLICT because of not corresponding LOCK {0} on file {1}", l, file));
            throw new WopiResponseError(Response.status(CONFLICT)
                .header(WOPI_LOCK_HEADER, file.lock().id())
                .build());
          });
      getTimestampVerificationElements().ifPresent(e -> {
        final String timestampHeaderValue = request.getHeader(e.getFirst());
        if (isDefined(timestampHeaderValue)) {
          final OffsetDateTime timestampToVerify = parse(timestampHeaderValue);
          logger().debug(() -> format("timestamp {0} verified on file {1}", timestampToVerify, file));
          if (!timestampToVerify.isEqual(file.lastModificationDate())) {
            logger().debug(() -> format("WRITE CONFLICT because of not corresponding timestamp {0} on file {1}", timestampToVerify, file));
            throw new WopiResponseError(Response.status(CONFLICT)
                .type(MediaType.APPLICATION_JSON).entity(e.getSecond())
                .build());
          }
        } else {
          logger().debug(() -> format("no timestamp verification on file {0}", file));
        }
      });
      try {
        file.updateFrom(getSilverpeasContext().getRequest().getInputStream());
      } catch (IOException e) {
        throw new WebApplicationException(e, Response.Status.NOT_FOUND);
      }
      getExitFieldNameDetection()
          .filter(f -> getBooleanValue(request.getHeader(f)))
          .ifPresent(f -> getEditionManager().revokeFile(file));
      final String json = JSONCodec.encodeObject(
          o -> o.put(LAST_MODIFIED_TIME_FIELD, toIso8601(file.lastModificationDate(), true)));
      return Response.ok().type(MediaType.APPLICATION_JSON).entity(json).build();
    });
  }
}
