package com.silverpeas.myLinks.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.myLinks.MyLinksRuntimeException;
import com.silverpeas.myLinks.dao.LinkDAO;
import com.silverpeas.myLinks.model.LinkDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * @author
 */
public class MyLinksBmEJB implements SessionBean {

  public Collection getAllLinks(String userId) {
    return getAllLinksByUser(userId);
  }

  public Collection getAllLinksByUser(String userId) {
    Connection con = initCon();
    try {
      Collection links = LinkDAO.getAllLinksByUser(con, userId);
      return links;
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getAllLinksByUser()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public Collection getAllLinksByInstance(String instanceId) {
    Connection con = initCon();
    try {
      Collection links = LinkDAO.getAllLinksByInstance(con, instanceId);
      return links;
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getAllLinksByInstance()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public Collection getAllLinksByObject(String instanceId, String objectId) {
    Connection con = initCon();
    try {
      Collection links = LinkDAO.getAllLinksByObject(con, instanceId, objectId);
      return links;
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getAllLinksByObject()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void createLink(LinkDetail link) {
    Connection con = initCon();
    try {
      int id = LinkDAO.createLink(con, link);
      link.setLinkId(id);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.createLink()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINK_NOT_CREATE", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void deleteLinks(String[] links) {
    Connection con = initCon();
    try {
      for (int i = 0; i < links.length; i++) {
        LinkDAO.deleteLink(con, links[i]);
      }
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.deleteLinks()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINK_NOT_DELETE", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void updateLink(LinkDetail link) {
    Connection con = initCon();
    try {
      LinkDAO.updateLink(con, link);
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.updateLink()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINK_NOT_UPDATE", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public LinkDetail getLink(String linkId) {
    Connection con = initCon();
    try {
      LinkDetail link = LinkDAO.getLink(con, linkId);
      return link;
    } catch (Exception e) {
      throw new MyLinksRuntimeException("MyLinksBmEJB.getLink()",
          SilverpeasRuntimeException.ERROR, "myLinks.MSG_LINKS_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  private Connection initCon() {
    Connection con;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException("MyLinksBmEJB.initCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  private void fermerCon(Connection con) {
    try {
      con.close();
    } catch (SQLException e) {
      // traitement des exceptions
      throw new MyLinksRuntimeException("MyLinksBmEJB.fermerCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  public void ejbCreate() {
    // not implemented
  }

  public void setSessionContext(SessionContext context) {
    // not implemented
  }

  public void ejbRemove() {
    // not implemented
  }

  public void ejbActivate() {
    // not implemented
  }

  public void ejbPassivate() {
    // not implemented
  }

}
