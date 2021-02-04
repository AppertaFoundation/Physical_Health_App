import React, { Component } from "react";
import {
  View,
  ImageBackground,
  Text,
  Button,
  Alert,
  ScrollView
} from "react-native";
import { TextField } from "react-native-material-textfield";
import CheckBox from "react-native-check-box";
import { connect } from "react-redux";
import { RaisedTextButton } from "react-native-material-buttons";
import {HcpDataActions} from 'nhsphysicalhealthcomms';
import {AccountDetailsActions} from 'nhsphysicalhealthcomms';
import { SafeAreaView } from "react-navigation";
import HandleHardwareBackButton from "../utils/HandleHardWareBackButton";
import GradientButton from "../utils/GradientButton";

import {KeyboardAwareScrollView} from 'react-native-keyboard-aware-scrollview';

import Loader from "../utils/Loader";

import R from "res/R";

// Keep pattern of multiple fields, in case
// more fields are added.
const fields = ["doctorId"];

class RegistrationScreenThree extends Component {
  static propTypes = {};

  static defaultProps = {};

  static navigationOptions = {
    title: "Your clinicians",
    headerLeft: null
  };

  constructor(props) {
    super(props);

    this.onSubmit = this.onSubmit.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onChangeText = this.onChangeText.bind(this);

    this.doctorIdRef = this.updateRef.bind(this, "doctorId");

    this.state = {
      doctorId: "",
      doctorIdResult: "",
      authorisationOne: false,
      authorisationTwo: false,

      loading: false
    };
  }

  componentDidMount() {
    // Clear any errors
    this.props.clearError();
    this.props.clearAccountSuccess();
  }

  componentDidUpdate(prevProps){
    if (this.props.hasError){
      // Display error within alert
      Alert.alert(
        "",
        "Unable to save registration data, please check you have a network connection and retry",
        [
          {text: 'OK', onPress: () => {this.props.clearError()}},
        ],
        {cancelable: false},
      );
    } else if (this.props.accountDoctors.careProfessionals.length == 1 && prevProps.accountDoctors.careProfessionals.length == 0) {
      this.props.navigation.navigate("App");
      this.props.clearAccountSuccess();
    }
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
    fields
      .map(name => ({ name, ref: this[name] }))
      .forEach(({ name, ref }) => {
        if (ref.isFocused()) {
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

    this.setState({ errors });

    if (noErrors) {
      let value = this.state.doctorId;
      this.props.lookUpDoctor(value);
    }
  }

  async onRegister() {
    if ( this.props.doctors.careProfessionals.length > 0 ){
      const newList = [].concat(this.props.doctors.careProfessionals);
      newList.push(this.props.hcpLookupResult);
      this.props.updateDoctors(newList, newList[0] );
    } else {
      this.props.navigation.navigate("App");
    }
  }

  onBack = () => {
    this.props.navigation.navigate("Auth");
    return true;
  }

  render() {
    let { errors = {}, secureTextEntry, ...data } = this.state;

    var doctorsName = "";
    var doctorLookup = this.props.hcpLookupResult;

    if ( doctorLookup ){
      doctorsName = doctorLookup.title + "  " + doctorLookup.firstNames + " " + doctorLookup.lastName;
    }

    var idError = errors.doctorId;
    if ( idError == null){
      idError = this.props.notFound ? "ID not found" : null;
    }

    return (
      <HandleHardwareBackButton onBack={this.onBack}>
        <SafeAreaView style={R.styles.container}>
      <KeyboardAwareScrollView horizontal={false} style={R.styles.scrollcontainer}>
      <Loader loading={this.props.isLoading ? this.props.isLoading : false } message="Saving" />
      <Loader loading={this.props.isSearching ? this.props.isSearching : false } message="Searching" />
        <View style={R.styles.container}>
          <Text style={R.styles.headerWhite}>
            Please enter the ID for your primary clinician and then press the
            search button to look-up the clinician on our database. If
            authorised by you below then your clinician will be able to see the
            health data that you enter into this app. You may select more than
            one clinician in the app settings
          </Text>

          <TextField
             inputContainerStyle={R.styles.textcontainer}
             inputContainerPadding={R.constants.textFieldLabelHeight}
             labelHeight={R.constants.textFieldLabelHeight}
             labelTextStyle={R.styles.textFieldLabelText}
             tintColor={R.colors.nhsblue}
            ref={this.doctorIdRef}
            value={data.doctorId}
            autoCapitalize="none"
            autoCorrect={false}
            enablesReturnKeyAutomatically={true}
            onFocus={this.onFocus}
            onChangeText={this.onChangeText}
            onSubmitEditing={this.onSubmit}
            returnKeyType="search"
            label="Clinician ID"
            error={idError}
          />

          <RaisedTextButton
            style={R.styles.button}
            onPress={() => {
              this.onSubmit();
            }}
            title="Search for my clinician"
            color="gray"
            titleColor="white"
          />

          <Text
            style={[
              R.styles.body,
              {
                backgroundColor: "orange",
                height: 40,
                marginTop: 20,
                marginBottom: 20,
                color: "white"
              }
            ]}
          >
            Your clinician is: {doctorsName}
          </Text>

          <CheckBox
            style={{ padding: 10 }}
            onClick={() => {
              this.setState({
                authorisationOne: !this.state.authorisationOne
              });
            }}
            isChecked={this.state.authorisationOne}
            rightText={
              "I authorise any clinicians approved by me in this app to view all of the data entered"
            }
            checkBoxColor="darkGrey"
            rightTextStyle={{ color: R.styles.darkNormal }}
          />

          <CheckBox
            style={{ padding: 10 }}
            onClick={() => {
              this.setState({
                authorisationTwo: !this.state.authorisationTwo
              });
            }}
            isChecked={this.state.authorisationTwo}
            rightText={
              "I authorise for the data to be used anonymously for research purposes"
            }
            checkBoxColor="darkGrey"
            rightTextStyle={{ color: R.styles.darkNormal }}
          />

          <GradientButton
            style={R.styles.button}
            onPress={async () => {this.onRegister()}}
            title="Register"
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
  console.log(state);
  return {
    doctors: state.comms.accountData.doctors,
    hcpLookupResult: state.comms.hcpData.hcpSearchResult,
    hasError : state.comms.accountData.hasError,
    isLoading: state.comms.accountData.isLoading,
    isSearching: state.comms.hcpData.isLoading,
    notFound: state.comms.hcpData.hasError,
    accountDoctors: state.comms.accountData.doctors
  };
};

const mapDispatchToProps = dispatch => {
  return {
    updateDoctors: (careProf, primaryCareProf) => {
      dispatch(AccountDetailsActions.saveDoctors(careProf, primaryCareProf));
    },
    lookUpDoctor: doctorId => {
      dispatch(HcpDataActions.searchForHCP(doctorId));
    },
    clearError: ()=> {
      dispatch(AccountDetailsActions.userAccountHasError(false));
    },
    clearAccountSuccess: () => {
      dispatch(AccountDetailsActions.clearUserAccountSuccess());
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(RegistrationScreenThree);
