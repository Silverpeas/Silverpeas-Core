package com.stratelia.webactiv.util.pool;

import java.sql.Connection;
import java.sql.SQLException;

public class PooledConnectionWrapper extends ConnectionWrapper {

  int connectionLot;

  public PooledConnectionWrapper(Connection toDelegate, int cl) {
    super(toDelegate);
    connectionLot = cl;
  }

  public void close() throws SQLException {
    ConnectionPool.releaseConnections();
  }

}