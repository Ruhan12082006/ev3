package src;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class test3 {
    

    public static float currentIntensity = 0;
    public static float currentDistance = 100; 

    public static void main(String[] args) {
        
       
        Thread sensorWorker = new Thread(new SensorScanner());
        sensorWorker.setDaemon(true); 
        sensorWorker.start();

        LCD.clear();
        LCD.drawString("Threaded Bot", 0, 0);
        Button.waitForAnyPress();

        while (!Button.ESCAPE.isDown()) {
            
          
            if (currentDistance < 0.15) {
                stopAndScan();
            } 
            else {
              
                if (currentIntensity < 0.4) {
                    Motor.A.setSpeed(200);
                    Motor.B.setSpeed(200);
                    Motor.A.forward();
                    Motor.B.forward();
                } else {
                    Motor.A.stop(true);
                    Motor.B.stop(true);
                }
            }
            Delay.msDelay(10);
        }
    }

   
    public static void stopAndScan() {
        Motor.A.stop(true);
        Motor.B.stop(false);
        LCD.clear();
        LCD.drawString("Object! Scanning", 0, 0);

      
        Motor.A.rotate(-150, true);
        Motor.B.rotate(150, false);
        Delay.msDelay(500);

        if (currentDistance > 0.30) {
            LCD.drawString("Good to go (L)", 0, 2);
            Delay.msDelay(1000);
        } else {
           
            Motor.A.rotate(300, true);
            Motor.B.rotate(-300, false);
            Delay.msDelay(500);

            if (currentDistance > 0.30) {
                LCD.drawString("Good to go (R)", 0, 2);
                Delay.msDelay(1000);
            } else {
              
                Motor.A.rotate(-150, true);
                Motor.B.rotate(150, false);
                LCD.drawString("Blocked", 0, 2);
            }
        }
    }
}


class SensorScanner implements Runnable {
    public void run() {
        EV3ColorSensor color = new EV3ColorSensor(SensorPort.S3);
        SampleProvider lightProc = color.getRedMode();
        float[] cSample = new float[lightProc.sampleSize()];
        
        EV3UltrasonicSensor ultra = new EV3UltrasonicSensor(SensorPort.S2);
        SampleProvider distProc = ultra.getDistanceMode();
        float[] uSample = new float[distProc.sampleSize()];
        
        
        while (true) {
            lightProc.fetchSample(cSample, 0);
            distProc.fetchSample(uSample, 0);

           
            test3.currentIntensity = cSample[0];
            test3.currentDistance = uSample[0];

            Delay.msDelay(20); 
        }
    }
}