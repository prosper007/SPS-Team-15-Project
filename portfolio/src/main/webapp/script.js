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
  return requestElement;
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

  const bookAuthor = bookRequest.book.author;
  const hasAuthor = !isStringEmpty(bookAuthor);
  const requesterEmail = bookRequest.requester.email;
  const requesterName = bookRequest.requester.userName;
  const bookTitle = bookRequest.book.title;

  document.title = `${bookTitle} ${hasAuthor ? ` by ${bookAuthor}` : ''}`
  const headerElement = document.getElementById('header');
  const ctaElement = document.getElementById('cta-link');
  const emailLink = `mailto:${requesterEmail}?Subject=Hey%20${requesterName}!%20I%20can%20lend%20you%20"${bookTitle}"`;

  if(authInfo.isUserLoggedIn){
    const currentUserEmail = authInfo.currentUser.email;
    const isUserRequestOwner = requesterEmail.localeCompare(currentUserEmail) == 0;
    if(isUserRequestOwner) {
      headerElement.innerHTML = `Your Request for <em>${bookTitle}</em> ${hasAuthor ? `by ${bookAuthor}` : ''}`;
      
      const statusElement = document.getElementById('request-status');
      const isUnfulfilled = bookRequest.status.localeCompare("UNFULFILLED") === 0
      statusElement.innerText = `${isUnfulfilled ? 'No' : 'Yes'}`;
      const statusTitle = document.getElementById('book-title');
      statusTitle.innerHTML = `<em>${bookTitle}</em>`;
      const statusContainer = document.getElementById('request-status-container');
      statusContainer.classList.remove('hide');

      ctaElement.innerText = 'Update Request';
      ctaElement.href = `/edit-request/${requestKey}`;

      const deleteElement = document.getElementById('delete-link');
      deleteElement.href = `/delete-request/${bookRequest.bookRequestKey}`;
      deleteElement.classList.remove('hide');

      const actionLinksElement = document.getElementById('action-links');
      actionLinksElement.style.justifyContent = 'space-between';
    } else {
      headerElement.innerHTML = `Do you have <em>${bookTitle}</em> ${hasAuthor ? `by ${bookAuthor}` : ''}?`;   
      ctaElement.innerText = `Email ${requesterName}`;
      ctaElement.href = emailLink;
    }
  
  } else {
    headerElement.innerText = `Do you have ${bookRequest.book.title} ${hasAuthor ? `by ${bookAuthor}` : ''}?`;
    ctaElement.innerText = `Email ${requesterName}`
    ctaElement.href = emailLink;
    
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
  // assuming date is in format "yyyy-MM-dd"
  const returnDateParts = bookRequest.returnDate.split('-');
  const returnDate = new Date(returnDateParts[0], returnDateParts[1], returnDateParts[2]);
  dateElement.innerText = `${returnDate.toDateString()}`;

  const emailElement = document.getElementById('email');
  emailElement.innerText = `${requesterEmail}`;
  emailElement.href = emailLink
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

async function populateEditForm(){
  const path = window.location.pathname.split('/');
  //request key should be last part of url
  requestKey = path[path.length-1];
  const response = await fetch(`/request/${requestKey}`);
  if(response.status != 200){
    window.location.replace("/request-not-found");
    return;
  }
  const bookRequest = await response.json();

  const userNameInput = document.getElementById('user-name-input');
  userNameInput.value = bookRequest.requester.userName;
  
  const titleInput = document.getElementById('title-input');
  titleInput.value = bookRequest.book.title;

  const authorInput = document.getElementById('author-input');
  authorInput.value = bookRequest.book.author;

  const isbnInput = document.getElementById('isbn-input');
  isbnInput.value = bookRequest.book.isbn;

  const dateInput = document.getElementById('date-input');
  dateInput.value = bookRequest.returnDate;

  const emailDisplay = document.getElementById('email');
  emailDisplay.innerText = bookRequest.requester.email;

  const yesButton = document.getElementById('yes-button');
  yesButton.checked = bookRequest.status === "FULFILLED";

  const noButton = document.getElementById('no-button');
  noButton.checked = bookRequest.status === "UNFULFILLED";

  const formAction = document.getElementById('request-form');
  formAction.action = `/edit-request/${requestKey}`
}

function constructErrorMessage(isUserNameInvalid, isTitleInvalid, isDateInvalid, numErrors) {
  let errorMessage = 'Please enter ';
  let tracker = 0;
  if(isUserNameInvalid){
    errorMessage += `your <span class="display-error">name</span>${numErrors>1 ? ', ' : ' '}`;
    tracker++;
    if(tracker == numErrors -1){
      errorMessage += 'and ';
    }
  }
  if(isTitleInvalid){
    errorMessage += `the <span class="display-error">book title</span>${numErrors>1 ? ', ' : ' '}`;
    tracker++;
    if(tracker == numErrors -1){
      errorMessage += 'and ';
    }
  }
  
  if(isDateInvalid){
    errorMessage += 'your intended <span class="display-error">return date</span>';
  }
  return errorMessage;
}

function isFormValid() {
  const userNameInput = document.getElementById('user-name-input');
  const userName = userNameInput.value;
  
  const titleInput = document.getElementById('title-input');
  const bookTitle = titleInput.value;

  const dateInput = document.getElementById('date-input');
  const returnDate = dateInput.value;
  console.log(returnDate);

  const isUserNameInvalid = isStringEmpty(userName);
  const isTitleInvalid = isStringEmpty(bookTitle);
  const isDateInvalid= isStringEmpty(returnDate);

  const errorMessageContainer = document.getElementById("required-fields-text");
  
  if(isUserNameInvalid || isTitleInvalid || isDateInvalid) {
    let numErrors = 0;

    if(isUserNameInvalid){
      userNameInput.classList.remove('required');
      userNameInput.classList.add('error');
      const nameLabel = document.getElementById('name-label');
      nameLabel.classList.add('display-error');
      numErrors++;
    }

    if(isTitleInvalid){
      titleInput.classList.remove('required');
      titleInput.classList.add('error');
      const titleLabel = document.getElementById('title-label');
      titleLabel.classList.add('display-error');
      numErrors++;
    }

    if(isDateInvalid){
      dateInput.classList.remove('required');
      dateInput.classList.add('error');
      const dateLabel = document.getElementById('date-label');
      dateLabel.classList.add('display-error');
      numErrors++;
    }

    const errorMessage = constructErrorMessage(isUserNameInvalid, isTitleInvalid, isDateInvalid, numErrors)
    errorMessageContainer.innerText = '';
    errorMessageContainer.innerHTML = errorMessage;
    return false;
  }
  const returnDateObject = new Date(returnDate);
  let now = new Date();
  if(returnDateObject < now){
    dateInput.classList.remove('required');
    dateInput.classList.add('error');
    const dateLabel = document.getElementById('date-label');
    dateLabel.classList.add('display-error');
    errorMessageContainer.innerHTML = 'Your intended <span class="display-error">return date cannot be in the past</span>';
    return false;
  }

  return true;
}

function submitForm(event) {
  if(isFormValid()){
  } else{
    event.preventDefault();
    return false;
  }
}
