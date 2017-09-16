package org.firstinspires.ftc.teamcode;

import static java.lang.Math.abs;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * Created by andrew on 8/31/2017.
 */
//version alpha 1.0
@TeleOp(name = "HolonomicDrive")
//@Disabled
public class HolonomicDrive extends LinearOpMode
{
    private DcMotor motorLeftFront;
    private DcMotor motorLeftRear;
    private DcMotor motorRightFront;
    private DcMotor motorRightRear;

    /*

        //=========== Settings and Config ==============
        public string motorNames = {"motorLeftFront","motorLeftRear","motorRightFront","motorRightRear"};
        public int[] MotorMappings = {0,1,2,3};
        public boolean[] MotorReverse = {False,False,False,False}; //applied after mappings--> corresponds to index of mapped motor
        public double PrecisionSpeed = 0.5;// 0.5 means one half speed... tap& release y to activate to toggle this
        public double RegularSpeed = 1.0;
        public boolean[] InvertControlsXY = {False,False};
        public double stickThreshold = 0.15;


        //=========== Initilizations ================
            //Store the state of Precision Status:
            public boolean isPrecisionSpeed = False;

            //=== Initial Motor fetch
            public DcMotor[] AllMotors = {
                hardwareMap.dcMotor.get(motorNames[0]),
                hardwareMap.dcMotor.get(motorNames[1]),
                hardwareMap.dcMotor.get(motorNames[2]),
                hardwareMap.dcMotor.get(motorNames[3])
            };

            //=== Mapped Motor Storage
            public MappedMotors = new DcMotors(4);

             //=== Initilize mappings:
            for(int j = 0; i<4; i++){
                MappedMotors[i] = AllMotors[MotorMappings[i]];
            }

             //=== Initilize reverses
            for( int i=0; i<4; i++){
                //Reverses:
                if(MotorReverse.get(i)){
                    MappedMotors[i].setDirection(DcMotor.Direction.REVERSE);
                }

            }
       //=== Wait for the Start:
       waitForStart()

       //=== Helper Methods:
       public void activateMotors(double[] activationValues,boolean usePrecision){
            for(int k=0;k<4;k++){
                if(usePrecision){
                    //multiply by setting
                     this.MappedMotors.setPower(this.PrecisionSpeed*activationValues[i]);
                }else{
                    //multiply by setting
                    this.MappedMotors.setPower(this.RegularSpeed*activationValues[i]);
                }

            }
       }

       public static boolean eXOR(boolean x, boolean y) {// Courtesy of ~stack overflow~
            //works like or except if both  are true then it is false also
            return ( ( x || y ) && ! ( x && y ) );
        }

       public boolean isAboveThreshold(doubble inputValue){
            if(abs(inputValue)> this.stickThreshold){
                return True;
            }else{
                return False;
            }
       }

       //========= Put in the loop: while the  opModeIsActive() is true do the following
           //=== Use Precision Modifier:
               if(gamepad.y){
                    this.isPrecisionSpeed = !this.isPrecisionSpeed; // toggle precision speed by 'clicking' y
               }

            //=== Rotation Movement: left_bumper = CounterClockwise, right_bumper = Clockwise (Makes more sense than the reverse...)
               if(this.eXOR(gamepad.left_bumper, gamepad.right_bumper)){
                    if(gamepad.right_bumper){// rotate Clockwise
                        doubble[] clockActivations = {1.0,1.0,-1.0,-1.0};
                        this.activateMotors(clockActivations,this.isPrecisionSpeed);
                    }else{ //if right is false than right must be true to meet the initial condition
                        double[] cntrClockActivations = {-1.0,-1.0,1.0,1.0};
                        this.activateMotors(clockActivations,this.isPrecisionSpeed);
                    }
               }

            //Redefine LEFT Stick Values (invert if settings say so):
                doubble stick_x = InvertControlesXY[0] ? -gamepad1.left_stick_x : gamepad1.left_stick_x;
                doubble stick_y = InvertControlesXY[1] ? -gamepad1.left_stick_y : gamepad1.left_stick_y;

           //=== Planar Movement XY
                //=== Natural Inversion Config :
                doubble[] horozontalActivations = {-stick_x,stick_x,stick_x,-stick_x};
                doubble[] verticalActivations = {-stick_x,-stick_x,-stick_x,-stick_x};
                doubble[] stayStill = {0.0,0.0,0.0,0.0};

               //== Movement Forewards & Reverse (vertical):
               if(this.isAboveThreshold(stick_y) && !this.isAboveThreshold(stick_x)){
                    this.activateMotors(verticalActivations,this.isPrecisionSpeed);
               }
               //== Movement Strafe (horozontal):
               if(this.isAboveThreshold(stick_X) && !this.isAboveThreshold(stick_y)){
                    this.activateMotors(horozontalActivations,this.isPrecisionSpeed);
               }

               //== Movement STILL
               if(!this.isAboveThreshold(stick_y) && !this.isAboveThreshold(stick_x)){
                    this.activateMotors(stayStill,this.isPrecisionSpeed);
               }
        //=== Wait for hardware:
        idle();
    */

