package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import static java.lang.Math.*;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by Mike on 9/23/2017.
 *
 * note to the user: documentation/help is provided as comments in the functions, methods, or sections
 */
@TeleOp(name = "HolonomicDriveRecord")
public class HolonomicDriveRecord extends LinearOpMode {
    //=========== Controls Guide ==================
    /*
    *   Rotation: <Right-Bumper>= Clockwise
    *             <Left-Bumper>= CounterClockwise
    *
    *   Precision: <y> = toggle precision/regular speed
    *
    *   Strafing (Horizontal):  <left-stick-x> = mapped right to right and left to left unless controls setting invertControlsXY[0] is true
    *
    *   Forwards/Backwards (Vertical): <left-stick-y> mapped up to forwards and down to backwards unless setting invertControlsXY[1] is true
    *
    *   Diagonal: <left-stick-x><left-stick-y>
    *
    *   Claw: open/close QUICKLY tap <x> to toggle open/closed
    *
    *   LinearSlide: <dpadUp> to move the slide upwards and <dpadDown> downwards
    *
    *   ~~~Planed Features~~~
    *   -add in encoder for motors 2,4 (back two motors) + option to toggle between using them and not
    *   -work on auton mode...
    *   -reccording
    *   -playback
    *
    * */

    /*
    * Note: add in diagonal threshold
    *       add in customizable speeds for turning
    *       add in variable speed
    *       add in rotation smoothing
    *       KEEP the acceleration curve function
    *       add in diagonal smoothing...
    * */
    //=========== Settings and Config ==============
        //=== Motors for Wheels
        public String[] motorNames = {"motorLeftFront","motorLeftRear","motorRightFront","motorRightRear"};
        public int[] motorMappings = {0,1,2,3};
        public boolean[] motorReverse = {true,true,false,false}; //applied after mappings--> corresponds to index of mapped motor

        //=== Motor for Linear slide:
        public String linearSlideMotorName = "linearSlide";
        public boolean linearSlideReverse = false;
        //public boolean useEncoders = false;

        //=== Servos
        public String[] servoNames = {"servoRight","servoLeft"}; // Use: same as Motors Section above
        public int[] servoMappings = {0,1};
        public boolean[] servoReverse = {false,true};
        public boolean clawInitialStateOpen = true;//sets whether the claw is positioned open: both servos should be at 0 deg  -true- or closed -false- on startup

        //=== Speed
        public double precisionSpeed = 0.5;//  % of max speed as fraction:  tap & release <y> to toggle precision speed
        public double regularSpeed = 0.8;// % of max speed as a fraction
        public double turningSpeed = 0.;// is a fraction of the CURRENT speed applied to the motors whether regular or precision
        public double linearSlideSpeed = 0.5;// independent of all other speeds and precision modes

        //=== Controls
        public boolean[] invertControlsXY = {false,false};
        public String[] makeToggleable = {"x","y"};// not significant since the toggle is  done bt index not index value.. mearly asthetic...
        public int[] toggleDelays = {1600,900};// toggle delays for claw and precisionSpeed of the robot
        public double stickThreshold = 0.18;//threshold for vertical and horizontal movement
        public double diagonalThreshold = 0.24;// threshold for diagonal movement
        public boolean useGamepad1 = true; //The controller that clicks <start> <a> is gamepad 1 else use gamepad 2 <start> <b>

        //=== Reccording
        public boolean enableRecording = false;//Allow the user to record the gamepad inputs by toggling <start>
        public int maxTimeReccording  = 600;// Time in Sec

