/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.statistic.model;

import java.io.Serializable;
import java.util.Date;

import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class HistoryByUser implements Serializable {
  private UserDetail user;
  private Date lastAccess;
  private int nbAccess;

  /**
   * Constructor declaration
   * 
   * 
   * @param date
   * @param userId
   * @param nbAccess
   * 
   * @see
   */
  public HistoryByUser(UserDetail user, Date lastAccess, int nbAccess) {
    this.lastAccess = lastAccess;
    this.user = user;
    this.nbAccess = nbAccess;
  }

  public Date getLastAccess() {
    return lastAccess;
  }

  public UserDetail getUser() {
    return user;
  }

  public int getNbAccess() {
    return nbAccess;
  }

  public void setLastAccess(Date lastAccess) {
    this.lastAccess = lastAccess;
  }

  public void setNbAccess(int nbAccess) {
    this.nbAccess = nbAccess;
  }

  public void setUser(UserDetail user) {
    this.user = user;
  }

}
