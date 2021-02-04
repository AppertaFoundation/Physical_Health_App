import {ADD_APPOINTMENT} from "./localTypes"
import {UPDATE_APPOINTMENT} from "./localTypes"
import {DELETE_APPOINTMENT} from "./localTypes"
import {APPOINTMENTS_INFO_SHOWN} from "./localTypes"

const addAppointment = (payload) => {
    return {
      type: ADD_APPOINTMENT,
      payload: payload
    }
  }

  const updateAppointment = (index, payload) => {
    console.log("Update appointnemt: " + index);

    return {
      type: UPDATE_APPOINTMENT,
      payload: {appt: payload, index: index}
    }
  }

  const deleteAppointment = (index) => {
    return {
      type: DELETE_APPOINTMENT,
      index: index
    }
  }

  const appointnemtInfoShown = () => {
    return {
      type: APPOINTMENTS_INFO_SHOWN
    }
  }

  export default {
    addAppointment,
    updateAppointment,
    deleteAppointment,
    appointnemtInfoShown
  };