import React, { Component } from 'react';
import { Alert, CardFooter, Card, CardBody, CardHeader, Col, Row, Button} from 'reactstrap';
import { Scatter } from 'react-chartjs-2';
import BootstrapTable from 'react-bootstrap-table-next';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

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
  dataField: 'bloodPressureSystolic',
  text: 'Systolic BP'
},{
  dataField: 'bloodPressureDiastolic',
  text: 'Diastolic BP'
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
        labelString: "mm[Hg]"
      }
    }]
},
legend: {
  display: true,
    onClick: (e) => e.stopPropagation()
}

};


class PatientData extends Component {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch(PatientDataActions.hasError(false));
    var patientUsername = this.props.match.params.username;
    console.log("Blood Pressure");
    console.log("Username = " + patientUsername);
    dispatch(PatientDataActions.fetchBloodPressureReadings(patientUsername));
}

bloodPressureGraphData = () => {
  if ( this.props.patientData.length > 0 ){
  var earliestDate = this.props.patientData[0].date;

  var bloodPressureDiastolic = this.props.patientData.map(
    (value, index, array) => {
      var date = array[index].date;
      var days = Math.floor((date - earliestDate) / (1000 * 60 * 60 * 24));

      return {
        x: date,
        y: parseInt(array[index].bloodPressureDiastolic)
      };
    }
  );

  var bloodPressureSystolic = this.props.patientData.map(
    (value, index, array) => {
      var date = array[index].date;
      var days = Math.floor((date - earliestDate) / (1000 * 60 * 60 * 24));

      return {
        x: date,
        y: parseInt(array[index].bloodPressureSystolic)
      };
    }
  );

  console.log(bloodPressureDiastolic);
  } else {
    var bloodPressureDiastolic;
    var bloodPressureSystolic
  }

  return {
    labels: ['Scatter'],
    datasets: [
      {
        label: 'Diastolic',
        fill: false,
        showLine: true, 
        backgroundColor: 'rgba(255,0,0,1)',
        borderColor: 'rgba(255,0,0,0.4)',
        pointBorderColor: 'rgba(255,0,0,1)',
        pointBackgroundColor: '#fff',
        pointBorderWidth: 1,
        pointHoverRadius: 5,
        pointHoverBackgroundColor: 'rgba(75,192,192,1)',
        pointHoverBorderColor: 'rgba(220,220,220,1)',
        pointHoverBorderWidth: 2,
        pointRadius: 4,
        pointHitRadius: 10,
        data: bloodPressureDiastolic
      },
      {
        label: 'Systolic',
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
        data: bloodPressureSystolic
      }
    ]
  };
};

  render() {

    const { patientData } = this.props;

    var patientUsername = this.props.match.params.username;
    var addDataLink = '/patients/view/'+patientUsername+"/add";

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
                <i className="fa fa-align-justify"></i> Blood Pressure
              </CardHeader>
              <CardBody>
              <div className="chart-wrapper">
              <Scatter data={this.bloodPressureGraphData()} options={options}/>
              </div>
              </CardBody>
            </Card>
          </Col>
        </Row>
        <Row>
          <Col>
            <Card>
              <CardHeader>
                <i className="fa fa-align-justify"></i> Patient Data
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
