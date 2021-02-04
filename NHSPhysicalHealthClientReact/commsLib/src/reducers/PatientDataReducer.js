import { ADD_PATIENT_DATA } from "../actions/types";
import { UPDATE_PATIENT_DATA } from "../actions/types";
import { FETCH_PATIENT_BLOOD_DATA } from "../actions/types";
import { FETCH_PATIENT_TEST_DATA } from "../actions/types";
import { FETCH_PATIENT_LATEST_TEST_DATA } from "../actions/types";
import { FETCH_PATIENT_SM_DATA } from '../actions/types';
import { PATIENT_DATA_ERROR } from "../actions/types";
import { PATIENT_DATA_IS_LOADING } from "../actions/types";
import { PATIENT_DATA_SUCCESS_CLEAR } from "../actions/types";
import { LOGOUT } from '../actions/types';

const initialState = {
  patientData: [
  ],
  latestResults: {},
  hasError: false,
  isLoading: false,
  errorMsg: null,
  updateSuccess: false
};

function updatePatientDataInArray(array, action) {
  return array.map((item, index) => {
    if (index !== action.index) {
      // This isn't the item we care about - keep it as-is
      return item;
    }

    // Otherwise, this is the one we want - return an updated value
    return {
      ...item,
      ...action.data
    };
  });
}

function updatePatientBloodDataArray(array) {
  return array.map((item, index) => {
    return {
      date: new Date(item.time),
      bloodPressureSystolic: item.systolic,
      bloodPressureDiastolic: item.diastolic,
      composerName: item.composerName,
      compositionId: item.compositionId
    };
  });
}

function updatePatientTestDataArray(array) {
  return array.map((item, index) => {
    return {
      date: new Date(item.result_time),
      result: item.result,
      units: item.result_unit,
      composerName: item.composerName,
      compositionId: item.compositionId
    };
  });
}

function updatePatientSmDataArray(array, field) {
  var resultsArray = [];
  array.map((item, index) => {
    if ( field == "weight"){
      if ( item.weight_time != null ){
        resultsArray.push({
          date: new Date(item.weight_time),
          result: item.weight,
          units: item.weight_unit,
          composerName: item.composerName,
          compositionId: item.compositionId
        });
      }
    }else if ( field =="height"){
      if ( item.height_time != null ){
        resultsArray.push({
          date: new Date(item.height_time),
          result: item.height,
          units: item.height_unit,
          composerName: item.composerName,
          compositionId: item.compositionId
        });
      }
     } else if ( field == "bmi"){
        if ( item.bmi_time != null ){
          resultsArray.push({
            date: new Date(item.bmi_time),
            result: item.bmi,
            units: item.bmi_unit,
            composerName: item.composerName,
            compositionId: item.compositionId
          });
        }
      } else if (field == "waist_circumference") {
        if ( item.waist_circumference_time != null ){
          resultsArray.push({
            date: new Date(item.waist_circumference_time),
            result: item.waist_circumference,
            units: item.waist_circumference_unit,
            composerName: item.composerName,
            compositionId: item.compositionId
          });
        }
      } else if ( field == "qrisk_score"){
        if ( item.qrisk_time != null ){
          resultsArray.push({
            date: new Date(item.qrisk_time),
            result: item.qrisk,
            units: item.qrisk_unit,
            composerName: item.composerName,
            compositionId: item.compositionId
          });
        }
      }
    });

  return resultsArray;
}

