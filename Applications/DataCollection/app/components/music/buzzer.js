import {Audio} from 'expo-av';

const finishSound = new Audio.Sound();
finishSound.loadAsync(require("./buzzer.mp3"));

export async function makeBuzzerSound(){
    try {
        await finishSound.playAsync();
    } catch (error) {console.error(error);}
}