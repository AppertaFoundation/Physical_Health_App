import { UPDATE_SERVER_CONFIG } from './types';
import { Base64 } from 'js-base64';

const updateServerConfig = (serverUrl, authUsername, authSecret) => {
  var hash = Base64.encode(authUsername + ":" + authSecret);
  return {
    type: UPDATE_SERVER_CONFIG,
    payload: {
      serverUrl: serverUrl,
      hash: hash,
      client_id: authUsername,
      client_secret: authSecret
    }
  }
}

export default {
  updateServerConfig 
}