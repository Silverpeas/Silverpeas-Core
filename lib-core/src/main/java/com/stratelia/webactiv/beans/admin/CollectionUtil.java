package com.stratelia.webactiv.beans.admin;


public class CollectionUtil {

  public static UserDetail[] sortUserDetailArray(UserDetail[] users) {
      if (users == null) {
          UserDetail[] emptyUsers = new UserDetail[0];
          return emptyUsers;
      } else {
          for (int i = users.length; --i>=0; ) {
                boolean swapped = false;
                for (int j = 0; j<i; j++) {
                    if (users[j].getLastName().compareToIgnoreCase(users[j+1].getLastName()) > 0) {
                        UserDetail T = users[j];
                        users[j] = users[j+1];
                        users[j+1] = T;
                        swapped = true;
                    }
                }
                if (!swapped)
                    break;
          }
          return users;
      }
  }  
  
}