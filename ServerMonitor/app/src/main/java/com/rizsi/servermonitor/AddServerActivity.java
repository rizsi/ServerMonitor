package com.rizsi.servermonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rizsi.servermonitor.data.DataModel;
import com.rizsi.servermonitor.data.ServerEntry;

public class AddServerActivity extends AppCompatActivity {

    public static final String resultKey="serverEntry";
    public static final String resultDeleteIndex="serverDeleteEntry";
    EditText editUrl;
    Button create;
    Button delete;
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server);
        editUrl = (EditText) findViewById(R.id.editUrl);
        create = (Button) findViewById(R.id.buttonCreate);
        delete = (Button) findViewById(R.id.buttonDelete);
        // Step 5: Get the Bundle from the Intent that started this Activity
        delete.setVisibility(View.GONE);
        create.setText("Create server entry...");
        Bundle extras = getIntent().getExtras();
        index=-1;
        if (extras != null) {
            // Step 6: Get the data out of the Bundle
            index=extras.getInt(resultKey)-1;
            if(index>=0)
            {
                ServerEntry se=DataModel.getInstance(getApplicationContext()).get(index);
                editUrl.setText(se.url);
                delete.setVisibility(View.VISIBLE);
                create.setText("Update server entry...");
            }
        }
    }
    public void createServerButton(View view) {
        Intent intent = new Intent();
        ServerEntry se=new ServerEntry(editUrl.getText().toString(), "");
        se.index=index;
        intent.putExtra(resultKey, se);
        setResult(RESULT_OK, intent);
        finish();
    }
    public void deleteEntryButton(View view) {
        Intent intent = new Intent();
        intent.putExtra(resultDeleteIndex, index);
        setResult(RESULT_OK, intent);
        finish();
    }
}