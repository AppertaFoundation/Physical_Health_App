import React from "react";
import { View, Text, StyleSheet, Button } from "react-native";
import MaterialIcon from "react-native-vector-icons/MaterialIcons";

import R from "res/R";

const TestDataListItem = props => {
  return (
    <View style={[styles.listItem, {
      borderBottomWidth: 1,
      borderColor: R.colors.faintBorderColor
    }]}>
      <Text style={{width: 120}}>{props.date}</Text>
      <Text style={{width: 60, textAlign: 'right'}}>{props.time}</Text>
      <Text style={{ flex: 3, textAlign: 'right' }}>{props.value}</Text>
      <View style={{ flex: 1 }}/>
      <MaterialIcon
          size={24}
          name={"edit"}
          color={R.colors.darkGrey}
          onPress={props.onEditPressed}
        />
    </View>
  );
};

const styles = StyleSheet.create({
  listItem: {
    width: "100%",
    flexDirection: "row"
  }
});

export default TestDataListItem;
