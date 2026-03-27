package src;

import lejos.hardware.motor.Motor;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class HelloWorld
{
     public static void main(String[] args)
    {
        // String message1 = "This is my 1st LEGO code.";
        // String message2 = "Make me autonomous";
        // String message3 = "Press any button to Stop.";
        LCD.clear();
        LCD.drawString("Ruhan", 0, 0);

        Delay.msDelay(1000);


        LCD.drawString("This is my 1st",0,1); 
        LCD.drawString("LEGO code.",0,2);

        Delay.msDelay(2000);
        LCD.drawString("Make me autonomous", 0, 3);
        // TextWrap(message3);
        LCD.drawString("Press any button to Stop.", 0,5);
        LCD.drawString("to Stop.", 0,6);
       
        // Wait for a button press to exit
        Button.waitForAnyPress();
    }

    // Robot Movement 👇
    // {
    //     LCD.clear();
    //     LCD.drawString("Movement Test", 0, 0);

    //     Motor.A.setSpeed(360); 
    //     Motor.B.setSpeed(360);

    //     LCD.drawString("Turning...", 0, 2);

    //     Motor.A.rotate(400, true);  
    //     Motor.B.rotate(-400, false); 

    //     Motor.A.forward();
    //     Motor.B.forward();
    //     Delay.msDelay(1000);

    //     LCD.drawString("Returning to base", 0, 4);
    //     Motor.A.stop(true);  
    //     Motor.B.stop(true);

    //     Motor.A.backward();
    //     Motor.B.backward();
    //     Delay.msDelay(1000);

    //     LCD.drawString("Stopping: Brake", 0, 4);
    //     Motor.A.stop(true);  
    //     Motor.B.stop(true);
        
    //     Delay.msDelay(1000);
        
    //     LCD.drawString("Done!", 0, 6);
    //     Button.waitForAnyPress();
    // }
}