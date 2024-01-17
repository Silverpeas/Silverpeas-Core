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
package org.silverpeas.core.persistence.datasource.repository.jpa.model;

import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Entity
@Table(name = "test_equipments")
public class Equipment extends SilverpeasJpaEntity<Equipment, UuidIdentifier> implements
    Serializable {

  @Column(name = "name", nullable = false)
  private String name;

  @Embedded
  private Period rentPeriod = Period.between(LocalDate.MIN, LocalDate.MAX);

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "animalId", referencedColumnName = "id", nullable = false)
  private Animal animal;

  public String getName() {
    return name;
  }

  public Equipment setName(final String name) {
    this.name = name;
    return this;
  }

  public Equipment rentOver(Period period) {
    this.rentPeriod = period;
    return this;
  }

  public Period getRentPeriod() {
    return rentPeriod;
  }

  public Animal getAnimal() {
    return animal;
  }

  public Equipment setAnimal(final Animal animal) {
    this.animal = animal;
    return this;
  }

  public Equipment copy() {
    Equipment copy = new Equipment();
    copy.name = name;
    copy.animal = animal;
    return copy;
  }
}