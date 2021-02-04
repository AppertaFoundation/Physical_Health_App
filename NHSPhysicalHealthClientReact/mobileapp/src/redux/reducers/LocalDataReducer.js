import { ADD_APPOINTMENT } from "../actions/localTypes"
import { UPDATE_APPOINTMENT } from "../actions/localTypes"
import { DELETE_APPOINTMENT } from "../actions/localTypes"

import { UPDATE_HEIGHT } from "../actions/localTypes"
import { SET_DISPLAY_WEIGHT_IMPERIAL } from "../actions/localTypes"
import { APPOINTMENTS_INFO_SHOWN} from "../actions/localTypes"
import { GRAPHS_INFO_SHOWN} from "../actions/localTypes"
import {STORE_USERNAME_LOCAL} from "../actions/localTypes"

const LOGOUT = "LOGOUT";

const initialState = {
  appointments: [
  ],
  lastUpdate: null,
  height: null,
  weightImperial: false,
  appointmentsInfoShown: false,
  graphsInfoShown: false,
  username: null
};

function updateAppointmentsInArray(array, payload, apptIndex) {
  return array.map((item, index) => {
    if (index !== apptIndex) {
      // This isn't the item we care about - keep it as-is
      return item;
    }

    // Otherwise, this is the one we want - return an updated value
    return {
      ...item,
      ...payload
    };
  });
}

const localDataReducer = (state = initialState, action) => {
  switch (action.type) {
    case ADD_APPOINTMENT:
      return {
        ...state,
        appointments: state.appointments.concat(action.payload).sort(function (a, b) { return a.dateTime - b.dateTime }),
        lastUpdate: new Date()
      };
    case UPDATE_APPOINTMENT:
      console.log(action);
      var appt = action.payload.appt;
      var index = action.payload.index;
      var updatedArray = updateAppointmentsInArray(state.appointments, appt, index).sort(function (a, b) { return a.dateTime - b.dateTime });
      console.log(updatedArray);

      return {
        ...state,
        appointments: updatedArray,
        lastUpdate: new Date()
      };

    case DELETE_APPOINTMENT:
      console.log(action);
      var index = action.index;

      var appts = state.appointments;
      appts.splice(index, 1);

      console.log(appts);

      return {
        ...state,
        appointments: appts,
        lastUpdate: new Date()
      };
    case UPDATE_HEIGHT:
      console.log(action);

      return {
        ...state,
        height: action.height
      };
    case SET_DISPLAY_WEIGHT_IMPERIAL:
      console.log(action);

      return {
        ...state,
        weightImperial: action.weightImperial
      };
    case APPOINTMENTS_INFO_SHOWN:
      console.log(action);

      return {
        ...state,
        appointmentsInfoShown: true
      };
    case GRAPHS_INFO_SHOWN:
      console.log(action);

      return {
        ...state,
        graphsInfoShown: true
      };
   case STORE_USERNAME_LOCAL:
    console.log(action);

    return {
      ...state,
      username: action.username
    };
    case LOGOUT:
      return {
        ...initialState,
        username: state.username
      }
    default:
      return state;
  }
};

export default localDataReducer;