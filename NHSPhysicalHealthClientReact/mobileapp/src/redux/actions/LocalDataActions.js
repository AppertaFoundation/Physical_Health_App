import {UPDATE_HEIGHT} from "./localTypes"
import {SET_DISPLAY_WEIGHT_IMPERIAL} from "./localTypes"
import {GRAPHS_INFO_SHOWN} from "./localTypes"
import {STORE_USERNAME_LOCAL} from "./localTypes"

  const updateHeight = (height) => {
    console.log("Update height: " + height);

    return {
      type: UPDATE_HEIGHT,
      height: height
    }
  }

  const setDisplayWeightImperial = (weightImperial) => {
    console.log(weightImperial ? "Imperial" : "Metric");

    return {
      type: SET_DISPLAY_WEIGHT_IMPERIAL,
      weightImperial: weightImperial
    }
  }

  const graphsInfoShown = () => {
    return {
      type: GRAPHS_INFO_SHOWN
    }
  }

  const storeUsername = (username) => {
    return {
      type: STORE_USERNAME_LOCAL,
      username: username
    }
  }

  export default {
    updateHeight,
    setDisplayWeightImperial,
    graphsInfoShown,
    storeUsername
  };