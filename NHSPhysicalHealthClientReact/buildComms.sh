#!/bin/bash
# Create comms lib and install on mobile app

cd commsLib
rm *.tgz

npm pack

cd ../mobileapp
rm -rf node_modules
npm install ../commsLib/nhsphysicalhealthcomms-1.0.*.tgz
npm install
