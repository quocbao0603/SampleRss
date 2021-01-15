package com.example.samplerss;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listViewRSS;
    ArrayList<String> titles;
    ArrayList<String> links;


    String urlPattern = "https://feeds.24.com/articles/Fin24/Tech/rss";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewRSS = (ListView) findViewById(R.id.listViewRSS);

        titles = new ArrayList<String>();
        links = new ArrayList<String>();

        listViewRSS.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(links.get(position));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        new ProcessInBackground().execute();
    }

    public InputStream getInputStream(URL url){
        try{
            return url.openConnection().getInputStream();
        }
        catch (IOException e){
            return null;
        }
    }

    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception>{
        //Arguments[2] is the same datatype with doInBackGround method
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        Exception exception = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Busy loading rss feed... please wait....");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... integers) {
            try{
                URL url = new URL("https://feeds.24.com/articles/Fin24/Tech/rss");
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                //extract data with document encoding is UTF_8
                xpp.setInput(getInputStream(url), "UTF_8");

                boolean insideItem = false;
                int eventType = xpp.getEventType();
                while(eventType != XmlPullParser.END_DOCUMENT){
                    if (eventType == XmlPullParser.START_TAG){
                        if (xpp.getName().equalsIgnoreCase("item")){
                            insideItem = true;
                        }
                        else if(xpp.getName().equalsIgnoreCase("title")){
                            //if we in title in a item;
                            if (insideItem){
                                titles.add(xpp.nextText());
                            }
                        }
                        else if(xpp.getName().equalsIgnoreCase("link")){
                            if (insideItem){
                                links.add(xpp.nextText());
                            }
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                        insideItem = false;
                    }
                }
                eventType = xpp.next();
            }
            catch (MalformedURLException e){
                exception = e;
            }
            catch(XmlPullParserException e){
                exception = e;
            }
            catch (IOException e){
                exception = e;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, titles);
            listViewRSS.setAdapter(adapter);
            progressDialog.dismiss();
        }
    }
}