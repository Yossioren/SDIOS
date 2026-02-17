import React, {Component} from 'react';
import {TouchableOpacity, Text, StyleSheet} from 'react-native';

export default class Button extends Component{
  constructor() {
    super();
  }
  
  onPress = () =>{
      if(this.props.onClick){
        this.props.onClick();
    }
  }
  
      render() {
        return (
            <TouchableOpacity
            style={this.props.disabled ? styles.disabled : styles.button}
            onPress={this.onPress}
            disabled={this.props.disabled}
          >
            <Text style={{fontSize: this.props.size}}> {this.props.text} </Text>
          </TouchableOpacity>
        );
      }
  }
  const styles = StyleSheet.create({
    container: {
      flex: 1,
      justifyContent: 'center',
      paddingHorizontal: 10,
      
    },
    button: {
      alignItems: 'center',
      backgroundColor: '#5080ff',
      padding: 10,
      marginBottom: 10
    },
    countContainer: {
      alignItems: 'center',
      padding: 10
    },
    countText: {
      color: '#FF00FF'
    },
    disabled: {
      alignItems: 'center',
      backgroundColor: '#807b7a',
      padding: 10,
      marginBottom: 10
    }
  })