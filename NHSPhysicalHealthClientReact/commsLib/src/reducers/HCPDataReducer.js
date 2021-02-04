import { UPDATE_HCP_PROFILE } from '../actions/types';
import { UPDATE_HCP_SEARCH } from '../actions/types';
import { HCP_DATA_ERROR } from '../actions/types';
import { HCP_DATA_IS_LOADING } from '../actions/types';
import { HCP_DATA_SUCCESS_CLEAR } from '../actions/types';
import { LOGOUT } from '../actions/types';

const initialState = {
    hcpProfile: null,
    hcpSearchResult: null,
    hasError: false,
    isLoading: false,
    errorMsg: null,
    profileUpdateSuccess: false
};

const hcpDataReducer = (state = initialState, action) => {
    const { type, payload } = action;

    switch (type) {
        case UPDATE_HCP_PROFILE:
            console.log('hcpprofile', action);

            return Object.assign({}, state, {
                hcpProfile: action.payload,
                hasError: false,
                profileUpdateSuccess: action.success
            });
        case UPDATE_HCP_SEARCH:
            console.log('hcpprofile', action);

            return Object.assign({}, state, {
                hcpSearchResult: action.payload,
                hasError: false
            });
        case HCP_DATA_ERROR:
            console.log('haserror', action);

            return Object.assign({}, state, {
                hasError: action.hasError,
                errorMsg: action.errorMsg,
                hcpSearchResult: null
            });
        case HCP_DATA_IS_LOADING:
            console.log('isloading', action);

            return Object.assign({}, state, {
                isLoading: action.isLoading,
            });
        case HCP_DATA_SUCCESS_CLEAR:{
            console.log('isloading', action);

            return Object.assign({}, state, {
                profileUpdateSuccess: false,
            });
        }
        case LOGOUT:
            return initialState;
        default:
            return state
    }
}

export default hcpDataReducer;