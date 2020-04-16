// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@WebServlet("/edit-request/*")
public class EditRequestServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    // must be logged in to edit
    UserService userService = UserServiceFactory.getUserService();
    if(!userService.isUserLoggedIn()){
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.sendRedirect("/");
      return;
    }

    //check for malformed url
    if(request.getPathInfo() == null || request.getPathInfo().length() < 2) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.sendRedirect("/");
      return;
    }

    // ensure request exists
    String requestKeyString = request.getPathInfo().substring(1);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity requestEntity;
    try{
      Key requestKey = KeyFactory.stringToKey(requestKeyString);
      requestEntity = datastore.get(requestKey);
    } catch(IllegalArgumentException | EntityNotFoundException e){
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // ensure only requester can edit request
    Key requesterKey = (Key) requestEntity.getProperty("requester");
    Entity requesterEntity = null;
    
    try{ 
      requesterEntity = datastore.get(requesterKey);
    } catch(EntityNotFoundException e){
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    String requesterEmail = (String) requesterEntity.getProperty("email");
    if(!requesterEmail.equals(userService.getCurrentUser().getEmail())){
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.sendRedirect("/");
      return;
    }

    RequestDispatcher view = request.getRequestDispatcher("/edit-request.html");
    view.forward(request, response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    
    // must be logged in
    if(!userService.isUserLoggedIn()){
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.sendRedirect("/");
      return;
    }

    // date field is required
    String dateString = request.getParameter("return-date");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date returnDate = null;
    try {  
      returnDate = sdf.parse(dateString);
    } catch (ParseException e) {
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.sendRedirect("/");
      return;
    }

    long timestamp = System.currentTimeMillis();
    String title = request.getParameter("title");
    String userName = request.getParameter("userName");
    String author = request.getParameter("author");
    String isbn = request.getParameter("isbn");
    
    // book title and user name fields are required
    if(isStringValid(title) && isStringValid(userName)) {      
      Entity bookEntity = new Entity("Book");
      bookEntity.setProperty("timestamp", timestamp);
      bookEntity.setProperty("title", title);
      bookEntity.setProperty("author", author);
      bookEntity.setProperty("isbn", isbn);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      datastore.put(bookEntity);
      
      // update user info
      String id = userService.getCurrentUser().getUserId();
      String email = userService.getCurrentUser().getEmail();
      Entity userEntity;
      try {
        Key userKey = KeyFactory.createKey("User", id);
        userEntity = datastore.get(userKey);
      } catch(EntityNotFoundException e) {
        userEntity = new Entity("User", id);
        userEntity.setProperty("email", email);
      }
      userEntity.setProperty("userName", userName);

      datastore.put(userEntity);

      Entity requestEntity = new Entity("Request");
      final String UNFULFILLED = "UNFULFILLED";
      requestEntity.setProperty("timestamp", timestamp);
      requestEntity.setProperty("book", bookEntity.getKey());
      requestEntity.setProperty("returnDate", returnDate);
      requestEntity.setProperty("status", UNFULFILLED);
      requestEntity.setProperty("requester", userEntity.getKey());

      datastore.put(requestEntity);
    }

    response.sendRedirect("/");

  }

  private Boolean isStringValid(String value){
    return value != null && !value.trim().isEmpty();
  }
}
