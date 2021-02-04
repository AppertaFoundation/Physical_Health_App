import React, { Component } from "react";
import { StyleSheet, View, Text, TouchableOpacity } from "react-native";

import { TextField } from "react-native-material-textfield";
import { getLocalDateString } from "../utils/DateUtils";
import AppointmentActions from "../redux/actions/AppointmentActions";
import DateTimePicker from "react-native-modal-datetime-picker";

import { connect } from "react-redux";
import { Agenda } from "react-native-calendars";
import { getLocalTimeString } from "../utils/DateUtils";
import Dialog from "react-native-dialog";
import MaterialIcon from "react-native-vector-icons/MaterialIcons";
import InfoPopup from "./components/InfoPopup";

import R from "res/R";

const fields = ["title", "location"];

class ViewAppointmentScreen extends Component {
  static navigationOptions = ({ navigation }) => ({
    title: "Appointments",
    headerRight: (
      <TouchableOpacity onPress={navigation.getParam("Add")} style={{}}>
        <Text
          style={{
            color: R.colors.highlightColorOne,
            fontSize: 16,
            fontWeight: "bold",
            marginRight: 16
          }}
        >
          Add
        </Text>
      </TouchableOpacity>
    )
  });

  constructor(props) {
    super(props);
    this.updateAppointments = this.updateAppointments.bind(this);
    this.onAdd = this.onAdd.bind(this);
    this.renderDialog = this.renderDialog.bind(this);

    this.titleRef = this.updateRef.bind(this, "title");
    this.locationRef = this.updateRef.bind(this, "location");

    this.onSubmit = this.onSubmit.bind(this);
    this.onDelete = this.onDelete.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.onCancel = this.onCancel.bind(this);
    this.onHideInfoDialog = this.onHideInfoDialog.bind(this);

    this.state = {
      items: {},
      dialogVisible: false,
      infoDialogVisible: false,

      appointmentDate: new Date(),
      appointmentTime: null,
      location: "",
      title: "",
      editMode: false
    };
  }

  componentDidMount() {
    this.updateAppointments();

    this.props.navigation.setParams({ Add: this.onAdd });

    const { navigation } = this.props;
    var addAppointment = navigation.getParam("create", false);
    if (addAppointment) {
      this.setState({ dialogVisible: true, editMode: false });
    }

    this.setState({infoDialogVisible: !this.props.appointmentsInfoShown});
  }

  componentDidUpdate(prevProps) {
    if (this.props.lastUpdate != prevProps.lastUpdate) {
      this.updateAppointments();
    }
  }

  updateAppointments() {
    var appointments = this.props.appointments;
    console.log(appointments);

    var items = {};

    // Clear item array
    Object.keys(this.state.items).forEach(key => {
      items[key] = [];
    });

    // Add appointments
    appointments.map((appointment, index) => {
      const strTime = this.timeToString(appointment.dateTime);
      if (!items[strTime]) {
        items[strTime] = [];
      }

      items[strTime].push({ appointment: appointment, index: index });
    });

    console.log(items);

    this.setState({
      items: items
    });
  }

  onAdd() {
    console.log("OnAdd");

    this.setState({
      appointmentTime: null,
      location: "",
      title: "",
      editMode: false,
      dialogVisible: true,
    });
  }

  loadItems(day) {
    setTimeout(() => {
      console.log("Load items");
      for (let i = -15; i < 85; i++) {
        const time = day.timestamp + i * 24 * 60 * 60 * 1000;
        const strTime = this.timeToString(time);
        if (!this.state.items[strTime]) {
          this.state.items[strTime] = [];
        }
      }
      //console.log(this.state.items);
      const newItems = {};
      Object.keys(this.state.items).forEach(key => {
        newItems[key] = this.state.items[key];
      });
      this.setState({
        items: newItems
      });
    }, 100);
    // console.log(`Load Items for ${day.year}-${day.month}`);
  }

  renderItem(item) {
    var appointment = item.appointment;

    return (
      <TouchableOpacity
        style={[styles.item,
        {
          flexDirection: "column",
          borderWidth: 2,
          borderColor: R.colors.highlightColorTwo,
        }]}
        onPress={async () => {
          this.editItem(item);
        }}
      >
        <View style={{ flexDirection: "row" }}>
          <Text style={R.styles.highlightNormal}>
            Time: {"  "}
          </Text>
          <Text style={R.styles.darkNormal}>
            {getLocalTimeString(new Date(appointment.dateTime))}
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
        <Text style={{ marginTop: 12 }}>{appointment.title}</Text>
      </TouchableOpacity>
    );
  }

  editItem(item) {
    var appointment = item.appointment;
    var index = item.index;

    var dateTime = new Date(appointment.dateTime);

    this.setState({
      appointmentDate: dateTime,
      appointmentTime: dateTime,
      location: appointment.location,
      title: appointment.title,
      editMode: true,
      dialogVisible: true,
      index: index
    });
  }

  renderEmptyDate() {
    return <View style={styles.emptyDate}></View>;
  }

