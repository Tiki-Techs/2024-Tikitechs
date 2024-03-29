package frc.robot.subsystems;

import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotContainer;
import frc.robot.Constants.LED;
import frc.robot.commands.swervedrive.HoodPositioner;

public class Lights extends SubsystemBase {

    DigitalOutput redAlliance = new DigitalOutput(LED.redAlliance);
    DigitalOutput blueAlliance = new DigitalOutput(LED.blueAlliance);
    DigitalOutput ready = new DigitalOutput(LED.Ready);

    @Override
    public void periodic() {
        if (DriverStation.getAlliance().isPresent()) {
            if (DriverStation.getAlliance().get() == Alliance.Blue) {
                redAlliance.set(true);
                blueAlliance.set(false);
            } else {
                blueAlliance.set(true);
                redAlliance.set(false);
            }
        }

        if(Math.abs(RobotContainer.s_Hood.getRotation()-RobotContainer.s_Shooter.getAutomaticState().angle) < 1
         && Math.abs(RobotContainer.s_Shooter.getVelocity()-RobotContainer.s_Shooter.getAutomaticState().speed) < RobotContainer.s_Shooter.getAutomaticState().speed * (0.10)
         && Vision.angle < 5)
        {
            ready.set(false);
        }
        else{
            ready.set(true);
        }
    }

    public double Error(){
        return (Vision.distance)/10;
    }
}
