package com.google.sps.data;
import com.google.sps.data.User;

public final class AuthInfo {
  private boolean isUserLoggedIn;
  private String loginUrl;
  private String logoutUrl;
  public User currentUser;

  public AuthInfo(boolean isUserLoggedIn, String loginUrl, String logoutUrl){
    this.isUserLoggedIn = isUserLoggedIn;
    this.loginUrl = loginUrl;
    this.logoutUrl = logoutUrl;
  }
}