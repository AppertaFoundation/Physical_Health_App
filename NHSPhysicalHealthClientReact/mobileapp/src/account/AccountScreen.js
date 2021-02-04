import React, { Component } from "react";
import {
  View,
  Text,
  Button,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  Alert
} from "react-native";
import { TextField } from "react-native-material-textfield";
import DateTimePicker from "react-native-modal-datetime-picker";
import CheckBox from "react-native-check-box";
import { connect } from "react-redux";
import { AccountDetailsActions } from "nhsphysicalhealthcomms";
import { PatientDataActions } from "nhsphysicalhealthcomms";
import LocalDataActions from "../redux/actions/LocalDataActions";

import { RaisedTextButton } from "react-native-material-buttons";
import Loader from "../utils/Loader";
import {dateToStateString} from "../utils/DateUtils";
import {getLocalDateString} from "../utils/DateUtils";
import GradientButton from "../utils/GradientButton";
import { selfMonitoringFields as SelfMonitoringFields } from "../dashboard/TestResultFields";

import {KeyboardAwareScrollView} from 'react-native-keyboard-aware-scrollview';

import R from "res/R";

const fields = ["title", "firstName", "surname", "password", "passwordConfirm", "height", "heightFoot", "heightInches"];
const submitFields = ["firstName", "surname"];

class AccountScreen extends Component {
  static propTypes = {};

  static defaultProps = {};

  static navigationOptions = {
    title: "Registration One"
  };

  constructor(props) {
    super(props);
    this.titleRef = this.updateRef.bind(this, "title");
    this.firstNameRef = this.updateRef.bind(this, "firstName");
    this.surnameRef = this.updateRef.bind(this, "surname");
    this.passwordRef = this.updateRef.bind(this, "password");
    this.passwordConfirmRef = this.updateRef.bind(this, "passwordConfirm");

    this.heightFootRef = this.updateRef.bind(this, "heightFoot");
    this.heightInchesRef = this.updateRef.bind(this, "heightInches");
    this.heightRef = this.updateRef.bind(this, "height");

    this.onSubmit = this.onSubmit.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onChangeText = this.onChangeText.bind(this);

    this.state = {
      title:  "null",
      firstName:  "" ,
      surname:  "" ,
      dateOfBirth: "" ,
      gender: "" ,

      secureTextEntry: true,
      loading: false,

      password: "",
      passwordConfirm: "",

      heightMetric: true,
      heightFoot: "",
      heightInches: "",
      height: "",

      weightImperial: true
    };
  }

  componentDidMount() {
    this.props.clearFetchSuccess();
    this.props.getAccountData();

//    if (this.props.latestPatientData.length === 0) {
//      this.props.fetchPatientData();
 //   }

  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.props.hasError) {
      // Display error within alert
      Alert.alert(
        "",
        "Unable to update account details, please check you have a network connection and retry",
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
    } else if (this.props.fetchSuccess && !prevProps.fetchSuccess ){
      console.log("Update account data");
      var accountData = this.props.accountData;
      // Account details loaded
      this.setState({
      title: accountData.title == null ? "" : accountData.title,
      firstName: accountData.firstNames == null ? "" : accountData.firstNames,
      surname: accountData.lastName == null ? "" : accountData.lastName,
      dateOfBirth:
        accountData.dateOfBirth == null ? "" : new Date(accountData.dateOfBirth),
      gender: accountData.gender == null ? "" : accountData.gender
      });

      var height = this.props.height;
      console.log(height);
      if ( height != null ){
          var realFeet = (height * 0.3937) / 12;
          var feet = Math.floor(realFeet);
          var inches = Math.round((realFeet - feet) * 12);

          this.setState({
            originalHeight: height,
            height: height,
            heightFoot: String(feet),
            heightInches: String(inches)
          });
      }

      this.setState({weightImperial: this.props.weightImperial});
    }

    /*
    if ( this.props.latestPatientData.length > 0 ){
        var heightField = SelfMonitoringFields["height"];
        var heightEntry = this.props.latestPatientData.find(item => item.code == "height");
        if ( heightEntry != null ){
          var height = heightEntry.value;
            var realFeet = (height * 0.3937) / 12;
            var feet = Math.floor(realFeet);
            var inches = Math.round((realFeet - feet) * 12);

            this.setState({
              originalHeight: height,
              height: height,
              heightFoot: String(feet),
              heightInches: String(inches)
            });
        }
        

    }
    */
  }

