import LoginActions from "./Login";

const getRequest = (path, successHandler, failureHandler, loadingHandler, successData) => {
  console.log("Get request:" + path);

  return (dispatch, getState) => {
    // Show loading indicator
    dispatch(loadingHandler(true));

    // Get authentication token and serverUrl
    var loginState = getState().comms.login;
    var token = loginState.accessToken;
    var serverConfig = getState().comms.serverConfig;

    // Timeout
    var timeoutPromise = new Promise(function (resolve, reject) {
      setTimeout(resolve, 30000, {
        status: 'error',
        code: 666,
        data: 'TIMEOUT',
      });
    });

    // Make GET request
    Promise.race([
      timeoutPromise,
      fetch(serverConfig.serverUrl + path, {
        method: "GET",
        headers: {
          Authorization: "Bearer " + token
        }
      })
    ])
      .then(res => {
        // Check if the request was successful
        console.log(res);
        if (res.ok) {
          // Convert body into JSON and pass to
          // success handler
          res
            .json()
            .then(response => {
              console.log("JSON reponse" + JSON.stringify(response));
              dispatch(loadingHandler(false));
              dispatch(successHandler(response, successData));
            })
            .catch(e => {
              // Error parsing json
              console.log("Caught error");
              console.log(e);
              dispatch(loadingHandler(false));
              dispatch(successHandler(null));
            });
        } else {
          if (res.status == 401) {
            // Access token expired, attempt to refresh token
            LoginActions.refreshTokenHandler(
              dispatch,
              getState,
              getRequest(path, successHandler, failureHandler, loadingHandler, successData)
            );
          } else {
            res
              .json()
              .then(response => {
                console.log("Error response" + JSON.stringify(response));
                // Report error
                dispatch(loadingHandler(false));
                dispatch(failureHandler(true, response));
              })
              .catch(e => {
                // Error parsing json
                console.log("Caught error");
                console.log(e);
                dispatch(loadingHandler(false));
                dispatch(failureHandler(true));
              });
          }
        }
      })
      .catch(e => {
        // Report error
        console.log("Caught error");
        console.log(e);
        dispatch(loadingHandler(false));
        dispatch(failureHandler(true));
      });
  };
};

const deleteRequest = (
  path,
  successHandler,
  failureHandler,
  loadingHandler
) => {
  console.log("Get request:" + path);

  return (dispatch, getState) => {
    // Show loading indicator
    dispatch(loadingHandler(true));

    // Get authentication token and serverUrl
    var loginState = getState().comms.login;
    var token = loginState.accessToken;
    var serverConfig = getState().comms.serverConfig;

    // Make GET request
    fetch(serverConfig.serverUrl + path, {
      method: "DELETE",
      headers: {
        Authorization: "Bearer " + token
      }
    })
      .then(res => {
        // Check if the request was successful
        console.log(res);
        if (res.ok) {
          // Convert body into JSON and pass to
          // success handler
          res.json().then(response => {
            console.log("JSON reponse" + JSON.stringify(response));
            dispatch(loadingHandler(false));
            dispatch(successHandler(response));
          });
        } else {
          if (res.status == 401) {
            // Access token expired, attempt to refresh token
            LoginActions.refreshTokenHandler(
              dispatch,
              getState,
              getRequest(path, successHandler, failureHandler, loadingHandler)
            );
          } else {
            res.json().then(response => {
              console.log("Error response" + JSON.stringify(response));
              // Report error
              dispatch(loadingHandler(false));
              dispatch(failureHandler(true, response));
            });
          }
        }
      })
      .catch(e => {
        // Report error
        console.log("Caught error");
        console.log(e);
        dispatch(loadingHandler(false));
        dispatch(failureHandler(true));
      });
  };
};

