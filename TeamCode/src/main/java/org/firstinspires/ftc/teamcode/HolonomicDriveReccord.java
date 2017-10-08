package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import static java.lang.Math.abs;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import java.util.Calendar;

/**
 * Created by Mike on 9/23/2017.
 */
@TeleOp(name = "HolonomicDriveReccord")
public class HolonomicDriveReccord extends LinearOpMode {
    //=========== Controls Guide ==================
    /*
    *   Rotation: <Right-Bumper>= Clockwise
    *             <Left-Bumper>= CounterClockwise
    *
    *   Precision: <y> = toggle precision/regular speed
    *
    *   Strafing (Horozontal):  <left-stick-x> = maped right to right and left to left unless controls setting invertControlsXY[0] is true
    *
    *   Forewards/Backwards (Vertical): <left-stick-y> maped up to forewards and down to backwards unless setting invertControlsXY[1] is true
    *
    *   ~~~Planed Features~~~
    *   -servo
    *   -reccording
    *   -playback
    *
    * */

    //=========== Settings and Config ==============
        //=== Motors
        public String[] motorNames = {"motorLeftFront","motorLeftRear","motorRightFront","motorRightRear"};
        public int[] motorMappings = {0,1,2,3};
        public boolean[] motorReverse = {true,true,false,false}; //applied after mappings--> corresponds to index of mapped motor
        //=== Speed
        public double precisionSpeed = 0.1;// 0.5 means half speed... tap & release <y> to toggle precision speed
        public double regularSpeed = 1.0;
        //=== Controls
        public boolean[] invertControlsXY = {false,false};
        public double stickThreshold = 0.15;
        public int toggleDelay = 70;//the Number of Ms since the last successful toggle, to prevent the next toggle
        public boolean useGamepad1 = true; //The controller that clicks <start> <a> is gamepad 1 else use gamepad2 <start> <b>
        //=== Reccording
        public boolean enableRecording = false;//Allow the user to reccord the gamepad inputs by toggling <start>
        public int maxTimeReccording  = 600;// Time in Sec
        //=== Servos
        public String[] servoNames = {"servoRight"}; // Use: same as Motors Section above
        public int[] servoMappings = {0};
        public boolean[] servoReverse = {false};
        //=== Playback
        public boolean enablePlayback = false;//Allow the user to play the previously recorded inputs by pressing <guide>

    //=========== Helper Classes ================
        //=== A toggle Class for True/False buttons
        /*    use:
                Toggleable yourButtonWithToggle = new Toggabable(Int DelayTimeInMs);
                yourButtonWithToggle.toggled(boolean yourButton); // returns true if a change is permitted else false
        */
        public class Toggleable {
            //Initilize toggleBegin etc..
            private Long lastAllowedToggle = System.currentTimeMillis();
            int delayMs;

            //Initilize a setting
            public Toggleable(int delayMs){
                this.delayMs = delayMs;
            }

            public boolean toggled(boolean gamepadButton){
                //Initilize the first time
                long testTime = System.currentTimeMillis();

                //Has the delay time passed?
                if(gamepadButton && (this.delayMs > (testTime-this.lastAllowedToggle))){
                    //reset the time return true
                    this.lastAllowedToggle = System.currentTimeMillis();
                    return true;
                }else{
                    //do nothing
                    return false;
                }

            }
        }

    //=========== Initilizations ================
        //=== Motors
        public DcMotor[] allMotors = new DcMotor[motorNames.length];//initial motor storage
        public DcMotor[] mappedMotors = new DcMotor[motorNames.length];//mapped motor storage
        //=== Speed
        public boolean isPrecisionSpeed = false;//Store the state of Precision Status:
        //=== Controls
        public Toggleable y = new Toggleable(toggleDelay);//Y-Toggle (must use a seperate toggleable instance for each button)
        //=== Reccording
        public boolean isReccording = false;
        public reccordingManager observer = new reccordingManager();
        //=== Servos
        public CRServo[] allServos = new CRServo[servoNames.length];//initial servo storage
        public CRServo[] mappedServos = new CRServo[servoNames.length];//mapped servo storage

        //=== Custom Init Method
        public void customInit(){
            //===== Motors
                //=== Initial Motor fetch
                for(int k=0;k<motorNames.length;k++){
                    this.allMotors[k]= hardwareMap.dcMotor.get(motorNames[k]);
                }
                //=== Initilize mappings:
                for(int j=0;j<motorNames.length;j++){
                    this.mappedMotors[j] = this.allMotors[this.motorMappings[j]];
                }
                //=== Initilize reverses
                for( int i=0; i<motorNames.length; i++){
                    //Reverses:
                    if(this.motorReverse[i]){
                        this.mappedMotors[i].setDirection(DcMotor.Direction.REVERSE);
                    }

                }
            //===== Servos
        }

