import React ,{Component} from 'react';
import { TouchableOpacity, Clipboard, View, Text, ImageBackground, BackHandler, StyleSheet, Alert, Dimensions,} from 'react-native';
import Button from './Button'
import {getSucceededMessages, getSentMessages} from './Sensors'
const { height, width } = Dimensions.get('window');

export default class FinalScreen extends Component{

     
  constructor(props) {
    super(props);
    BackHandler.addEventListener('hardwareBackPress', handleBackButtonClick);
    const { navigation } = props;
    let randomNumber = JSON.stringify(navigation.getParam('randomNumber', 'NO-ID'));
    this.state = {
        NumberHolder : randomNumber,
        textHolder: "The approval number for your participation is (tap to copy): ",
        randomButton: false,
        ok: getSucceededMessages(),
        tot: getSentMessages()
    }
  }
  handleBackButtonClick = () => {
    return true;
  }
  componentWillUnmount() {
    BackHandler.removeEventListener('hardwareBackPress', handleBackButtonClick);
  }
  onPress = () => {
    this.props.navigation.navigate('Description')
  }

  writeToClipboard = async (text) => {
    await Clipboard.setString(text);
    Alert.alert(
        'Copied!',
        'Your approvement number has been copied to your Clipboard!',[
      {text: 'Ok'}],
    );
  }; 
  render() {
    return (
      <ImageBackground style={{flex: 1,alignItems: 'center',justifyContent: 'center'}}>
        <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center'}}>
          <TouchableOpacity onPress={()=>this.setState({ok:getSucceededMessages(), tot:getSentMessages()})}>
            <Text style= {{fontWeight: 'bold', fontSize:30, width: width, textAlign: 'center'}}>
              {this.state.ok}/{this.state.tot} recived_ok/sent
            </Text>
          </TouchableOpacity>
          <Text style= {{fontWeight: 'bold', fontSize:30, width: width, textAlign: 'center'}}>Thank you for participating in the experiment</Text>
          <TouchableOpacity onPress={()=>this.writeToClipboard(this.state.NumberHolder)}>
              <Text style= {{fontWeight: 'bold', fontSize:24, width: width, textAlign: 'center'}}>{this.state.textHolder}</Text> 
              <Text style= {{fontWeight: 'bold', fontSize:30, color:'#cc3300', width: width, textAlign: 'center'}}>{this.state.NumberHolder} </Text>
          </TouchableOpacity>
          <Button size={20} text="Experiment description" onClick={()=>this.onPress()}></Button> 
        </View>
      </ImageBackground>  
    );
  } 
}

