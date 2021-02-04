import React, { Component } from "react";
import {
  View,
  ImageBackground,
  Text,
  Button,
  TouchableOpacity,
  Alert,
  ScrollView
} from "react-native";
import { TextField } from "react-native-material-textfield";
import DateTimePicker from "react-native-modal-datetime-picker";
import CheckBox from "react-native-check-box";
import { RaisedTextButton } from "react-native-material-buttons";
import MaterialIcon from "react-native-vector-icons/MaterialIcons";

import { connect } from "react-redux";
import { AccountDetailsActions } from "nhsphysicalhealthcomms";
import { LoginActions } from "nhsphysicalhealthcomms";
import LocalDataActions from "../redux/actions/LocalDataActions";
import { getLocalDateString } from "../utils/DateUtils";
import { SafeAreaView } from "react-navigation";
import HandleHardwareBackButton from "../utils/HandleHardWareBackButton";
import R from "res/R";
import GradientButton from "../utils/GradientButton";

import {KeyboardAwareScrollView} from 'react-native-keyboard-aware-scrollview';
import { HeaderBackButton } from 'react-navigation';

import Loader from "../utils/Loader";

const fields = ["firstName", "surname", "email", "password", "confirmPassword"];

class RegistrationScreenOne extends Component {
  static propTypes = {};

  static defaultProps = {};

  static navigationOptions = ({navigation}) => ({
    title: "Your details",
    headerLeft: <HeaderBackButton onPress={() => navigation.navigate('Auth')} />
  });

  constructor(props) {
    super(props);
    this.firstNameRef = this.updateRef.bind(this, "firstName");
    this.surnameRef = this.updateRef.bind(this, "surname");
    this.passwordRef = this.updateRef.bind(this, "password");
    this.confirmPasswordRef = this.updateRef.bind(this, "confirmPassword");
    this.emailRef = this.updateRef.bind(this, "email");

    this.onSubmit = this.onSubmit.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.onError = this.onError.bind(this);
    this.onAccessoryPress = this.onAccessoryPress.bind(this);
    this.renderPasswordAccessory = this.renderPasswordAccessory.bind(this);

    this.state = {
      firstName: "",
      surname: "",
      dateOfBirth: null,
      email: "",
      password: "",
      confirmPassword: "",
      gender: "",

      secureTextEntry: true,
      loading: false
    };
  }

  _showDOBPicker = () => this.setState({ isDOBPickerVisible: true });

  _hideDOBPicker = () => this.setState({ isDOBPickerVisible: false });

