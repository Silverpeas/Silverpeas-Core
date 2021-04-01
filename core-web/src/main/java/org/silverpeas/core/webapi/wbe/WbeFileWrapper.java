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

package org.silverpeas.core.webapi.wbe;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeFileLock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * This WRAPPER is used by Web Browser Edition host and gives the possibility to add functionality
 * contextualized to a WbeFile.
 * @author silveryocha
 */
public abstract class WbeFileWrapper extends WbeFile {

  private final WbeFile wbeFile;

  protected WbeFileWrapper(final WbeFile wbeFile) {
    this.wbeFile = wbeFile;
  }

  @Override
  public Optional<ResourceReference> linkedToResource() {
    return wbeFile.linkedToResource();
  }

  @Override
  public String silverpeasId() {
    return wbeFile.silverpeasId();
  }

  @Override
  public String id() {
    return wbeFile.id();
  }

  @Override
  public User owner() {
    return wbeFile.owner();
  }

  @Override
  public String name() {
    return wbeFile.name();
  }

  @Override
  public String ext() {
    return wbeFile.ext();
  }

  @Override
  public String mimeType() {
    return wbeFile.mimeType();
  }

  @Override
  public long size() {
    return wbeFile.size();
  }

  @Override
  public OffsetDateTime lastModificationDate() {
    return wbeFile.lastModificationDate();
  }

  @Override
  public String version() {
    return wbeFile.version();
  }

  @Override
  public void updateFrom(final InputStream input) throws IOException {
    wbeFile.updateFrom(input);
  }

  @Override
  public void loadInto(final OutputStream output) throws IOException {
    wbeFile.loadInto(output);
  }

  @Override
  public WbeFileLock lock() {
    return wbeFile.lock();
  }

  @Override
  public String toString() {
    return wbeFile.toString();
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return wbeFile.canBeAccessedBy(user);
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return wbeFile.canBeModifiedBy(user);
  }

  @Override
  public boolean canBeDeletedBy(final User user) {
    return wbeFile.canBeDeletedBy(user);
  }
}
