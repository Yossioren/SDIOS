# Applications

This directory includes applications that we developed and used during the research.

The applications were developed with Android Studio and tested on Android.

## AutomatedResonanceSearch

An application and a server purposed to find an Android smartphone resonance frequency.

## DataCollection

An application and a server purposed for data collection.

## SDIOSTFliteClient

An application used to test SDIOS SensorManager displays information on SDIOS and shows the trust value for each SensorEvent provided by the anomaly detection model.

The application uses SDIOS-Library to communicate with the SDIOS Service.

The SDIOS-Library can easily be integrated into existing applications with simple steps.

The library was also implemented similarly in the framework code.

## SDIOSTFliteService 

The SDIOS Service application.
It handles the connection to the service, registers sensors upon request, and analyzes the SensorEvents with the anomaly detection model before distributing them back to requesting applications.

A user can get models from a trusted server.

A user can show and configure options for the model package.

## TestingApplications

Applications used to test sensor output.
