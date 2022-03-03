/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.wopi.WopiFile;
import org.silverpeas.core.wopi.WopiFileLock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

/**
 * This WRAPPER is used by WOPI host and gives the possibility to add functionality
 * contextualized to a WopiFile.
 * @author silveryocha
 */
public class WopiFileWrapper extends WopiFile {

  private final WopiFile wopiFile;

  WopiFileWrapper(final WopiFile wopiFile) {
    this.wopiFile = wopiFile;
  }

  @Override
  public Optional<ResourceReference> linkedToResource() {
    return wopiFile.linkedToResource();
  }

  @Override
  public String silverpeasId() {
    return wopiFile.silverpeasId();
  }

  @Override
  public String id() {
    return wopiFile.id();
  }

  @Override
  public User owner() {
    return wopiFile.owner();
  }

  @Override
  public String name() {
    return wopiFile.name();
  }

  @Override
  public String ext() {
    return wopiFile.ext();
  }

  @Override
  public String mimeType() {
    return wopiFile.mimeType();
  }

  @Override
  public long size() {
    return wopiFile.size();
  }

  /**
   * Gets the last modification date with second precision.
   * @return an {@link OffsetDateTime} with cleared milliseconds.
   */
  @Override
  public OffsetDateTime lastModificationDate() {
    return wopiFile.lastModificationDate().withNano(0).withOffsetSameInstant(UTC);
  }

  @Override
  public String version() {
    return wopiFile.version();
  }

  @Override
  public void updateFrom(final InputStream input) throws IOException {
    wopiFile.updateFrom(input);
  }

  @Override
  public void loadInto(final OutputStream output) throws IOException {
    wopiFile.loadInto(output);
  }

  @Override
  public WopiFileLock lock() {
    return wopiFile.lock();
  }

  @Override
  public String toString() {
    return wopiFile.toString();
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return wopiFile.canBeAccessedBy(user);
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return wopiFile.canBeModifiedBy(user);
  }

  @Override
  public boolean canBeDeletedBy(final User user) {
    return wopiFile.canBeDeletedBy(user);
  }
}
