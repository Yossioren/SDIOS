import React, {useState,useEffect} from 'react';
import { View, Text, TextInput, ImageBackground, BackHandler, Dimensions, StyleSheet } from 'react-native';
import Button from './Button';


const { height, width } = Dimensions.get('window');


const PreGame = (props) => {
    useEffect(() => {
        BackHandler.addEventListener('hardwareBackPress', handleBackButtonClick);

          return () => {
            BackHandler.removeEventListener('hardwareBackPress', handleBackButtonClick);
          }
    });
handleBackButtonClick = () => {
        return true;
    }
    onPress = () => {
        props.navigation.navigate('Game', {
            randomNumber: RandomN,
        });
        //const { navigate } = props.navigation
        //navigate('Game');
    }
    const { navigation } = props
    randomNumber = JSON.stringify(navigation.getParam('randomNumber', 'NO-ID'));
    return (
        <ImageBackground  style={styles.container}>
            <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center'}}>
            <View style={{flex: 1, paddingTop: 200}}>
              <View style={{flex: 2, marginEnd: 10, marginStart: 10}}>
            <Text style= {{fontWeight: 'bold', fontSize:30, width: width, textAlign: 'center', marginBottom: 10}}>Game activity</Text>
            <Text style= {{fontWeight: 'bold', fontSize:20, width: width, textAlign: 'center', marginBottom: 10}}>
            In this stage we are going to messure while you play
            </Text>
            <Text style= {{fontWeight: 'bold', fontSize:20, width: width, textAlign: 'center', marginBottom: 10}}>
             a little game. The goal in this game is escaping the
            </Text>
            <Text style= {{fontWeight: 'bold', fontSize:20, width: width, textAlign: 'center', marginBottom: 10}}>
             falling bricks. Let's see if you can escape them all
            </Text>
            </View>
            </View>
            <View style={{flex: 1, alignItems: 'center', justifyContent:'center', marginBottom: 50}}>
            <Button size={20} text="Start" onClick={() => onPress()}/>
            </View>
          </View>
        </ImageBackground>    
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    inner: {
        fontWeight: 'bold',
        borderWidth: 1,
        borderColor: 'black',
        borderStyle: 'solid',
        padding: 10
    }
});

export default PreGame;