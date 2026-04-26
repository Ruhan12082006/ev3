/**
 * JAVADOC for Applied Application Project
 * 
 * LEGO EV3 Line Follower and Obstacle Avoider
 *
 * <p><b>Main Functions of the Program:</b></p>
 * <p>This program makes a LEGO EV3 robot follow a black line using a color sensor and PID math 
 * for smooth driving. While following the line, it watches for obstacles. If something blocks 
 * the path, the robot stops, looks left and right to see which side has more space, drives 
 * around the obstacle, and finds the line again to continue.</p>
 *
 * <p><b>How the Thread "run" Method Works:</b></p>
 * <p>The {@code UltrasonicThread} works like a background watcher. Its {@code run} method is an 
 * endless loop that constantly reads the distance from the ultrasonic sensor. Every 50 milliseconds, 
 * it checks how far away things are. If an object gets closer than 0.20 meters, it changes 
 * the {@code obstacleDetected} flag to true. This instantly tells the main program to stop 
 * following the line and start dodging the object.</p>
 *
 *  <p><b>Project Roles:</b></p>
 * <ul>
 * <li><b>Aurelija:</b> Created the background thread for the ultrasonic sensor (real-time distance checking). 
 * Calibrated the color sensor to understand black and white. Wrote the method to make the robot 
 * drive forward until it finds a black line.</li>
 * <li><b>Subhanjan:</b> Built the obstacle avoidance logic. Programmed the robot to scan left and right, 
 * decide which direction has more space, and wrote the full sequence to drive around the object 
 * and return to the line.</li>
 * <li><b>Ruhan:</b> Wrote the main program loop and tuned the PID math (kp, ki, kd) to follow the 
 * line smoothly. Controlled the motor speeds and managed the start and stop rules for the program.</li>
 * </ul>
 *
 * <li><b>References:</b> We used the links below to learn how to use certain features in our code, especially the 'Tacho' motor functions.</li>
 * 
 * @see <a href="https://stackoverflow.com/questions/8609734/lejos-nxt-how-can-i-detect-a-motor-stall">Stackoverflow</a>
 * @see <a href="https://lejos.sourceforge.io/ev3/docs/index.html?lejos/robotics/Tachometer.html">LeJOS EV3 Document</a>
 */ 

package src;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;

public class LegoProject {

    static volatile boolean obstacleDetected = false;
    static volatile float currentDistance = 999f;

    //     Aurelija's responsibilities:
    //   - Ultrasonic sensor thread (real-time distance + obstacle flag)
    //   - Color sensor calibration (black/white thresholds)
    //   - driveUntilLine (color sensor line detection)

