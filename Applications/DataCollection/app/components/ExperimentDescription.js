import React, {Component} from 'react';
import { View, Text, ImageBackground, BackHandler, Dimensions, StyleSheet} from 'react-native';
import Button from './Button';

export default class PocketScreen extends Component{
  constructor() {
    super();
    BackHandler.addEventListener('hardwareBackPress', handleBackButtonClick);
  }
  handleBackButtonClick = () => {
    return true;
}
componentWillUnmount() {
  BackHandler.removeEventListener('hardwareBackPress', handleBackButtonClick);
}
  
  
  
  onPressNextScreen(){
    this.props.navigation.navigate('Final', {
      randomNumber: RandomN,
    });
  }
  
      render() {
        const { navigation } =this.props
        randomNumber = JSON.stringify(navigation.getParam('randomNumber', 'NO-ID'));
        return (
          <ImageBackground  style={{flex: 1,alignItems: 'center',justifyContent: 'center'}}>
            <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center'}}>
            <View style={{flex: 1, paddingTop: 100}}>
              <View style={{flex: 2, marginEnd: 20, marginStart: 20}}>
            <Text style= {styles.titleText}>Project name:  Sensors Defense In android OS (SDIOS)</Text>
            <Text style= {styles.paragraphText}>
            The purpose is to build a machine learning model that is capable of identifying an attack on the position sensors of smartphones.
            </Text>
            <Text style= {styles.paragraphText}>
            Experiment description:
            </Text>
            <Text style= {styles.paragraphText}>
            Simplified steps to complete the project.</Text>
            <Text style= {styles.paragraphText}>
            1.Collect and store position sensors' output.</Text>
            <Text style= {styles.paragraphText}>
            2.Analyze the raw data.</Text>
            <Text style= {styles.paragraphText}>
            3.Train a machine learning model that will be able to defend smartphones from such attacks.</Text>
            <Text style= {styles.paragraphText}>
            4.Integrate the model in the smartphone to defend it.</Text>
            </View>
            </View>
            <View style={{flex: 1, alignItems: 'center', justifyContent:'center', marginBottom: 50}}>
            
            </View>
            </View>
            <Button size={20} text="close" onClick={()=>this.onPressNextScreen()}/>
          </ImageBackground>  
        );
      }
  }
  const styles = StyleSheet.create({
    titleText: {
        fontSize: 24,
        fontWeight: 'bold',
        flexWrap: 'wrap',
        color: '#000000',
    },
    paragraphText: {
      fontSize: 20,
      fontWeight: 'bold',
      flexWrap: 'wrap',
      color: '#000000',
  },
});