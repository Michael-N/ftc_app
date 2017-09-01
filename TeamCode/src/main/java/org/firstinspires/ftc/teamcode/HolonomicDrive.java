package org.firstinspires.ftc.teamcode;

import static java.lang.Math.abs;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * Created by andre on 8/31/2017.
 */
@TeleOp(name = "HolonomicDrive")
//@Disabled
public class HolonomicDrive extends LinearOpMode
{
    private DcMotor motorLeftFront;
    private DcMotor motorLeftRear;
    private DcMotor motorRightFront;
    private DcMotor motorRightRear;

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

        //public double slowDrive = 1.0;
        //public boolean wasPressed = false;
        public int driveSpeed = 1;
        //driveSpeed = 1 is regular
        //driveSpeed = -1 is half
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
            if



            idle();
        }
    }
}
