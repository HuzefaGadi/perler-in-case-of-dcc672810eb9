package com.esspro.ico;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.sql.DataSource;


public class CameraActivity extends Activity implements SurfaceHolder.Callback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    private static final float MAX_PICTURE_SIZE = 2.5f;    // megapixels
    private static final String TAG = "CameraActivity";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    String username = "incaseofapp@gmail.com";
    String password = "Q%66x#C855a&";
    Button settings;
    String fileNames[];
    StringBuilder st;

    SharedPreferences preferences;

    private double altitude = 0, latitude = 0, longitude = 0;

    boolean mPreviewRunning = false;
    private Context mContext = this;
    Camera mCamera;
    int countOfPicturesTaken;
    Timer timer;
    AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            altitude = extras.getDouble("altitude", 0.0);
            latitude = extras.getDouble("latitude", 0.0);
            longitude = extras.getDouble("longitude", 0.0);
        }
        buildGoogleApiClient();
        st = new StringBuilder();
        countOfPicturesTaken = 0;
        fileNames = new String[10];
        preferences = getSharedPreferences(com.esspro.ico.InCaseOfApp.PREFERENCES, MODE_PRIVATE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        String emailAddress = preferences.getString(com.esspro.ico.InCaseOfApp.SHOOTING_EMAIL_ADDRESS, "");


        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        AlertDialog.Builder alertDialogBuilderForResetPassword = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.editText1);
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // get user input and set it to result
                // edit text
                String userPassword = userInput.getText().toString();
                String password = preferences.getString(com.esspro.ico.InCaseOfApp.PASSWORD, "000000");

                if (userPassword.equals(password)) {
                    Intent intent = new Intent(CameraActivity.this, com.esspro.ico.SettingsActivity.class);
                    startActivity(intent);
                    alertDialog.hide();
                } else {
                    Toast.makeText(mContext, "Incorrect Password", Toast.LENGTH_LONG).show();
                }

                userInput.setText("");
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        userInput.setText("");
                        alertDialog.hide();

                    }
                });

        // create alert dialog
        alertDialog = alertDialogBuilder.create();
        settings = (Button) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });


        if (emailAddress.isEmpty()) {
            Toast.makeText(this, "Please provide email addresses and other factors to continue", Toast.LENGTH_LONG).show();
            settings.performClick();
        } else {
            mSurfaceView = (SurfaceView) findViewById(R.id.camera_surface);
//		mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				mCamera.takePicture(null, null, mPictureCallback);	// shutter, raw, jpg
//				return false;
//			}
//		});

            mSurfaceHolder = mSurfaceView.getHolder();
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            final int delay = preferences.getInt(com.esspro.ico.InCaseOfApp.SHOOTING_INTERVAL, 2);

            timer = new Timer();
            timer.scheduleAtFixedRate(task, delay * 1000, delay * 1 * 1000);

        }


    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (countOfPicturesTaken == 9) {
                        timer.cancel();
                    }
                    if (mCamera != null) {

                        mCamera.takePicture(null, null, mPictureCallback);    // shutter, raw, jpg
                    }
                }
            });
        }
    };

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.

    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        com.esspro.ico.InCaseOfApp.camera_done = System.currentTimeMillis();
    }

    String deg2DMS(double coord) {
        coord = coord > 0 ? coord : -coord; // -105.9876543 -> 105.9876543
        String result = Integer.toString((int) coord) + "/1,"; // 105/1,
        coord = (coord % 1) * 60; // .987654321 * 60 = 59.259258
        result += Integer.toString((int) coord) + "/1,"; // 105/1,59/1,
        coord = (coord % 1) * 60000; // .259258 * 60000 = 15555
        result += Integer.toString((int) coord) + "/1000"; // 105/1,59/1,15555/1000
        return result;
    }

    Location getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        return locationManager.getLastKnownLocation(bestProvider);
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] imageData, Camera c) {
            mCamera.startPreview();
            mPreviewRunning = true;

            if (imageData != null) {
                Log.i(TAG, "image length: " + imageData.length);
                //Toast.makeText(mContext, "image length: " + imageData.length, Toast.LENGTH_LONG).show();
                //mCamera.startPreview();

                String filename = String.format("%d.jpg", System.currentTimeMillis());
                FileOutputStream fos;
                try {
                    fos = openFileOutput(filename, Context.MODE_PRIVATE);
                    fos.write(imageData);
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, filename + " taken: " + imageData.length);
                Toast.makeText(CameraActivity.this, filename, Toast.LENGTH_SHORT).show();

                Location loc = getLocation();
                if (loc != null) {
                    Log.d(TAG, "got location from " + loc.getProvider() + ", lat: " + loc.getLatitude() +
                            " lon: " + loc.getLongitude() + " acc: " + loc.getAccuracy());
                    try {
                        ExifInterface exif = new ExifInterface(com.esspro.ico.InCaseOfApp.getContext().getFileStreamPath(filename).getAbsolutePath());
                        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, deg2DMS(loc.getLatitude()));
                        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, deg2DMS(loc.getLongitude()));
                        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, loc.getLatitude() > 0 ? "N" : "S");
                        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, loc.getLongitude() > 0 ? "E" : "W");
                        exif.saveAttributes();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                String latitude = "NOT FOUND";
                String longitude = "NOT FOUND";
                if (mLastLocation != null) {
                    latitude = String.valueOf(mLastLocation.getLatitude());
                    longitude = String.valueOf(mLastLocation.getLongitude());
                } else {
                    //Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
                }
                if (countOfPicturesTaken == 3) {
                    st = new StringBuilder();
                }
                st.append(filename + ":" + "http://maps.google.com/?ie=UTF&hq=&II=" + latitude + "," + longitude);
                st.append("\n");

                fileNames[countOfPicturesTaken] = filename;

                if (countOfPicturesTaken == 2) {
                    sendMail("Pictures Taken", st.toString(), fileNames);
                } else if (countOfPicturesTaken == 9) {

                    sendMail("Pictures Taken", st.toString(), fileNames);
                }

                countOfPicturesTaken++;
