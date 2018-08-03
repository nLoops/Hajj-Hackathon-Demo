package com.nloops.lossless;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.nloops.lossless.cards.CardsActivity;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.edit_input_camping)
  TextInputEditText myEditInput;
  @BindView(R.id.btn_enter_camping)
  Button mEnterCamping;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    mEnterCamping.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (myEditInput.getText().length() <= 0) {
          Toast.makeText(getApplicationContext(), "Please Enter Your Camping Number",
              Toast.LENGTH_LONG).show();
          return;
        }

        Intent cardIntent = new Intent(MainActivity.this, CardsActivity.class);
        startActivity(cardIntent);
      }
    });
  }
}
