package org.example;

import org.example.view.OfficeEmployee;

import java.awt.*;

public class App 
{
    public static void main( String[] args )
    {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                OfficeEmployee employee = new OfficeEmployee();
                employee.createView();
            }
        });
    }
}
