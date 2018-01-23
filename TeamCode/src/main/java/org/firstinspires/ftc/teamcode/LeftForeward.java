package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.Math.tanh;


/**
 * Created by Mike on 9/23/2017.
 *
 * note to the user: documentation/help is provided as comments in the functions, methods, or sections
 */
//below line changes to @Autonomous(name="AutonHolonomicDrive", group="Scenario Blue 1")
@Autonomous(name = "LeftForeward",group="Final")
public class LeftForeward extends LinearOpMode {
    //=========== Controls Guide ==================
    /*
    *   Rotation: <Right-Trigger> Clockwise, <Left-Trigger> CounterClockwise
    *
    *   Precision: <y>  toggle precision/regular speed
    *
    *   ChangePrecisionSpeed: <dpad-right> increment, <dpad-left> decrement
    *
    *   Strafing (Horizontal):  <left-stick-x> = mapped right to right and left to left unless controls setting invertControlsXY[0] is true
    *
    *   Forwards/Backwards (Vertical): <left-stick-y> mapped up to forwards and down to backwards unless setting invertControlsXY[1] is true
    *
    *   Diagonal: <left-stick-x> <left-stick-y>
    *
    *   Claw: open/close   <x> to close  <a> to open
    *
    *   LinearSlide: <dpadUp> to move the slide upwards and <dpadDown> downwards
    *
    *   HSlide: <left_bumper> Retract , <right_bumper> Extend ... if setting  hSlidePositionBased is true then the bumper fully extends/retracts not incrementally as button is pressed
    *
    *   Recording: Gamepad 2  <a> Start Recording, <b> End Recording                                                   (USE GAMEPAD 2 WHILE GAMEPAD 1 IS MAIN INPUT!)
    *   Playback:  Gamepad 2  Use <x> Start Playback, <y> End Playback    when startPlaybackWhenInit is false          (USE GAMEPAD 2 WHILE GAMEPAD 1 IS MAIN INPUT!)
    * */

    /*
    *   ============ Brief Outline of Code Logic ============
    *       Settings
    *           category (ex. a motor or servo or claw)
    *       Helper Classes
    *           category
    *       Initializations
    *           category
    *       Helper Methods
    *           category
    *       Feature Definitions
    *       Run Op Mode
    *           Initialization Call
    *           Loop
    *               Get Gamepad State
    *               Feature Calls
    *                   category
    * */
    //=========================== Settings ============================
        //=== AUTON MODE
            //YES this fundamentally affects all the code! do not mess with this.... if the above says teleop then this is false if it says auton then this can be true...
            public boolean ISAUTONMODE = true;

        //=== Motors for Wheels
            public String[] motorNames = {"motorLeftFront","motorLeftRear","motorRightFront","motorRightRear"};// the name of the motor as specified in the config file on the Robot Controller Phone
            public int[] motorMappings = {0,1,2,3};// permits switching how the program assigns indicies to the motors specified above
            public boolean[] motorReverse = {true,true,false,false}; //applied after mappings; corresponds to index of mapped motor; reverses initial spin direction
            public int[] hasEncoderSensor = {0,3}; // indexes of the motors which have encoders... index by 0 (after mappings)

        //=== Motor for Linear slide:
            public String linearSlideMotorName = "linearSlide";// the name of the motor as specified in the config file on the Robot Controller Phone
            public boolean linearSlideReverse = false;// Reverses the initial spin direction of the motor

        //=== Motor for H Slide
            public String hSlideMotorName = "hSlide";// THIS MOTOR MUST HAVE ENCODER!!!!
            public boolean hSlideReverse  = false;// Reverses the initial spin direction of the motor
            public boolean hSlidePositionBased = true; // Use position for extend instead of hold button tap button to run to the position
            public int hSlideMax = 5300;//Max Position for encoder value ~~ 1400 is 1 turn... see docs for exact details
            public int hSlideMin = 0;//Min Position for encoder value

        //=== Servos
            public String[] servoNames = {"servoRight","servoLeft"}; // the name of the servo as specified in the config file on the Robot Controller Phone
            public int[] servoMappings = {0,1};// permits switching how the program assigns indicies to the servos specified above
            public boolean[] servoReverse = {true,false};// Reverses the initial spin direction of the servos
            public boolean clawInitialStateOpen = true;//sets whether the claw is positioned open: both servos should be at 0 deg  -true- or closed -false- on startup

