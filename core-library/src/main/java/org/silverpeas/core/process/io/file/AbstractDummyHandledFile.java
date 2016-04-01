/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.process.io.file;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractDummyHandledFile implements DummyHandledFile {

  @Override
  public boolean isDeleted() {
    // By default, a dummy handled file is not deleted
    return false;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DummyHandledFile other = (DummyHandledFile) obj;
    EqualsBuilder matcher = new EqualsBuilder();
    matcher.append(getComponentInstanceId(), other.getComponentInstanceId());
    matcher.append(getPath(), other.getPath());
    matcher.append(getName(), other.getName());
    matcher.append(isDeleted(), other.isDeleted());
    return matcher.isEquals();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder hash = new HashCodeBuilder();
    hash.append(getComponentInstanceId());
    hash.append(getPath());
    hash.append(getName());
    hash.append(isDeleted());
    return hash.toHashCode();
  }
}
