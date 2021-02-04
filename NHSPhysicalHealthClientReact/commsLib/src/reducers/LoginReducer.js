import { IS_LOGGED } from '../actions/types';
import { LOGIN_HAS_ERROR } from '../actions/types';
import { LOGIN_IS_LOADING } from '../actions/types';
import { LOGIN } from '../actions/types';
import { LOGOUT } from '../actions/types';
import { CLIENT_CREDS_LOADING, CLIENT_CREDS_ERROR, CLIENT_CREDS } from '../actions/types';


const initialState = {
    isLogged: false,
    hasError : false,
    isLoading: false,
    errorMsg: null,

    accessToken: null,
    refreshToken: null,
    tokenRefreshed: false,
    username: null,

    clientAccessToken: null,
    clientRefreshToken: null
};

const loginReducer = (state = initialState, action) => {
    const { type, payload } = action;

    switch (type){
        case IS_LOGGED:
            console.log('islogged', action);

            return Object.assign({}, state, {
                isLogged: action.isLogged,
            });
        case LOGIN_HAS_ERROR:
            console.log('haserror', action);

            return Object.assign({}, state, {
                hasError: action.hasError,
                errorMessage: action.errorMsg
            });
        case LOGIN_IS_LOADING:
            console.log('isloading', action);

            return Object.assign({}, state, {
                isLoading: action.isLoading,
            });
        case LOGIN:
            console.log('login', action);
            return Object.assign({}, state, {
                isLogged: payload.isLogged,
                accessToken: payload.accessToken,
                refreshToken: payload.refreshToken,
                tokenRefreshed: true,
                username: payload.username
            });
        case LOGOUT:
            console.log('logout', action);
            return Object.assign({}, state, {
                isLogged: false,
                accessToken: null,
                refreshToken: null,
                tokenRefreshed: false
            });
        case CLIENT_CREDS_LOADING:
            console.log('fetchClientToken', action);
            return Object.assign({}, state, {
                isLoading: action.isLoading
            });
        case CLIENT_CREDS_ERROR:
            console.log('fetchClientToken', action);
            return Object.assign({}, state, {
                hasError: action.hasError,
                errorMessage: action.errorMsg
            });
        case CLIENT_CREDS:
            console.log('fetchClientToken', action);
            return Object.assign({}, state, {
                hasClientCreds: payload.isLogged,
                clientAccessToken: payload.clientAccessToken,
                clientRefreshToken: payload.clientRefreshToken
            });
        default:
            return state
    }
}

export default loginReducer;