  _handleDOBPicked = date => {
    console.log("A DOB has been picked: ", date);

    // Remove any error message
    let { errors = {} } = this.state;
    delete errors["dob"];
    this.setState({ errors });

    this.setState({
      dateOfBirth: date
    });
    this._hideDOBPicker();
  };

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
    fields
      .map(name => ({ name, ref: this[name] }))
      .forEach(({ name, ref }) => {
        if (ref.isFocused()) {
          console.log("FOCUSSED");
          console.log(name);
          this.setState({ [name]: text });
        }
      });
  }

  updateRef(name, ref) {
    this[name] = ref;
  }

  onSubmit() {
    let errors = {};
    let noErrors = true;

    fields.forEach(name => {
      let value = this[name].value();

      if (!value) {
        noErrors = false;
        errors[name] = "Should not be empty";
      }
    });

    // Validate a date of birth has been selected
    if (this.state.dateOfBirth == null) {
      noErrors = false;
      errors["dob"] = "Date of birth required";
    }

    // Validate gender has been chosen
    if ( this.state.gender == ""){
      noErrors = false;
      errors["gender"] = "Gender required";
    }

    // Check passwords match
    if ( this.state.password != this.state.confirmPassword ){
      noErrors = false;
      errors['password'] = 'Passwords must match';
      errors['confirmPassword'] = 'Passwords must match';
    } else if ( this.state.password.length < 5 ){
      noErrors = false;
      errors['password'] = 'Password must be at least 5 character long';
      errors['confirmPassword'] = errors['password'];
    } else {
      var reg = new RegExp("(?=.*[a-z])(?=.*[A-Z])");
      if ( !reg.test(this.state.password)){
        noErrors = false;
      errors['password'] = 'Passwords must contain a mix of upper and lower case characters';
      errors['confirmPassword'] = errors['password'];
      }
    }

    this.setState({ errors });

    if (noErrors) {
      this.props.register(this.state.email, this.state.password);
    }
  }

  componentDidMount() {
    // Clear any errors
    this.props.clearError();
    this.props.clearAccountSuccess();
  }

  componentDidUpdate(prevProps) {
    if (this.props.accountUpdateSuccess && !prevProps.accountUpdateSuccess) {
      this.props.navigation.replace("RegistrationScreenTwo");
    } else if (this.props.hasError && !prevProps.hasError){
        var errorMessage = this.props.errorMessage;
        if ( errorMessage == null ){
           errorMessage = "Unable to save registration data, please check you have a network connection and retry";
        } else {
          var errors = errorMessage.message.split(": ");
          if ( errors.length > 0){
            errorMessage = errors[1];
          }

        if ( this.props.errorMessage.validationErrors != null){
          errorMessage = "";

          this.props.errorMessage.validationErrors.map((item, index) => {
            var title = item.field == "emailAddress" ? "Email Address" : "Password";
            errorMessage = errorMessage + title + ": " + item.message + "\n";
          });
       }
      }

        if ( errorMessage == null || errorMessage.length == 0 ){
          errorMessage = "Unable to save registration data, please check you have a network connection and retry";
        }

        this.onError(errorMessage)
      } else if (!prevProps.accountHasError && this.props.accountHasError)
     {
      this.onError(this.props.accountErrorMessage == null ? "Unable to save registration data, please check you have a network connection and retry" : this.props.accountErrorMessage.message);
      
    } else if (this.props.isLogged && !prevProps.isLogged) {
      console.log("Register");

      this.props.storeEmail(this.state.email);

      var datestring =
        this.state.dateOfBirth.getFullYear() +
        "-" +
        ("0" + (this.state.dateOfBirth.getMonth() + 1)).slice(-2) +
        "-" +
        ("0" + this.state.dateOfBirth.getDate()).slice(-2);

      // Upload account information
      var updatedData = {
        title: "null",
        firstNames: this.state.firstName,
        lastName: this.state.surname,
        dateOfBirth: datestring,
        address: "",
        mobileNumber: "",
        telNumber: "",
        gender: this.state.gender,
        nhsNumber: ""
      };
      this.props.add(updatedData);
    }
  }

  onError(errorMessage) {
    Alert.alert(
      "",
      errorMessage,
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
  }

  onBack = () => {
    this.props.navigation.navigate("Auth");
    return true;
  }

  onAccessoryPress() {
    this.setState(({ secureTextEntry }) => ({
      secureTextEntry: !secureTextEntry
    }));
  }

  renderPasswordAccessory() {
    let { secureTextEntry } = this.state;

    let name = secureTextEntry ? "visibility" : "visibility-off";

    return (
      <MaterialIcon
        size={24}
        name={name}
        color={TextField.defaultProps.baseColor}
        onPress={this.onAccessoryPress}
        suppressHighlighting
      />
    );
  }

  render() {
    let { errors = {}, secureTextEntry, ...data } = this.state;

    var genderError = errors.gender && this.state.gender == "";

    var loading = (this.props.isLoading ? this.props.isLoading : false)
                  || (this.props.accountLoading ? this.props.accountLoading : false);

    return (
      <HandleHardwareBackButton onBack={this.onBack}>
        <SafeAreaView style={R.styles.container}>
          <KeyboardAwareScrollView
          horizontal={false}
          style={R.styles.scrollcontainer}
          keyboardDismissMode="none"
          keyboardShouldPersistTaps="handled"
          >
          <Loader loading={loading} message="Saving" />
            <View style={R.styles.container}>
              <Text style={R.styles.headerWhite}>
                Please enter your details to continue.
              </Text>

              <TextField
                inputContainerStyle={R.styles.textcontainer}
                inputContainerPadding={R.constants.textFieldLabelHeight}
                labelHeight={R.constants.textFieldLabelHeight}
                labelTextStyle={R.styles.textFieldLabelText}
                tintColor={R.colors.nhsblue}
                ref={this.firstNameRef}
                value={data.firstName}
                autoCapitalize="words"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                onSubmitEditing={() => {this.surname.focus()}}
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
                autoCapitalize="words"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                onSubmitEditing={() => {this.email.focus()}}
                returnKeyType="next"
                label="Surname"
                error={errors.surname}
              />

              <TextField
                inputContainerStyle={R.styles.textcontainer}
                inputContainerPadding={R.constants.textFieldLabelHeight}
                labelHeight={R.constants.textFieldLabelHeight}
                labelTextStyle={R.styles.textFieldLabelText}
                tintColor={R.colors.nhsblue}
                ref={this.emailRef}
                value={data.email}
                keyboardType="email-address"
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                onSubmitEditing={() => {this.password.focus()}}
                returnKeyType="next"
                label="Email Address"
                error={errors.email}
              />
              <Text style={{
     color: R.colors.darkGrey, marginTop: 10
     }}>
Password must be at least 5 characters long and a mix of upper and lower case letters      
              </Text>
              <TextField
                inputContainerStyle={R.styles.textcontainer}
                inputContainerPadding={R.constants.textFieldLabelHeight}
                labelHeight={R.constants.textFieldLabelHeight}
                labelTextStyle={R.styles.textFieldLabelText}
                tintColor={R.colors.nhsblue}
                ref={this.passwordRef}
                value={data.password}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                clearTextOnFocus={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                onSubmitEditing={() => {this.confirmPassword.focus()}}
                returnKeyType="next"
                label="Password"
                error={errors.password}
                secureTextEntry={secureTextEntry}
                renderAccessory={this.renderPasswordAccessory}
              />
            <TextField
                inputContainerStyle={R.styles.textcontainer}
                inputContainerPadding={R.constants.textFieldLabelHeight}
                labelHeight={R.constants.textFieldLabelHeight}
                labelTextStyle={R.styles.textFieldLabelText}
                tintColor={R.colors.nhsblue}
                ref={this.confirmPasswordRef}
                value={data.confirmPassword}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                clearTextOnFocus={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                returnKeyType="next"
                label="Confirm Password"
                error={errors.confirmPassword}
                secureTextEntry={secureTextEntry}
                renderAccessory={this.renderPasswordAccessory}
              />

              <TouchableOpacity onPress={this._showDOBPicker}>
                <TextField
                  inputContainerStyle={[R.styles.textcontainer, {marginTop:15}]}
                  inputContainerPadding={R.constants.textFieldLabelHeight}
                  labelHeight={R.constants.textFieldLabelHeight}
                  labelTextStyle={R.styles.textFieldLabelText}
                  tintColor={R.colors.nhsblue}
                  value={
                    this.state.dateOfBirth
                      ? getLocalDateString(this.state.dateOfBirth)
                      : ""
                  }
                  returnKeyType="done"
                  label="Date of birth"
                  error={errors.dob}
                  editable={false}
                />
              </TouchableOpacity>

              <DateTimePicker
                isVisible={this.state.isDOBPickerVisible}
                onConfirm={this._handleDOBPicked}
                onCancel={this._hideDOBPicker}
                datePickerModeAndroid="spinner"
                mode="date"
              />

              <View style={{ flexDirection: "row" }}>
                <CheckBox
                  style={{ padding: 20, flex: 1 }}
                  onClick={() => {
                    this.setState({
                      gender: "MALE"
                    });
                  }}
                  isChecked={this.state.gender == "MALE"}
                  rightText={"MALE"}
                  checkBoxColor={genderError ? "red" : "darkGrey"}
                  rightTextStyle={{ color: R.styles.darkNormal }}
                />
                <CheckBox
                  style={{ padding: 20, flex: 1 }}
                  onClick={() => {
                    this.setState({
                      gender: "FEMALE"
                    });
                  }}
                  isChecked={this.state.gender == "FEMALE"}
                  rightText={"FEMALE"}
                  checkBoxColor={genderError ? "red" : "darkGrey"}
                  rightTextStyle={{ color: R.styles.darkNormal}}
                />
              </View>
              <View style={{ flexDirection: "row" }}>
                <CheckBox
                  style={{ padding: 20, flex: 1 }}
                  onClick={() => {
                    this.setState({
                      gender: "OTHER"
                    });
                  }}
                  isChecked={this.state.gender == "OTHER"}
                  rightText={"OTHER"}
                  checkBoxColor={genderError ? "red" : "darkGrey"}
                  rightTextStyle={{ color: R.styles.darkNormal }}
                />
                <CheckBox
                  style={{ padding: 20, flex: 1 }}
                  onClick={() => {
                    this.setState({
                      gender: "UNKNOWN"
                    });
                  }}
                  isChecked={this.state.gender == "UNKNOWN"}
                  rightText={"UNKNOWN"}
                  checkBoxColor={genderError ? "red" : "darkGrey"}
                  rightTextStyle={{ color: R.styles.darkNormal }}
                />
              </View>
              {genderError ? 
              <Text style={{color: "red"}}
              >Gender required</Text>
              : null
              }

              <GradientButton
                style={R.styles.button}
                onPress={() => {
                  this.onSubmit();
                }}
                title="Next Page"
                color={R.colors.highlightColorOne}
                titleColor="white"
              />
              <View style={{ marginBottom: 40 }} />
            </View>
          </KeyboardAwareScrollView>
        </SafeAreaView>
      </HandleHardwareBackButton>
    );
  }
}

const mapStateToProps = state => {
  return {
    isLogged: state.comms.login.isLogged,
    hasError: state.comms.login.hasError,
    isLoading: state.comms.login.isLoading,
    errorMessage: state.comms.login.errorMessage,
    accountUpdateSuccess: state.comms.accountData.accountUpdateSuccess,
    accountLoading: state.comms.accountData.isLoading,
    accountHasError: state.comms.accountData.hasError,
    accountErrorMessage: state.comms.accountData.errorMessage
  };
};

const mapDispatchToProps = dispatch => {
  return {
    add: newAccountData => {
      dispatch(AccountDetailsActions.saveUserAccount(newAccountData));
    },
    register: (email, password) => {
      dispatch(LoginActions.register(email, email, password, "PATIENT"));
    },
    clearError: () => {
      dispatch(LoginActions.loginHasError(false));
      dispatch(AccountDetailsActions.userAccountHasError(false));
    },
    clearAccountSuccess: () => {
      dispatch(AccountDetailsActions.clearUserAccountSuccess());
    },
    storeEmail: (email) => {
      dispatch(LocalDataActions.storeUsername(email));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(RegistrationScreenOne);
