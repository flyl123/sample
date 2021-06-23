
package kah.sample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="DailyNutrition.db";

    private static final String TABLE_NUTRITION="DailyNutrition";
    private static final String KEY_DATE="Date";
    private static final String KEY_CALORIES="Calories";
    private static final String KEY_PROTEIN="Protein";
    private static final String KEY_FAT="Fat";
    private static final String KEY_CARBS="Carbohydrates";
    private static final String KEY_WEIGHT="Weight";

    private static final String TABLE_FOOD="FoodHistory";
    private static final String KEY_FOOD="Food";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_NUTRITION_TABLE="CREATE TABLE " + TABLE_NUTRITION + "("
                + KEY_DATE + " STRING PRIMARY KEY NOT NULL UNIQUE,"
                + KEY_CALORIES + " INT DEFAULT 0,"
                + KEY_PROTEIN + " INT DEFAULT 0,"
                + KEY_FAT + " INT DEFAULT 0,"
                + KEY_CARBS + " INT DEFAULT 0,"
                + KEY_WEIGHT + " INT DEFAULT 0"
                + ")";

        String CREATE_FOOD_TABLE="CREATE TABLE " + TABLE_FOOD + "("
                + KEY_DATE + " STRING NOT NULL,"
                + KEY_FOOD + " STRING,"
                + KEY_CALORIES + " INT DEFAULT 0,"
                + KEY_PROTEIN + " INT DEFAULT 0,"
                + KEY_FAT + " INT DEFAULT 0,"
                + KEY_CARBS + " INT DEFAULT 0"
                + ")";

        sqLiteDatabase.execSQL(CREATE_NUTRITION_TABLE);
        sqLiteDatabase.execSQL(CREATE_FOOD_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NUTRITION);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
        onCreate(sqLiteDatabase);
    }

    public boolean foodExists (String food) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FOOD, new String[]{KEY_FOOD},
                KEY_FOOD + " =?",
                new String[]{food}, null, null, null, null);

        if (cursor.getCount()>0)
            return true;
        else
            return false;
    }

    public Food getFood (String foodName) {
        Food food = new Food();
        SQLiteDatabase db= this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_FOOD, new String[]{KEY_FOOD, KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS},
                KEY_FOOD + " =?",
                new String[]{foodName}, null, null, null, null);
        if (cursor!=null) {
            cursor.moveToFirst();
            food = new Food(cursor.getString(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4));
        }
        return food;
    }

    public void addFoodHistory (String date, String food, double calories, double protein, double fat, double carbohydrates) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put(KEY_DATE, date);
        values.put(KEY_FOOD, food);
        values.put(KEY_CALORIES, calories);
        values.put(KEY_PROTEIN, protein);
        values.put(KEY_FAT, fat);
        values.put(KEY_CARBS, carbohydrates);

        db.insert(TABLE_FOOD, null, values);
        db.close();
    }


    public void updateWeight(String date, int weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put(KEY_WEIGHT, weight);

        String date_number= String.valueOf(date);
        db.update(TABLE_NUTRITION, values, KEY_DATE + " =?", new String[]{"0"});
        db.update(TABLE_NUTRITION, values, KEY_DATE + " =?", new String[]{date_number});

        db.close();
    }

    public ArrayList <String> getDateHistory (){
        SQLiteDatabase db=this.getWritableDatabase();
        ArrayList <String> dates = new ArrayList<String>();
        Cursor  cursor = db.rawQuery("select Date from " + TABLE_NUTRITION,null);
        if (cursor!=null)
            cursor.moveToFirst();

        for (int i=0; i<cursor.getCount(); i++) {
            String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
            dates.add(date);
            cursor.moveToNext();
        }
        return dates;
    }


    public ArrayList <Food> getFoodHistory (String date) {
        ArrayList <Food> foods = new ArrayList<Food>();

        SQLiteDatabase db= this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FOOD, new String[]{KEY_FOOD, KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null)
            cursor.moveToFirst();

        for (int i=0; i<cursor.getCount(); i++){
            String foodName;
            int calories=0,  protein=0, fat=0, carbs=0;

            foodName = cursor.getString(0);
            calories = cursor.getInt(1);
            protein = cursor.getInt(2);
            fat = cursor.getInt(3);
            carbs = cursor.getInt(4);

            Food food = new Food(foodName,calories,protein,fat,carbs);
            foods.add(food);

            cursor.moveToNext();
        }
        return foods;
    }


    public void addEntry(String date) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put(KEY_DATE, date);
        values.put(KEY_CALORIES, 0);
        values.put(KEY_PROTEIN, 0);
        values.put(KEY_FAT, 0);
        values.put(KEY_CARBS, 0);
        values.put(KEY_WEIGHT, 0);

        db.insert(TABLE_NUTRITION, null, values);
        db.close();
    }

    public int updateNutrition(String date, int calories, int protein, int fat, int carbs, int weight) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put(KEY_CALORIES, calories);
        values.put(KEY_PROTEIN, protein);
        values.put(KEY_FAT, fat);
        values.put(KEY_CARBS, carbs);
        values.put(KEY_WEIGHT, weight);

        String date_number= String.valueOf(date);
        return db.update(TABLE_NUTRITION, values, KEY_DATE + " =?", new String[]{date_number});
    }


    public void deleteEntry(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NUTRITION, KEY_DATE + " =?", new String[]{String.valueOf(date)});
        db.close();
    }
    public void deletefood(String food) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOOD, KEY_FOOD + " =?", new String[]{String.valueOf(food)});
        db.close();
    }

    public boolean dateExists (String date) {
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null) {
            if (cursor.getCount() <= 0) {
                cursor.close();
                db.close();
                return false;
            }
        }
        cursor.close();
        db.close();
        return true;
    }

    public int getCalories(String date) {
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null)
            cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int getProtein(String date) {
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null)
            cursor.moveToFirst();
        return cursor.getInt(1);
    }

    public int getFat(String date) {
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null)
            cursor.moveToFirst();
        return cursor.getInt(2);
    }

    public int getCarbs(String date) {
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null)
            cursor.moveToFirst();
        return cursor.getInt(3);
    }

    public int getWeight(String date) {
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null)
            cursor.moveToFirst();
        return cursor.getInt(4);
    }

    public class Food {

        public Food () {
            this.name="";
            this.calories=0;
            this.protein=0;
            this.fat=0;
            this.carbs=0;
        }
        public Food ( String name, int calories, int protein, int fat, int carbs ) {
            this.name = name;
            this.calories = calories;
            this.protein = protein;
            this.fat = fat;
            this.carbs = carbs;
        }

        public String name;
        public int calories;
        public int protein;
        public int fat;
        public int carbs;
    }
}
