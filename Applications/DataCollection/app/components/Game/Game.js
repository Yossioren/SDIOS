import React, { Component } from "react";
import { StyleSheet, Text, View, Dimensions, Alert } from "react-native";
import * as Device from 'expo-device';
import {Accelerometer} from "expo-sensors";
import { GameEngine } from "react-native-game-engine";
import Matter from "matter-js";
import randomColor from "randomcolor";
import Circle from "./GameComp/Circle";
import Box from "./GameComp/Box";
import Sensors from "../Sensors";
import { activateKeepAwake } from 'expo-keep-awake';

const FPS = 50;
const frameFPS = 1000/FPS;
var ballSensitivity = 3;
const { height, width } = Dimensions.get('window');
let accListener;
const DEBRIS_NUM = 6;
const BALL_SIZE = 20;
const DEBRIS_HEIGHT = height/10;
const DEBRIS_WIDTH = width/9;
let randomNumber = 0;
var lastAccEntryTime = -1;
var oldAvg = 0.5;
const mid_point = (width / 2) - (BALL_SIZE / 2);
const ballSettings = {
  isStatic: true
};

const debrisSettings = {
  isStatic: false,
  density: 50
};

const ball = Matter.Bodies.circle(0, height - 30, BALL_SIZE, {
  ...ballSettings,
  label: "ball"
});

const floorThick = 100;
const floorWidth = 4*width;
const floorStratX = width/2;
//rectangle(x,y, width, higth)
//https://pusher.com/tutorials/game-device-sensors-react-native
const floor = Matter.Bodies.rectangle(floorStratX, height+floorThick, floorWidth, floorThick, {
  isStatic: true,
  isSensor: true,
  label: "floor"
});

function randomInt(bot, top){
  return Math.floor(Math.random() * (top+1) + bot);
}

export default class Game extends Component {
  state = {
    x: mid_point,
    y: height,
    isGameReady: false,
    timePassed : 60,
    score: 0,
    speed: 4.5
  }


  constructor(props) {
    super(props);
    const str_brand = (''+Device.brand).toLowerCase();
    //console.log(str_brand, str_brand==='apple');
    if(str_brand==='apple'){//ios produce far less accelerometer data, therefore we need to have more sensitivity for the ball
      ballSensitivity = 2*ballSensitivity;
    }
    Accelerometer.setUpdateInterval(15);
    this.debris = [];
    this.sensor = new Sensors();
    const { engine, world } = this._addObjectsToWorld(ball);
    this.entities = this._getEntities(engine, world, ball);

    this._setupCollisionHandler(engine);

    this.physics = (entities, { time }) => {
      let engine = entities["physics"].engine;
      engine.world.gravity.y = this.state.speed;
      Matter.Engine.update(engine, time.delta);
      return entities;
    };
    this.reset(0);
  }

  _interval = setInterval(() => {
    this.setState(state => ({
      timePassed :  state.timePassed  - 1
    }), () => {
      if(this.state.timePassed == 0){
        this.endInterval();
      }
    });
  }, 1000);

  acceletometerInput = 
    (pos) => {
      let now=Date.now();
      //console.log(now,lastAccEntryTime,pos);
      if(now - lastAccEntryTime < frameFPS)
        return;
      lastAccEntryTime = now;//prevent sprints from sensors brust
      oldAvg = oldAvg*0.995 + pos.x*0.005;
      if(oldAvg < 0.15)
        oldAvg = 0.15;
      //console.log(this.state.x + pos.x/oldAvg + ' ' + width);
      let newX = this.state.x + ballSensitivity*(pos.x/oldAvg);
      let newY = height - 30;
      let toInsert = -1;

      if (newX < 0)
        toInsert = width;
      else if(newX > width)
        toInsert = 0;
      else
        toInsert = newX;
      
      if(toInsert != -1){
        Matter.Body.setPosition(ball, {
          x: toInsert,
          y: newY
        });
        this.setState({x:  toInsert});
      }
    }

  componentDidMount() {
    this.sensor.startMessure();
    accListener = Accelerometer.addListener(this.acceletometerInput);

    this.setState({isGameReady: true});
  }

