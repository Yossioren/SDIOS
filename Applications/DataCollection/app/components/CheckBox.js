import React, {Component} from 'react';
import {TouchableHighlight, Text, View, StyleSheet} from 'react-native';

const styles = StyleSheet.create(
    {

      uncheckedView:
      {
        flex: 1,
        backgroundColor: 'white'
      },
    });

export default class Checkbox extends Component {

    constructor() {
  
      super();
  
      this.state = { checked: null }
    }
  
    componentDidMount() {
  
      if (this.props.checked) {
        this.setState({ checked: true }, () => {
          this.props.selectedArrayObject.pushItem({
            'key': this.props.keyValue,
            'label': this.props.label,
            'value': this.props.value
          });
        });
      }
      else {
        this.setState({ checked: false });
      }
    }
  
    onCheck() {
      if(this.props.onCheck){
          this.props.onCheck();
      }
    }
  
    render() {
      return (
  
        <TouchableHighlight
          onPress={() => this.onCheck()}
          underlayColor="transparent"
          style={{ marginVertical: 10, borderColor: 'black' }}>
  
          <View style={{ flexDirection: 'row', alignItems: 'center' }}>
  
            <View style={{ width: this.props.size, height: this.props.size, backgroundColor: this.props.color, padding: 3 }}>
                
                  <View style={styles.uncheckedView} />
  
            </View>
  
  
          </View>
  
        </TouchableHighlight>
      );
    }
  }