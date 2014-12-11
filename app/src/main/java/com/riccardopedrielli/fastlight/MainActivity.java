package com.riccardopedrielli.fastlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;

import java.io.IOException;
import java.util.List;

public class MainActivity
        extends Activity
        implements SurfaceHolder.Callback,
                   Camera.AutoFocusCallback,
                   DialogInterface.OnClickListener
{
    private final Handler handler = new Handler();

    private ImageButton mPowerButton = null;

    private Camera mCamera = null;

    private SurfaceHolder mHolder = null;

    private boolean mInitOK = false;

    public void surfaceChanged(SurfaceHolder holder, int i, int i1, int i2) {}

    public void surfaceCreated(SurfaceHolder holder) {}

    public void surfaceDestroyed(SurfaceHolder holder) {}

    @Override
    public void onAutoFocus(boolean b, Camera camera) {}

    public void onClick(DialogInterface dialog, int which)
    {
        // Close the app after the user clicks OK in the error dialog
        finish();
    }

    public void toggleFlash(@SuppressWarnings("unused") View view)
    {
        setFlash(!isFlashOn());
    }

    private void log(String msg)
    {
        if (BuildConfig.DEBUG)
        {
            Log.d(getString(R.string.app_name), msg);
        }
    }

    private void showAlert(int msgId)
    {
        showAlert(getString(msgId));
    }

    private void showAlert(String msg)
    {
        if (BuildConfig.DEBUG)
        {
            Log.e(getString(R.string.app_name), msg);
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.error);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton(R.string.ok, this);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private boolean isFlashOn()
    {
        try
        {
            return mCamera.getParameters().getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH);
        }
        catch (Exception e)
        {
            showAlert(getString(R.string.flash_check_error, e.getMessage()));
            return false;
        }
    }

    private void setFlash(boolean on)
    {
        if (!mInitOK)
        {
            return;
        }

        try
        {
            Camera.Parameters parameters = mCamera.getParameters();

            if (on)
            {
                try
                {
                    mCamera.setPreviewDisplay(mHolder);
                }
                catch (IOException e)
                {
                    showAlert(getString(R.string.no_camera_display, e.getMessage()));
                    return;
                }

                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                mCamera.autoFocus(this);
            }
            else
            {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                mCamera.stopPreview();
            }
        }
        catch (Exception e)
        {
            showAlert(getString(R.string.flash_set_error, e.getMessage()));
        }
        finally
        {
            updatePowerButton();
        }
    }

    private void updatePowerButton()
    {
        if (!mInitOK)
        {
            return;
        }

        if (isFlashOn())
        {
            mPowerButton.setImageResource(R.drawable.ic_power_on);
        }
        else
        {
            mPowerButton.setImageResource(R.drawable.ic_power_off);
        }
    }

    private boolean init()
    {
        // Check system features
        PackageManager pm = getPackageManager();

        if (pm == null)
        {
            showAlert(R.string.no_packagemanager);
            return false;
        }

        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
            showAlert(R.string.no_feature_camera);
            return false;
        }

        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS))
        {
            showAlert(R.string.no_feature_autofocus);
            return false;
        }

        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
        {
            showAlert(R.string.no_feature_flash);
            return false;
        }

        // Initialize componenets
        if (mPowerButton == null)
        {
            log("INIT mPowerButton");

            mPowerButton = (ImageButton) findViewById(R.id.PowerButton);
        }
        else
        {
            log("mPowerButton OK");
        }

        if (mHolder == null)
        {
            log("INIT mHolder");

            SurfaceView preview = (SurfaceView) findViewById(R.id.CameraView);
            mHolder = preview.getHolder();

            if (mHolder == null)
            {
                showAlert(R.string.no_init);
                return false;
            }
            else
            {
                mHolder.addCallback(this);
            }
        }
        else
        {
            log("mHolder OK");
        }

        if (mCamera == null)
        {
            log("INIT mCamera");

            mCamera = Camera.open();

            if (mCamera == null)
            {
                showAlert(R.string.no_camera_control);
                return false;
            }
        }
        else
        {
            log("mCamera OK");
        }

        // Check camera features
        log("Check camera features");

        List<String> focusModes = mCamera.getParameters().getSupportedFocusModes();

        if (focusModes == null || !focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
        {
            showAlert(R.string.no_camera_autofocus);
            return false;
        }

        List<String> flashModes = mCamera.getParameters().getSupportedFlashModes();

        if (flashModes == null || !flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH))
        {
            showAlert(R.string.no_camera_flash);
            return false;
        }

        log("Check camera features OK");

        return true;
    }

    private void cleanup()
    {
        if(mCamera != null)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        log("onCreate");

        if (getResources().getBoolean(R.bool.portrait_only))
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_main);

        mInitOK = init();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        log("onStart");
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        log("onResume");

        if (getIntent().getBooleanExtra("LightOn", false))
        {
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    setFlash(true);
                }
            }, 100);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        log("onPause");

        setFlash(false);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        log("onStop");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        log("onDestroy");

        cleanup();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        log("onRestart");
    }
}
