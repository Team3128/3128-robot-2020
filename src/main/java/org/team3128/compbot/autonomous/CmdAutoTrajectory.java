package org.team3128.compbot.autonomous; 

import org.team3128.common.control.trajectory.Trajectory;
import org.team3128.common.control.trajectory.TrajectoryGenerator;
import org.team3128.common.control.trajectory.constraint.TrajectoryConstraint;
import org.team3128.common.utility.math.Pose2D;
import org.team3128.common.utility.units.Length;
import org.team3128.common.drive.Drive;
import org.team3128.compbot.subsystems.Constants;
import org.team3128.common.utility.Log;
import org.team3128.common.drive.DriveSignal;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.Timer;

import java.util.ArrayList;

public class CmdAutoTrajectory extends Command { 
    public ArrayList<Pose2D> waypoints = new ArrayList<Pose2D>();
    public Trajectory trajectory;

    private double speed, acceleration, timeExpected, timeInitial, timeCurrent, timeoutMs;
    private Drive drive;
    
    public CmdAutoTrajectory(Drive drive, double speed, double acceleration, double timeoutMs, Pose2D... inputWaypoints) { 

        this.speed = speed;
        this.acceleration = acceleration;
        this.drive = drive;
        this.timeoutMs = timeoutMs;

        for(Pose2D ele : inputWaypoints) {
            waypoints.add(ele);
        }
    }

    @Override protected void initialize() { 
        
        trajectory = TrajectoryGenerator.generateTrajectory(waypoints, new ArrayList<TrajectoryConstraint>(), 0, 0,
                speed * Constants.MechanismConstants.inchesToMeters, acceleration, false);
        drive.setAutoTrajectory(trajectory, false);
        drive.startTrajectory();
        timeExpected = trajectory.getTotalTimeSeconds();
        timeInitial = Timer.getFPGATimestamp();

    }

    @Override protected synchronized boolean isFinished() {
        timeCurrent = Timer.getFPGATimestamp();
        if ((timeCurrent - timeInitial) >= Math.min(timeExpected, timeoutMs)) {
           return true;
        } else {
           return false;
       }
    }

    @Override protected void end() {
        drive.setWheelPower(new DriveSignal(0, 0));
    }

    @Override public void interrupted() { 
        Log.info("CmdAutoTrajectory", "Interrupted.");
        end();
    }

}