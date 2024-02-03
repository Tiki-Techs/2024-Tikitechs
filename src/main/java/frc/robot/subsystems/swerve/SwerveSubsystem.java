// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.


package frc.robot.subsystems.swerve;

import java.io.IOException;
import java.nio.file.Path;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryUtil;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.chassisConstants;
import frc.robot.Constants.chassisSetUp;

public class SwerveSubsystem extends SubsystemBase {
  public final SwerveModule frontLeft = new SwerveModule(chassisSetUp.fLeftDriveMotorPort, chassisSetUp.isFrontLeftDriveMotorReverse, 
  chassisSetUp.fLeftTurnMotorPort, chassisSetUp.isFrontLeftTurnMotorReverse, chassisSetUp.fLeftAbsoluteEncoder, chassisSetUp.frontLAngle,chassisSetUp.frontLKP, chassisSetUp.frontLKI, chassisSetUp.frontLKD);

  public final SwerveModule frontRight = new SwerveModule(chassisSetUp.fRightDriveMotorPort, chassisSetUp.isFrontRightDriveMotorReverse, 
  chassisSetUp.fRightTurnMotorPort, chassisSetUp.isFrontRightTurnMotorReverse, chassisSetUp.fRightAbsoluteEncoder, chassisSetUp.frontRAngle, chassisSetUp.frontRKP, chassisSetUp.frontRKI, chassisSetUp.frontRKD);

  public final SwerveModule backLeft = new SwerveModule(chassisSetUp.bLeftDriveMotorPort, chassisSetUp.isBackLeftDriveMotorReverse, 
  chassisSetUp.bLeftTurnMotorPort, chassisSetUp.isBackLeftTurnMotorReverse, chassisSetUp.bLeftAbsoluteEncoder, chassisSetUp.backLAngle, chassisSetUp.backLKP, chassisSetUp.backLKI, chassisSetUp.backLKD);

  public final SwerveModule backRight = new SwerveModule(chassisSetUp.bRightDriveMotorPort, chassisSetUp.isBackRightDriveMotorReverse, 
  chassisSetUp.bRightTurnMotorPort, chassisSetUp.isBackRightTurnMotorReverse, chassisSetUp.bRightAbsoluteEncoder, chassisSetUp.backRAngle, chassisSetUp.backRKP, chassisSetUp.backRKI, chassisSetUp.backRKD);

  private AHRS gyro = new AHRS();
  //SwerveDriveOdometry swerveOdometry = new SwerveDriveOdometry(chassisConstants.swerveKinematics, getYaw(), getModulePositions());
  SwerveDriveOdometry swerveOdometry = new SwerveDriveOdometry(chassisConstants.swerveKinematics, new Rotation2d(), getModulePositions());
  private SwerveModuleState[] swerveModuleStates;
  //public static PhotonCamera tracker = new PhotonCamera("tracker");
  public static NetworkTable limelight = NetworkTableInstance.getDefault().getTable("limelight");

  public SwerveSubsystem() {
    new Thread(() -> {
      try {
        Thread.sleep(1000);
        zeroHeading();
      } catch(Exception e){
      }
    }).start();
  }
  public void zeroHeading(){
    gyro.reset();
  }

  public double getHeading(){
    return Math.IEEEremainder(gyro.getAngle(), 360);
  }
  public double getPitch(){
    return gyro.getPitch();
  }
  public Rotation2d getRotation2d(){
    return Rotation2d.fromDegrees(getHeading());
  }
  public Pose2d getPose(){
    return swerveOdometry.getPoseMeters();
  }
  public void resetOdometry(Pose2d pose){
    swerveOdometry.resetPosition(getRotation2d(), getModulePositions(), pose);
  }

  @Override
  public void periodic() {
    swerveOdometry.update(getRotation2d(), getModulePositions());
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("Robot Heading", getHeading());
    SmartDashboard.putNumber("FrontL Angle: ", (frontLeft.absoluteEncoder.getAbsolutePosition()));
    SmartDashboard.putNumber("FrontR Angle: ", (frontRight.absoluteEncoder.getAbsolutePosition()));
    SmartDashboard.putNumber("BackL Angle: ", (backLeft.absoluteEncoder.getAbsolutePosition()));
    SmartDashboard.putNumber("BackR Angle: ", (backRight.absoluteEncoder.getAbsolutePosition()));
  }
  public void stopModules(){
    frontLeft.stop();
    frontRight.stop();
    backLeft.stop();
    backRight.stop();
  }
  
  public void drive(Translation2d translation, double rotation, boolean fieldRelative) {
    swerveModuleStates =
      Constants.chassisConstants.swerveKinematics.toSwerveModuleStates(
          fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(
                                translation.getX(), 
                                translation.getY(), 
                                rotation, 
                                getYaw()
                            )
                            : new ChassisSpeeds(
                                translation.getX(), 
                                translation.getY(), 
                                rotation)
                            );
    setModuleStates(swerveModuleStates);
  }    
  public void setModuleStates(SwerveModuleState[] desiredStates) {
    SwerveDriveKinematics.desaturateWheelSpeeds(desiredStates, chassisConstants.maxSpeedMPS);
    backLeft.setDesiredState(desiredStates[2]);
    backRight.setDesiredState(desiredStates[3]);
    frontLeft.setDesiredState(desiredStates[0]);
    frontRight.setDesiredState(desiredStates[1]);
}
public SwerveModulePosition[] getModulePositions(){
  SwerveModulePosition[] positions = new SwerveModulePosition[4];
  positions[0] = frontLeft.getPosition();
  positions[1] = frontRight.getPosition();
  positions[2] = backLeft.getPosition();
  positions[3] = backRight.getPosition();
  return positions;
}
public void zeroModules(){
  frontLeft.zeroModules();
  frontRight.zeroModules();
  backLeft.zeroModules();
  backRight.zeroModules();
}
  
  public Rotation2d getYaw() {
  
   var yaw = gyro.getYaw();
    return (!chassisSetUp.invertedGyro) ? Rotation2d.fromDegrees(360 - yaw) : Rotation2d.fromDegrees(yaw);
}
public static Trajectory generateTrajectory (String trajectory){

  Trajectory traj = new Trajectory();

  try {

    Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajectory);
    traj = TrajectoryUtil.fromPathweaverJson(trajectoryPath);

   } catch (IOException ex){

    DriverStation.reportError("Unable to open trajectory: " + trajectory, ex.getStackTrace());
   }

  return traj;

 }
}