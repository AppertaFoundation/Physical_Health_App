import { UPDATE_SERVER_CONFIG } from '../actions/types';

const initialState = {
    serverUrl: '',
    hash: '',
    client_id: '',
    client_secret: ''
};

const serverConfigReducer = (state = initialState, action) => {
    const { type, payload } = action;

    switch (type){
        case UPDATE_SERVER_CONFIG:
            console.log('Update server config ', action);
            return Object.assign({}, state, {
                serverUrl: payload.serverUrl,
                hash: payload.hash,
                client_id: payload.client_id,
                client_secret: payload.client_secret
            });
        default:
            return state
    }
}

export default serverConfigReducer;