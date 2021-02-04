import React, { Component } from "react";
import { View, ImageBackground, Text, Button, ScrollView, Alert } from "react-native";
import { TextField } from "react-native-material-textfield";
import { RaisedTextButton } from "react-native-material-buttons";
import CheckBox from "react-native-check-box";
import { SafeAreaView } from "react-navigation";
import HandleHardwareBackButton from "../utils/HandleHardWareBackButton";
import GradientButton from "../utils/GradientButton";
import { connect } from "react-redux";

import { PatientDataActions } from "nhsphysicalhealthcomms";
import LocalDataActions from "../redux/actions/LocalDataActions";

import R from "res/R";

const fields = [
  "height",
  "weight",
  "heightFoot",
  "heightInches",
  "weightStone",
  "weightPounds"
];

class RegistrationScreenTwo extends Component {
  static propTypes = {};

  static defaultProps = {};

  static navigationOptions = {
    title: "Your details",
    headerLeft: null
  };

  constructor(props) {
    super(props);
    this.heightFootRef = this.updateRef.bind(this, "heightFoot");
    this.heightInchesRef = this.updateRef.bind(this, "heightInches");
    this.weightStoneRef = this.updateRef.bind(this, "weightStone");
    this.weightPoundsRef = this.updateRef.bind(this, "weightPounds");
    this.heightRef = this.updateRef.bind(this, "height");
    this.weightRef = this.updateRef.bind(this, "weight");

    this.onSubmit = this.onSubmit.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onChangeText = this.onChangeText.bind(this);

    this.state = {
      heightMetric: true,
      heightFoot: "",
      heightInches: "",
      height: "",
      weightMetric: true,
      weight: "",
      weightPounds: "",
      weightStone: "",

      secureTextEntry: true,
      loading: false
    };
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

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.props.hasError) {
      var errorMsg = "Unable to update data, please check you have a network connection and retry";
      this.onError(errorMsg);
    } else if (this.props.updateSuccess) {
      this.props.navigation.replace("RegistrationScreenThree");
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

  onChangeText(text) {
    fields
      .map(name => ({ name, ref: this[name] }))
      .forEach(({ name, ref }) => {
        if (ref && ref.isFocused()) {

          if (name == "height") {
            var cms = parseInt(text) || 0;
            var realFeet = (cms * 0.3937) / 12;
            var feet = Math.floor(realFeet);
            var inches = Math.round((realFeet - feet) * 12);

            this.setState({
              heightFoot: String(feet),
              heightInches: String(inches)
            });
          } else if (name == "heightFoot" || name == "heightInches") {  
            var feet = parseInt(name =="heightFoot" ? text : this.state.heightFoot) || 0;
            var inches = parseInt(name == "heightInches" ? text : this.state.heightInches) || 0;
            var cms = Math.round(feet * 30.48 + inches * 2.54);

            this.setState({
              height: String(cms)
            });
          } else if ( name == "weight"){
            var kgs = parseInt(text) || 0;
            var poundTotal = kgs * 2.20462;
            var stone = Math.floor(poundTotal/14);
            var pounds = Math.round(poundTotal - (stone * 14));
            console.log({kgs, poundTotal, stone, pounds});

            this.setState({
              weightPounds : String(pounds),
              weightStone: String(stone)
            });
          } else if ( name == "weightPounds" || name == "weightStone"){
              var pounds = parseInt(name == "weightPounds" ? text : this.state.weightPounds);
              var stone = parseInt(name == "weightStone" ? text : this.state.weightStone);

              var poundsTotal = stone * 14 + pounds;
              var kgs = Math.round(poundsTotal/2.20462);
              this.setState({
                weight: String(kgs)
              });
          } 

            this.setState({ [name]: text });
        }
      });
  }

  updateRef(name, ref) {
    this[name] = ref;
  }

  onSubmit() {
    if ( this.state.height != "" ){
      this.props.addHeight(this.state.height);
    } 

    if ( this.state.weight != "" ){
      this.props.addWeight(this.state.weight);
    } else {
      this.props.navigation.replace("RegistrationScreenThree");
    }
  }

  onBack = () => {
    this.props.navigation.navigate("Auth");
    return true;
  }

  render() {
    let { errors = {}, secureTextEntry, ...data } = this.state;

    return (
      <HandleHardwareBackButton onBack={this.onBack}>
        <SafeAreaView style={R.styles.container}>
        <ScrollView
          horizontal={false}
          style={R.styles.scrollcontainer}
          keyboardDismissMode="none"
          keyboardShouldPersistTaps="handled"
          >
          <View style={R.styles.container}>
            <Text style={R.styles.headerWhite}>
              Please enter your height and weight. These values are optional but
              we can use them to calculate your BMI (Body Mass Index) which can
              be a key health metric
            </Text>

            <View style={{ flexDirection: "row" }}>
              <Text style={[R.styles.normal, { alignSelf: "center" }]}>
                Height
              </Text>
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
                onSubmitEditing={() => {this.state.weightMetric ? this.weight.focus() : this.weightStone.focus()}}
                returnKeyType="next"
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
                onSubmitEditing={() => {this.state.weightMetric ? this.weight.focus() : this.weightStone.focus()}}
                returnKeyType="next"
                label="Height Inches"
                error={errors.heightInches}
              />
            )}
            <View style={{ flexDirection: "row" }}>
              <Text style={[R.styles.normal, { alignSelf: "center" }]}>
                Weight
              </Text>
              <CheckBox
                style={{ padding: 20, flex: 1 }}
                onClick={() => {
                  this.setState({
                    weightMetric: true
                  });
                }}
                isChecked={this.state.weightMetric}
                rightText={"Kgs"}
                checkBoxColor="darkGrey"
                rightTextStyle={{ color: R.styles.darkNormal }}
              />
              <CheckBox
                style={{ padding: 20, flex: 1 }}
                onClick={() => {
                  this.setState({
                    weightMetric: false
                  });
                }}
                isChecked={!this.state.weightMetric}
                rightText={"Stones/lbs"}
                checkBoxColor="darkGrey"
                rightTextStyle={{ color: R.styles.darkNormal }}
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
                ref={this.weightRef}
                value={data.weight}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                onSubmitEditing={this.onSubmit}
                returnKeyType="done"
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
                ref={this.weightStoneRef}
                value={data.weightStone}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                onSubmitEditing={() => {this.weightPounds.focus()}}
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
                ref={this.weightPoundsRef}
                value={data.weightPounds}
                autoCapitalize="none"
                autoCorrect={false}
                enablesReturnKeyAutomatically={true}
                onFocus={this.onFocus}
                onChangeText={this.onChangeText}
                onSubmitEditing={this.onSubmit}
                returnKeyType="done"
                label="Weight pounds"
                error={errors.weightPounds}
              />
            )}

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
        </ScrollView>
        </SafeAreaView>
      </HandleHardwareBackButton>
    );
  }
}

const mapStateToProps = state => {
  console.log(state);
  return {
    hasError: state.comms.patientData.hasError,
    isLoading: state.comms.patientData.isLoading,
    errorMsg: state.comms.patientData.errorMsg,
    updateSuccess: state.comms.patientData.updateSuccess
  };
};

const mapDispatchToProps = dispatch => {
  return {
    addHeight: (height) => {
      dispatch(LocalDataActions.updateHeight(height));
   }, 
   addWeight: (weight) => {
    var date = new Date();
    dispatch(
      PatientDataActions.addTestResultReadings(
        null, // current user
        date.getTime(),
        [],
        [{field:"weight", value:String(weight)}]
      ));  
   },
   clearError: () => {
    dispatch(PatientDataActions.hasError(false));
  }
  }
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(RegistrationScreenTwo);
