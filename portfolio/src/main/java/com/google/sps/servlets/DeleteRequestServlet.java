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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Book;
import com.google.sps.data.BookRequest;
import java.util.Date;
import com.google.sps.data.User;

@WebServlet("/delete-request/*")
public class DeleteRequestServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    UserService userService = UserServiceFactory.getUserService();
    if(!userService.isUserLoggedIn()){
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.sendRedirect("/");
      return;
    } 

    //First find the specific request. If found, then check if the correct user is 
    //trying to delete the request.
    String requestKeyString = request.getPathInfo().substring(1);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity requestEntity;
    Key requestKey = null;
    try{
      requestKey = KeyFactory.stringToKey(requestKeyString);
      requestEntity = datastore.get(requestKey);
    } catch(IllegalArgumentException | EntityNotFoundException e){
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    Key userKey = (Key) requestEntity.getProperty("requester");
    Entity userEntity = null;
    
    try{ 
      userEntity = datastore.get(userKey);
    } catch(EntityNotFoundException e){
      e.printStackTrace();
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    String email = (String) userEntity.getProperty("email");

    if (email.equals(userService.getCurrentUser().getEmail())){
        datastore.delete(requestKey);
    }

    response.sendRedirect("/index.html");


  }
}