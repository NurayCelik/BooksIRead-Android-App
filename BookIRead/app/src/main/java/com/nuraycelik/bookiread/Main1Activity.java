package com.nuraycelik.bookiread;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class Main1Activity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> nameArray;
    ArrayList<Integer> idArray;
    ArrayAdapter arrayAdapter;
    private SQLiteDatabase database;
    private boolean resultValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        listView = findViewById(R.id.listView);
        nameArray = new ArrayList<String>();
        idArray = new ArrayList<Integer>();

        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,nameArray);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(Main1Activity.this,Main2Activity.class);
                intent.putExtra("bookId",idArray.get(position));
                intent.putExtra("info","old");
                startActivity(intent);


        }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            AlertDialog.Builder alert = new AlertDialog.Builder(Main1Activity.this);

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                nameArray.remove(i);//where arg2 is position of item you click
                arrayAdapter.notifyDataSetChanged();
                //Toast.makeText(getApplicationContext(),"Selected Book Remove",Toast.LENGTH_LONG).show();
                //Toast.makeText(Main1Activity.this,"Selected Book Remove",Toast.LENGTH_LONG).show();
                database.execSQL("delete from "+ "books" +" where id='"+idArray.get(i)+"'");

                AlertDialog.Builder alert = new AlertDialog.Builder(Main1Activity.this);
                alert.setTitle("Info");
                alert.setMessage("Selected Book Remove");
                alert.setPositiveButton("OK",null);
                alert.show();

                return false;
            }
        });

    getData();
    uyariVer();

    }
    public void uyariVer() {
       AlertDialog.Builder alerti = new AlertDialog.Builder(Main1Activity.this);
        alerti.setTitle("Info");
        alerti.setMessage("Long click the selected record to delete");
        alerti.setPositiveButton("OK",null);
        alerti.show();
    }
    public void getData(){
        try {
            database = this.openOrCreateDatabase("BookRead",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM books", null);
            int nameIx = cursor.getColumnIndex("bName");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()) {
                nameArray.add(cursor.getString(nameIx));
                idArray.add(cursor.getInt(idIx));

            }

            arrayAdapter.notifyDataSetChanged();

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflater
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_book,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_book_item) {
            Intent intent = new Intent(Main1Activity.this,Main2Activity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}