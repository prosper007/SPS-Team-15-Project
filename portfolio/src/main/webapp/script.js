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

function isStringEmpty(string){
  return string == null || string.trim().length == 0;
}

function createRequestElement(bookRequest) {
  
  const titleElement = document.createElement('div');
  titleElement.classList.add('title-container');
  titleElement.innerText = `${bookRequest.book.title}`;

  const authorElement = document.createElement('div');
  authorElement.classList.add('author-container');
  const hasAuthor = !isStringEmpty(bookRequest.book.author);
  authorElement.innerText = `${hasAuthor ? `by ${bookRequest.book.author}` : ''}`;

  const bookElement = document.createElement('div');
  bookElement.classList.add('book-element');
  bookElement.appendChild(titleElement);
  bookElement.appendChild(authorElement);

  const viewRequestText = document.createElement('div');
  viewRequestText.classList.add('view-request-er-link')
  viewRequestText.innerText = `View request by ${bookRequest.requester.userName}`;

  const container = document.createElement('div');
  container.classList.add('book-request');
  container.appendChild(bookElement);
  container.appendChild(viewRequestText);


  const requestElement = document.createElement('a');
  requestElement.href = `/view-request/${bookRequest.bookRequestKey}`;
  requestElement.classList.add('request-link');
  requestElement.appendChild(container);
  //return requestElement;

  const requestDeleteButtonElement = document.createElement('BUTTON');
  requestDeleteButtonElement.innerHTML = "Delete Request";

  const requestDeleteElement = document.createElement('a');
  requestDeleteElement.href = `/delete-request/${bookRequest.bookRequestKey}`;
  requestDeleteElement.classList.add('request-link');
  requestDeleteElement.appendChild(requestDeleteButtonElement)

  const requestContainer = document.createElement('div');
  requestContainer.appendChild(requestElement);
  requestContainer.appendChild(requestDeleteElement);

  return requestContainer;
}

async function getLoginStatus() {
  const response = await fetch('/login-status');
  const authInfo = await response.json();

  const loginLink = document.getElementById('login-link');
  if(!authInfo.isUserLoggedIn){
    loginLink.href = authInfo.loginUrl;
    return;
  }

  const makeRequestLink = document.getElementById('make-request-link');
  makeRequestLink.href = '/make-request';
  const logoutLink = document.getElementById('logout-link');
  logoutLink.href = authInfo.logoutUrl;
  const loggedInContainer = document.getElementById('logged-in-container');
  loggedInContainer.style.display = 'flex';
  loginLink.style.display = 'none';
}

async function getRequests(){
  const response = await fetch('/requests');
  const bookRequests = await response.json();
  const contentElement = document.getElementById("book-requests");
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
  
  const authResponse = await fetch('/login-status');
  const authInfo = await authResponse.json();
  const requesterEmail = bookRequest.requester.email;
  const hasAuthor = !isStringEmpty(bookRequest.book.author);
  const headerElement = document.getElementById('header');
  if(authInfo.isUserLoggedIn){
    const currentUserEmail = authInfo.currentUser.email;
    const isUserRequestOwner = requesterEmail.localeCompare(currentUserEmail) == 0;
    if(isUserRequestOwner) {
      headerElement.innerHTML = `Your request for <em>${bookRequest.book.title}</em> ${hasAuthor ? `by ${bookRequest.book.author}` : ''}`
    } else {
      headerElement.innerHTML = `Do you have <em>${bookRequest.book.title}</em> ${hasAuthor ? `by ${bookRequest.book.author}` : ''}?`
    }
  
  } else {
    headerElement.innerText = `Do you have ${bookRequest.book.title} ${hasAuthor ? `by ${bookRequest.book.author}` : ''}?`
  }

  const userNameElement = document.getElementById('user-name-display');
  userNameElement.innerText = `${bookRequest.requester.userName}`;

  const titleElement = document.getElementById('title-display');
  titleElement.innerText = `${bookRequest.book.title}`;
  
  
  if(hasAuthor) {
    const authorDisplay = document.getElementById('author-display');
    authorDisplay.innerText = `${bookRequest.book.author}`;
    const authorContainer = document.getElementById('author-container');
    authorContainer.classList.remove('hide');
  }

  const hasIsbn = !isStringEmpty(bookRequest.book.isbn);
  if(hasIsbn) {
    const isbnDisplay = document.getElementById('isbn-display');
    isbnDisplay.innerText = `${bookRequest.book.isbn}`;
    const authorContainer = document.getElementById('isbn-container');
    authorContainer.classList.remove('hide');
  }

  const dateElement = document.getElementById('date-display');
  dateElement.innerText = `${bookRequest.returnDate.substring(0, 12)}`;

  const emailElement = document.getElementById('email');
  emailElement.innerText = `${requesterEmail}`;
}

async function populateForm(){
  const response = await fetch('/login-status');
  const authInfo = await response.json();
  const emailContainer = document.getElementById("email");
  // should never be true as server redirects all logged out users to '/'
  if(!authInfo.isUserLoggedIn) {
    emailContainer.innerText = "INV@ALID.com";
    return;
  }
  emailContainer.innerText = authInfo.currentUser.email;
  const userName = authInfo.currentUser.userName;
  if(!isStringEmpty(userName)){
    userNameInput = document.getElementById('user-name-input');
    userNameInput.value = userName;
  }
}
