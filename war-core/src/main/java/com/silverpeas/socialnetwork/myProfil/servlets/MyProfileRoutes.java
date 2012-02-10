package com.silverpeas.socialnetwork.myProfil.servlets;

public enum MyProfileRoutes {

  Main, MyInfos, MySettings, MyInvitations, MySentInvitations, LinkToSVP, UnlinkFromSVP, CreateLinkToSVP, PublishStatus, DoPublishStatus, UpdatePhoto, UpdateMyInfos, UpdateMySettings, MyWall, MyFeed, MyNetworks;

  public boolean isMyInfos() {
    return this == MyInfos;
  }


}
