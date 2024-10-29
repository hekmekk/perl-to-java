package com.sippy.wrapper.parent.request;

public class GetTnbListRequest {

  String number;

  public String getNumber() {
    return number;
  }

  public void setNumber(final String number) {
    this.number = number;
  }

  @Override
  public String toString() {
    return "GetTnbListRequest [number=" + number + "]";
  }
}
