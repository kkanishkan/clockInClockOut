package com.example.kkanishkan.clockin;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    public EditText idInput;
    public Button signInButton;
    public Button signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initalize buttons and Text Field
        signInButton = (Button) findViewById(R.id.inButton);
        signOutButton = (Button) findViewById(R.id.outButton);
        idInput = (EditText) findViewById(R.id.idField);

        //Disable in and out button
        //signInButton.setEnabled(false);
        signOutButton.setEnabled(false);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText enteredID = (EditText) findViewById(R.id.idField);
                long getID = Long.parseLong(enteredID.getText().toString());
                TextView resultView = (TextView) findViewById(R.id.textView3);

                //Find employee name in array based on array

                boolean set = false;
                ArrayList<Employee> employeesList = getJsonEmployeeData();
                for(int i = 0; i < employeesList.size(); i++) {
                    if(employeesList.get(i).id == getID) {
                        resultView.setText(employeesList.get(i).toString());
                        set = true;
                    }
                }
                if(!set) {
                    resultView.setText("Employee cannot be found");
                }
            }});
    }

    public ArrayList<Employee> getJsonEmployeeData() {
        System.out.println("hello there");
        ArrayList<Employee> employees = new ArrayList<>();

        AssetManager assetFiles = getAssets();
        String s = "";

        try {
            InputStream is = assetFiles.open("employeeData");
            Scanner scanner = new Scanner(is);
            StringBuilder builder = new StringBuilder();

            while(scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }

            s = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Actually parse the json file as a json, from the string we built
        StringBuilder builder = new StringBuilder();
        try {
            JSONObject root = new JSONObject(s);
            JSONArray employeeList = root.getJSONArray("Employees");
            int arrLength = employeeList.length();

            for(int i = 0; i < arrLength; i++) {
                JSONObject curr = employeeList.getJSONObject(i);
                long currID = curr.getLong("ID");
                String currFirst = curr.getString("First");
                String currLast = curr.getString("Last");
                Employee e = new Employee(currID, currFirst, currLast);
                employees.add(e);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return employees;
    }

    private class Employee {
        private String firstName;
        private String lastName;
        private long id;

        private Employee(long idNum, String first, String last) {
            firstName = first;
            lastName = last;
            id = idNum;
        }

        @Override
        public String toString() {
            return "Employee #" + id + ": " + lastName + ", " + firstName;
        }
    }

}
