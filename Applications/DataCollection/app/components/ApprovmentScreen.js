import React, {Component} from 'react';
import { View, Text ,ImageBackground, BackHandler, StyleSheet, Switch, ScrollView, Dimensions } from 'react-native';
import Button from './Button'
import CheckBox from './CheckBox';
import Sensors from './Sensors';


const { height, width } = Dimensions.get('window');

const randomDigits = 15;
const baseRandom = Math.pow(10,randomDigits);

export default class ApprovmentScreen extends Component{

    constructor() {
        super();
        BackHandler.addEventListener('hardwareBackPress', handleBackButtonClick);
        sensor = new Sensors();
    }
    componentWillUnmount() {
        BackHandler.removeEventListener('hardwareBackPress', handleBackButtonClick);
    }
    handleBackButtonClick = () => {
        return true;
    }

    GenerateRandomNumber=()=>{
        let RandomNumber = Math.floor(Math.random() * baseRandom*10) + baseRandom ;
        RandomN = RandomNumber;
        sensor.initialRandomNumber(RandomNumber);
    }

    onPressNextScreen=()=>{
        this.GenerateRandomNumber();
        this.props.navigation.navigate('Rest', {
            randomNumber: RandomN,
        });
    }


    onPress = () => {
        this.props.navigation.navigate('Home')
    }

    render() {
        const { navigate } =this.props.navigation;
        return (
            <ImageBackground  style={{flex: 1,alignItems: 'center',justifyContent: 'center'}}>
                <View style={{ flex: 1, alignItems: 'center', width: width, justifyContent: 'center', marginBottom: 10, marginTop: 10}}>
                <ScrollView>
                    <View style={{padding: 20, flex: 0.9 }}>
                        <Text style= {styles.titleText}>The research is conducted in the department of Software and Information Systems Engineering,</Text>
                        <Text style= {styles.titleText}>at the Ben-Gurion University of the Negev.</Text>
                        <Text style= {styles.titleText}>In this research, you are required to install an application and perform a sequence of actions.</Text>
                        <Text style= {styles.titleText}>While you will perform the actions, the app will record your smartphone position sensors' output.</Text>
                        <Text style= {styles.titleText}>The experiment will last around five minutes.</Text>
                        <Text style= {styles.titleText}>The experiment can be performed on the subject phone or a lab's phone, however, since the app is</Text>
                        <Text style= {styles.titleText}>already installed on the lab's phones a subject who will use a lab phone will need to experiment twice,</Text>
                        <Text style= {styles.titleText}>(since the application's installation time is not required).</Text>
                        <Text style= {styles.titleText}>The data will be sent to our private server for further analysis. The collected data will be saved anonymously.</Text>
                        <Text style= {styles.titleText}>The private server data is accessible just to the experiment team.</Text>
                        <Text style= {styles.titleText}>You can stop participating in the experiment at any point, however,</Text>
                        <Text style= {styles.titleText}>if you will not finish the measures you will not gain the participation credit</Text>
                    </View>
                </ScrollView>
                <Text style= {{fontWeight: 'bold', fontSize:15,width: width, flexWrap: 'wrap', color: '#000000', textAlign: 'center'}}>I confirm my participation in the experiment</Text>
                <CheckBox checked={false} onCheck={this.onPressNextScreen} size={30} color="#000000" ></CheckBox>
                <Button text="Go to Home" onClick={this.onPress}/>
                </View>
            </ImageBackground>
        );
    }
}

const styles = StyleSheet.create({
    titleText: {
        fontSize: 20,
        fontWeight: 'bold',
        flexWrap: 'wrap',
        color: '#000000',
    },
});
