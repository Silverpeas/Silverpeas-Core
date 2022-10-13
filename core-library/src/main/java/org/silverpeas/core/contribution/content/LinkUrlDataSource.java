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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.content;

import org.silverpeas.core.util.MemoizedSupplier;

import javax.activation.DataSource;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author silveryocha
 */
public class LinkUrlDataSource {
  private final String linkUrl;
  private final Supplier<DataSource> dataSource;

  public LinkUrlDataSource(final String linkUrl, final Supplier<DataSource> dataSource) {
    this.linkUrl = linkUrl;
    this.dataSource = dataSource instanceof MemoizedSupplier
        ? dataSource
        : new MemoizedSupplier<>(dataSource);
  }

  /**
   * Gets the link url.
   * @return a string.
   */
  public String getLinkUrl() {
    return linkUrl;
  }

  /**
   * Gets the related datasource of link url.
   * @return the initialized {@link DataSource} instance.
   */
  public DataSource getDataSource() {
    return dataSource.get();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final LinkUrlDataSource that = (LinkUrlDataSource) o;
    return Objects.equals(linkUrl, that.linkUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(linkUrl);
  }
}
