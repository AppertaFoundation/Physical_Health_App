import { combineReducers } from 'redux';
import { persistReducer } from 'redux-persist';
import storage from 'redux-persist/lib/storage';

import patientDataReducer from './reducers/PatientDataReducer';
import accountDataReducer from './reducers/AccountDetailsReducer';
import loginReducer from './reducers/LoginReducer';
import patientListReducer from './reducers/PatientListReducer';
import serverConfigReducer from './reducers/ServerConfigReducer';
import hcpDataReducer from './reducers/HCPDataReducer';

const loginPersistConfig = {
    key: 'login',
    storage: storage,
    blacklist: ['hasError','isLoading','tokenRefreshed']
  }
 
  export const commsReducer = combineReducers({
    patientData: patientDataReducer,
    accountData: accountDataReducer,
    login: persistReducer(loginPersistConfig, loginReducer),
    patientList: patientListReducer,
    serverConfig: serverConfigReducer,
    hcpData: hcpDataReducer
  });

  export { default as AccountDetailsActions } from './actions/AccountDetails'
  export { default as LoginActions } from './actions/Login'
  export { default as PatientDataActions } from './actions/PatientData'
  export { default as PatientListActions } from './actions/PatientList'
  export { default as ServerConfigActions } from './actions/ServerConfig'
  export { default as HcpDataActions } from './actions/HCPData'
  export { analytes as TestResultFields } from './data/TestResultFields'
  export { selfMonitoringFields as SelfMonitoringFields } from './data/TestResultFields'