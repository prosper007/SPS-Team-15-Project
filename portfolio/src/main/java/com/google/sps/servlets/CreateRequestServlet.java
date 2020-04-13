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

@WebServlet("/make-request")
public class CreateRequestServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    RequestDispatcher view = request.getRequestDispatcher("/make-request.html");
    view.forward(request, response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long timestamp = System.currentTimeMillis();
    String title = request.getParameter("title");
    String author = request.getParameter("author");
    String isbn = request.getParameter("isbn");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = request.getParameter("return-date");
    Date returnDate = null;
    try {  
      returnDate = sdf.parse(dateString);
    } catch (ParseException e) {
      // e.printStackTrace();
    }
    
    if(isStringValid(title)) {      
      Entity bookEntity = new Entity("Book");
      bookEntity.setProperty("timestamp", timestamp);
      bookEntity.setProperty("title", title);
      bookEntity.setProperty("author", author);
      bookEntity.setProperty("isbn", isbn);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      datastore.put(bookEntity);

      Entity requestEntity = new Entity("Request");
      final String UNFULFILLED = "UNFULFILLED";
      requestEntity.setProperty("timestamp", timestamp);
      requestEntity.setProperty("book", bookEntity.getKey());
      requestEntity.setProperty("returnDate", returnDate);
      requestEntity.setProperty("status", UNFULFILLED);

      datastore.put(requestEntity);
    }

    response.sendRedirect("/");

  }

  private Boolean isStringValid(String value){
    return value != null && !value.trim().isEmpty();
  }
}
