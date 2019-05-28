package computernotes.computernotes.activities;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import computernotes.computernotes.R;
import computernotes.computernotes.ServerCommunicator;
import computernotes.computernotes.users.CloudUser;

public class SettingsActivity extends AppCompatActivity {

    EditText leftSwipeNoteEditText;
    EditText rightSwipeNoteEditText;
    EditText usernameEditText;
    CheckBox cbBotTB;
    CheckBox cbDrawer;
    EditText botPrio1;
    EditText botPrio2;
    EditText botPrio3;
    EditText botPrio4;
    EditText botPrio5;
    EditText drawPrio1;
    EditText drawPrio2;
    EditText drawPrio3;
    EditText drawPrio4;
    EditText drawPrio5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        leftSwipeNoteEditText = findViewById(R.id.leftSwipeNote);
        rightSwipeNoteEditText = findViewById(R.id.rightSwipeNote);
        usernameEditText = findViewById(R.id.setUsername);

        leftSwipeNoteEditText.setHint("left");
        rightSwipeNoteEditText.setHint("right");
        if(ServerCommunicator.settingsTest.getLeftSwipeNoteString()!=null)
            leftSwipeNoteEditText.setText(ServerCommunicator.settingsTest.getLeftSwipeNoteString());
        if(ServerCommunicator.settingsTest.getRightSwipeNoteString()!=null)
            rightSwipeNoteEditText.setText(ServerCommunicator.settingsTest.getRightSwipeNoteString());
        if (ServerCommunicator.user instanceof CloudUser)
            if (((CloudUser) ServerCommunicator.user).getUsername() != null)
                usernameEditText.setText(((CloudUser) ServerCommunicator.user).getUsername());
            else usernameEditText.setHint("set username");
        else usernameEditText.setHint("upgrade to cloud user first");
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String text = s.toString();
                usernameEditText.setTextColor(Color.WHITE);
                ServerCommunicator.db.collection("users").whereEqualTo("username", text.toLowerCase())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                            if (task.getResult().isEmpty()) {
                                usernameEditText.setTextColor(Color.rgb(34,139,34));
                                Map<String, Object> data = new HashMap<>();
                                data.put("username", text);
                                ServerCommunicator.db.collection("users").document(ServerCommunicator.user.getUid()).update(data);
                                ((CloudUser) ServerCommunicator.user).setUsername(text);
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.getId().equals(ServerCommunicator.user.getUid()))
                                        usernameEditText.setTextColor(Color.GREEN);
                                    else
                                        usernameEditText.setTextColor(Color.RED);
                                }
                            }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cbBotTB = findViewById(R.id.checkBoxBotTB);
        cbBotTB.setChecked(ServerCommunicator.settingsTest.getBottomToolbarActive());

        cbDrawer = findViewById(R.id.checkBoxDrawer);
        cbDrawer.setChecked(ServerCommunicator.settingsTest.getDrawerMenuActive());

        botPrio1 = findViewById(R.id.tbPriority1);
        botPrio2 = findViewById(R.id.tbPriority2);
        botPrio3 = findViewById(R.id.tbPriority3);
        botPrio4 = findViewById(R.id.tbPriority4);
        botPrio5 = findViewById(R.id.tbPriority5);

        drawPrio1 = findViewById(R.id.drawerPrio1);
        drawPrio2 = findViewById(R.id.drawerPrio2);
        drawPrio3 = findViewById(R.id.drawerPrio3);
        drawPrio4 = findViewById(R.id.drawerPrio4);
        drawPrio5 = findViewById(R.id.drawerPrio5);

        if(ServerCommunicator.settingsTest.getBotTBprios().size() != 0 && ServerCommunicator.settingsTest.getDrawerPrios().size() != 0) {
            botPrio1.setText(ServerCommunicator.settingsTest.getBotTBprios().get(0));
            botPrio2.setText(ServerCommunicator.settingsTest.getBotTBprios().get(1));
            botPrio3.setText(ServerCommunicator.settingsTest.getBotTBprios().get(2));
            botPrio4.setText(ServerCommunicator.settingsTest.getBotTBprios().get(3));
            botPrio5.setText(ServerCommunicator.settingsTest.getBotTBprios().get(4));

            drawPrio1.setText(ServerCommunicator.settingsTest.getDrawerPrios().get(0));
            drawPrio2.setText(ServerCommunicator.settingsTest.getDrawerPrios().get(1));
            drawPrio3.setText(ServerCommunicator.settingsTest.getDrawerPrios().get(2));
            drawPrio4.setText(ServerCommunicator.settingsTest.getDrawerPrios().get(3));
            drawPrio5.setText(ServerCommunicator.settingsTest.getDrawerPrios().get(4));
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //back top button
                ServerCommunicator.settingsTest.setLeftSwipeNoteString(leftSwipeNoteEditText.getText().toString());
                ServerCommunicator.settingsTest.setRightSwipeNoteString(rightSwipeNoteEditText.getText().toString());
                ServerCommunicator.settingsTest.setBottomToolbarActive(cbBotTB.isChecked());
                ServerCommunicator.settingsTest.setDrawerMenuActive(cbDrawer.isChecked());
                ServerCommunicator.settingsTest.clearBotPrios();
                ServerCommunicator.settingsTest.addBotTBprio(botPrio1.getText().toString());
                ServerCommunicator.settingsTest.addBotTBprio(botPrio2.getText().toString());
                ServerCommunicator.settingsTest.addBotTBprio(botPrio3.getText().toString());
                ServerCommunicator.settingsTest.addBotTBprio(botPrio4.getText().toString());
                ServerCommunicator.settingsTest.addBotTBprio(botPrio5.getText().toString());
                ServerCommunicator.settingsTest.clearDrawerPrios();
                ServerCommunicator.settingsTest.addDrawerPrio(drawPrio1.getText().toString());
                ServerCommunicator.settingsTest.addDrawerPrio(drawPrio2.getText().toString());
                ServerCommunicator.settingsTest.addDrawerPrio(drawPrio3.getText().toString());
                ServerCommunicator.settingsTest.addDrawerPrio(drawPrio4.getText().toString());
                ServerCommunicator.settingsTest.addDrawerPrio(drawPrio5.getText().toString());

                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
