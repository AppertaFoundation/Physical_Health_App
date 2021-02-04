import { UPDATE_PATIENT_LIST } from '../actions/types';
import { PATIENT_LIST_ERROR } from '../actions/types';
import { PATIENT_LIST_IS_LOADING } from '../actions/types';
import { LOGOUT } from '../actions/types';

const initialState = {
    patientList:{
        pageSize: 0,
        start: 0,
        profiles: [],
    },
    hasError : false,
    isLoading: false,
    errorMsg: null
};

const patientListReducer = (state = initialState, action) => {
    const { type, payload } = action;

    switch (type){
        case UPDATE_PATIENT_LIST :
            console.log('islogged', action);

            return Object.assign({}, state, {
                patientList: action.payload,
                hasError: false
            });
        case PATIENT_LIST_ERROR:
            console.log('haserror', action);

            return Object.assign({}, state, {
                hasError: action.hasError,
                errorMsg: action.errorMsg
            });
        case PATIENT_LIST_IS_LOADING :
            console.log('isloading', action);

            return Object.assign({}, state, {
                isLoading: action.isLoading,
            });
            case LOGOUT:
            return initialState;
        default:
            return state
    }
}

export default patientListReducer;