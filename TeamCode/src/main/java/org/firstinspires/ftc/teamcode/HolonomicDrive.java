package org.firstinspires.ftc.teamcode;

/**
 * Created by Mike on 8/30/2017.
 */
public abstract class HolomorphicDrive {
    // Goals of this Class (Note to self Put this in a seperate README.md)
    /*
    *   Take arguments such as +-
    *
    *   move x steps    (horozontal shift)
    *   move y steps    (vertical shift)
    *   rotate θ deg   (yaw)
    *
    *   and return an array of 4 step values for the motor encoders (even if encoders not used the info is valid as a time ratio)
    *   based upon the holonomic drive principal
    *
    *   the wheel step values are returned as an array with the indicies corresponding
    *   to the diagram below:
    *
    *
    *                   +y
    *     Top/Foreward Orientation/ Front of robot
    *
    *           (1) //     \\ (0)
    *    -x  Left       ^       Right  +x
    *           (3) \\     // (2)
    *
    *                 Bottom
    *                   -y
    *
    *   example:    HolomorphicDrive.rotate(180) rotates 180 Left (as per standard vector direction for rotation matrix)
    *               which returns an array of 4 items corresponding to the number of steps for each motor
    * */

    //Setup
        //Relative Rotation in deg
        public static double holo_current_rotation = 0;

        //Wheel Mapping
            public static int[] wheel_maping = {0,1,2,3}// DO NOT TOUCH UNLESS YOU KNOW WHAT YOU ARE DOING!

    //Settings:
        //Holonomic Drive x y relative strengths (i.e mechenum wheels 30:70)
        // (X as being defined as Parallel to the axel of the wheels)
        public static double holo_x_strength = 0.3;
        public static double holo_y_strength = 0.7;

        //Wheel corrections/ Fine tuning:
        public int[] wheel_x_corrections = {0,0,0,0}; // One for each wheel corresponding top right,left bottom right,left
        public int[] wheel_y_corrections = {0,0,0,0}; // One for each wheel corresponding top right,left bottom right,left

        //Wheel invert spin direction: DO NOT TOUCH UNLESS YOU KNOW WHAT YOU ARE DOING!
        public int[] wheel_rot_invert = {false,false,false,false};

    //Rotation Code
    public rotate(double theta_in){
    }

    //Move code
    public move( double x_in,double y_in){

    }

}
