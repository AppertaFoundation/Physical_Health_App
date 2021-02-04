import { ADD_PATIENT_DATA } from './types';
import { UPDATE_PATIENT_DATA } from './types';
import { PATIENT_DATA_ERROR } from './types';
import { PATIENT_DATA_IS_LOADING } from './types';
import { PATIENT_DATA_SUCCESS_CLEAR } from './types';
import { FETCH_PATIENT_BLOOD_DATA } from './types';
import { FETCH_PATIENT_TEST_DATA } from './types';
import { FETCH_PATIENT_LATEST_TEST_DATA } from './types';
import { FETCH_PATIENT_SM_DATA } from './types';
import { analytes } from '../data/TestResultFields';
import { selfMonitoringFields } from '../data/TestResultFields';

import ApiHelper from './ApiHelper';


const updatePatientData = (patientData, index) => {
  return {
    type: UPDATE_PATIENT_DATA,
    payload: { data: patientData, index: index }
  }
}

const hasError = (bool, errorMsg) => {
  return {
    type: PATIENT_DATA_ERROR,
    hasError: bool,
    errorMsg: errorMsg
  }
};

const isLoading = (bool) => {
  return {
    type: PATIENT_DATA_IS_LOADING,
    isLoading: bool
  }
};

const clearUpdateSuccess = () => {
  return {
    type: PATIENT_DATA_SUCCESS_CLEAR
  }
};

const fetchPatientData = (patientData) => {
  return {
    type: FETCH_PATIENT_TEST_DATA,
    payload: patientData
  }
}

const fetchLatestPatientData = (patientData) => {
  return {
    type: FETCH_PATIENT_LATEST_TEST_DATA,
    payload: patientData
  }
}

const fetchPatientBloodData = (patientData) => {
  return {
    type: FETCH_PATIENT_BLOOD_DATA,
    payload: patientData
  }
}

const fetchLatestPatientSmData = (patientData, field) => {
  return {
    type: FETCH_PATIENT_SM_DATA,
    payload: patientData,
    field: field
  }
}

const addBloodPressureData = (patientData, successData) => {
  var systolicBP = successData.systolicBP;
  var diastolicBP = successData.diastolicBP;
  var date = new Date(successData.timeStamp);

  return {
    type: ADD_PATIENT_DATA,
    payload: {
      date: date,
      bloodPressureSystolic: systolicBP,
      bloodPressureDiastolic: diastolicBP
    }
  }
}

const fetchBloodPressureReadings = (username) => {
  console.log('fetching blood pressure');

  var composition = {
    queryTemplate: "PhysHealthBloodPressure"
  }

  if (username) {
    composition.username = username;
  }

  return ApiHelper.postRequest(
    "/api/ehr/query",
    composition,
    fetchPatientBloodData,
    hasError,
    isLoading,
  );
}

const fetchSelfMonitoringReading = ( username, field ) => {
  console.log('fetching ' + field);

  var composition = {
    queryTemplate: "PhysHealthBodyMeasurements"
  }

  if (username) {
    composition.username = username;
  }

  return ApiHelper.postRequest(
    "/api/ehr/query",
    composition,
    fetchLatestPatientSmData, 
    hasError,
    isLoading,
    field
  );
}

const fetchTestResultsReading = ( username, field ) => {
  console.log('fetching ' + field);

  var composition = {
      "queryTemplate":"PhysHealthLabResultsByCode",
      "parameters" : {
                      "lab_code" : field
      }
  }

  if (username) {
    composition.username = username;
  }

  return ApiHelper.postRequest(
    "/api/ehr/query",
    composition,
    fetchPatientData,
    hasError,
    isLoading,
  );
}

const createComposition = (username, timeStamp, testResults, selfMonitoring) => {
  var dateString = new Date(timeStamp).toISOString();

  var composition = {
    ehrTemplateName: "Physical health app self monitoring-v0",
    parameterMap: "PhysHealthSelf",
    recordDate: timeStamp,
    contents: {
    }
  }

  if ( selfMonitoring.length > 0 ){
      var smContent = selfMonitoring.map((item, key) => {
        var result = new Object;
        var lookup = selfMonitoringFields[item.field];
        result[lookup.unitsField] = lookup.units;
        result[item.field] = item.value;
        result[lookup.time] = dateString;

        return result;
      });

      composition.contents.physical_health_app_self_monitoring = smContent;
  }

  if (testResults.length > 0) {
    var labContents = testResults.map((item, key) => {
      // Lookup test name and terminology
      var element = analytes.find(element => element.analyte_name_code == item.test_code);

      return {
        "test_time": dateString,
        "test_name": element.analyte_name_text,
        "test_code": item.test_code,
        "test_terminology": element.analyte_name_system,
        "result_name": element.analyte_name_text,
        "result_code": item.test_code,
        "result_terminology": element.analyte_name_system,
        "result_value": item.value,
        "result_unit": element.currentUnits != null ? element.currentUnits : element.UCUM_unit[0]
      };
    }
    );

    composition.contents.labtest = labContents;
  }


  if (username != null) {
    composition.username = username;
  }

  return composition;
}

