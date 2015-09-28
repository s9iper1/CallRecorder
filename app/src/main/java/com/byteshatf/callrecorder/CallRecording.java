package com.byteshatf.callrecorder;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class CallRecording {

    public static boolean isRecording = false;
    private MediaRecorder mediaRecorder;

    public void startRecord() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setAudioEncodingBitRate(96000);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(AppGlobals.getDataDirectory("CallRec") + "/"
                + AppGlobals.sCurrentNumber + "_" + Helpers.getTimeStamp() + ".aac");
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
        Log.i(AppGlobals.getLogTag(getClass()), "Recording started.....");
        isRecording = true;
    }

    public void stopRecording() {
         if (CallRecording.isRecording) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }
    }
}