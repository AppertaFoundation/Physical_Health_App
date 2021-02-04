import React, { Component } from "react";
import { Image } from "react-native";

import {
  createStackNavigator,
  createAppContainer,
  createSwitchNavigator,
  createMaterialTopTabNavigator
} from "react-navigation";
import R from "res/R";
import DashBoard from "./dashboard/DashBoard";
import LoginScreen from "./login/LoginScreen";
import SplashLauncherScreen from "./login/SplashLauncherScreen";
import ResetPasswordScreen from "./login/ResetPasswordScreen";
import RegistrationScreenOne from "./login/RegistrationScreenOne";
import RegistrationScreenTwo from "./login/RegistrationScreenTwo";
import RegistrationScreenThree from "./login/RegistrationScreenThree";
import ViewByMetricScreen from "./dashboard/ViewByMetricScreen";
import AddPatientDataScreen from "./dashboard/AddPatientDataScreen";
import ViewPatientTestDetailsScreen from "./dashboard/ViewTestDetailsScreen";
import ViewBloodDetailsScreen from "./dashboard/ViewBloodDetailsScreen";
import ViewBmiWeightScreen from "./dashboard/ViewBmiWeightScreen";
import AccountScreen from "./account/AccountScreen";
import DoctorsScreen from "./account/DoctorsScreen";
import ViewAppointmentsScreen from "./dashboard/ViewAppointmentsScreen";
import ViewPatientDataGraph from "./dashboard/ViewPatientDataGraph";
import ViewBloodDataGraph from "./dashboard/ViewBloodDataGraph";
import ViewWeightBmiDataGraph from "./dashboard/ViewWeightBmiDataGraph";

const ViewDataStack = createMaterialTopTabNavigator(
  {
    GeneralHealth: {
      screen: ViewByMetricScreen,
      navigationOptions: {
        tabBarLabel: "General Health"
      }
    },
    HeartHealth: {
      screen: ViewByMetricScreen,
      navigationOptions: {
        tabBarLabel: "Heart Health"
      }
    },
    KidneyHealth: {
      screen: ViewByMetricScreen,
      navigationOptions: {
        tabBarLabel: "Kidney Health"
      }
    },
    BoneHealth: {
      screen: ViewByMetricScreen,
      navigationOptions: {
        tabBarLabel: "Bone Health"
      }
    },
    LiverHealth: {
      screen: ViewByMetricScreen,
      navigationOptions: {
        tabBarLabel: "Liver Health"
      }
    },
    HormoneHealth: {
      screen: ViewByMetricScreen,
      navigationOptions: {
        tabBarLabel: "Hormone Health"
      }
    }
  },
  {
    tabBarPosition: "top",
    tabBarOptions: {
      scrollEnabled: true,
      style: { backgroundColor: R.colors.topTabBarBackground },
      indicatorStyle: { backgroundColor: R.colors.topTabBarHighlight }
    }
  }
);

const AccountDetailsStack = createMaterialTopTabNavigator(
  {
    Data: {
      screen: AccountScreen,
      navigationOptions: {
        tabBarLabel: "Account details"
      }
    },
    Metrics: {
      screen: DoctorsScreen,
      navigationOptions: {
        tabBarLabel: "My clinicians"
      }
    }
  },
  {
    tabBarPosition: "top",
    tabBarOptions: {
      style: { backgroundColor: R.colors.topTabBarBackground },
      indicatorStyle: { backgroundColor: R.colors.topTabBarHighlight }
    }
  }
);

const HomeStack = createStackNavigator(
  {
    DashBoard: DashBoard,
    PatientDetailsList: {
      screen: ViewDataStack,
      navigationOptions: {
        title: "My health data"
      }
    },
    AddPatientData: AddPatientDataScreen,
    ViewPatientTestDetails: ViewPatientTestDetailsScreen,
    AccountDetails: {
      screen: AccountDetailsStack,
      navigationOptions: {
        title: "Account settings"
      }
    },
    ViewBloodDetails: ViewBloodDetailsScreen,
    ViewAppointments: ViewAppointmentsScreen,
    ViewPatientDataGraph: ViewPatientDataGraph,
    ViewBloodDataGraph, ViewBloodDataGraph,
    ViewBmiWeightScreen, ViewBmiWeightScreen,
    ViewWeightBmiDataGraph: ViewWeightBmiDataGraph
  },
  {
    initialRouteName: "DashBoard"
  }
);

const AuthStack = createStackNavigator(
  {
    Login: LoginScreen,
    ResetPasswordScreen: ResetPasswordScreen
  },
  {
    initialRouteName: "Login"
  }
);

const RegistrationStack = createStackNavigator(
  {
    RegistrationScreenOne: RegistrationScreenOne,
    RegistrationScreenTwo: RegistrationScreenTwo,
    RegistrationScreenThree: RegistrationScreenThree
  },
  {
    initialRouteName: "RegistrationScreenOne"
  }
);

const AppContainer = createAppContainer(
  createSwitchNavigator(
    {
      Splash: SplashLauncherScreen,
      App: HomeStack,
      Auth: AuthStack,
      Registration: RegistrationStack
    },
    {
      initialRouteName: "Splash"
    }
  )
);

export default class App extends React.Component {
  render() {
    return <AppContainer />;
  }
}
