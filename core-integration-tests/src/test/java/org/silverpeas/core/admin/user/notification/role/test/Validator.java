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
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.CompositeEntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;

import javax.persistence.*;

/**
 * A possible validator of a resource managed by the component instance used in tests.
 *
 * @author mmoquillon
 */
@Entity
@Table(name = "SC_MyComponent_Validators")
@NamedQuery(name = "Validator.findAllByResource",
    query = "select v from Validator v where v.resource = :resource order by v.id.validatorId")
@NamedQuery(name = "Validator.findAllByInstanceId",
    query = "select v from Validator v where v.resource.instanceId = :instanceId " +
        "order by v.id.resourceId, v.id.validatorId")
public class Validator extends BasicJpaEntity<Validator, ValidatorID> {

  @ManyToOne(optional = false)
  @JoinColumn(name = "resourceId", updatable = false, insertable = false,
      referencedColumnName = "id")
  private Resource resource;

  protected Validator() {
    // for JPA
  }

  protected Validator(User validator, Resource resource) {
    this.resource = resource;
    this.setId(validator.getId() + CompositeEntityIdentifier.COMPOSITE_SEPARATOR + resource.getId());
  }

  public static ValidatorBuilder newValidator(User user) {
    return new ValidatorBuilder(user);
  }

  public String getUserId() {
    return getNativeId().getValidatorId();
  }

  public Resource getResource() {
    return resource;
  }

  public User getUser() {
    return User.getById(getNativeId().getValidatorId());
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

  public void delete() {
    if (isPersisted()) {
      Transaction.performInOne(() -> {
        EntityManager em = EntityManagerProvider.get().getEntityManager();
        em.remove(this);
        return null;
      });
    }
  }

  public static class ValidatorBuilder {

    private final User validator;

    private ValidatorBuilder(User validator) {
      this.validator = validator;
    }

    public Validator onResource(Resource resource) {
      return new Validator(validator, resource);
    }
  }
}
  