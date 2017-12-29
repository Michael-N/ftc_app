package org.firstinspires.ftc.teamcode;

/**
 * Created by Mike on 12/28/2017.
 */
import com.qualcomm.robotcore.hardware.Gamepad;
import com.fasterxml.jackson.databind.*;
import java.io.File;
import java.util.ArrayList;

public class CommandObserver {
        /*
        *   Record(Gamepad): build list of gamepad coppies
        *   playback(): return the next command in the list
        *   Save(Filename): save as the json File Name
        *   open(filename): Open a json and Convert
        * */
    //Json Handler
    ObjectMapper myJsonHandler = new ObjectMapper();

    //Save Recorded Commands
    public ArrayList<Gamepad> gamepadsToSave = new ArrayList<Gamepad>();
    //Open Recorded Commands
    public ArrayList<Gamepad> recordedGamepads;
    public int currentPlaybackIndex = 0;

    //== Record Current Gamepad
    public void record(Gamepad currentGamepad){
        gamepadsToSave.add(currentGamepad);
    }

    //== Get the Next Command
    public Gamepad playback(){
        if(currentPlaybackIndex< recordedGamepads.size()){
            //return the commands
            Gamepad playbackGamepad = recordedGamepads.get(currentPlaybackIndex);
            currentPlaybackIndex +=1;

            return playbackGamepad;
        }else{
            // Return 'empty' gamepad = no commands
            return new Gamepad();
        }
    }

    //== Save the Recording to a filepath:
    public void save(String filepathname){
        try{
            recordedGamepads = myJsonHandler.writeValue(new File(filepathname),recordedGamepads);// skip exception handling for jackson... they wrote it wrong...
        }catch(java.io.IOException fileError){

        }
    }

    //== Save the Recording to a filepath:
    public void open(String filepathname){
        try{
            recordedGamepads = myJsonHandler.readValue(new File(filepathname),ArrayList.class);// skip exception handling for jackson... they wrote it wrong...
        }catch(java.io.IOException fileError){
            fileError.setStackTrace();
        }
    }


}
