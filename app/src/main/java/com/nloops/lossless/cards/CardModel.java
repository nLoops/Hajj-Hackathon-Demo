package com.nloops.lossless.cards;

/**
 * This Model will acts like one Card Item to define differrents cards types.
 */
public class CardModel {

  private String mCardName;
  private String mCardStatus;
  private int mCardImageID;

  public CardModel(String cardName, String cardStatus, int cardImageID) {
    this.mCardName = cardName;
    this.mCardStatus = cardStatus;
    this.mCardImageID = cardImageID;
  }

  public String getCardName() {
    return mCardName;
  }

  public String getCardStatus() {
    return mCardStatus;
  }

  public int getCardImageID() {
    return mCardImageID;
  }
}
