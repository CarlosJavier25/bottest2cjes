package com.ibm.watson_conversation;

import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.Intent;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.service.exception.BadRequestException;
import com.ibm.watson.developer_cloud.service.exception.UnauthorizedException;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;


import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AppIDAuthorizationManager;

import static com.ibm.watson_conversation.R.id.btn_ticket;
import static com.ibm.watson_conversation.R.id.entry_text;
import static com.ibm.watson_conversation.R.id.userTextView;


public class MainActivity extends AppCompatActivity {

    
    

    private static final String USER_USER = "user";
    private static final String USER_WATSON = "watson";

    private ConversationService conversationService;
    private Map<String, Object> conversationContext;
    private ArrayList<ConversationMessage> conversationLog;

    private Button btn_ticket, btn_llamar;
    private WebView ww;
    private LinearLayout layoutAnimado;
    private TextView entryText;

    Context mContext=MainActivity.this;
    public SharedPreferences appPreferences;
    boolean isAppInstalled = false;






    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        //////instalar icono en la pantalla pricipal




        setContentView(R.layout.activity_main);appPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        isAppInstalled = appPreferences.getBoolean("isAppInstalled",false);
        if(isAppInstalled==false){

            //  create short code

            android.content.Intent shortcutIntent = new android.content.Intent(getApplicationContext(),MainActivity.class);
            shortcutIntent.setAction(android.content.Intent.ACTION_MAIN);
            android.content.Intent intent = new android.content.Intent();
            intent.putExtra(android.content.Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(android.content.Intent.EXTRA_SHORTCUT_NAME, "Support WATSON");
            intent.putExtra(android.content.Intent.EXTRA_SHORTCUT_ICON_RESOURCE, android.content.Intent.ShortcutIconResource
                    .fromContext(getApplicationContext(), R.drawable.icon));
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(intent);

            //Make preference true

            SharedPreferences.Editor editor = appPreferences.edit();
            editor.putBoolean("isAppInstalled", true);
            editor.commit();
        }






        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btn_ticket=(Button)findViewById(R.id.btn_ticket);
        btn_llamar=(Button)findViewById(R.id.btn_llamar);
        layoutAnimado = (LinearLayout) findViewById(R.id.animado);
        ww=(WebView)findViewById(R.id.ww);
        setSupportActionBar(toolbar);

        // Initialize the Watson Conversation Service and instantiate the Message Log.
        conversationService = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
        conversationService.setUsernameAndPassword(getString(R.string.watson_conversation_username), 
                getString(R.string.watson_conversation_password));
        conversationLog = new ArrayList<>();

        btn_ticket.setVisibility(View.GONE);
        btn_llamar.setVisibility(View.GONE);
        layoutAnimado.setVisibility(View.GONE);

       ww.setVisibility(View.INVISIBLE);







        btn_ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "ADIOS!!", Toast.LENGTH_SHORT).show();


                ww.setVisibility(View.VISIBLE);


