package com.stratelia.webactiv.util;

import java.util.ArrayList;
import java.util.List;

import com.stratelia.webactiv.util.exception.UtilException;

/**
 * The SchemaPool class manages a pool of Schema shared by all the client (admin
 * classes).
 * 
 * All the public methods are static and the calls are deferred to a singleton.
 */
public abstract class SchemaPool {
  /**
   * The unique SchemaPool built to serve all the requests.
   */
  private int connectionLot = 0;
  private final List pool = new ArrayList();

  abstract protected Schema newSchema(int connectionLot) throws UtilException;

  /**
   * The constructor is private, so we can ensure that only one pool will be
   * created in the JVM.
   */
  protected SchemaPool() {
  }

  protected void releaseSchemas() {
    synchronized (pool) {
      connectionLot++;
      pool.clear();
    }
  }

  /**
   * Returns an Shema.
   * 
   * The returned schema is removed from the pool : so, if our client forgets to
   * release this schema we don't keep a useless entry.
   */
  protected Schema getInstance() throws UtilException {
    Schema instance = null;

    while (instance == null) {
      synchronized (pool) {
        if (!pool.isEmpty()) {
          instance = (Schema) pool.remove(0);
        }
      }

      if (instance == null) {
        // the pool is empty, so we build a new schema.
        instance = newSchema(connectionLot);
      } else {
        // throw away the instance if not ok.
        if (!instance.isOk())
          instance = null;
      }
    }
    return instance;
  }

  /**
   * Release an Scheme previously returned by the pool.
   * 
   * We put the released schema in the pool unless this schema isn't ok.
   */
  protected void release(Schema s) {
    if (s != null && s.isOk()) {
      synchronized (pool) {
        if (s.getConnectionLot() == connectionLot) {
          pool.add(s);
        }
      }
    }
  }

}
