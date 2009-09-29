package com.stratelia.silverpeas.domains.silverpeasdriver;

import com.stratelia.webactiv.organization.AdminPersistenceException;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.SchemaPool;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * The DomainSPSchemaPool class manages a pool of DomainSPSchema shared by all
 * the client (admin classes).
 * 
 * All the public methods are static and the calls are deferred to a singleton.
 */
public class DomainSPSchemaPool extends SchemaPool {
  /**
   * The unique OrganizationSchemaPool built to serve all the requests.
   */
  static private DomainSPSchemaPool singleton = new DomainSPSchemaPool();

  /**
   * The constructor is private, so we can ensure that only one pool will be
   * created in the JVM.
   */
  private DomainSPSchemaPool() {
  }

  /**
   * Returns an DomainSPSchemaPool.
   * 
   * The returned schema must be released after use.
   */
  static public DomainSPSchema getDomainSPSchema()
      throws AdminPersistenceException {
    try {
      return (DomainSPSchema) singleton.getInstance();
    } catch (UtilException ue) {
      throw new AdminPersistenceException("DomainSPSchema.getSchema",
          SilverpeasException.ERROR, "root.EX_DATASOURCE_INVALID", ue);
    }
  }

  /**
   * Release an DomainSPSchema previously returned by the pool.
   */
  static public void releaseDomainSPSchema(DomainSPSchema s) {
    singleton.release(s);
  }

  static public void releaseConnections() {
    singleton.releaseSchemas();
  }

  protected Schema newSchema(int connectionLot) throws UtilException {
    return (Schema) (new DomainSPSchema(connectionLot));
  }
}