                ww.loadUrl("https://www.ibm.com/support/servicerequest");



            }
        });







        btn_llamar.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "ADIOS!!", Toast.LENGTH_SHORT).show();

                //android.content.Intent LLamarActivity = new android.content.Intent(getApplicationContext(), LLamarActivity.class);
                //startActivity(LLamarActivity);

                try{
                    startActivity(new android.content.Intent(android.content.Intent.ACTION_CALL, Uri.parse("tel:5552705900")));
                }catch(Exception e){
                    e.printStackTrace();
                }





            }




        });















        // If we have a savedInstanceState, recover the previous Context and Message Log.
        if (savedInstanceState != null) {
            conversationContext = (Map<String,Object>)savedInstanceState.getSerializable("context");
            conversationLog = (ArrayList<ConversationMessage>)savedInstanceState.getSerializable("backlog");

            // Repopulate the UI with the previous Messages.
            if (conversationLog != null) {
                for (ConversationMessage message : conversationLog) {
                    addMessageFromUser(message);
                }
            }

            final ScrollView scrollView = (ScrollView)findViewById(R.id.message_scrollview);
            scrollView.scrollTo(0, scrollView.getBottom());
        } else {
            // Validate that the user's credentials are valid and that we can continue.
            // This also kicks off the first Conversation Task to obtain the intro message from Watson.
            ValidateCredentialsTask vct = new ValidateCredentialsTask();
            vct.execute();
        }







        ImageButton sendButton = (ImageButton)findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entryText = (TextView)findViewById(R.id.entry_text);
                String text = entryText.getText().toString();

                if (!text.isEmpty()) {
                    // Add the message to the UI.
                    addMessageFromUser(new ConversationMessage(text, USER_USER));

                    // Record the message in the conversation log.
                    conversationLog.add(new ConversationMessage(text, USER_USER));

                    // Send the message to Watson Conversation.
                    ConversationTask ct = new ConversationTask();
                    ct.execute(text);

                    entryText.setText("");
                }







            }
        });

        // Core SDK must be initialized to interact with Bluemix Mobile services.
        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_US_SOUTH);

        

        

        

        

        AppID.getInstance().initialize(this, getString(R.string.authTenantId), BMSClient.REGION_US_SOUTH);
        BMSClient.getInstance().setAuthorizationManager(new AppIDAuthorizationManager(AppID.getInstance()));









        
    }








    @Override
    public void onResume() {
        super.onResume();

        
        
    }

    @Override
    public void onPause() {
        super.onPause();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;






    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on
        // the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.clear_session_action) {
            // Clear the conversation log, the conversation context, and clear the UI.

            btn_ticket.setVisibility(View.GONE);
            btn_llamar.setVisibility(View.GONE);
            layoutAnimado.setVisibility(View.GONE);
            conversationContext = null;
            conversationLog = new ArrayList<>();

            LinearLayout messageContainer = (LinearLayout) findViewById(R.id.message_container);
            messageContainer.removeAllViews();

            // Restart the conversation with the same empty text string sent to Watson Conversation.
            ConversationTask ct = new ConversationTask();
            ct.execute("");
            
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Retain the conversation context and the log of previous messages, if they exist.
        if (conversationContext != null) {
            HashMap map = new HashMap(conversationContext);
            savedInstanceState.putSerializable("context", map);
        }
        if (conversationLog != null) {
            savedInstanceState.putSerializable("backlog", conversationLog);
        }
    }

    /**
     * Displays an AlertDialogFragment with the given parameters.
     * @param errorTitle Error Title from values/strings.xml.
     * @param errorMessage Error Message either from values/strings.xml or response from server.
     * @param canContinue Whether the application can continue without needing to be rebuilt.
     */
    private void showDialog(int errorTitle, String errorMessage, boolean canContinue) {
        DialogFragment newFragment = AlertDialogFragment.newInstance(errorTitle, errorMessage, canContinue);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Adds a message dialog view to the UI.
     * @param message ConversationMessage containing a message and the sender.
     */
    private void addMessageFromUser(ConversationMessage message) {
        View messageView;
        LinearLayout messageContainer = (LinearLayout) findViewById(R.id.message_container);

        if (message.getUser().equals(USER_WATSON)) {
            messageView = this.getLayoutInflater().inflate(R.layout.watson_text, messageContainer, false);
            TextView watsonMessageText = (TextView)messageView.findViewById(R.id.watsonTextView);
            watsonMessageText.setText(message.getMessageText());

                //String flag1 = "*" ;
                String Wtext=  watsonMessageText.getText().toString();
                String w =String.valueOf(Wtext.charAt(0));
               // Toast.makeText(getApplicationContext(), w, Toast.LENGTH_SHORT).show();


                if (w.equals("*")){

                    //Toast.makeText(getApplicationContext(),"TICKET", Toast.LENGTH_SHORT).show();


                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(entryText.getWindowToken(), 0);




                    new CountDownTimer(3000, 3000) {

                        public void onTick(long millisUntilFinished) {






                        }

                        public void onFinish() {

                            animar(true);


                            layoutAnimado.setVisibility(View.VISIBLE);
                            btn_ticket.setVisibility(View.VISIBLE);
                            btn_llamar.setVisibility(View.VISIBLE);




                        }
                    }.start();




























                }




        } else {
            messageView = this.getLayoutInflater().inflate(R.layout.user_text, messageContainer, false);
            TextView userMessageText = (TextView)messageView.findViewById(userTextView);
            userMessageText.setText(message.getMessageText());
        }

        messageContainer.addView(messageView);

        // Scroll to the bottom of the view so the user sees the update.
        final ScrollView scrollView = (ScrollView)findViewById(R.id.message_scrollview);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });









    }

    /**
     * Asynchronously contacts the Watson Conversation Service to see if provided Credentials are valid.
     */
    private class ValidateCredentialsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            // Mark whether or not the validation completes.
            boolean success = true;

            try {
                conversationService.getToken().execute();
            } catch (Exception ex) {

                success = false;

                // See if the user's credentials are valid or not, along with other errors.
                if (ex.getClass().equals(UnauthorizedException.class) ||
                        ex.getClass().equals(IllegalArgumentException.class)) {
                    showDialog(R.string.error_title_invalid_credentials,
                            getString(R.string.error_message_invalid_credentials), false);
                } else if (ex.getCause() != null &&
                        ex.getCause().getClass().equals(UnknownHostException.class)) {
                    showDialog(R.string.error_title_bluemix_connection,
                            getString(R.string.error_message_bluemix_connection), true);
                } else {
                    showDialog(R.string.error_title_default, ex.getMessage(), true);
                }
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // If validation succeeded, then get the opening message from Watson Conversation
            // by sending an empty input string to the ConversationTask.
            if (success) {
                ConversationTask ct = new ConversationTask();
                ct.execute("");
            }
        }
    }








    /**
     * Asynchronously sends the user's message to Watson Conversation and receives Watson's response.
     */
    private class ConversationTask extends AsyncTask<String, Void, String> {








        @Override
        protected String doInBackground(String... params) {
            String entryText = params[0];

            MessageRequest messageRequest;

            // Send Context to Watson in order to continue a conversation.
            if (conversationContext == null) {
                messageRequest = new MessageRequest.Builder()
                        .inputText(entryText).build();
            } else {
                messageRequest = new MessageRequest.Builder()
                        .inputText(entryText)
                        .context(conversationContext).build();
            }

            try {
                // Send the message to the workspace designated in watson_credentials.xml.
                MessageResponse messageResponse = conversationService.message(
                        getString(R.string.watson_conversation_workspace_id), messageRequest).execute();

                conversationContext = messageResponse.getContext();
                return messageResponse.getText().get(0);
            } catch (Exception ex) {
                // A failure here is usually caused by an incorrect workspace in watson_credentials.
                if (ex.getClass().equals(BadRequestException.class)) {
                    showDialog(R.string.error_title_invalid_workspace,
                            getString(R.string.error_message_invalid_workspace), false);
                } else {
                    showDialog(R.string.error_title_default, ex.getMessage(), true);
                }
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Add the message from Watson to the UI.
            addMessageFromUser(new ConversationMessage(result, USER_WATSON));

            // Record the message from Watson in the conversation log.
            conversationLog.add(new ConversationMessage(result, USER_WATSON));
        }
    }


    private void animar(boolean mostrar)
    {
        AnimationSet set = new AnimationSet(true);
        Animation animation = null;
        if (mostrar)
        {
            //desde la esquina inferior derecha a la superior izquierda
            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        }
        else
        {    //desde la esquina superior izquierda a la esquina inferior derecha
            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        }
        //duración en milisegundos
        animation.setDuration(500);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.25f);

        layoutAnimado.setLayoutAnimation(controller);
        layoutAnimado.startAnimation(animation);
    }




}