  rowHasChanged(r1, r2) {
    var appt1 = r1.appointment;
    var appt2 = r2.appointment;
    return (
      appt1.title !== appt2.title ||
      appt1.location !== appt2.location ||
      appt1.dateTime !== appt2.dateTime
    );
  }

  timeToString(time) {
    const date = new Date(time);
    return date.toISOString().split("T")[0];
  }

  _showDatePicker = () => this.setState({ isDatePickerVisible: true });

  _hideDatePicker = () => this.setState({ isDatePickerVisible: false });

  _handleDatePicked = date => {
    console.log("A date has been picked: ", date);

    // Remove any error message
    let { errors = {} } = this.state;
    delete errors["appointmentDate"];
    this.setState({ errors });

    this.setState({
      appointmentDate: date
    });
    this._hideDatePicker();
  };

  _showTimePicker = () => this.setState({ isTimePickerVisible: true });

  _hideTimePicker = () => this.setState({ isTimePickerVisible: false });

  _handleTimePicked = date => {
    console.log("A time has been picked: ", date);

    // Remove any error message
    let { errors = {} } = this.state;
    delete errors["appointmentTime"];
    this.setState({ errors });

    this.setState({
      appointmentTime: date
    });
    this._hideTimePicker();
  };

  onFocus() {
    let { errors = {} } = this.state;

    for (let name in errors) {
      let ref = this[name];

      if (ref && ref.isFocused()) {
        delete errors[name];
      }
    }

    this.setState({ errors });
  }

  onChangeText(text) {
    fields
      .map(name => ({ name, ref: this[name] }))
      .forEach(({ name, ref }) => {
        if (ref.isFocused()) {
          this.setState({ [name]: text });
        }
      });
  }

  updateRef(name, ref) {
    this[name] = ref;
  }

  onDelete() {
    this.props.deleteAppointment(this.state.index);
    this.setState({
      dialogVisible: false,
      noErrors: true,
      errors: []
    });
  }

  onSubmit() {
    let errors = {};
    let noErrors = true;

    fields.forEach(name => {
      let value = this[name].value();

      if (!value) {
        noErrors = false;
        errors[name] = "Should not be empty";
      }
    });

    // Validate a date  has been selected
    if (this.state.appointmentDate == null) {
      noErrors = false;
      errors["appointmentDate"] = "Date of appointment required";
    }

    // Validate a time  has been selected
    if (this.state.appointmentTime == null) {
      noErrors = false;
      errors["appointmentTime"] = "Time of appointment required";
    }

    this.setState({ errors });

    console.log(errors);

    if (noErrors) {
      var dateTime = this.state.appointmentDate;
      dateTime.setHours(this.state.appointmentTime.getHours());
      dateTime.setMinutes(this.state.appointmentTime.getMinutes());

      var appointment = {
        title: this.state.title,
        dateTime: dateTime.getTime(),
        location: this.state.location
      };
      if (this.state.editMode) {
        this.props.updateAppointment(this.state.index, appointment);
      } else {
        this.props.createAppointment(appointment);
      }

      this.setState({ dialogVisible: false });
    }
  }

  onCancel() {
    this.setState({
      dialogVisible: false,
      noErrors: true,
      errors: []
    })
  }

  onHideInfoDialog() {
    this.setState({infoDialogVisible: false});
    this.props.infoDialogShown();
  }

