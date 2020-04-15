package com.google.sps.data;

import com.google.sps.data.Book;
import java.util.Date;
import com.google.appengine.api.datastore.Key;
import com.google.sps.data.User;

public final class BookRequest {

  private final Book book;
  private final Date returnDate;
  private final String status;
  private final String bookRequestKey;
  private final User requester;

  public BookRequest(Book book, Date returnDate, String status, String bookRequestKey, User requester) {
    this.book = book;
    this.returnDate = returnDate;
    this.status = status;
    this.bookRequestKey = bookRequestKey;
    this.requester = requester;
  }
}