        //=== Speed
            public double precisionSpeed = 0.5;//  % of max speed as fraction:  tap & release <y> to toggle precision speed
            public double changePrecisionSpeedStep = 0.01;// The step size for speed changing
            public double regularSpeed = 0.5;// the % percentage  of max speed as a fraction
            public double linearSlideSpeed = 0.5;// independent of all other speeds and precision modes absolute speed value
            public double hSlideSpeed = 0.5;// as the dividing factor of the activation value... similar to linear slide speed

        //=== Controls
            public boolean[] invertControlsXY = {false,false};// invert x and y respectivly
            public String[] makeToggleable = {"x","y"};// not significant since the toggle is  done bt index not index value.. merly asthetic...
            public int[] toggleDelays = {1600,900};// toggle delays for claw and precisionSpeed of the robot
            public double stickThreshold = 0.18;//threshold for vertical and horizontal movement
            public double diagonalThreshold = 0.24;// threshold for diagonal movement
            public double triggerThreshold = 0.15;// only accepts values with a absolute value greater than this
            public boolean useGamepad1 = true; //The controller that clicks <start> <a> is gamepad 1 else use gamepad 2 <start> <b>
            public boolean useOneController = true;// THIS WILL DISABLE PLAYBACK!!!!! AND RECORDING THOSE REQUIRE a seccond controller
        //=== Reccording
            public boolean enableRecording = true;// Permit Recording... if useOneController is true then playback and recording is disabled... they require a second controller
            public String saveDirectory = ""; //Allow the user to record the gamepad inputs by pressing <start> and stop by <back>

        //=== Playback
            public boolean startPlaybackWhenInit = false;// Begin a playback specified below when robot is activated...  if useOneController is true then playback and recording is disabled... they require a second controller
            public String useThisFilePathForPlayback  = "";// The reccording file to be used for playback

    //========================= Helper Classes ================================
        //=== A toggle Class for True/False buttons
            public class Toggleable {
            /*    use:
            Toggleable yourButtonWithToggle = new Toggabable(Int DelayTimeInMs);
            yourButtonWithToggle.toggled(boolean yourButton); // returns true if a change is permitted else false
            */
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

