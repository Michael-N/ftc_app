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
    //=== Store the steps Recorded and read
    public JSONArray allStepsObserved;
    public JSONArray allStepsRead;
    public int currentStep=0;

    //=== Build a JSON representation of a gamepad sample
    public JSONObject __buildStep(Gamepad commands){
        JSONObject currentStep = new JSONObject();

        try{
            //== Buttons
            currentStep.put("y",commands.y);
            currentStep.put("x",commands.x);
            currentStep.put("a",commands.a);
            currentStep.put("b",commands.b);
            //== Bumpers
            currentStep.put("left_bumper",commands.left_bumper);
            currentStep.put("right_bumper",commands.right_bumper);
            //== Triggers
            currentStep.put("left_trigger",commands.left_trigger);
            currentStep.put("right_trigger",commands.right_trigger);
            //== Sticks
            currentStep.put("left_stick_x",commands.left_stick_x);
            currentStep.put("left_stick_y",commands.left_stick_y);
            currentStep.put("right_stick_x",commands.right_stick_x);
            currentStep.put("right_stick_y",commands.right_stick_y);
            //=== dpad
            currentStep.put("dpad_down",commands.dpad_down);
            currentStep.put("dpad_up",commands.dpad_up);
            currentStep.put("dpad_right",commands.dpad_right);
            currentStep.put("dpad_left",commands.dpad_left);


        }catch(JSONException e){
            //do nothing...
        }
        return currentStep;
    }

    //=== Initalize Recording:
    public void start(){
        this.allStepsObserved  = new JSONArray();
    }

    //=== Record the current Commands:
    public void observe(Gamepad currentCommands){
        this.allStepsObserved.put(this.__buildStep(currentCommands));
    }

    //=== Write to file:
    public void endAndSave(){
        String reccordingJsonText = this.allStepsObserved.toString();
        try{
            String myFileName = "OpReccording-"+ new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            PrintWriter abstractFile = new PrintWriter(myFileName);
            abstractFile.println(reccordingJsonText);
            abstractFile.close();
        }catch(FileNotFoundException e){
            // do nothing
        }
    }

    //=== Read
    public void readRecording(String recordingFileName){
        /*
        *   To save time part of the following method code was adapted from
        *   https://www.caveofprogramming.com/java/java-file-reading-and-writing-files-in-java.html
        *   ALL Credit to that author!!!!
        * */

        String allLines = "";
        String currentLine = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(recordingFileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            //Save all the lines to the string
            while((currentLine = bufferedReader.readLine()) != null) {
                allLines = allLines + currentLine;
            }

            // Always close files.
            bufferedReader.close();

            this.allStepsRead = new JSONArray(allLines);
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + recordingFileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + recordingFileName+ "'");
        }catch(JSONException ex){
            System.out.println("Unable To Interpret JSON OBJECT");
        }
    }

    //=== next Step Exists
    public boolean nextStepExists(){
        boolean isDone = allStepsRead.length()-this.currentStep > 0;
        if(isDone){
            this.currentStep = 0;
        }
        return isDone;
    }

    //=== get the next or first or last step...
    public Object getNextStep(){
        Object currentStepObject;
        try{
            currentStepObject = allStepsRead.get(this.currentStep);
            this.currentStep = this.currentStep + 1;
            return currentStepObject;
        }catch(JSONException ex){
            //do Nothing
        }

    }
}
