import React, { Component } from "react";
import {
  Image,
  StyleSheet,
  Text,
  View,
  Button,
  ScrollView,
  TextInput,
  TouchableOpacity,
  AsyncStorage,
  Alert,
  AppRegistry
} from "react-native";
import { SafeAreaView } from "react-navigation";
import { RaisedTextButton } from "react-native-material-buttons";
import { TextField } from "react-native-material-textfield";
import MaterialIcon from "react-native-vector-icons/MaterialIcons";
import Loader from "../utils/Loader";
import { getLocalTimeString } from "../utils/DateUtils";
import { getLocalDateString } from "../utils/DateUtils";
import R from "res/R";

import { connect } from "react-redux";
import { LoginActions } from "nhsphysicalhealthcomms";
import GradientButton from "../utils/GradientButton";

import SplashScreen from 'react-native-splash-screen'

class DashBoard extends React.Component {
  static propTypes = {};

  static defaultProps = {};

  static navigationOptions = {
    title: "Home"
  };

  constructor(props) {
    super(props);
    this.state = {};

    this.getAppointments = this.getAppointments.bind(this);
    this.renderAppointment = this.renderAppointment.bind(this);
  }

  async logout() {
    this.props.logout();
  }

  componentDidMount() {
    this.getAppointments();
    SplashScreen.hide();
  }

  componentDidUpdate(prevProps) {
    if (!this.props.isLogged) {
      this.props.navigation.navigate("Auth");
    }

    if (this.props.lastUpdate != prevProps.lastUpdate) {
      this.getAppointments();
    }
  }

  getAppointments() {
    console.log("Get Appointment");
    console.log(this.props.appointments);
    if (this.props.appointments != null) {
      // Find the next appoimtnemt.
      var now = new Date();
      var nextAppointment = this.props.appointments.find(appt => {
        return appt.dateTime > now;
      });

      // Check for recently expired appointment
      var twoHoursAgo = new Date();
      var twoHoursAgo = twoHoursAgo.setHours(twoHoursAgo.getHours() - 2);
      var appointmentsInLastTwoHours = this.props.appointments.filter(appt => {
        if ( appt.dateTime > now 
           || appt.dateTime < twoHoursAgo ){
             return null;
           }

           return appt;
      });

      var recentlyExpiredAppointment = null;
      if ( appointmentsInLastTwoHours != null
            && appointmentsInLastTwoHours.length > 0){
              recentlyExpiredAppointment = appointmentsInLastTwoHours[appointmentsInLastTwoHours.length - 1 ];
            }

      console.log(nextAppointment);
      this.setState({ nextAppointment: nextAppointment, recentlyExpiredAppointment: recentlyExpiredAppointment});
    } else {
      this.setState({ nextAppointment: null, recentlyExpiredAppointment: null });
    }
  }

  renderAppointment(appointment){
    if (appointment!= null) {
      apptDate = new Date(appointment.dateTime);
    }

    return (
        <TouchableOpacity
          onPress={() => {
            this.props.navigation.navigate("ViewAppointments");
          }}
          style={{
            backgroundColor: "white",
            padding: 10,
            marginTop: 20,
            marginBottom: 20,
            flexDirection: "column",
            borderWidth: 2,
            borderColor: R.colors.highlightColorThree,
          }}>
          <View style={{ flexDirection: "row" }}>
            <Text style={R.styles.highlightNormal}>
              Date: {"  "}
            </Text>
            <Text style={R.styles.darkNormal}>
              {getLocalDateString(apptDate)}
            </Text>
          </View>
          <View style={{ flexDirection: "row" }}>
            <Text style={R.styles.highlightNormal}>
              Time: {"  "}
            </Text>
            <Text style={R.styles.darkNormal}>
              {getLocalTimeString(apptDate)}
            </Text>
          </View>
          <View style={{ flexDirection: "row" }}>
            <Text style={R.styles.highlightNormal}>
              Location: {"  "}
            </Text>
            <Text style={R.styles.darkNormal}>
              {appointment.location}
            </Text>
          </View>
          <Text style={[R.styles.darkNormal, { marginTop: 12 }]}>
            {appointment.title}
          </Text>
        </TouchableOpacity>
    );
  };

  render() {
    var nextAppointment = this.state.nextAppointment;
    var recentlyExpiredAppointment = this.state.recentlyExpiredAppointment;


    return (
      <SafeAreaView style={R.styles.container}>
        <ScrollView horizontal={false} style={R.styles.scrollcontainer}>
          <View
            style={R.styles.subContainer}
          >
            <Text style={R.styles.headerWhiteLeft}>My health data</Text>
            <View style={R.styles.dashboardItemDivider} />
            <GradientButton
              style={R.styles.button}
              onPress={() => {
                this.props.navigation.navigate("PatientDetailsList");
              }}
              title="View my health data"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
            <GradientButton
              style={R.styles.button}
              onPress={() => {
                this.props.navigation.navigate("AddPatientData");
              }}
              title="Add new health data"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
          </View>

          <View
            style={R.styles.subContainer}
          >
            <Text style={R.styles.headerWhiteLeft}>My appointments</Text>
            <View style={R.styles.dashboardItemDivider} />

            {recentlyExpiredAppointment != null ? this.renderAppointment(recentlyExpiredAppointment) : null}
            {nextAppointment != null ? this.renderAppointment(nextAppointment) : null}

            <GradientButton
              style={R.styles.button}
              onPress={() => {
                this.props.navigation.navigate("ViewAppointments");
              }}
              title="View appointments"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
            <GradientButton
              style={R.styles.button}
              onPress={() => {
                this.props.navigation.navigate("ViewAppointments", { create: true });
              }}
              title="Add new appointment"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
          </View>

          <View
            style={R.styles.subContainer}
          >
            <Text style={R.styles.headerWhiteLeft}>My account</Text>
            <View style={R.styles.dashboardItemDivider} />
            <GradientButton
              style={R.styles.button}
              onPress={() => {
                this.props.navigation.navigate("AccountDetails");
              }}
              title="Account settings"
              color={R.colors.highlightColorOne}
              titleColor="white"
            />
            <RaisedTextButton
              style={R.styles.button}
              onPress={() => {
                this.logout();
              }}
              title="Log out"
              color="red"
              titleColor="white"
            />
          </View>
        </ScrollView>
      </SafeAreaView >
    );
  }
}

const mapStateToProps = state => {
  return {
    isLogged: state.comms.login.isLogged,
    appointments: state.local.appointments,
    lastUpdate: state.local.lastUpdate
  };
};

const mapDispatchToProps = dispatch => {
  return {
    logout: () => {
      dispatch(LoginActions.logout());
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(DashBoard);
