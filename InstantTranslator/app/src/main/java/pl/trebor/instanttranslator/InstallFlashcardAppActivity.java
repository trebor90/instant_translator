package pl.trebor.instanttranslator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class InstallFlashcardAppActivity extends AppCompatActivity {

    private Button installAnkiBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_flashcard_app);
        installAnkiBtn = (Button) findViewById(R.id.install_btn);
        installAnkiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAnkiDroidInMarket();
            }
        });
    }

    private void openAnkiDroidInMarket() {
        Intent ankiDroidMarketIntent = new Intent(Intent.ACTION_VIEW);
        ankiDroidMarketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ankiDroidMarketIntent.setData(Uri.parse("market://details?id=com.ichi2.anki"));
        startActivity(ankiDroidMarketIntent);
    }
}
