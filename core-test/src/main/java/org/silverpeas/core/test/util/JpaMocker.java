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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.test.util;

import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaEntity;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import org.silverpeas.core.persistence.datasource.repository.jpa.AbstractJpaEntityRepository;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.Mutable;

import javax.persistence.EntityManager;
import java.lang.reflect.ParameterizedType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A mocker of the JPA environment.
 * @author mmoquillon
 */
public class JpaMocker {

  private final CommonAPI4Test commonAPI4Test;

  /**
   * Constructs a mocker for the JPA environment context.
   * @param commonAPI4Test the {@link CommonAPI4Test} rule used in the test.
   */
  public JpaMocker(final CommonAPI4Test commonAPI4Test) {
    this.commonAPI4Test = commonAPI4Test;
  }

  /**
   * Mocks the specified repository and uses the specified mutable container to store any entity
   * that is asked to be saved with the saving method of the mocked repository.
   * @param repoType the actual type of the repository to mock.
   * @param savedEntity the mutable container used to store any entity saved.
   * @param <T> the concrete type of the repository to mock.
   * @param <E> the concrete type of the entity that is stored into the repository.
   * @return the mock of the repository.
   */
  @SuppressWarnings("unchecked")
  public <T extends AbstractJpaEntityRepository<E>, E extends AbstractJpaEntity> T mockRepository(
      final Class<T> repoType, final Mutable<E> savedEntity) {
    T repository = mock(repoType);
    Class<E> entityType =
        (Class<E>) ((ParameterizedType) repoType.getGenericSuperclass()).getActualTypeArguments()[0];
    when(repository.save(any(entityType))).thenAnswer(invocation -> {
      E entity = invocation.getArgument(0);
      savedEntity.set(EntityIdSetter.setIdTo(entity, UuidIdentifier.class));
      return savedEntity.get();
    });
    commonAPI4Test.injectIntoMockedBeanContainer(repository);
    return repository;
  }

  /**
   * Mocks the JPA backend.
   * @param savedEntity the mutable container having possibly an entity that was saved. Used to
   * find such an entity when invoking the JPA entity finder.
   * @param entityType the actual class of the entity.
   * @param <T> the type of the entity
   */
  public <T extends Entity> void mockJPA(final Mutable<T> savedEntity, Class<T> entityType) {
    EntityManagerProvider entityManagerProvider = mock(EntityManagerProvider.class);
    EntityManager entityManager = mock(EntityManager.class);
    when(entityManagerProvider.getEntityManager()).thenReturn(entityManager);
    // below is used when checking an entity is persisted: an entity is returned only if
    // the identifier passed as argument matches the entity that was saved.
    when(entityManager.find(eq(entityType), any(UuidIdentifier.class))).thenAnswer(invocation -> {
      UuidIdentifier id = invocation.getArgument(1);
      Entity entity = null;
      if (savedEntity.isPresent()) {
        entity = savedEntity.get().getId().equals(id.asString()) ? savedEntity.get() : null;
      }
      return entity;
    });
    commonAPI4Test.injectIntoMockedBeanContainer(entityManagerProvider);
    commonAPI4Test.injectIntoMockedBeanContainer(new Transaction());
  }
}
  