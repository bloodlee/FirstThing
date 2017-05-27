package com.innoli.firstthing;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private Gpio ledGpio;
  private ButtonInputDriver btnInputDriver;

  private Handler handler = new Handler();

  final Runnable blinkRunnable = new Runnable() {
    @Override
    public void run() {
      if (ledGpio == null) {
        return;
      }

      try {
        ledGpio.setValue(!ledGpio.getValue());

        handler.postDelayed(blinkRunnable, 1000);
      } catch (IOException e) {
        Log.e(TAG, "Error on PeripheralIO API", e);
      }

      //handler.postDelayed()
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    PeripheralManagerService service = new PeripheralManagerService();
    Log.d(TAG, "Available GPIO: " + service.getGpioList());

    Log.i(TAG, "Configuring GPIO pins");

    try {
      ledGpio = service.openGpio("BCM6");
      ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

      Log.i(TAG, "Registering button driver");

      btnInputDriver = new ButtonInputDriver("BCM21", Button.LogicState.PRESSED_WHEN_LOW, KeyEvent.KEYCODE_SPACE);
      // btnInputDriver.register();

      handler.post(blinkRunnable);
    } catch (IOException e) {
      Log.e(TAG, "Error configuring GPIO pins", e);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    Log.i(TAG, "Key Down " + keyCode);

    if (keyCode == KeyEvent.KEYCODE_SPACE) {
      setLedValue(true);
      return true;
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    Log.i(TAG, "Key Up " + keyCode);

    if (keyCode == KeyEvent.KEYCODE_SPACE) {
      setLedValue(false);
      return true;
    }

    return super.onKeyUp(keyCode, event);
  }

  private void setLedValue(boolean value) {
    try {
      ledGpio.setValue(value);
    } catch (IOException e) {
      Log.e(TAG, "Error update GPIO value", e);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (btnInputDriver != null) {
      btnInputDriver.unregister();
      try {
        btnInputDriver.close();
      } catch (IOException e) {
        Log.e(TAG, "Error closing Button driver", e);
      } finally{
        btnInputDriver = null;
      }
    }

    if (ledGpio != null) {
      try {
        ledGpio.close();
      } catch (IOException e) {
        Log.e(TAG, "Error closing LED GPIO", e);
      } finally{
        ledGpio = null;
      }
      ledGpio = null;
    }
  }

  //  @Override
//  public boolean onCreateOptionsMenu(Menu menu) {
//    // Inflate the menu; this adds items to the action bar if it is present.
//    getMenuInflater().inflate(R.menu.menu_main, menu);
//    return true;
//  }
//
//  @Override
//  public boolean onOptionsItemSelected(MenuItem item) {
//    // Handle action bar item clicks here. The action bar will
//    // automatically handle clicks on the Home/Up button, so long
//    // as you specify a parent activity in AndroidManifest.xml.
//    int id = item.getItemId();
//
//    //noinspection SimplifiableIfStatement
//    if (id == R.id.action_settings) {
//      return true;
//    }
//
//    return super.onOptionsItemSelected(item);
//  }
}