  renderDialog() {
    let { errors = {}, ...data } = this.state;

    return (
      <Dialog.Container visible={this.state.dialogVisible} onBackdropPress={() => { this.onCancel(); }}>
        <Dialog.Title style={R.styles.highlightNormal}>{this.state.editMode ? "Edit Appointment" : "Create Appointment"}</Dialog.Title>
        <View>
          <TouchableOpacity onPress={this._showDatePicker}>
            <TextField
              inputContainerStyle={R.styles.textcontainer}
              inputContainerPadding={R.constants.textFieldLabelHeight}
              labelHeight={R.constants.textFieldLabelHeight}
              labelTextStyle={R.styles.textFieldLabelText}
              tintColor={R.colors.nhsblue}
              value={
                this.state.appointmentDate
                  ? getLocalDateString(this.state.appointmentDate)
                  : ""
              }
              returnKeyType="done"
              label="Date of appointment"
              error={errors.appointmentDate}
              editable={false}
            />
          </TouchableOpacity>

          <DateTimePicker
            isVisible={this.state.isDatePickerVisible}
            onConfirm={this._handleDatePicked}
            onCancel={this._hideDatePicker}
            datePickerModeAndroid="spinner"
            mode="date"
            date={this.state.appointmentDate == null ? new Date() : this.state.appointmentDate}
          />
          <TouchableOpacity onPress={this._showTimePicker}>
            <TextField
              inputContainerStyle={R.styles.textcontainer}
              inputContainerPadding={R.constants.textFieldLabelHeight}
              labelHeight={R.constants.textFieldLabelHeight}
              labelTextStyle={R.styles.textFieldLabelText}
              tintColor={R.colors.nhsblue}
              value={
                this.state.appointmentTime
                  ? getLocalTimeString(this.state.appointmentTime)
                  : ""
              }
              returnKeyType="done"
              label="Appointment time"
              error={errors.appointmentTime}
              editable={false}
            />
          </TouchableOpacity>

          <DateTimePicker
            isVisible={this.state.isTimePickerVisible}
            onConfirm={this._handleTimePicked}
            onCancel={this._hideTimePicker}
            mode="time"
            date={this.state.appointmentTime == null ? new Date() : this.state.appointmentTime}
          />
          <TextField
            inputContainerStyle={R.styles.textcontainer}
            inputContainerPadding={R.constants.textFieldLabelHeight}
            labelHeight={R.constants.textFieldLabelHeight}
            labelTextStyle={R.styles.textFieldLabelText}
            tintColor={R.colors.nhsblue}
            ref={this.locationRef}
            value={data.location}
            autoCapitalize="none"
            autoCorrect={false}
            enablesReturnKeyAutomatically={true}
            onFocus={this.onFocus}
            onChangeText={this.onChangeText}
            onSubmitEditing={this.onSubmit}
            returnKeyType="next"
            label="Location"
            error={errors.location}
          />
          <TextField
            inputContainerStyle={R.styles.textcontainer}
            inputContainerPadding={R.constants.textFieldLabelHeight}
            labelHeight={R.constants.textFieldLabelHeight}
            labelTextStyle={R.styles.textFieldLabelText}
            tintColor={R.colors.nhsblue}
            ref={this.titleRef}
            value={data.title}
            autoCapitalize="none"
            autoCorrect={false}
            enablesReturnKeyAutomatically={true}
            onFocus={this.onFocus}
            onChangeText={this.onChangeText}
            onSubmitEditing={this.onSubmit}
            returnKeyType="next"
            label="Title"
            error={errors.title}
            multiline={true}
            numberOfLines={6}
            height={90}
          />
        </View>
        {this.state.editMode ?
        <Dialog.Button label="Delete" onPress={() => { this.onDelete() }} style={{ color: R.colors.highlightColorThree }} />
        : <Dialog.Button label="Cancel" onPress={() => {this.onCancel()}} style={{ color: R.colors.highlightColorThree }}/>
        }
        <Dialog.Button label="Save" onPress={() => { this.onSubmit() }} style={{ color: R.colors.highlightColorThree }} />
      </Dialog.Container>
    );
  }

  render() {
    return (
      <View style={{ flex: 1 }}>
        <Agenda
          items={this.state.items}
          loadItemsForMonth={this.loadItems.bind(this)}
          selected={this.state.appointmentDate}
          onDayChange={(day) => { console.log('day changed', day.year); this.setState({ appointmentDate: new Date(day.timestamp) }) }}
          onDayPress={(day) => { console.log('day pressed'); this.setState({ appointmentDate: new Date(day.timestamp) }) }}
          renderItem={this.renderItem.bind(this)}
          renderEmptyDate={this.renderEmptyDate.bind(this)}
          rowHasChanged={this.rowHasChanged.bind(this)}
          theme={{
            backgroundColor: R.colors.mainBackground,
            agendaDayNumColor: R.colors.highlightColorThree,
            agendaTodayColor: R.colors.highlightColorTwo,
            agendaKnobColor: R.colors.highlightColorThree,
            selectedDayBackgroundColor: R.colors.highlightColorTwo,
            dotColor: R.colors.highlightColorTwo,
            selectedDotColor: R.colors.highlightColorTwo,
            todayTextColor: R.colors.highlightColorTwo,
          }}
        />
        {this.renderDialog()}
        <InfoPopup
          message="Welcome to the Chart My Health calendar where you can see all of your appointments. 
                    Pull down on the blue bar to view the monthly calendar."
          onHideInfoDialog={() => {this.onHideInfoDialog()}} 
          isVisible={this.state.infoDialogVisible}         
          />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  item: {
    backgroundColor: "white",
    flex: 1,
    borderRadius: 5,
    padding: 10,
    marginRight: 10,
    marginTop: 17
  },
  emptyDate: {
    height: 15,
    flex: 1,
    paddingTop: 30
  }
});

const mapStateToProps = state => {
  return {
    appointments: state.local.appointments,
    lastUpdate: state.local.lastUpdate,
    appointmentsInfoShown: state.local.appointmentsInfoShown
  };
};

const mapDispatchToProps = dispatch => {
  return {
    createAppointment: appointment => {
      dispatch(AppointmentActions.addAppointment(appointment));
    },
    deleteAppointment: index => {
      dispatch(AppointmentActions.deleteAppointment(index));
    },
    updateAppointment: (index, appointment) => {
      dispatch(AppointmentActions.updateAppointment(index, appointment));
    },
    infoDialogShown: () => {
      dispatch(AppointmentActions.appointnemtInfoShown());
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ViewAppointmentScreen);
