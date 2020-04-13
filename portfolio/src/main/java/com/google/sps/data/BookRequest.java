package com.google.sps.data;

import com.google.sps.data.Book;
import java.util.Date;

public final class BookRequest {

  private final Book book;
  private final Date returnDate;
  private final String status;

  public BookRequest(Book book, Date returnDate, String status) {
    this.book = book;
    this.returnDate = returnDate;
    this.status = status;
  }
}