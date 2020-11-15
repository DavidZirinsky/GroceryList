package com.example.grocerylist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private HashMap<String, String> groceryList = new HashMap<>();
    private List<String> listOfCategories = new ArrayList<>();
    private String filename = "saveData";

    @Override
    protected void onStop() {
        EditText appText = findViewById(R.id.main_text);
        Editable text = appText.getText();
        String saveValue = Html.toHtml(text);
        Save(filename, saveValue);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText appText = findViewById(R.id.main_text);
        Spanned content = Html.fromHtml(Open(filename));
        appText.setText(content);
        FloatingActionButton fab = findViewById(R.id.fab);

        generateValuesForMap();

        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                String text = String.valueOf(appText.getText());
                String[] words = text.split("\n");

                HashMap<String, ArrayList<String>> whatToWrite = new HashMap<>();
                List<String> leftovers = new ArrayList<>();
                for(String word: words){
                    // applesx2 fixes
                    String lookupWord = word.split(" ")[0];
                    lookupWord = lookupWord.replaceAll("x(.*)", "");


                    if(groceryList.containsKey(lookupWord)){
                        String category = groceryList.get(lookupWord);
                        if (whatToWrite.containsKey(category)){
                           whatToWrite.get(category).add(word);
                        }
                        else{
                            ArrayList<String> newList = new ArrayList<>();
                            newList.add(word);
                            whatToWrite.put(category, newList);
                        }
                    }
                    // avoids appending all the categories at end of list after
                    // pressing button multiple times
                    else if(listOfCategories.contains(word)){
                        continue;
                    }
                    else{
                        leftovers.add(word);
                    }
                }

                StringBuilder output = new StringBuilder();
                whatToWrite.forEach((k, v) -> {
                    output.append( "<p><b>" + k + "</b></p>");
                    for(String str: v){
                        output.append(str + "<br>");
                    }
                });
                output.append("<br>");

                for(String str: leftovers){
                    // if statement avoids adding a bunch of blank lines at the end of the file
                    if(!str.equals(" ") && !str.equals("") && !str.equals("\n")) {
                        output.append(str + "<br>");
                    }
                }
                appText.setText(Html.fromHtml(output.toString()));
            }
        });

    }

    public void Save(String fileName, String output) {
        try {
            OutputStreamWriter out =
                    new OutputStreamWriter(openFileOutput(fileName, 0));
            out.write(output);
            out.close();
            Toast.makeText(this, "Note Saved!", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public String Open(String fileName) {
        String content = "";
        if (FileExists(fileName)) {
            try {
                InputStream in = openFileInput(fileName);
                if ( in != null) {
                    InputStreamReader tmp = new InputStreamReader( in );
                    BufferedReader reader = new BufferedReader(tmp);
                    String str;
                    StringBuilder buf = new StringBuilder();
                    while ((str = reader.readLine()) != null) {
                        buf.append(str + "\n");
                    } in .close();
                    content = buf.toString();
                }
            } catch (java.io.FileNotFoundException e) {} catch (Throwable t) {
                Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
            }
        }
        return content;
    }

    public boolean FileExists(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    private void generateValuesForMap(){
        InputStream inputStream = getResources().openRawResource(R.raw.categories);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                try{
                    this.groceryList.put(row[0], row[1]);
                    if(!this.listOfCategories.contains(row[1])){
                        listOfCategories.add(row[1]);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }

            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }
    }

}