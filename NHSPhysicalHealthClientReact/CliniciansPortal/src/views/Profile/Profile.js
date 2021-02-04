import React, { Component } from 'react';
import { Alert, Form, Label, FormGroup, Card, CardBody, CardHeader, Col, FormFeedback, Button, CardFooter, Input, InputGroup, InputGroupText, InputGroupAddon } from 'reactstrap';
import { connect } from 'react-redux';

import { HcpDataActions } from 'nhsphysicalhealthcomms';

const fields = ["email",
  "firstNames",
  "jobTitle",
  "lastName",
  "location",
  "nhsId",
  "title",
  "username"];

class Profile extends Component {
  constructor(props) {
    super(props);

    this.state = {
      user: {
        email: "",
        firstNames: "",
        jobTitle: "",
        lastName: "",
        location: "",
        nhsId: "",
        title: "",
        username: ""
      },
      submitted: false,
      loaded: false
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
    console.log(this.props.success ? "Success": "Failure");
//    dispatch(HcpDataActions.clearHcpUpdateSuccess());
    this.setState({ loaded: false });
    dispatch(HcpDataActions.getHcpProfile());
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    console.log("Profile update");
    console.log(this.props.hcpData);
    console.log(this.state.loaded);
    if (this.props.hcpData != null && !this.state.loaded) {
      this.setState({ loaded: true });

      var user = this.props.hcpData;
      this.setState({
        user: {
          email: user.email ? user.email : "",
          firstNames: user.firstNames ? user.firstNames : "",
          jobTitle: user.jobTitle ? user.jobTitle : "",
          lastName: user.lastName ? user.lastName : "",
          location: user.location ? user.location : "",
          nhsId: user.nhsId ? user.nhsId : "",
          title: user.title ? user.title : "",
          username: user.username ? user.username : ""
        }
      });
    }
  }

  handleChange(event) {
    if ( this.props.success ){
      // Clear success flag
      const { dispatch } = this.props;
      dispatch(HcpDataActions.clearHcpUpdateSuccess());
    }

    const { id, name, value } = event.target;
    const { user } = this.state;

    this.setState({
      user: {
        ...user,
        [id]: value
      }
    });

    console.log(this.state);
  }

  handleSubmit(event) {
    event.preventDefault();

    let errors = {};
    let noErrors = true;

    fields.forEach(name => {
      let value = this.state.user[name];
      console.log(name + " = " + value);
      if (value.length == 0) {
        noErrors = false;
        errors[name] = "Should not be empty";
      }
    });

    this.setState({ errors });

    if (noErrors) {
      this.setState({ submitted: true });
      const { user } = this.state;
      const { dispatch } = this.props;

      dispatch(HcpDataActions.saveHcpProfile(user));
    }
  }

  render() {
    const { user, errors = {} } = this.state;

    console.log(errors);

    return (
      <div className="animated fadeIn">
        <Col xs="12" sm="6">
          <Card>
            <CardHeader>
              <strong>Profile</strong>
            </CardHeader>
            <Form onSubmit={this.handleSubmit}>
              <CardBody>
                <FormGroup>
                  <Label htmlFor="firstNames">First name</Label>
                  <Input
                    type="text"
                    id="firstNames"
                    placeholder="Enter your first name"
                    value={user.firstNames}
                    onChange={this.handleChange}
                    invalid={errors.firstNames != null}
                  />
                  <FormFeedback invalid>
                    Should not be empty
                  </FormFeedback>
                </FormGroup>
                <FormGroup>
                  <Label htmlFor="lastName">Last name</Label>
                  <Input
                    type="text"
                    id="lastName"
                    placeholder="Enter your last name"
                    value={user.lastName}
                    onChange={this.handleChange}
                    invalid={errors.lastName != null}
                  />
                  <FormFeedback invalid>
                    Should not be empty
                    </FormFeedback>
                </FormGroup>
                <FormGroup>
                  <Label htmlFor="jobTitle">Job title</Label>
                  <Input
                    type="text"
                    id="jobTitle"
                    placeholder="Enter your job title"
                    value={user.jobTitle}
                    onChange={this.handleChange}
                    invalid={errors.jobTitle != null}
                  />
                  <FormFeedback invalid>
                    Should not be empty
                    </FormFeedback>
                </FormGroup>
                <FormGroup>
                  <Label htmlFor="location">Location</Label>
                  <Input
                    type="text"
                    id="location"
                    placeholder="Enter your location"
                    value={user.location}
                    onChange={this.handleChange}
                    invalid={errors.location != null}
                  />
                  <FormFeedback invalid>
                    Should not be empty
                    </FormFeedback>
                </FormGroup>
                <FormGroup>
                  <Label htmlFor="title">Title</Label>
                  <Input
                    type="text"
                    id="title"
                    placeholder="Enter your title"
                    value={user.title}
                    onChange={this.handleChange}
                    invalid={errors.title != null}
                  />
                  <FormFeedback invalid>
                    Should not be empty
                    </FormFeedback>
                </FormGroup>
                <FormGroup>
                  <Label htmlFor="nhsId">Doctor or Nurse ID</Label>
                  <Input
                    type="text"
                    id="nhsId"
                    placeholder="Enter your Doctor or Nurse ID"
                    value={user.nhsId}
                    onChange={this.handleChange}
                    invalid={errors.nhsId != null}
                  />
                  <FormFeedback invalid>
                    Should not be empty
                    </FormFeedback>
                </FormGroup>
                <FormGroup>
                  <Label htmlFor="username">Username</Label>
                  <Input
                    type="text"
                    id="username"
                    placeholder="Enter your username"
                    value={user.username}
                    onChange={this.handleChange}
                    invalid={errors.username != null}
                  />
                  <FormFeedback invalid>
                    Should not be empty
                    </FormFeedback>
                </FormGroup>
                <FormGroup>
                  <Label htmlFor="email">Email</Label>
                  <Input
                    type="email"
                    id="email"
                    placeholder="Enter your email"
                    value={user.email}
                    onChange={this.handleChange}
                    invalid={errors.email != null}
                  />
                  <FormFeedback invalid>
                    Should not be empty
                    </FormFeedback>
                </FormGroup>
              </CardBody>
              <CardFooter>
                <Button type="submit" size="sm" color="primary"><i className="fa fa-dot-circle-o"></i> Update</Button>
                {
          this.props.success
            ?  <Alert color="success">
            Profile Updated.
          </Alert>
            : null
        }
        {
          this.props.hasError
            ?  <Alert color="danger">
            Error updating profile
          </Alert>
            : null
        }
              </CardFooter>
            </Form>
          </Card>
        </Col>
      </div>

    );
  }
}

function mapStateToProps(state) {
  return {
    hcpData: state.comms.hcpData.hcpProfile,
    hasError: state.comms.hcpData.hasError,
    isLoading: state.comms.hcpData.isLoading,
    errorMsg: state.comms.hcpData.errorMsg,
    success: state.comms.hcpData.profileUpdateSuccess
  };
}

const connectedProfile = connect(mapStateToProps)(Profile);
export default connectedProfile;
