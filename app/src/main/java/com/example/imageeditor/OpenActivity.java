package com.example.imageeditor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OpenActivity extends Activity {

    ImageView imageView;
    ImageView imageView1;
    TextView textView;
    TextView exifInfoTextView;
    Button button;
    Button button1;
    Button button2;
    String imagePath;
    private float rotationAngle = 0f;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        imageView = findViewById(R.id.editCircle);
        imageView1 = findViewById(R.id.showImage);
        textView = findViewById(R.id.data);
        exifInfoTextView = findViewById(R.id.txt2);
        button = findViewById(R.id.crop);
        button1 = findViewById(R.id.rotate);
        button2 = findViewById(R.id.save);
        button.setVisibility(View.GONE);
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);

        imageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        });

        button2.setOnClickListener(v -> {
            Bitmap originalBitmap = ((BitmapDrawable) imageView1.getDrawable()).getBitmap();
            addImageToGallery(OpenActivity.this, originalBitmap);
        });

        button1.setOnClickListener(view -> {
            rotationAngle += 90f;
            Bitmap originalBitmap = ((BitmapDrawable) imageView1.getDrawable()).getBitmap();
            Matrix mat = new Matrix();
            mat.postRotate(rotationAngle);
            Bitmap bMapRotate = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), mat, true);
            imageView1.setImageBitmap(bMapRotate);
        });

        button.setOnClickListener(view -> cropImage());
    }

    private void cropImage() {
        Bitmap originalBitmap = ((BitmapDrawable) imageView1.getDrawable()).getBitmap();
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        int targetWidth;
        int targetHeight;
        if (originalWidth > originalHeight) {
            targetWidth = originalWidth - 200;
            targetHeight = originalHeight;
        } else {
            targetWidth = originalWidth;
            targetHeight = originalHeight - 200;
        }
        int centerX = originalWidth / 2;
        int centerY = originalHeight / 2;

        int left = centerX - (targetWidth / 2);
        int top = centerY - (targetHeight / 2);
        int right = centerX + (targetWidth / 2);
        int bottom = centerY + (targetHeight / 2);

        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.min(originalWidth, right);
        bottom = Math.min(originalHeight, bottom);

        Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmap, left, top, right - left, bottom - top);
        imageView1.setImageBitmap(croppedBitmap);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            button.setVisibility(View.VISIBLE);
            button1.setVisibility(View.VISIBLE);
            button2.setVisibility(View.VISIBLE);
            imagePath = getPathFromUri(uri);
            imageView1.setImageURI(uri);
            try {
                ExifInterface exifInterface = new ExifInterface(imagePath);

                String dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                String make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                String model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
                String exposureTime = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);

                String exifInfo = "Date & Time: " + dateTime +
                        "\nMake: " + make +
                        "\nModel: " + model +
                        "\nExposure Time: " + exposureTime;
                exifInfoTextView.setText(exifInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return contentUri.getPath();
    }

    public void addImageToGallery(final Context context, Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String savePath = imagePath;
        File imageFile = new File(savePath.substring(0, savePath.lastIndexOf("/")), "/IMG_" + timeStamp + ".jpg");
        try {
            OutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            MediaScannerConnection.scanFile(context, new String[]{imageFile.getAbsolutePath()}, null, null);
            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}