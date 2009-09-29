/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.alertUserPeas.control;

import java.util.Arrays;

import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class AlertUserPeasSessionController extends
    AbstractComponentSessionController {
  protected AlertUser m_AlertUser = null;
  protected Selection m_Selection = null;
  protected NotificationSender m_NotificationSender = null;
  protected String m_Context = "";
  protected UserDetail[] m_userRecipients;
  protected Group[] m_groupRecipients;

  /**
   * Standard Session Controller Constructeur
   * 
   * 
   * @param mainSessionCtrl
   *          The user's profile
   * @param componentContext
   *          The component's profile
   * 
   * @see
   */
  public AlertUserPeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.alertUserPeas.multilang.alertUserPeasBundle",
        "com.stratelia.silverpeas.alertUserPeas.settings.alertUserPeasIcons");
    setComponentRootName(URLManager.CMP_ALERTUSERPEAS);
    m_Context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");
    m_AlertUser = getAlertUser();
    m_Selection = getSelection();
    m_NotificationSender = new NotificationSender(null);
  }

  public void init() {
    m_userRecipients = new UserDetail[0];
    m_groupRecipients = new Group[0];
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ------------------------------------------- Navigation Functions
  // ----------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  public PairObject getHostComponentName() {
    return m_AlertUser.getHostComponentName();
  }

  public String getHostSpaceName() {
    return m_AlertUser.getHostSpaceName();
  }

  public String getHostComponentId() {
    return m_AlertUser.getHostComponentId();
  }

  public UserDetail[] getUserRecipients() {
    return m_userRecipients;
  }

  public Group[] getGroupRecipients() {
    return m_groupRecipients;
  }

  public NotificationMetaData getNotificationMetaData() {
    return m_AlertUser.getNotificationMetaData();
  }

  // initialisation de Selection pour nav vers SelectionPeas
  public String initSelection() {
    String url = m_Context + URLManager.getURL(getComponentRootName());
    String goUrl = url + "FromSelection";
    String cancelUrl = url + "Close";

    m_Selection.resetAll();

    m_Selection.setGoBackURL(goUrl);
    m_Selection.setCancelURL(cancelUrl);

    // bien que le up s'affiche en popup, le mécanisme de fermeture est assuré
    // par le composant=> il est donc nécessaire d'indiquer
    // à l'UserPanelPeas de ne pas s'occuper de cette fermeture!
    m_Selection.setHostPath(null);
    m_Selection.setHostComponentName(getHostComponentName());
    m_Selection.setHostSpaceName(getHostSpaceName());
    m_Selection.setFirstPage(Selection.FIRST_PAGE_BROWSE);

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getHostComponentId());
    m_Selection.setExtraParams(sug);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  // recupération des users et groupes selectionnés au travers de selectionPeas
  public void computeSelection() {
    m_userRecipients = SelectionUsersGroups.getUserDetails(m_Selection
        .getSelectedElements());
    m_groupRecipients = SelectionUsersGroups.getGroups(m_Selection
        .getSelectedSets());
    Arrays.sort(m_userRecipients);
    Arrays.sort(m_groupRecipients);
  }

  public void prepareNotification(String message) {
    NotificationMetaData notifMetaData = getNotificationMetaData();
    notifMetaData.addUserRecipients(SelectionUsersGroups
        .getUserIds(getUserRecipients()));
    notifMetaData.addGroupRecipients(SelectionUsersGroups
        .getGroupIds(getGroupRecipients()));
    if (message != null && message.trim().length() > 0
        && (getUserRecipients().length > 0 || getGroupRecipients().length > 0)) {
      /*
       * StringBuffer content = new
       * StringBuffer(notifMetaData.getContent("fr"));
       * content.append(getString("AuthorMessage"
       * )).append(" : \"").append(message).append("\"\n\n\n");
       * notifMetaData.setContent("fr", content.toString());
       */
      setNotificationContent(message, "fr");
      setNotificationContent(message, "en");
    }
  }

  private void setNotificationContent(String message, String language) {
    StringBuffer content = new StringBuffer(getNotificationMetaData()
        .getContent(language));
    SilverTrace.info("alertUserPeas",
        "AlertUserPeasSessionController.setNotificationContent()",
        "root.MSG_GEN_PARAM_VALUE", "language = " + language + ", content = "
            + content.toString());
    content.append("\n\n").append(getString("AuthorMessage")).append(" : \n\"")
        .append(message).append("\"");
    getNotificationMetaData().setContent(content.toString(), language);
    SilverTrace.info("alertUserPeas",
        "AlertUserPeasSessionController.setNotificationContent()",
        "root.MSG_GEN_EXIT_METHOD", "content = "
            + getNotificationMetaData().getContent(language));
  }

  public void sendNotification() {
    try {
      SilverTrace.info("alertUserPeas",
          "AlertUserPeasSessionController.sendNotification()",
          "root.MSG_GEN_PARAM_VALUE", "content_en = "
              + getNotificationMetaData().getContent("en"));
      m_NotificationSender.notifyUser(getNotificationMetaData());
    } catch (NotificationManagerException e) {
      SilverTrace.warn("alertUserPeas",
          "AlertUserPeasSessionController.sendNotification()",
          "root.EX_NOTIFY_USERS_FAILED", e);
    }
  }

}
