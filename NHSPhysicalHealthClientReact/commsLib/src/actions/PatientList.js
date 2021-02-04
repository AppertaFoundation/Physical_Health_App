import { UPDATE_PATIENT_LIST } from './types';
import { PATIENT_LIST_ERROR } from './types';
import { PATIENT_LIST_IS_LOADING } from './types';

import ApiHelper from './ApiHelper';

const patientListHasError = (bool, errorMsg) => {
    return {
        type: PATIENT_LIST_ERROR ,
        hasError: bool,
        errorMsg: errorMsg
    }
};

const patientListIsLoading = (bool) => {
    return {
        type: PATIENT_LIST_IS_LOADING,
        isLoading: bool
    }
};

const updatePatientList = (payload) => {
    return {
        type: UPDATE_PATIENT_LIST,
        payload: payload
    }
};

const getPatientList = (pageSize, start) => {
    console.log('Fetching patient list');

    return ApiHelper.getRequest(
        "/api/patients?pageSize="+pageSize+"&start="+start,
        updatePatientList,
        patientListHasError,
        patientListIsLoading
    );
};

export default {
    patientListHasError,
    patientListIsLoading,
    getPatientList
}