/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.core.persistence.datasource.repository.basicjpa.model;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaCustomEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Entity
@Table(name = "test_animals")
@NamedQueries({@NamedQuery(name = "getAnimalsByTypeCustom", query =
    "from AnimalCustomEntity a where a.type = " + ":type"),
    @NamedQuery(name = "getAnimalsByNameCustom", query =
        "from AnimalCustomEntity a where a.name = " + ":name"),
    @NamedQuery(name = "updateAnimalNameCustom",
        query = "update AnimalCustomEntity a set a.name = :name where a.id = :id"),
    @NamedQuery(name = "deleteAnimalsByTypeCustom", query = "delete from AnimalCustomEntity a " +
        "where a.type = :type")})
public class AnimalCustomEntity
    extends AbstractJpaCustomEntity<AnimalCustomEntity, UniqueLongIdentifier>
    implements Serializable {

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "name", nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "personId", referencedColumnName = "id", nullable = false)
  private PersonCustomEntity person;

  @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
  private List<EquipmentCustomEntity> equipments;

  public AnimalTypeCustomEntity getType() {
    return AnimalTypeCustomEntity.valueOf(type);
  }

  public AnimalCustomEntity setType(final AnimalTypeCustomEntity type) {
    this.type = type.name();
    return this;
  }

  public String getName() {
    return name;
  }

  public AnimalCustomEntity setName(final String name) {
    this.name = name;
    return this;
  }

  public PersonCustomEntity getPerson() {
    return person;
  }

  public AnimalCustomEntity setPerson(final PersonCustomEntity personCustomEntity) {
    this.person = personCustomEntity;
    return this;
  }

  public List<EquipmentCustomEntity> getEquipments() {
    return equipments;
  }

  public AnimalCustomEntity setEquipments(
      final List<EquipmentCustomEntity> equipmentCustomEntities) {
    this.equipments = equipmentCustomEntities;
    return this;
  }
}