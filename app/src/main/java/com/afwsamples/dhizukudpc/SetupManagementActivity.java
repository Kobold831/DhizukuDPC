package com.afwsamples.dhizukudpc;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SetupManagementActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .add(R.id.container, new SetupManagementFragment(), SetupManagementFragment.FRAGMENT_TAG)
          .commit();
    }
  }
}
