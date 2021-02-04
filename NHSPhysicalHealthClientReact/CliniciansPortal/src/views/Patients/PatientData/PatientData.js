import React, { Component } from 'react';
import { Alert, CardFooter, Card, CardBody, CardHeader, Col, Button, Nav, NavItem, NavLink, Row, TabContent, TabPane } from 'reactstrap';
import { Scatter } from 'react-chartjs-2';
import BootstrapTable from 'react-bootstrap-table-next';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import { tab_layout as TAB_TEST_FIELDS } from '../SupportedFieldsConstants';
import { supported_fields as SUPPORTED_FIELDS } from '../SupportedFieldsConstants';

import { HcpDataActions } from 'nhsphysicalhealthcomms';
import {PatientDataActions} from 'nhsphysicalhealthcomms';


function dateFormatter(cell, row) {
  return (
    <span>{ cell.toLocaleDateString() }</span>
  );
}

function timeFormatter(cell, row) {
  return (
    <span>{ cell.toLocaleTimeString() }</span>
  );
}

function rankFormatter(cell, row, rowIndex, formatExtraData) {
  var username = row.username;
  var singular = row.singular;

  if ( singular ){
    return (<Button block color="secondary" disabled={true} >View</Button>)
  } else {
  // Show blood screen for blood pressure
  if ( cell == "systolic" || cell == "diastolic" ){
    cell = "blood";
  }

  var link = '/patients/view/'+ username + '/' + cell;
  return (
    <Link to={link}>
      <Button block color="primary">View</Button>
    </Link>
  );
  }
}

const columns = [{
  dataField: 'name',
  text: 'Measurement',
}, {
  dataField: 'value',
  text: 'Value',
},
{
  dataField : 'units',
  text: 'Units',
},
{
dataField: 'date',
  text: 'Date',
  formatter: dateFormatter
},
{
  dataField: 'date',
    text: 'Time',
    formatter: timeFormatter
  },{
    dataField: 'composerName',
    text: "Recorded by"
  },{
  dataField: "code",
  text: 'History',
  formatter: rankFormatter
}];

