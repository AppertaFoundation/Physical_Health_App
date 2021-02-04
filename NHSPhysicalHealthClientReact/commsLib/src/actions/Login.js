import { IS_LOGGED } from './types';
import { LOGIN_HAS_ERROR } from './types';
import { LOGIN_IS_LOADING } from './types';
import { LOGIN } from './types';
import { LOGOUT } from './types';
import { CLIENT_CREDS_LOADING, CLIENT_CREDS_ERROR, CLIENT_CREDS } from './types';

let sessionTimeout = null;

const SESSION_TIMEOUT_THRESHOLD = 300; // Will refresh the access token 5 minutes before it expires

const isLogged = (bool) => {
    return {
        type: IS_LOGGED,
        isLogged: bool
    }
};

const loginHasError = (bool, errorMsg) => {
    return {
        type: LOGIN_HAS_ERROR,
        hasError: bool,
        errorMsg: errorMsg
    }
};

const loginIsLoading = (bool) => {
    return {
        type: LOGIN_IS_LOADING,
        isLoading: bool
    }
};

const updateTokens = (payload) => {
    return {
        type: LOGIN,
        payload: payload
    }
};

const clientCredsHasError = (bool, errorMsg) => {
    return {
        type: CLIENT_CREDS_ERROR,
        hasError: bool,
        errorMsg: errorMsg
    }
}

const clientCredsIsLoading = (bool) => {
    return {
        type: CLIENT_CREDS_LOADING,
        isLoading: bool
    }
}

const updateClientCreds = (payload) => {
    return {
        type: CLIENT_CREDS,
        payload: payload
    }
}

const login = (username, password) => {
     console.log('user', username);
     console.log('pass', password);
    return (dispatch, getState) => {
        dispatch(loginIsLoading(true));

        if(!username || !password){
            // Missing fields, report error
            dispatch(loginHasError(true));
            dispatch(loginIsLoading(false));

            return;
        }


        var serverConfig = getState().comms.serverConfig;

        const hash = serverConfig.hash;
        fetch(serverConfig.serverUrl + "/oauth/token", {
            method: 'POST',
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                'Authorization': "Basic "+ hash
            },
            body: "grant_type=password&username="+username+"&password="+password,
        })
            .then(response => response.json())
            .then(res => {
                // Cancel loading indicator
                dispatch(loginIsLoading(false));

                console.log(res);
                if(res.access_token){
                    dispatch(loginHasError(false));

                    var payload = {
                        isLogged: true,
                        accessToken: res.access_token,
                        refreshToken: res.refresh_token,
                        username: username
                    }

                    dispatch(updateTokens(payload));
                    setSessionTimeout(res.expires_in);
                } else {
                    dispatch(loginHasError(true, res));
                }
            })
            .catch((e) => {
                console.log("Caught error");
                console.warn(e);
                dispatch(loginIsLoading(false));
                dispatch(loginHasError(true));
            });
    }
};

function getClientCreds (client_id, client_secret) {
   return (dispatch, getState) => {
       dispatch(clientCredsIsLoading(true));

       if(!client_id || !client_secret){
           // Missing fields, report error
           dispatch(clientCredsHasError(true));
           dispatch(clientCredsIsLoading(false));
           return;
       }

       var serverConfig = getState().comms.serverConfig;
       const hash = serverConfig.hash;
       return fetch(serverConfig.serverUrl + "/oauth/token", {
           method: 'POST',
           headers: {
               "Content-Type": "application/x-www-form-urlencoded",
               'Authorization': "Basic "+ hash
           },
           body: "grant_type=client_credentials&client_id="+client_id+"&client_secret="+client_secret,
       })
           .then(response => response.json())
           .then(res => {
               // Cancel loading indicator
               dispatch(clientCredsIsLoading(false));

               console.log(res);
               if(res.access_token){
                   dispatch(clientCredsHasError(false));

                   var payload = {
                       isLogged: true,
                       clientAccessToken: res.access_token,
                       clientRefreshToken: res.refresh_token
                   }

                   dispatch(updateClientCreds(payload));
               } else {
                   dispatch(clientCredsHasError(true, res));
               }
           })
           .catch((e) => {
               console.log("Caught error");
               console.warn(e);
               dispatch(clientCredsIsLoading(false));
               dispatch(clientCredsHasError(true));
           });
   }
};

