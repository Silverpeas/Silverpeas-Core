package com.stratelia.webactiv.persistence;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class IdPK extends WAPrimaryKey {

  /**
   * IdPK()
   */
  public IdPK() {
    super("");
  }

  public IdPK(String id) {
    super(id);
  }

  public IdPK(int id) {
    super(Integer.toString(id));
  }

  /**
   * IdPK(String id, WAPrimaryKey value)
   */
  public IdPK(String id, WAPrimaryKey value) {
    super(id, value);
  }

  /**
   * equals
   */
  public boolean equals(Object other) {
    if (!(other instanceof IdPK)) {
      return false;
    } else {
      return (getId() == ((IdPK) other).getId());
    }
  }

  /**
   * setIdAsLong( long value )
   */
  public void setIdAsLong(long value) {
    setId(new Long(value).toString());
  }

  /**
   * getIdAsLong()
   */
  public long getIdAsLong() {
    return new Integer(getId()).longValue();
  }
}
