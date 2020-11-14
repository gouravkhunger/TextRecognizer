package app.web.gourav_khunger.textrecognizer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.muddzdev.styleabletoast.StyleableToast;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Home extends AppCompatActivity {

    Toolbar toolbar;
    CardView imageHolder;
    ImageView image;
    LinearLayout selectButtons, processButtons;
    Button captureImage, selectFromStorage, clearAll, processImage, crop;
    TextView selectImageText;
    Bitmap bitmap = null;

    private static final int PICK_IMAGE_CODE = 0;
    private static final int CAPTURE_IMAGE_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int WRITE_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();

    }

    private void init() {
        toolbar = findViewById(R.id.toolbar_home);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        selectImageText = findViewById(R.id.selectImageText);
        imageHolder = findViewById(R.id.imageHolder);
        image = findViewById(R.id.image);
        selectButtons = findViewById(R.id.selectionButtons);
        crop = findViewById(R.id.cropImage);
        crop.setOnClickListener(v -> {
            if (bitmap != null) {
                Uri uri = getImageUri(this, bitmap);
                if (uri != null) {
                    CropImage.activity(uri)
                            .start(this);
                } else {
                    new StyleableToast
                            .Builder(this)
                            .text("I'm confused :(")
                            .textColor(Color.WHITE)
                            .backgroundColor(Color.RED)
                            .font(R.font.google)
                            .show();
                }

            }
        });
        processButtons = findViewById(R.id.processButtons);
        captureImage = findViewById(R.id.captureImage);
        captureImage.setOnClickListener(v -> captureAnImage());
        selectFromStorage = findViewById(R.id.selectFromStorage);
        selectFromStorage.setOnClickListener(v -> selectFromStorage());
        processImage = findViewById(R.id.processImage);
        processImage.setOnClickListener(v -> processImage());
        clearAll = findViewById(R.id.clearAll);
        clearAll.setOnClickListener(v -> hideAll());

        hideAll();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
        } else {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Image", null);
            return Uri.parse(path);
        }
        return null;
    }

    private void copyText(String text) {
        if (!TextUtils.isEmpty(text) && text != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Recognized Text", text);
            clipboard.setPrimaryClip(clip);
            new StyleableToast
                    .Builder(this)
                    .text("Text Copied!")
                    .textColor(Color.WHITE)
                    .backgroundColor(ContextCompat.getColor(this, R.color.green))
                    .font(R.font.google)
                    .show();
        }
    }

    private void selectFromStorage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select from:"),
                PICK_IMAGE_CODE);
    }

    private void captureAnImage() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAPTURE_IMAGE_CODE);
            } else {
                new StyleableToast
                        .Builder(this)
                        .text("Error: ")
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.RED)
                        .font(R.font.google)
                        .show();
            }
        }
    }

    private void hideAll() {
        image.setImageResource(0);
        imageHolder.setVisibility(View.GONE);
        processButtons.setVisibility(View.GONE);
        selectButtons.setVisibility(View.VISIBLE);
        selectImageText.setVisibility(View.VISIBLE);
        bitmap = null;
    }

    private void showAll() {
        imageHolder.setVisibility(View.VISIBLE);
        processButtons.setVisibility(View.VISIBLE);
        selectButtons.setVisibility(View.GONE);
        selectImageText.setVisibility(View.GONE);
    }

    private void processImage() {
        if (bitmap != null) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Processing...");
            dialog.setMessage("Please have patience ._.");
            dialog.setCancelable(false);
            dialog.show();
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient();
            recognizer.process(inputImage)
                    .addOnSuccessListener(result -> {
                        String text = result.getText();
                        dialog.dismiss();
                        if (!TextUtils.isEmpty(text) && text != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                            builder.setTitle("Text Recognized")
                                    .setMessage(text)
                                    .setCancelable(false)
                                    .setPositiveButton("Copy", (dialog1, id) -> copyText(text))
                                    .setNegativeButton("Cancel", (dialog1, which) -> hideAll())
                                    .setNeutralButton("Close Dialog", (dialog1, which) -> dialog1.cancel());
                            AlertDialog alert = builder.create();
                            alert.getWindow().setBackgroundDrawableResource(R.drawable.round_dialog);
                            alert.setOnShowListener(arg0 -> {
                                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(Home.this, R.color.blue));
                                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(Home.this, R.color.blue));
                            });
                            alert.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                            builder.setTitle("Ooof")
                                    .setMessage("No Text Detected! ")
                                    .setCancelable(false)
                                    .setPositiveButton("Copy", (dialog1, id) -> dialog1.dismiss());
                            AlertDialog alert = builder.create();
                            alert.getWindow().setBackgroundDrawableResource(R.drawable.round_dialog);
                            alert.setOnShowListener(arg0 -> {
                                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(Home.this, R.color.blue));
                                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(Home.this, R.color.blue));
                            });
                            alert.show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        hideAll();
                        new StyleableToast
                                .Builder(this)
                                .text("Error: " + e.getMessage())
                                .textColor(Color.WHITE)
                                .backgroundColor(Color.RED)
                                .font(R.font.google)
                                .show();
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        if (requestCode == PICK_IMAGE_CODE
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {

            try {
                bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                this.getContentResolver(),
                                data.getData());

                Glide.with(Home.this)
                        .load(bitmap)
                        .into(image);

                showAll();

            } catch (IOException e) {
                hideAll();
            }

        }


        if (requestCode == CAPTURE_IMAGE_CODE) {
            if(resultCode==Activity.RESULT_OK){
                bitmap = (Bitmap) data.getExtras().get("data");

                Glide.with(Home.this)
                        .load(bitmap)
                        .into(image);

                showAll();
            } else {
                hideAll();
            }


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    this.getContentResolver(),
                                    result.getUri());
                    Glide.with(Home.this)
                            .load(bitmap)
                            .into(image);
                    File fdelete = new File(result.getUri().getPath());
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            new StyleableToast
                                    .Builder(this)
                                    .text("Saved storage by deleting unwanted image crop data :)")
                                    .textColor(Color.WHITE)
                                    .backgroundColor(ContextCompat.getColor(this, R.color.blue))
                                    .font(R.font.google)
                                    .length(Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            new StyleableToast
                                    .Builder(this)
                                    .text("Could not delete unwanted cropped image data :(")
                                    .textColor(Color.WHITE)
                                    .backgroundColor(Color.RED)
                                    .font(R.font.google)
                                    .length(Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    showAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                hideAll();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
            builder.setTitle("About")
                    .setMessage(getResources().getString(R.string.about))
                    .setCancelable(false)
                    .setPositiveButton("Nice!", (dialog1, id) -> {
                        dialog1.dismiss();
                        new StyleableToast
                                .Builder(this)
                                .text("Thank you :)")
                                .textColor(Color.WHITE)
                                .backgroundColor(ContextCompat.getColor(this, R.color.green))
                                .font(R.font.google)
                                .show();
                    });
            AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawableResource(R.drawable.round_dialog);
            alert.setOnShowListener(arg0 -> {
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(Home.this, R.color.blue));
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(Home.this, R.color.blue));
            });
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}