package com.nloops.lossless.models;

public class Guide {

  private String mGuideName;
  private boolean isGuideOnline;

  public Guide(boolean isGuideOnline) {
    this.isGuideOnline = isGuideOnline;
  }

  public String getGuideName() {
    return mGuideName;
  }

  public boolean isGuideOnline() {
    return isGuideOnline;
  }

  public void setGuideOnline(boolean guideOnline) {
    isGuideOnline = guideOnline;
  }
}
