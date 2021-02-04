import { USER_ACCOUNT_IS_LOADING } from './types';
import { UPDATE_USER_ACCOUNT } from './types';
import { UPDATE_USER_PERMISSIONS } from './types';
import { USER_ACCOUNT_ERROR } from './types';
import { USER_ACCOUNT_SAVE_SUCCESS } from './types';
import { USER_ACCOUNT_CLEAR_SUCCESS } from './types';
import { USER_ACCOUNT_CLEAR_FETCH_SUCCESS } from './types';
import { UPDATE_DOCTORS } from './types';

import ApiHelper from './ApiHelper';

const updateUserAccount = accountDetails => {
  return {
    type: UPDATE_USER_ACCOUNT,
    payload: accountDetails
  }
}

const updateUserPermissions = (termsApproved) => {
  return {
    type: UPDATE_USER_PERMISSIONS,
    termsApproved: termsApproved
  }
}

const userAccountIsLoading = (bool) => {
  return {
    type: USER_ACCOUNT_IS_LOADING,
    isLoading: bool
  }
};

const userAccountHasError = (bool, errorMsg) => {
  return {
    type: USER_ACCOUNT_ERROR,
    hasError: bool,
    errorMsg: errorMsg
  }
};

const userAccountSuccess = (response) => {
  return {
    type: USER_ACCOUNT_SAVE_SUCCESS,
    payload: response
  }
};

const clearUserAccountSuccess = () => {
  return {
      type: USER_ACCOUNT_CLEAR_SUCCESS
  }
};

const clearUserFetchSuccess = () => {
  return {
    type: USER_ACCOUNT_CLEAR_FETCH_SUCCESS
  }
};

const updateDoctors = (payload) => {
  return {
    type: UPDATE_DOCTORS,
    payload: payload
  }
}

const getUserAccount = () => {
  console.log('Fetching user account');

  return ApiHelper.getRequest(
    "/api/user/profile",
    updateUserAccount,
    userAccountHasError,
    userAccountIsLoading
  );
};

const saveUserAccount = (accountDetails) => {
  console.log('Saving user account');

  return ApiHelper.postRequest(
    "/api/user/profile",
    accountDetails,
    userAccountSuccess,
    userAccountHasError,
    userAccountIsLoading
  );
};

const getDoctors = () => {
  console.log('Fetching doctors list');

  return ApiHelper.getRequest(
    "/api/user/profile/hcp",
    updateDoctors,
    userAccountHasError,
    userAccountIsLoading
  );
};

const saveDoctors = (careProfs, primaryProf) => {
  console.log('Saving doctors list');

  var doctorsDetails = {
    careProfessionals : careProfs,
    primaryCareProfessional: primaryProf
  };

  return ApiHelper.postRequest(
    "/api/user/profile/hcp",
    doctorsDetails,
    updateDoctors,
    userAccountHasError,
    userAccountIsLoading
  );
};

const passwordResetEmailRequest = (username) => {
  console.log('Reset Password email request');

  return ApiHelper.postRequest(
    "/api/user/passwordResetTokenRequest",
    {username: username},
    userAccountSuccess,
    userAccountHasError,
    userAccountIsLoading
  );
}

const getUserPermissions = () => {
  console.log('Fetching user permissions');

  return ApiHelper.getRequest(
    "/api//user/permission",
    updateUserPermissions,
    userAccountHasError,
    userAccountIsLoading
  );
};

const saveUserPermissions = (termsApproved) => {
  console.log('Saving user permission');

  return ApiHelper.postRequest(
    "/api/user/permission",
    {termsApproved: termsApproved},
    userAccountSuccess,
    userAccountHasError,
    userAccountIsLoading
  );
};

const changePassword = (newPassword) => {
  console.log("Change password");

  return ApiHelper.postRequest(
    "/api/user/passwordReset",
    {password: newPassword},
    userAccountSuccess,
    userAccountHasError,
    userAccountIsLoading
  );
}

const forgottenPassword = (username) => {
  console.log("Change password");

  return ApiHelper.postRequestClientToken(
    "/api/user/passwordResetTokenRequest",
    {username: username},
    userAccountSuccess,
    userAccountHasError,
    userAccountIsLoading
  );
}

export default {
  getUserAccount,
  saveUserAccount,
  userAccountHasError,
  passwordResetEmailRequest,
  getUserPermissions,
  saveUserPermissions,
  getDoctors,
  saveDoctors,
  clearUserAccountSuccess,
  clearUserFetchSuccess,
  changePassword,
  forgottenPassword
}