    //========================= Initializations ===============================
        //=== Motors
            public DcMotor[] allMotors = new DcMotor[motorNames.length];//initial motor storage
            public DcMotor[] mappedMotors = new DcMotor[motorNames.length];//mapped motor storage
            public DcMotor linearSlideMotor;
            public DcMotor hSlideMotor;
        //=== Speed State
            public boolean isPrecisionSpeed = false;//Store the state of Precision Status:
            public double initialPrecisionSpeed = precisionSpeed;
        //=== Controls
            public Toggleable[] allToggleables = new Toggleable[makeToggleable.length];
        //=== Recording State
            public boolean isRecording = false;
            public boolean isPlaybacking = false;
            public CommandObserver observer = new CommandObserver();
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
                    //=== Initialize reverses and change for auton mode
                    for( int i=0; i<motorNames.length; i++){
                        //Reverses:
                        if(this.motorReverse[i]){
                            this.mappedMotors[i].setDirection(DcMotor.Direction.REVERSE);
                        }

                        //Auton
                        if(this.ISAUTONMODE){
                            this.mappedMotors[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            this.mappedMotors[i].setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
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
                    this.linearSlideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                    if(this.linearSlideReverse){
                        //Reverse Direction
                        this.linearSlideMotor.setDirection(DcMotor.Direction.REVERSE);
                    }

                //=== H Slide
                    this.hSlideMotor = hardwareMap.dcMotor.get(this.hSlideMotorName);
                    if(this.hSlideReverse){
                        //Reverse Direction
                        this.hSlideMotor.setDirection(DcMotor.Direction.REVERSE);
                    }

                    //ACCEPT encoder values!!
                    if(hSlidePositionBased){
                        this.hSlideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        this.hSlideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    }else{
                        this.hSlideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        this.hSlideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);// THIS IS COUNTERINTUATIVE but run without encoder means it modifies how the lib works... you can still observe the encoder...
                    }

                    //this.linearSlideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

                //======== Wait for Start ========
                this.waitForStart();

            }

    //=====================  Helper Methods =================================
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
                    * double y_ =0.5*sin(PI*(abs(x)-0.5)) +0.5;// G(x) second favorite
                    *
                    * or an alternate
                    *
                    * double y_ = tanh(1.0926*(abs(x)-0.5)) + 0.5;
                    * double y_ = 0.5*tanh(k*(abs(x)-0.5)) + 0.5;  // K(x) my favorite
                    *
                    *  interactive graph located at: https://www.desmos.com/calculator/w5vdhzlmlo
                    *
                    *  this graph shows that for the d/dx K(x) > d/dx G(x) which means more acceleration
                    *  However if K(x) is tuned the overall max acceleration can be further minimized...
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
            //== check if an input is withine a certian range both min inclusive max inclusive
                public boolean isInRange(int input,int min,int max){
                    return (input<=max) && (input>=min);
                }

        //===== Hardware methods Teleop
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
                    //Power Value
                    double powerVal = this.linearSlideSpeed*activationValue;
                    this.linearSlideMotor.setPower(powerVal);
                }
            //==Activate H slide
                public void activateHSlide(double activationValue,int in,int position,boolean auton){
                    /*
                    *   Activation Value is Speed
                    *    int in   .... when int in is positive 1 then move slide in else -1 then extend slide
                    *
                    *
                    * */
                    if(this.hSlidePositionBased){
                        this.hSlideMotor.setTargetPosition(position);
                        double powerVal = in*this.hSlideSpeed*abs(activationValue);
                        if(auton){
                            while((abs(hSlideMotor.getCurrentPosition()) < position) && (position!=0)){
                                this.hSlideMotor.setPower(powerVal);
                            }
                        }else{
                            this.hSlideMotor.setPower(powerVal);
                        }


                    }else{
                        double powerVal = in*this.hSlideSpeed*abs(activationValue);
                        this.hSlideMotor.setPower(powerVal);
                    }
                }
                public void hSlideExtend(){
                    this.activateHSlide(1,1,hSlideMax,ISAUTONMODE);
                }
                public void hSlideRetract(){
                    this.activateHSlide(1,-1,hSlideMin,ISAUTONMODE);
                }
                public void hSlideStayStill(){
                    if(!this.hSlidePositionBased){
                        this.activateHSlide(0,0,0,ISAUTONMODE);// This makes sure if it is not using position the motor is stopped... the position 0 is ignored
                    }

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
        //===== Hardware Methods Autonomous
                public void autonPlanarMovement(int x, int y, int magnitude,Telemetry telem){
                    // x and y must both be on [-1,1]
                    double stick_x = x;
                    double stick_y = y;

                    double[] horizontalActivations = {stick_x,-stick_x,-stick_x,stick_x};
                    double[] verticalActivations = {-stick_y,-stick_y,-stick_y,-stick_y};
                    double[] stopActivations = {0,0,0,0};

                    DcMotor motorLeft = this.allMotors[this.hasEncoderSensor[0]];//front motor
                    DcMotor motorRight = this.allMotors[this.hasEncoderSensor[1]];


                    motorLeft.setTargetPosition(magnitude);// left rear
                    motorRight.setTargetPosition(magnitude);//right rear

                    if(x!=0){
                        this.activateMotors(horizontalActivations);
                    }
                    else if (y!=0){
                        this.activateMotors(verticalActivations);
                    }

                    while(opModeIsActive() && (abs(motorLeft.getCurrentPosition())<magnitude)){// use for 2 encoders...(motorLeft.isBusy() || motorRight.isBusy())
                        DcMotor lf = this.allMotors[0];
                        DcMotor rf = this.allMotors[2];
                        DcMotor lb = this.allMotors[1];
                        DcMotor rb = this.allMotors[3];
                        telem.addData("[Encoder Vals]","test");
                        telem.addData("[left front]",lf.getCurrentPosition());
                        telem.addData("[right front]",rf.getCurrentPosition());
                        telem.addData("[left back]",lb.getCurrentPosition());
                        telem.addData("[right back]",rb.getCurrentPosition());
                        /*
                        telem.addData("Left",motorLeft.getCurrentPosition());
                        telem.addData("right",motorRight.getCurrentPosition());
                        */
                        telem.update();
                    }

                    this.activateMotors(stopActivations);

                }
        //===== Playback and Recording methods
            //== handle the intermediary recording Logic
                public void handleRecording(Gamepad inputCommands,Gamepad recordingManager){
                // Do the settings allow recording
                if(this.enableRecording){

                    //=== Permit a new recording to begin
                    if(recordingManager.a){
                        this.isRecording = true;
                        telemetry.addData("IS RECCORDING:","start");
                        telemetry.update();
                    }
                    //=== Observe:
                    if(this.isRecording){
                        telemetry.addData("IS RECCORDING:","observing");
                        telemetry.update();
                        this.observer.record(inputCommands);
                    }
                    //=== Save the reccording if stop command
                    if(recordingManager.b && this.isRecording){

                        //== Unique Timestamp
                        String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());

                        //== Filename concat
                        String filename = "Recording " + timeStamp + ".json";

                        //== FilePATH formatting hack
                        String filepath = new File(saveDirectory,filename).toString();

                        //== Save the file
                        this.observer.save(filepath,this.telemetry);

                        this.isRecording = false;
                        telemetry.addData("IS RECCORDING:","Saved");
                        telemetry.update();
                    }
                }
            }
            //== handle getting the commands
                public Gamepad handlePlayback(Gamepad inputCommands,Gamepad playbackManager){

                    //Initilize the playback
                    if(startPlaybackWhenInit || playbackManager.x){
                        this.observer.open(useThisFilePathForPlayback,telemetry);
                        this.isPlaybacking = true;
                        telemetry.update();
                    }

                    //If playback then playback commands else the input passes through
                    if(playbackManager.y){
                        this.isPlaybacking = false;
                        return inputCommands;
                    }else if(this.isPlaybacking){
                        return this.observer.playback();
                    }else{
                        return inputCommands;
                    }
                }


