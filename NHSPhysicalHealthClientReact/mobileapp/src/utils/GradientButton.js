import React from "react";
import { Text, StyleSheet, TouchableOpacity } from "react-native";
import LinearGradient from 'react-native-linear-gradient';

const GradientButton = props => {
  var colors = props.disabled ? ['#dddddddd', '#aaaaaaaa']   : ['#4a3cdb', '#ff0a6c'];
  return (
    <TouchableOpacity
      onPress={() =>
        props.onPress()
      }
      disabled={props.disabled}
    >
      <LinearGradient start={{x: 0, y: 0}} end={{x: 1, y: 0}} colors={colors} style={styles.linearGradient}>
        <Text style={styles.buttonText}>
          {String(props.title).toUpperCase()}
        </Text>
      </LinearGradient>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  linearGradient: {
    flex: 1,
    paddingLeft: 15,
    paddingRight: 15,
    borderRadius: 5,
    marginTop: 10
  },
  buttonText: {
    textAlign: 'center',
    margin: 10,
    color: '#ffffff',
    backgroundColor: 'transparent',
    fontWeight: "bold"
  },
});

export default GradientButton;
