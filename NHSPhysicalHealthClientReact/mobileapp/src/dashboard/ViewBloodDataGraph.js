import React, { Component } from "react";
import {
  View,
  Text,
  FlatList,
  SafeAreaView,
  ScrollView,
  Dimensions,
  StyleSheet,
  Alert
} from "react-native";
import TestDataListItem from "./components/TestDataListItem";
import { connect } from "react-redux";
import { RaisedTextButton } from "react-native-material-buttons";
import {
  VictoryLine,
  VictoryChart,
  VictoryTheme,
  VictoryAxis,
  VictoryLegend,
  VictoryZoomContainer
} from "victory-native";
import { getLocalDateString } from "../utils/DateUtils";
import { PatientDataActions } from "nhsphysicalhealthcomms";
import R from "res/R";
import GradientButton from "../utils/GradientButton";

class ViewBloodDataGraph extends Component {
  static navigationOptions = {
    title: "View Data"
  };

  state = {
    filterDays: 10000,
    zoomDomain: {},
    displayData: null,
    tickValues: null,
    earliestDate: null,
    minValue: null,
    maxValue: null
  }

  componentDidMount() {
    const { navigation } = this.props;
    this.updateGraphData();
  }

  componentDidUpdate(prevProps) {
    if (prevProps == this.props) {
      return;
    }

    this.updateGraphData();
  }

  updateGraphData = () => {
    console.log("TEST DETAILS");

    var data = this.props.patientData;
    console.log(data);

    var earliestDate = data[0].date;

    var startDate = new Date();
    var day = startDate.getDate();
    startDate.setDate(day - this.state.filterDays);

    console.log("START DATE");
    console.log(startDate);

    console.log("EARLIEST DATE");
    console.log(earliestDate);

    if (earliestDate < startDate) {
      earliestDate = startDate;
    }

    var tickValues = [];

    var maxValue = null;
    var minValue = null;

    var bloodPressureDiastolic = data.map(
      (value, index, array) => {
        var date = array[index].date;

        if (date < earliestDate) {
          return null;
        }

        days = (date - earliestDate) / (1000 * 60 * 60 * 24);

        var value = parseFloat(array[index].bloodPressureDiastolic)
        if (value > maxValue || maxValue == null) {
          maxValue = value;
        }

        if (value < minValue || minValue == null) {
          minValue = value;
        }


        return {
          date: days,
          bloodPressureDiastolic: parseInt(
            array[index].bloodPressureDiastolic
          )
        };
      }
    );

    var bloodPressureSystolic = data.map(
      (value, index, array) => {
        var date = array[index].date;

        if (date < earliestDate) {
          return null;
        }

        days = (date - earliestDate) / (1000 * 60 * 60 * 24);

        var value = parseFloat(array[index].bloodPressureSystolic)
        if (value > maxValue || maxValue == null) {
          maxValue = value;
        }

        if (value < minValue || minValue == null) {
          minValue = value;
        }

        return {
          date: days,
          bloodPressureSystolic: parseInt(array[index].bloodPressureSystolic)
        };
      }
    );

    // Remove null from array
    bloodPressureDiastolic = bloodPressureDiastolic.filter(item => {
      return item != null;
    });
    bloodPressureSystolic = bloodPressureSystolic.filter(item => {
      return item != null;
    });

    console.log(bloodPressureDiastolic);
    console.log(bloodPressureSystolic);

    // Evenly space the tick values.
    var date = new Date();
    tickValues.push(
      Math.floor((date - earliestDate) / (1000 * 60 * 60 * 24))
    );

    this.setState({
      tickValues: tickValues,
      bloodPressureDiastolic: bloodPressureDiastolic,
      bloodPressureSystolic: bloodPressureSystolic,
      earliestDate: earliestDate,
      minValue: minValue,
      maxValue: maxValue
    })
  }

  onFilterPressed = filter => {
    console.log(filter);
    this.setState({
      filterDays: filter,
      zoomDomain: {
        x: [this.state.tickValues[0] - filter, this.state.tickValues[0]],
        y: [this.state.minValue, this.state.maxValue]
      }
    });
    //    this.updateGraphData();
  };

  onZoomDomainChange = domain => {
    console.log("DOMAIN CHANGE");
    console.log(domain)
    this.setState({ zoomDomain: domain });
  }

