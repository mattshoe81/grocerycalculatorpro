package com.example.matts.grocerycalculatorlite;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.LinkAddress;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.matts.grocerycalculatorlite.MainActivity.currencyFormat;
import static com.example.matts.grocerycalculatorlite.MainActivity.pairList;
import static com.example.matts.grocerycalculatorlite.MainActivity.tax;

/**
 * Created by matts on 8/15/2017.
 */

public class ShoppingItemFragment extends Fragment {
    private ImageView deleteButton;
    private CheckBox checkBox;
    private String itemDescription;
    private TextView totalTextMain;
    private TextView totalTextShopping;
    private final int UNIQUE_KEY = MainActivity.priceKey;
    private LinearLayout mainListBox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.shopping_list_item_fragment, container, false);

        MainActivity.priceKey = MainActivity.priceKey + 1;
        deleteButton = (ImageView) rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new DeleteButtonListener());
        checkBox = (CheckBox) rootView.findViewById(R.id.checkBox);
        checkBox.setChecked(false);
        checkBox.setOnCheckedChangeListener(new CheckBoxListener());
        itemDescription = getArguments().getString(ShoppingListActivity.ITEM_DESCRIPTION_TAG);
        checkBox.setText(itemDescription);
        Log.d("troubleshoot", itemDescription);
        totalTextMain = (TextView) getActivity().findViewById(R.id.totalText);
        totalTextShopping = (TextView) getActivity().findViewById(R.id.totalTextShopping);
        mainListBox = (LinearLayout) getActivity().findViewById(R.id.priceListContainer);

        return rootView;
    }

    private void removeFragment() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.remove(ShoppingItemFragment.this);
        ft.commit();
    }

    private class DeleteButtonListener implements View.OnClickListener {
        public void onClick(View view) {
            removeFragment();
        }
    }



    private void setTotalText() {
        totalTextShopping.setText(currencyFormat.format(MainActivity.calculateTotal()));
    }

    private class CheckBoxListener implements CheckBox.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton checkedBox, boolean checked) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            LinearLayout layout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(LinearLayout.SHOW_DIVIDER_MIDDLE);
            final CheckBox checkBox = new CheckBox(getActivity());
            final EditText inputText = new EditText(getActivity());
            final EditText qtyText = new EditText(getActivity());
            final TextView multiplyText = new TextView(getActivity());
            multiplyText.setText("X");
            alert.setTitle("Enter Price");
            alert.setMessage("Item: " +itemDescription);
            qtyText.setHint("Qty/Wgt");
            qtyText.setText("1");
            checkBox.setChecked(false);
            checkBox.setText("Tax");
            qtyText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            qtyText.setRawInputType(Configuration.KEYBOARD_12KEY);
            inputText.setHint("Enter Price");
            inputText.setLayoutParams(params);
            inputText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            inputText.setRawInputType(Configuration.KEYBOARD_12KEY);
            layout.addView(inputText);
            layout.addView(multiplyText);
            layout.addView(qtyText);
            layout.addView(checkBox);
            alert.setView(layout);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    boolean calculateTax = checkBox.isChecked();
                    if (!inputText.getText().toString().equals("") && Double.parseDouble(inputText.getText().toString()) >= 0.01) {
                        if (calculateTax) {
                            double price = Double.parseDouble(inputText.getText().toString())*Double.parseDouble(qtyText.getText().toString()) +Double.parseDouble(inputText.getText().toString())*Double.parseDouble(qtyText.getText().toString()) * tax;
                            pairList.put(UNIQUE_KEY, price);
                            removeFragment();
                            setTotalText();
                        }
                        else {
                            double price = Double.parseDouble(inputText.getText().toString()) * Double.parseDouble(qtyText.getText().toString());
                            pairList.put(UNIQUE_KEY, price);
                            removeFragment();
                            setTotalText();
                        }
                    }
                    else {
                        Toast.makeText(getActivity(),"Invalid Request", Toast.LENGTH_SHORT).show();
                        checkBox.setChecked(false);
                    }
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    checkBox.setChecked(false);
                }
            });
            alert.show();
        }
    }
}
