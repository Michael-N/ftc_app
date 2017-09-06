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
    //public double slowDrive = 1.0;
    //public boolean wasPressed = false;
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
            //if slowDrive = 1, drive mode normal
            //if slowDrive = 0.5, drive mode half
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
            //rotate clock
            if((gamepad1.left_bumper) && (!gamepad1.right_bumper))
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
            //rotate counter
            if((gamepad1.right_bumper) && (!gamepad1.left_bumper))
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
