package com.example.kkanishkan.clockin;

import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private static final String csvFile = "test.csv";
    private EditText idInput;
    private Button actualSignIn;
    private Button export;
    private TextView currentUser;
    private ArrayList<Employee> employeesList;
    private ArrayList<signedInEmployee> employeesSignedIn;
    private ArrayList<Long> signInList;

    //public long clockedInID;
    //public ArrayList<Employee> master;
    //public NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initalize buttons and Text Field
        actualSignIn = (Button) findViewById(R.id.signIn);
        idInput = (EditText) findViewById(R.id.idField);
        export = (Button) findViewById(R.id.button3);
        //nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        //Have Employees Read in and Ready
        signInList = new ArrayList<>();
        employeesSignedIn = new ArrayList<>();
        employeesList = getJsonEmployeeData();

        //If end of day, write to csv file
        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeEmployeeDataToCSV();
            }
        });


        actualSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText enteredID = (EditText) findViewById(R.id.idField);
                currentUser = (TextView) findViewById(R.id.textView3);
                if(!enteredID.getText().toString().matches("")) {
                    long getID = Long.parseLong(enteredID.getText().toString());
                    //TextView resultView = (TextView) findViewById(R.id.textView3);

                    //Find employee name in array based on array

                    boolean set = false;
                    //Employee temp = new Employee(0, null, null);
                    for (int i = 0; i < employeesList.size(); i++) {
                        if (employeesList.get(i).id == getID) {
                            //temp = employeesList.get(i);
                            //employeesList.set(i, temp);
                            currentUser.setText(employeesList.get(i).toString());
                            Toast.makeText(getApplicationContext(), employeesList.get(i).toString(), Toast.LENGTH_LONG);
                            //resultView.setText(employeesList.get(i).toString());
                            set = true;

                            signedInEmployee temp = new signedInEmployee(getID);
                            if(!signInList.contains(getID)) {
                                signInList.add(getID);

                                Date currentTime = Calendar.getInstance().getTime();
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                temp.signInTime = sdf.format(currentTime);
                                employeesSignedIn.add(temp);

                            } else {
                                for(int j = 0; j < employeesSignedIn.size(); j++) {
                                    if(employeesSignedIn.get(j).id == getID) {
                                        Date currentTime = Calendar.getInstance().getTime();
                                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                        employeesSignedIn.get(j).signOutTime = sdf.format(currentTime);
                                    }
                                }
                            }
                        }
                    }
                    if (!set) {
                        Toast.makeText(getApplicationContext(), "Employee cannot be found", Toast.LENGTH_LONG).show();
                        //resultView.setText("Employee cannot be found");
                    }
                }
            }
        });
    }

    public void writeEmployeeDataToCSV() {
        //Makes output folder
        String myFolder = Environment.getExternalStorageDirectory()+"/Hours";
        File f = new File(myFolder);
        if(!f.exists()){
            if(!f.mkdir()) {
                //Toast.makeText(getApplicationContext(), myFolder + "can't be created", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(getApplicationContext(), myFolder + "can be created", Toast.LENGTH_LONG).show();
            }
        } else {
            //Toast.makeText(getApplicationContext(), myFolder + "Already exists", Toast.LENGTH_LONG).show();
        }

        //Import arraylist data to csv
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String dateF = df.format(c);

        String folderPath = f.getAbsolutePath();
        try {
            File file = new File(folderPath + "/Hours "+ dateF +".csv");
            if(!file.exists()) {
                file.createNewFile();
                Toast.makeText(getApplicationContext(), "File created", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "File updated", Toast.LENGTH_LONG).show();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            CSVWriter cwriter = new CSVWriter(fw);
            //Headers
            String[] header = {"ID", "First Name", "Last Name", "Clock In", "Clock Out"};
            cwriter.writeNext(header);
            String[] data = new String[6];
            int eLength = employeesList.size();
            for(int i = 0; i < eLength; i++) {
                if(signInList.contains(employeesList.get(i).id)) {
                    String str = ""+employeesList.get(i).id;
                    data[0] = str;
                    data[1] = employeesList.get(i).firstName;
                    data[2] = employeesList.get(i).lastName;
                    //Track down employee sign in and sign out time
                    for(int j = 0; j < employeesSignedIn.size(); j++) {
                        if(employeesList.get(i).id == employeesSignedIn.get(j).id) {
                            data[3] = employeesSignedIn.get(j).signInTime;
                            data[4] = employeesSignedIn.get(j).signOutTime;
                        }
                    }
                    cwriter.writeNext(data);
                }
            }
            cwriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Employee> getJsonEmployeeData() {
        //System.out.println("hello there");
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

        //Actually parse the json file as a json, from the string built
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
        //private boolean clockedIn;

        private Employee(long idNum, String first, String last) {
            firstName = first;
            lastName = last;
            id = idNum;
            //clockedIn = false;
        }

        @Override
        public String toString() {
            return "Employee #" + id + ": " + lastName + ", " + firstName;
        }
    }

    public class signedInEmployee {
        private long id;
        private String signInTime;
        private String signOutTime;

        private signedInEmployee(long idNum) {
            id = idNum;
            signInTime = null;
            signOutTime = null;
        }
    }

}
