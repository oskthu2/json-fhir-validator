package com.gitb.vs;

import java.util.Collections;
import java.util.Map;

public class ValidateRequest {
  private Map<String, Object> input;

  public ValidateRequest() {}

  public ValidateRequest(Map<String, Object> input) {
    this.input = input;
  }

  public Map<String, Object> getInput() {
    return input == null ? Collections.emptyMap() : input;
  }

  public void setInput(Map<String, Object> input) {
    this.input = input;
  }
}



