import React from "react";
import { View, Text, StyleSheet, Button } from "react-native";

const WeightListItem = props => {
  return (
    <View style={styles.listItem}>
      <Text style={{ flex: 1 }}>{props.date}</Text>
      <Text style={{ flex: 1 }}>{props.weightStone}</Text>
      <Text style={{ flex: 1 }}>{props.weightPounds}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  listItem: {
    width: "100%",
    flexDirection: "row"
  }
});

export default WeightListItem;
