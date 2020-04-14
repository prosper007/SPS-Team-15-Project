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

  const container = document.createElement('div');
  container.classList.add('book-request');
  container.appendChild(titleElement);
  container.appendChild(authorElement);
  container.appendChild(isbnElement);
  container.appendChild(returnDateElement);
  container.appendChild(statusElement);

  const requestElement = document.createElement('a');
  requestElement.href = `/view-request/${bookRequest.bookRequestKey}`;
  requestElement.appendChild(container);
  return requestElement;
}

async function getRequests(){
  const response = await fetch('/requests');
  const bookRequests = await response.json();
  const contentElement = document.getElementById("content");
  bookRequests.forEach((bookRequest) => {
    contentElement.appendChild(createRequestElement(bookRequest));
  });
}

async function getRequest(){
  const path = window.location.pathname.split('/');
  //request key should be last part of url
  requestKey = path[path.length-1];
  const response = await fetch(`/request/${requestKey}`);
  if(response.status != 200){
    window.location.replace("/request-not-found");
    return;
  }
  const bookRequest = await response.json();
  const contentElement = document.getElementById("content");
  contentElement.appendChild(createRequestElement(bookRequest));
}
