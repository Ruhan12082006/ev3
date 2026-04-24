package src;

import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.motor.Motor;
import lejos.utility.Delay;
import lejos.hardware.port.SensorPort;
import lejos.hardware.lcd.LCD;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;   // allows the sensor to return the samples or data
                                        // e.g., for getting distance data from sonic sensor etc
<<<<<<< HEAD:src/Sultra.java

public class Sultra {
=======
public class UltraS {
>>>>>>> 16e1a25d1b73b5b343aee325631a8c0e776ca2db:src/UltraS.java

    public static void main(String[] args) {
        // Creating an instance of US sensor at port 2
        EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S2);
        
        // Get the distance sample provider
        SampleProvider distance = ultrasonicSensor.getDistanceMode();
        LCD.clear();

        // Create a sample array to hold the distance value
        // even though sonic sensor gives distance as an o/p, but since other sensors, e.g., light sensor
        // can provide multiple values, therefore to keep consistency, I'm using sampleprovider
        float[] sample = new float[distance.sampleSize()];

         Motor.A.setSpeed(360); 
            Motor.B.setSpeed(360);
            Motor.A.forward();
            Motor.B.forward();
            Delay.msDelay(1000);

            boolean BroStop = false; 
        // Keep displaying the distance, until user presses a button
        while (!Button.ESCAPE.isDown() && BroStop == false)
        {
            
            // Get the curRent distnce reading from the US sensor
            distance.fetchSample(sample, 0);
            
            // Display the distance on the LCD screen
            LCD.clear();
            LCD.drawString("Dist: " + sample[0] + " meters", 0, 0);

            //try-1
            //first we have tried while loop 👇
            // while (sample[0] < 0.3 && sample[0] > 0.1)
            // {
            //     Motor.A.setSpeed(100);
            //     Motor.B.setSpeed(100);
            // }
            // if (sample[0] < 0.1){
            //     Motor.A.stop(true);  
            //     Motor.B.stop(true);
            // }

            //try-2
            if (sample[0] < 0.3 && sample[0] > 0.1)
            {
                Motor.A.setSpeed(100);
                Motor.B.setSpeed(100);
                Motor.A.forward();
                Motor.B.forward();
            }

            if (sample[0] <= 0.1)
            {
                Motor.A.stop(true);  
                Motor.B.stop(false);

                LCD.drawString("Returning", 0, 2);

                Motor.A.setSpeed(360);
                Motor.B.setSpeed(360);
                Motor.A.rotate(360, true);
                Motor.B.rotate(-360, false);

                Motor.A.forward();
                Motor.B.forward();
                Delay.msDelay(3000);

                Motor.A.stop(true);
                Motor.B.stop(false);

                BroStop = true;
            }
            // Refresh display every 100 ms
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        
        // Close US sensor
        ultrasonicSensor.close();
        LCD.drawString("I am Back..!!", 0, 4);
        Button.waitForAnyPress();
    }
}