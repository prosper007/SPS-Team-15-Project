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

async function getRequests(){
  const response = await fetch('/requests');
  const bookRequests = await response.json();
  const contentElement = document.getElementById("content");
  bookRequests.forEach((bookRequest) => {
    contentElement.appendChild(createRequestElement(bookRequest));
  });
}

function createRequestElement(bookRequest) {
  
  const titleElement = document.createElement('div');
  titleElement.innerText = "Title: " + bookRequest.book.title;

  const authorElement = document.createElement('div');
  authorElement.innerText = "Author: " + bookRequest.book.author;

  const isbnElement = document.createElement('div');
  isbnElement.innerText = "ISBN: " + bookRequest.book.isbn;
  
  const returnDateElement = document.createElement('div');
  returnDateElement.innerText = "Return date: " + bookRequest.returnDate;

  const statusElement = document.createElement('div');
  statusElement.innerText = "Status: " + bookRequest.status;

  const requestElement = document.createElement('div');
  requestElement.classList.add('book-request');
  requestElement.appendChild(titleElement);
  requestElement.appendChild(authorElement);
  requestElement.appendChild(isbnElement);
  requestElement.appendChild(returnDateElement);
  requestElement.appendChild(statusElement);

  return requestElement;
}
