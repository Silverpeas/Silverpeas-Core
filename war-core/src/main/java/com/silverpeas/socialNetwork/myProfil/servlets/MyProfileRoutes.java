package com.silverpeas.socialNetwork.myProfil.servlets;

public enum MyProfileRoutes {
  
  Main, MyInfos, MySettings, MyInvitations, MySentInvitations, UpdatePhoto, UpdateMyInfos, UpdateMySettings, MyWall, MyFeed;
  
  public boolean isMyInfos() {
    return this == MyInfos;
  }
  

}
