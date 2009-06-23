package com.stratelia.webactiv.util.readingControl.control;

import java.sql.Connection;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.readingControl.ejb.PublicationActorLinkDAO;
import com.stratelia.webactiv.util.readingControl.model.ReadingControlRuntimeException;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import com.stratelia.webactiv.util.statistic.model.HistoryNodePublicationActorDetail;


public class ReadingControlBmEJB implements SessionBean {

    private final String rootTableName = "ReadingControl";
    private String dbName = JNDINames.FAVORIT_DATASOURCE;

    public ReadingControlBmEJB() {
    }

    private Connection getConnection() {
        try {
            Connection con = DBUtil.makeConnection(dbName);
            return con;
        } catch (Exception e) {
            throw new ReadingControlRuntimeException("ReadingControlBmEJB.getConnection()",SilverpeasRuntimeException.ERROR,"root.EX_CONNECTION_OPEN_FAILED", e);
        }
    }

    private void freeConnection(Connection con) {
        if (con != null)
            try {
                con.close();
            } catch (Exception e) {
                SilverTrace.error("readingControl","ReadingControlBmEJB.freeConnection()", "root.EX_EMERGENCY_CONNECTION_CLOSE_FAILED",e);
            }
    }

    public void addReadingControls(Collection userIds, PublicationPK pubPK) {
        SilverTrace.info("readingControl","ReadingControlBmEJB.addReadingControls()", "root.MSG_GEN_ENTER_METHOD");
        Connection con = null;
        try {
            con = getConnection();
            PublicationActorLinkDAO.addReadingControls(con, rootTableName, userIds, pubPK);
        } catch (Exception e) {
            throw new ReadingControlRuntimeException("ReadingControlBmEJB.addReadingControls()",SilverpeasRuntimeException.ERROR,"readingControl.EX_ADD_READING_CONTROL_FAILED", e);
        } finally {
            freeConnection(con);
        }
        SilverTrace.info("readingControl","ReadingControlBmEJB.addReadingControls()", "root.MSG_GEN_EXIT_METHOD");
    }

    public void removeReadingControl(Collection userIds, PublicationPK pubPK){
        SilverTrace.info("readingControl","ReadingControlBmEJB.removeReadingControl()", "root.MSG_GEN_ENTER_METHOD");
        Connection con = null;
        try {
            con = getConnection();
            PublicationActorLinkDAO.remove(con, rootTableName, userIds, pubPK);
        } catch (Exception e) {
            throw new ReadingControlRuntimeException("ReadingControlBmEJB.removeReadingControl()",SilverpeasRuntimeException.ERROR,"readingControl.EX_REMOVE_READING_CONTROL_FAILED","pubPK = "+pubPK.toString(), e);
        } finally {
            freeConnection(con);
        }
        SilverTrace.info("readingControl","ReadingControlBmEJB.removeReadingControl()", "root.MSG_GEN_EXIT_METHOD");
    }

    public void removeReadingControlByUser(String userId){
        SilverTrace.info("readingControl","ReadingControlBmEJB.removeReadingControlByUser()", "root.MSG_GEN_ENTER_METHOD");
        Connection con = null;
        try {
            con = getConnection();
            PublicationActorLinkDAO.removeByUser(con, rootTableName, userId);
        } catch (Exception e) {
            throw new ReadingControlRuntimeException("ReadingControlBmEJB.removeReadingControlByUser()",SilverpeasRuntimeException.ERROR,"readingControl.EX_REMOVE_READING_CONTROL_BY_USER_FAILED","userId = "+userId,e);
        } finally {
            freeConnection(con);
        }
        SilverTrace.info("readingControl","ReadingControlBmEJB.removeReadingControlByUser()", "root.MSG_GEN_EXIT_METHOD");
    }

