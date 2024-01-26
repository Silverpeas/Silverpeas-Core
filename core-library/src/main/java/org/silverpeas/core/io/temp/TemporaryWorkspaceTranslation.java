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
package org.silverpeas.core.io.temp;

import org.apache.commons.io.FileUtils;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.text.MessageFormat.format;
import static org.silverpeas.core.util.SerializationUtil.deserializeFromString;
import static org.silverpeas.core.util.SerializationUtil.serializeAsString;
import static org.silverpeas.core.util.file.FileRepositoryManager.getTemporaryPath;

/**
 * If a treatment uses a real resource identifier for a temporary resource, it is possible
 * that someone else accesses this temporary resource without any right.<br>
 * So, this class provides a set of tool to handle workspace which the root path of the workspace
 * is translated.<br>
 * @author Yohann Chastagnier
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class TemporaryWorkspaceTranslation {

  private static final String TRANSLATION_ID = "__sptrans_id";
  private static final String SILVERPEAS_TRANSLATION_PREFIX = "__sptrans_";

  private final File descriptor;
  private File workspace;
  private Map<String, String> descriptorContent = new HashMap<>();
  private boolean workInProgress = false;
  private Object lock = new Object();

  /**
   * Indicates if the given path corresponds to a temporary workspace translation.
   * @param path the path to verify.
   * @return true if the given path is a translation, false otherwise.
   */
  public static boolean startWithTranslationDescriptorPrefix(final String path) {
    return StringUtil.isDefined(path) &&
        path.replaceAll("^/", "").startsWith(SILVERPEAS_TRANSLATION_PREFIX);
  }

  /**
   * Gets an instance from the given identifier.
   * @param identifier an identifier.
   * @return an instance of {@link TemporaryWorkspaceTranslation}.
   */
  public static TemporaryWorkspaceTranslation from(final String identifier) {
    return new TemporaryWorkspaceTranslation(identifier);
  }

  private TemporaryWorkspaceTranslation(final String id) {
    this.descriptor = new File(getTemporaryPath(), checkedPath(SILVERPEAS_TRANSLATION_PREFIX + id));
    initialize();
  }

  /**
   * Centralizes the initialization.
   */
  private void initialize() {
    synchronized (lock) {
      if (descriptor.isFile() && descriptor.length() > 30) {
        try {
          for (String line : FileUtils.readLines(this.descriptor, Charsets.UTF_8)) {
            int splitIndex = line.indexOf('=');
            if (splitIndex > 0) {
              descriptorContent.put(line.substring(0, splitIndex), line.substring(splitIndex + 1));
            }
          }
        } catch (IOException e) {
          throw new SilverpeasRuntimeException(e);
        }
      } else {
        descriptorContent.put(TRANSLATION_ID, UUID.randomUUID().toString());
      }
      this.workspace = new File(getTemporaryPath(), checkedPath(descriptorContent.get(TRANSLATION_ID)));
    }
  }

  private String checkedPath(final String path) {
    final String pathToCheck = StringUtil.defaultStringIfNotDefined(path, "unknown");
    if (pathToCheck.contains("..")) {
      final String errorMsg = format("Path Traversal attack detected at {0}", LocalDateTime.now());
      SilverLogger.getLogger("silverpeas.core.security").error(errorMsg);
      throw new SilverpeasRuntimeException(errorMsg);
    }
    return pathToCheck;
  }

  /**
   * Removes the workspace from the filesystem.
   * @return true if deletion is completely effective, false otherwise.
   */
  public boolean remove() {
    synchronized (lock) {
      boolean result = FileUtils.deleteQuietly(workspace) && FileUtils.deleteQuietly(descriptor);
      initialize();
      return result;
    }
  }

  /**
   * Indicates if the workspace exists.
   * @return true if it exists, false otherwise.
   */
  public boolean exists() {
    synchronized (lock) {
      return descriptor.isFile() && descriptor.length() > 30 && workspace.exists();
    }
  }

  /**
   * Indicates if the workspace is empty.
   * @return true if it is empty, false otherwise.
   */
  public boolean empty() {
    synchronized (lock) {
      return exists() && FileUtils.sizeOfDirectory(workspace) == 0;
    }
  }

  /**
   * Gets the last time the workspace was modified.
   * @return
   */
  public long lastModified() {
    synchronized (lock) {
      return exists() ? descriptor.lastModified() : 0;
    }
  }

  /**
   * Updates the last modified date of workspace.
   */
  public void updateLastModifiedDate() {
    LastModifiedDateFileTask.addFile(descriptor);
    LastModifiedDateFileTask.addFile(workspace);
  }

  /**
   * Gets the root path of the workspace.
   * @return
   */
  public File getRootPath() {
    return workspace;
  }

  /**
   * Create the workspace.
   */
  public void create() {
    synchronized (lock) {
      if (!exists()) {
        workspace.mkdirs();
        saveDescriptor();
      }
    }
  }

  /**
   * Saves on filesystem the descriptor content.
   */
  private void saveDescriptor() {
    synchronized (lock) {
      try {
        if (descriptorContent.containsKey(TRANSLATION_ID)) {
          StringBuilder content = new StringBuilder();
          for (Map.Entry<String, String> line : descriptorContent.entrySet()) {
            if (content.length() > 0) {
              content.append("\n");
            }
            content.append(line.getKey()).append("=").append(line.getValue());
          }
          FileUtils.writeStringToFile(descriptor, content.toString(), Charsets.UTF_8);
        }
      } catch (IOException e) {
        SilverLogger.getLogger(this).silent(e);
      }
    }
  }

  public void markWorkInProgress() {
    synchronized (lock) {
      workInProgress = true;
    }
  }

  private void markWorkNoMoreInProgress() {
    synchronized (lock) {
      workInProgress = false;
    }
  }

  public boolean isWorkInProgress() {
    synchronized (lock) {
      return workInProgress;
    }
  }

  /**
   * Puts into workspace a string value linked to a key.
   * @param key the key.
   * @param value the value.
   * @return the current instance.
   */
  public TemporaryWorkspaceTranslation put(String key, Serializable value) {
    if (value == null) {
      descriptorContent.remove(key);
    } else {
      descriptorContent.put(key, serializeAsString(value));
    }
    if (exists()) {
      saveDescriptor();
    }
    markWorkNoMoreInProgress();
    return this;
  }

  /**
   * Gets from context a value from a key that has been stored into the context instance.
   * @param key the key associated to the searched value.
   * @return the value if any, null if the expected type does not match with the one of the existing
   * value.
   */
  @SuppressWarnings("unchecked")
  public <T extends Serializable> T get(String key) {
    try {
      Object value = descriptorContent.get(key);
      if (value == null) {
        return null;
      }
      return deserializeFromString(descriptorContent.get(key));
    } catch (RuntimeException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String toString() {
    return descriptor.getName() + " -> " + workspace.getName();
  }
}