    public int driveSpeed = 1;
    //driveSpeed = 1 is regular
    //driveSpeed = -1 is half
    @Override
    public void runOpMode() throws InterruptedException
    {
        motorLeftFront = hardwareMap.dcMotor.get("motorLeftFront");
        motorLeftRear = hardwareMap.dcMotor.get("motorLeftRear");

        motorRightFront = hardwareMap.dcMotor.get("motorRightFront");
        motorRightRear = hardwareMap.dcMotor.get("motorRightRear");

        //reverse two motors, may need to do left instead
        motorRightFront.setDirection(DcMotor.Direction.REVERSE);
        motorRightRear.setDirection(DcMotor.Direction.REVERSE);


        waitForStart();

        while(opModeIsActive())
        {
            //if driveSpeed = 1, drive mode normal
            //if driveSpeed = -, drive mode half
            if(gamepad1.y)
            {
                driveSpeed = -driveSpeed;
            }



            //forwards and reverse
            if((abs(gamepad1.left_stick_y) >= 0.15) &&  (abs(gamepad1.left_stick_x) <= 0.15))
            {
                if(driveSpeed == 1)
                {
                    motorLeftFront.setPower(-gamepad1.left_stick_y);
                    motorLeftRear.setPower(-gamepad1.left_stick_y);

                    motorRightFront.setPower(-gamepad1.left_stick_y);
                    motorRightRear.setPower(-gamepad1.left_stick_y);
                }
                if(driveSpeed == -1)
                {
                    motorLeftFront.setPower(-0.5*gamepad1.left_stick_y);
                    motorLeftRear.setPower(-0.5*gamepad1.left_stick_y);

                    motorRightFront.setPower(-0.5*gamepad1.left_stick_y);
                    motorRightRear.setPower(-0.5*gamepad1.left_stick_y);
                }
            }
            //left and right strafe~ note: x axis may be flipped so that right is negative, if strafe malfunctions, try the opposite of the x-axis input values
            //for now, assume left on x-axis is 1; right is -1
            if((abs(gamepad1.left_stick_x) >= 0.15) && (abs(gamepad1.left_stick_y) <= 0.15))
            {
                if(driveSpeed == 1)
                {
                    motorLeftFront.setPower(-gamepad1.left_stick_x);
                    motorLeftRear.setPower(gamepad1.left_stick_x);

                    motorRightFront.setPower(gamepad1.left_stick_x);
                    motorRightRear.setPower(-gamepad1.left_stick_x);

                }
                if(driveSpeed == -1)
                {
                    motorLeftFront.setPower(-0.5*gamepad1.left_stick_x);
                    motorLeftRear.setPower(0.5*gamepad1.left_stick_x);

                    motorRightFront.setPower(0.5*gamepad1.left_stick_x);
                    motorRightRear.setPower(-0.5*gamepad1.left_stick_x);

                }
            }
            //rotate counter
            if((gamepad1.left_bumper) && (!gamepad1.right_bumper))
            {
                if(driveSpeed == 1)
                {
                    motorLeftFront.setPower(-1.0);
                    motorLeftRear.setPower(-1.0);

                    motorRightFront.setPower(1.0);
                    motorRightRear.setPower(1.0);
                }
                if(driveSpeed == -1)
                {
                    motorLeftFront.setPower(-0.5);
                    motorLeftRear.setPower(-0.5);

                    motorRightFront.setPower(0.5);
                    motorRightRear.setPower(0.5);
                }
            }
            //rotate clock
            if((gamepad1.right_bumper) && (!gamepad1.left_bumper))
            {
                if(driveSpeed == 1)
                {
                    motorLeftFront.setPower(1.0);
                    motorLeftRear.setPower(1.0);

                    motorRightFront.setPower(-1.0);
                    motorRightRear.setPower(-1.0);
                }
                if(driveSpeed == -1)
                {
                    motorLeftFront.setPower(0.5);
                    motorLeftRear.setPower(0.5);

                    motorRightFront.setPower(-0.5);
                    motorRightRear.setPower(-0.5);
                }
            }
            //brake while no significant controller input
            if (((abs(gamepad1.left_stick_x) <= 0.15) && (abs(gamepad1.left_stick_y) <= 0.15))  &&  ((!gamepad1.left_bumper) && (!gamepad1.right_bumper)))
            {
                motorLeftFront.setPower(0.0);
                motorLeftRear.setPower(0.0);
                motorRightFront.setPower(0.0);
                motorRightRear.setPower(0.0);
            }


            //gives the loop a little time for the hardware to catch up with the software; allows time for linear loop to process termination request before looping again
            idle();
        }
    }
}
