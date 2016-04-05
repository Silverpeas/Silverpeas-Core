package org.silverpeas.core.persistence.datasource.repository.jpa;

import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.persistence.datasource.repository.BasicEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.QueryCriteria;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.PaginationList;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author: ebonnet
 */
public class JpaBasicEntityManager<ENTITY extends IdentifiableEntity, ENTITY_IDENTIFIER_TYPE
    extends EntityIdentifier>
    implements BasicEntityRepository<ENTITY, ENTITY_IDENTIFIER_TYPE> {

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
    Collection<ENTITY_IDENTIFIER_TYPE> identifiers = new ArrayList<>(size);
    if (size > 0) {
      for (String id : idsAsString) {
        identifiers.add(convertToEntityIdentifier(id));
      }
    }
    return identifiers;
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
   * Synchronizes the persistence context to the underlying data source. Within a transactional
   * context, the persistence context is directly put to the data source but will be effective
   * only when the transaction will be committed. The consequence of the synchronization within
   * a transaction context is the persistence context is then validated by the data source. Making
   * it work, the data source has to support the transactions.
   * <p>
   * Warning, the behavior of this method is implementation-dependent. According to the type of
   * the repository or of the underlying data source, the flush can not to be working.
   */
  @Override
  public void flush() {
    getEntityManager().flush();
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
   * Finds the single entity matching the specified named query (a JPQL instruction)
   * and with the specified parameters.
   * @param namedQuery the named query. It is an identifier to a JPQL instruction.
   * @param parameters the parameters to apply on the query.
   * @return the single entity that matches the specified query or null if no one matches the
   * query.
   * @throws java.lang.IllegalArgumentException if there is more than one entity matching the query.
   */
  public ENTITY findOneByNamedQuery(String namedQuery, final NamedParameters parameters) {
    return unique(listFromNamedQuery(namedQuery, parameters));
  }

  /**
   * Finds the first entity matching the specified named query (a JPQL instruction)
   * and with the specified parameters.
   * @param namedQuery the named query. It is an identifier to a JPQL instruction.
   * @param parameters the parameters to apply on the query.
   * @return the first encountered entity that matches the specified query or null if no entities
   * match the specified query.
   */
  public ENTITY findFirstByNamedQuery(String namedQuery, final NamedParameters parameters) {
    List<ENTITY> results = listFromNamedQuery(namedQuery, parameters);
    return (results.isEmpty() ? null:results.get(0));
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
    List<ENTITY> entities = new ArrayList<>(ids.size());
    String selectQuery = "select a from " + getEntityClass().getName() + " a where a.id in :ids";
    for (Collection<ENTITY_IDENTIFIER_TYPE> entityIds : split(new HashSet<>(ids))) {
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
  public ENTITY save(final ENTITY entity) {
    return save(Collections.singletonList(entity)).get(0);
  }

  @Override
  public ENTITY saveAndFlush(final ENTITY entity) {
    ENTITY curEntity = save(entity);
    flush();
    return curEntity;
  }

  @SafeVarargs
  @Override
  public final List<ENTITY> save(final ENTITY... entities) {
    return save(CollectionUtil.asList(entities));
  }

  @Override
  public List<ENTITY> save(final List<ENTITY> entities) {
    List<ENTITY> savedEntities = new ArrayList<>(entities.size());
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

  @SafeVarargs
  @Override
  public final void delete(final ENTITY... entity) {
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
   * @param instanceId the unique instance identifier.
   * @return the number of deleted entities.
   */
  @Override
  public long deleteByComponentInstanceId(final String instanceId) {
    Query deleteQuery = getEntityManager().createQuery(
        "delete from " + getEntityClass().getName() + " a where a.instanceId = :instanceId");
    return newNamedParameters().add("instanceId", instanceId).applyTo(deleteQuery).executeUpdate();
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
   * @return instance of {@link NamedParameter}
   */
  public NamedParameters newNamedParameters() {
    return new NamedParameters();
  }

  /**
   * Gets an instance that represents the fact that it does not exist parameter for the query to
   * execute.
   * @return instance of {@link NoNamedParameter}
   */
  public NoNamedParameter noParameter() {
    return new NoNamedParameter();
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
    return parameters.applyTo(updateQuery).executeUpdate();
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
