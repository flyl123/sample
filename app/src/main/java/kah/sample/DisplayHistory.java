

package kah.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kah.sample.R;

public class DisplayHistory extends AppCompatActivity {

    private DataBaseHelper db;
    private TextView valueGoal;
    private TextView valueFood;
    private TextView valueRemaining;
    private TextView valueProtein;
    private TextView valueFat;
    private TextView valueCarbs;
    private TextView valueWeight;

    private ListView foodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_history);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        foodList = (ListView) findViewById(R.id.foodList);
        valueGoal = (TextView) findViewById(R.id.valueGoal);
        valueFood = (TextView) findViewById(R.id.valueFood);
        valueRemaining = (TextView) findViewById(R.id.valueRemaining);
        valueProtein = (TextView) findViewById(R.id.valueProtein);
        valueFat = (TextView) findViewById(R.id.valueFat);
        valueCarbs = (TextView) findViewById(R.id.valueCarbs);
        valueWeight = (TextView) findViewById(R.id.valueWeight);

        db = new DataBaseHelper(this);

        final Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        setTitle("Summary for " + date);

        if (!db.dateExists(date))
            db.addEntry(date);

        int defaultCalories = db.getCalories("0");
        int foodCalories = db.getCalories(date);
        int caloriesLeft = defaultCalories - foodCalories;

        Button button = findViewById(R.id.deletebtn);

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                db.deleteEntry(intent.getStringExtra("date"));
                startActivity(new Intent(DisplayHistory.this, DailyPage.class));
            }
        });

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

        int dailyProtein, dailyFat, dailyCarbs, dailyWeight;
        dailyProtein = db.getProtein(date);
        dailyFat = db.getFat(date);
        dailyCarbs = db.getCarbs(date);
        dailyWeight = db.getWeight(date);

        valueProtein.setText(dailyProtein + " / " + defaultProtein + " g");
        valueFat.setText(dailyFat + " / " + defaultFat + " g");
        valueCarbs.setText(dailyCarbs + " / " + defaultCarbs + " g");
        valueWeight.setText(dailyWeight + " Kg");

        if (dailyProtein > defaultProtein) valueProtein.setTextColor(Color.RED);
        else valueProtein.setTextColor(Color.GREEN);

        if (dailyFat > defaultFat) valueFat.setTextColor(Color.RED);
        else valueFat.setTextColor(Color.GREEN);

        if (dailyCarbs > defaultCarbs) valueCarbs.setTextColor(Color.RED);
        else valueCarbs.setTextColor(Color.GREEN);

        final ArrayList<DataBaseHelper.Food> history = db.getFoodHistory(date);

        ArrayList<String> foods = new ArrayList<String>();
        for (int i=0; i<history.size(); i++){
            foods.add(history.get(i).name);
        }

        ArrayAdapter<String> foodAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, foods);
        foodList.setAdapter(foodAdapter);

        foodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String selectedItem = history.get(position).name;

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    builder = new AlertDialog.Builder(DisplayHistory.this, android.R.style.Theme_Material_Dialog_Alert);
                else
                    builder = new AlertDialog.Builder(DisplayHistory.this);

                builder.setTitle("Nutrition for " + selectedItem)
                        .setMessage("칼로리: " + String.valueOf(history.get(position).calories)
                                + "\n단백질: " + String.valueOf(history.get(position).protein)
                                + " g\n지방: " + String.valueOf(history.get(position).fat)
                                + " g\n탄수화물: " + String.valueOf(history.get(position).carbs)
                                + " g" )
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), History.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}