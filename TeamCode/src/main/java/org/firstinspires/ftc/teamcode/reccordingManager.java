package org.firstinspires.ftc.teamcode;
import java.io.*;

/**
 * Created by Mike on 9/27/2017.
 */
public class reccordingManager {
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
    //=== Does recording
    public void saveSettings(){}
    public void start(){}
    public void observe(){}
    public void endAndSave(){}

    //=== Does Playback
    public boolean isFinished = false;
    public void getNextStep(){}
}
