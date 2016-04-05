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
package org.silverpeas.core.io.upload;

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.io.FileUtils;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.silverpeas.core.security.authorization.AccessControllerProvider.getAccessController;
import static org.silverpeas.core.admin.service.OrganizationControllerProvider.getOrganisationController;

/**
 * Manage a session of file & folder upload. Each file is saved in a temporary folder the server.
 * The different treatments which use this mechanism must use all the services provided by {@link
 * FileUploadManager} and {@link UploadedFile} in order to save definitely the uploaded files.
 * @author Yohann Chastagnier
 */
public class UploadSession {

  final static String X_UPLOAD_SESSION = "X-UPLOAD-SESSION";

  private static final String SESSION_CACHE_KEY = "@@@_" + UploadSession.class.getName();
  private static final String UPLOAD_SESSION_CACHE_KEY_PREFIX = "@@@_instance_for_";

  private String uploadSessionId;
  private File uploadSessionFolder;
  private String componentInstanceId;
  private Map<String, String> componentInstanceParameters = new HashMap<String, String>();
  private Map<String, Boolean> currentFileWritings = new ConcurrentHashMap<String, Boolean>();

  /**
   * Hidden constructor.
   * @param uploadSessionId
   */
  private UploadSession(String uploadSessionId) {
    this.uploadSessionId = uploadSessionId;
  }

  /**
   * Sets the component instance id associated to the upload session.
   * @param componentInstanceId
   */
  public UploadSession forComponentInstanceId(final String componentInstanceId) {
    this.componentInstanceId = componentInstanceId;
    return this;
  }

  /**
   * Indicates if the current user is authorized to perform this upload session.
   * @param componentInstanceId
   */
  public boolean isUserAuthorized(final String componentInstanceId) {
    return StringUtil.isDefined(getComponentInstanceId()) &&
        getComponentInstanceId().equals(componentInstanceId) &&
        getAccessController(ComponentAccessControl.class)
            .isUserAuthorized(UserDetail.getCurrentRequester().getId(), componentInstanceId);
  }

  /**
   * Gets the session identifier.
   * @return a string.
   */
  public String getId() {
    return uploadSessionId;
  }

  /**
   * Gets the component instance id if any associated.
   * @return a string.
   */
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * Gets the parameter value of a component instance. The component instance is the one passed on
   * file uploads. If no component instance identifier has been passed, then nothing is done.
   * @return a string that represents the parameter value, empty value if the component instance
   * identifier is unknown or if the parameter is not defined for component instance.
   */
  public String getComponentInstanceParameterValue(String parameterName) {
    String parameterValue = null;
    if (StringUtil.isDefined(getComponentInstanceId())) {
      if (!componentInstanceParameters.containsKey(parameterName)) {
        parameterValue = getOrganisationController()
            .getComponentParameterValue(getComponentInstanceId(), parameterName);
        componentInstanceParameters.put(parameterName, parameterValue);
      } else {
        parameterValue = componentInstanceParameters.get(parameterName);
      }
    }
    return parameterValue;
  }

  /**
   * Indicates if the upload session has been physically performed.
   * @return true if it is, false otherwise.
   */
  private boolean isHandledOnFileSystem() {
    return uploadSessionFolder != null;
  }

  /**
   * Clears the session (deletes all the file from the server).
   */
  public void clear() {
    if (isHandledOnFileSystem()) {
      FileUtils.deleteQuietly(uploadSessionFolder);
      removeSessionFromCache(this);
      uploadSessionFolder = null;
    }
  }

  /**
   * Removes from the upload session the file identified by the given identifier.
   * If the file path is currently in writing mode, nothing is removed.
   * @param fullPath the path of the file into the session.
   * @return true of removed has been effective, false otherwise.
   * @throws Exception
   */
  public synchronized boolean remove(String fullPath) throws Exception {
    boolean removed = false;
    if (isHandledOnFileSystem()) {
      UploadSessionFile uploadSessionFile = getUploadSessionFile(fullPath);
      if (!currentFileWritings.containsKey(fullPath)) {
        removed = FileUtils.deleteQuietly(uploadSessionFile.getServerFile());
      }
    }
    return removed;
  }

  /**
   * Gets a new upload file from the session.
   * If the file path is currently in writing mode, the the {@link
   * UploadSessionFile#getServerFile()} of the returned instance is null.
   * @param fullPath the path of the file into the session.
   * @return
   * @throws Exception
   */
  public synchronized UploadSessionFile getUploadSessionFile(String fullPath) throws Exception {
    initialize();
    return new UploadSessionFile(this, fullPath, new File(uploadSessionFolder, fullPath));
  }

  /**
   * Gets the root folder on the server of the upload session.<br/>
   * If the folder does not yet exist, then it is created.
   * @return a {@link File} that represents the upload session folder.
   */
  public File getRootFolder() {
    initialize();
    return uploadSessionFolder;
  }

