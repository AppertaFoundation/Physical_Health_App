LICENSE

This software is made available under the GNU Affero General Public License under the following terms:
https://www.gnu.org/licenses/agpl-3.0.en.html

INSTRUCTIONS TO LINK COMMS LIB.

*** Setting server url ***

The server url is set using ServerConfigActions.updateServerConfig currently on line 41 of CliniciansPortal/src/app.js for the portal and in Constants.js for the mobile app.

*** Linking commslib ***

REACT-NATIVE (MobileApp) and REACT (ClinicalPortal)

cd commsLib
rm *.tgz
npm pack

cd ../mobileApp
rm -rf node_modules
npm install ../commsLib/nhsphysicalhealthcomms-1.0.0.tgz
npm install

## Build Clinical Portal via docker
From the top level directory:

docker build -t <tag name> .
docker run -p 3000:3000 -d <tag name>

## For Production
(all from the top level directory)
### Via Docker
docker build -f Dockerfile-prod -t <tag name> .
docker rm -p 80:80 -d <tag name>

### Via Docker compose
(remember to remove any previously installed container first)
To build:
docker-compose up -d --build

Or to just rerun:
docker-compose up -d


