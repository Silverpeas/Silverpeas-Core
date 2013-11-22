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
package org.silverpeas.persistence.repository.jpa;

import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.StringUtil;
import org.hibernate.ejb.QueryImpl;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.persistence.model.Entity;
import org.silverpeas.persistence.model.EntityIdentifier;
import org.silverpeas.persistence.repository.EntityRepository;
import org.silverpeas.persistence.repository.OperationContext;

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
import java.util.regex.Pattern;

/**
 * Abstract implementation of {@link EntityRepository} interface.
 * All interface signatures are implemented at this level.
 * The aim of this abstraction is also hiding the use of the entity manager.
 * <p/>
 * It provides additional signatures to handle friendly the JPA queries into extensions of
 * repository classes.
 * <p/>
 * Take a look into this class to analyse how query parameters are performed ({@link
 * NamedParameters}).
 * <p/>
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
public abstract class AbstractJpaEntityRepository<ENTITY extends Entity<ENTITY,
    ENTITY_IDENTIFIER_TYPE>, ENTITY_IDENTIFIER_TYPE extends EntityIdentifier>
    implements EntityRepository<ENTITY, ENTITY_IDENTIFIER_TYPE> {

  /**
   * Maximum number of items to be passed into a SQL "in" clause.
   * Indeed, according to the database, the treatment of the "in" clause may be different and pass
   * too much value "in" this clause may result in an error ...
   * The behaviour is the following : when a query includes a "in" clause potentially filled with a
   * lot of values, the protected split method can be used to divide a collection of values into
   * several collections of values, each one with its size limited to this parameter.
   * Please take a look at {@link org.silverpeas.persistence.repository
   * .EntityRepository#getByIdentifier} or {@link org.silverpeas.persistence.repository
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
        entityIdentifierClass =
            ((Class<ENTITY_IDENTIFIER_TYPE>) ((ParameterizedType) this.getClass().
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

  @Override
  public ENTITY getByIdentifier(final ENTITY_IDENTIFIER_TYPE id) {
    return getEntityManager().find(getEntityClass(), id);
  }

  @Override
  public List<ENTITY> getByIdentifier(final ENTITY_IDENTIFIER_TYPE... ids) {
    return getByIdentifier(CollectionUtil.asList(ids));
  }

  @Override
  public List<ENTITY> getByIdentifier(final Collection<ENTITY_IDENTIFIER_TYPE> ids) {
    List<ENTITY> entities = new ArrayList<ENTITY>(ids.size());
    String selectQuery = "select a from " + getEntityClass().getName() + " a where a.id in :ids";
    for (Collection<ENTITY_IDENTIFIER_TYPE> entityIds : split(
        new HashSet<ENTITY_IDENTIFIER_TYPE>(ids))) {
      List<ENTITY> tmp = initializeNamedParameters().add("ids", entityIds)
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

  @Override
  public long deleteByIdentifier(final ENTITY_IDENTIFIER_TYPE... ids) {
    return deleteByIdentifier(CollectionUtil.asList(ids));
  }

  @Override
  public long deleteByIdentifier(final Collection<ENTITY_IDENTIFIER_TYPE> ids) {
    long nbDeletes = 0;
    Query deleteQuery = getEntityManager()
        .createQuery("delete from " + getEntityClass().getName() + " a where a.id in :ids");
    for (Collection<ENTITY_IDENTIFIER_TYPE> entityIds : split(ids)) {
      nbDeletes +=
          initializeNamedParameters().add("ids", entityIds).applyTo(deleteQuery).executeUpdate();
    }
    return nbDeletes;
  }

  /**
   * Gets a query parameter container.
   * @return
   */
  protected NamedParameters initializeNamedParameters() {
    return new NamedParameters();
  }

  /**
   * Lists entities from a jpql query.
   * @param jpqlQuery
   * @param parameters
   * @return
   */
  protected List<ENTITY> listFromJpqlString(String jpqlQuery, NamedParameters parameters) {
    return listFromJpqlString(jpqlQuery, parameters, getEntityClass());
  }

  /**
   * Lists entities from a jpql query.
   * @param jpqlQuery
   * @param parameters
   * @return
   */
  protected <AN_ENTITY> List<AN_ENTITY> listFromJpqlString(String jpqlQuery,
      NamedParameters parameters, Class<AN_ENTITY> returnEntityType) {
    return listFromQuery(getEntityManager().createQuery(jpqlQuery, returnEntityType), parameters);
  }

  /**
   * Updates entities from a jpql query.
   * Please using this method as little as possible. If it is used, please handle the technical
   * following data :
   * - last updated by (id of the user in the most of cases)
   * - last update date (new Timestamp((new Date()).getTime()))
   * - version (entity.version = (entity.version + 1))
   * Not to handle the above technical data could bring to a unsustainable entity state ...
   * @param jpqlQuery
   * @param parameters
   * @return
   */
  protected long updateFromJpqlQuery(String jpqlQuery, NamedParameters parameters) {
    return updateFromQuery(getEntityManager().createQuery(jpqlQuery), parameters);
  }

  /**
   * Deletes entities from a jpql query.
   * @param jpqlQuery
   * @param parameters
   * @return
   */
  protected long deleteFromJpqlQuery(String jpqlQuery, NamedParameters parameters) {
    return deleteFromQuery(getEntityManager().createQuery(jpqlQuery), parameters);
  }

  /**
   * Lists entities from a named query.
   * @param namedQuery
   * @param parameters
   * @return
   */
  protected List<ENTITY> listFromNamedQuery(String namedQuery, NamedParameters parameters) {
    return listFromNamedQuery(namedQuery, parameters, getEntityClass());
  }

  /**
   * Lists entities from a named query.
   * @param namedQuery
   * @param parameters
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
    String queryString = ((QueryImpl) query).getHibernateQuery().getQueryString();
    for (String requiredParameterName : new String[]{"lastUpdatedBy", "lastUpdateDate"}) {
      if (query.getParameter(requiredParameterName) != null) {
        NamedParameter parameter = parameters.namedParameters.get(requiredParameterName);
        if (parameter != null && StringUtil.isDefined(parameter.getValue().toString())) {
          continue;
        }
      }
      throw new IllegalArgumentException("parameter '" + requiredParameterName +
          "' is missing from the query '" + queryString + "' or is missing from given parameters.");
    }
    if (!Pattern.compile("version.*=.*version[ ]*\\+[ ]*1").matcher(queryString).find()) {
      throw new IllegalArgumentException(
          "version management is missing from the query '" + queryString +
              "' -> expected entity.version = (entity.version + 1)");
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
}
