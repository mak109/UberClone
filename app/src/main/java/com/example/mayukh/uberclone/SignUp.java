package com.example.mayukh.uberclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        if(edtUserChoice.getText().toString().equalsIgnoreCase("Driver") || edtUserChoice.getText().toString().equalsIgnoreCase("Passenger"))
        {
            if(ParseUser.getCurrentUser() == null){
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if(user != null && e == null){
                            FancyToast.makeText(SignUp.this,"Anonymous session started as "+edtUserChoice.getText().toString(),FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,true).show();
                            user.put("as",edtUserChoice.getText().toString());
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    transitionToPassengerActivity();
                                }
                            });

                        }
                        else
                            FancyToast.makeText(SignUp.this,e.getMessage(),FancyToast.LENGTH_SHORT,FancyToast.ERROR,true).show();
                    }
                });
            }
        }
        else
            FancyToast.makeText(SignUp.this,"Are you a driver or passenger ?",FancyToast.LENGTH_SHORT,FancyToast.CONFUSING,true).show();

    }

    enum State
    {
        SIGNUP,LOGIN
    }
    private State state;
    private Button btnSignUpLogIn,btnOneTimeLogin;
    private EditText edtUserName,edtPassword,edtUserChoice;
    private RadioButton rdbPassenger,rdbDriver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        state = State.SIGNUP;

        rdbDriver = findViewById(R.id.rdbDriver);
        rdbPassenger = findViewById(R.id.rdbPassenger);

        btnSignUpLogIn = findViewById(R.id.btnSignUpLogIn);
        btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);

        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        edtUserChoice = findViewById(R.id.edtUserChoice);

        if(ParseUser.getCurrentUser() != null){
            //ParseUser.logOut();
            transitionToPassengerActivity();
        }
        btnOneTimeLogin.setOnClickListener(this);
        btnSignUpLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(state == State.SIGNUP){
                    if(rdbPassenger.isChecked() == false && rdbDriver.isChecked() == false){
                        FancyToast.makeText(SignUp.this,"Are you a driver or passenger ?",FancyToast.LENGTH_SHORT,FancyToast.CONFUSING,true).show();
                        return;
                    }
                    else
                    {
                        final ParseUser appUser = new ParseUser();
                        appUser.setUsername(edtUserName.getText().toString());
                        appUser.setPassword(edtPassword.getText().toString());
                        if(rdbPassenger.isChecked() == true){
                            appUser.put("as","Passenger");
                        }
                        else if(rdbDriver.isChecked() == true){
                            appUser.put("as","Driver");
                        }
                        if(edtUserName.getText().toString().equals("") || edtPassword.getText().toString().equals("")){
                            FancyToast.makeText(SignUp.this,"Username/Password required",FancyToast.LENGTH_SHORT,FancyToast.ERROR,true).show();
                            return;
                        }
                        final ProgressDialog progressDialog = new ProgressDialog(SignUp.this);
                        progressDialog.setMessage("Signing Up....");
                        progressDialog.show();
                        appUser.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null)
                                {
                                    FancyToast.makeText(SignUp.this,"User signed up as "+appUser.get("as"),FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,true).show();
                                    transitionToPassengerActivity();
                                }
                                else
                                    FancyToast.makeText(SignUp.this,e.getMessage(),FancyToast.LENGTH_SHORT,FancyToast.ERROR,true).show();
                                progressDialog.dismiss();
                            }
                        });

                    }
                }
                else
                {
                    if(edtUserName.getText().toString().equals("") || edtPassword.getText().toString().equals("")) {
                        FancyToast.makeText(SignUp.this, "Username/Password required", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                        return;
                    }
                    else
                    {
                        final ProgressDialog progressDialog = new ProgressDialog(SignUp.this);
                        progressDialog.setMessage("Logging in....");
                        progressDialog.show();
                        ParseUser.logInInBackground(edtUserName.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(user != null && e == null){
                                    FancyToast.makeText(SignUp.this,"User logged in ",FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,true).show();
                                    transitionToPassengerActivity();
                                }
                                else
                                    FancyToast.makeText(SignUp.this,e.getMessage(),FancyToast.LENGTH_SHORT,FancyToast.ERROR,true).show();
                                progressDialog.dismiss();
                            }
                        });
                    }

                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.logInSignUpItem:
                if(state == State.SIGNUP){
                    btnSignUpLogIn.setText("LOG IN");
                    item.setTitle("SIGN UP");
                    state = State.LOGIN;
                }
                else
                {
                    btnSignUpLogIn.setText("SIGN UP");
                    item.setTitle("LOG IN");
                    state = State.SIGNUP;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void transitionToPassengerActivity(){
        if(ParseUser.getCurrentUser() != null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent = new Intent(SignUp.this,PassengerActivity.class);
                startActivity(intent);
            }
        }
    }
}
