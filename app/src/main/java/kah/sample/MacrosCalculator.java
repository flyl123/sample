
package kah.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import kah.sample.R;

public class MacrosCalculator extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private DataBaseHelper db;
    private EditText editAge;
    private EditText editWeight;
    private EditText editHeight;
    private Button calculateButton;
    private Spinner activitySpinner;
    private Spinner genderSpinner;
    private Spinner goalSpinner;

    TextView result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_macros_calculator);
        setTitle("TDEE 계산기");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        editAge = (EditText) findViewById(R.id.editAge);
        editWeight = (EditText) findViewById(R.id.editWeight);
        editHeight = (EditText) findViewById(R.id.editHeight);
        calculateButton = (Button) findViewById(R.id.calculateButton);
        activitySpinner = (Spinner) findViewById(R.id.activeSpinner);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        goalSpinner = (Spinner) findViewById(R.id.goalSpinner);
        result = (TextView) findViewById(R.id.resultText);

        db = new DataBaseHelper(this);

        activitySpinner.setOnItemSelectedListener( MacrosCalculator.this);
        genderSpinner.setOnItemSelectedListener( MacrosCalculator.this);

        ArrayList<String> activityCategories = new ArrayList<String>();
        activityCategories.add("( 운동량 )");
        activityCategories.add("거의 움직이지 않음");
        activityCategories.add("가벼운 활동량");
        activityCategories.add("많은 활동량");

        ArrayList<String> genderCategories = new ArrayList<String>();
        genderCategories.add("( 성별 )");
        genderCategories.add("남성");
        genderCategories.add("여성");

        ArrayList<String> goalCategories = new ArrayList<String>();
        goalCategories.add("( 목표 선택 )");
        goalCategories.add("감량");
        goalCategories.add("유지");
        goalCategories.add("증량");

        ArrayAdapter<String> activityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, activityCategories);
        activitySpinner.setAdapter(activityAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genderCategories);
        genderSpinner.setAdapter(genderAdapter);

        ArrayAdapter<String> goalAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, goalCategories);
        goalSpinner.setAdapter(goalAdapter);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int age=0;
                double weight=0, height=0, feet=0, inches=0;
                if(editAge.getText().toString().length()>0)
                    age=Integer.parseInt(editAge.getText().toString());
                if(editWeight.getText().toString().length()>0)
                    weight = Integer.parseInt(editWeight.getText().toString());
                if(editHeight.getText().length()>0)
                    height=Integer.parseInt(editHeight.getText().toString());

                int activityPosition=activitySpinner.getSelectedItemPosition();
                int genderPosition=genderSpinner.getSelectedItemPosition();
                int goalPosition=goalSpinner.getSelectedItemPosition();

                double TDEE=0, activeLevel=0, calories=0;

                if (activityPosition==1) activeLevel = 1.2;
                else if (activityPosition==2) activeLevel = 1.375;
                else if (activityPosition==3) activeLevel = 1.725;

                if (activityPosition==1) activeLevel = 1.2;
                else if (activityPosition==2) activeLevel = 1.375;
                else if (activityPosition==3) activeLevel = 1.725;

                if (genderPosition==1) {
                    double maleRDEE = (10 * weight * 0.45359237) + (6.25 * height) - (5 * age) + 5;
                    TDEE=maleRDEE*activeLevel;
                }
                if (genderPosition==2){
                    double femaleRDEE =  (10 * weight * 0.45359237) + (6.25 * height) - (5 * age) -161;
                    TDEE=femaleRDEE*activeLevel;
                }

                if (goalPosition==1) calories = TDEE - (TDEE*.20);
                else if (goalPosition==2) calories = TDEE;
                else if (goalPosition==3) calories = TDEE + (TDEE*.20);

                double protein = weight * 0.825;
                double fat = (calories* 0.25)/9 ;
                double carbohydrates = (calories - (protein + fat))/4;

                final int Calories = (int) calories;
                final int Protein = (int) Math.round(protein);
                final int Fat = (int) Math.round(fat);
                final int Carbohydrates = (int) Math.round(carbohydrates);
                final int Weight = (int) weight;

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    builder = new AlertDialog.Builder(view.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                else
                    builder = new AlertDialog.Builder(view.getContext());

                builder.setTitle("영양소 목표")
                        .setMessage("칼로리: " + Calories +
                        "\n프로틴: " + Protein + " g\n" + "지방: " + Fat + " g\n" + "탄수화물: " + Carbohydrates + " g")
                        .setNegativeButton("저장", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (!db.dateExists("0")) {
                                    db.addEntry("0");
                                    db.updateNutrition("0", Calories, Protein, Fat, Carbohydrates, Weight);
                                }
                                else {
                                    db.updateNutrition("0", Calories, Protein, Fat, Carbohydrates, Weight);
                                }
                                startActivity(new Intent(MacrosCalculator.this, DailyPage.class));
                            }
                        })
                        .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), DailyPage.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
