package com.stratelia.webactiv.util.viewGenerator.html;

import org.apache.ecs.xhtml.span;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class UserNameGenerator {

  public static span generate(String userId, String currentUserId) {
    return generate(UserDetail.getById(userId), currentUserId);
  }

  public static span generate(UserDetail user, String currentUserId) {
    span userName = new span(org.owasp.encoder.Encode.forHtml(user.getDisplayedName()));
    if (StringUtil.isDefined(currentUserId)) {
      if (!user.getId().equals(currentUserId) && !UserDetail.isAnonymousUser(currentUserId)) {
        userName.setClass("userToZoom");
        userName.addAttribute("rel", user.getId());
      }
    }
    return userName;
  }

  public static String toString(String userId, String currentUserId) {
    return generate(userId, currentUserId).toString();
  }

  public static String toString(UserDetail user, String currentUserId) {
    return generate(user, currentUserId).toString();
  }

}
