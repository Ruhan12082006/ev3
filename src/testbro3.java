package src;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class testbro3 {
    static volatile boolean obstacleDetected = false;
    static volatile float currentDistance = 999f;

    // --- Ultrasonic Thread ---
    static class UltrasonicThread extends Thread {
        EV3UltrasonicSensor ultrasonicSensor;
        UltrasonicThread() {
            ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S2);
        }
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

 
    static final int TACHO_90 = 360; // Tune this!
    static void turn90(int direction) {
        Motor.A.resetTachoCount();
        Motor.B.resetTachoCount();
        int traveled = 0;
        while (traveled < TACHO_90) {
            float progress = (float) traveled / TACHO_90;
            int speed = (int)(100 + 100 * (1 - progress));
            speed = Math.max(speed, 80);
            if (direction == 1) {
                Motor.A.setSpeed(speed);
                Motor.B.setSpeed(speed);
                Motor.A.backward();
                Motor.B.forward();
            } else {
                Motor.A.setSpeed(speed);
                Motor.B.setSpeed(speed);
                Motor.A.forward();
                Motor.B.backward();
            }
            traveled = Math.abs(Motor.B.getTachoCount());
            Delay.msDelay(10);
        }
        Motor.A.stop(true);
        Motor.B.stop(true);
        Delay.msDelay(200);
    }


    static void driveForward(int ms) {
        Motor.A.setSpeed(150);
        Motor.B.setSpeed(150);
        Motor.A.forward();
        Motor.B.forward();
        Delay.msDelay(ms);
        Motor.A.stop(true);
        Motor.B.stop(true);
    }


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


    // detection
 
    static void detectAndAvoid(SampleProvider light, float blackThreshold) {
        Motor.A.stop(true);
        Motor.B.stop(true);
        LCD.clear();
        LCD.drawString("Obstacle! Stop", 0, 0);
        Delay.msDelay(300);


        Motor.A.setSpeed(100);
        Motor.B.setSpeed(100);
        Motor.A.backward();
        Motor.B.forward();
        Delay.msDelay(150); 
        Motor.A.stop(true);
        Motor.B.stop(true);
        Delay.msDelay(200); 
        float leftDist = currentDistance;

    
        Motor.A.setSpeed(100);
        Motor.B.setSpeed(100);
        Motor.A.forward();
        Motor.B.backward();
        Delay.msDelay(150);
        Motor.A.stop(true);
        Motor.B.stop(true);
        Delay.msDelay(200);
        float centerDist = currentDistance;

       
        Motor.A.setSpeed(100);
        Motor.B.setSpeed(100);
        Motor.A.forward();
        Motor.B.backward();
        Delay.msDelay(150);
        Motor.A.stop(true);
        Motor.B.stop(true);
        Delay.msDelay(200);
        float rightDist = currentDistance;

       
        Motor.A.setSpeed(100);
        Motor.B.setSpeed(100);
        Motor.A.backward();
        Motor.B.forward();
        Delay.msDelay(150);
        Motor.A.stop(true);
        Motor.B.stop(true);
        Delay.msDelay(200);

    
        LCD.clear();
        LCD.drawString("L: " + String.format("%.2f", leftDist) + "m", 0, 0);
        LCD.drawString("C: " + String.format("%.2f", centerDist) + "m", 0, 1);
        LCD.drawString("R: " + String.format("%.2f", rightDist) + "m", 0, 2);
        Delay.msDelay(1000);

        
        if (leftDist < rightDist) {
        
            LCD.clear();
            LCD.drawString("Obstacle: LEFT", 0, 0);
            LCD.drawString("Avoiding: RIGHT", 0, 1);
            Delay.msDelay(500);

            turn90(-1);
            driveForward(800);
            turn90(1);
            driveUntilLine(light, blackThreshold);
            turn90(-1);

        } else if (rightDist < leftDist) {
          
            LCD.clear();
            LCD.drawString("Obstacle: RIGHT", 0, 0);
            LCD.drawString("Avoiding: LEFT", 0, 1);
            Delay.msDelay(500);

            turn90(1);
            driveForward(800);
            turn90(-1);
            driveUntilLine(light, blackThreshold);
            turn90(1);

        } else {
           
            LCD.clear();
            LCD.drawString("Obstacle: CENTER", 0, 0);
            LCD.drawString("TODO: handle", 0, 1);
            Delay.msDelay(2000);
        }

        LCD.clear();
        LCD.drawString("Back on track!", 0, 0);
        Delay.msDelay(500);
    }

    // main class
    
    public static void main(String[] args) {
        EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S3);
        SampleProvider light = colorSensor.getRedMode();
        float[] sample = new float[light.sampleSize()];

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

        float kp = 600.0f;
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

        while (!Button.ESCAPE.isDown()) {
         
            if (obstacleDetected) {
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
            LCD.drawString("Int: " + String.format("%.2f", intensity), 0, 0);
            LCD.drawString("Err: " + String.format("%.2f", error), 0, 1);
            LCD.drawString("PID: " + String.format("%.1f", pidOutput), 0, 2);
            Delay.msDelay(10);
        }

        colorSensor.close();
        Motor.A.stop();
        Motor.B.stop();
    }
}