  endInterval(){    
    clearInterval(this._interval);
    this.sensor.sendMessures("game",randomNumber, ()=>{
      this.componentWillUnmount()
      this.props.navigation.navigate('Final', {
        randomNumber: RandomN,
      })});
    this.setState({
      isGameReady: false,
      speed: 0
    });
  }

  componentWillUnmount() {
    accListener.remove();
    this.debris.forEach((debris) => { 
      Matter.Body.set(debris, {
        isStatic: true 
      });
    });
  }

   getRandomDecimal = (min, max) => {
    return Math.random() * (max - min) + min;
  }
  _addObjectsToWorld = (ball) => {
    const engine = Matter.Engine.create({ enableSleeping: false });
    const world = engine.world;

    let objects = [
      ball,
      floor
    ];

    for (let x = 0; x < DEBRIS_NUM; x++) {
      const debris = Matter.Bodies.rectangle(
        randomInt(1, width - DEBRIS_WIDTH),
        randomInt(0, 200),
        DEBRIS_WIDTH,
        DEBRIS_HEIGHT,
        {
          frictionAir: this.getRandomDecimal(0.2, 0.3),
          label: 'debris'
        }
      );

      this.debris.push(debris);
    }

    objects = objects.concat(this.debris);

    Matter.World.add(world, objects);

    return {
      engine,
      world
    }
  }


  _getEntities = (engine, world, ball) => {
    const entities = {
      physics: {
        engine,
        world
      },

      playerBall: {
        body: ball,
        size: [BALL_SIZE, BALL_SIZE],
        renderer: Circle
      },

      gameFloor: {
        body: floor,
        size: [floorWidth, floorThick],
        color: '#FF0000',
        renderer: Box
      }
    };

    for (let x = 0; x < DEBRIS_NUM; x++) {

      Object.assign(entities, {
        ['debris_' + x]: {
          body: this.debris[x],
          size: [DEBRIS_WIDTH, DEBRIS_HEIGHT],
          color: randomColor({
            luminosity: 'dark',
          }),
          renderer: Box
        }
      });

    }

    return entities;
  }

 
  _setupCollisionHandler = (engine) => {
    var alerted = false;
    Matter.Events.on(engine, "collisionStart", (event) => {
      var pairs = event.pairs;

      var objA = pairs[0].bodyA.label;
      var objB = pairs[0].bodyB.label;

      if (objA === 'floor' && objB === 'debris') {
        Matter.Body.setPosition(pairs[0].bodyB, {
          x: randomInt(1, width - 30),
          y: randomInt(0, 200)
        });
      
      this.setState(state => ({
        score: state.score + 1
      }));
    }
      if (objA === 'ball' && objB === 'debris' && this.state.isGameReady) {
        if(!alerted){
          alerted = true;
          let tmp_score = this.state.score;
          Alert.alert(
              'Watch out',
              'Avoid the bricks!',[
            {text: 'Ok', onPress: () => {
              this.reset(Math.floor(tmp_score/2));
            }}],
          );
        }
        else
          this.reset(Math.floor(this.state.score/2));
      }
    });
  }


  render() {
    const { isGameReady, timePassed, score } = this.state;
    const { navigation } =this.props;
    activateKeepAwake();
    randomNumber = JSON.stringify(navigation.getParam('randomNumber', 'NO-ID'));

    if (isGameReady) {
      return (
        <GameEngine
          style={styles.container}
          systems={[this.physics]}
          entities={this.entities}
        >
          <View style={styles.header}>
            <Text style={styles.scoreText}>Time:{timePassed}</Text>
            <Text style={styles.scoreText}>Score:{score}</Text>
          </View>
        </GameEngine>
      );
    }

    return null;
  }


  reset = (newScore) => {
    this.debris.forEach((debris) => { // loop through all the blocks
      Matter.Body.set(debris, {
        isStatic: false // make the block susceptible to gravity again
      });
      Matter.Body.setPosition(debris, { // set new position for the block
        x: randomInt(1, width - 30),
        y: randomInt(0, 200)
      });
    });
    if (this.state.score > 0) {
      this.setState({ 
        score: newScore // reset the player score
      });
    }
  }

}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5FCFF',
    marginBottom: 10,
    marginTop: 30
  },
  header: {
    padding: 20,
    alignItems: 'center'
  },
  scoreText: {
    fontSize: 25,
    fontWeight: 'bold',
    width: width,
    textAlign: 'center'
  }
});