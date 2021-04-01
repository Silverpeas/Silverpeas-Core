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

package org.silverpeas.core.wbe;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.StringJoiner;

import static java.time.ZoneOffset.UTC;

/**
 * Representation of a Silverpeas's file exposed to the Office infrastructure.
 * <p>
 * The instance is not immutable and data returned by different signatures of a same instance MAY
 * not the same during the time.
 * </p>
 * @author silveryocha
 */
public abstract class WbeFile implements Securable {

  private OffsetDateTime lastEditionDate;
  private WbeFileLock lock;

  /**
   * Gets the date of last edition.
   * @return an {@link OffsetDateTime} instance.
   */
  public OffsetDateTime getLastEditionDate() {
    return this.lastEditionDate;
  }

  /**
   * Gets the optional resource reference the file is linked to.
   * @return an optional {@link ResourceReference}.
   */
  public abstract Optional<ResourceReference> linkedToResource();

  /**
   * Silverpeas's identifier identifies the file from point of view of Silverpeas's platform.
   * <p>
   * In most of case, {@link #id()} will return same identifier as {@link #silverpeasId()} returns.
   * </p>
   * <p>
   * But sometimes, when a temporary view exists for a file and that the temporary view is the
   * content exposed for modifications, {@link #silverpeasId()} returns the identifier of the
   * document and {@link #id()} returns the identifier of the view (the one used by Web Browser Edition exchanges).
   * </p>
   * @return a unique identifier as string.
   */
  public abstract String silverpeasId();

  /**
   * A File ID is a string that represents a file being operated on via WBE operations. A host
   * must issue a unique ID for any file used by a WBE client. The client will, in turn, include
   * the file ID when making requests to the WBE host. Thus, a host must be able to use the file
   * ID to locate a particular file.
   * <p>
   * A file ID must:
   * <ul>
   * <li>Represent a single file.</li>
   * <li>Be an URL-safe string because IDs are passed in URLs</li>
   * <li>Remain the same when the file is edited.</li>
   * <li>Remain the same when the file is moved or renamed.</li>
   * <li>Remain the same when any ancestor container, including the parent container, is renamed
   * .</li>
   * <li>In the case of shared files, the ID for a given file must be the same for every user that
   * accesses the file.</li>
   * </ul>
   * </p>
   * @return a unique identifier as string.
   */
  public String id() {
    return silverpeasId();
  }

  /**
   * A string that uniquely identifies the owner of the file. In most cases, the user who
   * uploaded or created the file should be considered the owner.
   * @return the {@link User} which is the owner.
   */
  public abstract User owner();

  /**
   * Gets the string name of the file, including extension, without a path. Used for display in user
   * interface (UI), and determining the extension of the file.
   * @return a string representing a file name.
   */
  public abstract String name();

  /**
   * Gets the extension of the file.
   * @return a string representing a mime-type.
   */
  public String ext() {
    return FilenameUtils.getExtension(name());
  }

  /**
   * Gets the mime type of the file.
   * @return a string representing a mime-type.
   */
  public abstract String mimeType();

  /**
   * Gets the size of the file in bytes.
   * @return a long representing a file content length.
   */
  public abstract long size();

  /**
   * Gets an {@link OffsetDateTime} instance that represents the last time that the file was
   * modified.
   * @return an {@link OffsetDateTime}.
   */
  public abstract OffsetDateTime lastModificationDate();

  /**
   * The current version of the file based on the server’s file version schema, as a string. This
   * value must change when the file changes, and version values must never repeat for a given file.
   * @return a version value as string.
   */
  public String version() {
    return StringUtil.asBase64((name() + "#" +
        lastModificationDate().withOffsetSameInstant(UTC)).getBytes(Charsets.UTF_8));
  }

  /**
   * Updates the content of underlying Silverpeas's file from data provided by the given
   * {@link InputStream}.
   * @param input an {@link InputStream} from which the data are written.
   * @throws IOException when it is not possible to write into the physical file.
   */
  public abstract void updateFrom(final InputStream input) throws IOException;

  /**
   * Loads the content of the underlying Silverpeas's file into the given {@link OutputStream}.
   * @param output the stream into which content file MUST be loaded.
   * @throws IOException when it is not possible to read the physical file.
   */
  public abstract void loadInto(final OutputStream output) throws IOException;

  /**
   * Gets the current lock identifier.
   * @return a string.
   */
  public WbeFileLock lock() {
    if (lock == null) {
      lock = new WbeFileLock();
    }
    return lock;
  }

  /**
   * Updates the last edition date with the current date and time.
   */
  public void setLastEditionDateAtNow() {
    this.lastEditionDate = OffsetDateTime.now();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", WbeFile.class.getSimpleName() + "[", "]")
        .add("silverpeasId=" + silverpeasId())
        .add("id=" + id())
        .add("name=" + name())
        .add("lastModificationDate=" + lastModificationDate())
        .add("mimeType=" + mimeType())
        .add("size=" + size())
        .add("version=" + version())
        .add("lock=" + lock())
        .add("lastEditionDate=" + getLastEditionDate())
        .toString();
  }
}