const addTestResultReadings = (username, timeStamp, testResults, selfMonitoring) => {
  console.log('Uploading blood pressure');

  var successData = {
    timeStamp: timeStamp
  }

  var composition = createComposition(username, timeStamp, testResults, selfMonitoring);

  return ApiHelper.postRequest(
    "/api/ehr/composition",
    composition,
    addBloodPressureData,
    hasError,
    isLoading,
    successData
  );
}

const compositionConversionFields = {
  "blood_pressure/systolic|magnitude":{
    field: "systolic",
    fieldName: "systolic"
  },
  "blood_pressure/systolic|unit":{
    field: "systolic",
    fieldName: "systolic_unit"
  },
  "blood_pressure/diastolic|magnitude":{
    field: "diastolic",
    fieldName: "diastolic"
  },
  "blood_pressure/diastolic|unit":{
    field: "diastolic",
    fieldName: "diastolic_unit"
  },
  "weight/weight|unit": {
    field: "weight",
    fieldName: "weight_unit"
  },
  "weight/weight|magnitude": {
    field: "weight",
    fieldName: "weight"
  },
  "weight/time": {
    field: "weight",
    fieldName: "weight_time"
  },
  "height/time": {
    field: "height",
    fieldName: "height_time"
  },
  "height/height|magnitude": {
    field: "height",
    fieldName: "height"
  },
  "height/height|unit": {
    field: "height",
    fieldName: "height_unit"
  },
  "body_mass_index/time": {
    field: "bmi",
    fieldName: "bmi_time"
  },
  "body_mass_index/body_mass_index|magnitude": {
    field: "bmi",
    fieldName: "bmi"
  },
  "body_mass_index/body_mass_index|unit": {
    field: "bmi",
    fieldName: "bmi_unit"
  },
  "waist_circumference/time": {
    field: "waist_circumference",
    fieldName: "waist_time"
  },
  "waist_circumference/waist_circumference|magnitude": {
    field: "waist_circumference",
    fieldName: "waist_circumference"
  },
  "waist_circumference/waist_circumference|unit": {
    field: "waist_circumference",
    fieldName: "waist_circumference_unit"
  },
  "qrisk2_2015_score/time": {
    field: "qrisk_score",
    fieldName: "qrisk_time"
  },
  "qrisk2_2015_score/qrisk2_score|magnitude": {
    field: "qrisk_score",
    fieldName: "qrisk_score"
  },
  "qrisk2_2015_score/qrisk2_score|unit": {
    field: "qrisk_score",
    fieldName: "qrisk_unit"
  }
};

