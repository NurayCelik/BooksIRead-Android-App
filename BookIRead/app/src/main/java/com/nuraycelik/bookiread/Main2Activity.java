package com.nuraycelik.bookiread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class Main2Activity extends AppCompatActivity {

    Bitmap selectedImage;
    ImageView imageView;
    EditText bookNameText, bookAuthorText, readDateText, textView;
    Button button;
    SQLiteDatabase database;
    AlertDialog.Builder alert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        alert = new AlertDialog.Builder(Main2Activity.this);

        imageView = findViewById(R.id.imageView);
        bookNameText = findViewById(R.id.bookNameText);
        bookAuthorText = findViewById(R.id.bookAuthorText);
        readDateText = findViewById(R.id.readDateText);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button2);

        database = this.openOrCreateDatabase("BookRead", MODE_PRIVATE, null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.matches("new")) {
            bookNameText.setText("");
            bookAuthorText.setText("");
            readDateText.setText("");
            textView.setText("");
            button.setVisibility(View.VISIBLE);

            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.selectimage);
            imageView.setImageBitmap(selectImage);

        } else {
            int artId = intent.getIntExtra("bookId", 1);
            button.setVisibility(View.INVISIBLE);
            try {

                Cursor cursor = database.rawQuery("SELECT * FROM books WHERE id = ?", new String[]{String.valueOf(artId)});
                int bookNameIx = cursor.getColumnIndex("bName");
                int authorIx = cursor.getColumnIndex("author");
                int notIx = cursor.getColumnIndex("notes");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {


                    bookNameText.setText(cursor.getString(bookNameIx));
                    bookAuthorText.setText(cursor.getString(authorIx));
                    readDateText.setText(cursor.getString(yearIx));
                    textView.setText(cursor.getString(notIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bitmap);

                }

                cursor.close();

            } catch (Exception e) {

            }


        }
    }

    public void selectImage(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            //galeriden resim topla: pick
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery, 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //grantResults : verilen degerler
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery, 2);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {

            Uri imageData = data.getData();

            try {

                if (Build.VERSION.SDK_INT >= 28) {

                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);

                } else {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageData);
                    imageView.setImageBitmap(selectedImage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean save(View view) {
        boolean state = false;
        String bName = bookNameText.getText().toString();
        String author = bookAuthorText.getText().toString();
        String notes = textView.getText().toString();
        String year = readDateText.getText().toString();

        if(selectedImage==null || bName.length()==0 || author.length()==0 || notes.length()==0 || year.length()==0) {
            state= false;
            alert.setTitle("Info");
            alert.setMessage("Please select image or fill in the blank fields");
            alert.setPositiveButton("OK", null);
            alert.show();

        }
        else {
            Bitmap smallImage = makeSmallerImage(selectedImage, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            try {

                database = this.openOrCreateDatabase("BookRead", MODE_PRIVATE, null);
                database.execSQL("CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY, bName VARCHAR, author VARCHAR, notes VARCHAR, year VARCHAR, image BLOB)");


                String sqlString = "INSERT INTO books (bName, author, notes, year, image) VALUES (?, ?, ?, ?, ?)";
                SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
                sqLiteStatement.bindString(1, bName);
                sqLiteStatement.bindString(2, author);
                sqLiteStatement.bindString(3, notes);
                sqLiteStatement.bindString(4, year);
                sqLiteStatement.bindBlob(5, byteArray);
                sqLiteStatement.execute();

            } catch (Exception e) {

            }

            //Activity ler kapatılıyor, bir syfadan diğerine geçişlerde önceki işlemler de sona eriyor, karışıklık gideriliyor
            Intent intent = new Intent(Main2Activity.this, Main1Activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            state = true;
            //finish();
        }
    return true;
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

            int width = image.getWidth();
            int height = image.getHeight();

            float bitmapRatio = (float) width / (float) height;

            if (bitmapRatio > 1) {
                width = maximumSize;
                height = (int) (width / bitmapRatio);
            } else {
                height = maximumSize;
                width = (int) (height * bitmapRatio);
            }
            return Bitmap.createScaledBitmap(image, width, height, true);



    }
}