class PatientData extends Component {
  constructor(props) {
    super(props);

    this.toggle = this.toggle.bind(this);
    this.resultsList = this.resultsList.bind(this);

    var patientUsername = this.props.match.params.id;
    console.log("Username = " + patientUsername);

    var patient = this.props.patients.find(function(element) {
      return element.username == patientUsername;
    });

    this.state = {
      activeTab: '1',
      patientUsername: patientUsername,
      patient: patient,
    };
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch(PatientDataActions.hasError(false));
    
    dispatch(PatientDataActions.fetchLatestReadings(this.state.patientUsername, SUPPORTED_FIELDS));

    if ( this.props.hcpData == null ){
      dispatch(HcpDataActions.getHcpProfile());
    }
}

resultsList(tab) {
  var patientUsername = this.props.match.params.id;

  var tabData = TAB_TEST_FIELDS[tab];
  var resultsArray = this.props.latestData;

  var tabArray = [];
  var arrayLength = resultsArray.length;
    for (var i = 0; i < arrayLength; i++) {
      var resultItem = resultsArray[i];
      var found = tabData.find(function(element) {
        return element.code == resultItem.code;
      });

      if ( found != null ){
        resultItem.username = patientUsername;
        resultItem.name = found.name;
        tabArray.push(resultItem);
      }
  }


  const rowStyle2 = (row, rowIndex) => {
    const style = {};

    console.log(row.composerName);

    if (row.composerName != "Patient" ) {
      style.backgroundColor = '#ffffff';
    } else {
      style.backgroundColor = '#f9d8e1';
    }
  
    return style;
  };

  return (<BootstrapTable keyField='code' data={ tabArray } columns={ columns } rowStyle={ rowStyle2 } noDataIndication="No data"/>);
}

toggle(tab) {
  this.setState({
    activeTab: tab,
  });
}

tabPane() {
  if ( this.props.hasError ){
    return (
      <>
        <TabPane tabId="1">
        </TabPane>
        <TabPane tabId="2">
        </TabPane>
        <TabPane tabId="3">
        </TabPane>
        <TabPane tabId="4">
        </TabPane>
        <TabPane tabId="5">
        </TabPane>
        <TabPane tabId="6">
        </TabPane>
      </>);
  } 

  return (
    <>
      <TabPane tabId="1">
        {this.resultsList(0)}
      </TabPane>
      <TabPane tabId="2">
        {this.resultsList(1)}
      </TabPane>
      <TabPane tabId="3">
        {this.resultsList(2)}
      </TabPane>
      <TabPane tabId="4">
        {this.resultsList(3)}
      </TabPane>
      <TabPane tabId="5">
        {this.resultsList(4)}
      </TabPane>
      <TabPane tabId="6">
        {this.resultsList(5)}
      </TabPane>
    </>
  );
}
  render() {

    const { patientData } = this.props;

    var patientUsername = this.props.match.params.id;
    var addDataLink = '/patients/view/'+patientUsername+"/add";

    if ( this.state.patient ){

    var dob = new Date(this.state.patient.dateOfBirth);
    var diff_ms = Date.now() - dob.getTime();
    var age_dt = new Date(diff_ms); 
    var ageYears =  Math.abs(age_dt.getUTCFullYear() - 1970);

    var title = "Patient Data - " 
                + this.state.patient.firstNames
                + " "
                + this.state.patient.lastName
                + " ( DOB: " + dob.toLocaleDateString() + "  Age: "+ ageYears + " )" ;
    }

    return (
      <div className="animated fadeIn">
      {
          this.props.hasError
            ?  <Alert color="danger">
            Error fetching data
          </Alert>
            : null
        }
        { this.props.isLoading ? <div className="animated fadeIn pt-1 text-center">Loading...</div> :
        <Row>
          <Card>
        <CardHeader>
                <i className="fa fa-align-justify"></i> {title}
              </CardHeader>
              <CardBody>
                <p>
              <Link to={addDataLink}>
                <Button size="lg" color="primary"><i className="fa"></i> Add Data </Button>
              </Link>
              </p>
        <Nav tabs>
              <NavItem>
                <NavLink
                  active={this.state.activeTab === '1'}
                  onClick={() => { this.toggle('1'); }}
                >
                  General Health
                </NavLink>
              </NavItem>
              <NavItem>
                <NavLink
                  active={this.state.activeTab === '2'}
                  onClick={() => { this.toggle('2'); }}
                >
                  Heart Health
                </NavLink>
              </NavItem>
              <NavItem>
                <NavLink
                  active={this.state.activeTab === '3'}
                  onClick={() => { this.toggle('3'); }}
                >
                  Kidney Health
                </NavLink>
              </NavItem>
              <NavItem>
                <NavLink
                  active={this.state.activeTab === '4'}
                  onClick={() => { this.toggle('4'); }}
                >
                  Bone Health
                </NavLink>
              </NavItem>
              <NavItem>
                <NavLink
                  active={this.state.activeTab === '5'}
                  onClick={() => { this.toggle('5'); }}
                >
                  Liver Health
                </NavLink>
              </NavItem>
              <NavItem>
                <NavLink
                  active={this.state.activeTab === '6'}
                  onClick={() => { this.toggle('6'); }}
                >
                  Hormone Health
                </NavLink>
              </NavItem>
            </Nav>
            <TabContent activeTab={this.state.activeTab}>
              {this.tabPane()}
            </TabContent>
            </CardBody>
            <CardFooter>
                <div style={{ display:"flex", alignItems:'center'}}>
                <div style={{width: "30px", height: "30px", backgroundColor: '#f9d8e1', borderColor: "lightgrey", borderWidth: 1, marginRight: "10px"}}/> Data entered by patient
                <Button style={{width: "30px", height: "30px", backgroundColor: '#ffffff', borderColor: "lightgrey", borderWidth: 1, marginLeft: "20px", marginRight: "10px"}}/> Data entered by clincian
                </div>
              </CardFooter>
            </Card>
        </Row>
        }
      </div>

    );
  }
}

function mapStateToProps(state) {
  return {
      patients: state.comms.patientList.patientList.profiles,
      patientData: state.comms.patientData.patientData,
      isLoading: state.comms.patientData.isLoading,
      latestData: state.comms.patientData.latestResults,
      hasError: state.comms.patientData.hasError,
      hcpData: state.comms.hcpData.hcpProfile,
  };
}

const connectedPatientData = connect(mapStateToProps)(PatientData);
export default connectedPatientData;
