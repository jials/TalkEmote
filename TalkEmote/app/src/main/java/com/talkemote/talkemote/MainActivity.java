package com.talkemote.talkemote;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    // Server IP Address and Port
    private static final String HOST = "http://128.199.130.147/upload.php";

    // 44.1k sample rate
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private static boolean isRecording;
    private String outputFile;
    private ProgressDialog loadingDialog;
    private Handler loadingDialogHandler;

    private ByteArrayOutputStream recData;

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        // Prepare AudioRecord
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sample.wav";
        isRecording = false;

        loadingDialogHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                loadingDialog.dismiss();
                changeView();
            }
        };
    }

    public void btnSpeak(View view) {
        if (isRecording) {
            stopRecording();
            loadingDialog = ProgressDialog.show(MainActivity.this, "", "Analysing...");
            Thread analysingAudioThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO: Send the data to server
                    try {
                        sendAudioFileToServer();
                        Thread.sleep(4000);
                        loadingDialogHandler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "analysingAudio Thread");
            analysingAudioThread.start();
        } else {
            startRecording();
            changeView();
        }
    }

    private void startRecording() {
        final int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void stopRecording() {
        // stops the recording activity
        if (recorder != null) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    // Convert PCM to WAV by adding WAV headers
    private void convertPcmToWav() {
        long mySubChunk1Size = 16;
        int myBitsPerSample = 16;
        int myFormat = 1;
        long myChannels = 1;
        long mySampleRate = 44100;
        long myByteRate = mySampleRate * myChannels * myBitsPerSample/8;
        int myBlockAlign = (int) (myChannels * myBitsPerSample/8);

        byte[] clipData = recData.toByteArray();

        long clipDataSize = recData.size();
        long myChunk2Size =  clipDataSize * myChannels * myBitsPerSample/8;
        long myChunkSize = 36 + myChunk2Size;

        try {
            OutputStream os = new FileOutputStream(new File(outputFile));
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream outFile = new DataOutputStream(bos);

            outFile.writeBytes("RIFF");                                 // 00 - RIFF
            outFile.write(waveIO.intToByteArray((int) myChunkSize), 0, 4);      // 04 - how big is the rest of this file?
            outFile.writeBytes("WAVE");                                 // 08 - WAVE
            outFile.writeBytes("fmt ");                                 // 12 - fmt
            outFile.write(waveIO.intToByteArray((int)mySubChunk1Size), 0, 4);  // 16 - size of this chunk
            outFile.write(waveIO.shortToByteArray((short)myFormat), 0, 2);     // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
            outFile.write(waveIO.shortToByteArray((short)myChannels), 0, 2);   // 22 - mono or stereo? 1 or 2?
            outFile.write(waveIO.intToByteArray((int)mySampleRate), 0, 4);     // 24 - samples per second (numbers per second)
            outFile.write(waveIO.intToByteArray((int)myByteRate), 0, 4);       // 28 - bytes per second
            outFile.write(waveIO.shortToByteArray((short)myBlockAlign), 0, 2); // 32 - # of bytes in one sample, for all channels
            outFile.write(waveIO.shortToByteArray((short)myBitsPerSample), 0, 2);  // 34 - how many bits in a sample(number)?  usually 16 or 24
            outFile.writeBytes("data");                                 // 36 - data
            outFile.write(waveIO.intToByteArray((int)clipDataSize), 0, 4);       // 40 - how big is this data chunk
            outFile.write(clipData);                                    // 44 - the actual data itself - just a long string of numbers

            outFile.flush();
            outFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        recData = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(recData);
        short buffer[] = new short[BufferElements2Rec];

        recorder.startRecording();
        isRecording = true;

        while (isRecording) {
            // gets the voice output from microphone to byte format

            int bytesRead = recorder.read(buffer, 0, BufferElements2Rec);
            try {
                // writes the data to file from buffer
                // stores the voice buffer
                byte bufferData[] = waveIO.shortArrayToByteArray(buffer);
                dos.write(bufferData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        convertPcmToWav();
    }

    private void sendAudioFileToServer() {
        String boundary = "*****";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        File file = new File(outputFile);
        String fileName = "sample.wav";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            URL url = new URL(HOST);
            FileInputStream fis = new FileInputStream(file);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);

            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fis.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fis.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fis.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

            if (serverResponseCode == 200) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                // TODO: receive JSON from server
            }

            fis.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Toggle between recording view and idle view
    // http://stackoverflow.com/questions/4446105/is-it-possible-to-do-transition-animations-when-changing-views-in-the-same-activ
    private void changeView() {
        LayoutInflater inflator = getLayoutInflater();
        View view = inflator.inflate(isRecording?R.layout.activity_main:R.layout.activity_recording, null, false);
        view.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right));
        setContentView(view);
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        intent.putExtra("android.speech.extra.GET_AUDIO", true);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //http://stackoverflow.com/questions/23047433/record-save-audio-from-voice-recognition-intent
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    Uri audioUri = data.getData();
                    ContentResolver contentResolver = getContentResolver();
                    try {
                        InputStream filestream = contentResolver.openInputStream(audioUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}