  patientGraphOutput = () => {
    var bloodPressureDiastolic = this.state.bloodPressureDiastolic;
    var bloodPressureSystolic = this.state.bloodPressureSystolic;

    if (bloodPressureDiastolic == null
      || bloodPressureSystolic == null) {
      return;
    }

    var tickValues = this.state.tickValues;
    var earliestDate = this.state.earliestDate;

    var width = Dimensions.get("window").width - 40;

    var units = "kdjdjdj";
    console.log("units");
    console.log(units);

    const { navigation } = this.props;
    const title = navigation.getParam("dataTitle", "");

    return (
      <View style={[styles.container, { marginTop: 30 }]}>
        <VictoryChart
          height={width}
          width={width}
          theme={VictoryTheme.material}
          domainPadding={{y: [20, 20]}}
          padding={{top: 50, bottom: 60, left: 50, right: 60}}
          containerComponent={
            <VictoryZoomContainer
              zoomDomain={this.state.zoomDomain}
              onZoomDomainChange={this.onZoomDomainChange}
            />
          }
        >
          <VictoryAxis
            style={{
              tickLabels: {
                padding: 1,
                angle: 45,
                verticalAnchor: "middle",
                textAnchor: "start"
              }
            }}
            tickFormat={t => {
              var result = new Date(earliestDate);
              result.setDate(result.getDate() + t);
              return getLocalDateString(result);
            }}
          />
          <VictoryAxis dependentAxis />
          {bloodPressureDiastolic.length > 1 ?
            <VictoryLine
              data={bloodPressureDiastolic}
              x="date"
              y="bloodPressureDiastolic"
              style={{
                data: { stroke: "#c43a31" },
                parent: { border: "1px solid #ccc" }
              }}
            /> : null}
          {bloodPressureSystolic.length > 1 ?
            <VictoryLine
              data={bloodPressureSystolic}
              x="date"
              y="bloodPressureSystolic"
              style={{
                data: { stroke: "blue" },
                parent: { border: "1px solid #ccc" }
              }}
            /> : null}
          <VictoryLegend
            x={70}
            y={10}
            orientation="horizontal"
            gutter={20}
            style={{ border: { stroke: "black" } }}
            colorScale={["#c43a31", "blue"]}
            data={[{ name: "Diastolic BP" }, { name: "Systolic BP" }]}
          />
        </VictoryChart>
        <Text style={[{ marginTop: 10, marginBottom: 20 }]}>Date</Text>
      </View>
    );
  };

  render() {
    const { navigation } = this.props;
    const title = navigation.getParam("dataTitle", "");

    return (
      <SafeAreaView style={R.styles.container}>
        <ScrollView horizontal={false}>
          <Text style={[R.styles.header, { paddingLeft: 20, paddingTop: 20 }]}>Blood Pressure</Text>

          <View style={{ flexDirection: "row", width: "100%", padding: 20 }}>
            <GradientButton
              style={[R.styles.button, { flex: 1, width: "20%" }]}
              onPress={() => this.onFilterPressed(7)}
              title="1 week"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
            <GradientButton
              style={[
                R.styles.button,
                { flex: 1, marginLeft: 10, marginRight: 10, width: "20%" }
              ]}
              onPress={() => this.onFilterPressed(30)}
              title="1 month"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
            <GradientButton
              style={[R.styles.button, { flex: 1, width: "20%" }]}
              onPress={() => this.onFilterPressed(365)}
              title="1 year"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
          </View>
          <View style={{ width: "100%" }}>{this.patientGraphOutput()}</View>
        </ScrollView>
      </SafeAreaView>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#f5fcff"
  }
});

const mapStateToProps = state => {
  return {
    patientData: state.comms.patientData.patientData,
    hasError: state.comms.patientData.hasError,
    isLoading: state.comms.patientData.isLoading
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchTestData: testCode => {
      dispatch(PatientDataActions.fetchTestResultsReading("", testCode));
    },
    fetchSelfMonitoringData: testCode => {
      dispatch(PatientDataActions.fetchSelfMonitoringReading("", testCode));
    },
    clearError: () => {
      dispatch(PatientDataActions.hasError(false));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ViewBloodDataGraph);
