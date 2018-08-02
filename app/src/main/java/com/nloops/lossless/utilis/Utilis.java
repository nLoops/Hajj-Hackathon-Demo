package com.nloops.lossless.utilis;

import android.content.Context;
import com.google.android.gms.maps.model.LatLng;
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
        context.getString(R.string.str_moc_init_status), R.drawable.ic_muslim_second));
    models.add(new CardModel(context.getString(R.string.str_mock_guide_name),
        context.getString(R.string.str_mock_guide_online), R.drawable.ic_guide_avatar));
    return models;
  }

  public static ArrayList<LatLng> createLatLngTable() {
    ArrayList<LatLng> locations = new ArrayList<>();
    locations.add(new LatLng(21.619053, 39.160785));
    locations.add(new LatLng(21.619621, 39.160828));
    locations.add(new LatLng(21.619721, 39.161193));
    locations.add(new LatLng(21.620409, 39.161043));
    locations.add(new LatLng(21.620748, 39.160989));
    return locations;
  }

}
