/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.persistence.datasource.repository.jpa;

import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifierConverter;
import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.persistence.datasource.repository.QueryCriteria;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.PaginationList;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.silverpeas.core.util.annotation.ClassAnnotationUtil
    .searchClassThatDeclaresAnnotation;

/**
 * Abstract implementation of the {@link EntityRepository} interface that uses the JPA API.
 * This implementation defines all the common methods that can be required by the more concrete
 * repositories to implement easily their persistence related business operations.
 *
 * It provides additional signatures to handle friendly the JPA queries into extensions of
 * repository classes.
 * Take a look into this class to analyse how query parameters are performed ({@link
 * NamedParameters}).
 * @param <T> the class name of the identifiable entity which is handled by the repository.
 * @author mmoquillon
 */
public abstract class AbstractJpaEntityRepository<T extends IdentifiableEntity>
    implements EntityRepository<T> {

  private int maximumItemsInClause = 500;
  private EntityIdentifierConverter identifierConverter =
      new EntityIdentifierConverter(getEntityIdentifierClass());

  @Override
  public void flush() {
    getEntityManager().flush();
  }

  @Override
  public boolean contains(T entity) {
    return getEntityManager().contains(entity);
  }

  @Override
  public List<T> getAll() {
    return getEntityManager().createQuery(
        "select a from " + getEntityClass().getSimpleName() + " a", getEntityClass())
        .getResultList();
  }

  @Override
  public T getById(final String id) {
    return getByIdentifier(getIdentifierConverter().convertToEntityIdentifier(id));
  }

  @Override
  public List<T> getById(final String... ids) {
    return getByIdentifiers(getIdentifierConverter().convertToEntityIdentifiers(ids));
  }

  @Override
  public List<T> getById(final Collection<String> ids) {
    return getByIdentifiers(getIdentifierConverter().convertToEntityIdentifiers(ids));
  }

  @Override
  public void delete(final List<T> entities) {
    entities.stream().filter(T::isPersisted).forEach(e -> {
      T entity = getById(e.getId());
      getEntityManager().remove(entity);
    });
  }

  @Override
  public long deleteById(final Collection<String> ids) {
    return deleteByIdentifier(getIdentifierConverter().convertToEntityIdentifiers(ids));
  }

  @Override
  public List<T> findByCriteria(final QueryCriteria criteria) {
    String jpqlQuery = toJPQLQuery(criteria);
    NamedParameters parameters = criteria.clause().parameters();
    TypedQuery<T> query = getEntityManager().createQuery(jpqlQuery, getEntityClass());
    long count = -1;
    if (criteria.pagination().isDefined()) {
      String jpqlCountQuery = "select count(*) " + jpqlQuery.replaceFirst("order by.*", "");
      count = getFromJpqlString(jpqlCountQuery, parameters, Long.class);
      query.setFirstResult((criteria.pagination().getPageNumber() - 1) * criteria.pagination().
          getItemCount());
      query.setMaxResults(criteria.pagination().getItemCount());
    }
    List<T> listOfEntities = getAllFromQuery(query, parameters);
    return (count >= 1 ? PaginationList.from(listOfEntities, count) : listOfEntities);
  }

  /**
   * Finds the entities by the specified named query (a JPQL instruction) and with the specified
   * parameters.
   * @param namedQuery the named query. It is an identifier to a JPQL instruction.
   * @param parameters the parameters to apply on the query.
   * @return a list of entities that match the specified query.
   */
  protected List<T> findByNamedQuery(String namedQuery, final NamedParameters parameters) {
    return listFromNamedQuery(namedQuery, parameters);
  }

  /**
   * Finds the first entity matching the specified named query (a JPQL instruction)
   * and with the specified parameters.
   * @param namedQuery the named query. It is an identifier to a JPQL instruction.
   * @param parameters the parameters to apply on the query.
   * @return the first encountered entity that matches the specified query or null if no entities
   * match the specified query.
   */
  protected T findFirstByNamedQuery(String namedQuery, final NamedParameters parameters) {
    return first(listFromNamedQuery(namedQuery, parameters));
  }

  /**
   * Constructs a container of query parameters to use in JPQL queries.
   * @return a container of query parameters.
   */
  protected NamedParameters newNamedParameters() {
    return new NamedParameters();
  }

  /**
   * Gets an instance that represents the fact that it does not exist parameter for the query to
   * execute.
   * @return instance of {@link NoNamedParameter}
   */
  protected NoNamedParameter noParameter() {
    return new NoNamedParameter();
  }

  /**
   * Gets an entity from the specified query written in JPQL and with the specified parameters.
   * @param query the JPQL query.
   * @param parameters the parameters to apply to the query.
   * @return the required entity if exists, null otherwise
   * @throws IllegalArgumentException if it exists more than one entity from the query result.
   */
  protected T getFromJpqlString(String query, NamedParameters parameters) {
    return getFromJpqlString(query, parameters, getEntityClass());
  }

  /**
   * Gets an entity of a given type from the specified JPQL query and with the specified parameters.
   * This method is for fetching any information about the entities stored into this repository; it
   * can be an entity itself or a count of entities satisfying some properties, and so on.
   * @param <U> the type of the returned entities.
   * @param query the JPQL query.
   * @param parameters the parameters to apply to the query.
   * @param returnEntityType the class of the returned entities.
   * @return the required entity if exists, null otherwise
   * @throws IllegalArgumentException if it exists more than one entity from the query result.
   */
  protected <U> U getFromJpqlString(String query, NamedParameters parameters,
      Class<U> returnEntityType) {
    return getFromQuery(getEntityManager().createQuery(query, returnEntityType), parameters);
  }

  /**
   * Lists entities from the specified JPQL query and with the specified parameters.
   * @param query the JPQL query.
   * @param parameters the parameters to apply to the query.
   * @return a list of entities matching the query and the parameters. If no entities match the
   * query then an empty list is returned.
   */
  protected List<T> listFromJpqlString(String query, NamedParameters parameters) {
    return listFromJpqlString(query, parameters, getEntityClass());
  }

  /**
   * Lists entities from a the specified JPQL query and with the specified parameters.
   * This method is for fetching any information about the entities stored into this repository; it
   * can be the entities themselves or some of their properties or relationships, and so on.
   * @param <U> the type of the returned entities.
   * @param query the JPQL query.
   * @param parameters the parameters to apply to the query.
   * @param returnEntityType the class of the returned entities.
   * @return a list of entities matching the query and the parameters. If no entities match the
   * query then an empty list is returned.
   */
  protected <U> List<U> listFromJpqlString(String query, NamedParameters parameters,
      Class<U> returnEntityType) {
    return getAllFromQuery(getEntityManager().createQuery(query, returnEntityType), parameters);
  }

  /**
   * Deletes entities from a JPQL query and with the specified parameters.
   * @param query the JPQL query.
   * @param parameters the parameters to apply to the query.
   * @return the number of deleted entities.
   */
  protected long deleteFromJpqlQuery(String query, NamedParameters parameters) {
    return deleteFromQuery(getEntityManager().createQuery(query), parameters);
  }

  /**
   * Gets an entity from a named query and with the specified parameters.
   * @param namedQuery the name of the query.
   * @param parameters the parameters to apply to the query.
   * @return the required entity if exists, null otherwise
   * @throws IllegalArgumentException if it exists more than one entity from the query result.
   */
  protected T getFromNamedQuery(String namedQuery, NamedParameters parameters) {
    return getFromNamedQuery(namedQuery, parameters, getEntityClass());
  }

  /**
   * Gets an entity from a named query and with the specified parameters.
   * This method is for fetching any information about the entities stored into this repository; it
   * can be an entity itself or a count of entities satisfying some properties, and so on.
   * @param <U> the type of the returned entities.
   * @param namedQuery the name of the query.
   * @param parameters the parameters to apply to the query.
   * @param returnEntityType the class of the returned entities.
   * @return the required entity if exists, null otherwise
   * @throws IllegalArgumentException if it exists more than one entity from the query result.
   */
  protected <U> U getFromNamedQuery(String namedQuery, NamedParameters parameters,
      Class<U> returnEntityType) {
    return getFromQuery(getEntityManager().createNamedQuery(namedQuery, returnEntityType),
        parameters);
  }

  /**
   * Lists entities from a named query and with the specified parameters.
   * @param namedQuery the n ame of the query.
   * @param parameters the parameters to apply to the query.
   * @return the list of entities matching the query and the parameters.
   */
  protected List<T> listFromNamedQuery(String namedQuery, NamedParameters parameters) {
    return listFromNamedQuery(namedQuery, parameters, getEntityClass());
  }

  /**
   * Lists entities from a named query and with the specified parameters.
   * @param <U> the type of the returned entities.
   * @param namedQuery the name of the query.
   * @param parameters the parameters to apply to the query.
   * @param returnEntityType the class of the returned entities.
   * @return a list of entities of the given type or an empty list if no entities match the
   * specified named query with the given parameters.
   */
  protected <U> List<U> listFromNamedQuery(String namedQuery, NamedParameters parameters,
      Class<U> returnEntityType) {
    return getAllFromQuery(getEntityManager().createNamedQuery(namedQuery, returnEntityType),
        parameters);
  }

  /**
   * Deletes the entities from a named query and with the specified parameters.
   * @param namedQuery the name of the query.
   * @param parameters the parameters to apply to the query.
   * @return the count of entities deleted by the named query.
   */
  protected long deleteFromNamedQuery(String namedQuery, NamedParameters parameters) {
    return deleteFromQuery(getEntityManager().createNamedQuery(namedQuery), parameters);
  }

  /**
   * Maximum number of items to be passed into a SQL "in" clause.
   * Indeed, according to the database, the treatment of the "in" clause may be different and pass
   * too much value "in" this clause may result in an error ...
   * The behaviour is the following : when a query includes a "in" clause potentially filled with a
   * lot of values, the protected split method can be used to divide a collection of values into
   * several collections of values, each one with its size limited to this parameter.
   * Please take a look at {@link org.silverpeas.core.persistence.datasource.repository
   * .EntityRepository#getByIdentifier} or {@link org.silverpeas.core.persistence.datasource
   * .repository
   * .EntityRepository#deleteByIdentifier} methods for examples of use.
   * Returns of experiences drives us to set the default value at 500.
   * @return the maximum number of items in a clause.
   */
  protected int getMaximumItemsInClause() {
    return maximumItemsInClause;
  }

  /**
   * Sets the maximum items in a in clause.
   * @param maximumItemsInClause
   */
  protected void setMaximumItemsInClause(final int maximumItemsInClause) {
    this.maximumItemsInClause = maximumItemsInClause;
  }

  /**
   * Gets the entity manager with which the persistence of the entities stored in this repository
   * is performed.
   * @return the JPA entity manager.
   */
  protected EntityManager getEntityManager() {
    return EntityManagerProvider.get().getEntityManager();
  }

  protected EntityIdentifierConverter getIdentifierConverter() {
    return identifierConverter;
  }

  protected Class<T> getEntityClass() {
    return ((Class<T>) ((ParameterizedType) this.getClass()
        .getGenericSuperclass()).getActualTypeArguments()[0]);
  }

  private T getByIdentifier(final EntityIdentifier id) {
    return getEntityManager().find(getEntityClass(), id);
  }

  private <U extends EntityIdentifier> List<T> getByIdentifiers(final Collection<U> ids) {
    List<T> entities = new ArrayList<T>(ids.size());
    String selectQuery = "select a from " + getEntityClass().getName() + " a where a.id in :ids";
    for (Collection<U> entityIds : split(new HashSet<U>(ids))) {
      List<T> tmp = newNamedParameters().add("ids", entityIds)
          .applyTo(getEntityManager().createQuery(selectQuery, getEntityClass()))
          .getResultList();
      if (entities.isEmpty()) {
        entities = tmp;
      } else {
        entities.addAll(tmp);
      }
    }
    return entities;
  }

  private String toJPQLQuery(QueryCriteria criteria) {
    String query = criteria.clause().text();
    String queryInLowerCase = query.toLowerCase();
    if (queryInLowerCase.startsWith("select")) {
      query = query.substring(queryInLowerCase.indexOf("from"));
    }
    if (!queryInLowerCase.startsWith("from ")) {
      query = "from " + getEntityClass().getSimpleName() + " where " + query;
    }
    return query;
  }

  private <U> List<U> getAllFromQuery(TypedQuery<U> query, NamedParameters parameters) {
    return parameters.applyTo(query).getResultList();
  }

  private <U> U getFromQuery(TypedQuery<U> query,
      NamedParameters parameters) {
    try {
      return parameters.applyTo(query).getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (NonUniqueResultException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private <U extends EntityIdentifier> long deleteByIdentifier(final Collection<U> ids) {
    long nbDeletes = 0;
    Query deleteQuery = getEntityManager().createQuery(
        "delete from " + getEntityClass().getName() + " a where a.id in :ids");
    for (Collection<U> entityIds : split(ids)) {
      nbDeletes += newNamedParameters().add("ids", entityIds).applyTo(deleteQuery).
          executeUpdate();
    }
    return nbDeletes;
  }

  private long deleteFromQuery(Query deleteQuery, NamedParameters parameters) {
    return parameters.applyTo(deleteQuery).executeUpdate();
  }

  private <E> Collection<Collection<E>> split(Collection<E> collection) {
    return CollectionUtil.split(collection, getMaximumItemsInClause());
  }


  private <U> U first(List<U> entities) {
    if (entities.isEmpty()) {
      return null;
    }
    return entities.get(0);
  }

  private <U extends EntityIdentifier> Class<U> getEntityIdentifierClass() {
    return ((Class<U>) ((ParameterizedType) getEntityClass().getGenericSuperclass())
        .getActualTypeArguments()[1]);
  }
}
