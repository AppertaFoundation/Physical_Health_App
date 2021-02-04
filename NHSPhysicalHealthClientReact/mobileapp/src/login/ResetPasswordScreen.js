import React, { Component } from "react";
import {
  StyleSheet,
  ImageBackground,
  Text,
  View,
  Button,
  ScrollView,
  TextInput,
  TouchableOpacity,
  Alert
} from "react-native";
import { SafeAreaView } from "react-navigation";
import { RaisedTextButton } from "react-native-material-buttons";
import { TextField } from "react-native-material-textfield";
import MaterialIcon from "react-native-vector-icons/MaterialIcons";
import Loader from "../utils/Loader";
import { connect } from "react-redux";
import { AccountDetailsActions } from "nhsphysicalhealthcomms";
import GradientButton from "../utils/GradientButton";

import R from "res/R";

class ResetPasswordScreen extends Component {
  static propTypes = {};

  static defaultProps = {};

  static navigationOptions = {
    title: "Reset password"
  };

  constructor(props) {
    super(props);

    this.onFocus = this.onFocus.bind(this);
    this.onSubmit = this.onSubmit.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.emailRef = this.updateRef.bind(this, "email");

    this.state = {
      email: ""
    };
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.props.hasError) {
      // Display error within alert
      Alert.alert(
        "",
        "Unable to send forgotten password email, please check you have a network connection and retry",
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
    } else if (this.props.accountUpdateSuccess && !prevProps.accountUpdateSuccess ){
      console.log("Update account data");
     
      Alert.alert(
        "",
        "Password reminder email sent",
        [
          {
            text: "OK",
            onPress: () => {
              this.props.navigation.navigate("Login");
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
    ["email"]
      .map(name => ({ name, ref: this[name] }))
      .forEach(({ name, ref }) => {
        if (ref.isFocused()) {
          this.setState({ [name]: text });
        }
      });
  }

  onSubmit() {
    let errors = {};
    let noErrors = true;

    ["email"].forEach(name => {
      let value = this[name].value();

      if (!value) {
        noErrors = false;
        errors[name] = "Should not be empty";
      }
    });

    this.setState({ errors });

    if (noErrors) {
      this.props.forgottenPassword(this.state.email);
    }
  }

  updateRef(name, ref) {
    this[name] = ref;
  }

  render() {
    let { errors = {}, secureTextEntry, ...data } = this.state;

    return (
      <ImageBackground
        source={require("../../assets/images/grey-bg.jpg")}
        style={R.styles.backgroundImage}
      >
        <SafeAreaView style={R.styles.container}>
          <ScrollView horizontal={false} style={R.styles.scrollcontainer}>
          <Loader loading={this.props.isLoading ? this.props.isLoading : false} message="Busy" />
            <Text style={R.styles.headerWhite}>
              Please enter your email address below and a link to reset your
              password will be sent to you.
            </Text>

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
              onSubmitEditing={this.onSubmit}
              returnKeyType="send"
              label="Email Address"
              error={errors.email}
            />
            <View style={R.styles.buttoncontainer}>
              <GradientButton
                onPress={this.onSubmit}
                title="SEND"
                color={R.colors.highlightColorOne}
                titleColor="white"
              />
            </View>
          </ScrollView>
        </SafeAreaView>
      </ImageBackground>
    );
  }
}

const mapStateToProps = state => {
  console.log(state);
  return {
    accountData: state.comms.accountData.accountData,
    hasError: state.comms.accountData.hasError,
    isLoading: state.comms.accountData.isLoading,
    accountUpdateSuccess: state.comms.accountData.accountUpdateSuccess
  };
};

const mapDispatchToProps = dispatch => {
  return {
    forgottenPassword: (emailAddress) => {
      dispatch(AccountDetailsActions.forgottenPassword(emailAddress));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ResetPasswordScreen);
