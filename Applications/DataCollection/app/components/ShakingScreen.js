import React ,{Component} from 'react';
import { View, Text, ImageBackground, BackHandler, Dimensions, StyleSheet} from 'react-native';
import { createStackNavigator, createAppContainer } from 'react-navigation';
import Sensors from './Sensors';
import {makeBuzzerSound} from './music/buzzer'
import Button from './Button'
import { activateKeepAwake } from 'expo-keep-awake';

const { height, width } = Dimensions.get('window');

export default class ShakingScreen extends Component{
  constructor() {
    super();
    BackHandler.addEventListener('hardwareBackPress',handleBackButtonClick);
    sensor = new Sensors();
    
      this.state = {
        timePassed : 60,
        startButton: false,
        nextTaskButton: true
      }
    }
    handleBackButtonClick = () => {
    return true;
}
componentWillUnmount() {
  BackHandler.removeEventListener('hardwareBackPress', handleBackButtonClick);
}
  setTimePassed = () => {
    this.setState({
      timePassed: this.state.timePassed - 1
    })
  }
  
  async startCounting() {    
    sensor.startMessure();
    this.setState({
      startButton: true
    })
    this._interval = setInterval(() => {
      this.setTimePassed()
      if(this.state.timePassed == 1){
        makeBuzzerSound();
      }
      if(this.state.timePassed == 0){
        this.stopCounting();
      }
    }, 1000);
  }
  
  
  stopCounting = () =>{
      clearInterval(this._interval);
    sensor.sendMessures("shaking",randomNumber, ()=>
     this.setState({nextTaskButton: false}));
  }
  
  onPressNextScreen(){
    this.props.navigation.navigate('Running', {
      randomNumber: RandomN,
    });
  }
  
      render() {
        const { navigation } =this.props
        activateKeepAwake();
        randomNumber = JSON.stringify(navigation.getParam('randomNumber', 'NO-ID'));
        return (
          <ImageBackground  style={{flex: 1,alignItems: 'center',justifyContent: 'center'}}>
            <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center'}}>
            <View style={{flex: 1, paddingTop: 100}}>
              <View style={{flex: 1, textAlign: 'center', marginEnd: 10, marginStart: 10}}>
              <Text style= {{fontWeight: 'bold', fontSize:40, width: width, textAlign: 'center'}}>{this.state.timePassed}</Text>
              </View>
              <View style={{flex: 2, marginEnd: 10, marginStart: 10}}>
            <Text style= {{fontWeight: 'bold', fontSize:30, width: width, textAlign: 'center', marginBottom: 10}}>Shaking activity</Text>
            <Text style= {{fontWeight: 'bold', fontSize:20, width: width, textAlign: 'center', marginBottom: 10}}>
            Shake your device until the time runs out.
            </Text>
            </View>
            </View>
            <View style={{flex: 1, alignItems: 'center', justifyContent:'center', marginBottom: 50}}>
            <Button size={20} disabled={this.state.startButton} text="Start" onClick={() => this.startCounting()}></Button>
            <Button size={20} disabled={this.state.nextTaskButton} text="Go to the next task" onClick={() => this.onPressNextScreen()}/>
            </View>
          </View>
          </ImageBackground>  
        );
      }
  }