    public void removeReadingControlByPublication(PublicationPK pubPK) {
        SilverTrace.info("readingControl","ReadingControlBmEJB.removeReadingControlByPublication()", "root.MSG_GEN_ENTER_METHOD");
        Connection con = null;
        try {
            con = getConnection();
            PublicationActorLinkDAO.removeByPublication(con, rootTableName, pubPK);
        } catch (Exception e) {
            throw new ReadingControlRuntimeException("ReadingControlBmEJB.removeReadingControlByPublication()",SilverpeasRuntimeException.ERROR,"readingControl.EX_REMOVE_READING_CONTROL_BY_PUBLICATION_FAILED","pubPK = "+pubPK.toString(),e);
        } finally {
            freeConnection(con);
        }
        SilverTrace.info("readingControl","ReadingControlBmEJB.removeReadingControlByPublication()", "root.MSG_GEN_EXIT_METHOD");
    }

    public Collection getReadingControls(PublicationPK pubPK){
        SilverTrace.info("readingControl","ReadingControlBmEJB.getReadingControls()", "root.MSG_GEN_ENTER_METHOD");
        Connection con = null;
        Collection result = null;
        try {
            con = getConnection();
            result = PublicationActorLinkDAO.getReadingControls(con, rootTableName, pubPK);
        } catch (Exception e) {
            throw new ReadingControlRuntimeException("ReadingControlBmEJB.getReadingControls()",SilverpeasRuntimeException.ERROR,"readingControl.EX_GET_READING_CONTROL_BY_PUBLICATION_FAILED","pubPK = "+pubPK.toString(), e);
        } finally {
            freeConnection(con);
        }
        SilverTrace.info("readingControl","ReadingControlBmEJB.getReadingControls()", "root.MSG_GEN_EXIT_METHOD");
        return result;
    }

    public Hashtable getReadingStates(PublicationPK pubPK) {
        SilverTrace.info("readingControl","ReadingControlBmEJB.getReadingStates()", "root.MSG_GEN_ENTER_METHOD");
        Collection history = null;
        StatisticBmHome statisticBmhome = null;
        StatisticBm statisticBm = null;

        //get the user Ids who have TO read the publication
        Collection controlledUsers = getReadingControls(pubPK);
        Iterator iterator = controlledUsers.iterator();

        //init the hashTable
        Hashtable result = new Hashtable();
        while (iterator.hasNext()) {
            String userId = (String) iterator.next();
            String neverReadStr = new String("Never");
            result.put(userId, neverReadStr);
        }

        //get the users who have read the publication
        try {
            statisticBmhome = (StatisticBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBmHome.class);
            statisticBm = statisticBmhome.create();
            history = statisticBm.getReadingHistoryByPublication(pubPK);
        } catch (Exception e) {
            throw new ReadingControlRuntimeException("ReadingControlBmEJB.getReadingStates()",SilverpeasRuntimeException.ERROR,"root.EX_GET_READING_STATES_FAILED", e);
        }
        Iterator historyIterator = history.iterator();

        //check the controlled users who have read
        while (historyIterator.hasNext()) {
            HistoryNodePublicationActorDetail historyItem = (HistoryNodePublicationActorDetail) historyIterator.next();
            String actorId = historyItem.getUserId();
            java.util.Date date = historyItem.getDate();
            if (result.containsKey(actorId)){
                result.remove(actorId);
                result.put(actorId, date);
            }
        }
        SilverTrace.info("readingControl","ReadingControlBmEJB.getReadingStates()", "root.MSG_GEN_EXIT_METHOD");
        return result;
    }

  public void ejbCreate() throws CreateException {
    //Debug.println("ReadingControlBmEJB.ejbCreate()");
  }

  public void ejbRemove() {
    //Debug.println("ReadingControlBmEJB.ejbRemove()");
  }

  public void ejbActivate() {
    //Debug.println("ReadingControlBmEJB.ejbActivate()");
  }
  public void ejbPassivate() {
    //Debug.println("ReadingControlBmEJB.ejbPassivate()");
  }

  public void setSessionContext(SessionContext sc) {
    //Debug.println("ReadingControlBmEJB.setSessionContext()");
  }
}