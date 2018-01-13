package org.firstinspires.ftc.teamcode;

/**
 * Created by Mike on 12/28/2017.
 */

import com.qualcomm.robotcore.hardware.Gamepad;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;

import org.firstinspires.ftc.robotcore.external.Telemetry;



  /*
        *   Record(Gamepad): build list of gamepad coppies
        *   playback(): return the next command in the list
        *   Save(Filename): save as the json File Name
        *   open(filename): Open a json and Convert
        * */


public class CommandObserver {


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
    public void save(String filepathname, Telemetry telem){

        try{
            //Json Handler
            ObjectMapper mapper = new ObjectMapper();

            //Temp

            String jsonData = mapper.writeValueAsString(this.recordedGamepads);
            /*
            //Write To Byte Stream
                ByteArrayOutputStream temp = new ByteArrayOutputStream();
                mapper.writeValue(temp,);

                //Get Text From ByteStream
                final byte[] bytedata = temp.toByteArray();

                //Convert to string
                String jsonData = new String(bytedata);
            */
            telem.addData("Command Observer [Save]:","Json Made "+jsonData);
            //Create the new file:
            File recordingFile = new File(filepathname);
            recordingFile.createNewFile();
            telem.addData("Command Observer [Save]:","File Created");

            //Write the String Json Data to the file:
            FileWriter writer = new FileWriter(recordingFile);
            writer.write(jsonData);
            writer.flush();
            telem.addData("Command Observer [Save]:","Data Written");

            //Close
            writer.close();
            telem.addData("Command Observer [Save]:","File Closed");


        }catch(Exception err){
            err.printStackTrace();
            telem.addData("FILE THING:",err);
        }
    }

    //== Save the Recording to a filepath:
    public void open(String filepathname){
        try{
            File recordingFile  = new File(filepathname);
            ObjectMapper mapper = new ObjectMapper();
            String jsonFromFile= new Scanner(recordingFile).useDelimiter("\\Z").next();
            ArrayList<Gamepad> convertedFileData = mapper.readValue(jsonFromFile, new TypeReference<ArrayList<Gamepad>>(){});
            this.recordedGamepads = convertedFileData;
        }catch(Exception err){
            err.printStackTrace();
        }
    }


}