        //=== Playback
        public boolean enablePlayback = false;//Allow the user to play the previously recorded inputs by pressing <guide>
        public boolean useMostRecentPlayback = true; // IF this is false then the code will look at the useThisFileForPlayback string
        public String useThisFileForPlayback  = "";
        public boolean allowPlaybackInterupt = false;

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
                if(gamepadButton && (this.delayMs < (testTime-this.lastAllowedToggle))){
                    //reset the time return true
                    this.lastAllowedToggle = System.currentTimeMillis();
                    return true;
                }else{
                    //do nothing
                    return false;
                }

            }
        }

    //=========== Initializations ================
        //=== Motors
        public DcMotor[] allMotors = new DcMotor[motorNames.length];//initial motor storage
        public DcMotor[] mappedMotors = new DcMotor[motorNames.length];//mapped motor storage
        public DcMotor linearSlideMotor;
        //=== Speed State
        public boolean isPrecisionSpeed = false;//Store the state of Precision Status:
        //=== Controls
        public Toggleable[] allToggleables = new Toggleable[makeToggleable.length];
        //=== Recording State
        public boolean isRecording = false;
        public recordingManager observer = new recordingManager();
        //=== Servos
        public Servo[] allServos = new Servo[servoNames.length];//initial servo storage
        public Servo[] mappedServos = new Servo[servoNames.length];//mapped servo storage
        public boolean clawIsOpen;

        //=== Custom Init Method
        public void customInit(){
            //===== Toggleables
            for(int e=0; e<makeToggleable.length;e++){
                this.allToggleables[e] = new Toggleable(this.toggleDelays[e]);
            }
            //===== Motors
                //=== Initial Motor fetch
                for(int k=0;k<motorNames.length;k++){
                    this.allMotors[k]= hardwareMap.dcMotor.get(motorNames[k]);
                }
                //=== Initialize mappings:
                for(int j=0;j<motorNames.length;j++){
                    this.mappedMotors[j] = this.allMotors[this.motorMappings[j]];
                }
                //=== Initialize reverses
                for( int i=0; i<motorNames.length; i++){
                    //Reverses:
                    if(this.motorReverse[i]){
                        this.mappedMotors[i].setDirection(DcMotor.Direction.REVERSE);
                    }

                }
            //=== Claw State
                this.clawIsOpen = clawInitialStateOpen;
            //===== Servos
                //=== Initial Servo fetch
                for(int l=0;l<servoNames.length;l++){
                    this.allServos[l] = hardwareMap.servo.get(servoNames[l]);
                }
                //=== Initialize mappings:
                for(int p=0;p<servoNames.length;p++){
                    this.mappedServos[p] = this.allServos[this.servoMappings[p]];
                }
                //=== Initialize Reverses
                for(int r=0;r<servoNames.length;r++){
                    //Reverses:
                    if(this.servoReverse[r]){
                        this.mappedServos[r].setDirection(Servo.Direction.REVERSE);
                    }
                }
            //=== Linear Slide
                this.linearSlideMotor = hardwareMap.dcMotor.get(this.linearSlideMotorName);
                if(this.linearSlideReverse){
                    this.linearSlideMotor.setDirection(DcMotor.Direction.REVERSE);
                }
            //======== Wait for Start ========
            this.waitForStart();

        }

    //===========  Helper Methods ===========
        //===== Misc. methods
        //== Handle acceleration of input: how it is mapped across [-1,1]
        public double accelerationCurve(double x){
            double decimalPlacesToRoundTo = 2.0;
            /*
            *   y_ = -ln(1.36788 -x)  for x in [0,1]  graph = concave /
            *
            *   input to this function will be [-1,1]
            * == 3 Candidates ==
            *
            * double y_ = -log(1.36788 -abs(x));
            *
            *
            * or an alternate in radians
            *
            *
            * double y_ =0.5*sin(PI*(abs(x)-0.5)) +0.5;
            *
            * or an alternate
            *
            * double y_ = tanh(1.0926*(abs(x)-0.5)) + 0.5;
            * double y_ = 0.5*tanh(k*(abs(x)-0.5)) + 0.5;  // my favorite
            *
            * */
        //== The base equation: be sure to be careful with domain and range d: [-1,1] range: [0,1]
        double y_ = 0.5*tanh(5*(abs(x)-0.5)) + 0.5;
        //== This extends the range to be [-1,1] uniformly for the behavior in the 1st quadrant
        double vectorized = (x/abs(x)) * y_;

        //rounding makes discritizizes the numbers enabling the tanh equation to actually reach y=1 for x=1 not y=0.993
        return round(vectorized*pow(10,decimalPlacesToRoundTo))/pow(10,decimalPlacesToRoundTo);
    }
        //== Check if input is above threshold defined by the class settings
        public boolean isAboveThreshold(double inputValue,double thresholdValue){

        //Returns true/false
        return abs(inputValue) > thresholdValue;
    }

        //===== Hardware methods
        //== Activates Motors with activationValues parameter and a precision parameter
        public void activateMotors(double[] activationValues){
            /*
            * For settings configured as the following:
            *
            * public String[] motorNames = {"motorLeftFront","motorLeftRear","motorRightFront","motorRightRear"};
            * public int[] motorMappings = {0,1,2,3};
            *
            *
            * Robot Diagram is the index activation corresponds to:
            *         top
            *       1\\  //3
            *               Right
            *       2//  \\4
            * */
            for(int k=0;k<activationValues.length;k++){
                if(this.isPrecisionSpeed){
                    //multiply by setting
                    this.mappedMotors[k].setPower(this.accelerationCurve(this.precisionSpeed*activationValues[k]));
                }else{
                    //multiply by setting
                    this.mappedMotors[k].setPower(this.accelerationCurve(this.regularSpeed*activationValues[k]));
                }

            }
        }
        //==Activate linear Slide Motor
        public void  activateSlide(double activationValue){
            this.linearSlideMotor.setPower(this.linearSlideSpeed*activationValue);
        }
        //== Activates Servos with POSITION values as a list:
        public void activateServos(double[] activationValues){
            for(int k=0;k<activationValues.length;k++){
                double activationScaledFromDeg = activationValues[k]/180;
                this.mappedServos[k].setPosition(activationScaledFromDeg);
            }
        }

        //===== Logical methods
        //== logical or in the strictest sense (the values must be opposites)
        public boolean eXOR(boolean x, boolean y) {// Courtesy of ~stack overflow~
            //works like or except if both  are true then it is false also
            return ( ( x || y ) && ! ( x && y ) );
        }
        //== Returns true if all the items in the list are false
        public boolean allFalse(boolean[] vals){
            for(int i=0; i<vals.length; i++){
                if(vals[i]){
                    return false;
                }
            }
            return true;
        }

        //===== Playback and Recording methods
        //== handle the intermediary recording Logic
        public void handleRecording(Gamepad inputCommands){
        // Do the settings allow recording
        if(this.enableRecording){
            //=== Permit a new recording to begin
            if(inputCommands.start){
                if(!this.isRecording){
                    this.observer.start();
                    this.isRecording = true;
                }else{
                    // end and save the recording
                    this.observer.endAndSave();
                    this.isRecording = false;
                }

            }

            //=== Observe:
            if(this.isRecording){
                this.observer.observe(inputCommands);
            }
        }
    }
        //== handle getting the commands
        //public Object handlePlayback(){}

        //===== Gamepad Logic Method
        //== dynamic control getter:
        public Gamepad getCommands(){
            //Command Storage
            Gamepad giveTheseCommands;

            //This is where the commands are selected.... (useful for recording playback)
            if(this.useGamepad1){// Note this code could be modified to pick and choose and return even more commands...
                giveTheseCommands = gamepad1;
            }else{
                giveTheseCommands = gamepad2;
            }

            //Modify the Toggle Buttons!!!!
            giveTheseCommands.y = this.allToggleables[1].toggled(giveTheseCommands.y);// playback should override this...
            giveTheseCommands.x = this.allToggleables[0].toggled(giveTheseCommands.x);

            //=== Record Commands: press <????> to toggle reccording...
            this.handleRecording(giveTheseCommands);

            //=== Use Precision Modifier:
            if(giveTheseCommands.y){
                this.isPrecisionSpeed = !this.isPrecisionSpeed; // toggle precision speed by 'clicking' y
            }

            //Return the commands
            return giveTheseCommands;

        }



    //=========== Run the Op Mode ===========
    public void runOpMode() throws InterruptedException{

        //======== Run the custom initializations! ========
        this.customInit();

        //======== Run the Loop ========
        while(opModeIsActive()){

            //=== Controls and Recording
            Gamepad currentCommands = this.getCommands();// get the current commands: abstractify controls instead of redefining...

            //=== Activation computations (NOTE! README: these are redefined for each iteration of the loop!!!!!! )
                //=== Controls: Redefine LEFT Stick Values (invert if settings say so):
                    double stick_x = this.invertControlsXY[0] ? -currentCommands.left_stick_x : currentCommands.left_stick_x;
                    double stick_y = this.invertControlsXY[1] ? -currentCommands.left_stick_y : currentCommands.left_stick_y;
                //=== Diagonal Movement Normalized:
                    /* ^above

                    motor only accepts values [-1,1] and the diagonal movement must be responsive to both x & y rather the
                    length of the vector <x,y>: denoted u a vector, x,y are scalars and all for which  |u|,|x|,|y| <= 1 so
                    (ax)^2 + (ay)^2 <= |u|^2 , the y direction is still important so the final thing we want is
                    |u|* (y/|y|) which gives us the y axis direction... solving a<= sqrt(0.5) approx: 0.7071

                          U   /|
                            /  |   y   if x,y = 1 |U| = sqrt(2) > 1 ---> err...
                            ---
                            x
                    */
                    double normalizedU = sqrt(pow(0.7071*stick_x,2.0) + pow(0.7071*stick_y,2.0)) * (stick_y/abs(stick_y));

            //=== Activation Values Motors
                double[] clockActivations = {turningSpeed,turningSpeed,-turningSpeed,-turningSpeed};
                double[] cntrClockActivations = {-turningSpeed,-turningSpeed,turningSpeed,turningSpeed};
                double[] horizontalActivations = {stick_x,-stick_x,-stick_x,stick_x};
                double[] verticalActivations = {-stick_y,-stick_y,-stick_y,-stick_y};
                double[] diagonalTopRightBackLeft = {0,-normalizedU,-normalizedU,0};// spin perpendicular diagonal wheels
                double[] diagonalTopLeftBackRight = {-normalizedU,0,0,-normalizedU};
                double[] stopActivations = {0,0,0,0};

            //=== Activation Positions Servos
                double[] clawOpenActivations = {105,105};// Max = 180deg min = 0 deg
                double[] clawClosedActivations= {0,0};

            //=== Command Conditions Motors:
                boolean doTurn = this.eXOR(currentCommands.left_bumper,currentCommands.right_bumper);//intermediary abstraction
                boolean doClockwise = doTurn && currentCommands.right_bumper;// Clockwise
                boolean doCounterClockwise = doTurn && currentCommands.left_bumper;//CounterClockwise
                boolean doForRev= this.isAboveThreshold(stick_y,stickThreshold) && !this.isAboveThreshold(stick_x,this.stickThreshold);// Forwards and Reverse
                boolean doHorz = this.isAboveThreshold(stick_x,this.stickThreshold) && !this.isAboveThreshold(stick_y,this.stickThreshold);// Horizontal
                boolean doDiagonal= this.isAboveThreshold(stick_x,this.diagonalThreshold) && this.isAboveThreshold(stick_y,this.diagonalThreshold);// Intermediary helper abstraction
                boolean diagonalOne = doDiagonal && (0<(stick_x * stick_y));//  / diagonal movement
                boolean diagonalTwo = doDiagonal && (0>(stick_x*stick_y));//    \ diagonal movement
                boolean[] willRunSequenceForOtherCommands = {doForRev,doHorz,doTurn,diagonalOne,diagonalTwo};// Stop

            //=== Command Conditions Servos:
                boolean doClawOpen = currentCommands.x && !this.clawIsOpen;// open if toggled and the claw is not open
                boolean doClawClose = currentCommands.x && this.clawIsOpen;

            //=== Command Conditions LinearSlide
                boolean doSlideUp = currentCommands.dpad_down;
                boolean doSlideDown = currentCommands.dpad_up;
                boolean doSlideStop = !doSlideDown && !doSlideUp;

            //===== Directional Movement
                //=== Rotation Movement: left_bumper = CounterClockwise, right_bumper = Clockwise
                    if(doClockwise){// rotate Clockwise
                        this.activateMotors(clockActivations);
                    }
                    if(doCounterClockwise){// rotate CounterClockwise
                        this.activateMotors(cntrClockActivations);
                    }

                //=== Planar Movement XY
                    if(doForRev){//== Movement Forwards & Reverse (vertical):
                        this.activateMotors(verticalActivations);
                    }
                    if(doHorz){//== Movement Strafe (horizontal):
                        this.activateMotors(horizontalActivations);
                    }

                //=== Planar Movement Diagonal
                    if(diagonalOne){//== / diagonal movement
                        this.activateMotors(diagonalTopRightBackLeft);
                    }
                    if(diagonalTwo){//== \ diagonal movement
                        this.activateMotors(diagonalTopLeftBackRight);
                    }

                //=== Stop Movement
                    if(this.allFalse(willRunSequenceForOtherCommands)){//== If all the other commands are false then therfore stop!
                        this.activateMotors(stopActivations);
                    }

            //===== Claw Movement
                if(doClawOpen){//=== Claw Open
                    this.activateServos(clawOpenActivations);
                    this.clawIsOpen = !this.clawIsOpen;
                }
                if(doClawClose){//=== Claw Close
                    this.activateServos(clawClosedActivations);
                    this.clawIsOpen = !this.clawIsOpen;// is used in the loop...
                }

            //===== LinearSlide Movement
                if(doSlideDown){//move slide down
                    this.activateSlide(-1.0);
                }
                if(doSlideUp){//move slide up
                    this.activateSlide(1.0);
                }
                if(doSlideStop){// hold position
                    this.activateSlide(0.0);
                }
            idle();
        }
    }
}
