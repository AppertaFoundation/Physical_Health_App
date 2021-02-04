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
import PairedListItem from "./components/PairedListItem";
import TestDataListItem from "./components/TestDataListItem";
import { connect } from "react-redux";
import { RaisedTextButton } from "react-native-material-buttons";
import {
  VictoryLine,
  VictoryChart,
  VictoryTheme,
  VictoryAxis,
  VictoryLegend
} from "victory-native";
import { getLocalDateString, getLocalTimeString } from "../utils/DateUtils";
import { PatientDataActions } from "nhsphysicalhealthcomms";
import R from "res/R";
import GradientButton from "../utils/GradientButton";
import {convertKgsToStonesAndPounds} from "../utils/WeightConversionUtils";
import {getStonesDecimal} from "../utils/WeightConversionUtils";

class ViewBmiWeightScreen extends Component {
  static navigationOptions = {
    title: "View Data"
  };

  state = {
    patientData: [],
    filterDays: 10000
  };

  componentDidMount() {
    this.fetchData();
    this.willFocusSubscription = this.props.navigation.addListener(
      'willFocus',
      () => {
        this.fetchData();
      }
    );
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.props.patientData != prevProps.patientData) {
      this.setState({ patientData: this.props.patientData });

      var height = this.props.height;
      if (height != null) {
        var height = parseInt(height, 10);

        if (!isNaN(height)) {

          var heightMetres = height / 100;

          var bmi = this.props.patientData.map((data) => {
            var weightFloat = parseFloat(data.result);
            console.log(weightFloat);
            var bmi = weightFloat / (heightMetres * heightMetres);
            bmi = (Math.round(bmi * 100) / 100).toFixed(2);
            console.log(bmi);

            var item = {
              date: data.data,
              value: bmi
            }

            return item;
          });

          this.setState({ bmiData: bmi });
        }
      }
    }
  }

  componentWillUnmount() {
    this.willFocusSubscription.remove();
  }

  fetchData = () => {
    this.props.fetchSelfMonitoringData('weight');
  }

  onEditPressed = (date, value, compositionId) => {
    console.log("Edit Pressed");
    console.log(date);
    console.log(value);

    const { navigation } = this.props;
    navigation.navigate("AddPatientData", {
      dataCode: [{ code: "weight" }],
      date: date,
      values: [value],
      compositionId: compositionId
    });
  };

  patientDataOutput = () => {
    if (this.state.bmiData == null) {
      return (
        <FlatList
          style={R.styles.listContainer}
          data={this.state.patientData}
          keyExtractor={(item, index) => index.toString()}
          renderItem={({ item, index }) => {
            var weight = item.result + " kg";
            if ( this.props.weightImperial ){
              var imperialWeight = convertKgsToStonesAndPounds(item.result);
              weight = imperialWeight.stones + " st " + imperialWeight.pounds + " lb";
            }
            return (
              <TestDataListItem
                date={getLocalDateString(item.date)}
                time={getLocalTimeString(item.date)}
                value={weight}
                onEditPressed={() => this.onEditPressed(item.date, item.result, item.compositionId)}
              />
            )
          }}
        />
      );
    } else {
      return (
        <FlatList
          style={R.styles.listContainer}
          data={this.state.patientData}
          keyExtractor={(item, index) => index.toString()}
          renderItem={({ item, index }) => {
            var weight = item.result + " kg";
            if ( this.props.weightImperial ){
              var imperialWeight = convertKgsToStonesAndPounds(item.result);
              weight = imperialWeight.stones + " st " + imperialWeight.pounds + " lb";
            }
            return (
              <PairedListItem
                date={getLocalDateString(item.date)}
                time={getLocalTimeString(item.date)}
                value={weight}
                secondValue={this.state.bmiData[index].value}
                onEditPressed={() => this.onEditPressed(item.date, item.result, item.compositionId)}
              />
            )
          }}
        />
      );
    }
  };

  patientGraphOutput = () => {
    console.log("TEST DETAILS");
    console.log(this.state.patientData);

    if (
      this.state.patientData.length == 0 ||
      this.state.patientData[0].result == null
    ) {
      return;
    }

    var width = (Dimensions.get("window").width - 40);

    var earliestDate = this.state.patientData[0].date;

    var startDate = new Date();
    var day = startDate.getDate();
    startDate.setDate(day - this.state.filterDays);

    console.log(startDate);
    console.log(earliestDate);

    if (earliestDate < startDate) {
      earliestDate = startDate;
    }

    var tickValues = [];

    var bmiData = this.state.bmiData;

    var data = this.state.patientData.map((value, index, array) => {
      var date = array[index].date;

      if (date < earliestDate) {
        return null;
      }

      if (array[index].result == null) {
        return null;
      }

      days = (date - earliestDate) / (1000 * 60 * 60 * 24);

      var weight = parseFloat(array[index].result);
      if ( this.props.weightImperial ){
        weight = getStonesDecimal(weight);
      }

      if (bmiData != null) {
        return {
          date: days,
          value: weight,
          value2: parseFloat(bmiData == null ? 40 : bmiData[index].value),
        };
      } else {
        return {
          date: days,
          value: weight
        };
      }
    });

    // Remove null from array
    data = data.filter(item => {
      return item !== null;
    });

    console.log(data);

    // Evenly space the tick values.
    var date = new Date();
    tickValues.push(0);
    tickValues.push(Math.floor((date - earliestDate) / (1000 * 60 * 60 * 24)));
    tickValues.push(Math.floor((date - earliestDate) / (1000 * 60 * 60 * 48)));
    tickValues.push(
      Math.floor((date - earliestDate) / ((1000 * 60 * 60 * 24 * 4) / 3))
    );
    tickValues.push(
      Math.floor((date - earliestDate) / (1000 * 60 * 60 * 24 * 4))
    );


    var units = this.state.patientData[0].units;
    if ( this.props.weightImperial ){
      units = "stone"
    }

    return (
      <View style={[styles.container, { marginTop: 30 }]} pointerEvents="none">
        <VictoryChart
          height={width}
          width={width}
          theme={VictoryTheme.material}
          domainPadding={{ y: [20, 20] }}
          padding={{top: 50, bottom: 60, left: 50, right: 60}}
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
            tickCount={5}
            tickValues={tickValues}
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
          {this.state.bmiData != null ?
            <VictoryAxis dependentAxis orientation="right" />
            : null}
          {this.state.bmiData != null ? <VictoryLine
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
            data={[{ name: "Weight" + " (" + units + ")" }]}
          />
          {this.state.bmiData != null ?
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

  onOpenGraphPressed = () => {
    var props = this.props;
    const { navigation } = this.props;
    const dataTitle = navigation.getParam("dataTitle", "");

    console.log("Button pressed");
    props.navigation.navigate("ViewWeightBmiDataGraph", {
      dataTitle: dataTitle
    })
  };

  render() {
    var height = parseInt(this.props.height, 10);
    var data = this.state.patientData;
    var showBmi = !isNaN(height) && (data != null && data.length > 0);
    var title = showBmi ? "Weight and BMI" : "Weight";

    return (
      <SafeAreaView style={R.styles.container}>
        <ScrollView horizontal={false} style={R.styles.scrollcontainer}>
          <Text style={R.styles.header}>{title}</Text>
          <View
            style={{
              width: "100%",
              flexDirection: "row"
            }}
          >
            <Text style={{ width: 100, fontWeight: "bold" }}>Date</Text>
            <Text style={{ width: 60, fontWeight: "bold", textAlign: 'right' }}>Time</Text>
            <Text style={{ flex: 1, fontWeight: "bold", textAlign: 'right' }}>Weight</Text>
            {this.state.bmiData != null ?
              <Text style={{ flex: 1, fontWeight: "bold", textAlign: 'right' }}>BMI</Text>
              : null
            }
            <View style={{ flex: 0.25 }} />
            <View style={{ width: 24 }} />
          </View>
          <View style={{ width: "100%" }}>{this.patientDataOutput()}</View>
          <View style={{ flex: 1, width: "100%" }} >{this.patientGraphOutput()}</View>
          <GradientButton
            style={[R.styles.button, { flex: 1, width: "100%" }]}
            onPress={() => this.onOpenGraphPressed()}
            title="Open full chart"
            color={R.colors.highlightColorOne}
            titleColor="white"
          />
          <Text style={[R.styles.body, { paddingBottom: 20, paddingTop: 20 }]}>
            {" "}
            Information about individual tests, their methodology and why they
            are done can go here if this information can be sourced from the
            NHS.{" "}
          </Text>
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
    isLoading: state.comms.patientData.isLoading,
    height: state.local.height,
    weightImperial: state.local.weightImperial
  };
};

const mapDispatchToProps = dispatch => {
  return {
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
)(ViewBmiWeightScreen);