const register = (username, email, password, role) => {
    console.log('user', email);
    console.log('pass', password);
    return (dispatch, getState) => {
        // get client creds
        var serverConfig = getState().comms.serverConfig;
        return dispatch(getClientCreds(serverConfig.client_id, serverConfig.client_secret))
        .then(() => {
            // Do register
            dispatch(loginIsLoading(true));

            if(!email || !password){
                // Missing fields, report error
                dispatch(loginHasError(true));
                dispatch(loginIsLoading(false));
                return;
            }
     
            var loginState = getState().comms.login;
            var token = loginState.clientAccessToken;
            if (!token){
                // still no client creds
                dispatch(loginHasError(true, "Client not allowed to connect"));
                dispatch(loginIsLoading(false));
                return;
            }
     
            var serverConfig = getState().comms.serverConfig;
            fetch(serverConfig.serverUrl + "/api/user/register", {
                method: 'POST',
                headers: {
                    "Content-Type": "application/json",
                    'Authorization': "Bearer "+ token
                },
                body: JSON.stringify({
                 username: username,
                 password: password,
                 role: role,
                 emailAddress: email
               }),
            })  
                .then(res => {
                    console.log(res);
                    if(res.status === 201){
                        // Registration success
                        console.log("success");
     
                        // Login into account
                        dispatch(login(username, password));
                    } else {
                     res.json().then( (response) => {
                         console.log("Error response" + JSON.stringify(response));
                         // Report error
                     dispatch(loginIsLoading(false));
                     dispatch(loginHasError(true, response));
                     });
                    }
                })
                .catch((e) => {
                    console.log("Caught error");
                    console.warn(e);
                    dispatch(loginIsLoading(false));
                    dispatch(loginHasError(true));
                });
        })
    }
};

const logout = () => {
    clearTimeout(sessionTimeout);
    return {
        type: LOGOUT
    }
};

const refreshToken = () => {
    console.log('Refresh Token');
   
   return (dispatch, getState) => {
       var loginState = getState().comms.login;
       var token = loginState.refreshToken;
       var username = loginState.username;
       console.log('Refresh Token');

       var serverConfig = getState().comms.serverConfig;
       const hash = serverConfig.hash;
       fetch(serverConfig.serverUrl + "/oauth/token", {
           method: 'POST',
           headers: {
               "Content-Type": "application/x-www-form-urlencoded",
               'Authorization': "Basic "+ hash
           },
           body: "grant_type=refresh_token&refresh_token="+token,
       })
           .then(response => response.json())
           .then(res => {

               console.log(res);
               if(res.access_token){
                   var payload = {
                       isLogged: true,
                       accessToken: res.access_token,
                       refreshToken: res.refresh_token,
                       username: username
                   }

                   dispatch(updateTokens(payload));
                   setSessionTimeout(res.expires_in);
               } else {
                 dispatch(isLogged(false));  
                 dispatch(loginHasError(true));
               }
           })
           .catch((e) => {
               console.log("Caught error");
               console.warn(e);
               dispatch(isLogged(false)); 
               dispatch(loginHasError(true));
           });
   }
};

const refreshTokenHandler = (dispatch, getState, successCallback) =>{
    var loginState = getState().comms.login;
       var token = loginState.refreshToken;
       var username = loginState.username;
       console.log('Refresh Token');

       var serverConfig = getState().comms.serverConfig;
       const hash = serverConfig.hash;
       fetch(serverConfig.serverUrl + "/oauth/token", {
           method: 'POST',
           headers: {
               "Content-Type": "application/x-www-form-urlencoded",
               'Authorization': "Basic "+ hash
           },
           body: "grant_type=refresh_token&refresh_token="+token,
       })
           .then(response => response.json())
           .then(res => {

               console.log(res);
               if(res.access_token){
                   var payload = {
                       isLogged: true,
                       accessToken: res.access_token,
                       refreshToken: res.refresh_token,
                       username: username
                   }

                   dispatch(updateTokens(payload));
                   setSessionTimeout(res.expires_in);
                   dispatch(successCallback);
               } else {
                 dispatch(isLogged(false));  
                 dispatch(loginHasError(true));
               }
           })
           .catch((e) => {
               console.log("Caught error");
               console.warn(e);
               dispatch(isLogged(false)); 
               dispatch(loginHasError(true));
           });
}

const setSessionTimeout = (duration) => {
	clearTimeout(sessionTimeout);
	sessionTimeout = setTimeout(
		refreshToken, // eslint-disable-line no-use-before-define
		(duration - SESSION_TIMEOUT_THRESHOLD) * 1000
	);
};

export default {
    isLogged,
    loginHasError,
    loginIsLoading,
    login,
    logout,
    register,
    refreshToken,
    refreshTokenHandler,
    getClientCreds
}