        //===== Gamepad Logic Method
            //== dynamic control getter:
                public Gamepad getCommands(){
                    //Command Storage
                    Gamepad giveTheseCommands;
                    Gamepad playbackReccordingManager;

                    //This is where the commands are selected.... (useful for recording playback)
                    if(this.useGamepad1){// Note this code could be modified to pick and choose and return even more commands...
                        giveTheseCommands = gamepad1;
                        playbackReccordingManager = gamepad2;
                    }else{
                        giveTheseCommands = gamepad2;
                        playbackReccordingManager = gamepad1;
                    }

                    //Modify the Toggle Buttons!!!!
                    giveTheseCommands.y = this.allToggleables[1].toggled(giveTheseCommands.y);// playback should override this...

                    //use one controller...
                    if(!useOneController){
                        //=== Record Commands: press <start> to start reccording... <back> to stop
                        this.handleRecording(giveTheseCommands,playbackReccordingManager);
                        //If not playback then giveTheseCommands is not modified...
                        giveTheseCommands = this.handlePlayback(giveTheseCommands,playbackReccordingManager);
                    }


                    //Return the commands
                    return giveTheseCommands;

                }

    //======================= Feature Definitions ==========================
        public void DirectionalMovement(Gamepad currentCommands){

            //=== Use Precision Modifier:
            if(currentCommands.y){
                this.isPrecisionSpeed = !this.isPrecisionSpeed; // toggle precision speed by 'clicking' y
            }
            //=== Controls: Redefine LEFT Stick Values (invert if settings say so):
            double stick_x = this.invertControlsXY[0] ? -currentCommands.left_stick_x : currentCommands.left_stick_x;
            double stick_y = this.invertControlsXY[1] ? -currentCommands.left_stick_y : currentCommands.left_stick_y;
            //== Triggers
            double rt = currentCommands.right_trigger;
            double lt=  currentCommands.left_trigger;
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
            //Change precision speed
            if(currentCommands.dpad_right && (this.precisionSpeed <= 1)){//tick speed up
                this.precisionSpeed += changePrecisionSpeedStep;
            }
            if(currentCommands.dpad_left && (this.precisionSpeed >=0)){//tick seed down
                this.precisionSpeed -= changePrecisionSpeedStep;
            }
            if(currentCommands.dpad_right && currentCommands.dpad_left){//reset speed
                this.precisionSpeed = initialPrecisionSpeed;
            }

            //=== Command Conditions Motors:
            boolean cDoTurn = this.isAboveThreshold(rt,triggerThreshold);// clockwise
            boolean crDoTurn = this.isAboveThreshold(lt,triggerThreshold);// counter clockwise
            boolean doTurn = this.eXOR(cDoTurn,crDoTurn);//intermediary : Ensures do nothing if both are pressed
            boolean doClockwise = doTurn && cDoTurn;// Clockwise
            boolean doCounterClockwise = doTurn && crDoTurn;//CounterClockwise
            boolean doForRev= this.isAboveThreshold(stick_y,stickThreshold) && !this.isAboveThreshold(stick_x,this.stickThreshold);// Forwards and Reverse
            boolean doHorz = this.isAboveThreshold(stick_x,this.stickThreshold) && !this.isAboveThreshold(stick_y,this.stickThreshold);// Horizontal
            boolean doDiagonal= this.isAboveThreshold(stick_x,this.diagonalThreshold) && this.isAboveThreshold(stick_y,this.diagonalThreshold);// Intermediary helper abstraction
            boolean diagonalOne = doDiagonal && (0<(stick_x * stick_y));//  / diagonal movement
            boolean diagonalTwo = doDiagonal && (0>(stick_x*stick_y));//    \ diagonal movement
            boolean[] willRunSequenceForOtherCommands = {doForRev,doHorz,doTurn,diagonalOne,diagonalTwo};// Stop

            //=== Activation Values Motors
            double[] clockActivations = {rt,rt,-rt,-rt};
            double[] cntrClockActivations = {-lt,-lt,lt,lt};
            double[] horizontalActivations = {stick_x,-stick_x,-stick_x,stick_x};
            double[] verticalActivations = {-stick_y,-stick_y,-stick_y,-stick_y};
            double[] diagonalTopRightBackLeft = {0,-normalizedU,-normalizedU,0};// spin perpendicular diagonal wheels
            double[] diagonalTopLeftBackRight = {-normalizedU,0,0,-normalizedU};
            double[] stopActivations = {0,0,0,0};

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
            Telemetry telem = this.telemetry;
            DcMotor lf = this.allMotors[0];
            DcMotor rf = this.allMotors[2];
            DcMotor lb = this.allMotors[1];
            DcMotor rb = this.allMotors[3];
            telem.addData("[Encoder Vals]","test");
            telem.addData("[left front]",lf.getCurrentPosition());
            telem.addData("[right front]",rf.getCurrentPosition());
            telem.addData("[left back]",lb.getCurrentPosition());
            telem.addData("[right back]",rb.getCurrentPosition());
            telem.update();
        }
        public void LinearSlide(Gamepad currentCommands){
            //=== Command Conditions LinearSlide
            boolean doSlideUp = currentCommands.dpad_down;
            boolean doSlideDown = currentCommands.dpad_up;
            boolean doSlideStop = !doSlideDown && !doSlideUp;

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
        }
        public void Claw(Gamepad currentCommands){
            //=== Activation Positions Servos
            double[] clawClosedActivations = {96,98};// Max = 180deg min = 0 deg asymmetry of activations due to frame asymmetry
            double[] clawClosedFurtherActivations = {103,105};
            double[] clawOpenActivations= {10,10};

            //doClawOpen
            if(currentCommands.a){//=== Claw Open
                this.activateServos(clawOpenActivations);
                this.clawIsOpen = !this.clawIsOpen;
            }
            //doClawClose
            if(currentCommands.x){//=== Claw Close
                this.activateServos(clawClosedActivations);
                this.clawIsOpen = !this.clawIsOpen;// is used in the loop...
            }
            //doClawClose Further
            if(currentCommands.y){
                this.activateServos(clawClosedFurtherActivations);
                this.clawIsOpen = !this.clawIsOpen;
            }

        }
        public void HSlide(Gamepad currentCommands){
            //this code deserves to be re-written and simplified... this is grotesquely overcomplex...
            /*
            *           Activation Value:  motor speed
            *           in: -1 means retract... 1 means extend
            *           3rd Arg: keep moving till encoder hits this value...
            *
            * */
            // The code works for extend/retract all and incrementally.. if not position based the 3rd arg is ignored... otherwise all 3 used...
            if(currentCommands.left_bumper){// Retract
                this.hSlideRetract();
            }else if (currentCommands.right_bumper){// Extend
                this.hSlideExtend();
            }else if (!hSlidePositionBased){
               this.hSlideStayStill();
            }
        }
        public void AutonScenarioBlueOne(){
            /*
                foreward
                left
                backwards
            *
            *
            * */
            // Magnitude is based off of the encoder value ... absolute distance... not vector

            this.autonPlanarMovement(-1,0,722,telemetry);// Left
            this.hSlideExtend();
            this.autonPlanarMovement(0,1,722,telemetry);// Foreward


            telemetry.addData("Extending h Slide","extended");
            telemetry.update();
            //this.hSlideStayStill() NOT NEEDED BC it is position based
            telemetry.addData("End of Auton","loop");
            telemetry.update();
        }


    //========================== Run Op Mode ==============================
    public void runOpMode() throws InterruptedException{

        //======== Initialization Call ========
        this.customInit();

        //======== Loop ========
        while(opModeIsActive()){

            if(!ISAUTONMODE){
                //=== Get Gamepad State
                Gamepad currentCommands = this.getCommands();// get the current commands: abstractify controls instead of redefining...

                //=== Feature Calls
                DirectionalMovement(currentCommands);
                LinearSlide(currentCommands);
                Claw(currentCommands);
                HSlide(currentCommands);

            }else{

                AutonScenarioBlueOne(); // Spin 3 backwards and then extend hSlide
                break;
            }

            idle();
        }
    }
}
