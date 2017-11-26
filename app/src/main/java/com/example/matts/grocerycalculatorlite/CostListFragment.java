package com.example.matts.grocerycalculatorlite;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import org.w3c.dom.Text;

import static android.R.attr.fragment;
import static android.R.attr.id;
import static android.content.Context.MODE_PRIVATE;
import static com.example.matts.grocerycalculatorlite.ItemDatabaseHelper.PRICE;
import static com.example.matts.grocerycalculatorlite.MainActivity.PREFS_NAME;
import static com.example.matts.grocerycalculatorlite.MainActivity.calculateTotal;
import static com.example.matts.grocerycalculatorlite.MainActivity.currencyFormat;
import static com.example.matts.grocerycalculatorlite.MainActivity.db;
import static com.example.matts.grocerycalculatorlite.MainActivity.lastPrice;
import static com.example.matts.grocerycalculatorlite.MainActivity.pairList;
import static com.example.matts.grocerycalculatorlite.MainActivity.sp;
import static com.example.matts.grocerycalculatorlite.MainActivity.tax;
import static com.example.matts.grocerycalculatorlite.R.id.center_horizontal;
import static com.example.matts.grocerycalculatorlite.R.id.totalText;

public class CostListFragment extends Fragment{
    private View rootView;
    private TextView itemTextView;
    public double price;
    public long UNIQUE_KEY = 0;
    public boolean taxable = false;
    private TextView totalText;
    private FragmentManager fragmentManager;

    public void initializeFragmentValues(double priceValue, long key) {
        setPriceValue(priceValue);
        UNIQUE_KEY = key;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.cost_list_fragment, container, false);

        incrementPriceKey();
        createTextView();
        setPriceText(currencyFormat.format(price));
        initializeRemoveButton();
        initializeEditButton();
        totalText = (TextView) getActivity().findViewById(R.id.totalText);
        fragmentManager = getActivity().getSupportFragmentManager();

        return rootView;
    }

    private void setTotalText() {
        double thisTotal = MainActivity.calculateTotal();
        totalText.setText(currencyFormat.format(thisTotal));
    }

    private void initializeRemoveButton() {
        Button removeButton = (Button) rootView.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new RemoveButtonClickListener());
    }

    private void initializeEditButton() {
        Button editButton = (Button) rootView.findViewById(R.id.editButton);
        editButton.setOnClickListener(new EditButtonClickListener());
    }

    private void createTextView() {
        itemTextView = (TextView) rootView.findViewById(R.id.itemText);
    }

    private void incrementPriceKey() {
        MainActivity.priceKey = MainActivity.priceKey + 1;
    }

    private void setPriceText(String text) {
        itemTextView.setText(text);
    }

    private void setPriceValue(double value) {
        value = value * 100.0;
        value = Math.round(value);
        value = value / 100;
        price = value;
    }

    public void deleteFragment() {
        deleteFromDatabase();
        removeFragment();
        removePriceValueFromPairList();
        setTotalText();
    }

    public void deleteFromDatabase() {
        MainActivity.db.delete(ItemDatabaseHelper.PRICE_TABLE, ItemDatabaseHelper.ID_COLUMN +"=?", new String[]{Long.toString(UNIQUE_KEY)});
    }

    public void setTaxable(boolean taxBoolean) {
        taxable = taxBoolean;
    }

    public void removeFragment() {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.remove(CostListFragment.this);
        ft.commit();

    }

    private void removePriceValueFromPairList() {
        pairList.remove(CostListFragment.this.UNIQUE_KEY);
    }

    private class RemoveButtonClickListener implements View.OnClickListener {
        public void onClick(View view) {
            deleteFragment();
        }
    }

    private class EditButtonClickListener implements View.OnClickListener {
        public void onClick(View view) {
            setRetainInstance(true);
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            LinearLayout layout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(400, ViewGroup.LayoutParams.WRAP_CONTENT);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(LinearLayout.SHOW_DIVIDER_MIDDLE);
            final CheckBox checkBox = new CheckBox(getActivity());
            final EditText inputText = new EditText(getActivity());
            inputText.setText(Double.toString(price));
            alert.setTitle("Enter New Value");
            alert.setMessage("for item: " +currencyFormat.format(price));
            checkBox.setChecked(false);
            checkBox.setText("Tax");
            inputText.setHint("New Value");
            inputText.setLayoutParams(params);
            inputText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            inputText.setRawInputType(Configuration.KEYBOARD_12KEY);
            layout.addView(inputText);
            layout.addView(checkBox);
            alert.setView(layout);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    boolean calculateTax = checkBox.isChecked();
                    if (!inputText.getText().toString().equals("") && Double.parseDouble(inputText.getText().toString()) >= 0.01) {
                        if (calculateTax) {
                            double newPrice = Double.parseDouble(inputText.getText().toString()) +Double.parseDouble(inputText.getText().toString()) * tax;
                            setPriceValue(newPrice);
                            ContentValues content = new ContentValues();
                            content.put(PRICE, newPrice);
                            db.update(ItemDatabaseHelper.PRICE_TABLE, content, ItemDatabaseHelper.ID_COLUMN +"=" +UNIQUE_KEY, null);
                            itemTextView.setText(currencyFormat.format(price));
                            setTotalText();
                        }
                        else {
                            double newPrice = Double.parseDouble(inputText.getText().toString());
                            ContentValues content = new ContentValues();
                            content.put(PRICE, newPrice);
                            db.update(ItemDatabaseHelper.PRICE_TABLE, content, ItemDatabaseHelper.ID_COLUMN +"=" +UNIQUE_KEY, null);
                            setPriceValue(Double.parseDouble(inputText.getText().toString()));
                            itemTextView.setText(currencyFormat.format(price));
                            setTotalText();
                        }
                    }
                    else {
                        Toast.makeText(getActivity(),"Invalid Request", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });
            alert.show();

        }
    }
}
