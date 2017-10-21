package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import static java.lang.Math.*;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import java.util.Calendar;

/**
 * Created by Mike on 9/23/2017.
 *
 * note to the user: documentation/help is provided as comments in the functions, methods, or sections
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
    *   Diagonal: <left-stick-x><left-stick-y>
    *
    *   ~~~Planed Features~~~
    *   -add in encoder for motors 2,4 (back two motors) + option to toggle between using them and not
    *   -work on auton mode...
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
        //public CRServo[] allServos = new CRServo[servoNames.length];//initial servo storage
        //public CRServo[] mappedServos = new CRServo[servoNames.length];//mapped servo storage

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
        //== Handle acceleration of input: how it is mapped across [-1,1]
        public double accelerationCurve(double x){
            /*
            *   y = -ln(1.36788 -x)  for x in [0,1]  graph = concave /
            *
            *   input to this function will be [-1,1]
            * == 3 Canidates ==
            *
            * double y = -log(1.36788 -abs(x));
            *
            *
            * or an alternate in radians
            *
            * double y = sin(4*PI*abs(x));
            *
            *
            * or an alternate
            *
            * double y = tanh(1.0926*(abs(x)-0.5)) + 0.5;
            * double y = tanh(5*(abs(x)-0.5)) + 0.5;        //my favorite
            *
            * */
        double y = tanh(5*(abs(x)-0.5)) + 0.5;;
        return (x/abs(x)) * y;
    }

        //== Activates Motors with activationValues parameter and a precision parameter
        public void activateMotors(double[] activationValues,boolean usePrecision){
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
            for(int k=0;k<4;k++){
                if(usePrecision){
                    //multiply by setting
                    this.mappedMotors[k].setPower(this.accelerationCurve(this.precisionSpeed*activationValues[k]));
                }else{
                    //multiply by setting
                    this.mappedMotors[k].setPower(this.accelerationCurve(this.regularSpeed*activationValues[k]));
                }

            }
        }

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
            boolean doTurn = this.eXOR(currentCommands.left_bumper,currentCommands.right_bumper);
            if(doTurn){
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
                //=== Natural Inversion Config : see diagram in this.activateMotors method...
                double[] horozontalActivations = {stick_x,-stick_x,-stick_x,stick_x};
                double[] verticalActivations = {-stick_y,-stick_y,-stick_y,-stick_y};

                //== Movement Forewards & Reverse (vertical):
                boolean doForRev= this.isAboveThreshold(stick_y) && !this.isAboveThreshold(stick_x);
                if(doForRev){
                    this.activateMotors(verticalActivations,this.isPrecisionSpeed);
                }
                //== Movement Strafe (horozontal):
                boolean doHorz = this.isAboveThreshold(stick_x) && !this.isAboveThreshold(stick_y);
                if(doHorz){
                    this.activateMotors(horozontalActivations,this.isPrecisionSpeed);
                }

            //=== Planar Movement Diagonal
                //=== Get commands
                    boolean doDiagonal= this.isAboveThreshold(stick_x) && this.isAboveThreshold(stick_y);
                    boolean diagonalOne = doDiagonal && (0<(stick_x * stick_y));// like the 4 quadrents in math -+ ++      + * + = +   -+ * +- = -
                    boolean diagonalTwo = doDiagonal && (0>(stick_x*stick_y));//                                -- +-      - * - = +
                    double normalizedU = sqrt(pow(0.7071*stick_x,2.0) + pow(0.7071*stick_y,2.0)) * (stick_y/abs(stick_y));
                    /* ^above

                    motor only accepts values [-1,1] and the diagonal movement must be responsive to both x & y rather the
                    length of the vector <x,y>: denoted u, x,y are scalars which for which  |u|,|x|,|y| <= 1 so
                    (ax)^2 + (ay)^2 <= |u|^2 , the y direction is still important so the final thing we want is
                    |u|* (y/|y|) which gives us the y axis direction... solving a<= sqrt(0.5) approx: 0.7071

                          U   /|
                            /  |   y   if x,y = 1 |U| = sqrt(2) > 1 ---> err...
                            ---
                            x
                    */
                    double[] diagonalTopRightBackLeft = {0,-normalizedU,-normalizedU,0};// spin perpendicular diagonal wheels
                    double[] diagonalTopLeftBackRight = {-normalizedU,0,0,-normalizedU};
                    //== / diagonal movement
                    if(diagonalOne){
                        this.activateMotors(diagonalTopRightBackLeft,this.isPrecisionSpeed);
                    }
                    //== \ diagonal movement
                    if(diagonalTwo){
                            this.activateMotors(diagonalTopLeftBackRight,this.isPrecisionSpeed);
                    }

            //=== Stop Movement
                //== Note: Do NOT create a stayStill function using 0 as all activation values: jittery motors... if if sttement bad
                boolean[] willRunSequenceForOtherCommands = {doForRev,doHorz,doTurn,diagonalOne,diagonalTwo};
                double[] stopActivations = {0,0,0,0};

                //== If all the other commands are false then therfore stop!
                if(this.allFalse(willRunSequenceForOtherCommands)){
                    this.activateMotors(stopActivations,this.isPrecisionSpeed);
                }

            idle();
        }
    }
}
