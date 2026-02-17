import React from 'react';
import * as Device from 'expo-device';
import { Accelerometer, Gyroscope, Magnetometer  } from 'expo-sensors';
import { Alert } from 'react-native';
import {sendThisMessure, createNewStudent, getSucceeded, getSent } from './Communication/HTTPYFileServer'

var helpText = 'There is a problem with your measurs, please contact for help';
let acc, gyro, magne;
const ms_sensor_interval = 1;//1000/7~141HZ
const accName = "accelerometer";
const gyroName = "gyroscope";
const magName = "magnetometer";

export function getSucceededMessages(){
    return getSucceeded();
}

export function getSentMessages(){
    return getSent();;
}

var unavailableSensors = 0;
export default class Sensors extends React.Component {    
    constructor() {
        super();
        unavailableSensors = 3;
        currGyroMessures=[];
        currMagneMessures=[];
        currAccMessures=[];

        Accelerometer.isAvailableAsync().then(this.execIfArgFalse(()=>this.checkSensorsAvailable()));
        Gyroscope.isAvailableAsync().then(this.execIfArgFalse(()=>this.checkSensorsAvailable()));
        Magnetometer.isAvailableAsync().then(this.execIfArgFalse(()=>this.checkSensorsAvailable()));
        Accelerometer.setUpdateInterval(ms_sensor_interval);
        Gyroscope.setUpdateInterval(ms_sensor_interval);
        Magnetometer.setUpdateInterval(ms_sensor_interval);
    }

    initialRandomNumber =  (RandomNumber)=>{
        createNewStudent(RandomNumber, this.extractAllData());        
    }

    startMessure = () => {
        acc = Accelerometer.addListener(accelerometerData =>{//TODO timestamp
            let messure = {x: accelerometerData.x, y: accelerometerData.y, z: accelerometerData.z, t: Date.now()};
            currAccMessures.push(messure);
        });
        gyro = Gyroscope.addListener(gyroscopeData =>{//TODO timestamp
            let messure = {x: gyroscopeData.x, y: gyroscopeData.y, z: gyroscopeData.z, t: Date.now()};
            currGyroMessures.push(messure);
        });
        magne = Magnetometer.addListener(magnetometerData =>{//TODO timestamp
            let messure = {x: magnetometerData.x, y: magnetometerData.y, z: magnetometerData.z, t: Date.now()};
            currMagneMessures.push(messure);
        });
    }

    execIfArgFalse = (func) =>
        (bool)=>
        {
            //console.log(bool);
            if(!bool)
                return func();
        };
    
    alertSensor = (title, msg) =>{
        //console.log('missing ' + title);
        Alert.alert(
            title,
            msg,[
          {text: 'Ok'}],
        );
    }

    checkSensorsAvailable = ()=>{
        unavailableSensors = unavailableSensors - 1;
        if(unavailableSensors < 1) {
            Alert.alert(
                'Sensors problem', 'None of your sensors is available!'
            );
        }
    }

    alertMe = () =>{
        Alert.alert(helpText);
    }

    checkMessures = () =>{
        if(currMagneMessures.length == 0 && currAccMessures.length == 0  && currGyroMessures.length == 0){
            console.error(magName + ':' + currMagneMessures.length + ' ' +
                        accName + ':' + currAccMessures.length + ' ' +
                        gyroName + ':' + currGyroMessures.length);
            this.alertMe();
            return true;
        }
        return false;
    };

    promiseFunc = (then) =>{
        if(then === undefined)
            return then;
        return ()=>{
                iter-=1;
                if(iter == 0)
                    then();
            };
    } 

    sendMessures = (activ,ID, then) => {
        //console.log(ID);
        Accelerometer.removeAllListeners();        Gyroscope.removeAllListeners();        Magnetometer.removeAllListeners();
        if(this.checkMessures())            return;
        iter = 3;
        promise = this.promiseFunc(then);
        sendThisMessure(activ, ID, accName, currAccMessures, promise);
        sendThisMessure(activ, ID, gyroName, currGyroMessures, promise);
        sendThisMessure(activ, ID, magName, currMagneMessures, promise);
        currAccMessures = [];        currGyroMessures = [];        currMagneMessures = [];
    }

    extractAllData = ()=>{
        return {
            "brand": Device.brand,
            "designName": Device.designName,
            "deviceName": Device.deviceName,
            "deviceYearClass": Device.deviceYearClass,
            "isDevice": Device.isDevice,
            "manufacturer": Device.manufacturer,
            "modelId": Device.modelId,
            "modelName": Device.modelName,
            "osBuildFingerprint": Device.osBuildFingerprint,
            "osBuildId": Device.osBuildId,
            "osInternalBuildId": Device.osInternalBuildId,
            "osName": Device.osName,
            "osVersion": Device.osVersion,
            "platformApiLevel": Device.platformApiLevel,
            "productName": Device.productName,
            "supportedCpuArchitectures": Device.supportedCpuArchitectures,
            "totalMemory": Device.totalMemory
        };
    }
}

