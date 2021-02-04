import React from 'react';
import { Route} from 'react-router-dom';
import Loadable from 'react-loadable';
import { withRouter } from "react-router";

const loading = () => <div className="animated fadeIn pt-3 text-center">Loading...</div>;

const Login = Loadable({
    loader: () => import('../views/Pages/Login'),
    loading
  });

// Containers
const DefaultLayout = Loadable({
    loader: () => import('../containers/DefaultLayout'),
    loading
  });

class EnsureLoggedInContainer extends React.Component
{
    componentDidMount() {
        const {isLoggedIn} = this.props;
        console.log("LoggedIn: " + isLoggedIn);
        if ( !isLoggedIn )
        {
            this.props.history.push('/login');
        }
    }

    render() {
        const {isLoggedIn} = this.props;
        console.log("LoggedIn: " + isLoggedIn);
        if ( this.props.isLoggedIn )
        {
            console.log("Logged in");
            return <Route path="/" name="Home" component={DefaultLayout} />;
        }
        else
        {
            console.log("Logged out");
            return <Route path="/" component={Login} key="Login" />;
        }
    }
}

export default withRouter(EnsureLoggedInContainer);