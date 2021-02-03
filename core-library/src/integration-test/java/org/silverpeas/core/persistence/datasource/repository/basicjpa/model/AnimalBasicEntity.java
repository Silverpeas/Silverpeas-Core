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
package org.silverpeas.core.persistence.datasource.repository.basicjpa.model;

import org.hibernate.LazyInitializationException;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.repository.EquipmentBasicEntityRepository;
import org.silverpeas.core.util.ServiceProvider;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Entity
@Table(name = "test_animals")
@NamedQueries({@NamedQuery(name = "getAnimalsByTypeCustom", query =
    "from AnimalBasicEntity a where a.type = " + ":type"),
    @NamedQuery(name = "getAnimalsByNameCustom", query =
        "from AnimalBasicEntity a where a.name = " + ":name"),
    @NamedQuery(name = "updateAnimalNameCustom",
        query = "update AnimalBasicEntity a set a.name = :name where a.id = :id"),
    @NamedQuery(name = "deleteAnimalsByTypeCustom", query = "delete from AnimalBasicEntity a " +
        "where a.type = :type")})
public class AnimalBasicEntity
    extends BasicJpaEntity<AnimalBasicEntity, UniqueLongIdentifier>
    implements Serializable {

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "name", nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "personId", referencedColumnName = "id", nullable = false)
  private PersonBasicEntity person;

  @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
  private List<EquipmentBasicEntity> equipments;

  public AnimalTypeBasicEntity getType() {
    return AnimalTypeBasicEntity.valueOf(type);
  }

  public AnimalBasicEntity setType(final AnimalTypeBasicEntity type) {
    this.type = type.name();
    return this;
  }

  public String getName() {
    return name;
  }

  public AnimalBasicEntity setName(final String name) {
    this.name = name;
    return this;
  }

  public PersonBasicEntity getPerson() {
    return person;
  }

  public AnimalBasicEntity setPerson(final PersonBasicEntity personBasicEntity) {
    this.person = personBasicEntity;
    return this;
  }

  public List<EquipmentBasicEntity> getEquipments() {
    return equipments;
  }

  public AnimalBasicEntity setEquipments(final List<EquipmentBasicEntity> equipmentCustomEntities) {
    this.equipments = equipmentCustomEntities;
    return this;
  }

  public AnimalBasicEntity copy() {
    AnimalBasicEntity entity = new AnimalBasicEntity();
    entity.type = type;
    entity.name = name;
    entity.person = person;
    entity.equipments =
        equipments.stream().map(EquipmentBasicEntity::copy).collect(Collectors.toList());
    return entity;
  }
}