package com.google.sps.data;

public final class AuthInfo {
  private boolean isUserLoggedIn;
  private String loginUrl;
  private String logoutUrl;
  public String userName;

  public AuthInfo(boolean isUserLoggedIn, String loginUrl, String logoutUrl){
    this.isUserLoggedIn = isUserLoggedIn;
    this.loginUrl = loginUrl;
    this.logoutUrl = logoutUrl;
  }
}