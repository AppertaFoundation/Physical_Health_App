import React from "react";
import { View, Text, StyleSheet, Button } from "react-native";
import Dialog from "react-native-dialog";
import MaterialIcon from "react-native-vector-icons/MaterialIcons";

import R from "res/R";

const InfoPopup = props => {
  return (
    <Dialog.Container visible={props.isVisible} onBackdropPress={props.onHideInfoDialog }>
      <View style={{
  alignItems: "center"
}}>
      <MaterialIcon
        size={60}
        name={"info"}
        color={R.colors.highlightColorThree}
      />
      </View>
      <Text style={[R.styles.darkNormal, {textAlign: 'center'}]}>
      {props.message}
      </Text>
       <Dialog.Button label="Okay" onPress={props.onHideInfoDialog} style={{ color: R.colors.highlightColorThree }} />
    </Dialog.Container>
  );
};

export default InfoPopup;
