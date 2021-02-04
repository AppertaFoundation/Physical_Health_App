import { USER_ACCOUNT_ERROR } from "../actions/types";
import { UPDATE_USER_ACCOUNT } from "../actions/types";
import { USER_ACCOUNT_IS_LOADING } from "../actions/types";
import { UPDATE_DOCTORS } from "../actions/types";
import { UPDATE_USER_PERMISSIONS } from '../actions/types';
import { USER_ACCOUNT_SAVE_SUCCESS } from '../actions/types';
import { USER_ACCOUNT_CLEAR_SUCCESS } from '../actions/types';
import { USER_ACCOUNT_CLEAR_FETCH_SUCCESS } from '../actions/types';

import { LOGOUT } from '../actions/types';

const initialState = {
  accountData: {
    title: "",
    firstNames: "",
    lastName: "",
    dateOfBirth: "",
    address: "",
    mobileNumber: "",
    telNumber: "",
    gender: "",
    nhsNumber: "",
  },
  termsApproved: false,
  doctors: {
    "careProfessionals": [
     ],
    "primaryCareProfessional": {
    }
  },
  hasError: false,
  errorMsg: null,
  accountUpdateSuccess: false,
  accountFetchSuccess: false
};

const accountDetailsReducer = (state = initialState, action) => {
  switch (action.type) {
    case UPDATE_USER_ACCOUNT:
      var accountData = { ...state.accountData, ...action.payload };

      return {
        ...state,
        accountData: accountData,
        accountFetchSuccess: true
      };
    case UPDATE_USER_PERMISSIONS:
      console.log('update user permissions', action);

      return Object.assign({}, state, {
        termsApproved: action.termsApproved
      });
    case USER_ACCOUNT_ERROR:
      console.log('haserror', action);

      return Object.assign({}, state, {
        hasError: action.hasError,
        errorMsg: action.errorMsg
      });
    case USER_ACCOUNT_IS_LOADING:
      console.log('isloading', action);

      return Object.assign({}, state, {
        isLoading: action.isLoading,
      });
    case UPDATE_DOCTORS:
      return {
        ...state,
        doctors: action.payload
      };
    case USER_ACCOUNT_SAVE_SUCCESS:
    return {
      ...state,
      accountUpdateSuccess: true
    };
    case USER_ACCOUNT_CLEAR_SUCCESS:
    return {
      ...state,
      accountUpdateSuccess: false
    };
    case USER_ACCOUNT_CLEAR_FETCH_SUCCESS:
    return {
      ...state,
      accountFetchSuccess: false
    };
    case LOGOUT:
        return initialState;
    default:
      return state;
  }
};

export default accountDetailsReducer;
