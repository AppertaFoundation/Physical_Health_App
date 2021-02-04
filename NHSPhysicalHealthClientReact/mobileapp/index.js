import App from './src/App';
import {name as appName} from './app.json';
import { AppRegistry } from 'react-native';
import React from 'react';
import { Provider } from 'react-redux';

import configureStore from './src/redux/store';
import { PersistGate } from 'redux-persist/integration/react'

const { persistor, store } = configureStore()

const RNRedux = () => (
  <Provider store = { store }>
  <PersistGate loading={null} persistor={persistor}>
    <App />
  </PersistGate>
  </Provider>
)

AppRegistry.registerComponent(appName, () => RNRedux);
