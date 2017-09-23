package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import static java.lang.Math.abs;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import java.util.ArrayList;
/*
import org.json.simple.JSONObject;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
*/
import java.io.*;

/**
 * Created by Mike on 9/23/2017.
 */
@TeleOp(name = "HolonomicDriveReccord")
public class HolonomicDriveReccord extends LinearOpMode {

    //=========== Settings and Config ==============
    public String[] motorNames = {"motorLeftFront","motorLeftRear","motorRightFront","motorRightRear"};
    public int[] MotorMappings = {0,1,2,3};
    public boolean[] MotorReverse = {false,false,false,false}; //applied after mappings--> corresponds to index of mapped motor
    public double PrecisionSpeed = 0.5;// 0.5 means half speed... tap & release <y> to toggle precision speed
    public double RegularSpeed = 1.0;
    public boolean[] InvertControlsXY = {false,false};
    public double stickThreshold = 0.15;
    public Gamepad commands = gamepad1; //The controller that clicks <start> <a> is gamepad 1
    //public boolean enableRecording = false;//Allow the user to reccord the gamepad inputs by toggling <start>
    //public boolean enablePlayback = false;//Allow the user to play the previously recorded inputs by pressing <guide>

    //=========== Initilizations ================
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

        //Store the state of Precision Status:
        public boolean isPrecisionSpeed = false;

        //~~~ this may cause a memory overflow for LONG recordings: consider streaming the json...
        public ArrayList<Object> allSteps = new ArrayList<Object>();

        //=== Settings saved as part of the recording
        public Object[] recordingSettings= {PrecisionSpeed,RegularSpeed,stickThreshold,InvertControlsXY};

        //=== Initial Motor fetch
        public DcMotor[] AllMotors = {
                hardwareMap.dcMotor.get(motorNames[0]),
                hardwareMap.dcMotor.get(motorNames[1]),
                hardwareMap.dcMotor.get(motorNames[2]),
                hardwareMap.dcMotor.get(motorNames[3])
        };

        //=== Mapped Motor Storage
        public DcMotor[] MappedMotors = new DcMotor[4];

        //=== Custom Initilization method
        public void customInit(){
            //=== Initilize mappings:
            for(int j=0;j<4;j++){
                this.MappedMotors[j] = this.AllMotors[this.MotorMappings[j]];
            }

            //=== Initilize reverses
            for( int i=0; i<4; i++){
                //Reverses:
                if(this.MotorReverse[i]){
                    this.MappedMotors[i].setDirection(DcMotor.Direction.REVERSE);
                }

            }
        }


    //===========  Helper Methods ===========

        //== Activates Motors with activationValues parameter and a precision parameter
        public void activateMotors(double[] activationValues,boolean usePrecision){
            for(int k=0;k<4;k++){
                if(usePrecision){
                    //multiply by setting
                    this.MappedMotors[k].setPower(this.PrecisionSpeed*activationValues[k]);
                }else{
                    //multiply by setting
                    this.MappedMotors[k].setPower(this.RegularSpeed*activationValues[k]);
                }

            }
        }

        //== logical or in the strictest sense
        public boolean eXOR(boolean x, boolean y) {// Courtesy of ~stack overflow~
            //works like or except if both  are true then it is false also
            return ( ( x || y ) && ! ( x && y ) );
        }

        //== Check if input is above threshold defined by the class settings
        public boolean isAboveThreshold(double inputValue){
            //Returns true/false
            return abs(inputValue) > this.stickThreshold;
        }

        //== reccord the step:
        public void reccordStep(){
            Object[] stepFormatted = new Object[5];
            stepFormatted[0] = this.commands.left_bumper;
            stepFormatted[1] = this.commands.right_bumper;
            stepFormatted[2] = this.commands.left_stick_x;
            stepFormatted[3] = this.commands.left_stick_y;
            stepFormatted[4] = this.commands.y;

            //save the object
            this.allSteps.add(stepFormatted);

        }

    //=========== Run the Op Mode ===========
    public void runOpMode() throws InterruptedException{

        //Run the custom initilizations!
        this.customInit();

        //=== Wait for Start
        waitForStart();

        //=== Run the Loop
        while(opModeIsActive()){
            //=== Use Precision Modifier:
            if(this.commands.y){
                this.isPrecisionSpeed = !this.isPrecisionSpeed; // toggle precision speed by 'clicking' y
            }

            //=== Rotation Movement: left_bumper = CounterClockwise, right_bumper = Clockwise (Makes more sense than the reverse...)
            if(this.eXOR(this.commands.left_bumper, this.commands.right_bumper)){
                if(this.commands.right_bumper){// rotate Clockwise
                    double[] clockActivations = {1.0,1.0,-1.0,-1.0};
                    this.activateMotors(clockActivations,this.isPrecisionSpeed);
                }else{ //if right is false than right must be true to meet the initial condition
                    double[] cntrClockActivations = {-1.0,-1.0,1.0,1.0};
                    this.activateMotors(cntrClockActivations,this.isPrecisionSpeed);
                }
            }

            //Redefine LEFT Stick Values (invert if settings say so):
            double stick_x = this.InvertControlsXY[0] ? -this.commands.left_stick_x : this.commands.left_stick_x;
            double stick_y = this.InvertControlsXY[1] ? -this.commands.left_stick_y : this.commands.left_stick_y;

            //=== Planar Movement XY
                //=== Natural Inversion Config :
                double[] horozontalActivations = {-stick_x,stick_x,stick_x,-stick_x};
                double[] verticalActivations = {-stick_x,-stick_x,-stick_x,-stick_x};
                double[] stayStill = {0.0,0.0,0.0,0.0};

                //== Movement Forewards & Reverse (vertical):
                if(this.isAboveThreshold(stick_y) && !this.isAboveThreshold(stick_x)){
                    this.activateMotors(verticalActivations,this.isPrecisionSpeed);
                }
                //== Movement Strafe (horozontal):
                if(this.isAboveThreshold(stick_x) && !this.isAboveThreshold(stick_y)){
                    this.activateMotors(horozontalActivations,this.isPrecisionSpeed);
                }

                //== Movement STILL
                if(!this.isAboveThreshold(stick_y) && !this.isAboveThreshold(stick_x)){
                    this.activateMotors(stayStill,this.isPrecisionSpeed);
                }

            idle();
        }
    }
}