  _showDOBPicker = () => this.setState({ isDOBPickerVisible: true });

  _hideDOBPicker = () => this.setState({ isDOBPickerVisible: false });

  _handleDOBPicked = date => {
    console.log("A DOB has been picked: ", date);
    this.setState({
      dateOfBirth: date
    });
    this._hideDOBPicker();

    // Remove any error
    let { errors = {} } = this.state;
    delete errors["dateOfBirth"];
    this.setState({ errors });
  };

  _handleGenderPicked = gender => {
    this.setState({gender: gender});

    // Remove any error
    let { errors = {} } = this.state;
    delete errors["gender"];
    this.setState({ errors });
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

  onChangeText(text) {
    console.log(text);
    fields
      .map(name => ({ name, ref: this[name] }))
      .forEach(({ name, ref }) => {
        if (ref && ref.isFocused()) {
          this.setState({ [name]: text });

          if (name == "height") {
            console.log("*** HEIGHT");
            console.log(text);
            var cms = parseInt(text) || 0;
            var realFeet = (cms * 0.3937) / 12;
            var feet = Math.floor(realFeet);
            var inches = Math.round((realFeet - feet) * 12);

            this.setState({
              heightFoot: String(feet),
              heightInches: String(inches)
            });
          } else if (name == "heightFoot" || name == "heightInches") {
            var feet = parseInt(name == "heightFoot" ? text : this.state.heightFoot) || 0;
            var inches = parseInt(name == "heightInches" ? text : this.state.heightInches) || 0;
            var cms = Math.round(feet * 30.48 + inches * 2.54);

            this.setState({
              height: String(cms)
            });
          } 
        }
      });
  }

  updateRef(name, ref) {
    this[name] = ref;
  }

  onSubmit() {
    let errors = {};
    let noErrors = true;

    console.log("************* HERE 1");

    submitFields.forEach(name => {
      console.log("************* HERE 2 - " + name);
      let value = this[name].value();

      if (!value) {
        noErrors = false;
        errors[name] = "Should not be empty";
      }
    });

    console.log("************* HERE 3");

    // Validate a date of birth has been selected
    if (!this.state.dateOfBirth) {
      noErrors = false;
      errors["dateOfBirth"] = "Date of Birth required";
    }

    // Validate gender has been chosen
    if (!this.state.gender) {
      noErrors = false;
      errors["gender"] = "Gender required";
    }

    this.setState({ errors });

    var dateOfBirth =  dateToStateString(this.state.dateOfBirth);
    console.log(dateOfBirth);

    if (noErrors) {
       var dateOfBirth =  dateToStateString(this.state.dateOfBirth);

      var updatedData = {
        title: this.state.title,
        firstNames: this.state.firstName,
        lastName: this.state.surname,
        dateOfBirth: dateOfBirth,
        address: "",
        mobileNumber: "",
        telNumber: "",
        gender: this.state.gender,
        nhsNumber: "",
      };

      this.props.update(updatedData);
    }

    if ( this.state.height != this.state.originalHeight){
      // Height has changed
      this.props.addHeight(this.state.height);
    }

    if ( this.state.weightImperial != this.props.weightImperial ){
      // Weight metric setting changed
      this.props.setDisplayWeightImperial(this.state.weightImperial);
    }

  }

  onChangePassword() {
    console.log("OnChangePassword");

    let errors = {};
    let noErrors = true;

    if ( this.state.password == ""){
      noErrors = false;
      errors["password"] = "Should not be empty";
    }

    if (this.state.passwordConfirm == "" ){
      noErrors = false;
      errors["password"] = "Should not be empty";
    }

    if (noErrors && this.state.password !== this.state.passwordConfirm){
      noErrors = false;
      errors["password"] = "Password should match";
      errors["passwordConfirm"] = "Password should match";
    }

    this.setState({ errors });

    console.log(this.state);

    if (noErrors) {
      this.props.changePassword(this.state.password);
    }
  }

  render() {
    let { errors = {}, secureTextEntry, ...data } = this.state;

    return (
      <KeyboardAwareScrollView horizontal={false} style={[R.styles.scrollcontainer, {backgroundColor: R.colors.mainBackground}]}>
      <Loader loading={this.props.isLoading} message="Loading" />
      <Text style={[R.styles.highlightNormal, {paddingBottom:10}]}>Name</Text>
        <View style={{ paddingBottom: 40 }}>
          <TextField
            inputContainerStyle={R.styles.textcontainer}
            inputContainerPadding={R.constants.textFieldLabelHeight}
            labelHeight={R.constants.textFieldLabelHeight}
            labelTextStyle={R.styles.textFieldLabelText}
            tintColor={R.colors.nhsblue}
            ref={this.firstNameRef}
            value={data.firstName}
            autoCapitalize="none"
            autoCorrect={false}
            enablesReturnKeyAutomatically={true}
            onFocus={this.onFocus}
            onChangeText={this.onChangeText}
            onSubmitEditing={() => {this.surname.focus()}}
            blurOnSubmit={false}
            returnKeyType="next"
            label="First name"
            error={errors.firstName}
          />

          <TextField
            inputContainerStyle={R.styles.textcontainer}
            inputContainerPadding={R.constants.textFieldLabelHeight}
            labelHeight={R.constants.textFieldLabelHeight}
            labelTextStyle={R.styles.textFieldLabelText}
            tintColor={R.colors.nhsblue}
            ref={this.surnameRef}
            value={data.surname}
            autoCapitalize="none"
            autoCorrect={false}
            enablesReturnKeyAutomatically={true}
            onFocus={this.onFocus}
            onChangeText={this.onChangeText}
            blurOnSubmit={true}
            returnKeyType="next"
            label="Surname"
            error={errors.surname}
          />

          <Text style={[R.styles.highlightNormal, {marginTop:20}]}>Date of birth</Text>

          <TouchableOpacity
            onPress={this._showDOBPicker}
            style={{ width: 100 }}
          >
            <Text
              style={{
                width: 100,
                height: 40,
                paddingTop: 10,
                textAlign: "center",
                justifyContent: "center",
                backgroundColor: "white"
              }}
            >
              {this.state.dateOfBirth
                ? getLocalDateString(this.state.dateOfBirth)
                : "Select Date of birth"}
            </Text>
          </TouchableOpacity>

          <DateTimePicker
            isVisible={this.state.isDOBPickerVisible}
            onConfirm={this._handleDOBPicked}
            onCancel={this._hideDOBPicker}
            datePickerModeAndroid="spinner"
            mode="date"
          />

          {errors.dateOfBirth ? (
              <Text style={[R.styles.normal, { color: "red" }]}>
                {errors.dateOfBirth}
              </Text>
            ) : null}

          <Text style={[R.styles.highlightNormal, {marginTop:20}]}>Gender</Text>
          <View style={{ flexDirection: "row" }}>
            <CheckBox
              style={{ padding: 10, flex: 1 }}
              onClick={() => {
                this._handleGenderPicked("MALE");
              }}
              isChecked={this.state.gender == "MALE"}
              rightText={"Male"}
            />
            <CheckBox
              style={{ padding: 10, flex: 1 }}
              onClick={() => {
                this._handleGenderPicked("FEMALE");
              }}
              isChecked={this.state.gender == "FEMALE"}
              rightText={"Female"}
            />
          </View>
          <View style={{ flexDirection: "row" }}>
            <CheckBox
              style={{ padding: 10, flex: 1 }}
              onClick={() => {
                this._handleGenderPicked("OTHER");
              }}
              isChecked={this.state.gender == "OTHER"}
              rightText={"Other"}
            />
            <CheckBox
              style={{ padding: 10, flex: 1 }}
              onClick={() => {
                this._handleGenderPicked("UNKNOWN");
              }}
              isChecked={this.state.gender == "UNKNOWN"}
              rightText={"Unknown"}
            />
          </View>
          {errors.gender ? (
              <Text style={[R.styles.normal, { color: "red" }]}>
                {errors.gender}
              </Text>
            ) : null}
            <Text style={[R.styles.highlightNormal, {marginTop:20}]}>Height</Text>
            <View style={{ flexDirection: "row" }}>
              <CheckBox
                style={{ padding: 20, flex: 1 }}
                onClick={() => {
                  this.setState({
                    heightMetric: true
                  });
                }}
                isChecked={this.state.heightMetric}
                rightText={"cm"}
                checkBoxColor="darkGrey"
                rightTextStyle={{ color: R.styles.darkNormal }}
              />
              <CheckBox
                style={{ padding: 20, flex: 1 }}
                onClick={() => {
                  this.setState({
                    heightMetric: false
                  });
                }}
                isChecked={!this.state.heightMetric}
                rightText={"Feet/inches"}
                checkBoxColor="darkGrey"
                rightTextStyle={{ color: R.styles.darkNormal }}
              />
            </View>
            {this.state.heightMetric ? (
              <TextField
              inputContainerStyle={R.styles.textcontainer}
              inputContainerPadding={R.constants.textFieldLabelHeight}
              labelHeight={R.constants.textFieldLabelHeight}
              labelTextStyle={R.styles.textFieldLabelText}
              tintColor={R.colors.nhsblue}
                keyboardType="number-pad"
                ref={this.heightRef}
                value={String(data.height)}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                onSubmitEditing={this.onSubmit}
                returnKeyType="done"
                label="Height (cm)"
                error={errors.height}
              />
            ) : (
              <TextField
              inputContainerStyle={R.styles.textcontainer}
              inputContainerPadding={R.constants.textFieldLabelHeight}
              labelHeight={R.constants.textFieldLabelHeight}
              labelTextStyle={R.styles.textFieldLabelText}
              tintColor={R.colors.nhsblue}
                keyboardType="number-pad"
                ref={this.heightFootRef}
                value={data.heightFoot}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                onSubmitEditing={() => {this.heightInches.focus()}}
                returnKeyType="next"
                label="Height feet"
                error={errors.heightFoot}
              />
            )}
            {this.state.heightMetric ? null : (
              <TextField
              inputContainerStyle={R.styles.textcontainer}
              inputContainerPadding={R.constants.textFieldLabelHeight}
              labelHeight={R.constants.textFieldLabelHeight}
              labelTextStyle={R.styles.textFieldLabelText}
              tintColor={R.colors.nhsblue}
                keyboardType="number-pad"
                ref={this.heightInchesRef}
                value={data.heightInches}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                onSubmitEditing={this.onSubmit}
                returnKeyType="done"
                label="Height Inches"
                error={errors.heightInches}
              />
            )}
            <Text style={[R.styles.highlightNormal, {marginTop:20}]}>Weight display units</Text>
            <View style={{ flexDirection: "row" }}>
              <CheckBox
                style={{ padding: 20, flex: 1 }}
                onClick={() => {
                  this.setState({
                    weightImperial: false
                  });
                }}
                isChecked={!this.state.weightImperial}
                rightText={"Kgs"}
                checkBoxColor="darkGrey"
                rightTextStyle={{ color: R.styles.darkNormal }}
              />
              <CheckBox
                style={{ padding: 20, flex: 1 }}
                onClick={() => {
                  this.setState({
                    weightImperial: true
                  });
                }}
                isChecked={this.state.weightImperial}
                rightText={"Stone/pounds"}
                checkBoxColor="darkGrey"
                rightTextStyle={{ color: R.styles.darkNormal }}
              />
            </View>
<GradientButton
            style={R.styles.button}
            onPress={() => {
              this.onSubmit();
            }}
            title="Save"
            color={R.colors.highlightColorOne}
            titleColor="white"
          />
        <View style={{ marginBottom: 40 }} />
        </View>
      </KeyboardAwareScrollView>
    );
  }
}

const mapStateToProps = state => {
  console.log(state);
  return {
    accountData: state.comms.accountData.accountData,
    hasError: state.comms.accountData.hasError,
    isLoading: state.comms.accountData.isLoading,
    fetchSuccess: state.comms.accountData.accountFetchSuccess,
    // latestPatientData: state.comms.patientData.latestResults,
    height: state.local.height,
    weightImperial: state.local.weightImperial
  };
};

const mapDispatchToProps = dispatch => {
  return {
    update: newAccountData => {
      dispatch(AccountDetailsActions.saveUserAccount(newAccountData));
    },
    getAccountData: () => {
      dispatch(AccountDetailsActions.getUserAccount());
    },
    clearError: () => {
      dispatch(AccountDetailsActions.userAccountHasError(false));
    },
    clearFetchSuccess: () => {
      dispatch(AccountDetailsActions.clearUserFetchSuccess());
    },
    changePassword: (newPassword) => {
      dispatch(AccountDetailsActions.changePassword(newPassword));
    },
/*    addHeight: (height) => {
      var date = new Date();
      dispatch(
        PatientDataActions.addTestResultReadings(
          null, // current user
          date.getTime(),
          [],
          [{field:"height", value:String((height/100))}]
        )
      );
    },
*/
   addHeight: (height) => {
      dispatch(LocalDataActions.updateHeight(height));
   },  
   setDisplayWeightImperial: (weightImperial) => {
    dispatch(LocalDataActions.setDisplayWeightImperial(weightImperial));
   }  
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(AccountScreen);
