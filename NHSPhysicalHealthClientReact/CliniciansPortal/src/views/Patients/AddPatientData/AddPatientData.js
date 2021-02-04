import React, { Component } from 'react';
import {
  Alert, Form, Label, FormGroup, Card, CardBody, CardHeader, Col, Row, FormFeedback, Button, CardFooter, Input, InputGroup, InputGroupText, InputGroupAddon,
  ButtonDropdown,
  DropdownItem,
  DropdownMenu,
  DropdownToggle
} from 'reactstrap';
import { connect } from 'react-redux';
import Select from 'react-select'
import Datetime from 'react-datetime';
import 'react-datetime/css/react-datetime.css'
import { TestResultFields } from 'nhsphysicalhealthcomms';
import { SelfMonitoringFields } from 'nhsphysicalhealthcomms';

import { PatientDataActions } from 'nhsphysicalhealthcomms';
import { supported_fields as SUPPORTED_FIELDS } from '../SupportedFieldsConstants';

class AddPatientData extends Component {
  constructor(props) {
    super(props);

    var date = new Date();

    this.state = {
      date: date,
      data: {
      },
      testData: {
      },
      formElements: [],
      smFormElements: [],
      submitted: false,
      loaded: false,
      dropDown: {},
      selectedOption: null
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleDateChange = this.handleDateChange.bind(this);
    this.handleTestChange = this.handleTestChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.addTestField = this.addTestField.bind(this);
    this.handleSelectChange = this.handleSelectChange.bind(this);
    this.handleUnitSelectionOnClick = this.handleUnitSelectionOnClick.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;

    // Clear any previous success state
    dispatch(PatientDataActions.clearUpdateSuccess());
    dispatch(PatientDataActions.hasError(false));

    // Get Existing data fields
    var resultsArray = this.props.latestData;

    var formElementArray = [];
    var smFormElementsArray = [];
    var arrayLength = resultsArray.length;
    for (var i = 0; i < arrayLength; i++) {
      var resultItem = resultsArray[i];
      var formField = TestResultFields.find(function (element) {
        return element.analyte_name_code == resultItem.code;
      });

      if (formField != null) {
        formField.currentUnits = formField.UCUM_unit[0];
        formElementArray.push(formField);
      } else {
        // Look up self reporting fields
        var smFormField = SelfMonitoringFields[resultItem.code];
        smFormField.code = resultItem.code;
        smFormElementsArray.push(smFormField);
      }
    }

    // Generate array of missing fields
    var missingFieldsArray = [];
    arrayLength = SUPPORTED_FIELDS.length;
    for (var i = 0; i < arrayLength; i++) {
      var code = SUPPORTED_FIELDS[i];
      console.log(code);

      if (code == "blood") {
        var found = smFormElementsArray.find(function (element) {
          return element.code == "systolic";
        });

        if (!found) {
          var bloodField = { value: "blood", name: "Blood Pressue" };
          missingFieldsArray.push(bloodField);
        }
      } else {
        // Is ths code already added?
        var found = formElementArray.find(function (element) {
          return element.analyte_name_code == code;
        });

        if (found == null) {
          console.log("NOT FOUND");
          var found = smFormElementsArray.find(function (element) {
            return element.code == code;
          });

          if (found == null) {
            var formField = TestResultFields.find(function (element) {
              return element.analyte_name_code == code;
            });

            var missingField = { value: code };
            if (formField != null) {
              console.log("TEST FIELD");
              missingField.label = formField.localised_analyte_name;
            } else {
              console.log("SELF REPORTING FIELD");
              // Look up self reporting fields
              var smFormField = SelfMonitoringFields[code];
              missingField.label = smFormField.name;
            }

            missingFieldsArray.push(missingField);
          }
        }
      }
    }

    this.setState({ formElements: formElementArray });
    this.setState({ smFormElements: smFormElementsArray });
    this.setState({ missingFields: missingFieldsArray });

    console.log(this.state);
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    // On success automatically go back
    if (this.props.updateSuccess) {
      this.props.history.goBack();
    }
  }

  handleChange(event) {
    const { id, name, value } = event.target;
    const { data } = this.state;

    this.setState({
      data: {
        ...data,
        [id]: value
      }
    });

    console.log("Handle change");
    console.log("id " + id);
    console.log(this.state);
  }

  handleTestChange(event) {
    const { id, name, value } = event.target;
    const { testData } = this.state;

    this.setState({
      testData: {
        ...testData,
        [id]: value
      }
    });

    console.log("Handle change");
    console.log("id " + id);
    console.log(this.state);
  }

  handleDateChange(dateTime) {
    this.setState({
      date: new Date(dateTime)
    });
  }

  handleSubmit(event) {
    event.preventDefault();

    let errors = {};
    let noErrors = true;
    this.setState({ errors });

    if (noErrors) {
      const { data } = this.state;
      const { testData } = this.state;
      const { date } = this.state;
      const { dispatch } = this.props;

      console.log(data);
      console.log(testData);

      var username = this.props.match.params.username;

      var smContent = Object.keys(data).map(function (key) {
        var result = new Object;
        result.field = key;
        result.value = data[key];
        return result;
      });

      console.log("**********************");
      console.log(smContent);
      console.log("***************");

      // Convert test results into an array
      var testResults = Object.keys(testData).map(function (key) {
        var testResult = new Object;
        testResult.test_code = key;
        testResult.value = testData[key];
        return testResult;
      });

      dispatch(PatientDataActions.addTestResultReadings(username, date.getTime(), testResults, smContent));
    }
  }

  dropDownSelect(event) {
    this.setState({
      dropdownOpen: !this.state.dropdownOpen,
      value: event.target.innerText
    });
  }

  addTestField() {
    console.log("ADD TEST FIELD");
    var selectedOption = this.state.selectedOption;

    console.log(selectedOption);

    var code = selectedOption.value;

    var formElementArray = this.state.formElements;
    var smFormElementsArray = this.state.smFormElements;

    var formField = TestResultFields.find(function (element) {
      return element.analyte_name_code == code;
    });

    if ( code == "blood" ){
      var smFormField = SelfMonitoringFields["systolic"];
      smFormField.code = "systolic";
      smFormElementsArray.push(smFormField);

      var smFormField2 = SelfMonitoringFields["diastolic"];
      smFormField2.code = "diastolic";
      smFormElementsArray.push(smFormField2);

    } else if (formField != null) {
      formField.currentUnits = formField.UCUM_unit[0];
      formElementArray.push(formField);
    } else {
      // Look up self reporting fields
      var smFormField = SelfMonitoringFields[code];
      smFormField.code = code;
      smFormElementsArray.push(smFormField);
    }

    var missingFieldsArray = this.state.missingFields;
    var updatedMissingFieldsArray = missingFieldsArray.filter(field => {
      return field.value != code;
    });

    this.setState({ formElements: formElementArray });
    this.setState({ smFormElements: smFormElementsArray });
    this.setState({ missingFields: updatedMissingFieldsArray });
    this.setState({ selectedOption: null});
  }

  handleSelectChange(selectedOption) {
    this.setState({ "selectedOption": selectedOption });
  }

  handleUnitSelectionOnClick(sender) {
    var value = sender.currentTarget.getAttribute("dropdownvalue");
    var testCode= sender.currentTarget.getAttribute("testCode");

    var formField = this.state.formElements.find(function (element) {
      return element.analyte_name_code == testCode;
    });

    formField.currentUnits = value;
  }

  render() {
    const { data, testData, formElements, smFormElements, date, selectedOption, missingFields, errors = {} } = this.state;

    var items = formElements.map((item, key) => {
      var units = item.currentUnits;
      var multipleUnits = item.UCUM_unit.length > 1;
      var currentState = this.state.dropDown[item.analyte_name_code];

      return (<FormGroup>
        <Label htmlFor={item.analyte_name_code}>{item.localised_analyte_name}</Label>
        <Row className="align-items-center">
          <Col xs="12" md="9">
            <Input
              type="text"
              id={item.analyte_name_code}
              value={testData[item.analyte_name_code]}
              onChange={this.handleTestChange}
              invalid={errors[item.analyte_name_code] != null}
            />
          </Col>
          <Col md="1">
            <Label>{units}</Label>
          </Col>
          {multipleUnits ?
            <Col md="2">
              <ButtonDropdown isOpen={currentState}
                toggle={() => { 
                  var dropDown = this.state.dropDown;
                  dropDown[item.analyte_name_code] = !currentState
                  this.setState({ dropDown: dropDown });
                   }}>
                <DropdownToggle caret color="light" visible="false">
                  Units
                            </DropdownToggle>
                <DropdownMenu className={currentState ? 'show' : ''}>
                  {item.UCUM_unit.map(unit => (
                    <DropdownItem testCode={item.analyte_name_code} dropDownValue={unit} onClick={this.handleUnitSelectionOnClick}>{unit}</DropdownItem>
                  ))}
                </DropdownMenu>
              </ButtonDropdown>
            </Col>
            : null}
        </Row>
      </FormGroup>);
    }
    );

    var smItems = smFormElements.map((item, key) =>
      <FormGroup>
        <Label htmlFor={item.code}>{item.name}</Label>
        <FormGroup>
          <Row className="align-items-center">
            <Col xs="12" md="9">
              <Input
                type="text"
                id={item.code}
                value={data[item.code]}
                onChange={this.handleChange}
                invalid={errors[item.code] != null}
              />
            </Col>
            <Col md="3">
              <Label>{item.units}</Label>
            </Col>
          </Row>
        </FormGroup>
        <FormFeedback invalid>
          Should not be empty
        </FormFeedback>
      </FormGroup>
    );

    return (
      <div className="animated fadeIn">
        <Col xs="14" sm="10">
          <Card>
            <CardHeader>
              <strong>Patient Data</strong>
            </CardHeader>
            <Form onSubmit={this.handleSubmit}>
              <CardBody>
                <FormGroup>
                  <Row className="align-items-center">
                    <Col xs="12" md="9">
                      <Label htmlFor="date">Date</Label>
                      <Datetime
                      id="date"
                      value={date}
                      onChange={this.handleDateChange}
                      />
                      <FormFeedback invalid>
                        Should not be empty
                  </FormFeedback>
                    </Col>
                  </Row>
                </FormGroup>
                {smItems}
                {items}

                <Label>Add Test Field</Label>
                <FormGroup row className="my-0 flex-row align-items-center">
                  <Col xs="8">
                    <Select
                      options={missingFields}
                      isSearchable="true"
                      value={selectedOption}
                      onChange={this.handleSelectChange} />
                  </Col>
                  <Col xs="4">
                    <Button
                      type="button"
                      onClick={() => { this.addTestField(); }}
                      disabled={selectedOption == null}
                      color="primary">Add Field</Button>
                  </Col>
                </FormGroup>

              </CardBody>
              <CardFooter>
                <Button type="submit" color="primary"><i className="fa fa-dot-circle-o"></i> Submit</Button>
                {
                  this.props.success
                    ? <Alert color="success">
                      Patient Data Updated.
          </Alert>
                    : null
                }
                {
                  this.props.hasError
                    ? <Alert color="danger">
                      Error updating data
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
    latestData: state.comms.patientData.latestResults,
    hasError: state.comms.patientData.hasError,
    isLoading: state.comms.patientData.isLoading,
    errorMsg: state.comms.patientData.errorMsg,
    updateSuccess: state.comms.patientData.updateSuccess
  };
}

const connectedAddPatientData = connect(mapStateToProps)(AddPatientData);
export default connectedAddPatientData;
