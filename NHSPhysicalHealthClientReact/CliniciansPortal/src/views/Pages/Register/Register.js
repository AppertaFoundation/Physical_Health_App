import React, { Component } from 'react';
import { Button, Card, CardBody, CardFooter, Col, Container, Form, Input, InputGroup, InputGroupAddon, InputGroupText, Row, Alert } from 'reactstrap';
import { connect } from 'react-redux';
import {LoginActions} from 'nhsphysicalhealthcomms';
import { OutgoingMessage } from 'http';

class Register extends Component {
  constructor(props) {
    super(props);

    this.state = {
        user: {
            username: '',
            email: '',
            password: '',
            repeatPassword: ''
        },
        submitted: false
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);

    this.props.dispatch(LoginActions.loginHasError(false, null));
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

handleChange(event) {
  const { name, value } = event.target;
  const { user } = this.state;
  this.setState({
      user: {
          ...user,
          [name]: value
      }
  });
}

handleSubmit(event) {
  event.preventDefault();

  this.setState({ submitted: true });
  const { user } = this.state;
  const { dispatch } = this.props;
  if (user.username && user.email && user.password) {
      dispatch(LoginActions.register(user.username, user.email, user.password, "HCP"));
  }
}

  render() {
    const { user } = this.state;

    var errors = [];
    if (this.props.registrationError && this.props.registrationError.validationErrors){
      errors =  this.props.registrationError.validationErrors;
      } else {
        errors = [
          {
            field: "Error",
            message: "Unknown error registering"
          }
        ]
      }

    return (
      <div className="app flex-row align-items-center">
        <Container>
          <Row className="justify-content-center">
            <Col md="9" lg="7" xl="6">
            {
          this.props.loginError
            ?  
            <Alert color="danger">
           {
             errors.map((error, idx) =>{
              var errorMessage;
              if ( error.field == "emailAddress"){
                errorMessage = "Email address: " + error.message;
              } else if ( error.field == "password"){
                errorMessage = "Password: " + error.message;
              } else {
                errorMessage = error.field + ": " + error.message; 
              }
              
              return <li key={idx}>{errorMessage}</li>;
            })
           }
          </Alert>
            : null
        }
              <Card className="mx-4">
                <CardBody className="p-4">
                  <Form onSubmit={this.handleSubmit}>
                    <h1>Register</h1>
                    <p className="text-muted">Create your account</p>
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
                      value={ user.username }
                      onChange={this.handleChange} 
                      />
                    </InputGroup>
                    <InputGroup className="mb-3">
                      <InputGroupAddon addonType="prepend">
                        <InputGroupText>@</InputGroupText>
                      </InputGroupAddon>
                      <Input 
                      type="text" 
                      placeholder="Email" 
                      autoComplete="email" 
                      name="email"
                        id="email"
                        value={ user.email }
                        onChange={this.handleChange} 
                      />
                    </InputGroup>
                    <InputGroup className="mb-3">
                      <InputGroupAddon addonType="prepend">
                        <InputGroupText>
                          <i className="icon-lock"></i>
                        </InputGroupText>
                      </InputGroupAddon>
                      <Input
                      type="password" 
                      placeholder="Password" 
                      autoComplete="new-password" 
                      name="password"
                        id="password"
                        value={ user.password }
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
                      placeholder="Repeat password" 
                      autoComplete="new-password" 
                      name="repeatPassword"
                        id="repeatPassword"
                        value={ user.repeatPassword }
                        onChange={this.handleChange} 
                      />
                    </InputGroup>
                    <Button color="success" block>Create Account</Button>
                  </Form>
                </CardBody>
              </Card>
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}


const mapStateToProps = (state) => {
  return {
    registering: state.comms.login.isLoading,
    isLoginSuccess: state.comms.login.isLogged,
    loginError: state.comms.login.hasError,
    registrationError: state.comms.login.errorMessage
  };
}

const connectedRegisterPage = connect(mapStateToProps)(Register);
export default connectedRegisterPage ;
