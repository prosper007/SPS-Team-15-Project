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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import com.google.gson.Gson;
import com.google.sps.data.Book;
import com.google.sps.data.BookRequest;
import java.util.Date;

@WebServlet("/request/*")
public class RequestServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    // path name will be of valid length for substring operation because we check for malformed url in ViewRequestServlet
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

    Key bookKey = (Key) requestEntity.getProperty("book");
    Entity bookEntity = null;
    try{
      bookEntity = datastore.get(bookKey);
    } catch(EntityNotFoundException e){
      e.printStackTrace();
      // if book is missing, something went wrong internally
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    String bookTitle = (String) bookEntity.getProperty("title");
    String bookAuthor = (String) bookEntity.getProperty("author");
    String bookIsbn = (String) bookEntity.getProperty("isbn");
    Book book = new Book(bookTitle, bookAuthor, bookIsbn);

    Date returnDate = (Date) requestEntity.getProperty("returnDate") ;
    String status = (String) requestEntity.getProperty("status");

    String bookRequestKey = KeyFactory.keyToString(requestEntity.getKey());

    BookRequest bookRequest = new BookRequest(book, returnDate, status, bookRequestKey);

    String json = convertToJson(bookRequest);

    response.setContentType("application/json; charset=utf-8");
    response.getWriter().println(json);
  }

  private String convertToJson(BookRequest bookRequest){
    Gson gson = new Gson();
    String json = gson.toJson(bookRequest);
    return json;
  }
}