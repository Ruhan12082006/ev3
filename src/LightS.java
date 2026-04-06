package src;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.Button;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class LightS
{
    public static void main(String[] args)
    {
        EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S4);
        SampleProvider light = colorSensor.getAmbientMode();
        
        // Create an array to hold the sensor data
        float[] sample = new float[light.sampleSize()];
        
        // Continuously display the light intensity until a button is pressed
        while (!Button.ESCAPE.isDown())                 // Exit if the ESCAPE button is pressed
        {
            // Get the current light intensity reading from the sensor
            light.fetchSample(sample, 0);               // 0 is the index where data will be stored
            
            // Display the light intensity value on the LCD screen
            LCD.clear();
            LCD.drawString("Light Intensity: " + (int)(sample[0] * 100) + "%", 0, 0);  // Display as percentage
            
            int color = colorSensor.getColorID();

            if(color == Color.BLACK)
            {
                Motor.A.setSpeed(300);  
                Motor.B.setSpeed(300);
                Motor.A.forward();
                Motor.B.forward(); 
            }
            else
            {
                Motor.A.stop();
                Motor.B.stop();
            }

            try 
            {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        colorSensor.close();
    }
}