  /**
   * Gets the {@link File} list (so file or folder) from the root folder on the server of the
   * upload
   * session.<br/>
   * If the folder does not yet exist, then it is created.
   * @return a list of {@link File} from the root folder provided by {@link #getRootFolder()}.
   */
  public File[] getRootFolderFiles() {
    File[] files = getRootFolder().listFiles();
    if (files == null) {
      files = new File[0];
    }
    return files;
  }

  /**
   * Initializes the session file structure.
   */
  private synchronized void initialize() {
    if (!isHandledOnFileSystem()) {
      uploadSessionFolder = new File(FileRepositoryManager.getTemporaryPath(), uploadSessionId);
      if (!uploadSessionFolder.exists()) {
        uploadSessionFolder.mkdirs();
      }
    }
  }

  void markFileWritingInProgress(UploadSessionFile uploadSessionFile) throws IOException {
    String fullPath = uploadSessionFile.getFullPath();
    if (currentFileWritings.containsKey(fullPath)) {
      throw new IOException("An other file with the same name is currently updated (" +
          fullPath + ")");
    }
    currentFileWritings.put(fullPath, true);
  }

  void markFileWritingDone(UploadSessionFile uploadSessionFile) throws IOException {
    currentFileWritings.remove(uploadSessionFile.getFullPath());
  }

  /**
   * Initializes an instance from a request (if not created, a new one will be created if
   * necessary).
   * @param request an http servlet request.
   * @return a new initialized instance.
   */
  public static UploadSession from(HttpServletRequest request) {
    String uploadSessionId = request.getHeader(X_UPLOAD_SESSION);
    if (StringUtil.isNotDefined(uploadSessionId)) {
      uploadSessionId = request.getParameter(X_UPLOAD_SESSION);
    }
    return from(uploadSessionId);
  }

  /**
   * Initializes an instance from a session id (if not created, a new one will be created if
   * necessary).
   * @param uploadSessionId an existing, or not, upload session id.
   * @return a new initialized instance.
   */
  public static UploadSession from(String uploadSessionId) {
    UploadSession uploadSession = getSessionFromCache(uploadSessionId);
    if (uploadSession == null) {
      uploadSession = new UploadSession(handleUploadSessionId(uploadSessionId));
      registerSessionInCache(uploadSession);
    }
    return uploadSession;
  }

  /**
   * Creates a new upload session id if given is not defined.
   * Returns the given one if it is defined.
   * @param uploadSessionIdFromRequest
   * @return the session upload id as string.
   */
  private static String handleUploadSessionId(String uploadSessionIdFromRequest) {
    return StringUtil.isDefined(uploadSessionIdFromRequest) ? uploadSessionIdFromRequest :
        UUID.randomUUID().toString();
  }

  /**
   * Gets the upload session corresponding to the given identifier if any.
   * @param uploadSessionId an upload session identifier.
   * @return an upload session instance if any, null otherwise.
   */
  private static UploadSession getSessionFromCache(String uploadSessionId) {
    return CacheServiceProvider.getSessionCacheService()
        .get(UPLOAD_SESSION_CACHE_KEY_PREFIX + uploadSessionId, UploadSession.class);
  }

  /**
   * Register the session in cache in order to clean the session folders in case of user
   * disconnection during an upload.
   * @param uploadSession
   */
  @SuppressWarnings("unchecked")
  private static void registerSessionInCache(UploadSession uploadSession) {
    Set<String> sessionIds =
        CacheServiceProvider.getSessionCacheService().get(SESSION_CACHE_KEY, Set.class);
    if (sessionIds == null) {
      sessionIds = new HashSet<String>();
      CacheServiceProvider.getSessionCacheService().put(SESSION_CACHE_KEY, sessionIds);
    }
    sessionIds.add(uploadSession.uploadSessionId);
    CacheServiceProvider.getSessionCacheService()
        .put(UPLOAD_SESSION_CACHE_KEY_PREFIX + uploadSession.getId(), uploadSession);
  }

  /**
   * Register the session in cache in order to clean the session folders in case of user
   * disconnection during an upload.
   * @param uploadSession
   */
  @SuppressWarnings("unchecked")
  private static void removeSessionFromCache(UploadSession uploadSession) {
    Set<String> sessionIds =
        CacheServiceProvider.getSessionCacheService().get(SESSION_CACHE_KEY, Set.class);
    if (sessionIds != null) {
      sessionIds.remove(uploadSession.getId());
      CacheServiceProvider.getSessionCacheService()
          .remove(UPLOAD_SESSION_CACHE_KEY_PREFIX + uploadSession.getId());
    }
  }

  /**
   * Clears the upload sessions still attached to a user session.
   * @param sessionInfo the session of a user.
   */
  @SuppressWarnings("unchecked")
  public static void clearFrom(SessionInfo sessionInfo) {
    Set<String> sessionIds = sessionInfo.getCache().get(SESSION_CACHE_KEY, Set.class);
    if (sessionIds != null) {
      for (String uploadSessionId : new ArrayList<String>(sessionIds)) {
        try {
          UploadSession.from(uploadSessionId).clear();
        } catch (Exception ignore) {
        }
      }
      sessionInfo.getCache().remove(SESSION_CACHE_KEY);
    }
  }
}
