import React from "react";
import { View, Text, StyleSheet, Button } from "react-native";
import { RaisedTextButton } from "react-native-material-buttons";
import R from "res/R";

const DoctorListItem = props => {
  var doctorsName = props.details.title + "  " + props.details.firstNames + " " + props.details.lastName;

  return (
    <View style={styles.listItem}>
      <Text style={{ flex: 1, textAlignVertical: "center" }}>{doctorsName}</Text>
      <RaisedTextButton
          style={R.styles.button}
        onPress={() => {
          props.delete(props.index)
         }}
        title="REMOVE"
      />
    </View>
  );
};

const styles = StyleSheet.create({
  listItem: {
    width: "100%",
    padding: 10,
    marginBottom: 10,
    backgroundColor: "#eee",
    flexDirection: "row"
  }
});

export default DoctorListItem;
