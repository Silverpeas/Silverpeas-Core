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
package org.silverpeas.core.persistence.datasource.repository.jpa.model;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Entity
@Table(name = "test_animals")
@NamedQueries({@NamedQuery(name = "getAnimalsByType", query = "from Animal a where a.type = :type"),
    @NamedQuery(name = "getAnimalsByName", query = "from Animal a where a.name = :name"),
    @NamedQuery(name = "updateAnimalName",
        query = "update Animal a set a.name = :name, " +
            "a.lastUpdatedBy = :lastUpdatedBy, a.lastUpdateDate = :lastUpdateDate, " +
            "a.version = :version where a.id = :id"),
    @NamedQuery(name = "deleteAnimalsByType", query = "delete from Animal a where a.type = :type")})
public class Animal extends AbstractJpaEntity<Animal, UniqueLongIdentifier> implements
    Serializable {

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "name", nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "personId", referencedColumnName = "id", nullable = false)
  private Person person;

  @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
  private List<Equipment> equipments;

  public AnimalType getType() {
    return AnimalType.valueOf(type);
  }

  public Animal setType(final AnimalType type) {
    this.type = type.name();
    return this;
  }

  public String getName() {
    return name;
  }

  public Animal setName(final String name) {
    this.name = name;
    return this;
  }

  public Person getPerson() {
    return person;
  }

  public Animal setPerson(final Person person) {
    this.person = person;
    return this;
  }

  public List<Equipment> getEquipments() {
    return equipments;
  }

  public Animal setEquipments(final List<Equipment> equipments) {
    this.equipments = equipments;
    return this;
  }
}