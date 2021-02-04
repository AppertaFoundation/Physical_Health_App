import React, { Component } from "react";
import {
  View,
  Text,
  ImageBackground,
  Button,
  ScrollView,
  TouchableOpacity,
  Alert
} from "react-native";
import { TextField } from "react-native-material-textfield";
import { SafeAreaView } from "react-navigation";
import { RaisedTextButton } from "react-native-material-buttons";
import { connect } from "react-redux";
import { PatientDataActions } from "nhsphysicalhealthcomms";
import DateTimePicker from "react-native-modal-datetime-picker";
import CheckBox from "react-native-check-box";
import ModalFilterPicker from "react-native-modal-filter-picker";

import { analytes as TestResultFields } from "./TestResultFields";
import { selfMonitoringFields as SelfMonitoringFields } from "./TestResultFields";
import { supported_fields as SUPPORTED_FIELDS } from "./SupportedFieldsConstants";

import { getLocalDateString } from "../utils/DateUtils";
import { getLocalTimeString } from "../utils/DateUtils";

import Loader from "../utils/Loader";
import GradientButton from "../utils/GradientButton";
import MaterialIcon from "react-native-vector-icons/MaterialIcons";

import {KeyboardAwareScrollView} from 'react-native-keyboard-aware-scrollview';

import R from "res/R";

const fields = [
  "weight",
  "weightStone",
  "weightPounds",
  "bloodPressureSystolic",
  "bloodPressureDiastolic"
];

class AddPatientDataScreen extends Component {
  static propTypes = {};

  static defaultProps = {};

  static navigationOptions = {
    title: "Edit Data"
  };

  inputs = {};

  constructor(props) {
    super(props);

    this.onSubmit = this.onSubmit.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.onWeightChangeText = this.onWeightChangeText.bind(this);
    this.handleTestChange = this.handleTestChange.bind(this);
    this.generateInputFields = this.generateInputFields.bind(this);
    this.onDeletePressedForm = this.onDeletePressedForm.bind(this);
    this.onDeletePressedSmForm = this.onDeletePressedSmForm.bind(this);

    this.state = {
      date: new Date(),
      weightStone: "",
      weightPounds: "",
      weight: "",
      weightMetric: !this.props.weightImperial,

      secureTextEntry: true,
      loading: false,
      editing: false,
      testData: [],
      smData: [],
      missingFields: [],
      formElements: [],
      smFormElements: [],

      missingFieldsVisible: false,
      missingFieldsPicked: null
    };
  }

