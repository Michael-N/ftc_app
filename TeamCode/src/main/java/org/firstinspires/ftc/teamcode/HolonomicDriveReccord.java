package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import static java.lang.Math.abs;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * Created by Mike on 9/23/2017.
 */
@TeleOp(name = "HolonomicDriveReccord")
public class HolonomicDriveReccord extends LinearOpMode {

    //=========== Settings and Config ==============
    public String[] motorNames = {"motorLeftFront","motorLeftRear","motorRightFront","motorRightRear"};
    public int[] MotorMappings = {0,1,2,3};
    public boolean[] MotorReverse = {true,true,false,false}; //applied after mappings--> corresponds to index of mapped motor
    public double PrecisionSpeed = 0.1;// 0.5 means half speed... tap & release <y> to toggle precision speed
    public double RegularSpeed = 1.0;
    public boolean[] InvertControlsXY = {false,false};
    public double stickThreshold = 0.15;
    public int toggleDelay = 7;//The toggle delay # of 20ms loops before accepting next toggle update
    public boolean useGamepad1 = true; //The controller that clicks <start> <a> is gamepad 1 else use gamepad2
    public boolean enableRecording = false;//Allow the user to reccord the gamepad inputs by toggling <start>
    public boolean enablePlayback = false;//Allow the user to play the previously recorded inputs by pressing <guide>

    //=========== Initilizations ================
        //Store the state of Precision Status:
        public boolean isPrecisionSpeed = false;
        //===Initilize motors
        public DcMotor[] AllMotors = new DcMotor[4];
        //=== Mapped Motor Storage
        public DcMotor[] MappedMotors = new DcMotor[4];
        //=== the current status of the delay
        public int toggleDelayState = 0;
        //=== Custom Initilization method
        public void customInit(){
            //=== Initial Motor fetch
            for(int k=0;k<4;k++){
                this.AllMotors[k]= hardwareMap.dcMotor.get(motorNames[k]);
            }

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
        //=== the state of the reccoding
        public boolean isReccording = false;
        //=== Reccording Management
        public reccordingManager observer = new reccordingManager();

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

        //== dynamic controle getter:
        public Gamepad getCommands(){
            if(this.useGamepad1){
                return gamepad1;
            }else{
                return gamepad2;
            }

        }

        //== Allow a toggle update
        public boolean toggleUpdatePermitted(){
            //20 ms is not enough time for a human to release the button...
            //this sets a delay between percieved toggle updates...
            if(this.getCommands().y && ((this.toggleDelayState % this.toggleDelay) == 0)){
                return true;
            }else if (this.getCommands().y){
                this.toggleDelayState += 1;
                return false;
            }else{
                return false;
            }
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

        //Run the custom initilizations!
        this.customInit();

        //=== Wait for Start
        waitForStart();

        //=== Run the Loop
        while(opModeIsActive()){

            //=== get the current commands
            Gamepad currentCommands = this.getCommands();

            //=== Manages start/stop reccording of the commands and their saving etc...
            this.handleReccording(currentCommands);

            //=== Use Precision Modifier:
            if(currentCommands.y && this.toggleUpdatePermitted()){
                this.isPrecisionSpeed = !this.isPrecisionSpeed; // toggle precision speed by 'clicking' y
            }

            //=== Rotation Movement: left_bumper = CounterClockwise, right_bumper = Clockwise (Makes more sense than the reverse...)
            if(currentCommands.left_bumper || currentCommands.right_bumper){
                if(currentCommands.right_bumper){// rotate Clockwise
                    double[] clockActivations = {1.0,1.0,-1.0,-1.0};
                    this.activateMotors(clockActivations,this.isPrecisionSpeed);
                }else{ //if right is false than right must be true to meet the initial condition
                    double[] cntrClockActivations = {-1.0,-1.0,1.0,1.0};
                    this.activateMotors(cntrClockActivations,this.isPrecisionSpeed);
                }
            }

            //Redefine LEFT Stick Values (invert if settings say so):
            double stick_x = this.InvertControlsXY[0] ? -currentCommands.left_stick_x : currentCommands.left_stick_x;
            double stick_y = this.InvertControlsXY[1] ? -currentCommands.left_stick_y : currentCommands.left_stick_y;

            //=== Planar Movement XY
                //=== Natural Inversion Config :
                double[] horozontalActivations = {-stick_x,stick_x,stick_x,-stick_x};
                double[] verticalActivations = {-stick_y,-stick_y,-stick_y,-stick_y};
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
                if((!this.isAboveThreshold(stick_y) && !this.isAboveThreshold(stick_x)) || (!currentCommands.left_bumper || !currentCommands.right_bumper)){
                    // NEEDS WORK THIS IS THE CAUSE OF THE  slow motor speeds!!! CONSIDER REMOVING THE ENTIRE IF STATEMENT
                    this.activateMotors(stayStill,this.isPrecisionSpeed);
                }

            idle();
        }
    }
}
