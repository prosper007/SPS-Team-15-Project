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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import com.google.sps.data.Book;
import java.util.Date;
import com.google.sps.data.BookRequest;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.KeyFactory;


@WebServlet("/requests")
public class RequestsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Request").addSort("timestamp", SortDirection.ASCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    
    List<BookRequest> bookRequests = new ArrayList<>();
    for(Entity entity : results.asIterable()){
      Key bookKey = (Key) entity.getProperty("book");
      Entity bookEntity = null;
      try{
        bookEntity = datastore.get(bookKey);
      } catch(EntityNotFoundException e){
        e.printStackTrace();
        continue;
      }
      String bookTitle = (String) bookEntity.getProperty("title");
      String bookAuthor = (String) bookEntity.getProperty("author");
      String bookIsbn = (String) bookEntity.getProperty("isbn");
      Book book = new Book(bookTitle, bookAuthor, bookIsbn);

      Date returnDate = (Date) entity.getProperty("returnDate") ;
      String status = (String) entity.getProperty("status");

      String bookRequestKey = KeyFactory.keyToString(entity.getKey());

      BookRequest bookRequest = new BookRequest(book, returnDate, status, bookRequestKey);

      bookRequests.add(bookRequest);
    }

    String json = convertToJson(bookRequests);

    response.setContentType("application/json; charset=utf-8");
    response.getWriter().println(json);
  }

  private String convertToJson(List<BookRequest> bookRequests){
    Gson gson = new Gson();
    String json = gson.toJson(bookRequests);
    return json;
  }
}