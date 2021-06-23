package kah.sample;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kah.sample.R;

public class ApiActivity extends AppCompatActivity {
    String food;
    EditText et;
    TextView tv1, tv2,tv3, tv4, tv5, tv6, tv7;
    View v1, v2;
    DataBaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("음식 검색");

        db = new DataBaseHelper(this);

        et = (EditText)findViewById(R.id.editText);
        tv1 = (TextView)findViewById(R.id.textView13);
        tv2 = (TextView)findViewById(R.id.textView16);
        tv3 = (TextView)findViewById(R.id.textView17);
        tv4 = (TextView)findViewById(R.id.textView18);
        tv5 = (TextView)findViewById(R.id.textView19);
        tv6 = (TextView)findViewById(R.id.textView20);
        tv7 = (TextView)findViewById(R.id.textView21);
    }


    public void calculateClk(View view) {
        food = et.getText().toString();
        Toast.makeText(this, "검색 중...", Toast.LENGTH_SHORT).show();
        new MyAsyncTask().execute();
    }


    private class MyAsyncTask extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... strings) {

            String allStrings;
            try{
                URL myUrl = new URL("https://api.nutritionix.com/v1_1/search/" +
                        food +"?fields=item_name%2Citem_id%2Cnf_calories%2Cnf_total_fat%2Cnf_protein%2Cnf_total_carbohydrate" +
                        "&appId=3fe5fa47&appKey=61729b9d2d8612a629467f0cdbbd6d2c");
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();
                connection.setConnectTimeout(700);
                connection.connect();

                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);

                String inputLine;
                StringBuilder stringBuilder = new StringBuilder();
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                reader.close();
                streamReader.close();
                allStrings = stringBuilder.toString();
                publishProgress(allStrings);

            }catch(Exception e){}
            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            try {
                JSONObject j = new  JSONObject(values[0]);

                JSONArray h= (JSONArray) j.get("hits");

                JSONObject rec = h.getJSONObject(0);

                JSONObject fields = rec.getJSONObject("fields");


                String name = fields.getString("item_name");
                double calories = fields.getDouble("nf_calories");
                double protein = fields.getDouble("nf_protein");
                double fat = fields.getDouble("nf_total_fat");
                double carb = fields.getDouble("nf_total_carbohydrate");


                tv2.setText("영양소 성분");
                tv3.setText(name);
                tv4.setText("칼로리: " +calories);
                tv5.setText("단백질: " + protein);
                tv6.setText("지방: " + fat);
                tv7.setText("탄수화물: " + carb);

                v1 = findViewById(R.id.view);
                v1.setVisibility(View.VISIBLE);
                v2 = findViewById(R.id.view);
                v2.setVisibility(View.VISIBLE);

                saves(name, calories, protein, fat, carb);



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }


    String name = null;
    double calories = 0;
    double protein = 0;
    double fat = 0;
    double carb = 0;

    public void saves(String names, double caloriess, double proteins, double fats, double carbs){
        name = names;
        calories = caloriess;
        protein = proteins;
        fat = fats;
        carb = carbs;
    }


    public void Set(View view){
        int Calorie, Protein, Fat, Carb, weight;
        Calorie = (int)calories;
        Protein = (int)protein;
        Fat = (int)fat;
        Carb = (int)carb;

        weight = db.getWeight("0");

        String date = getDate();
        if (db.dateExists(date)) {
            updateFood(date, Calorie, Protein, Fat, Carb);
            db.addFoodHistory(date, name, calories, protein, fat, carb);
            startActivity(new Intent(ApiActivity.this, DailyPage.class));
        }
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

}
