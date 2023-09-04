package jp.ac.jec.cm0129.funnycamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int mode = 1; // Default mode is set to 1
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 123;

    private TextView choiceMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choiceMode = findViewById(R.id.choidMode);

        if (checkCameraPermission()) {
            //startCameraActivity();
        } else {
            requestCameraPermission();
        }

        Spinner spn = findViewById(R.id.spinner);
        String[] modeOptions = {"NormalMode", "CharacterMode", "FrameMode","Edit Text Mode"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, modeOptions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // Set padding for the view (adjust the padding values as needed)
                int paddingLeftRight = 16; // in pixels
                int paddingTopBottom = 8; // in pixels
                view.setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);

                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);

                // Set padding for the drop-down view (adjust the padding values as needed)
                int paddingLeftRight = 16; // in pixels
                int paddingTopBottom = 8; // in pixels
                view.setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);

                return view;
            }
        };
        spn.setAdapter(adapter);

        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // Update the 'mode' variable based on the selected item in the spinner
                switch (position) {
                    case 0:
                        mode = 1; // NormalMode
                        break;
                    case 1:
                        mode = 2; // CharacterMode
                        break;
                    case 2:
                        mode = 3; // FrameMode
                        break;
                    case 3:
                        mode = 4; // Edit Text Mode
                        break;
                    default:
                        // Handle any other cases if needed
                        break;
                }

                // Update the choiceMode TextView with the selected mode text
                choiceMode.setText("Selected Mode: " + modeOptions[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case when nothing is selected in the spinner
            }
        });

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("mode", mode);
                Log.i("MainActivity", "MODE " + mode);
                startActivity(intent);

            }
        });

    }
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void startCameraActivity() {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra("mode", mode);
        startActivity(intent);
        finish(); // Optional: Close the MainActivity if you don't need it anymore
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //startCameraActivity();
            } else {
                // Handle the case when permission is denied
                showPermissionDeniedDialog();
            }
        }
    }
    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Camera Permission Denied")
                .setMessage("This app needs access to your Camera. Please go to the app settings and grant the required permission.")
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open app settings so the user can grant the location permission.
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                //.setNegativeButton("Cancel", null)
                .show();
    }
}
