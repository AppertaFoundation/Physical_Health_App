import React, { Component } from 'react';
import { HashRouter, Route, Switch } from 'react-router-dom';
// import { renderRoutes } from 'react-router-config';
import Loadable from 'react-loadable';
import './App.scss';

import { ServerConfigActions } from 'nhsphysicalhealthcomms';
import { LoginActions } from 'nhsphysicalhealthcomms';
import { connect } from 'react-redux';

import EnsureLoggedInContainer from './utils/EnsureLoggedInContainer';

const loading = () => <div className="animated fadeIn pt-3 text-center">Loading...</div>;

// Pages
const Login = Loadable({
  loader: () => import('./views/Pages/Login'),
  loading
});

const Register = Loadable({
  loader: () => import('./views/Pages/Register'),
  loading
});

const Page404 = Loadable({
  loader: () => import('./views/Pages/Page404'),
  loading
});

const Page500 = Loadable({
  loader: () => import('./views/Pages/Page500'),
  loading
});

class App extends Component {
  constructor(props) {
    super(props);

    const { dispatch } = this.props;
    dispatch(ServerConfigActions.updateServerConfig("/* SERVER URL */", "my-trusted-client", "secret"));
    console.log("App constructor");

    const {loginState} = this.props;
    if ( !loginState.tokenRefreshed ){
      console.log("Refeshing token");
      dispatch(LoginActions.refreshToken());
    }
  }

  render() {
    const {loginState} = this.props;

    return (
      <HashRouter>
          <Switch>
            <Route exact path="/login" name="Login Page" component={Login} />
            <Route exact path="/register" name="Register Page" component={Register} />
            <Route exact path="/404" name="Page 404" component={Page404} />
            <Route exact path="/500" name="Page 500" component={Page500} />
            <EnsureLoggedInContainer isLoggedIn={loginState.isLogged}/>
          </Switch>
      </HashRouter>
    );
  }
}

function mapStateToProps(state) {
  return {
      loginState: state.comms.login
    };
}

const connectedApp = connect(mapStateToProps)(App);
export default connectedApp; 
