package com.example.matts.grocerycalculatorlite;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.Space;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.id;
import static com.example.matts.grocerycalculatorlite.R.id.action_set_tax;
import static com.example.matts.grocerycalculatorlite.R.id.settingsButton;

public class  MainActivity extends AppCompatActivity {

    public static Map<Integer,Double> pairList;
    public static ArrayList<CostListFragment> priceListFragmentArray;
    public static int priceKey=0;
    public static double lastPrice;
    private static double total;
    private EditText priceText;
    private EditText qtyText;
    public static TextView totalText;
    public static NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    public static double tax;
    public LinearLayout listBox;
    public static final String tag = "troubleshoot";
    public static SharedPreferences sp;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String TAX_TAG = "TaxPercentage";
    private static final String TOTAL_TAG = "Total";
    private static final String FIRST_TIME_TAG = "FirstTime";
    private TextView qtyLabel;
    private int numberOfOpens;
    public ItemDatabaseHelper dbHelper;
    public static SQLiteDatabase db;
    public static Cursor cursor;
    public boolean onCreateCalled = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        onCreateCalled = true;
        priceListFragmentArray = new ArrayList<CostListFragment>();
        sp = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        numberOfOpens = sp.getInt(FIRST_TIME_TAG, 0);
        tax = Double.parseDouble(sp.getString(TAX_TAG, "0.075"));
        pairList = new HashMap<Integer, Double>();
        priceText = (EditText) findViewById(R.id.foodPriceText);
        priceText.setOnTouchListener(new TouchListener());
        qtyText = (EditText) findViewById(R.id.otherPriceText);
        qtyText.setOnTouchListener(new TouchListener());
        totalText = (TextView) findViewById(R.id.totalText);
        listBox = (LinearLayout) findViewById(R.id.priceListContainer);
        ViewCompat.setElevation(listBox, 10);
        listBox.setOrientation(LinearLayout.VERTICAL);
        qtyLabel = (TextView) findViewById(R.id.qtyLabel);
        qtyLabel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(),"Enter the quantity, or enter the weight of the item if sold by weight", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        //loadPriceListFragments();
        if (numberOfOpens == 0) {
            welcomeUser();
        }
        else {
            promptForRating();
            //askForLastTotal();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates a dialog to prompt the user to rate the app every so often
     */
    public void promptForRating() {
        try {

            // Get the app's shared preferences
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);

            // Get the value for the run counter
            int counter = app_preferences.getInt("counter", 0);

            // Do every x times
            int RunEvery = 15;

            if(counter != 0  && counter % RunEvery == 0 )
            {
                //Toast.makeText(this, "This app has been started " + counter + " times.", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder alert = new AlertDialog.Builder(
                        this);
                alert.setTitle("Thank you so much!");
                alert.setIcon(R.drawable.shoppingcart); //app icon here
                alert.setMessage("We really hope our app has been helpful! \n\nWe'd appreciate your feedback and suggestions for improvements if you've got just a few moments!");

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                //Do nothing
                            }
                        });

                alert.setPositiveButton("Rate it!",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                final String appName = getApplicationContext().getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id="
                                                    + appName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id="
                                                    + appName)));
                                }

                            }
                        });
                alert.show();
            }


            // Increment the counter
            SharedPreferences.Editor editor = app_preferences.edit();
            editor.putInt("counter", ++counter);
            editor.apply(); // Very important

        } catch (Exception e) {
            //Do nothing, don't run but don't break
        }

        createItemList();
    }

    public void createItemList () {
        try {
            dbHelper = new ItemDatabaseHelper(this);
            db = dbHelper.getWritableDatabase();
            String[] column = {ItemDatabaseHelper.ID_COLUMN, ItemDatabaseHelper.PRICE};
            cursor = db.query(ItemDatabaseHelper.PRICE_TABLE, column, null, null, null, null, null);
        }
        catch (SQLiteException e) {
            Toast.makeText(this, "Database Unavailable", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        loadPriceListFragments(cursor);
    }

    /**
     * Open email when users click the email in enenu.
     *
     * @param item clicked email
     */
    public boolean onEmailClick(MenuItem item) {
        // Create the text message with a string
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {this.getString(R.string.email_address)});
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.email_subject));
        sendIntent.setType("text/plain");

        // Verify that the intent will resolve to an activity
        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(sendIntent);
        }

        return true;
    }

    public boolean clearAllMenuButtonClick(MenuItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Clear all items?");
        alert.setMessage("This will permanently delete your total");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (total > 0) {
                    for (int k = 0; k < priceListFragmentArray.size(); k++) {
                        CostListFragment thisFragment = priceListFragmentArray.get(k);
                        if (thisFragment != null) {
                            thisFragment.deleteFragment();
                        }

                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "No Items to Delete", Toast.LENGTH_SHORT).show();
                }
                db.execSQL("DELETE FROM " +ItemDatabaseHelper.PRICE_TABLE);
                setTotalText();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();

        return true;
    }

    public void clearAllFloatingButtonClick(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Clear all items?");
        alert.setMessage("This will permanently delete your total");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (total > 0) {
                    for (int k = 0; k < priceListFragmentArray.size(); k++) {
                        CostListFragment thisFragment = priceListFragmentArray.get(k);
                        if (thisFragment != null) {
                            thisFragment.deleteFragment();
                        }
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "No Items to Delete", Toast.LENGTH_SHORT).show();
                }
                db.execSQL("DELETE FROM " +ItemDatabaseHelper.PRICE_TABLE);
                setTotalText();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();
    }

    public void welcomeUser() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Welcome!");
        alert.setMessage("Would you like to enter the sales tax percentage for your area?");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                settingsMenuButtonClick((MenuItem) findViewById(action_set_tax));
                numberOfOpens++;
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(FIRST_TIME_TAG, numberOfOpens);
                editor.apply();
            }
        });

        alert.setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), "Sales Tax will be defaulted to 7.5%", Toast.LENGTH_LONG).show();
                numberOfOpens++;
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(FIRST_TIME_TAG, numberOfOpens);
                editor.apply();
            }
        });

        alert.show();
    }

    public void loadPriceListFragments(Cursor cursor) {
            cursor.moveToFirst();
            int priceIndex = cursor.getColumnIndex(ItemDatabaseHelper.PRICE);
            int idIndex = cursor.getColumnIndex(ItemDatabaseHelper.ID_COLUMN);
            while (!cursor.isAfterLast()) {
                double itemPrice = cursor.getDouble(priceIndex);

                int id = cursor.getInt(cursor.getColumnIndex(ItemDatabaseHelper.ID_COLUMN));
                Log.d(tag, "Price: " +itemPrice);
                createNewItemFragmentFromDatabase(itemPrice, id);
                cursor.moveToNext();
            }
            setTotalText();
    }

    public void createNewItemFragmentFromDatabase(double priceValue, int id) {
        Log.d(tag, "This id:" +id);
        CostListFragment itemFragment = new CostListFragment();
        itemFragment.initializeFragmentValues(priceValue, id);
        priceListFragmentArray.add(itemFragment);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(listBox.getId(), itemFragment);
        ft.commit();
    }

    public void untaxedButtonClick(View view) {
        if (((priceText.getText().toString().equals("")
                || Double.parseDouble(priceText.getText().toString()) < 0.01
                || qtyText.getText().toString().equals("")
                || Double.parseDouble(qtyText.getText().toString()) <= 0.0))
                || Double.parseDouble(priceText.getText().toString()) * Double.parseDouble(qtyText.getText().toString()) <= 0.01) {
            Toast.makeText(this, "Invalid Entry", Toast.LENGTH_SHORT).show();
        }
        else {
            double thisPrice = Double.parseDouble(priceText.getText().toString());
            double thisQty = Double.parseDouble(qtyText.getText().toString());
            double cost = thisPrice * thisQty;
            addPriceListItem(cost, false);
            priceText.setText("");
            qtyText.setText("1");
            setTotalText();
        }

        priceText.requestFocus();
    }

    public void taxedButtonClick(View view) {

        if (((priceText.getText().toString().equals("")
                || Double.parseDouble(priceText.getText().toString()) < 0.01
                || qtyText.getText().toString().equals("")
                || Double.parseDouble(qtyText.getText().toString()) <= 0.0))
                || Double.parseDouble(priceText.getText().toString()) * Double.parseDouble(qtyText.getText().toString()) <= 0.01) {
            Toast.makeText(this, "Invalid Entry", Toast.LENGTH_SHORT).show();
        }
        else{
            double thisPrice = Double.parseDouble(priceText.getText().toString());
            double thisQty = Double.parseDouble(qtyText.getText().toString());
            double cost = thisPrice * thisQty + (thisPrice * thisQty * tax);
            addPriceListItem(cost, true);
            priceText.setText("");
            qtyText.setText("1");
            setTotalText();
        }

        priceText.requestFocus();
    }

    private void addPriceListItem(double value, boolean taxable) {
        long id = addToDatabase(value, taxable);
        updatePriceMap(value);
        createNewItemFragment(value, id, taxable);
    }

    public int addToDatabase(double value, boolean taxable) {
        double priceValue = value;
        dbHelper.insertItem(db, priceValue);
        String[] column = {ItemDatabaseHelper.ID_COLUMN, ItemDatabaseHelper.PRICE};
        cursor = db.query(ItemDatabaseHelper.PRICE_TABLE, column, null, null, null, null, null);
        cursor.moveToLast();
        int id = cursor.getInt(cursor.getColumnIndex(ItemDatabaseHelper.ID_COLUMN));

        return id;
    }

    private void updatePriceMap(Double value) {
        lastPrice = value;
        pairList.put(priceKey, value);
    }

    private void createNewItemFragment(double priceValue, long id, boolean taxable) {
        CostListFragment itemFragment = new CostListFragment();
        itemFragment.initializeFragmentValues(priceValue, id);
        itemFragment.setTaxable(taxable);
        priceListFragmentArray.add(itemFragment);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(listBox.getId(), itemFragment);
        ft.commit();
    }

    public void setTotalText() {
        double thisTotal = calculateTotal();
        totalText.setText(currencyFormat.format(thisTotal));
        SharedPreferences.Editor editor  = sp.edit();
        editor.putString(TOTAL_TAG, Double.toString(thisTotal));
        editor.apply();
    }

    public static double calculateTotal() {
        total = 0;
        String[] column = {ItemDatabaseHelper.ID_COLUMN, ItemDatabaseHelper.PRICE};
        cursor = db.query(ItemDatabaseHelper.PRICE_TABLE, column, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                total += cursor.getDouble(cursor.getColumnIndex(ItemDatabaseHelper.PRICE));
                cursor.moveToNext();
            }
        }

        return total;
    }

    /**
     * Opens a dialog menu that prompts the user for
     *
     * @param view
     */
    public void clearAllButtonClick(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.tax_help_button, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText inputText = (EditText) promptView.findViewById(R.id.salesTaxValue);
        double currentTaxValue = getCurrentSalesTaxValue() * 100;
        inputText.setText(Double.toString(currentTaxValue));
        alert.setTitle("Sales Tax");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (!inputText.getText().toString().equals("") && Double.parseDouble(inputText.getText().toString()) > 0) {
                    tax = Double.parseDouble(inputText.getText().toString())/100;
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(TAX_TAG, Double.toString(tax));
                    editor.apply();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Invalid Entry", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.setView(promptView);
        alert.show();
    }

    public boolean settingsMenuButtonClick(MenuItem item) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.tax_help_button, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText inputText = (EditText) promptView.findViewById(R.id.salesTaxValue);
        double currentTaxValue = getCurrentSalesTaxValue() * 100;
        inputText.setText(Double.toString(currentTaxValue));
        alert.setTitle("Sales Tax");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (!inputText.getText().toString().equals("") && Double.parseDouble(inputText.getText().toString()) > 0) {
                    tax = Double.parseDouble(inputText.getText().toString())/100;
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(TAX_TAG, Double.toString(tax));
                    editor.apply();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Invalid Entry", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.setView(promptView);
        alert.show();

        return true;
    }

    private double getCurrentSalesTaxValue() {
        return Double.parseDouble(sp.getString(TAX_TAG, "0.075"));
    }

    public void openShoppingList(View view) {
        Toast.makeText(getApplicationContext(), "Shopping List Feature Coming Soon!", Toast.LENGTH_LONG).show();
        //Intent intent = new Intent(this, ShoppingListActivity.class);
        //startActivity(intent);
    }

    public void openTaxCalculator(View view) {
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.taxjar.com/sales-tax-calculator/"));
        startActivity(launchBrowser);
    }

    private class TouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent action) {
            if (action.getAction() == MotionEvent.ACTION_UP) {
                switch(view.getId()) {
                    case R.id.otherPriceText:
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(view, 0);
                        if (getCurrentFocus().getId() != R.id.otherPriceText && getCurrentFocus() != null) {
                            if (qtyText.getText().toString().equals("1")) {
                                qtyText.setText("");
                            }
                            qtyText.requestFocus();
                        }
                        break;
                    case R.id.foodPriceText:
                        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(view, 0);
                        if (qtyText.getText().toString().equals("")) {
                            qtyText.setText("1");
                            view.requestFocus();
                        }
                        else {
                            view.requestFocus();
                        }
                        break;
                }
            }

            if (action.getAction() == MotionEvent.ACTION_DOWN) {
                switch(view.getId()) {
                    case R.id.otherPriceText:
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(view, 0);
                        if (getCurrentFocus().getId() != R.id.otherPriceText && getCurrentFocus() != null) {
                            if (qtyText.getText().toString().equals("1")) {
                                qtyText.setText("");
                            }
                            qtyText.requestFocus();
                        }
                        break;
                    case R.id.foodPriceText:
                        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(view, 0);
                        if (qtyText.getText().toString().equals("")) {
                            qtyText.setText("1");
                            view.requestFocus();
                        }
                        else {
                            view.requestFocus();
                        }
                        break;
                }
            }

            return true;
        }

    }
}
