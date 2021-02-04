import React from "react";
import { View, Text, StyleSheet, Button } from "react-native";
import { RaisedTextButton } from "react-native-material-buttons";
import R from "res/R";
import GradientButton from "../../utils/GradientButton";
import {convertKgsToStonesAndPounds} from '../../utils/WeightConversionUtils';

const PatientListItem = props => {
  var page = "ViewPatientTestDetails";
  var value = props.value;
  var units = props.itemUnits;
  if (props.itemCode == "systolic" || props.itemCode == "diastolic") {
    page = "ViewBloodDetails";
  } else if (props.itemCode == 'bmi' || props.itemCode == 'weight'){
    page = "ViewBmiWeightScreen"
    if ( props.weightImperial ){
      var imperialWeight = convertKgsToStonesAndPounds(value);
      value = imperialWeight.stones + " st " + imperialWeight.pounds + " lb";
      units = "";
    }
  }

  return (
    <View style={styles.listItem}>
      <Text style={{ flex: 1, fontSize: 22, fontWeight: 'bold' }}>{props.localisedName}</Text>
      <View style={{ flexDirection: "row" }}>
        <View style={{ flex: 1, flexDirection: "column" }}>
          <View style={{ flexDirection: "row", marginBottom:6 }}>
            <Text style={{ flex: 0, fontSize: 20 }}>{value}</Text>
            <Text style={{ flex: 0, fontSize: 20 }}> {units}</Text>
          </View>
          <Text style={{ flex: 1, fontSize: 16 }}>{props.date}</Text>
        </View>
        <View style={{marginBottom:10}}>
        <GradientButton
          style={R.styles.button}
          onPress={() => {
            props.navigation.navigate(page, {
              dataIndex: props.index,
              dataCode: props.itemCode,
              dataTitle: props.title
            });
          }}
          title="View"
          color={R.colors.highlightColorOne}
          titleColor="white"
          disabled={props.singular}
        />
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  listItem: {
    width: "100%",
    padding: 10,
    marginBottom: 10,
    backgroundColor: "white",
    flexDirection: "column"
  }
});

export default PatientListItem;
