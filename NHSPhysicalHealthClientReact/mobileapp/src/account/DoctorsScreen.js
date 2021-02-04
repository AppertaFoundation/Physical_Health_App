import React, { Component } from "react";
import {
  StyleSheet,
  View,
  TextInput,
  FlatList,
  Text,
  Alert,
  ScrollView
} from "react-native";
import DoctorListItem from "./components/DoctorListItem";
import { connect } from "react-redux";
import { RaisedTextButton } from "react-native-material-buttons";
import { TextField } from "react-native-material-textfield";
import R from "res/R";
import { AccountDetailsActions } from "nhsphysicalhealthcomms";
import { HcpDataActions } from "nhsphysicalhealthcomms";
import Loader from "../utils/Loader";
import GradientButton from "../utils/GradientButton";

import {KeyboardAwareScrollView} from 'react-native-keyboard-aware-scrollview';

const fields = ["doctorId"];

class DoctorsScreen extends Component {
  state = {
    doctors: []
  };

  constructor(props) {
    super(props);

    this.onLookup = this.onLookup.bind(this);
    this.onSave = this.onSave.bind(this);

    this.onFocus = this.onFocus.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.onDelete = this.onDelete.bind(this);

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
    this.props.getDoctors();
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.props.hasError) {
      // Display error within alert
      Alert.alert(
        "",
        "Unable to update clinicians details, please check you have a network connection and retry",
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

  onLookup() {
    let errors = {};
    let noErrors = true;

    fields.forEach(name => {
      let value = this[name].value();

      if (!value || value == "") {
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

  onSave() {
      const newList = [].concat(this.props.doctors.careProfessionals);
      newList.push(this.props.hcpLookupResult);
      this.props.updateDoctors(newList, newList[0]);
  }

  onDelete(index) {
    const newList = [].concat(this.props.doctors.careProfessionals);
    newList.splice(index, 1);
    this.props.updateDoctors(newList, newList.length > 0 ? newList[0] : null);
  }

  doctorsDataOutput = () => {
    console.log(this.props.doctors.careProfessionals);

    return (
      <FlatList
        style={styles.listContainer}
        data={this.props.doctors.careProfessionals}
        keyExtractor={(item, index) => index.toString()}
        renderItem={({ item, index }) => (
          <DoctorListItem details={item} delete={this.onDelete} index={index} />
        )}
      />
    );
  };

  render() {
    let { errors = {}, secureTextEntry, ...data } = this.state;

    var doctorsName = "";
    var doctorLookup = this.props.hcpLookupResult;

    if (doctorLookup) {
      doctorsName =
        doctorLookup.title +
        "  " +
        doctorLookup.firstNames +
        " " +
        doctorLookup.lastName;
    }

    var idError = errors.doctorId;
    if ( idError == null){
      idError = this.props.notFound ? "ID not found" : null;
    }

    return (
      <View style={R.styles.container}>
        <KeyboardAwareScrollView horizontal={false} style={R.styles.scrollcontainer}>
          <View style={styles.listContainer}>{this.doctorsDataOutput()}</View>
          <Loader loading={this.props.isLoading} message="Saving" />
          <Loader loading={this.props.isSearching} message="Searching" />
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
            onSubmitEditing={() => {
              this.onLookup();
            }}
            returnKeyType="search"
            label="Clincian id"
            error={idError}
          />
          <GradientButton
            style={R.styles.button}
            onPress={() => {
              this.onLookup();
            }}
            title="Search for my clinician"
            color={R.colors.highlightColorOne}
            titleColor="white"
          />
          <Text
            style={[
              R.styles.body,
              {
                backgroundColor: "red",
                height: 50,
                marginTop: 20,
                marginBottom: 20,
                paddingLeft: 20,
                color: "white",
                textAlignVertical: "center"
              }
            ]}
          >
            Clinician = {doctorsName}
          </Text>

          <GradientButton
            style={R.styles.button}
            onPress={() => {
              this.onSave();
            }}
            title="Add clinician"
            color={R.colors.highlightColorOne}
            titleColor="white"
            disabled={doctorLookup == null}
          />
          <View style={{ height: 40 }} />
        </KeyboardAwareScrollView>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    paddingTop: 30,
    justifyContent: "flex-start",
    alignItems: "center"
  },
  listContainer: {
    width: "100%",
    marginTop: 20
  }
});

const mapStateToProps = state => {
  return {
    doctors: state.comms.accountData.doctors,
    hcpLookupResult: state.comms.hcpData.hcpSearchResult,
    hasError: state.comms.accountData.hasError,
    isLoading: state.comms.accountData.isLoading,
    isSearching: state.comms.hcpData.isLoading,
    notFound: state.comms.hcpData.hasError
  };
};

const mapDispatchToProps = dispatch => {
  return {
    updateDoctors: (careProf, primaryCareProf) => {
      dispatch(AccountDetailsActions.saveDoctors(careProf, primaryCareProf));
    },
    getDoctors: () => {
      dispatch(AccountDetailsActions.getDoctors());
    },
    lookUpDoctor: doctorId => {
      dispatch(HcpDataActions.searchForHCP(doctorId));
    },
    clearError: () => {
      dispatch(AccountDetailsActions.userAccountHasError(false));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(DoctorsScreen);