    static class UltrasonicThread extends Thread {
        EV3UltrasonicSensor ultrasonicSensor;
        UltrasonicThread() {
            ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S2);
        }
        @Override
        public void run() {
            SampleProvider distance = ultrasonicSensor.getDistanceMode();
            float[] sample = new float[distance.sampleSize()];
            while (!Button.ESCAPE.isDown()) {
                distance.fetchSample(sample, 0);
                currentDistance = sample[0];
                obstacleDetected = (currentDistance < 0.20f);
                LCD.drawString("Dist: " + String.format("%.2f", currentDistance) + "m  ", 0, 3);
                Delay.msDelay(50);
            }
            ultrasonicSensor.close();
        }
    }

    // Color sensor: drive until black line is found
    static void driveUntilLine(SampleProvider light, float blackThreshold) {
        float[] sample = new float[light.sampleSize()];
        while (true) {
            light.fetchSample(sample, 0);
            if (sample[0] < blackThreshold) {
                break;
            }
            Motor.A.setSpeed(150);
            Motor.B.setSpeed(150);
            Motor.A.forward();
            Motor.B.forward();
            Delay.msDelay(20);
        }
        Motor.A.stop(true);
        Motor.B.stop(true);
    }

    //      Subhanjan's responsibilities:
    //   - Progressive left/right ultrasonic scanning
    //   - Direction decision logic (leftscan vs rightscan)
    //   - Full obstacle bypass sequence (turn, pass, realign)

    static void detectAndAvoid(SampleProvider light, float blackThreshold) {
        Motor.A.stop(true);
        Motor.B.stop(true);
        LCD.clear();
        LCD.drawString("Blocked fr", 0, 0);
        Delay.msDelay(300);

        // STEP 1: Scan LEFT progressively
        LCD.drawString("Scanning LEFT...", 0, 1);
        Motor.A.resetTachoCount();
        Motor.B.resetTachoCount();
        int leftscan = 0;
        int maxscan = 400;

        while (leftscan < maxscan) {
            float progress = (float) leftscan / maxscan;
            int speed = (int)(50 + 100 * progress);
            speed = Math.min(speed, 150);
            Motor.A.setSpeed(speed);
            Motor.B.setSpeed(speed);
            Motor.A.backward();
            Motor.B.forward();
            leftscan = Math.abs(Motor.B.getTachoCount());
            if (currentDistance >= 0.30f) break;
            Delay.msDelay(20);
        }
        Motor.A.stop(true);
        Motor.B.stop(true);
        Delay.msDelay(200);
        LCD.drawString("Left Scan: " + leftscan + "  ", 0, 1);

        // STEP 2: Return to center
        Motor.A.resetTachoCount();
        Motor.B.resetTachoCount();
        int returned = 0;
        while (returned < leftscan) {
            Motor.A.setSpeed(150);
            Motor.B.setSpeed(150);
            Motor.A.forward();
            Motor.B.backward();
            returned = Math.abs(Motor.B.getTachoCount());
            Delay.msDelay(20);
        }
        Motor.A.stop(true);
        Motor.B.stop(true);
        Delay.msDelay(300);

        // STEP 3: Scan RIGHT progressively
        LCD.drawString("Scanning RIGHT..", 0, 1);
        Motor.A.resetTachoCount();
        Motor.B.resetTachoCount();
        int rightscan = 0;

        while (rightscan < maxscan) {
            float progress = (float) rightscan / maxscan;
            int speed = (int)(50 + 100 * progress);
            speed = Math.min(speed, 150);
            Motor.A.setSpeed(speed);
            Motor.B.setSpeed(speed);
            Motor.A.forward();
            Motor.B.backward();
            rightscan = Math.abs(Motor.B.getTachoCount());
            if (currentDistance >= 0.30f) break;
            Delay.msDelay(20);
        }
        Motor.A.stop(true);
        Motor.B.stop(true);
        Delay.msDelay(200);
        LCD.drawString("Right Scan " + rightscan + "  ", 0, 2);

        // STEP 4: Return to center
        Motor.A.resetTachoCount();
        Motor.B.resetTachoCount();
        returned = 0;
        while (returned < rightscan) {
            Motor.A.setSpeed(150);
            Motor.B.setSpeed(150);
            Motor.A.backward();
            Motor.B.forward();
            returned = Math.abs(Motor.B.getTachoCount());
            Delay.msDelay(20);
        }
        Motor.A.stop(true);
        Motor.B.stop(true);
        Delay.msDelay(300);

        // STEP 5: Compare and choose side with more space
        boolean goRight = (leftscan >= rightscan);
        LCD.clear();
        LCD.drawString("L:" + leftscan + " R:" + rightscan, 0, 0);
        LCD.drawString("Go: " + (goRight ? "RIGHT" : "LEFT"), 0, 1);
        Delay.msDelay(800);

        // STEP 6: Turn 90 degrees in chosen direction
        Motor.A.setSpeed(180);
        Motor.B.setSpeed(180);
        if (goRight) {
            Motor.A.rotate(180, true);
            Motor.B.rotate(-180, false);
        } else {
            Motor.A.rotate(-180, true);
            Motor.B.rotate(180, false);
        }
        Delay.msDelay(300);

        // STEP 7: Drive forward to clear obstacle
        LCD.clear();
        LCD.drawString("Passing obj...", 0, 0);
        Motor.A.setSpeed(150);
        Motor.B.setSpeed(150);
        Motor.A.forward();
        Motor.B.forward();
        Delay.msDelay(3500);
        Motor.A.stop(true);
        Motor.B.stop(true);

        // STEP 8: Turn 90 degrees back toward line
        Motor.A.setSpeed(180);
        Motor.B.setSpeed(180);
        if (goRight) {
            Motor.A.rotate(-180, true);
            Motor.B.rotate(180, false);
        } else {
            Motor.A.rotate(180, true);
            Motor.B.rotate(-180, false);
        }
        Delay.msDelay(300);

        // STEP 9: Drive forward past obstacle width
        Motor.A.setSpeed(150);
        Motor.B.setSpeed(150);
        Motor.A.forward();
        Motor.B.forward();
        Delay.msDelay(7000);
        Motor.A.stop(true);
        Motor.B.stop(true);

        // STEP 10: Turn 90 degrees toward line
        Motor.A.setSpeed(180);
        Motor.B.setSpeed(180);
        if (goRight) {
            Motor.A.rotate(-180, true);
            Motor.B.rotate(180, false);
        } else {
            Motor.A.rotate(180, true);
            Motor.B.rotate(-180, false);
        }
        Delay.msDelay(300);

        // STEP 11: Drive until color sensor finds the black line
        LCD.clear();
        LCD.drawString("Finding line...", 0, 0);
        driveUntilLine(light, blackThreshold);
        Delay.msDelay(200);

        // STEP 12: Realign with line direction
        Motor.A.setSpeed(180);
        Motor.B.setSpeed(180);
        if (goRight) {
            Motor.A.rotate(90, true);
            Motor.B.rotate(-90, false);
            Motor.A.setSpeed(80);
            Motor.B.setSpeed(80);
            Motor.A.forward();
            Motor.B.forward();
            Delay.msDelay(700);
            Motor.A.stop(true);
            Motor.B.stop(true);
        } else {
            Motor.A.rotate(-180, true);
            Motor.B.rotate(180, false);
        }
        Delay.msDelay(300);

        // STEP 13: Small forward nudge to center sensor on line
        Motor.A.setSpeed(100);
        Motor.B.setSpeed(100);
        Motor.A.forward();
        Motor.B.forward();
        Delay.msDelay(300);
        Motor.A.stop(true);
        Motor.B.stop(true);
        Delay.msDelay(200);

        LCD.clear();
        LCD.drawString("Back on track!", 0, 0);
        Delay.msDelay(500);
    }
    //      Ruhan's responsibilities
    //   - Implemention of PID controller.
    //   - Main Execuation of Loop and Logic.
    //   - Motor Speed Management.
    public static void main(String[] args) 
    {
        EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S3);
        SampleProvider light = colorSensor.getRedMode();
        float[] sample = new float[light.sampleSize()];

        // Calibration
        LCD.clear();
        LCD.drawString("On BLACK, press", 0, 0);
        Button.waitForAnyPress();
        light.fetchSample(sample, 0);
        float blackThreshold = sample[0] + 0.02f;

        LCD.clear();
        LCD.drawString("On WHITE, press", 0, 0);
        Button.waitForAnyPress();
        light.fetchSample(sample, 0);
        float whiteThreshold = sample[0] - 0.02f;

        float target = (blackThreshold + whiteThreshold) / 2.0f;
        LCD.clear();
        LCD.drawString("Target: " + String.format("%.2f", target), 0, 0);
        Delay.msDelay(1000);

        float kp = 200.0f;
        float ki = 0.1f;
        float kd = 200.0f;
        int baseSpeed = 150;
        float previousError = 0.0f;
        float integral = 0.0f;

        LCD.clear();
        LCD.drawString("PID + Bypass", 0, 0);
        LCD.drawString("Press to Start", 0, 1);
        Button.waitForAnyPress();
        LCD.clear();

        // Start ultrasonic thread
        UltrasonicThread ultraThread = new UltrasonicThread();
        ultraThread.setDaemon(true);
        ultraThread.start();

        while (!Button.ESCAPE.isDown()) 
        {

            if (obstacleDetected) 
            {
                Motor.A.stop(true);
                Motor.B.stop(true);
                Delay.msDelay(300);
                detectAndAvoid(light, blackThreshold);
                previousError = 0.0f;
                integral = 0.0f;
                continue;
            }

            light.fetchSample(sample, 0);
            float intensity = sample[0];
            float error = intensity - target;
            integral += error;
            integral = Math.max(-100.0f, Math.min(100.0f, integral));
            float derivative = error - previousError;
            previousError = error;
            float pidOutput = (kp * error) + (ki * integral) + (kd * derivative);
            int leftSpeed = (int)(baseSpeed + pidOutput);
            int rightSpeed = (int)(baseSpeed - pidOutput);
            leftSpeed = Math.max(0, Math.min(900, leftSpeed));
            rightSpeed = Math.max(0, Math.min(900, rightSpeed));
            Motor.A.setSpeed(leftSpeed);
            Motor.B.setSpeed(rightSpeed);
            Motor.A.forward();
            Motor.B.forward();
            LCD.drawString("I= " + String.format("%.2f", intensity), 0, 0);
            LCD.drawString("Err= " + String.format("%.2f", error), 0, 1);
            LCD.drawString("PID= " + String.format("%.1f", pidOutput), 0, 2);
            Delay.msDelay(10);
        }

        colorSensor.close();
        Motor.A.stop();
        Motor.B.stop();
    }
}