//				Intent intent = new Intent();
//				intent.putExtra( "IMAGE_FILE", filename);
//				setResult( RESULT_OK, intent);
//				finish();
            } else {
                Toast.makeText(mContext, "image data is NULL", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void surfaceCreated(SurfaceHolder holder) {
//		Log.e(TAG, "surfaceCreated");
        try {
            mCamera = Camera.open();
        } catch (RuntimeException e) {
            e.getStackTrace();
            setResult(RESULT_FIRST_USER, new Intent());
            finish();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.e(TAG, "surfaceChanged: " + w + "x" + h);
        if (mCamera == null) return;

        // XXX stopPreview() will crash if preview is not running
        if (mPreviewRunning) {
            mCamera.stopPreview();
        }


        Camera.Parameters p = mCamera.getParameters();
        Log.i(TAG, "camera params:" + p.flatten());
        p.setPictureFormat(ImageFormat.JPEG);
//		if( w > h ) {
//			p.setPreviewSize(w, h);
//			p.setRotation(0);
//		} else {
//			p.setPreviewSize(h, w);
//			p.setRotation(90);
//		}
        int flash = preferences.getInt(com.esspro.ico.InCaseOfApp.SHOOTING_FLASH, 0);
        switch (flash) {
            case 1:
                p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                break;
            case 2:
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case 0:
                p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            default:
                p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }
        p.setRotation(90);    // -- landscape?
        p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        //p.setFlashMode(use_flash ? Camera.Parameters.FLASH_MODE_ON : Camera.Parameters.FLASH_MODE_OFF);
//		p.set("flash-mode", Camera.Parameters.FLASH_MODE_OFF);	// "auto" ?
        final int exposure = preferences.getInt(com.esspro.ico.InCaseOfApp.SHOOTING_EXPOSURE, 0);
        if (exposure == 1) {
            p.setExposureCompensation(p.getMinExposureCompensation() / 2);
        }
        if (exposure == 2) {
            p.setExposureCompensation(p.getMaxExposureCompensation() / 2);
        }

        List<Size> psizes = p.getSupportedPictureSizes();
        int current_size = 0;
        for (int i = 0; i < psizes.size(); i++) {
            Size size = psizes.get(i);
            int total_size = size.height * size.width;
            if (total_size < MAX_PICTURE_SIZE * 1024 * 1024 && total_size > current_size) {
                current_size = total_size;
                p.setPictureSize(size.width, size.height);
                Log.i(TAG, "used   :" + size.width + "x" + size.height);
            } else {
                Log.i(TAG, "ignored:" + size.width + "x" + size.height);
            }
        }
        //Log.i(TAG, "jpeg quality:" + p.getJpegQuality());
        if (latitude > 10.0 && longitude > 10.0) {
            p.setGpsAltitude(altitude);
            p.setGpsLatitude(latitude);
            p.setGpsLongitude(longitude);
            p.setGpsTimestamp(System.currentTimeMillis() / 1000);
        }
        try {
            mCamera.setParameters(p);
            mCamera.setDisplayOrientation(90);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        mPreviewRunning = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
//		Log.e(TAG, "surfaceDestroyed");
        if (mCamera == null) return;

        if (mPreviewRunning) {
            mCamera.stopPreview();
        }
        mPreviewRunning = false;
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera = null;
    }

    private void sendMail(String subject, String messageBody, String[] imagePaths) {
        Session session = createSessionObject();

        try {

            Message message = createMessage(subject, messageBody, session);
            Multipart multipart = new MimeMultipart();
            int size = imagePaths.length;
            MimeBodyPart messageText = new MimeBodyPart();
            messageText.setText(messageBody);
            multipart.addBodyPart(messageText);
            int i = 0;
            if (countOfPicturesTaken >= 3) {
                i = 3;
            }
            for (; i <= countOfPicturesTaken; i++) {

                MimeBodyPart messageBodyPart = new MimeBodyPart();
                String file = getApplicationContext().getFileStreamPath(imagePaths[i]).getAbsolutePath();
                String fileName = imagePaths[i];
                FileDataSource source = new FileDataSource(file);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(fileName);
                multipart.addBodyPart(messageBodyPart);
            }

            message.setContent(multipart);

            new SendMailTask().execute(message);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private Message createMessage(String subject, String messageBody, Session session) throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username, "InCaseOfApp"));
        InternetAddress[] addresses = InternetAddress.parse(preferences.getString(com.esspro.ico.InCaseOfApp.SHOOTING_EMAIL_ADDRESS, ""));
        System.out.println("Message" + messageBody);
        message.addRecipients(Message.RecipientType.TO, addresses);
        message.setSubject(subject);

        message.setText(messageBody);


        return message;
    }

    private Session createSessionObject() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        return Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private class SendMailTask extends AsyncTask<Message, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
                System.out.println("SENT MESSAGE");
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
