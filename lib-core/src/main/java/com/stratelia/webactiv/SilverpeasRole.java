package com.stratelia.webactiv;

public enum SilverpeasRole {
  admin, publisher, writer, user;

  public boolean isInRole(String role) {
    return this.equals(valueOf(role));
  }
}