const handleCompositionFetchSuccess = (response, testData) => {
  console.log('Updating composition');

  var testResults = {};
  var smResults = {};

  for (var key in response.composition){
    var item = response.composition[key];

    // Remove start of string
    var key = key.substring( "physical_health_app_self_monitoring/".length);
    if ( key.startsWith("laboratory_test_result:")){
      // Test results
      var index = key.match("[^\:]+(?=\/)");
      if (  typeof testResults[index] === 'undefined'){
        testResults[index] = {};  
      }

      var testResult = testResults[index];

      var splitKey = key.split('/').pop();
      if ( splitKey == "time") {
        testResult["test_time"] = item;
      } else if ( splitKey == "test_name|value") {
        testResult["test_name"] = item;
      } else if ( splitKey == "test_name|code") {
        testResult["test_code"] = item;
      } else if ( splitKey == "test_name|terminology") {
        testResult["test_terminology"] = item;
      } else if ( splitKey == "analyte_name|value") {
        testResult["result_name"] = item;
      } else if ( splitKey == "analyte_name|code") {
        testResult["result_code"] = item;
      } else if ( splitKey == "analyte_name|terminology") {
        testResult["result_terminology"] = item;
      } else if ( splitKey == "analyte_result:0|magnitude") {
        testResult["result_value"] = item;
      } else if ( splitKey == "analyte_result:0|unit") {
        testResult["result_unit"] = item;
      }
    } else {
      // Self monitoring results
      var lookup = compositionConversionFields[key];

      if ( lookup != null ){
        if (  typeof smResults[lookup.field] === 'undefined'){
          smResults[lookup.field] = {};  
        }

        var field = smResults[lookup.field];
        field[lookup.fieldName] = item;
      } else if ( key == "blood_pressure/time"){
        if (  typeof smResults["diastolic"] === 'undefined'){
          smResults["diastolic"] = {};  
        }

        if (  typeof smResults["systolic"] === 'undefined'){
          smResults["systolic"] = {};  
        }

        var diastolic = smResults["diastolic"];
        diastolic["bp_time"] = item;

        var systolic = smResults["systolic"];
        systolic["bp_time"] = item;
      }
    }
  }


  // Remove results that will be updated
  testData.selfMonitoring.map((item) => {
    delete smResults[item.field];
  });

  var testResultsArray = Object.values(testResults);

  console.log("Test results array");
  console.log(testResultsArray);
  testData.testResults.map((item) => {
    console.log(item);
    var index = testResultsArray.findIndex(t => t.test_code == item.test_code);
    console.log("item index " + index);
    if ( index > -1 ){
      testResultsArray.splice(index, 1);
    }
  });
  console.log(testResultsArray);

  // Create composition
  var composition = createComposition(testData.username, testData.timeStamp, testData.testResults, testData.selfMonitoring);

  // Merge fields
  var selfMonitoringFields = composition.contents.physical_health_app_self_monitoring;
  var smResultsArray = Object.values(smResults);
  if ( selfMonitoringFields ){
    console.log("Concat sm");
    composition.contents.physical_health_app_self_monitoring = selfMonitoringFields.concat(smResultsArray);
  } else if ( smResultsArray.length > 0 ){
    console.log("add sm");
    composition.contents.physical_health_app_self_monitoring = smResultsArray;
  }

  var labFields = composition.contents.labtest;
  if ( labFields ){
    console.log("Concat lab");
    composition.contents.labtest = labFields.concat(testResultsArray);
  } else if ( testResultsArray.length > 0 ){
    console.log("Add lab");
    composition.contents.labtest = testResultsArray;
  }

  console.log("Updated composition");
  console.log(composition);

  var successData = {
    timeStamp: testData.timeStamp
  }

  return ApiHelper.putRequest(
    "/api/ehr/composition?compositionUid="+testData.compositionId,
    composition,
    addBloodPressureData,
    hasError,
    isLoading,
    successData
  );
}

const upateTestResultReadings = (username, timeStamp, compositionId, testResults, selfMonitoring) => {
  console.log('Updating composition');
  return (dispatch, getState) => {
    var successData = {
      timeStamp: timeStamp,
      compositionId: compositionId,
      testResults: testResults,
      selfMonitoring: selfMonitoring
    }

    var loginState = getState().comms.login;
    var username = loginState.username;

    dispatch(ApiHelper.getRequest(
      "/api/ehr/composition?compositionUid=" + compositionId + "&username=" + username,
      handleCompositionFetchSuccess,
      hasError,
      isLoading,
      successData
    ));
  };
}



const fetchLatestReadings = (username, tests) => {
  console.log('fetching latest readings');

  var query = {};
  tests.map((item) => {
    if (item == "height") {
      query[item] = { "queryTemplate": "PhysHealthLatestHeight" };
    } else if (item == "weight") {
      query[item] = { "queryTemplate": "PhysHealthLatestWeight" };
    } else if (item == "bmi") {
      query[item] = { "queryTemplate": "PhysHealthLatestBmi" };
    } else if (item == "waist") {
      query[item] = { "queryTemplate": "PhysHealthLatestWaist" };
    } else if (item == "qrisk_score") {
      query[item] = { "queryTemplate": "PhysHealthLatestQrisk" };
    } else if (item == "blood") {
      query[item] = { "queryTemplate": "PhysHealthLatestBloodPressure" };
    } else {
      query[item] = {
          "queryTemplate": "PhysHealthLatestLabByCode",
          "parameters": {
            "lab_code": item
          }
      }
    }
  });

  var composition = {
    "queries": query
  };

  if (username) {
    composition.username = username;
  }

  return ApiHelper.postRequest(
    "/api/ehr/multiquery",
    composition,
    fetchLatestPatientData,
    hasError,
    isLoading,
  );
}

export default {
  addTestResultReadings,
  upateTestResultReadings,
  fetchBloodPressureReadings,
  fetchLatestReadings,
  fetchSelfMonitoringReading,
  fetchTestResultsReading,
  updatePatientData,
  clearUpdateSuccess,
  hasError
}