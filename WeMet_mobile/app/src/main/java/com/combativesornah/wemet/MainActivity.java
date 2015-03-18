package com.combativesornah.wemet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;
import android.graphics.Color;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.brand);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_main);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_by, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        Updates notifs = new Updates();
        int num_notifs = notifs.wemets.length + notifs.messages.length;
        final TextView notif_text = (TextView) findViewById(R.id.notif_text);
        final RelativeLayout button = (RelativeLayout) findViewById(R.id.notif_btn);

        if (num_notifs > 0) {
            notif_text.setText(num_notifs + " new notifications");
            notif_text.setTextColor(Color.parseColor("#000000"));
            button.setBackgroundColor(Color.parseColor("#d2cb5a"));
        }
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Show notifications", Toast.LENGTH_SHORT).show();
                TextView notes = (TextView) findViewById(R.id.notif_text);
                notes.setText("No new notifications");
                notes.setTypeface(notes.getTypeface(), Typeface.ITALIC);
                notes.setTextColor(Color.parseColor("#4e4e4e"));
                notes.setTextSize(12);
                button.setBackgroundColor(Color.parseColor("#e5e5e5"));
                ImageView dot = (ImageView) findViewById(R.id.imageView);
                dot.setVisibility(View.INVISIBLE);

            }
        });

        class PersonArrayAdapter extends ArrayAdapter<Profile> {
            private final Context context;
            private final ArrayList<Profile> profiles;
            String[] colors = {"#447799", "#aacccc", "#88aaaa", "#6699aa", "#334433"};

            public PersonArrayAdapter(Context context, ArrayList<Profile> profiles) {
                super(context, R.layout.person, profiles);
                this.context = context;
                this.profiles = profiles;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Log.e("Response", "getting view");

                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.person, parent, false);
                TextView textView = (TextView) rowView.findViewById(R.id.name);
                final ImageView imageView = (ImageView) rowView.findViewById(R.id.headshot);
                textView.setText(profiles.get(position).first + " " + profiles.get(position).last);
//                int pic_id = getResources().getIdentifier(profiles[position].pic, "drawable", getPackageName());
//                imageView.setImageResource(pic_id);

                RequestQueue queue = Volley.newRequestQueue(context);
                String url = "https://themes.shopify.com/assets/icons/icon-emotion-neutral-bf91351ee98b3caa91c0df06de4b18e6.png";
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }, 0, 0, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                imageView.setImageResource(R.drawable.mypic3);
                            }
                        });
                queue.add(request);

                RelativeLayout bg = (RelativeLayout) rowView.findViewById(R.id.p_container);
                bg.setBackgroundColor(Color.parseColor(colors[(position/5) % 5]));

                return rowView;
            }
        }

        final ListView listview = (ListView) findViewById(R.id.listView);

        final ProfileList list = new ProfileList();
        final PersonArrayAdapter p_adapter = new PersonArrayAdapter(this, list.profiles);
        listview.setAdapter(p_adapter);
        Log.e("Response", "Adapter Set");
        list.refresh(p_adapter);


        final RelativeLayout screen = (RelativeLayout) findViewById(R.id.screen);
        final ImageView inspect_img = (ImageView) findViewById(R.id.inspect_img);
        final RelativeLayout inspect_bg = (RelativeLayout) findViewById(R.id.inspect_bg);
        final TextView inspect_name = (TextView) findViewById(R.id.inspect_name);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(),
                        "Click ListItem Number " + position, Toast.LENGTH_LONG)
                        .show();
                screen.setVisibility(View.VISIBLE);
//                inspect_img.setImageResource(getResources().getIdentifier(list.profiles.get(position).pic,
//                        "drawable", getPackageName()));
                inspect_img.setImageResource(R.drawable.mypic3);
                inspect_img.setVisibility(View.VISIBLE);
                inspect_bg.setBackgroundColor(Color.parseColor(p_adapter.colors[(position/5) % 5]));
                inspect_name.setText(list.profiles.get(position).first + " " + list.profiles.get(position).last);
            }
        });
        screen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                screen.setVisibility(View.INVISIBLE);
                inspect_img.setVisibility(View.INVISIBLE);

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