const postRequest = (
  path,
  body,
  successHandler,
  failureHandler,
  loadingHandler,
  successData
) => {
  console.log("POST request: " + path);
  console.log("Body: " + JSON.stringify(body));

  return (dispatch, getState) => {
    // Show loading indicator
    dispatch(loadingHandler(true));

    // Get authentication token and serverUrl
    var loginState = getState().comms.login;
    var token = loginState.accessToken;
    var serverConfig = getState().comms.serverConfig;

    // Timeout
    var timeoutPromise = new Promise(function (resolve, reject) {
      setTimeout(resolve, 30000, {
        status: 'error',
        code: 666,
        data: 'TIMEOUT',
      });
    });

    // Make POST request
    Promise.race([
      timeoutPromise,
      fetch(serverConfig.serverUrl + path, {
        method: "POST",
        headers: {
          Authorization: "Bearer " + token,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
      })
    ])
      .then(res => {
        // Check if the request was successful
        console.log(res);
        if (res.ok) {
          res
            .json()
            .then(response => {
              console.log("Server response" + JSON.stringify(response));
              // Report error
              dispatch(loadingHandler(false));
              dispatch(successHandler(response, successData));
            })
            .catch(e => {
              // Error parsing response. 
              console.log(e);
              dispatch(loadingHandler(false));
              dispatch(successHandler(null, successData));
            });
        } else {
          if (res.status == 401) {
            // Access token expired, attempt to refresh token
            LoginActions.refreshTokenHandler(
              dispatch,
              getState,
              postRequest(
                path,
                body,
                successHandler,
                failureHandler,
                loadingHandler,
                successData
              )
            );
          } else {
            // Report error
            res.json().then(response => {
              console.log("Error response" + JSON.stringify(response));
              // Report error
              dispatch(loadingHandler(false));
              dispatch(failureHandler(true, response));
            });
          }
        }
      })
      .catch(e => {
        // Report error
        console.log("Caught error");
        console.log(e);
        dispatch(loadingHandler(false));
        dispatch(failureHandler(true));
      });
  };
};

const postRequestClientToken = (
  path,
  body,
  successHandler,
  failureHandler,
  loadingHandler,
  successData
) => {
  console.log("POST request: " + path);
  console.log("Body: " + JSON.stringify(body));

  // Show loading indicator
  return (dispatch, getState) => {
    dispatch(loadingHandler(true));
    var serverConfig = getState().comms.serverConfig;
    return dispatch(
      LoginActions.getClientCreds(serverConfig.client_id, serverConfig.client_secret)
    ).then(() => {
      // Get authentication token and serverUrl
      var loginState = getState().comms.login;
      var token = loginState.clientAccessToken;

      if (!token) {
        // still no client creds
        dispatch(failureHandler(true));
        dispatch(loadingHandler(false));
        return;
      }

      var serverConfig = getState().comms.serverConfig;

      // Make POSY request
      fetch(serverConfig.serverUrl + path, {
        method: "POST",
        headers: {
          Authorization: "Bearer " + token,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
      })
        .then(res => {
          // Check if the request was successful
          console.log(res);
          if (res.ok) {
            res
              .json()
              .then(response => {
                console.log("Server response" + JSON.stringify(response));
                // Report error
                dispatch(loadingHandler(false));
                dispatch(successHandler(response, successData));
              })
              .catch(e => {
                // Error parsing response. 
                console.log(e);
                dispatch(loadingHandler(false));
                dispatch(successHandler(null, successData));
              });
          } else {
            // Report error
            res.json().then(response => {
              console.log("Error response" + JSON.stringify(response));
              // Report error
              dispatch(loadingHandler(false));
              dispatch(failureHandler(true, response));
            });
          }
        })
        .catch(e => {
          // Report error
          console.log("Caught error");
          console.log(e);
          dispatch(loadingHandler(false));
          dispatch(failureHandler(true));
        });
    });
  };
};

const putRequest = (
  path,
  body,
  successHandler,
  failureHandler,
  loadingHandler,
  successData
) => {
  console.log("POST request: " + path);
  console.log("Body: " + JSON.stringify(body));

  return (dispatch, getState) => {
    // Show loading indicator
    dispatch(loadingHandler(true));

    // Get authentication token and serverUrl
    var loginState = getState().comms.login;
    var token = loginState.accessToken;
    var serverConfig = getState().comms.serverConfig;

    // Timeout
    var timeoutPromise = new Promise(function (resolve, reject) {
      setTimeout(resolve, 30000, {
        status: 'error',
        code: 666,
        data: 'TIMEOUT',
      });
    });

    // Make PUT request
    Promise.race([
      timeoutPromise,
      fetch(serverConfig.serverUrl + path, {
        method: "PUT",
        headers: {
          Authorization: "Bearer " + token,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
      })
    ])
      .then(res => {
        // Check if the request was successful
        console.log(res);
        if (res.ok) {
          res
            .json()
            .then(response => {
              console.log("Server response" + JSON.stringify(response));
              // Report error
              dispatch(loadingHandler(false));
              dispatch(successHandler(response, successData));
            })
            .catch(e => {
              // Error parsing response. 
              console.log(e);
              dispatch(loadingHandler(false));
              dispatch(successHandler(null, successData));
            });
        } else {
          if (res.status == 401) {
            // Access token expired, attempt to refresh token
            LoginActions.refreshTokenHandler(
              dispatch,
              getState,
              postRequest(
                path,
                body,
                successHandler,
                failureHandler,
                loadingHandler,
                successData
              )
            );
          } else {
            // Report error
            res.json().then(response => {
              console.log("Error response" + JSON.stringify(response));
              // Report error
              dispatch(loadingHandler(false));
              dispatch(failureHandler(true, response));
            });
          }
        }
      })
      .catch(e => {
        // Report error
        console.log("Caught error");
        console.log(e);
        dispatch(loadingHandler(false));
        dispatch(failureHandler(true));
      });
  };
};

export default {
  getRequest,
  postRequest,
  postRequestClientToken,
  putRequest
};