function updateLatestTestDataArray(array) {
  var resultsArray = [];
  console.log(array);

  for (var key in array){
    var item = array[key];

    if ( item.resultSet != null ){
      var resultItem ={"code": key};
      var resultSet = item.resultSet[0];
      var singular = item.resultSet.length == 1;
      var composerName = resultSet.composerName;
      var compositionId = resultSet.compositionId;

      console.log("***** RESULT SET 2 *******");
      console.log(resultSet);
      
      var dateString = "";
      var value = "";
      var units = "";
      var name = "";
      if (key == "height") {
        dateString = resultSet.height_time;
        value = resultSet.height;
        units = resultSet.height_unit;
        name = "height";
      } else if (key == "weight") {
        dateString = resultSet.weight_time;
        value = resultSet.weight;
        units = resultSet.weight_unit;
        name = "Weight";
      } else if (key == "bmi") {
        dateString = resultSet.bmi_time;
        value = resultSet.bmi;
        units = resultSet.bmi_unit;
        name = "BMI";
      } else if (key == "waist") {
        dateString = resultSet.waist_circumference_time;
        value = resultSet.waist_circumference,
        units = resultSet.waist_circumference_unit,
        name = "Waist";
      } else if (key == "qrisk") {
        dateString = resultSet.qrisk_time;
        value = resultSet.qrisk,
        units = resultSet.qrisk_unit,
        name = "QRisk";
      } else if (key == "blood") {
        dateString = resultSet.time;
        key = "diastolic";
        value = resultSet.diastolic;
        units = resultSet.diastolic_units;
        name = "Blood Pressure - Diastolic"

        // Handle dystolic
        var systolic = {
        "code" : "systolic",
        "date" : new Date(resultSet.time),
        "value" : resultSet.systolic,
        "units" : resultSet.systolic_units,
        "name"  : "Blood Pressure - Systolic",
        "singular" : singular,
        "composerName": composerName
        }
        resultsArray.push(systolic)
      } else {
        dateString = resultSet.result_time,
        value = resultSet.result,
        units = resultSet.result_unit
        name = resultSet.result_name;
      }

      console.log("dateString = " + dateString);  

      var resultItem = {
                      "code": key,
                      "date": new Date(dateString),
                      "value": value,
                      "units": units,
                      "name": name,
                      "singular" : singular,
                      "composerName": composerName,
                      "compositionId": compositionId
                    };

      console.log("RESULT SET");              
      console.log(resultItem);              

      resultsArray.push(resultItem);
    }
  }

  return resultsArray;
}

const patientDataReducer = (state = initialState, action) => {
  switch (action.type) {
    case ADD_PATIENT_DATA:
      return {
        ...state,
        patientData: state.patientData.concat(action.payload).sort(function (a, b) { return a.date - b.date }),
        updateSuccess: true
      };
    case UPDATE_PATIENT_DATA:
      console.log(action);
      var updatedArray = updatePatientDataInArray(state.patientData, action.payload).sort(function (a, b) { return a.date - b.date });
      console.log(updatedArray);

      return {
        ...state,
        patientData: updatedArray
      };
    case FETCH_PATIENT_BLOOD_DATA:
    console.log(action);
      var array = updatePatientBloodDataArray(action.payload.resultSet).sort(function (a, b) { return a.date - b.date });
      
      return Object.assign({}, state, {
        patientData: array,
        updateSuccess: false
      });
    case FETCH_PATIENT_TEST_DATA:
    console.log(action);
    var array = updatePatientTestDataArray(action.payload.resultSet).sort(function (a, b) { return a.date - b.date });
      
      return Object.assign({}, state, {
        patientData: array,
        updateSuccess: false
      });
    case FETCH_PATIENT_SM_DATA:  
    console.log(action);
    var array = updatePatientSmDataArray(action.payload.resultSet, action.field).sort(function (a, b) { return a.date - b.date });
      
      return Object.assign({}, state, {
        patientData: array,
        updateSuccess: false
      });
    case FETCH_PATIENT_LATEST_TEST_DATA:
    console.log(action);
    var array = updateLatestTestDataArray(action.payload.queryResults);
      
      return Object.assign({}, state, {
        latestResults: array,
        updateSuccess: false
      });

    case PATIENT_DATA_ERROR:
      console.log('haserror', action);

      return Object.assign({}, state, {
        hasError: action.hasError,
        errorMsg: action.errorMsg,
        updateSuccess: false
      });
    case PATIENT_DATA_IS_LOADING:
      console.log('isloading', action);

      return Object.assign({}, state, {
        isLoading: action.isLoading,
      });
    case PATIENT_DATA_SUCCESS_CLEAR: {
      console.log('isloading', action);

      return Object.assign({}, state, {
        updateSuccess: false,
      });
    }
    case LOGOUT:
    return initialState;
    default:
      return state;
  }
};

export default patientDataReducer;