  componentDidMount() {
    console.log(this.props.latestData);

    // Clear any previous success flag
    this.props.clearSuccess();

    if (Object.keys(this.props.latestData).length === 0) {
      this.props.fetchPatientData();
    }

    const { navigation } = this.props;
    const date = navigation.getParam("date", null);
    if (date != null) {
      this.setState({
        date: date,
        editing: true
      });
    }

    this.generateInputFields();
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.props.hasError) {
      var errorMsg = "Unable to update data, please check you have a network connection and retry";
      if (this.props.errorMsg && this.props.errorMsg.message) {
        // There is an error messsage from the server, display a user friendly message.
        errorMsg = "Unable to update data, please check that the values you entered are correct";
      }
      // Display error within alert
      Alert.alert(
        "",
        errorMsg,
        [
          {
            text: "OK",
            onPress: () => {
              this.props.clearError();
            }
          }
        ],
        { cancelable: false }
      );
    } else if (this.props.updateSuccess) {
      // Refresh data
      this.props.fetchPatientData();

      // Return to previous screen.
      this.props.navigation.goBack();
    } else if (Object.keys(prevProps.latestData).length === 0
      && Object.keys(this.props.latestData).length != 0) {
      this.generateInputFields();
    }
  }

  generateInputFields() {
    // Get Existing data fields
    var formElementArray = [];
    var smFormElementsArray = [];
    var missingFieldsArray = [];

    var resultsArray = [];

    const { navigation } = this.props;
    const dataCode = navigation.getParam("dataCode", "");
    const values = navigation.getParam("values", []);

    if (dataCode != "") {
      resultsArray = dataCode;
    } else {
      resultsArray = this.props.latestData;
    }

    var arrayLength = resultsArray.length;
    for (var i = 0; i < arrayLength; i++) {
      var resultItem = resultsArray[i];
      var formField = TestResultFields.find(function (element) {
        return element.analyte_name_code == resultItem.code;
      });

      if (formField != null) {
        formField.currentUnits = formField.UCUM_unit[0];
        formElementArray.push(formField);
      } else {
        // Look up self reporting fields ( Hide bmi which is calculated from weight/height)
        if (resultItem.code != "bmi") {
          var smFormField = SelfMonitoringFields[resultItem.code];
          smFormField.code = resultItem.code;
          smFormElementsArray.push(smFormField);
        }
      }
    }

    if (dataCode != "") {
      var testData = this.state.testData;
      var smData = this.state.smData;

      for (var i = 0; i < arrayLength; i++) {
        var resultItem = resultsArray[i];
        var formField = TestResultFields.find(function (element) {
          return element.analyte_name_code == resultItem.code;
        });

        if (formField != null) {
          testData = {
            ...testData,
            [resultItem.code]: values[i].toString()
          };
        } else {
          if (resultItem.code == "weight") {
            var kgs = parseFloat(values[i]) || 0;
            var poundTotal = kgs * 2.20462;
            var stone = Math.floor(poundTotal / 14);
            var pounds = Math.round(poundTotal - stone * 14);
            console.log({ kgs, poundTotal, stone, pounds });

            this.setState({
              weightPounds: String(pounds),
              weightStone: String(stone),
              weight: values[i] != "" ? String(kgs) : ""
            });
          } else {
            smData = {
              ...smData,
              [resultItem.code]: values[i].toString()
            };
          }
        }
      }

      this.setState({
        testData: testData,
        smData: smData
      });

    } else {
      // Generate array of missing fields
      arrayLength = SUPPORTED_FIELDS.length;
      for (var i = 0; i < arrayLength; i++) {
        var code = SUPPORTED_FIELDS[i];

        if (code == "bmi") {
          // Hide bmi which is calculated from weight/height
        }
        else if (code == "blood") {
          var found = smFormElementsArray.find(function (element) {
            return element.code == "systolic";
          });

          if (!found) {
            var bloodField = { key: "blood", label: "Blood Pressure" };
            missingFieldsArray.push(bloodField);
          }
        } else {
          // Is ths code already added?
          var found = formElementArray.find(function (element) {
            return element.analyte_name_code == code;
          });

          if (found == null) {
            var found = smFormElementsArray.find(function (element) {
              return element.code == code;
            });

            if (found == null) {
              var formField = TestResultFields.find(function (element) {
                return element.analyte_name_code == code;
              });

              var missingField = { key: code };
              if (formField != null) {
                missingField.label = formField.localised_analyte_name;
              } else {
                // Look up self reporting fields
                var smFormField = SelfMonitoringFields[code];
                missingField.label = smFormField.name;
              }

              missingFieldsArray.push(missingField);
            }
          }
        }
      }
    }

    var missingFieldsSorted = missingFieldsArray.sort(function (a, b) {
      if (a.label < b.label) { return -1; }
      if (a.label > b.label) { return 1; }
      return 0;
    })

    this.setState({
      formElements: formElementArray,
      smFormElements: smFormElementsArray,
      missingFields: missingFieldsSorted
    });
  }

  onFocus() {
    let { errors = {} } = this.state;

    for (let name in errors) {
      let ref = this[name];

      if (ref && ref.isFocused()) {
        delete errors[name];
      }
    }

    this.setState({ errors });
  }

  focusTheField = id => {
    console.log("FOCUS_THE_FIELD");
    console.log(id);
    console.log(this.inputs);
    this.inputs[id].focus();
  };

  onWeightChangeText(text, code) {
    if (code == "weight") {
      var kgs = parseFloat(text) || 0;
      var enteringDecimal = text.charAt(text.length - 1) == ".";
      var poundTotal = kgs * 2.20462;
      var stone = Math.floor(poundTotal / 14);
      var pounds = Math.round(poundTotal - stone * 14);
      console.log(code);
      console.log(text);
      console.log({ kgs, poundTotal, stone, pounds });

      var kgsString = String(kgs) + (enteringDecimal ? "." : "");

      this.setState({
        weightPounds: String(pounds),
        weightStone: String(stone),
        weight: text != "" ? kgsString  : ""
      });
    } else if (code == "weightPounds" || code == "weightStone") {
      var pounds = code == "weightPounds" ? text : this.state.weightPounds;
      var stones = code == "weightStone" ? text : this.state.weightStone;

      // Get the numeric value of pounds
      var poundsValue = parseInt(pounds) || 0;

      // Restrict number of pounds to less than 14
      if (poundsValue >= 14) {
        poundsValue = 13;
        pounds = "13";
      }

      // Get numeric value of stones
      var stonesValue = parseInt(stones) || 0;

      // Calculate number of kgs.
      var poundsTotal = stonesValue * 14 + poundsValue;
      var kgs = Math.round(poundsTotal / 2.20462);
      console.log(code);
      console.log(text);
      console.log({ kgs, poundsTotal, stonesValue, poundsValue });

      this.setState({
        weight: String(kgs),
        weightPounds: pounds != "" ? String(pounds) : "",
        weightStone: stones != "" ? String(stones) : ""
      });
    }
  }

  onChangeText(text, code) {
    const { smData } = this.state;

    this.setState({
      smData: {
        ...smData,
        [code]: text
      }
    });
  }

  updateRef(name, ref) {
    this[name] = ref;
  }

  onSubmit() {
    // Validate input
    let errors = {};
    let noErrors = true;

    if (!this.state.date) {
      noErrors = false;
      errors["date"] = "Date needed";
    }

    this.setState({ errors });

    if (noErrors) {
      var updatedData = {
        date: this.state.date
      };

      const { testData } = this.state;
      const { smData } = this.state;
      const { weight } = this.state;

      // Convert test results into an array
      var testResults = Object.keys(testData).map(function (key) {
        var testResult = new Object();
        testResult.test_code = key;
        testResult.value = testData[key];
        return testResult;
      });

      updatedData.testResults = testResults;

      // Convert self-monitoring results into an array
      var selfmonitoringResults = Object.keys(smData).map(function (key) {
        var testResult = new Object();
        testResult.field = key;
        testResult.value = smData[key];
        return testResult;
      });

      if (weight != "") {
        var testResult = new Object();
        testResult.field = "weight";
        testResult.value = weight;
        selfmonitoringResults.push(testResult);

        var height = this.props.height;
        if (height != null) {
          var height = parseInt(height, 10);
          var weightFloat = parseFloat(weight);

          if (height != NaN && weightFloat != NaN) {
            var bmiResult = new Object();
            var heightMetres = height / 100;
            var bmi = weight / (heightMetres * heightMetres);
            bmi = (Math.round(bmi * 100) / 100).toFixed(2);

            bmiResult.field = "bmi";
            bmiResult.value = bmi;
            selfmonitoringResults.push(bmiResult);
          }
        }

      }

      updatedData.selfmonitoringResults = selfmonitoringResults;

      const { navigation } = this.props;
      const dataCode = navigation.getParam("dataCode", "");

      if (dataCode == "") {
        this.props.add(updatedData);
      }
      else {
        var compositionId = navigation.getParam("compositionId", "");
        this.props.update(updatedData, compositionId);
      }
    }
  }

  _showDatePicker = () => this.setState({ isDatePickerVisible: true });

  _hideDatePicker = () => this.setState({ isDatePickerVisible: false });

  _showTimePicker = () => this.setState({ isTimePickerVisible: true });

  _hideTimePicker = () => this.setState({ isTimePickerVisible: false });


  _handleDatePicked = date => {
    console.log("A date has been picked: ", date);
    this.setState({
      date: date
    });

    // Remove any error
    let { errors = {} } = this.state;
    delete errors["date"];
    this.setState({ errors });

    this._hideDatePicker();
    this._hideTimePicker();
  };

  handleTestChange(text, id) {
    const { testData } = this.state;

    this.setState({
      testData: {
        ...testData,
        [id]: text
      }
    });

    console.log("Handle change");
    console.log(text);
    console.log("id " + id);
    console.log(this.state);
  }

  onMissingFieldsShow = () => {
    this.setState({ missingFieldsVisible: true });
  };

  onMissingFieldsSelect = pickedKey => {
    console.log("On Missing field");
    console.log(pickedKey);

    var picked = this.state.missingFields.find(function (element) {
      return element.key == pickedKey.key;
    });

    this.setState({
      missingFieldsPicked: picked,
      missingFieldsVisible: false
    });
  };

  onMissingFieldsCancel = () => {
    this.setState({
      missingFieldsVisible: false
    });
  };

  onMissingFieldsAdd = () => {
    console.log("ADD TEST FIELD");

    var code = this.state.missingFieldsPicked.key;
    console.log("Code = " + code);

    var formElementArray = this.state.formElements;
    var smFormElementsArray = this.state.smFormElements;

    var formField = TestResultFields.find(function (element) {
      return element.analyte_name_code == code;
    });

    if (code == "blood") {
      var smFormField = SelfMonitoringFields["systolic"];
      smFormField.code = "systolic";
      smFormElementsArray.push(smFormField);

      var smFormField2 = SelfMonitoringFields["diastolic"];
      smFormField2.code = "diastolic";
      smFormElementsArray.push(smFormField2);

    } else if (formField != null) {
      formField.currentUnits = formField.UCUM_unit[0];
      formElementArray.push(formField);
    } else {
      // Look up self reporting fields
      var smFormField = SelfMonitoringFields[code];
      smFormField.code = code;
      smFormElementsArray.push(smFormField);
    }

    var missingFieldsArray = this.state.missingFields;
    var updatedMissingFieldsArray = missingFieldsArray.filter(field => {
      return field.value != code;
    });

    this.setState({
      formElements: formElementArray,
      smFormElements: smFormElementsArray,
      missingFields: updatedMissingFieldsArray,
      missingFieldsPicked: null
    });
  };

  onDeletePressedForm(index) {
    console.log("delete pressed");
    console.log(index);

    var array = [...this.state.formElements];
    array.splice(index, 1);
    this.setState({ formElements: array });
  }

  onDeletePressedSmForm(index) {
    console.log("delete pressed");
    console.log(index);

    var array = [...this.state.smFormElements];
    array.splice(index, 1);
    this.setState({ smFormElements: array });
  }

  testResultsFieldsOutput = () => {
    let {
      errors = {},
      secureTextEntry,
      testData,
      formElements,
      ...data
    } = this.state;

    var lastElement = formElements.length - 1;
    var items = formElements.map((item, index) => {
      var code = item.analyte_name_code;
      return (
        <View>
          <View style={{ flexDirection: "row", paddingTop: 20, paddingBottom: 10 }}>
            <Text style={[R.styles.highlightNormal, { alignSelf: "center" }]}>
              {item.localised_analyte_name}
            </Text>
            <View style={{ flex: 1 }} />
            {!this.state.editing ?
              <MaterialIcon
                size={24}
                name={"delete"}
                color={R.colors.darkGrey}
                style={{ marginTop: 10 }}
                onPress={() => this.onDeletePressedForm(index)}
              />
              : null}
          </View>
          <View style={{ flexDirection: "row" }}>
            <View style={{ flex: 3 }}>
              <TextField
                inputContainerStyle={R.styles.textcontainer}
                inputContainerPadding={R.constants.textFieldLabelHeight}
                labelHeight={R.constants.textFieldLabelHeight}
                labelTextStyle={R.styles.textFieldLabelText}
                tintColor={R.colors.nhsblue}
                keyboardType="number-pad"
                value={testData[code]}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={text => {
                  this.handleTestChange(text, code);
                }}
                onSubmitEditing={() => {
                  index == lastElement
                    ? this.onSubmit()
                    : this.focusTheField(formElements[index + 1].analyte_name_code);
                }}
                returnKeyType={index == lastElement ? "done" : "next"}
                ref={input => {
                  this.inputs[code] = input;
                }}
                label={item.localised_analyte_name}
                error={errors[code]}
              />
            </View>

            <Text style={[R.styles.darkNormal, { alignSelf: "center", width: 100, marginLeft: 10 }]}>
              {item.UCUM_unit[0]}
            </Text>
          </View>
        </View>
      );
    });

    return items;
  };

  smResultsFieldOutput = () => {
    let {
      errors = {},
      secureTextEntry,
      testData,
      smData,
      formElements,
      smFormElements,
      ...data
    } = this.state;

    var lastElement = smFormElements.length - 1;
    var firstFormCode = "";
    if (formElements.length > 0) {
      firstFormCode = formElements[0].analyte_name_code;
    }

    var items = smFormElements.map((item, index) => {
      var code = item.code;
      var nextCode = "";
      if (index == lastElement) {
        if (firstFormCode != "") {
          nextCode = firstFormCode;
        }
      } else {
        nextCode = smFormElements[index + 1].code;
      }

      if (code == "weight") {
        return (
          <View>
            <View style={{ flexDirection: "row", paddingTop: 20 }}>
              <Text style={[R.styles.highlightNormal, { alignSelf: "center" }]}>
                Weight
            </Text>
              <View style={{ flex: 1 }} />
              {!this.state.editing ?
                <MaterialIcon
                  size={24}
                  name={"delete"}
                  color={R.colors.darkGrey}
                  style={{ marginTop: 10 }}
                  onPress={() => this.onDeletePressedSmForm(index)}
                />
                : null}
            </View>
            <View style={{ flexDirection: "row" }}>
              <CheckBox
                style={{ paddingTop: 10, flex: 1 }}
                onClick={() => {
                  this.setState({
                    weightMetric: true
                  });
                }}
                isChecked={this.state.weightMetric}
                rightText={"Kgs"}
                checkBoxColor={R.colors.darkGrey}
                rightTextStyle={R.styles.darkNormal}
              />
              <CheckBox
                style={{ padding: 10, flex: 2 }}
                onClick={() => {
                  this.setState({
                    weightMetric: false
                  });
                }}
                isChecked={!this.state.weightMetric}
                rightText={"Stones/lbs"}
                checkBoxColor={R.colors.darkGrey}
                rightTextStyle={R.styles.darkNormal}
              />
            </View>
            {this.state.weightMetric ? (
              <TextField
                inputContainerStyle={R.styles.textcontainer}
                inputContainerPadding={R.constants.textFieldLabelHeight}
                labelHeight={R.constants.textFieldLabelHeight}
                labelTextStyle={R.styles.textFieldLabelText}
                tintColor={R.colors.nhsblue}
                keyboardType="number-pad"
                value={data.weight}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={text => {
                  this.onWeightChangeText(text, "weight");
                }}
                onSubmitEditing={() => {
                  nextCode == ""
                    ? this.onSubmit()
                    : this.focusTheField(nextCode);
                }}
                returnKeyType={nextCode == "" ? "done" : "next"}
                ref={input => {
                  this.inputs[code] = input;
                }}
                label="Weight (kgs)"
                error={errors.weight}
              />
            ) : (
                <TextField
                  inputContainerStyle={R.styles.textcontainer}
                  inputContainerPadding={R.constants.textFieldLabelHeight}
                  labelHeight={R.constants.textFieldLabelHeight}
                  labelTextStyle={R.styles.textFieldLabelText}
                  tintColor={R.colors.nhsblue}
                  keyboardType="number-pad"
                  value={data.weightStone}
                  autoCapitalize="none"
                  autoCorrect={false}
                  enablesReturnKeyAutomatically={true}
                  onFocus={this.onFocus}
                  onChangeText={text => {
                    this.onWeightChangeText(text, "weightStone");
                  }}
                  onSubmitEditing={() => {
                    this.focusTheField("weight_pounds");
                  }}
                  ref={input => {
                    this.inputs[code] = input;
                  }}
                  returnKeyType="next"
                  label="Weight Stone"
                  error={errors.weightStone}
                />
              )}
            {this.state.weightMetric ? null : (
              <TextField
                inputContainerStyle={R.styles.textcontainer}
                inputContainerPadding={R.constants.textFieldLabelHeight}
                labelHeight={R.constants.textFieldLabelHeight}
                labelTextStyle={R.styles.textFieldLabelText}
                tintColor={R.colors.nhsblue}
                keyboardType="number-pad"
                value={data.weightPounds}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={text => {
                  this.onWeightChangeText(text, "weightPounds");
                }}
                onSubmitEditing={() => {
                  nextCode == ""
                    ? this.onSubmit()
                    : this.focusTheField(nextCode);
                }}
                returnKeyType={nextCode == "" ? "done" : "next"}
                ref={input => {
                  this.inputs["weight_pounds"] = input;
                }}
                label="Weight pounds"
                error={errors.weightPounds}
              />
            )}
          </View>
        );
      }

      return (
        <View>
          <View style={{ flexDirection: "row", paddingTop: 20, paddingBottom: 10 }}>
            <Text style={[R.styles.highlightNormal, { alignSelf: "center" }]}>
              {item.name}
            </Text>
            <View style={{ flex: 1 }} />
            {!this.state.editing ?
              <MaterialIcon
                size={24}
                name={"delete"}
                color={R.colors.darkGrey}
                style={{ marginTop: 10 }}
                onPress={() => this.onDeletePressedSmForm(index)}
              />
              : null}
          </View>
          <View style={{ flexDirection: "row" }}>
            <View style={{ flex: 3 }}>
              <TextField
                inputContainerStyle={R.styles.textcontainer}
                inputContainerPadding={R.constants.textFieldLabelHeight}
                labelHeight={R.constants.textFieldLabelHeight}
                labelTextStyle={R.styles.textFieldLabelText}
                tintColor={R.colors.nhsblue}
                keyboardType="number-pad"
                value={smData[code]}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={text => {
                  this.onChangeText(text, code);
                }}
                onSubmitEditing={() => {
                  nextCode == "" ? this.onSubmit() : this.focusTheField(nextCode);
                }}
                returnKeyType={nextCode == "" ? "done" : "next"}
                ref={input => {
                  this.inputs[code] = input;
                }}
                label={item.name}
                error={errors[code]}
              />
            </View>
            <Text style={[R.styles.darkNormal, { alignSelf: "center", width: 100, marginLeft: 10 }]}>
              {item.units}
            </Text>
          </View>
        </View>
      );
    });

    return items;
  };

  render() {
    let {
      errors = {},
      secureTextEntry,
      missingFields,
      missingFieldsPicked,
      missingFieldsVisible,
      ...data
    } = this.state;

    return (
      <ImageBackground
        source={require("../../assets/images/grey-bg.jpg")}
        style={R.styles.backgroundImage}
      >
        <SafeAreaView style={R.styles.container}>
          <KeyboardAwareScrollView
            horizontal={false}
            style={R.styles.scrollcontainer}
            keyboardDismissMode="none"
            keyboardShouldPersistTaps="handled"
          >
            <Loader loading={this.props.isLoading} message="Saving" />
            <Text style={[R.styles.highlightNormal]}>
              Date of test
              </Text>
            {this.state.editing ?
              <Text>
                {getLocalDateString(this.state.date)}
              </Text>
              :
              <TouchableOpacity
                onPress={this._showDatePicker}
                style={{ width: "100%", marginBottom: 10 }}
              >
                <Text style={R.styles.textcontainer}>
                  {this.state.date
                    ? getLocalDateString(this.state.date)
                    : "Date of test"}
                </Text>
              </TouchableOpacity>
            }

            <DateTimePicker
              isVisible={this.state.isDatePickerVisible}
              onConfirm={this._handleDatePicked}
              onCancel={this._hideDatePicker}
              datePickerModeAndroid="spinner"
              mode="date"
              date={this.state.date}
            />
            {errors.date ? (
              <Text style={[R.styles.normal, { color: "red" }]}>
                {errors.date}
              </Text>
            ) : null}

            <Text style={[R.styles.highlightNormal]}>
              Time of test
              </Text>
            {this.state.editing ?
              <Text>
                {getLocalTimeString(this.state.date)}
              </Text>
              :
              <TouchableOpacity
                onPress={this._showTimePicker}
                style={{ width: "100%", marginBottom: 10 }}
              >
                <Text style={R.styles.textcontainer}>
                  {this.state.date
                    ? getLocalTimeString(this.state.date)
                    : "Time of test"}
                </Text>
              </TouchableOpacity>
            }

            <DateTimePicker
              isVisible={this.state.isTimePickerVisible}
              onConfirm={this._handleDatePicked}
              onCancel={this._hideTimePicker}
              datePickerModeAndroid="spinner"
              mode="time"
              date={this.state.date}
            />
            {errors.date ? (
              <Text style={[R.styles.normal, { color: "red" }]}>
                {errors.date}
              </Text>
            ) : null}

            {!this.state.editing ?
              <Text style={[R.styles.highlightNormal, { paddingTop: 20, paddingBottom: 10 }]}>
                Add a new test type
            </Text>
              : null}

            {!this.state.editing ?
              <View style={{ flexDirection: "row" }}>
                <TouchableOpacity
                  onPress={this.onMissingFieldsShow}
                  style={R.styles.textcontainernarrow}
                >
                  <Text style={[R.styles.normal, { color: R.colors.darkGrey }]}>
                    {missingFieldsPicked
                      ? missingFieldsPicked.label
                      : "Add a new test type"}
                  </Text>
                </TouchableOpacity>
                <View style={{ marginBottom: 10, marginLeft: 10 }}>
                  <GradientButton
                    style={R.styles.button}
                    onPress={() => {
                      this.onMissingFieldsAdd();
                    }}
                    title="Add"
                    titleColor="white"
                    disabled={missingFieldsPicked == null}
                  />
                </View>
              </View>
              : null}

            {!this.state.editing ?
              <ModalFilterPicker
                visible={missingFieldsVisible}
                onSelect={this.onMissingFieldsSelect}
                onCancel={this.onMissingFieldsCancel}
                options={missingFields}
              />
              : null}

            {this.smResultsFieldOutput()}
            {this.testResultsFieldsOutput()}


            <GradientButton
              style={[R.styles.button, { marginBottom: 40 }]}
              onPress={() => {
                this.onSubmit();
              }}
              title="SAVE"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
            <View style={[{ height: 40 }]} />
          </KeyboardAwareScrollView>
        </SafeAreaView>
      </ImageBackground>
    );
  }
}

