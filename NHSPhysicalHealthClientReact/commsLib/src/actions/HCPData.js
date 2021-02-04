import { UPDATE_HCP_PROFILE } from './types';
import { UPDATE_HCP_SEARCH } from './types';
import { HCP_DATA_ERROR } from './types';
import { HCP_DATA_IS_LOADING } from './types';
import { HCP_DATA_SUCCESS_CLEAR } from './types';

import ApiHelper from './ApiHelper';

const hasError = (bool, errorMsg) => {
    return {
        type: HCP_DATA_ERROR,
        hasError: bool,
        errorMsg: errorMsg
    }
};

const isLoading = (bool) => {
    return {
        type: HCP_DATA_IS_LOADING,
        isLoading: bool
    }
};

const updateHcpProfile = (payload) => {
    return {
        type: UPDATE_HCP_PROFILE,
        payload: payload
    }
};

const updateHcpSearch = (payload) => {
    return {
        type: UPDATE_HCP_SEARCH ,
        payload: payload
    }
};

const saveHcpProfileSuccess = (payload) => {
    return {
        type: UPDATE_HCP_PROFILE,
        payload: payload,
        success: true
    }
};

const clearHcpUpdateSuccess = () => {
    return {
        type: HCP_DATA_SUCCESS_CLEAR
    }
};

const getHcpProfile= () => {
    console.log('Fetching hcp profile');

    return ApiHelper.getRequest(
        "/api/user/hcp/profile",
        updateHcpProfile,
        hasError,
        isLoading
    );
};

const saveHcpProfile= (profile) => {
    console.log('Fetching hcp profile');

    return ApiHelper.postRequest(
        "/api/user/hcp/profile/",
        profile,
        saveHcpProfileSuccess,
        hasError,
        isLoading
    );
};

const searchForHCP=(nhsId) => {
    console.log('Fetching hcp profile');

    return ApiHelper.getRequest(
        "/api/user/hcp/search/?nhsId="+nhsId,
        updateHcpSearch,
        hasError,
        isLoading
    );
}

export default {
    getHcpProfile,
    saveHcpProfile,
    searchForHCP,
    hasError,
    clearHcpUpdateSuccess
}