package frc.robot.subsystems;

import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.IntakeConstants;
import frc.robot.commands.swervedrive.HoodPositioner;
import frc.robot.RobotContainer;

public class Intake extends SubsystemBase {
    public static CANSparkMax mot = new CANSparkMax(IntakeConstants.m_Intake, MotorType.kBrushless);

    public void run() {
        if (RobotContainer.mechXbox.getAButton()) {
            if(RobotContainer.s_Hood.getRotation() > 55){
                mot.set(1);
            }
            HoodPositioner.setpoint = 65;
        } 
        else if(RobotContainer.mechXbox.getYButton()){
            mot.set(-0.5);
        }else {
            mot.set(0);
        }
    }

    public Command StartIntake() {
        return new RunCommand(() -> {
            mot.set(1);
            HoodPositioner.setpoint = 65;
        }, this);
    }

    public Command StopIntake() {
        return new RunCommand(() -> mot.set(0), this);
    }
}
