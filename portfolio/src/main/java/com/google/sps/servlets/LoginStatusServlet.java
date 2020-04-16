package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.sps.data.AuthInfo;
import com.google.sps.data.User;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@WebServlet("/login-status")
public class LoginStatusServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    boolean isUserLoggedIn = userService.isUserLoggedIn();
    String loginUrl = userService.createLoginURL("/");
    String logoutUrl = userService.createLogoutURL("/");
    AuthInfo authInfo = new AuthInfo(isUserLoggedIn, loginUrl, logoutUrl);
    

    if(isUserLoggedIn) {
      // save user info
      String id = userService.getCurrentUser().getUserId();
      String email = userService.getCurrentUser().getEmail();
      Entity userEntity;
      try{
        Key userKey = KeyFactory.createKey("User", id);
        userEntity = datastore.get(userKey);
      } catch(EntityNotFoundException e){
        userEntity = new Entity("User", id);
        userEntity.setProperty("email", email);
      }
      String userName = (String) userEntity.getProperty("userName");

      datastore.put(userEntity);

      User currentUser = new User(email, userName);
      authInfo.currentUser = currentUser;
    }

    Gson gson = new Gson();
    String userStatus = gson.toJson(authInfo);

    response.setContentType("application/json");
    response.getWriter().println(userStatus);
  }

  private Boolean isStringValid(String value){
    return value != null && !value.trim().isEmpty();
  }

}