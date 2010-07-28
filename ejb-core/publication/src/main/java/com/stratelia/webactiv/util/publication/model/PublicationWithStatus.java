package com.stratelia.webactiv.util.publication.model;

/**
 * 
 * @author bourakbi
 * */


public class PublicationWithStatus {
    
    private PublicationDetail pub;
    private boolean update;
    
    public PublicationWithStatus(PublicationDetail pub, boolean update) {
      this.pub = pub;
      this.update = update;
    }

    public PublicationDetail getPublication() {
      return pub;
    }

    public boolean isUpdate() {
      return update;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((pub == null) ? 0 : pub.hashCode());
      result = prime * result + (update ? 1231 : 1237);
      return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof PublicationWithStatus)) {
        return false;
      }
      PublicationWithStatus other = (PublicationWithStatus) obj;
      if (pub == null) {
        if (other.pub != null) {
          return false;
        }
      } else if (!pub.equals(other.pub)) {
        return false;
      }
      if (update != other.update) {
        return false;
      }
      return true;
    }
    
    
}
