package com.silverpeas.socialNetwork.myProfil.servlets;

public enum MyProfileRoutes {
  
  Main, MyInfos, MySettings, MyInvitations, MySentInvitations,SendInvitation,
  CancelSentInvitation, IgnoreInvitation, AcceptInvitation;
  
  public boolean isMyInfos() {
    return this == MyInfos;
  }
  

}
