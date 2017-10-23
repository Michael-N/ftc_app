package org.firstinspires.ftc.teamcode;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * Created by Mike on 9/27/2017.
 */
public class recordingManager {
    //Recording data storage:
            /*      Outline:
            *       -Save some of the settings in the json file
            *            + Precision Speed
            *            + Regular Speed
            *            + Stick Threshold
            *            + invertControlsXY
            *       -apply those settings before running
            *       -Save the UserGamepad input NOT the activationValues
            *           +truncated gamepad object
            *           + time @ step n after idle()  - time @ step n begin
            *       -Program in the delay caused by idle()
            *       -instant test playback by pressing <guide>
            *       -load a reccording from a json file on the phone
            *       - All Requirements:
            *           import org.json.simple.JSONObject; // For JSON decoding and encoding
            *           import org.json.simple.JSONObject;
            *           import org.json.simple.JSONArray;
            *           import org.json.simple.parser.ParseException;
            *           import org.json.simple.parser.JSONParser;
            *           import java.io.*; // For writing and reading the files
            * */
    //=== Store the steps
    public JSONArray allSteps;
    //=== Build a JSON representation of a gamepad sample
    public JSONObject __buildStep(Gamepad commands){
        JSONObject currentStep = new JSONObject();

        try{
            currentStep.put("y",commands.y);
            currentStep.put("left_bumper",commands.left_bumper);
            currentStep.put("right_bumper",commands.right_bumper);
            currentStep.put("left_stick_x",commands.left_stick_x);
            currentStep.put("left_stick_y",commands.left_stick_y);

        }catch(JSONException e){
            //do nothing...
        }
        return currentStep;
    }
    //=== Initilize Recording:
    public void start(){
        this.allSteps  = new JSONArray();
    }
    //=== Reccord the current Commands:
    public void observe(Gamepad currentCommands){
        this.allSteps.put(this.__buildStep(currentCommands));
    }
    //=== Write to file:
    public void endAndSave(){
        String reccordingJsonText = this.allSteps.toString();
        try{
            String myFileName = "OpReccording-"+ new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            PrintWriter abstractFile = new PrintWriter(myFileName);
            abstractFile.println(reccordingJsonText);
            abstractFile.close();
        }catch(FileNotFoundException e){
            // do nothing
        }
    }

}
