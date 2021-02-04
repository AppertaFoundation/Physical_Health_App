import React, { Component } from "react";
import {
  View,
  Linking,
  Text,
  FlatList,
  SafeAreaView,
  ScrollView,
  ImageBackground,
  Dimensions,
  TouchableOpacity,
  StyleSheet,
  Alert
} from "react-native";
import BloodListItem from "./components/BloodListItem";
import { connect } from "react-redux";
import {
  VictoryLine,
  VictoryChart,
  VictoryTheme,
  VictoryAxis,
  VictoryLegend
} from "victory-native";
import { RaisedTextButton } from "react-native-material-buttons";
import { PatientDataActions } from "nhsphysicalhealthcomms";
import R from "res/R";
import { getLocalDateString, getLocalTimeString } from "../utils/DateUtils";
import GradientButton from "../utils/GradientButton";

class ViewBloodDetailsScreen extends Component {
  static navigationOptions = {
    title: "Blood Pressure"
  };

  state = {
    patientData: [],
    filterDays: 365
  };

  componentDidMount() {
    this.props.fetchBloodData();
    this.willFocusSubscription = this.props.navigation.addListener(
      'willFocus',
      () => {
        this.props.fetchBloodData();
      }
    );
  }

  componentWillUnmount() {
    this.willFocusSubscription.remove();
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if ( this.props.patientData != prevProps.patientData){
      this.setState( {patientData : this.props.patientData});
    }
  }

  onFilterPressed = filter => {
    console.log(filter);
    this.setState({ filterDays: filter });
  };

  onEditPressed = (date, systolic,diastolic ,compositionId ) => {
    console.log("Edit Pressed");
    console.log(date);

    const { navigation } = this.props;
    navigation.navigate("AddPatientData",{
      dataCode: [
        {code: "systolic"},
        {code: "diastolic"}],
      date: date,
      values: [systolic, diastolic ],
      compositionId: compositionId
    });
  };

  patientDataOutput = () => {
    console.log(this.state.patientData);
    return (
      <FlatList
        style={R.styles.listContainer}
        data={this.state.patientData}
        keyExtractor={(item, index) => index.toString()}
        renderItem={({ item, index }) => (
          <BloodListItem
            date={getLocalDateString(item.date)}
            time={getLocalTimeString(item.date)}
            systolic={item.bloodPressureSystolic}
            diastolic={item.bloodPressureDiastolic}
            onEditPressed={() => this.onEditPressed(item.date, item.bloodPressureSystolic, item.bloodPressureDiastolic, item.compositionId)}
          />
        )}
      />
    );
  };

