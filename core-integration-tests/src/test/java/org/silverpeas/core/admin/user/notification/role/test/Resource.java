/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.admin.user.notification.role.test;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A resource managed by the component instance used in tests.
 *
 * @author mmoquillon
 */
@Entity
@Table(name = "SC_MyComponent_Resources")
public class Resource extends BasicJpaEntity<Resource, UniqueLongIdentifier>
    implements Contribution {

  @Column(nullable = false)
  @NotNull
  private String name;
  @Column(nullable = false)
  @NotNull
  private String description = "";
  @Column(nullable = false)
  @NotNull
  private Instant creationDate;
  @Column(nullable = false)
  @NotNull
  private Instant updateDate;
  @Column(nullable = false)
  @NotNull
  private String creator;
  @Column(nullable = false)
  @NotNull
  private String updater;
  private String validator;
  private Instant validationDate;
  @Column(nullable = false)
  @NotNull
  private String instanceId;


  public static Optional<Resource> getById(String id) {
    return Transaction.performInOne(() -> {
      EntityManager em = EntityManagerProvider.get().getEntityManager();
      return Optional.ofNullable(em.find(Resource.class, UniqueLongIdentifier.from(id)));
    });
  }

  protected Resource() {
    super();
    // for JPA
  }

  public Resource(String instanceId, String name, String description) {
    this.instanceId = instanceId;
    this.name = name;
    this.description = description;
    this.creator = Optional.ofNullable(User.getCurrentRequester())
        .map(User::getId)
        .orElse(User.getSystemUser().getId());
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return ContributionIdentifier.from(instanceId, getId(), getClass().getSimpleName());
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public Date getCreationDate() {
    return Date.from(creationDate);
  }

  @Override
  public Date getLastUpdateDate() {
    return Date.from(updateDate);
  }

  @Override
  public User getCreator() {
    return User.getById(creator);
  }

  @Override
  public User getLastUpdater() {
    return User.getById(updater);
  }

  public User getValidator() {
    return User.getById(validator);
  }

  public void setValidator(User validator) {
    this.validator = validator.getId();
  }

  public Date getValidationDate() {
    return Date.from(validationDate);
  }

  public void save() {
    Transaction.performInOne(() -> {
      EntityManager em = EntityManagerProvider.get().getEntityManager();
      if (this.isPersisted()) {
        em.persist(this);
      } else {
        em.merge(this);
      }
      return null;
    });
  }

  public void validate() {
    this.validationDate = Instant.now();
    this.validator = Optional.ofNullable(User.getCurrentRequester())
        .map(User::getId)
        .orElse(User.getSystemUser().getId());
    save();
  }

  public List<User> getPossibleValidators() {
    if (isPersisted()) {
      return getResourceValidators()
          .stream()
          .map(Validator::getUser)
          .collect(Collectors.toList());
    } else {
      return List.of();
    }
  }

  public void addAsValidator(User user) {
    if (isPersisted()) {
      Validator.newValidator(user)
          .onResource(this)
          .save();
    }
  }

  public void removeFromValidators(User user) {
    getResourceValidators().stream()
        .filter(v -> v.getUserId().equals(user.getId()))
        .findFirst()
        .ifPresent(Validator::delete);
  }

  public boolean isValidated() {
    return validationDate != null && validator != null;
  }

  @Override
  protected void performBeforeUpdate() {
    super.performBeforeUpdate();
    this.updateDate = Instant.now();
    this.updater = Optional.ofNullable(User.getCurrentRequester())
        .map(User::getId)
        .orElse(User.getSystemUser().getId());
  }

  @Override
  protected void performBeforePersist() {
    super.performBeforeUpdate();
    this.creationDate = Instant.now();
    this.creator = Optional.ofNullable(User.getCurrentRequester())
        .map(User::getId)
        .orElse(User.getSystemUser().getId());
  }

  private List<Validator> getResourceValidators() {
    return Transaction.performInOne(() -> {
      EntityManager em = EntityManagerProvider.get().getEntityManager();
      return em.createNamedQuery("Validator.findAllByResource", Validator.class)
          .setParameter("resource", this)
          .getResultList();
    });
  }
}
  