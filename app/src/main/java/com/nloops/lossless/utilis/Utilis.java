package com.nloops.lossless.utilis;

import android.content.Context;
import com.nloops.lossless.R;
import com.nloops.lossless.cards.CardModel;
import java.util.ArrayList;

public class Utilis {

  /*private constructor to prevent calling*/
  private Utilis() {
  }

  public static ArrayList<CardModel> createListModels(Context context) {
    ArrayList<CardModel> models = new ArrayList<>();
    models.add(new CardModel(context.getString(R.string.str_mock_haji_name),
        context.getString(R.string.str_moc_init_status), R.drawable.ic_map));
    models.add(new CardModel(context.getString(R.string.str_mock_guide_name),
        context.getString(R.string.str_moc_init_status), R.mipmap.ic_fab_route));
    return models;
  }

}
