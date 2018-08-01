package com.nloops.lossless.cards;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nloops.lossless.GuideActivity;
import com.nloops.lossless.HajiActivity;
import com.nloops.lossless.R;
import com.nloops.lossless.utilis.Utilis;
import java.util.Arrays;
import java.util.List;

public class CardsActivity extends AppCompatActivity implements CardsAdapter.OnCardClickListener {

  private static final int RC_SIGN_IN = 123;
  /*Ref of CardsAdapter*/
  CardsAdapter mAdapter;
  /* ref of Auth to signUser In and handle Authentication*/
  private FirebaseAuth mFirebaseAuth;
  /*get Activity elements ref using ButterKnife library*/
  @BindView(R.id.rv_cards_activity)
  RecyclerView mCardRecyclerView;
  /* this listener will works to handle different user using cases */
  private FirebaseAuth.AuthStateListener mAuthStateListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cards);
    ButterKnife.bind(this);
    /*init firebase Auth*/
    mFirebaseAuth = FirebaseAuth.getInstance();
    /*call sign in*/
    signIn();
    /*init and feed adapter with data*/
    mAdapter = new CardsAdapter(Utilis.createListModels(CardsActivity.this), this);
    /*Setup RecyclerView*/
    GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL,
        false);
    mCardRecyclerView.setHasFixedSize(true);
    mCardRecyclerView.setLayoutManager(mLayoutManager);
    mCardRecyclerView.setAdapter(mAdapter);

  }

  @Override
  public void onCardClick(int position) {
    if (position == 0) {
      Intent hajiIntent = new Intent(CardsActivity.this, HajiActivity.class);
      startActivity(hajiIntent);
    } else if (position == 1) {
      Intent guideIntent = new Intent(CardsActivity.this, GuideActivity.class);
      startActivity(guideIntent);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mFirebaseAuth.addAuthStateListener(mAuthStateListener);

  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mAuthStateListener != null) {
      mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
  }

  private void signIn() {
    /*set Firebase Auth Listener*/
    mAuthStateListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

        } else {
          /*Choose authentication providers*/
          List<IdpConfig> providers = Arrays.asList(
              new AuthUI.IdpConfig.PhoneBuilder().build(),
              new AuthUI.IdpConfig.GoogleBuilder().build());

          /*Create and launch sign-in intent*/
          startActivityForResult(
              AuthUI.getInstance()
                  .createSignInIntentBuilder()
                  .setAvailableProviders(providers)
                  .build(),
              RC_SIGN_IN);
        }
      }
    };
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      IdpResponse response = IdpResponse.fromResultIntent(data);

      if (resultCode == RESULT_OK) {
        // Successfully signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // ...
      } else {
        // Sign in failed. If response is null the user canceled the
        // sign-in flow using the back button. Otherwise check
        // response.getError().getErrorCode() and handle the error.
        // ...
        finish();
      }
    }
  }

}

