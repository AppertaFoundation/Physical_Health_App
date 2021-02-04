import { createStore, applyMiddleware, combineReducers } from 'redux';
import thunk from 'redux-thunk';
import { persistStore, persistReducer } from 'redux-persist';
import storage from 'redux-persist/lib/storage';

import {commsReducer} from 'nhsphysicalhealthcomms';
import localDataReducer from './reducers/LocalDataReducer';

const rootReducer = combineReducers({
  comms: commsReducer,
  local: localDataReducer,
});

const persistConfig = {
  key: 'root',
  storage,
  blacklist: ['comms']
};

const configureStore = () => {
  const middlewares = [thunk];
  const enhancer = applyMiddleware(...middlewares);
  const persistedReducer = persistReducer(persistConfig, rootReducer);

  // create store
  return createStore(persistedReducer, enhancer);
};

export default () => {
  let store = configureStore()
  let persistor = persistStore(store)
  return { store, persistor }
}