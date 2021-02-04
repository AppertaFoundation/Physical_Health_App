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
import {getStonesDecimal} from "../utils/WeightConversionUtils";
import InfoPopup from "./components/InfoPopup";
import LocalDataActions from "../redux/actions/LocalDataActions";

class ViewWeightBmiDataGraph extends Component {
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
    maxValue: null,
    
    infoDialogVisible: false,
  };

  componentDidMount() {
    const { navigation } = this.props;
    this.updateGraphData();

    this.setState({infoDialogVisible: !this.props.graphsInfoShown});
  }

  componentDidUpdate(prevProps) {
    if (prevProps == this.props) {
      return;
    }

    if (this.props.patientData != prevProps.patientData) {
      this.setState({ patientData: this.props.patientData });
    }

    this.updateGraphData();
  }

  updateGraphData = () => {
    console.log("TEST DETAILS");

    var data = this.props.patientData;
    console.log(data);

    var bmiData = null;
    var maxValue = null;
    var minValue = null;

    var height = this.props.height;
    if (height != null) {
      var height = parseInt(height, 10);

      if (!isNaN(height)) {
        var heightMetres = height / 100;
            
        var bmiData = this.props.patientData.map((data) => {
          var weightFloat = parseFloat(data.result);
          console.log(weightFloat);
          var bmi = weightFloat / ( heightMetres * heightMetres);
          bmi = (Math.round(bmi * 100) / 100).toFixed(2);
          console.log(bmi);

          if (bmi > maxValue || maxValue == null) {
            maxValue = bmi;
          }
    
          if (bmi < minValue || minValue == null) {
            minValue = bmi;
          }

          var item = {
            date: data.data,
            value: bmi
          }

          return item;
        });
      }
    }

    console.log("MINVALUE = " + minValue);

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

    var data = data.map((value, index, array) => {
      console.log(array);

      var date = array[index].date;

      if (date < earliestDate) {
        return null;
      }

      days = (date - earliestDate) / (1000 * 60 * 60 * 24);

      var value = parseFloat(array[index].result);

      if ( this.props.weightImperial ){
        value = getStonesDecimal(value);
      }

      if (value > maxValue || maxValue == null) {
        maxValue = value;
      }

      if (value < minValue || minValue == null) {
        minValue = value;
      }

      return {
        date: days,
        value: value,
        value2: parseFloat(bmiData == null ? 40 : bmiData[index].value),
      };
    });

    // Remove null from array
    data = data.filter(item => {
      return item !== null;
    });

    console.log(data);

    // Evenly space the tick values.
    var date = new Date();
    tickValues.push(
      Math.floor((date - earliestDate) / (1000 * 60 * 60 * 24))
    );

    this.setState(
      {
        tickValues: tickValues,
        displayData: data,
        earliestDate: earliestDate,
        minValue: minValue,
        maxValue: maxValue
      })
  }

  onFilterPressed = filter => {
    console.log(filter);
    this.setState({ filterDays: filter, zoomDomain: { x: [this.state.tickValues[0] - filter, this.state.tickValues[0]], y: [this.state.minValue, this.state.maxValue] } });
    //    this.updateGraphData();
  };

  onZoomDomainChange = domain => {
    console.log("DOMAIN CHANGE");
    console.log(domain)
    this.setState({ zoomDomain: domain });
  }

  patientGraphOutput = () => {
    var data = this.state.displayData;

    if (
      data == null
    ) {
      return;
    }

    var tickValues = this.state.tickValues;
    var earliestDate = this.state.earliestDate;

    var height = parseInt(this.props.height, 10);
    var showBmi = !isNaN(height) && data.length > 0;

    var width = Dimensions.get("window").width - 40;

    var units = this.props.patientData[0].units;
    if ( this.props.weightImperial ){
      units = "stone"
    }
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
          {data.length > 1 ? <VictoryLine
            data={data}
            x="date"
            y="value"
            style={{
              data: { stroke: "#c43a31" },
              parent: { border: "1px solid #ccc" }
            }}
          /> : null}
           { showBmi ?
        <VictoryAxis dependentAxis orientation="right"/>
        : null}
          {showBmi ? <VictoryLine
            data={data}
            x="date"
            y="value2"
            style={{
              data: { stroke: "#3633ff" },
              parent: { border: "1px solid #ccc" }
            }}
          /> : null}
          <VictoryLegend
            x={70}
            y={10}
            orientation="horizontal"
            gutter={20}
            style={{ border: { stroke: "black" } }}
            colorScale={["#c43a31"]}
            data={[{ name: title + " (" + units + ")" }]}
          />
           { showBmi ?
          <VictoryLegend
            x={200}
            y={10}
            orientation="horizontal"
            gutter={20}
            style={{ border: { stroke: "black" } }}
            colorScale={["#3633ff"]}
            data={[{ name: "BMI" }]}
          />
          : null}
        </VictoryChart>
        <Text style={[{ marginTop: 10, marginBottom: 20 }]}>Date</Text>
      </View>
    );
  };

  onHideInfoDialog = () => {
    this.setState({infoDialogVisible: false});
    this.props.infoDialogShown();
  }

  render() {
    var height = parseInt(this.props.height, 10);
    var data = this.state.displayData;
    var showBmi = !isNaN(height) && (data != null && data.length > 0);
    var title = showBmi ? "Weight and BMI" : "Weight";

    return (
      <SafeAreaView style={R.styles.container}>
        <ScrollView horizontal={false}>
          <Text style={[R.styles.header, {paddingLeft: 20, paddingTop: 20}]}>{title}</Text>
          <View style={{ flexDirection: "row", width: "100%", padding: 10 }}>
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
        <InfoPopup
          message="Chart my health helps you to visualise your health data by displaying it on an interactive graph. You can select various timeframes, swipe your finger to pan the graph and pinch to zoom in and out"
          onHideInfoDialog={() => {this.onHideInfoDialog()}} 
          isVisible={this.state.infoDialogVisible}         
          />
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
    isLoading: state.comms.patientData.isLoading,
    height: state.local.height,
    weightImperial: state.local.weightImperial,
    graphsInfoShown: state.local.graphsInfoShown
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
    },
    infoDialogShown: () => {
      dispatch(LocalDataActions.graphsInfoShown());
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ViewWeightBmiDataGraph);