const mapStateToProps = state => {
  return {
    latestData: state.comms.patientData.latestResults,
    patientData: state.comms.patientData.patientData,
    height: state.local.height,
    hasError: state.comms.patientData.hasError,
    isLoading: state.comms.patientData.isLoading,
    errorMsg: state.comms.patientData.errorMsg,
    updateSuccess: state.comms.patientData.updateSuccess,
    weightImperial: state.local.weightImperial
  };
};

const mapDispatchToProps = dispatch => {
  return {
    add: data => {
      dispatch(
        PatientDataActions.addTestResultReadings(
          null, // current user
          data.date.getTime(),
          data.testResults,
          data.selfmonitoringResults
        )
      );
    },
    update: (data, compositionId) => {
      dispatch(PatientDataActions.upateTestResultReadings(
        null, // current user
        data.date.getTime(),
        compositionId,
        data.testResults,
        data.selfmonitoringResults
      )
      );
    },
    clearSuccess: () => {
      dispatch(PatientDataActions.clearUpdateSuccess());
    },
    clearError: () => {
      dispatch(PatientDataActions.hasError(false, null));
    },
    fetchPatientData: () => {
      dispatch(PatientDataActions.fetchLatestReadings(null, SUPPORTED_FIELDS));
    },
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(AddPatientDataScreen);
