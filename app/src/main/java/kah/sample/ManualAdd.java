

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
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kah.sample.R;

public class ManualAdd extends AppCompatActivity {

    private DataBaseHelper db;
    private EditText editFood;
    private EditText editCalories;
    private EditText editProtein;
    private EditText editFat;
    private EditText editCarbs;
    private Button saveButton;

    private String foodName;
    private int calories=0, protein=0, fat=0, carbohydrates=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_add);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("커스텀 음식 추가");

        editFood = (EditText) findViewById(R.id.editFood);
        editCalories = (EditText) findViewById(R.id.editCalories);
        editProtein = (EditText) findViewById(R.id.editProtein);
        editFat = (EditText) findViewById(R.id.editFat);
        editCarbs = (EditText) findViewById(R.id.editCarbs);
        saveButton = (Button) findViewById(R.id.saveButton);

        db = new DataBaseHelper(this);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = getDate();


                foodName = editFood.getText().toString();
                calories = Integer.parseInt(editCalories.getText().toString());
                protein = Integer.parseInt(editProtein.getText().toString());
                fat = Integer.parseInt(editFat.getText().toString());
                carbohydrates = Integer.parseInt(editCarbs.getText().toString());

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    builder = new AlertDialog.Builder(ManualAdd.this, android.R.style.Theme_Material_Dialog_Alert);
                else
                    builder = new AlertDialog.Builder(ManualAdd.this);

                builder.setTitle(foodName + "을(를) 저장하시겠습니까?")
                        .setMessage("칼로리: " + calories + "\n단백질: " + protein + " g\n지방: " + fat + " g\n탄수화물: " + carbohydrates + " g")
                        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String date = getDate();
                                if (db.dateExists(date)) {
                                    updateFood(date, calories, protein, fat, carbohydrates);
                                    db.addFoodHistory(date, foodName, calories, protein, fat, carbohydrates);
                                    startActivity(new Intent(ManualAdd.this, DailyPage.class));
                                }
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
    }

    public void updateFood (String date, int calories, int protein, int fat, int carbohydrates) {
        int Calories, Protein, Fat, Carbohydrates, Weight;
        Calories = calories;
        Protein= protein;
        Fat = fat;
        Carbohydrates = carbohydrates;
        Weight = db.getWeight("0");

        Calories += db.getCalories(date);
        Protein += db.getProtein(date);
        Fat += db.getFat(date);
        Carbohydrates += db.getCarbs(date);

        db.updateNutrition(date, Calories, Protein, Fat, Carbohydrates, Weight);
    }

    public String getDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), DailyPage.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
