package com.nloops.lossless.cards;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.nloops.lossless.HajiActivity;
import com.nloops.lossless.R;
import com.nloops.lossless.utilis.Utilis;

public class CardsActivity extends AppCompatActivity implements CardsAdapter.OnCardClickListener {


  /*get Activity elements ref using ButterKnife library*/
  @BindView(R.id.rv_cards_activity)
  RecyclerView mCardRecyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cards);
    ButterKnife.bind(this);
    /*init and feed adapter with data*/
    CardsAdapter mAdapter = new CardsAdapter(Utilis.createListModels(CardsActivity.this), this);
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

    }
  }
}
