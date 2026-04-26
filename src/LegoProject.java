package src;

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