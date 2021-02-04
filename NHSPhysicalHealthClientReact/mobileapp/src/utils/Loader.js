import React, { Component } from 'react';
import {
  StyleSheet,
  View,
  Modal,
  Text,
  ActivityIndicator
} from 'react-native';

const Loader = props => {
  const {
    loading,
    message
  } = props;

return (
  <Modal
  transparent={true}
  animationType={'none'}
  visible={loading}>
  <View style={styles.modalBackground}>
    <View style={styles.activityIndicatorWrapper}>
      <ActivityIndicator
        animating={loading} 
        size="large" 
        color="red"
        />
         <Text
            style={{
              color: 'black',
              fontSize: 16,
              fontWeight: 'normal',
              fontFamily: 'Helvetica Neue',
              textAlign: 'center'
            }}>
            {message}
          </Text>
    </View>
  </View>
</Modal>
  )
}

const styles = StyleSheet.create({
  modalBackground: {
    flex: 1,
    alignItems: 'center',
    flexDirection: 'column',
    justifyContent: 'space-around',
    backgroundColor: '#00000040'
  },
  activityIndicatorWrapper: {
    backgroundColor: '#FFFFFF',
    height: 150,
    width: 150,
    borderRadius: 10,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'space-around'
  }
});

export default Loader;