import React from 'react';
import DefaultLayout from './containers/DefaultLayout';

const Patients = React.lazy(() => import('./views/Patients'));
const PatientData = React.lazy(() => import('./views/Patients/PatientData/PatientData'));
const EditData = React.lazy(() => import('./views/Patients/AddPatientData/AddPatientData'));
const ViewPatientData = React.lazy(() => import('./views/Patients/PatientData/ViewData'));
const ViewPatientBloodData = React.lazy(() => import('./views/Patients/PatientData/BloodPressure'));

const Profile = React.lazy(() => import('./views/Profile'));


// https://github.com/ReactTraining/react-router/tree/master/packages/react-router-config
const routes = [
  { path: '/', exact: true, name: 'Home', component: Patients },
  { path: '/patients', exact: true, name: 'My Patients', component: Patients},
  { path: '/patients/view/:username/add', exact: true, name: 'Add Data', component: EditData},
  { path: '/patients/view/:username/blood', exact: true, name: 'Blood', component: ViewPatientBloodData},
  { path: '/patients/view/:username/:testcode', exact: true, name: 'Test History', component: ViewPatientData},
  { path: '/patients/view/:id', exact: true, name: 'Patient Data', component: PatientData},
  { path: '/profile', name: 'Profile', component: Profile}
];

export default routes;
