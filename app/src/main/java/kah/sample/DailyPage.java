
package kah.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import kah.sample.R;

public class DailyPage extends AppCompatActivity {

    private TextView valueGoal;
    private TextView valueFood;
    private TextView valueRemaining;
    private TextView valueProtein;
    private TextView valueFat;
    private TextView valueCarbs;
    private TextView valueWeight;

    private ListView foodList;

    private String date;
    int dailyProtein, dailyFat, dailyCarbs, dailyWeight;
    DataBaseHelper db;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.history:
                    startActivity(new Intent(DailyPage.this, History.class));
                    return true;
                case R.id.add_food:
                    addFoodDialogue();
                    return true;
                case R.id.nutrition_calculator:
                    startActivity(new Intent(DailyPage.this, MacrosCalculator.class));
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_page);
        setTitle(getDate());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        foodList = (ListView) findViewById(R.id.foodList);

        valueGoal = (TextView) findViewById(R.id.valueGoal);
        valueFood = (TextView) findViewById(R.id.valueFood);
        valueRemaining = (TextView) findViewById(R.id.valueRemaining);
        valueProtein = (TextView) findViewById(R.id.valueProtein);
        valueFat = (TextView) findViewById(R.id.valueFat);
        valueCarbs = (TextView) findViewById(R.id.valueCarbs);
        valueWeight = (TextView) findViewById(R.id.valueWeight);

        db = new DataBaseHelper(this);

        date = getDate();
        if (!db.dateExists(date))
            db.addEntry(date);


        int defaultCalories = db.getCalories("0");
        int foodCalories = db.getCalories(date);
        final int caloriesLeft = defaultCalories - foodCalories;

        valueGoal.setText(String.valueOf(defaultCalories));
        valueFood.setText(String.valueOf(foodCalories));
        valueRemaining.setText(String.valueOf(caloriesLeft));
        if (caloriesLeft < 0)
            valueRemaining.setTextColor(Color.RED);
        else
            valueRemaining.setTextColor(Color.GREEN);

        int defaultProtein, defaultFat, defaultCarbs, defaultWeight;
        defaultProtein = db.getProtein("0");
        defaultFat = db.getFat("0");
        defaultCarbs = db.getCarbs("0");
        defaultWeight = db.getWeight("0");

        dailyProtein = db.getProtein(date);
        dailyFat = db.getFat(date);
        dailyCarbs = db.getCarbs(date);
        dailyWeight = db.getWeight(date);



        valueProtein.setText(dailyProtein + " / " + defaultProtein + " g");
        valueFat.setText(dailyFat + " / " + defaultFat + " g");
        valueCarbs.setText(dailyCarbs + " / " + defaultCarbs + " g");
        valueWeight.setText(defaultWeight + " Kg");

        if (dailyProtein > defaultProtein) valueProtein.setTextColor(Color.RED);
        else valueProtein.setTextColor(Color.GREEN);

        if (dailyFat > defaultFat) valueFat.setTextColor(Color.RED);
        else valueFat.setTextColor(Color.GREEN);

        if (dailyCarbs > defaultCarbs) valueCarbs.setTextColor(Color.RED);
        else valueCarbs.setTextColor(Color.GREEN);

        final ArrayList<DataBaseHelper.Food> history = db.getFoodHistory(date);

        ArrayList<String> foods = new ArrayList<String>();
        for (int i = 0; i < history.size(); i++) {
            foods.add(history.get(i).name);
        }

        final ArrayAdapter<String> foodAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, foods);
        foodList.setAdapter(foodAdapter);

        foodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String selectedItem = history.get(position).name;

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    builder = new AlertDialog.Builder(DailyPage.this, android.R.style.Theme_Material_Dialog_Alert);
                else
                    builder = new AlertDialog.Builder(DailyPage.this);

                builder.setTitle(selectedItem + "의 영양소 정보")
                        .setMessage("칼로리: " + String.valueOf(history.get(position).calories)
                                + "\n단백질: " + String.valueOf(history.get(position).protein)
                                + " g\n지방: " + String.valueOf(history.get(position).fat)
                                + " g\n탄수화물: " + String.valueOf(history.get(position).carbs)
                                + " g")
                        .setNegativeButton("완료", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                int foodCalories2 = db.getCalories(date);
                                foodCalories2 -= history.get(position).calories;
                                dailyProtein -= history.get(position).protein;
                                dailyFat -= history.get(position).fat;
                                dailyCarbs -= history.get(position).carbs;
                                db.updateNutrition(date, foodCalories2, dailyProtein, dailyFat, dailyCarbs, dailyWeight);
                                db.deletefood(selectedItem);
                                startActivity(new Intent(DailyPage.this, DailyPage.class));
                            }

                        }).show();
            }
        });
    }


    public String getDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    public void addFoodDialogue() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(DailyPage.this, android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(DailyPage.this);


        builder.setTitle("음식 추가")
                .setMessage("음식을 검색하거나 직접 등록")
                .setNegativeButton("검색", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(DailyPage.this, ApiActivity.class));
                    }
                })

                .setPositiveButton("직접 등록", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(DailyPage.this, ManualAdd.class));
                    }
                }).show();
    }


    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Homepage.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
