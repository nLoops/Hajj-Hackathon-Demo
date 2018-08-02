package com.nloops.lossless.cards;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.nloops.lossless.R;
import com.nloops.lossless.cards.CardsAdapter.CardsViewHolder;
import java.util.ArrayList;

public class CardsAdapter extends RecyclerView.Adapter<CardsViewHolder> {

  /*ref of Card Click interface*/
  private OnCardClickListener mListener;

  /*this array list will holds the models that needs to display*/
  private ArrayList<CardModel> mModelsArrayList;
  private Context mContext;

  public CardsAdapter(ArrayList<CardModel> models, OnCardClickListener listener, Context context) {
    this.mModelsArrayList = models;
    this.mListener = listener;
    this.mContext = context;
  }

  /*Declare Interface for Card Click*/
  public interface OnCardClickListener {

    void onCardClick(int position);
  }

  @NonNull
  @Override
  public CardsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    int layoutID = R.layout.card_list_item;
    View returnedView = LayoutInflater.from(parent.getContext()).inflate(
        layoutID, parent, false);
    return new CardsViewHolder(returnedView);
  }

  @Override
  public void onBindViewHolder(@NonNull final CardsViewHolder holder, int position) {
    if (mModelsArrayList != null && !mModelsArrayList.isEmpty()) {
      /*Get the Current Model upon position*/
      final CardModel currentModel = mModelsArrayList.get(position);
      /*Set Current View elements values*/
      holder.mCardImageView.post(new Runnable() {
        @Override
        public void run() {
          holder.mCardImageView.setImageResource(currentModel.getCardImageID());
        }
      });
      holder.mCardName.setText(currentModel.getCardName());
      holder.mCardStatus.setText(currentModel.getCardStatus());
      if (currentModel.getCardName().equals(mContext.getString(R.string.str_mock_guide_name))) {
        holder.mCardStatus.setTextColor(ContextCompat.getColor(mContext, R.color.colorGreen));
      }
    }
  }

  @Override
  public int getItemCount() {
    return mModelsArrayList != null ? mModelsArrayList.size() : 0;
  }

  /**
   * This class will holds the Views for each Element in {@link #mModelsArrayList}
   */
  class CardsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    @BindView(R.id.iv_card_item)
    ImageView mCardImageView;
    @BindView(R.id.tv_card_name)
    TextView mCardName;
    @BindView(R.id.tv_card_status)
    TextView mCardStatus;

    public CardsViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      mListener.onCardClick(getAdapterPosition());
    }
  }

}
