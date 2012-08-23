/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.wysiwyg.dynamicvalue.pool;

import java.io.Serializable;

/**
 * class which contains the connection information to access database
 */
public class ConnectionPoolInformation implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 4167870461229759855L;

  /** Description */
  private String description = null;
  /** Password */
  private String password = null;
  /** Url name */
  private String url = null;
  /** User name */
  private String user = null;
  /** Driver class name */
  private String driver = null;

  /** JNDI name of the resource allowing the access to database */
  private String jndiName = null;

  /**
   * type of connection (JNDI or JDBC)
   */
  private String connectionType = null;

  /**
   *The maximum number of active connections that can be allocated from this pool at the same time,
   * or non-positive for no limit.
   */
  private int maxActive = 10;
  /**
   * The maximum number of active connections that can remain idle in the pool, without extra ones
   * being released, or negative for no limit.
   */
  private int maxIdle = 10;
  /**
   * The number of milliseconds to sleep between runs of the idle-object evictor thread. When
   * negative, no idle-object evictor thread will run. Use this parameter only when you want the
   * evictor thread to run.
   */
  private int timeBetweenEvictionRunsMillis = -1;
  /**
   * The number of objects to examine during each run of the idle object evictor thread (if any)
   */
  private int numTestsPerEvictionRun = -1;
  /**
   * The minimum amount of time an object, if active, may sit idle in the pool before it is eligible
   * for eviction by the idle-object evictor. If a negative value is supplied, no objects are
   * evicted due to idle time alone.
   */
  private int minEvictableIdleTimeMillis = -1;
  /**
   * The maximum number of milliseconds that the pool will wait (when there are no available
   * connections) for a connection to be returned before throwing an exception, or -1 to wait
   * indefinitely.
   */
  private int maxWait = 50;

  /**
   * default constructor
   */
  public ConnectionPoolInformation() {
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * @return the driver
   */
  public String getDriver() {
    return driver;
  }

  /**
   * @param driver the driver to set
   */
  public void setDriver(String driver) {
    this.driver = driver;
  }

  /**
   * @return the maxActive
   */
  public int getMaxActive() {
    return maxActive;
  }

  /**
   * @param maxActive the maxActive to set
   */
  public void setMaxActive(int maxActive) {
    this.maxActive = maxActive;
  }

  /**
   * @return the maxIdle
   */
  public int getMaxIdle() {
    return maxIdle;
  }

  /**
   * @param maxIdle the maxIdle to set
   */
  public void setMaxIdle(int maxIdle) {
    this.maxIdle = maxIdle;
  }

  /**
   * @return the timeBetweenEvictionRunsMillis
   */
  public int getTimeBetweenEvictionRunsMillis() {
    return timeBetweenEvictionRunsMillis;
  }

  /**
   * @param timeBetweenEvictionRunsMillis the timeBetweenEvictionRunsMillis to set
   */
  public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
    this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
  }

  /**
   * @return the numTestsPerEvictionRun
   */
  public int getNumTestsPerEvictionRun() {
    return numTestsPerEvictionRun;
  }

  /**
   * @param numTestsPerEvictionRun the numTestsPerEvictionRun to set
   */
  public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
    this.numTestsPerEvictionRun = numTestsPerEvictionRun;
  }

  /**
   * @return the minEvictableIdleTimeMillis
   */
  public int getMinEvictableIdleTimeMillis() {
    return minEvictableIdleTimeMillis;
  }

  /**
   * @param minEvictableIdleTimeMillis the minEvictableIdleTimeMillis to set
   */
  public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
    this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
  }

  /**
   * @return the jndiName
   */
  public String getJndiName() {
    return jndiName;
  }

  /**
   * @param jndiName the jndiName to set
   */
  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ConnectionPoolInformation [");
    if (description != null) {
      builder.append("description=");
      builder.append(description);
      builder.append(", ");
    }
    if (driver != null) {
      builder.append("driver=");
      builder.append(driver);
      builder.append(", ");
    }
    builder.append("maxActive=");
    builder.append(maxActive);
    builder.append(", maxIdle=");
    builder.append(maxIdle);
    builder.append(", minEvictableIdleTimeMillis=");
    builder.append(minEvictableIdleTimeMillis);
    builder.append(", numTestsPerEvictionRun=");
    builder.append(numTestsPerEvictionRun);
    builder.append(", ");
    if (password != null) {
      builder.append("password=");
      builder.append(password);
      builder.append(", ");
    }
    builder.append("timeBetweenEvictionRunsMillis=");
    builder.append(timeBetweenEvictionRunsMillis);
    builder.append(", ");
    if (url != null) {
      builder.append("url=");
      builder.append(url);
      builder.append(", ");
    }
    if (user != null) {
      builder.append("user=");
      builder.append(user);
    }
    builder.append("]");
    return builder.toString();
  }

  /**
   * @return the maxWait
   */
  public int getMaxWait() {
    return maxWait;
  }

  /**
   * @param maxWait the maxWait to set
   */
  public void setMaxWait(int maxWait) {
    this.maxWait = maxWait;
  }

  /**
   * @return the connectionType
   */
  public String getConnectionType() {
    return connectionType;
  }

  /**
   * @param connectionType the connectionType to set
   */
  public void setConnectionType(String connectionType) {
    this.connectionType = connectionType;
  }

}
