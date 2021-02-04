import React, { Component } from "react";
import {
  View,
  Text,
  ScrollView,
  Alert,
  FlatList,
  StyleSheet
} from "react-native";
import { RaisedTextButton } from "react-native-material-buttons";
import PatientListItem from "./components/PatientListItem";
import Loader from "../utils/Loader";
import { connect } from "react-redux";
import { PatientDataActions } from "nhsphysicalhealthcomms";
import { tab_layout as TAB_TEST_FIELDS } from "./SupportedFieldsConstants";
import { supported_fields as SUPPORTED_FIELDS } from "./SupportedFieldsConstants";
import {getLocalDateTimeString} from "../utils/DateUtils";
import MaterialIcon from "react-native-vector-icons/MaterialIcons";
import GradientButton from "../utils/GradientButton";

import R from "res/R";

const conversionObject = {
  GeneralHealth: {
    index: 0,
    title: "General Health"
  },
  HeartHealth: { index: 1, title: "Heart Health" },
  KidneyHealth: { index: 2, title: "Kidney Health" },
  BoneHealth: { index: 3, title: "Bone Health" },
  LiverHealth: { index: 4, title: "Liver Health" },
  HormoneHealth: { index: 5, title: "Hormone Health" }
};

const TAB_ARRAY = ["GeneralHealth",
  "HeartHealth",
  "KidneyHealth",
  "BoneHealth",
  "LiverHealth",
  "HormoneHealth"];

class ViewByMetricScreen extends Component {
  static navigationOptions = {
    title: "Data"
  };

  state = {};

  constructor(props) {
    super(props);

    const tabKey = this.props.navigation.state.key;
    this.onLeftPressed = this.onLeftPressed.bind(this);
    this.onRightPressed = this.onRightPressed.bind(this);
  }

  componentDidMount() {
    const tabKey = this.props.navigation.state.key;
    var tabData = TAB_TEST_FIELDS[conversionObject[tabKey].index];

    var firstTab = tabKey == "GeneralHealth";
    var lastTab = tabKey == "HormoneHealth";

    this.setState({
      tabData: tabData,
      title: conversionObject[tabKey].title,
      firstTab: firstTab,
      lastTab: lastTab
    });

    if (firstTab) {
      // Request data on 1st tab only
      this.props.fetchPatientData();
    }
  }

  componentDidUpdate(prevProps) {
    if (this.props.hasError && this.state.firstTab) {
      // Display error within alert
      Alert.alert(
        "",
        "Unable to load patient data, please check you have a network connection and retry",
        [
          {
            text: "OK",
            onPress: () => {
              this.props.clearError();
            }
          }
        ],
        { cancelable: false }
      );
    }
  }

  onLeftPressed() {
    const tabKey = this.props.navigation.state.key;
    var index = conversionObject[tabKey].index - 1;
    this.props.navigation.navigate(TAB_ARRAY[index]);
  }

  onRightPressed() {
    const tabKey = this.props.navigation.state.key;
    var index = conversionObject[tabKey].index + 1;
    this.props.navigation.navigate(TAB_ARRAY[index]);
  }

  patientDataOutput = () => {
    var resultsArray = this.props.latestData;

    var height = parseInt(this.props.height, 10);
    var showBmi = !isNaN(height);

    var tabArray = [];
    if (this.state.tabData != null) {
      var arrayLength = resultsArray.length;
      for (var i = 0; i < arrayLength; i++) {
        var resultItem = resultsArray[i];
        var found = this.state.tabData.find(function(element) {
          return element.code == resultItem.code;
        });

        // Do not show BMI if user has not stored a
        // valid height
        if ( resultItem.code == "bmi" && !showBmi){
          found = null;
        }

        if (found != null) {
          if (resultItem.name == null) {
            resultItem.name = found.name;
          }
          resultItem.localisedName = found.name;
          tabArray.push(resultItem);
        }
      }
    }

    return (
      <FlatList
        style={styles.listContainer}
        //   data={this.props.patientData}
        data={tabArray}
        keyExtractor={(item, index) => index.toString()}
        renderItem={({ item, index }) => (
          <PatientListItem
            date={getLocalDateTimeString(item.date)}
            title={item.name}
            localisedName={item.localisedName}
            itemCode={item.code}
            itemUnits={item.units}
            navigation={this.props.navigation}
            value={item.value}
            index={index}
            singular={item.singular}
            weightImperial={this.props.weightImperial}
          />
        )}
      />
    );
  };

  render() {
    return (
      <View style={{flex: 1, flexDirection: 'row'}}>
        <View style={ {width: 30, flex: 0, padding:0, alignItems: 'center', flexDirection: 'row'}}>
         { !this.state.firstTab ? 
        <MaterialIcon
          size={30}
          name={"chevron-left"}
          color={R.colors.darkGrey}
          onPress={this.onLeftPressed}
        />
        : null}
    </View>
        <ScrollView horizontal={false} style={[R.styles.scrollcontainer, {flex:1, padding: 0}]}>
          <Loader loading={this.props.isLoading} message="Loading" />
          <View
            style={R.styles.subContainer}
          >
            <Text style={R.styles.headerWhiteLeft}>{this.state.title}</Text>
            <GradientButton
              style={[R.styles.button]}
              onPress={() => {
                this.props.navigation.navigate("AddPatientData");
              }}
              title="Add new health data"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
          </View>
          <View
            style={[R.styles.subContainer, { paddingTop: 0}]}
          >
            <View style={{ width: "100%", marginBottom: 20 }}>
              {this.patientDataOutput()}
            </View>
          </View>
        </ScrollView>
        <View style={ {width: 30, flex: 0, padding:0, alignItems: 'center', flexDirection: 'row'}}>
        { !this.state.lastTab ?
        <MaterialIcon
          size={30}
          name={"chevron-right"}
          color={R.colors.darkGrey}
          onPress={this.onRightPressed}
        />
        : null }
    </View>
        </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    paddingTop: 30,
    paddingLeft: 10,
    paddingRight: 10,
    justifyContent: "flex-start",
    alignItems: "center",
    flexDirection: "column"
  },
  listContainer: {
    width: "100%",
    marginTop: 20,
    marginBottom: 40
  }
});

const mapStateToProps = state => {
  return {
    latestData: state.comms.patientData.latestResults,
    height: state.local.height,
    hasError: state.comms.patientData.hasError,
    isLoading: state.comms.patientData.isLoading,
    weightImperial: state.local.weightImperial
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchPatientData: () => {
      dispatch(PatientDataActions.fetchLatestReadings(null, SUPPORTED_FIELDS));
    },
    clearError: () => {
      dispatch(PatientDataActions.hasError(false));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ViewByMetricScreen);
