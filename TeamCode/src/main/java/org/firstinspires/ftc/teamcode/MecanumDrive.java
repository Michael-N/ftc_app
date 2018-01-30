package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.hardware.DcMotor;
import java.lang.Math;
//import java.util.concurrent

/**
 * Created by Mike on 1/27/2018.
 */

public class MecanumDrive {

    //=== Initializations
    double speed = 1.0;

    //=== Configurations / Adjust Settings
        public void setSpeed(double newSpeed){
            this.speed = newSpeed;
        }
    //=== Class Internal Helping Functions

        //== Calculation functions
            //= Basic Movement
                /*     Vx corresponds to these positions
                *       1   2
                *       3   4
                * */
                private double __V1(double robotSpeed,double angle, double rotate){
                    /*
                    *   robotSpeed          [-1,1]
                    *   angle               [0,2pi]
                    *   directionalSpeed    [-1,1]
                    *
                    * */
                    return this.speed*robotSpeed*Math.sin(angle + Math.PI/4) + rotate;
                }
                private double __V2(double robotSpeed,double angle, double rotate){
                    /*
                    *   robotSpeed          [-1,1]
                    *   angle               [0,2pi]
                    *   directionalSpeed    [-1,1]
                    *
                    * */
                    return this.speed*robotSpeed*Math.cos(angle + Math.PI/4) - rotate;
                }
                private double __V3(double robotSpeed,double angle, double rotate){
                    /*
                    *   robotSpeed          [-1,1]
                    *   angle               [0,2pi]
                    *   directionalSpeed    [-1,1]
                    *
                    * */
                    return this.speed*robotSpeed*Math.sin(angle + Math.PI/4) + rotate;
                }
                private double __V4(double robotSpeed,double angle, double rotate){
                    /*
                    *   robotSpeed          [-1,1]
                    *   angle               [0,2pi]
                    *   directionalSpeed    [-1,1]
                    *
                    * */
                    return this.speed*robotSpeed*Math.sin(angle + Math.PI/4) - rotate;
                }

            //= Over-ride-able input transform function
                public double inputTransform(double x){
                    // Default function
                    double y_ = 0.5*Math.tanh(5*(Math.abs(x)-0.5)) + 0.5;
                    //== This extends the range to be [-1,1] uniformly for the behavior in the 1st quadrant
                    return (x/Math.abs(x)) * y_;
                }

        //== Calculation Preforming functions
            private double[] __calculateActivations(double magnitude, double angle,double rotate){

                // Motor activations
                double m1 = __V1(this.inputTransform(magnitude), angle, rotate)/2;
                double m2 = __V2(this.inputTransform(magnitude), angle, rotate)/2;
                double m3 = __V3(this.inputTransform(magnitude), angle, rotate)/2;
                double m4 = __V4(this.inputTransform(magnitude), angle, rotate)/2;

                double[] calcRes = {m1,m2,m3,m4};
                return calcRes;
            }
            public double[] calculate(double stickRightX,double stickRightY,double leftTrigger, double rightTrigger){
                double magnitudeNormalized = Math.sqrt(Math.pow(0.7071*stickRightX,2.0) + Math.pow(0.7071*stickRightY,2.0));
                double angle = Math.atan(stickRightY/stickRightX);
                double rotation = 0 -rightTrigger +  leftTrigger;
                return this.__calculateActivations(magnitudeNormalized,angle,rotation);
            }

}
