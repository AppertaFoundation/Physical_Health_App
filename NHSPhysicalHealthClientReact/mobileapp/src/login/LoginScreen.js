import React, { Component } from "react";
import {
  View,
  Text,
  Button,
  Image,
  AsyncStorage,
  ImageBackground,
  ScrollView,
  Dimensions,
  StyleSheet
} from "react-native";
import { TextField } from "react-native-material-textfield";
import MaterialIcon from "react-native-vector-icons/MaterialIcons";
import { RaisedTextButton } from "react-native-material-buttons";
import { SafeAreaView } from "react-navigation";

import { connect } from "react-redux";
import { LoginActions } from "nhsphysicalhealthcomms";
import LocalDataActions from "../redux/actions/LocalDataActions";
import Loader from "../utils/Loader";
import GradientButton from "../utils/GradientButton";

import SplashScreen from 'react-native-splash-screen'

import R from "res/R";

class LoginScreen extends Component {
  static propTypes = {};

  static defaultProps = {};

  static navigationOptions = {
    header: null
  };

  constructor(props) {
    super(props);
    this.state = {};

    this.onFocus = this.onFocus.bind(this);
    this.onSubmit = this.onSubmit.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.onSubmitEmail = this.onSubmitEmail.bind(this);
    this.onSubmitPassword = this.onSubmitPassword.bind(this);
    this.onAccessoryPress = this.onAccessoryPress.bind(this);
    this.login = this.login.bind(this);

    this.emailRef = this.updateRef.bind(this, "email");
    this.passwordRef = this.updateRef.bind(this, "password");

    this.renderPasswordAccessory = this.renderPasswordAccessory.bind(this);

    this.state = {
      email: "",
      password: "",
      secureTextEntry: true,
      submitted: false
    };
  }

  componentDidMount() {
    SplashScreen.hide();

    this.setState({email : this.props.lastUserEmail});
  }

  componentDidUpdate(prevProps) {
    if (this.props.isLogged) {
      this.props.storeEmail(this.state.email);
      this.props.navigation.navigate("App");
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

  onSubmitEmail() {
    this.password.focus();
  }

  onSubmitPassword() {
    this.password.blur();
  }

  onAccessoryPress() {
    this.setState(({ secureTextEntry }) => ({
      secureTextEntry: !secureTextEntry
    }));
  }

  onChangeText(text) {
    ["email", "password"]
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

    ["email", "password"].forEach(name => {
      let value = this[name].value();

      if (!value) {
        noErrors = false;
        errors[name] = "Should not be empty";
      }
    });

    this.setState({ errors });

    if (noErrors) {
      this.setState({submitted: true});
      this.login();
    }
  }

  updateRef(name, ref) {
    this[name] = ref;
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

  async login() {
    this.props.login(this.state.email, this.state.password);
  }

  render() {
    let { errors = {}, secureTextEntry, ...data } = this.state;

    var width = Dimensions.get("window").width;
    var height = width / 1.3;

    return (
      <SafeAreaView style={[R.styles.container, { backgroundColor: R.colors.mainBackground }]}>
        <Loader loading={this.props.isLoading} message="Logging in" />
        <ScrollView horizontal={false} style={{
          paddingLeft: 10,
          paddingRight: 10
        }}>
          <Image
            source={require("../../assets/images/login/logo.png")}
            style={{
              width: width,
              height: height,
            }
            }
            resizeMode={"contain"}
          />
          {(this.props.hasError && this.state.submitted)? (
            <View>
              <Text style={[R.styles.headerWhite, { color: "red" }]}>
                Login failed.
              </Text>
              <Text style={[R.styles.headerWhite, { color: "red" }]}>
                Please check you have a network connection and retry
              </Text>
            </View>
          ) : null}
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
            onSubmitEditing={this.onSubmitEmail}
            returnKeyType="next"
            label="Email Address"
            error={errors.email}
          />

          <TextField
            inputContainerStyle={R.styles.textcontainer}
            inputContainerPadding={R.constants.textFieldLabelHeight}
            labelHeight={R.constants.textFieldLabelHeight}
            labelTextStyle={R.styles.textFieldLabelText}
            tintColor={R.colors.nhsblue}
            ref={this.passwordRef}
            value={data.password}
            secureTextEntry={secureTextEntry}
            autoCapitalize="none"
            autoCorrect={false}
            enablesReturnKeyAutomatically={true}
            clearTextOnFocus={true}
            onFocus={this.onFocus}
            onChangeText={this.onChangeText}
            onSubmitEditing={this.onSubmitPassword}
            returnKeyType="done"
            label="Password"
            error={errors.password}
            renderAccessory={this.renderPasswordAccessory}
          />

          <GradientButton
            onPress={() => {
              this.onSubmit();
            }}
            title="Login" />
          <GradientButton
            onPress={() => {
              this.props.navigation.navigate("Registration");
            }}
            title="Register"
          />
          <RaisedTextButton
            style={R.styles.button}
            onPress={() => {
              this.props.navigation.navigate("ResetPasswordScreen");
            }}
            title="Forgotten password?"
            color="gray"
            titleColor="white"
          />
          <View style={{ marginBottom: 40 }} />
        </ScrollView>
      </SafeAreaView>
    );
  }
}


const mapStateToProps = state => {
  return {
    isLogged: state.comms.login.isLogged,
    hasError: state.comms.login.hasError,
    isLoading: state.comms.login.isLoading,
    errorMessage: state.comms.login.errorMsg,
    lastUserEmail: state.local.username
  };
};

const mapDispatchToProps = dispatch => {
  return {
    login: (email, password) => {
      dispatch(LoginActions.login(email, password));
    },
    storeEmail: (email) => {
      dispatch(LocalDataActions.storeUsername(email));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(LoginScreen);
