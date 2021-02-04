import React, { Component } from 'react';
import { Alert, CardFooter, Card, CardBody, CardHeader, Col, Row, Button } from 'reactstrap';
import { Scatter } from 'react-chartjs-2';
import BootstrapTable from 'react-bootstrap-table-next';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import {TestResultFields} from 'nhsphysicalhealthcomms';

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

const columns = [{
  dataField: 'date',
  text: 'Date',
  formatter: dateFormatter
},{
  dataField: 'date',
  text: 'Time',
  formatter: timeFormatter
},{
  dataField: 'composerName',
  text: "Recorded by"
},{
  dataField: 'result',
  text: 'Magnitude'
},{
  dataField: 'units',
  text: 'Units'
}
];

const options = {
  tooltips: {
      callbacks: {
          label: function(tooltipItem, data) {
             var date = new Date(tooltipItem.xLabel);
             var datestring = date.toLocaleDateString();
              var label = data.datasets[tooltipItem.datasetIndex].label || '';

              if (label) {
                  label += ': ';
              }
              label += Math.round(tooltipItem.yLabel * 100) / 100;
              return label + "  (" + datestring + ")";
          }
      }
  },
  scales: {
    xAxes: [
      {
          ticks: {
             callback: function(label, index, labels) {
               var date = new Date(label);
              var datestring = date.toLocaleDateString();
               return datestring;
             }
          }
      }
    ],
    yAxes: [{
      scaleLabel: {
        display: true,
        labelString: ''
      }
    }]
},
legend: {
  display: true,
    onClick: (e) => e.stopPropagation()
}
};


class PatientData extends Component {
  constructor(props) {
    super(props);

    var testCode = this.props.match.params.testcode;
    console.log("testcode = " + testCode);

    var lookup = {};
    var isSM = false;

    if ( testCode == 'weight' ){
      lookup.testName = 'Weight';
      isSM = true;
    } else if (testCode == 'bmi'){
      lookup.testName = 'BMI';
      isSM = true;
    } else if (testCode == 'qrisk'){
      lookup.testName = 'QRisk Score';
      isSM = true;
       } else {
        var testResultFields= TestResultFields.find(element => element.analyte_name_code == testCode);
        console.log(testResultFields);

        lookup.testName = testResultFields.localised_analyte_name;
       }

    this.state = {
       testInfo : lookup,
       testCode : testCode,
       isSelfMonitoring : isSM
    };
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch(PatientDataActions.hasError(false));
    var patientUsername = this.props.match.params.username;
    console.log("Username = " + patientUsername);
    console.log("testCode = " + this.state.testCode);

    if ( this.state.isSelfMonitoring ){
      dispatch(PatientDataActions.fetchSelfMonitoringReading(patientUsername, this.state.testCode));
    } else {
      dispatch(PatientDataActions.fetchTestResultsReading(patientUsername, this.state.testCode));
    }
}

testGraphData = () => {
  console.log("TEST RESULTS");
  console.log(this.props.patientData);

  if ( this.props.patientData.length > 0 ){
  var earliestDate = this.props.patientData[0].date;

  var testData = this.props.patientData.map(
    (value, index, array) => {
      var date = array[index].date;
      var days = Math.floor((date - earliestDate) / (1000 * 60 * 60 * 24));

      return {
        x: date,
        y: parseFloat(array[index].result)
      };
    }
  );

  } else {
    console.log("NONE FOUND");
    var testData;
  }

  return {
    labels: ['Scatter'],
    datasets: [
      {
        label: this.state.testInfo.testName,
        fill: false,
        showLine: true,  //!\\ Add this line
        backgroundColor: 'rgba(0,0,255,1)',
        borderColor: 'rgba(0,0,255,0.4)',
        pointBorderColor: 'rgba(0,0,255,1)',
        pointBackgroundColor: '#fff',
        pointBorderWidth: 1,
        pointHoverRadius: 5,
        pointHoverBackgroundColor: 'rgba(75,192,192,1)',
        pointHoverBorderColor: 'rgba(220,220,220,1)',
        pointHoverBorderWidth: 2,
        pointRadius: 4,
        pointHitRadius: 10,
        data: testData
      }
    ]
  };
};



  render() {

    const { patientData } = this.props;

    const { testInfo } = this.state;

    var patientUsername = this.props.match.params.username;
    var addDataLink = '/patients/view/'+patientUsername+"/add";

    // Set y axis label
    var options2 = Object.assign({}, options); 
    if ( patientData.length > 0 ){
      options2.scales.yAxes[0].scaleLabel.labelString = patientData[0].units;
    }

    const rowStyle2 = (row, rowIndex) => {
      const style = {};
  
      console.log(row);
  
      if (row.composerName != "Patient" ) {
        style.backgroundColor = '#ffffff';
      } else {
        style.backgroundColor = '#f9d8e1';
      }
    
      return style;
    };

    return (
      <div className="animated fadeIn">
      {
          this.props.hasError
            ?  <Alert color="danger">
            Error fetching data
          </Alert>
            : null
        }
        <Row>
          <Col>
            <Card>
              <CardHeader>
                <i className="fa fa-align-justify"></i> {testInfo.testName}
              </CardHeader>
              <CardBody>
              <div className="chart-wrapper">
              <Scatter data={this.testGraphData()} options={options2}/>
              </div>
              </CardBody>
            </Card>
          </Col>
        </Row>
        <Row>
          <Col>
            <Card>
              <CardHeader>
                <i className="fa fa-align-justify"></i> History
              </CardHeader>
              <CardBody>
                <p>
              <Link to={addDataLink}>
                <Button size="lg" color="primary"><i className="fa"></i> Add Data </Button>
              </Link>
              </p>
              <BootstrapTable keyField='id' data={ patientData } columns={ columns } rowStyle={ rowStyle2 }/>
              </CardBody>
              <CardFooter>
                <div style={{ display:"flex", alignItems:'center'}}>
                <div style={{width: "30px", height: "30px", backgroundColor: '#f9d8e1', borderColor: "lightgrey", borderWidth: 1, marginRight: "10px"}}/> Data entered by patient
                <Button style={{width: "30px", height: "30px", backgroundColor: '#ffffff', borderColor: "lightgrey", borderWidth: 1, marginLeft: "20px", marginRight: "10px"}}/> Data entered by clincian
                </div>
              </CardFooter>
            </Card>
          </Col>
        </Row>
      </div>

    );
  }
}

function mapStateToProps(state) {
  return {
      patientData: state.comms.patientData.patientData,
      hasError: state.comms.patientData.hasError
  };
}

const connectedPatientData = connect(mapStateToProps)(PatientData);
export default connectedPatientData;