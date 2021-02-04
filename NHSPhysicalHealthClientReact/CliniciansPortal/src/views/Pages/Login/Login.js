import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import { Button, Card, CardBody, CardGroup, Col, Container, Form, Input, InputGroup, InputGroupAddon, InputGroupText, Row, Alert } from 'reactstrap';

import { connect } from 'react-redux';
import {LoginActions} from 'nhsphysicalhealthcomms';

class Login extends Component {
  constructor(props) {
    super(props);

    // reset login status
    this.props.dispatch(LoginActions.logout());
    this.props.dispatch(LoginActions.loginHasError(false));

    this.state = {
        username: '',
        password: '',
        submitted: false
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
}

componentDidUpdate(prevProps, prevState, snapshot) {
  console.log(prevProps.loginError);

  if (prevProps.loginError !== this.props.loginError) {
      
  }

  if (this.props.isLoginSuccess){
      console.log("Login Success");
      this.props.history.push('/');
  }
}

handleChange(e) {
  const { name, value } = e.target;
  this.setState({ [name]: value });
}

handleSubmit(e) {
  e.preventDefault();

  this.setState({ submitted: true });
  const { username, password } = this.state;
  const { dispatch } = this.props;
  if (username && password) {
      dispatch(LoginActions.login(username, password));
  }
}
  
  render() {
    const { username, password, submitted } = this.state;
    return (
      <div className="app flex-row align-items-center">
        <Container>
          <Row className="justify-content-center">
            <Col md="8">
            {
          (this.props.loginError && submitted)
            ?  <Alert color="danger">
            Login failed.
          </Alert>
            : null
        }
           
              <CardGroup>
                <Card className="p-4">
                  <CardBody>
                    <Form onSubmit={this.handleSubmit}>
                      <img src="/full_logo.jpg" alt="image" style={{height:null,flex:1,width:300}}/>
                      <h5 style={{paddingTop:"20px", paddingBottom:"30px", fontFamily: "Oswald"}}>Apperta Open Platform - Clinician Portal</h5>
                      <p className="text-muted">Login to your account</p>
                      <InputGroup className="mb-3">
                        <InputGroupAddon addonType="prepend">
                          <InputGroupText>
                            <i className="icon-user"></i>
                          </InputGroupText>
                        </InputGroupAddon>
                        <Input 
                        type="text" 
                        placeholder="Username" 
                        autoComplete="username" 
                        name="username"
                        id="username"
                        value={ username }
                        onChange={this.handleChange} 
                        />
                      </InputGroup>
                      <InputGroup className="mb-4">
                        <InputGroupAddon addonType="prepend">
                          <InputGroupText>
                            <i className="icon-lock"></i>
                          </InputGroupText>
                        </InputGroupAddon>
                        <Input
                        type="password"
                        placeholder="Password"
                        autoComplete="current-password"
                        name="password"
                        id="password"
                        value={ password }
                        onChange={this.handleChange} 
                        />
                      </InputGroup>
                      <Row>
                        <Col xs="6">
                          <Button color="primary" className="px-4">Login</Button>
                        </Col>
                        <Col xs="6" className="text-right">
                          <Button color="link" className="px-0">Forgot password?</Button>
                        </Col>
                      </Row>
                    </Form>
                  </CardBody>
                </Card>
                <Card className="text-white bg-primary py-5 d-md-down-none" style={{ width: '44%' }}>
                  <CardBody className="text-center">
                    <div>
                      <h2>Don't have an account?</h2>
                      <p>Select Register Now to create a new Apperta Open Platform Clinical Portal account for clinicians</p>
                      <Link to="/register">
                        <Button color="primary" className="mt-3" active tabIndex={-1}>Register Now!</Button>
                      </Link>
                    </div>
                  </CardBody>
                </Card>
              </CardGroup>
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    loggingIn: state.comms.login.isLoading,
    isLoginSuccess: state.comms.login.isLogged,
    loginError: state.comms.login.hasError
  };
}

const connectedLoginPage = connect(mapStateToProps)(Login);
export default connectedLoginPage; 