    //===========  Helper Methods ===========

        //== Activates Motors with activationValues parameter and a precision parameter
        public void activateMotors(double[] activationValues,boolean usePrecision){
            for(int k=0;k<4;k++){
                if(usePrecision){
                    //multiply by setting
                    this.mappedMotors[k].setPower(this.precisionSpeed*activationValues[k]);
                }else{
                    //multiply by setting
                    this.mappedMotors[k].setPower(this.regularSpeed*activationValues[k]);
                }

            }
        }

        //== logical or in the strictest sense (the values must be opposites)
        public boolean eXOR(boolean x, boolean y) {// Courtesy of ~stack overflow~
            //works like or except if both  are true then it is false also
            return ( ( x || y ) && ! ( x && y ) );
        }

        //== Check if input is above threshold defined by the class settings
        public boolean isAboveThreshold(double inputValue){
            //Returns true/false
            return abs(inputValue) > this.stickThreshold;
        }

        //== dynamic control getter:
        public Gamepad getCommands(){
            //Command Storage
            Gamepad giveTheseCommands;

            //This is where the commands are selected.... (usefull for reccording playback)
            if(this.useGamepad1){
                giveTheseCommands = gamepad1;
            }else{
                giveTheseCommands = gamepad2;
            }

            //Modify the Toggle Buttons!!!!
            giveTheseCommands.y = this.y.toggled(giveTheseCommands.y);// WARNING THAT PROPERTY MAY BE IMUTABLE!!!!! also playback should override this...

            //Return the commands
            return giveTheseCommands;

        }

        //== handle the intermediary reccording Logic
        public void handleReccording(Gamepad inputCommands){
            // Do the settings allow reccording
            if(this.enableRecording){
                //=== Permit a new reccording to begin
                if(inputCommands.start){
                    if(!this.isReccording){
                        this.observer.start();
                        this.isReccording = true;
                    }else{
                        // end and save the reccording
                        this.observer.endAndSave();
                        this.isReccording = false;
                    }

                }

                //=== Observe:
                if(this.isReccording){
                    this.observer.observe(inputCommands);
                }
            }
        }

    //=========== Run the Op Mode ===========
    public void runOpMode() throws InterruptedException{

        //======== Run the custom initilizations! ========
        this.customInit();
        //======== Wait for Start ========
        waitForStart();

        //======== Run the Loop ========
        while(opModeIsActive()){

            //=== get the current commands: abstractify controls instead of redefining...
            Gamepad currentCommands = this.getCommands();

            //=== Manages start/stop reccording of the commands and their saving etc...
            this.handleReccording(currentCommands);

            //=== Use Precision Modifier:
            if(currentCommands.y){
                this.isPrecisionSpeed = !this.isPrecisionSpeed; // toggle precision speed by 'clicking' y
            }

            //=== Rotation Movement:
            /*
                left_bumper = CounterClockwise, right_bumper = Clockwise
            */
            if(this.eXOR(currentCommands.left_bumper,currentCommands.right_bumper)){
                if(currentCommands.right_bumper){// rotate Clockwise
                    double[] clockActivations = {1.0,1.0,-1.0,-1.0};
                    this.activateMotors(clockActivations,this.isPrecisionSpeed);
                }else{ // rotate CounterClockwise
                    //if right is false than right must be true to meet the initial condition
                    double[] cntrClockActivations = {-1.0,-1.0,1.0,1.0};
                    this.activateMotors(cntrClockActivations,this.isPrecisionSpeed);
                }
            }

            //=== Controls
                //Redefine LEFT Stick Values (invert if settings say so):
                double stick_x = this.invertControlsXY[0] ? -currentCommands.left_stick_x : currentCommands.left_stick_x;
                double stick_y = this.invertControlsXY[1] ? -currentCommands.left_stick_y : currentCommands.left_stick_y;

            //=== Planar Movement XY
                //=== Natural Inversion Config :
                double[] horozontalActivations = {-stick_x,stick_x,stick_x,-stick_x};
                double[] verticalActivations = {-stick_y,-stick_y,-stick_y,-stick_y};

                //== Movement Forewards & Reverse (vertical):
                if(this.isAboveThreshold(stick_y) && !this.isAboveThreshold(stick_x)){
                    this.activateMotors(verticalActivations,this.isPrecisionSpeed);
                }
                //== Movement Strafe (horozontal):
                if(this.isAboveThreshold(stick_x) && !this.isAboveThreshold(stick_y)){
                    this.activateMotors(horozontalActivations,this.isPrecisionSpeed);
                }
                //== Note: Do NOT create a stayStill function using 0 as all activation values: jittery motors...

            idle();
        }
    }
}
