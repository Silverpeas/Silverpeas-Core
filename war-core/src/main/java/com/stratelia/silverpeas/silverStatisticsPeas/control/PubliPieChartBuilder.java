/*
 * Created on 5 août 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.stratelia.silverpeas.silverStatisticsPeas.control;

import java.sql.SQLException;
import java.util.Hashtable;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author CBONIN
 * 
 * 
 */
public class PubliPieChartBuilder extends AbstractPieChartBuilder {
  private String dateStat;
  private String dateFormate;
  private String currentUserId;
  private String filterIdGroup;
  private String filterIdUser;
  private String spaceId;
  private OrganizationController organizationController;
  private ResourceLocator message;

  public PubliPieChartBuilder(String dateStat, String dateFormate,
      String currentUserId, String filterIdGroup, String filterIdUser,
      String spaceId, OrganizationController organizationController,
      ResourceLocator message) {
    this.dateStat = dateStat;
    this.dateFormate = dateFormate;
    this.currentUserId = currentUserId;
    this.filterIdGroup = filterIdGroup;
    this.filterIdUser = filterIdUser;
    this.spaceId = spaceId;
    this.organizationController = organizationController;
    this.message = message;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.stratelia.silverpeas.silverStatisticsPeas.control.AbstractPieChartBuilder
   * #getChartTitle()
   */
  public String getChartTitle() {
    String title = message.getString("silverStatisticsPeas.VolumeNumber") + " ";

    if (!this.filterIdGroup.equals("") && this.filterIdUser.equals("")) {
      title += " "
          + message.getString("silverStatisticsPeas.EvolutionAccessGroup")
          + " "
          + this.organizationController.getGroup(this.filterIdGroup).getName()
          + " ";
    }
    if (!this.filterIdUser.equals("")) {
      title += " "
          + message.getString("silverStatisticsPeas.EvolutionAccessUser")
          + " "
          + this.organizationController.getUserDetail(this.filterIdUser)
              .getLastName() + " ";
    }

    try {
      if ((this.spaceId != null) && (this.spaceId.length() > 0)
          && (!this.spaceId.equals("WA0"))) {
        SpaceInstLight space = new Admin().getSpaceInstLightById(this.spaceId);
        title += message.getString("silverStatisticsPeas.FromSpace") + " ["
            + space.getName() + "] ";
      }
    } catch (Exception e) {
      SilverTrace
          .error("silverStatisticsPeas",
              "PubliPieChartBuilder.getChartTitle()",
              "root.EX_SQL_QUERY_FAILED", e);
    }

    title += message.getString("silverStatisticsPeas.In") + " "
        + this.dateFormate;

    return title;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.stratelia.silverpeas.silverStatisticsPeas.control.AbstractPieChartBuilder
   * #getCmpStats()
   */
  Hashtable getCmpStats() {
    // Hashtable key=componentId, value=new String[3] {tout, groupe, user}
    Hashtable cmpStats = new Hashtable();
    try {
      cmpStats.putAll(SilverStatisticsPeasDAOAccesVolume
          .getStatsPublicationsVentil(dateStat, currentUserId, filterIdGroup,
              filterIdUser));
    } catch (SQLException e) {
      SilverTrace.error("silverStatisticsPeas",
          "PubliPieChartBuilder.getCmpStats()", "root.EX_SQL_QUERY_FAILED", e);
    }
    return cmpStats;
  }

}
