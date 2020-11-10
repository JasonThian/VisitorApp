package com.example.visitorapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class GetQrCode extends AppCompatActivity {

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private String qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_qr_code);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        previewView = findViewById(R.id.activity_main_previewView);
        // Enables Always-on
        startCamera();
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                Toast.makeText(getApplicationContext(),"Starting camera",Toast.LENGTH_SHORT).show();
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                GetQrCode.this.bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(GetQrCode.this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
        Preview preview = new Preview.Builder()
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setTargetResolution(new Size(640, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(GetQrCode.this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
                    @Override
                    public void onQRCodeFound(String _qrCode) {
                        Toast.makeText(getApplicationContext(),"QR Code Found",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GetQrCode.this, MainActivity2.class);
                        qrCode = _qrCode;
                        intent.putExtra("data", qrCode);
                        setResult(MainActivity.REQUEST_QRCODE, intent);
                        finish();
                    }
                    @Override
                    public void qrCodeNotFound() {
                    }
                }));
                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)GetQrCode.this, cameraSelector, imageAnalysis, preview);
            }
        });
    }
}