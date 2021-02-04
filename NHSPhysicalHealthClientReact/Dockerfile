FROM node:10.15 as commsbuilder
RUN mkdir /usr/src/app
WORKDIR /usr/src/app

COPY ./commsLib ./
RUN ["npm", "pack"]

# base image
FROM node:10.15

# set working directory
RUN mkdir /usr/src/app
RUN mkdir /usr/src/app/lib
WORKDIR /usr/src/app

# install app dependencies
COPY ./CliniciansPortal/package*.json ./
COPY --from=commsBuilder /usr/src/app/nhsphysicalhealthcomms-*.tgz ./lib/

# find the first comms lib tgz
RUN echo $(ls ./lib/*.tgz | head -n1) > ./libver

# run npm install for comms lib and app
RUN npm install $(cat ./libver)
RUN npm install

RUN rm ./libver

# Bundle app source
COPY ./CliniciansPortal/ ./

EXPOSE 3000

# start
CMD ["npm", "start"]