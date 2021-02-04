import React, {Component} from 'react';
import {
  ActivityIndicator,
  AsyncStorage,
  StatusBar,
  StyleSheet,
  View,
} from 'react-native';
import { connect } from "react-redux";
import {LoginActions} from 'nhsphysicalhealthcomms';
import {ServerConfigActions} from 'nhsphysicalhealthcomms';

import R from "res/R";

class SplashLauncherScreen extends Component {
    constructor() {
      super();
    }

    componentDidMount() {
      console.log('componentDidMount');

      this.props.registerServerConfig();

      if ( this.props.isLogged ){
        // Check token valid
        console.log('Refresh Token');
        this.props.refreshToken();
      } else {
        // Mo access token, proceed to auth flow
        console.log('Login');
        this.props.navigation.navigate('Auth' );
      }
  }

  componentDidUpdate(prevProps){
    console.log('componentDidUpdate');

    if ( this.props.tokenRefreshed ){
      this.props.navigation.navigate('App');
    } else {
      // Mo access token, proceed to auth flow
      console.log('Login');
      this.props.navigation.navigate('Auth' );
    }
  }
  
    // Render any loading content that you like here
    render() {
      return (
        <View style={styles.container}>
          <ActivityIndicator />
          <StatusBar barStyle="default" />
        </View>
      )
    }
  }
  
  const styles = StyleSheet.create({
    container: {
      flex: 1,
      alignItems: 'center',
      justifyContent: 'center',
    },
  });

  const mapStateToProps = state => {
    return {
      isLogged: state.comms.login.isLogged,
      hasError : state.comms.login.hasError,
      tokenRefreshed: state.comms.login.tokenRefreshed,
      refreshToken: state.comms.login.refreshToken
    };
  };
  
  const mapDispatchToProps = dispatch => {
    return {
      refreshToken: () => {
        dispatch(LoginActions.refreshToken());
      },
      registerServerConfig: () => {
        dispatch(ServerConfigActions.updateServerConfig(R.constants.serverUrl, R.constants.authUsername,R.constants.authSecret));
      }
    };
  };
  
  export default connect(
    mapStateToProps,
    mapDispatchToProps
  )(SplashLauncherScreen);