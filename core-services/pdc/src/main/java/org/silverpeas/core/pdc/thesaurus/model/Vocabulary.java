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
package org.silverpeas.core.pdc.thesaurus.model;

import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.kernel.annotation.NonNull;

import java.util.Objects;

/**
 * This class contains a full information about a Vocabulary
 */

@SuppressWarnings("deprecation")
public class Vocabulary extends SilverpeasBean implements Comparable<Vocabulary> {

  private static final long serialVersionUID = -5979441125808657400L;
  private String name;
  private String description;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Vocabulary other = (Vocabulary) obj;
    return Objects.equals(this.name, other.name);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
    return hash;
  }

  @Override
  public int compareTo(Vocabulary voca) {
    return this.getName().compareTo(voca.getName());
  }

  @Override
  @NonNull
  protected String getTableName() {
    return "SB_Thesaurus_Vocabulary";
  }

}