import React, {useState,useEffect} from 'react';
import { View, Text, ImageBackground, BackHandler, Dimensions, StyleSheet } from 'react-native';
import Button from './Button'
import { useKeepAwake } from 'expo-keep-awake';


const { height, width } = Dimensions.get('window');


const HomeScreen = (props) => {
    useKeepAwake();
    
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
        const { navigate } = props.navigation
        navigate('Approvment');
    }
    return (
        <ImageBackground  style={styles.container}>
            <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center'}}>
            <View style={{flex: 1, paddingTop: 200}}>
              <View style={{flex: 1, textAlign: 'center', marginEnd: 10, marginStart: 10}}>
              <Text style= {{fontWeight: 'bold', fontSize:40, width: width, textAlign: 'center'}}>Welcome To SDIOSApp</Text>
              </View>
            </View>
            <View style={{flex: 10, alignItems: 'center', justifyContent:'center', marginBottom: 50}}>
            <Button size={30} text="Start" onClick={()=>this.onPress()}/>
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

export default HomeScreen;