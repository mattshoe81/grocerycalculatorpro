package com.example.matts.grocerycalculatorlite;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity {
    private Button addShoppingItemButton;
    private TextView totalText;
    private EditText itemDescription;
    private LinearLayout listBox;
    public final static String ITEM_DESCRIPTION_TAG = "Item Description";
    public double priceShopping = MainActivity.calculateTotal();
    private List<View> fragmentList = new ArrayList<View>();
    public static final String NUMBER_OF_SHOPPING_FRAGMENTS = "numberOfShoppingFragments";
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        addShoppingItemButton = (Button) findViewById(R.id.addShoppingItemButton);
        totalText = (TextView) findViewById(R.id.totalTextShopping);
        itemDescription = (EditText) findViewById(R.id.itemDescription);
        listBox = (LinearLayout) findViewById(R.id.shoppingItemContainer);

        totalText.setText(MainActivity.currencyFormat.format(MainActivity.calculateTotal()));


    }



    public void onAddShoppingItemClick(View view) {
        if (itemDescription.getText().toString().equals("")) {
            Toast.makeText(this, "You must enter a description", Toast.LENGTH_SHORT).show();
        }

        else {
            Bundle bundle = new Bundle();
            bundle.putString(ITEM_DESCRIPTION_TAG,itemDescription.getText().toString());
            ShoppingItemFragment itemFragment = new ShoppingItemFragment();
            itemFragment.setArguments(bundle);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(listBox.getId(), itemFragment);
            ft.commit();
            itemDescription.setText("");
        }

    }
}
