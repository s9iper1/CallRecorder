package com.byteshatf.callrecorder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshatf.callrecorder.contactpicker.ContactsPicker;
import com.byteshatf.callrecorder.database.DatabaseHelpers;

import java.util.ArrayList;


public class AddRuleActivity extends AppCompatActivity implements View.OnClickListener,
        Switch.OnCheckedChangeListener {

    private ImageButton imageButton;
    private EditText editText;
    private String editTextData;
    private boolean mShowTemporaryCheckedContacts = false;
    private String mCheckedContacts;
    private boolean isStartedFresh = false;
    private Spinner mSpinner;
    private int mSpinnerValue;
    private DatabaseHelpers mDatabaseHelpers;
    private Switch mSwitch;
    private ListView mContactsListView;
    private Helpers mHelpers;
    private ArrayList<String> arrayList = null;
    private String mId = null;
    private TextView mTextViewSwitch;
    private static String sDataFromEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_details);
        isStartedFresh = true;
        mDatabaseHelpers = new DatabaseHelpers(getApplicationContext());
        mHelpers = new Helpers(getApplicationContext());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#006666")));
        mSpinner = (Spinner) findViewById(R.id.spinner_add_fragment);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        mSwitch = (Switch) findViewById(R.id.switch1);
        mSwitch.setOnCheckedChangeListener(this);
        mTextViewSwitch = (TextView) findViewById(R.id.switchTextView);
        mTextViewSwitch.setText("On");
        mTextViewSwitch.setTextColor(Color.parseColor("#229636"));
        editText = (EditText) findViewById(R.id.et_title);
        mContactsListView = (ListView) findViewById(R.id.lv_edit_rule);
        imageButton.setOnClickListener(this);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                String str = s.toString();
                if(str.length() > 0 && str.startsWith(" ")){
                    Log.v("","Cannot begin with space");
                    editText.setText("");
                }else{
                    Log.v("", "Doesn't contain space, good to go!");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        if (getIntent().getExtras() != null) {
            String title = getIntent().getExtras().getString("title", "");
            editText.setText(title);
            sDataFromEditText = title;
            String[] detailsForThisNote = mDatabaseHelpers.retrieveNoteDetails(title);
            mId = detailsForThisNote[0];
            mCheckedContacts = detailsForThisNote[1];
            setTitle("Edit Category");
            mSpinner.setSelection(mHelpers.getValuesFromSharedPreferences(title, 0));
            mSwitch.setChecked(mHelpers.getSwitchState((AppGlobals.sSwitchState + title).trim()));
            if (mSwitch.isChecked()) {
                mTextViewSwitch.setText("On");
                mTextViewSwitch.setTextColor(Color.parseColor("#229636"));
            } else {
                mTextViewSwitch.setText("Off");
                mTextViewSwitch.setTextColor(Color.RED);
            }
            String contacts = mCheckedContacts.replace("[","").replace("]", "");
            String[] items = contacts.split(",");
            arrayList = new ArrayList<>();
            for (String item : items) {
                arrayList.add(item);
            }
            ArrayAdapter<String> arrayAdapter = new FinalizedContacts(getApplicationContext(),
                    R.layout.row, arrayList);
            mContactsListView.setAdapter(arrayAdapter);
            AppGlobals.setUpdateStatus(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AddRuleActivity.this.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton:
                Intent intent = new Intent(getApplicationContext(), ContactsPicker.class);
                intent.putExtra("pre_checked", mCheckedContacts);
                intent.putExtra("temporary_select", mShowTemporaryCheckedContacts);
                mCheckedContacts = null;
                startActivityForResult(intent, AppGlobals.REQUEST_CODE);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_rules_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            mSpinnerValue = mSpinner.getSelectedItemPosition();
            editTextData = editText.getText().toString();
            if (editTextData.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (mCheckedContacts == null) {
                Toast.makeText(getApplicationContext(), "Please select at least one contact",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            System.out.println(AppGlobals.getUpdateStatus());

            if (!editTextData.isEmpty() && editTextData != null && mCheckedContacts != null &&
                    !AppGlobals.getUpdateStatus()) {
                System.out.println("new entry");
                mDatabaseHelpers.createNewEntry(editTextData, arrayList);
                mHelpers.saveValues(editTextData, mSpinnerValue);
                mHelpers.saveSwitchState((AppGlobals.sSwitchState + editTextData).trim(), mSwitch.isChecked());
                AddRuleActivity.this.finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else if (AppGlobals.getUpdateStatus()) {
                System.out.println(mDatabaseHelpers.checkIfItemAlreadyExist(editTextData));
                if (mDatabaseHelpers.checkIfItemAlreadyExist(editTextData) &&
                        !editTextData.equals(sDataFromEditText) ) {
                    Toast.makeText(this, "Rule already exist", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    System.out.println("update");
                    mHelpers.saveValues(editTextData, mSpinnerValue);
                    mHelpers.saveSwitchState((AppGlobals.sSwitchState + editTextData).trim(), mSwitch.isChecked());
                    mDatabaseHelpers.updateCategory(mId, editTextData, arrayList.toString());
                    AppGlobals.setUpdateStatus(false);
                    AddRuleActivity.this.finish();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppGlobals.REQUEST_CODE:
                if (resultCode == AppGlobals.RESULT_OK) {
                    mShowTemporaryCheckedContacts = true;
                    isStartedFresh = false;
                    Bundle extras = data.getExtras();
                    if (extras == null) {
                        System.out.println("OK");
                    } else {
                        mCheckedContacts = extras.getString("selected_contacts");
                        ArrayList<String> mCheckedNames = (ArrayList<String>) extras.get("selected_names");
                        if (mCheckedContacts != null) {
                            mContactsListView.setAdapter(null);
//                            String[] items = mCheckedNames.split(",");
                            arrayList = new ArrayList<>();
                            for (String item : mCheckedNames) {
                                arrayList.add(item);
                            }
                            ArrayAdapter<String> arrayAdapter = new FinalizedContacts(getApplicationContext(),
                                    R.layout.row, arrayList);
                            mContactsListView.setAdapter(arrayAdapter);
                        } else {
                            mCheckedContacts = null;
                            mContactsListView.setAdapter(null);
                        }
                    }
                }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch1:
                if (mSwitch.isChecked()) {
                    mTextViewSwitch.setText("On");
                    mTextViewSwitch.setTextColor(Color.parseColor("#229636"));
                } else {
                    mTextViewSwitch.setText("Off");
                    mTextViewSwitch.setTextColor(Color.RED);
                }
                break;
        }
    }

    class FinalizedContacts extends ArrayAdapter<String> {

        private ArrayList<String> mArrayList;
        public FinalizedContacts(Context context, int resource,
                                 ArrayList<String> arrayList) {
            super(context, resource, arrayList);
            mArrayList = arrayList;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.row, parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String title = mArrayList.get(position);
            holder.title.setText(title);
            return convertView;
        }
        class ViewHolder {
            public TextView title;
        }
    }
}
