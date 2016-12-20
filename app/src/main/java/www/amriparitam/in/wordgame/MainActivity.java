package www.amriparitam.in.wordgame;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName() ;
    EditText word_text, def_text;
    String real_word;
    Button chk, next_word, tryAgain, hint, endGame;
    int point=0;
    static int high_score=0;
    int hint_val=0;

    String word_url="http://api.wordnik.com:80/v4/words.json/randomWord?hasDictionaryDef=false&minCorpusCount=0&maxCorpusCount=-1&minDictionaryCount=1&maxDictionaryCount=-1&minLength=5&maxLength=-1&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";
    String def_url=null;
    String word_rel_url=null;

    JSONArray jsonDef = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI elements references
        word_text = (EditText) findViewById(R.id.word);
        def_text = (EditText) findViewById(R.id.def);

        chk = (Button) findViewById(R.id.check);
        next_word = (Button) findViewById(R.id.next);
        tryAgain = (Button) findViewById(R.id.tryAgain);
        hint = (Button) findViewById(R.id.hint);
        endGame = (Button) findViewById(R.id.end);

        tryAgain.setEnabled(false);
        hint.setEnabled(false);


        endGame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(),"score="+point, Toast.LENGTH_LONG).show();
                if(point>high_score)
                {
                    high_score=point;
                    Toast.makeText(getApplicationContext(), "Yaye U score highest!!!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        chk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                String val=word_text.getText().toString();
                if(val.equals(real_word))
                {
                    Toast.makeText(getApplicationContext(), "correct", Toast.LENGTH_LONG).show();
                    point+=10;
                    word_text.setText("");
                    jsonDef =null;
                    hint_val=0;
                    new HttpAsyncTask_word().execute(word_url);

                }
                else
                {
                    Toast.makeText(getApplicationContext(), "wrong", Toast.LENGTH_LONG).show();
                    if(hint_val>0)
                        point-=1;
                    else
                        point-=2;
                    word_text.setText("");

                    tryAgain.setEnabled(true);
                    hint.setEnabled(true);
                    chk.setEnabled(false);

                }
            }
        });

        tryAgain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                chk.setEnabled(true);
                word_text.setEnabled(true);

            }
        });

        hint.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                hint_val+=1;
                JSONObject jObj=null;
                try {
                    jObj = jsonDef.getJSONObject(hint_val);
                } catch (JSONException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if((jsonDef.length())==hint_val)
                {
                    hint_val--;
                    point-=hint_val;
                    hint.setEnabled(false);
                    chk.setEnabled(false);
                    tryAgain.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "word is "+real_word+" try next word ", Toast.LENGTH_LONG).show();
                }
                else{
                    try {
                        Toast.makeText(getApplicationContext(),jObj.getString("text"),Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }





            }
        });
        next_word.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                hint_val=0;
                word_text.setText("");
                word_text.setEnabled(true);
                hint.setEnabled(false);
                chk.setEnabled(true);
                tryAgain.setEnabled(false);
                jsonDef=null;
                Toast.makeText(getApplicationContext(),"word is :"+real_word, Toast.LENGTH_LONG).show();
                new HttpAsyncTask_word().execute(word_url);
            }
        });


        //checking internet connection
        if(isConnected())
        {
            new HttpAsyncTask_word().execute(word_url);
        }
        else
        {
            Toast toast=Toast.makeText(getApplicationContext(),"Not connected", Toast.LENGTH_LONG);
            toast.show();
        }

    }

    //function to check Internet connection
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


    public static String GET(String urlStr){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String result = null;
        try {
            Uri uri = Uri.parse(urlStr);

            URL url =  new URL(uri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            result = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return result;
    }

    //convert result to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class HttpAsyncTask_word extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            JSONObject json = null;
            try {
                json = new JSONObject(result);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                real_word=json.getString("word");
                definition(real_word);
               // Toast.makeText(getApplicationContext(), real_word, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {

                e.printStackTrace();
            }

        }

        //to get the definition of word
        private void definition(String word) {
            // TODO Auto-generated method stub
            def_url = "http://api.wordnik.com:80/v4/word.json/"+word+"/definitions?limit=200&includeRelated=true&useCanonical=false&includeTags=false&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";
            new HttpAsyncTask_def().execute(def_url);
            word_rel_url="http://api.wordnik.com:80/v4/word.json/"+word+"/relatedWords?useCanonical=false&limitPerRelationshipType=10&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";
        }
    }

    //async function to check the definition of random word and post the result
    private class HttpAsyncTask_def extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result != null){
                JSONObject jObj = null;
                try {
                    jsonDef = new JSONArray(result);
                    jObj = jsonDef.getJSONObject(0);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    def_text.setText(jObj.getString("text"));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
            else {
                new HttpAsyncTask_word().execute(word_url);
            }

        }
    }
}
