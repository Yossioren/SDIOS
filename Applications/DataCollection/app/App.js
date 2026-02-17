import React, {useState} from 'react';
import { StyleSheet, Text, View , Button ,TextInput ,ImageBackground } from 'react-native';
import HomeScreen from './components/HomeScreen';
import RestingScreen from './components/RestingScreen';
import PocketScreen from './components/PocketScreen';
import RunningScreen from './components/RunningScreen';
import ShakingScreen from './components/ShakingScreen';
import TextingScreen from './components/TextingScreen';
import ApprovmentScreen from './components/ApprovmentScreen';
import ExperimentDescriptionScreen from './components/ExperimentDescription';
import WalkingScreen from './components/WalkingScreen';
import GameScreen from './components/Game/Game';
import PreGameScreen from './components/PreGame';
import FinalScreen from './components/FinalScreen';
import {createAppContainer} from 'react-navigation';
import {createStackNavigator} from 'react-navigation-stack';



const AppNavigator = createStackNavigator(
  {
    Home: {screen: HomeScreen},
    Rest: {screen: RestingScreen},
    Pocket: {screen: PocketScreen},
    Running: {screen: RunningScreen},
    Shaking: {screen: ShakingScreen},
    Texting: {screen: TextingScreen},
    Walking: {screen: WalkingScreen},
    Approvment: {screen: ApprovmentScreen},
    Description: {screen: ExperimentDescriptionScreen},
    PreGame: {screen: PreGameScreen},
    Game: {screen: GameScreen},
    Final: {screen: FinalScreen}
  },
  {
    initialRouteName: 'Home',
    headerMode: 'none',
    navigationOptions: {
        headerVisible: false,
    }
  }
);

const styles = StyleSheet.create({ 
  container: {
    flex: 1,
    //backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});


export default createAppContainer(AppNavigator);
