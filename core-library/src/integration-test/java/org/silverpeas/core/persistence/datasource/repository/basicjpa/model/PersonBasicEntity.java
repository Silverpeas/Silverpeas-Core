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
package org.silverpeas.core.persistence.datasource.repository.basicjpa.model;

import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Entity
@Table(name = "test_persons")
@NamedQuery(name = "getPersonsByNameCustom",
    query = "select p from PersonBasicEntity p where p.lastName = :name")
public class PersonBasicEntity extends BasicJpaEntity<PersonBasicEntity, UuidIdentifier>
    implements Serializable {

  @Column(name = "firstName", nullable = false)
  @NotNull
  private String firstName;

  @Column(name = "lastName", nullable = false)
  @NotNull
  private String lastName;

  @OneToMany(mappedBy = "person", fetch = FetchType.EAGER)
  private List<AnimalBasicEntity> animals;

  @Override
  public PersonBasicEntity setId(final String id) {
    return super.setId(id);
  }

  public String getFirstName() {
    return firstName;
  }

  public PersonBasicEntity setFirstName(final String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public PersonBasicEntity setLastName(final String lastName) {
    this.lastName = lastName;
    return this;
  }

  public List<AnimalBasicEntity> getAnimals() {
    return animals;
  }

  public PersonBasicEntity setAnimals(final List<AnimalBasicEntity> animalCustomEntities) {
    this.animals = animalCustomEntities;
    return this;
  }

  public PersonBasicEntity copy() {
    PersonBasicEntity entity = new PersonBasicEntity();
    entity.firstName = firstName;
    entity.lastName = lastName;
    entity.animals = animals.stream().map(AnimalBasicEntity::copy).collect(Collectors.toList());
    return entity;
  }
}