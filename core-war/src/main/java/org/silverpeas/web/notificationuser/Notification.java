package org.silverpeas.web.notificationuser;

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.owasp.encoder.Encode;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Notification {

  private String subject;
  private String body;
  private int priority = 0;
  private int channel = NotificationParameters.ADDRESS_COMPONENT_DEFINED;
  private List<UserDetail> users;
  private List<Group> groups;

  public Notification() {

  }

  public Notification(String subject, String body) {
    setSubject(subject);
    setBody(body);
  }

  public String getSubject() {
    return subject;
  }
  public void setSubject(String subject) {
    this.subject = subject;
  }
  public String getBody() {
    return body;
  }
  public void setBody(String body) {
    this.body = body;
  }
  public int getPriority() {
    return priority;
  }
  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void setPriority(String priority) {
    if (StringUtil.isInteger(priority)) {
      this.priority = Integer.parseInt(priority);
    }
  }

  public int getChannel() {
    return channel;
  }
  public void setChannel(String channel) {
    if (StringUtil.isInteger(channel)) {
      this.channel = Integer.parseInt(channel);
    }
  }
  public List<UserDetail> getUsers() {
    if (users == null) {
      users = new ArrayList<UserDetail>();
    }
    return users;
  }
  public List<String> getUserIds() {
    List<String> ids = new ArrayList<String>();
    if (users != null) {
      for (UserDetail user : users) {
        ids.add(user.getId());
      }
    }
    return ids;
  }

  public void setUsers(List<UserDetail> users) {
    this.users = users;
  }
  public void setUsers(String... ids) {
    getUsers().clear();
    for (String id : ids) {
      UserDetail user = UserDetail.getById(id);
      if (user != null) {
        getUsers().add(user);
      }
    }
  }
  public List<Group> getGroups() {
    if (groups == null) {
      groups = new ArrayList<Group>();
    }
    return groups;
  }
  public List<String> getGroupIds() {
    List<String> ids = new ArrayList<String>();
    if (getGroups() != null) {
      for (Group group : groups) {
        ids.add(group.getId());
      }
    }
    return ids;
  }
  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

  public void setGroups(String... groupIds) {
    getGroups().clear();
    for (String id : groupIds) {
      Group group = Group.getById(id);
      if (group != null) {
        getGroups().add(group);
      }
    }
  }

  public NotificationMetaData toNotificationMetaData() {
    NotificationMetaData notifMetaData =
        new NotificationMetaData(getPriority(), Encode.forHtml(getSubject()),
            Encode.forHtml(getBody()));
    List<UserRecipient> userRecipients = new ArrayList<UserRecipient>();
    if (getUsers() != null) {
      for (UserDetail user : getUsers()) {
        userRecipients.add(new UserRecipient(user));
      }
    }
    notifMetaData.addUserRecipients(userRecipients);
    List<GroupRecipient> groupRecipients = new ArrayList<GroupRecipient>();
    if (getGroups() != null) {
      for (Group group : getGroups()) {
        groupRecipients.add(new GroupRecipient(group));
      }
    }
    notifMetaData.addGroupRecipients(groupRecipients);
    for (String language : DisplayI18NHelper.getLanguages()) {
      LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.alertUserPeas.multilang.alertUserPeasBundle", language);
      notifMetaData.addLanguage(language, getSubject(), bundle.getString("AuthorMessage") + " :");
      notifMetaData.addExtraMessage(getBody(), language);
    }
    return notifMetaData;
  }



}
