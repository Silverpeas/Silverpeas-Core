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
package org.silverpeas.core.persistence.datasource.repository.jpa;

import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.datasource.repository.OperationContext;
import org.silverpeas.core.persistence.datasource.repository.QueryCriteria;
import org.silverpeas.core.persistence.datasource.repository.SilverpeasEntityRepository;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.StringUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * A Silverpeas dedicated entity manager that wraps the JPA {@link javax.persistence.EntityManager}
 * and that provides convenient methods to perform the CRUD operations on entities.
 * <p>
 * All repositories that use only JPA for managing the persistence of their entities should extends
 * this JPA manager. If the different parts of an entity are persisted into several data source
 * beside a SQL-based one, then this repository should be used within a delegation of JPA related
 * operations.
 * <p>
 * It provides additional signatures to handle friendly the JPA queries into extensions of
 * repository classes.
 * <p>
 * Take a look into this class to analyse how query parameters are performed ({@link
 * NamedParameters}).
 * <p>
 * @param <ENTITY> specify the class name of the entity which is handled by the repository
 * manager.
 * @param <ENTITY_IDENTIFIER_TYPE> the identifier class name used by {@link ENTITY} for its
 * primary key definition.
 * @author Yohann Chastagnier
 */
public class SilverpeasJpaEntityManager<ENTITY extends Entity<ENTITY, ENTITY_IDENTIFIER_TYPE>,
    ENTITY_IDENTIFIER_TYPE extends EntityIdentifier>
    implements SilverpeasEntityRepository<ENTITY, ENTITY_IDENTIFIER_TYPE> {

  /**
   * Maximum number of items to be passed into a SQL "in" clause.
   * Indeed, according to the database, the treatment of the "in" clause may be different and pass
   * too much value "in" this clause may result in an error ...
   * The behaviour is the following : when a query includes a "in" clause potentially filled with a
   * lot of values, the protected split method can be used to divide a collection of values into
   * several collections of values, each one with its size limited to this parameter.
   * Please take a look at {@link org.silverpeas.core.persistence.datasource.repository
   * .EntityRepository#getByIdentifier} or {@link org.silverpeas.core.persistence.datasource.repository
   * .EntityRepository#deleteByIdentifier} methods for examples of use.
   * Returns of experiences drives us to set the default value at 500.
   */
  private int maximumItemsInClause = 500;

  private Class<ENTITY> entityClass;

  private Class<ENTITY_IDENTIFIER_TYPE> entityIdentifierClass;

  @PersistenceContext
  private EntityManager em;

  /**
   * Gets the entity class managed by the repository.
   * @return
   */
  protected Class<ENTITY> getEntityClass() {
    initializeEntityClasses();
    return entityClass;
  }

  /**
   * Gets the identifier class of the entity managed by the repository.
   * @return
   */
  protected Class<ENTITY_IDENTIFIER_TYPE> getEntityIdentifierClass() {
    initializeEntityClasses();
    return entityIdentifierClass;
  }

  /**
   * Gets the identifier class of the entity managed by the repository.
   * @return
   */
  @SuppressWarnings("unchecked")
  private void initializeEntityClasses() {
    if (entityIdentifierClass == null) {
      try {
        entityClass = ((Class<ENTITY>) ((ParameterizedType) this.getClass().
            getGenericSuperclass()).getActualTypeArguments()[0]);
        entityIdentifierClass = ((Class<ENTITY_IDENTIFIER_TYPE>) ((ParameterizedType) this.
            getClass().
            getGenericSuperclass()).getActualTypeArguments()[1]);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Converts the given String id into the right entity identifier.
   * @param idAsString
   * @return
   */
  protected ENTITY_IDENTIFIER_TYPE convertToEntityIdentifier(String idAsString) {
    try {
      ENTITY_IDENTIFIER_TYPE identifier = getEntityIdentifierClass().newInstance();
      identifier.fromString(idAsString);
      return identifier;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts the given String ids into the right entity identifiers.
   * @param idsAsString
   * @return
   */
  protected Collection<ENTITY_IDENTIFIER_TYPE> convertToEntityIdentifiers(String... idsAsString) {
    return convertToEntityIdentifiers(
        idsAsString == null ? null : CollectionUtil.asList(idsAsString));
  }

  /**
   * Converts the given String ids into the right entity identifiers.
   * @param idsAsString
   * @return
   */
  protected Collection<ENTITY_IDENTIFIER_TYPE> convertToEntityIdentifiers(
      Collection<String> idsAsString) {
    int size = (idsAsString == null) ? 0 : idsAsString.size();
    Collection<ENTITY_IDENTIFIER_TYPE> identifiers = new ArrayList<ENTITY_IDENTIFIER_TYPE>(size);
    if (size > 0) {
      for (String id : idsAsString) {
        identifiers.add(convertToEntityIdentifier(id));
      }
    }
    return identifiers;
  }

  /**
   * Synchronizes the persistence context to the underlying data source. Within a transactional
   * context, the persistence context is directly put to the data source but will be effective
   * only when the transaction will be committed. The consequence of the synchronization within
   * a transaction context is the persistence context is then validated by the data source. Making
   * it work, the data source has to support the transactions.
   * <p>
   * Warning, the behavior of this method is implementation-dependent. According to the type of
   * the repository or of the underlying data source, the flush can not to be working.
   */
  public void flush() {
    getEntityManager().flush();
  }

  /**
   * Does this repository contains the specified entity? It contains the entity if its persistence
   * context is taken in charge by the instances of the repository class.
   * @param entity an entity.
   * @return true if the specified entity exists in the persistence context backed by this
   * repository, false otherwise.
   */
  @Override
  public boolean contains(ENTITY entity) {
    return getEntityManager().contains(entity);
  }

  /**
   * Finds the entities by the specified named query (a JPQL instruction) and with the specified
   * parameters.
   * @param namedQuery the named query. It is an identifier to a JPQL instruction.
   * @param parameters the parameters to apply on the query.
   * @return a list of entities that match the specified query.
   */
  public List<ENTITY> findByNamedQuery(String namedQuery, final NamedParameters parameters) {
    return listFromNamedQuery(namedQuery, parameters);
  }

  /**
   * Finds the first or the single entity matching the specified named query (a JPQL instruction)
   * and with the specified parameters.
   * @param namedQuery the named query. It is an identifier to a JPQL instruction.
   * @param parameters the parameters to apply on the query.
   * @return the first encountered entity or the single one that matches the specified query.
   */
  public ENTITY findOneByNamedQuery(String namedQuery, final NamedParameters parameters) {
    return unique(listFromNamedQuery(namedQuery, parameters));
  }

  @Override
  public List<ENTITY> getAll() {
    return getEntityManager()
        .createQuery("select a from " + getEntityClass().getSimpleName() + " a", getEntityClass())
        .getResultList();
  }

  @Override
  public ENTITY getById(final String id) {
    return getByIdentifier(convertToEntityIdentifier(id));
  }

  @Override
  public List<ENTITY> getById(final String... ids) {
    return getByIdentifier(convertToEntityIdentifiers(ids));
  }

  @Override
  public List<ENTITY> getById(final Collection<String> ids) {
    return getByIdentifier(convertToEntityIdentifiers(ids));
  }

  private ENTITY getByIdentifier(final ENTITY_IDENTIFIER_TYPE id) {
    return getEntityManager().find(getEntityClass(), id);
  }

  private List<ENTITY> getByIdentifier(final Collection<ENTITY_IDENTIFIER_TYPE> ids) {
    List<ENTITY> entities = new ArrayList<ENTITY>(ids.size());
    String selectQuery = "select a from " + getEntityClass().getName() + " a where a.id in :ids";
    for (Collection<ENTITY_IDENTIFIER_TYPE> entityIds : split(
        new HashSet<ENTITY_IDENTIFIER_TYPE>(ids))) {
      List<ENTITY> tmp = newNamedParameters().add("ids", entityIds)
          .applyTo(getEntityManager().createQuery(selectQuery, getEntityClass())).getResultList();
      if (entities.isEmpty()) {
        entities = tmp;
      } else {
        entities.addAll(tmp);
      }
    }
    return entities;
  }

  @Override
  public ENTITY save(final OperationContext context, final ENTITY entity) {
    return save(context, Collections.singletonList(entity)).get(0);
  }

  @Override
  public List<ENTITY> save(final OperationContext context, final ENTITY... entities) {
    return save(context, CollectionUtil.asList(entities));
  }

  @Override
  public List<ENTITY> save(final OperationContext context, final List<ENTITY> entities) {
    context.putIntoCache();
    List<ENTITY> savedEntities = new ArrayList<ENTITY>(entities.size());
    for (ENTITY entity : entities) {
      if (entity.isPersisted()) {
        savedEntities.add(getEntityManager().merge(entity));
      } else {
        getEntityManager().persist(entity);
        savedEntities.add(entity);
      }
    }
    return savedEntities;
  }

  @Override
  public void delete(final ENTITY... entity) {
    delete(CollectionUtil.asList(entity));
  }

  @Override
  public void delete(final List<ENTITY> entities) {
    for (ENTITY entity : entities) {
      if (entity.isPersisted()) {
        getEntityManager().remove(getEntityManager().merge(entity));
      }
    }
  }

  @Override
  public long deleteById(final String... ids) {
    return deleteByIdentifier(convertToEntityIdentifiers(ids));
  }

  @Override
  public long deleteById(final Collection<String> ids) {
    return deleteByIdentifier(convertToEntityIdentifiers(ids));
  }

  /**
   * Deletes all entities belonging to the specified component instance.
   * @param componentInstanceId the unique instance identifier.
   * @return the number of deleted entities.
   */
  @Override
  public long deleteByComponentInstanceId(final String componentInstanceId) {
    Query deleteQuery = getEntityManager().createQuery(
        "delete from " + getEntityClass().getName() + " a where a.componentInstanceId = :id");
    return newNamedParameters().add("id", componentInstanceId).applyTo(deleteQuery).executeUpdate();
  }

  private long deleteByIdentifier(final Collection<ENTITY_IDENTIFIER_TYPE> ids) {
    long nbDeletes = 0;
    Query deleteQuery = getEntityManager()
        .createQuery("delete from " + getEntityClass().getName() + " a where a.id in :ids");
    for (Collection<ENTITY_IDENTIFIER_TYPE> entityIds : split(ids)) {
      nbDeletes += newNamedParameters().add("ids", entityIds).applyTo(deleteQuery).
          executeUpdate();
    }
    return nbDeletes;
  }

  /**
   * Gets a new query parameter container.
   * @return
   */
  public NamedParameters newNamedParameters() {
    return new NamedParameters();
  }

  /**
   * Gets an entity from a jpql query string.
   * @param jpqlQuery the JPQL query in string.
   * @param parameters the parameters to apply to the query.
   * @return the required entity if exists, null otherwise
   * @throws IllegalArgumentException if it exists more than one entity from the query result.
   */
  protected ENTITY getFromJpqlString(String jpqlQuery, NamedParameters parameters) {
    return getFromJpqlString(jpqlQuery, parameters, getEntityClass());
  }

  /**
   * Gets an entity from a jpql query string.
   * If the result
   * @param <AN_ENTITY> the type of the returned entities.
   * @param jpqlQuery the JPQL query in string.
   * @param parameters the parameters to apply to the query.
   * @param returnEntityType the class of the returned entities.
   * @return the required entity if exists, null otherwise
   * @throws IllegalArgumentException if it exists more than one entity from the query result.
   */
  protected <AN_ENTITY> AN_ENTITY getFromJpqlString(String jpqlQuery, NamedParameters parameters,
      Class<AN_ENTITY> returnEntityType) {
    return unique(
        listFromQuery(getEntityManager().createQuery(jpqlQuery, returnEntityType), parameters));
  }

  /**
   * Lists entities from a jpql query string.
   * @param jpqlQuery the JPQL query in string.
   * @param parameters the parameters to apply to the query.
   * @return a list of entities matching the query and the parameters
   */
  protected List<ENTITY> listFromJpqlString(String jpqlQuery, NamedParameters parameters) {
    return listFromJpqlString(jpqlQuery, parameters, getEntityClass());
  }

  /**
   * Lists entities from a JPQL query string.
   * @param <AN_ENTITY> the type of the returned entities.
   * @param jpqlQuery the JPQL query in string.
   * @param parameters the parameters to apply to the query.
   * @param returnEntityType the class of the returned entities.
   * @return a list of entities matching the query and the parameters
   */
  protected <AN_ENTITY> List<AN_ENTITY> listFromJpqlString(String jpqlQuery,
      NamedParameters parameters, Class<AN_ENTITY> returnEntityType) {
    return listFromQuery(getEntityManager().createQuery(jpqlQuery, returnEntityType), parameters);
  }

  /**
   * Lists entities from the specified criteria.
   * @param criteria the criteria constraining the query and for which the entities to list have to
   * satisfy.
   * @return a list of entities matching specified criteria. If a pagination criterion is defined
   * in the criteria, then the returned list is a {@link PaginationList}
   * instance.
   */
  public List<ENTITY> findByCriteria(final QueryCriteria criteria) {
    String jpqlQuery = jpqlQueryFrom(criteria);
    NamedParameters parameters = criteria.clause().parameters();
    TypedQuery<ENTITY> query = getEntityManager().createQuery(jpqlQuery, getEntityClass());
    long count = -1;
    if (criteria.pagination().isDefined()) {
      String jpqlCountQuery = "select count(*) " + jpqlQuery.replaceFirst("order by.*", "");
      count = getFromJpqlString(jpqlCountQuery, parameters, Long.class);
      query.setFirstResult((criteria.pagination().getPageNumber() - 1) * criteria.pagination().
          getItemCount());
      query.setMaxResults(criteria.pagination().getItemCount());
    }
    List<ENTITY> listOfEntities = listFromQuery(query, parameters);
    return (count >= 1 ? PaginationList.from(listOfEntities, count) : listOfEntities);
  }

  /**
   * Gets from an entity list result the unique entity expected.
   * @param entities entity list result from a query.
   * @param <AN_ENTITY> the type of the returned entities.
   * @return the unique entity result.
   * @throws IllegalArgumentException if it exists more than one entity in the specified entity
   * result list.
   */
  private <AN_ENTITY> AN_ENTITY unique(List<AN_ENTITY> entities) {
    if (entities.isEmpty()) {
      return null;
    }
    if (entities.size() == 1) {
      return entities.get(0);
    }
    throw new IllegalArgumentException(
        "wanted to get a unique entity from a list that contains more than one entity ...");
  }

  /**
   * Updates entities from a jpql query.
   * Please using this method as little as possible. If it is used, please handle the technical
   * following data :
   * - last updated by (id of the user in the most of cases)
   * - last update date (new Timestamp((new Date()).getTime()))
   * - version (entity.version = (entity.version + 1))
   * Not to handle the above technical data could bring to a unsustainable entity state ...
   * @param jpqlQuery the query string
   * @param parameters the parameters to apply to the query.
   * @return the number of updated entities.
   */
  protected long updateFromJpqlQuery(String jpqlQuery, NamedParameters parameters) {
    return updateFromQuery(getEntityManager().createQuery(jpqlQuery), parameters);
  }

  /**
   * Deletes entities from a jpql query.
   * @param jpqlQuery the query string.
   * @param parameters the parameters to apply to the query.
   * @return the number of deleted entities.
   */
  protected long deleteFromJpqlQuery(String jpqlQuery, NamedParameters parameters) {
    return deleteFromQuery(getEntityManager().createQuery(jpqlQuery), parameters);
  }

  /**
   * Gets an entity from a named query.
   * @param namedQuery the name of the query.
   * @param parameters the parameters to apply to the query.
   * @return the required entity if exists, null otherwise
   * @throws IllegalArgumentException if it exists more than one entity from the query result.
   */
  protected ENTITY getFromNamedQuery(String namedQuery, NamedParameters parameters) {
    return getFromNamedQuery(namedQuery, parameters, getEntityClass());
  }

  /**
   * Gets an entity from a named query.
   * @param <AN_ENTITY> the type of the returned entities.
   * @param namedQuery the name of the query.
   * @param parameters the parameters to apply to the query.
   * @param returnEntityType the class of the returned entities.
   * @return the required entity if exists, null otherwise
   * @throws IllegalArgumentException if it exists more than one entity from the query result.
   */
  protected <AN_ENTITY> AN_ENTITY getFromNamedQuery(String namedQuery, NamedParameters parameters,
      Class<AN_ENTITY> returnEntityType) {
    return unique(listFromQuery(getEntityManager().createNamedQuery(namedQuery, returnEntityType),
        parameters));
  }

  /**
   * Lists entities from a named query.
   * @param namedQuery the n ame of the query.
   * @param parameters the parameters to apply to the query.
   * @return the list of entities matching the query and the parameters.
   */
  protected List<ENTITY> listFromNamedQuery(String namedQuery, NamedParameters parameters) {
    return listFromNamedQuery(namedQuery, parameters, getEntityClass());
  }

  /**
   * Lists entities from a named query.
   * @param <AN_ENTITY>
   * @param namedQuery
   * @param parameters
   * @param returnEntityType
   * @return
   */
  protected <AN_ENTITY> List<AN_ENTITY> listFromNamedQuery(String namedQuery,
      NamedParameters parameters, Class<AN_ENTITY> returnEntityType) {
    return listFromQuery(getEntityManager().createNamedQuery(namedQuery, returnEntityType),
        parameters);
  }

  /**
   * Updates entities from a named query.
   * Please using this method as little as possible. If it is used, please handle the technical
   * following data :
   * - last updated by (id of the user in the most of cases)
   * - last update date (new Timestamp((new Date()).getTime()))
   * - version (entity.version = (entity.version + 1))
   * Not to handle the above technical data could bring to a unsustainable entity state ...
   * @param namedQuery
   * @param parameters
   * @return
   */
  protected long updateFromNamedQuery(String namedQuery, NamedParameters parameters) {
    return updateFromQuery(getEntityManager().createNamedQuery(namedQuery), parameters);
  }

  /**
   * Deletes entities from a named query.
   * @param namedQuery
   * @param parameters
   * @return
   */
  protected long deleteFromNamedQuery(String namedQuery, NamedParameters parameters) {
    return deleteFromQuery(getEntityManager().createNamedQuery(namedQuery), parameters);
  }


  /**
   * Lists entities from a jpql query.
   * @param query
   * @param parameters
   * @return
   */
  private <AN_ENTITY> List<AN_ENTITY> listFromQuery(TypedQuery<AN_ENTITY> query,
      NamedParameters parameters) {
    return parameters.applyTo(query).getResultList();
  }

  /**
   * Centralization.
   * @param updateQuery
   * @param parameters
   * @return
   */
  private long updateFromQuery(Query updateQuery, NamedParameters parameters) {
    parameters.add("lastUpdateDate", new Timestamp((new Date()).getTime()));
    verify(updateQuery, parameters);
    return parameters.applyTo(updateQuery).executeUpdate();
  }

  /**
   * Verifies the technical update data are not missing in query and parameters.
   * @param query
   * @param parameters
   */
  private void verify(Query query, NamedParameters parameters) {
    for (String requiredParameterName : new String[]{"lastUpdatedBy", "lastUpdateDate", "version"}) {
      if (query.getParameter(requiredParameterName) != null) {
        NamedParameter parameter = parameters.namedParameters.get(requiredParameterName);
        if (parameter != null && StringUtil.isDefined(parameter.getValue().toString())) {
          continue;
        }
      }
      throw new IllegalArgumentException(
          "parameter '" + requiredParameterName + "' is missing from the query '" +
              "' or is missing from given parameters.");
    }
  }

  /**
   * Centralization.
   * @param deleteQuery
   * @param parameters
   * @return
   */
  private long deleteFromQuery(Query deleteQuery, NamedParameters parameters) {
    return parameters.applyTo(deleteQuery).executeUpdate();
  }

  /**
   * A centralized access to the entity manager (just in case ...)
   * @return
   */
  private EntityManager getEntityManager() {
    return em;
  }

  /**
   * Collection spliter, useful for in clauses.
   * @param collection
   * @param <E>
   * @return
   */
  protected <E> Collection<Collection<E>> split(Collection<E> collection) {
    return CollectionUtil.split(collection, getMaximumItemsInClause());
  }

  /**
   * Gets the maximum items in a in clause.
   * @return
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

  private String jpqlQueryFrom(QueryCriteria criteria) {
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
}
