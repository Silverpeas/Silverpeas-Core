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

import org.apache.commons.io.IOUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;

import static java.time.OffsetDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;

/**
 * @author silveryocha
 */
public class SimpleWbeFile extends WbeFile {

  private final File file;

  public SimpleWbeFile(final File file) {
    this.file = file;
  }

  @Override
  public Optional<ResourceReference> linkedToResource() {
    return Optional.empty();
  }

  @Override
  public String silverpeasId() {
    return StringUtil.asBase64(name().getBytes(Charsets.UTF_8)).replace("=", "-");
  }

  @Override
  public User owner() {
    return User.getById("0");
  }

  @Override
  public String name() {
    return file.getName();
  }

  @Override
  public String mimeType() {
    return FileUtil.getMimeType(file.getPath()) ;
  }

  @Override
  public long size() {
    return file.length();
  }

  @Override
  public OffsetDateTime lastModificationDate() {
    return ofInstant(new Date(file.lastModified()).toInstant(), systemDefault());
  }

  @Override
  public void updateFrom(final InputStream input) throws IOException {
    try (final OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
      IOUtils.copy(input, stream);
    }
  }

  @Override
  public void loadInto(final OutputStream output) throws IOException {
    try (final InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
      IOUtils.copy(stream, output);
    }
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return true;
  }
}