  patientGraphOutput = () => {
    console.log("BLOOD DETAILS");
    if (
      this.state.patientData.length > 0 &&
      this.state.patientData[0].bloodPressureDiastolic != null
    ) {
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

      var bloodPressureDiastolic = this.state.patientData.map(
        (value, index, array) => {
          var date = array[index].date;

          if (date < earliestDate) {
            return null;
          }

          if ( array[index].bloodPressureDiastolic == null ){
            return null;
          }

          days = (date - earliestDate) / (1000 * 60 * 60 * 24);

          return {
            date: days,
            bloodPressureDiastolic: parseInt(
              array[index].bloodPressureDiastolic
            )
          };
        }
      );

      var bloodPressureSystolic = this.state.patientData.map(
        (value, index, array) => {
          var date = array[index].date;

          if (date < earliestDate) {
            return null;
          }

          if ( array[index].bloodPressureSystolic == null ){
            return null;
          }

          days = (date - earliestDate) / (1000 * 60 * 60 * 24);

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
      tickValues.push(0);
      tickValues.push(
        Math.floor((date - earliestDate) / (1000 * 60 * 60 * 24))
      );
      tickValues.push(
        Math.floor((date - earliestDate) / (1000 * 60 * 60 * 48))
      );
      tickValues.push(
        Math.floor((date - earliestDate) / ((1000 * 60 * 60 * 24 * 4) / 3))
      );
      tickValues.push(
        Math.floor((date - earliestDate) / (1000 * 60 * 60 * 24 * 4))
      );

      var width = (Dimensions.get("window").width - 40);

      return (
        <View style={styles.container} pointerEvents="none">
          <VictoryChart
            width={width}
            height={width}
            theme={VictoryTheme.material}
            domainPadding={{y: [20, 20]}}
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
            { bloodPressureSystolic.length > 1 ?
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
    }
  };

  onOpenGraphPressed = () => {
    var props = this.props;
    const { navigation } = this.props;
    const dataTitle = navigation.getParam("dataTitle", "");

    console.log("Button pressed");  
              props.navigation.navigate("ViewBloodDataGraph", {
                dataTitle: dataTitle
              })
  };

  render() {
    return (
      <SafeAreaView style={R.styles.container}>
        <ScrollView horizontal={false} style={R.styles.scrollcontainer}>
          <View style={{paddingBottom:20}}>
            <Text style={R.styles.header}>My blood pressure record</Text>
            <View
              style={{
                width: "100%",
                flexDirection: "row"
              }}
            >
              <Text style={{ width: 100, fontWeight: "bold" }}>Date</Text>
              <Text style={{ width: 60, fontWeight: "bold", textAlign: 'right'}}>Time</Text>
              <Text style={{ flex: 1, fontWeight: "bold", textAlign: 'right'  }}>Systolic</Text>
              <Text style={{ flex: 1, fontWeight: "bold", textAlign: 'right'  }}>Diastolic</Text>
              <View style={{ flex: 0.25}}/>
              <View style={{ width: 24}}/>
            </View>
            <View style={{ width: "100%", marginBottom: 20 }}>
              {this.patientDataOutput()}
            </View>

          <View style={{ flex: 1, width: "100%" }} >{this.patientGraphOutput()}</View>
          <GradientButton
              style={[R.styles.button, { flex: 1, width: "100%", marginBottom:20}]}
              onPress={() => this.onOpenGraphPressed()}
              title="Open full chart"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
            <Text style={R.styles.header}>What is blood pressure?</Text>
            <Text style={R.styles.body}>
              Blood pressure is a measure of the force that your heart uses to
              pump blood around your body.
            </Text>
            <Text style={R.styles.header}>How is blood pressure measured?</Text>
            <Text style={R.styles.body}>
              Blood pressure is measured in millimetres of mercury (mmHg) and is
              given as 2 figures: - systolic pressure – the pressure when your
              heart pushes blood out - diastolic pressure – the pressure when
              your heart rests between beats For example, if your blood pressure
              is "140 over 90" or 140/90mmHg, it means you have a systolic
              pressure of 140mmHg and a diastolic pressure of 90mmHg. As a
              general guide: - ideal blood pressure is considered to be between
              90/60mmHg and 120/80mmHg - high blood pressure is considered to be
              140/90mmHg or higher - low blood pressure is considered to be
              90/60mmHg or lower
            </Text>
            <Text style={R.styles.header}>High blood pressure</Text>
            <Text style={R.styles.body}>
              High blood pressure is often related to unhealthy lifestyle
              habits, such as smoking, drinking too much alcohol, being
              overweight and not exercising enough. Left untreated, high blood
              pressure can increase your risk of developing a number of serious
              long-term health conditions, such as coronary heart disease and
              kidney disease.
            </Text>
            <Text style={R.styles.header}>Low blood pressure</Text>
            <Text style={R.styles.body}>
              Low blood pressure is less common. Some medications can cause low
              blood pressure as a side effect. It can also be caused by a number
              of underlying conditions, including heart failure and dehydration.
            </Text>
            <Text style={R.styles.header}>
              Where can I find further information?
            </Text>
            <TouchableOpacity
              onPress={() =>
                Linking.openURL(
                  "https://www.nhs.uk/conditions/cardiovascular-disease/"
                )
              }
            >
              <Text style={{ color: "blue", fontSize: 16 }}>
                Cardiovascular disease (NHS)
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              onPress={() =>
                Linking.openURL(
                  "https://www.nhs.uk/conditions/blood-pressure-test/"
                )
              }
            >
              <Text style={{ color: "blue", fontSize: 16 }}>
                Blood Pressure Tests (NHS)
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              onPress={() =>
                Linking.openURL(
                  "https://www.nhs.uk/conditions/high-blood-pressure-hypertension/prevention/"
                )
              }
            >
              <Text style={{ color: "blue", fontSize: 16 }}>
                Keeping your blood pressure healthy (NHS)
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              onPress={() =>
                Linking.openURL("http://www.bloodpressureuk.org/Home")
              }
            >
              <Text style={{ color: "blue", fontSize: 16 }}>
                Blood Pressure UK
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              onPress={() =>
                Linking.openURL("http://www.bhf.org.uk/default.aspx")
              }
            >
              <Text style={{ color: "blue", fontSize: 16, paddingBottom: 20 }}>
                British Heart Foundation
              </Text>
            </TouchableOpacity>
          </View>
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
    fetchBloodData: () => {
      dispatch(PatientDataActions.fetchBloodPressureReadings());
    },
    clearError: () => {
      dispatch(PatientDataActions.hasError(false));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ViewBloodDetailsScreen);
