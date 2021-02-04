import { CHECK_PERMISSION } from './types';

const checkPermission = () => {
    console.log('Check Permission');
   
   return (dispatch, getState) => {
       var loginState = getState().comms.login;
       var token = loginState.accessToken;

       console.log(token);

       var serverConfig = getState().comms.serverConfig;
       fetch(serverConfig.serverUrl + "/user/permission", {
           method: 'GET',
           headers: {
               'Authorization': "Bearer " + token,
               'Accept': 'application/json'
           },
       })
           .then(response => response.json())
           .then(res => {
               console.log(res);
               
           })
           .catch((e) => {
               console.log("Caught error");
               console.warn(e);
           });
   }
};

export default {
    checkPermission
}