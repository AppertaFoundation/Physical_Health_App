import React, { Component } from 'react';
import { Alert, Card, CardBody, CardHeader, Col, Row, Button } from 'reactstrap';
import BootstrapTable from 'react-bootstrap-table-next';
import ToolkitProvider, { Search } from 'react-bootstrap-table2-toolkit';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

import { PatientListActions } from 'nhsphysicalhealthcomms';

const { SearchBar, ClearSearchButton } = Search;

function rankFormatter(cell, row, rowIndex, formatExtraData) {
  var link = '/patients/view/' + cell;
  return (
    <Link to={link}>
      <Button block color="primary">View</Button>
    </Link>
  );
}

function nameFormatter(cell, row) {
  return `${row.firstNames} ${row.lastName}`
}

function dateFormatter(cell, row) {
  var dob = new Date(cell);
  return (
    <span>{dob.toLocaleDateString()}</span>
  );
}

function ageFormatter(cell, row) {
  var dob = new Date(cell);
    var diff_ms = Date.now() - dob.getTime();
    var age_dt = new Date(diff_ms); 
  
    var ageYears =  Math.abs(age_dt.getUTCFullYear() - 1970);

  return (
    <span>{ageYears}</span>
  );
}


const columns = [ {
  dataField: 'lastName',
  text: 'Patient Name ',
  formatter: nameFormatter,
  sort: true,
  filterValue: nameFormatter,
  sortCaret: (order, column) => {
    if (!order) return (<i className="fa fa-unsorted fa-lg mt-4"></i>);
    else if (order === 'asc') return (<i className="fa fa-sort-asc fa-lg mt-4"></i>);
    else if (order === 'desc') return (<i className="fa fa-sort-desc fa-lg mt-4"></i>);
    return null;
  }
},{
  dataField: 'dateOfBirth',
  text: 'Date Of Birth    ',
  formatter: dateFormatter,
  sort: true,
  searchable: false,
  sortCaret: (order, column) => {
    if (!order) return (<i className="fa fa-unsorted fa-lg mt-4"></i>);
    else if (order === 'asc') return (<i className="fa fa-sort-asc fa-lg mt-4"></i>);
    else if (order === 'desc') return (<i className="fa fa-sort-desc fa-lg mt-4"></i>);
    return null;
  }
}, {
  dataField: 'dateOfBirth',
  text: 'Age ',
  formatter: ageFormatter,
  sort: true,
  searchable: false,
  sortCaret: (order, column) => {
    if (!order) return (<i className="fa fa-unsorted fa-lg mt-4"></i>);
    else if (order === 'asc') return (<i className="fa fa-sort-asc fa-lg mt-4"></i>);
    else if (order === 'desc') return (<i className="fa fa-sort-desc fa-lg mt-4"></i>);
    return null;
  }
}, {
  dataField: "username",
  text: '',
  searchable: false,
  formatter: rankFormatter
}];

class Patients extends Component {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch(PatientListActions.getPatientList(100, 0));
  }

  render() {

    const { patients } = this.props;
    console.log(patients);

    return (
      <div className="animated fadeIn">
        {
          this.props.hasError
            ? <Alert color="danger">
              Error fetching data
          </Alert>
            : null
        }
        <Row>
          <Col xs="12" lg="6">
            <Card>
              <CardHeader>
                <i className="fa fa-align-justify"></i> My Patients
              </CardHeader>
              <CardBody>
                <ToolkitProvider
                  keyField="username"
                  data={patients.patientList.profiles}
                  columns={columns}
                  search
                >
                  {
                    props => (
                      <div>
                        <SearchBar {...props.searchProps} placeholder="Search Patient name"/>
                        <ClearSearchButton {...props.searchProps} />
                        <hr />
                        <BootstrapTable
                          {...props.baseProps}
                          noDataIndication={patients.patientList.profiles.length == 0 ? "You currently have no patients assigned to you" : ""}
                        />
                      </div>
                    )
                  }
                </ToolkitProvider>
              </CardBody>
            </Card>
          </Col>
        </Row>

      </div>

    );
  }
}

function mapStateToProps(state) {
  return {
    patients: state.comms.patientList,
    hasError: state.comms.patientList.hasError
  };
}

const connectedPatients = connect(mapStateToProps)(Patients);
export default connectedPatients;
