/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.pdcPeas.vo;

import java.util.List;

public class ResultGroupFilter {
  private List<AuthorVO> authors = null;
  private List<ComponentVO> components = null;
  private List<String> year = null;

  /**
   * Default constructor
   */
  public ResultGroupFilter() {
    super();
  }

  public List<AuthorVO> getAuthors() {
    return authors;
  }

  public void setAuthors(List<AuthorVO> authors) {
    this.authors = authors;
  }

  public List<ComponentVO> getComponents() {
    return components;
  }

  public void setComponents(List<ComponentVO> components) {
    this.components = components;
  }

  public List<String> getYear() {
    return year;
  }

  public void setYear(List<String> year) {
    this.year = year;
  }

}
