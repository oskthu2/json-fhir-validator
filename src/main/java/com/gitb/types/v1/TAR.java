package com.gitb.types.v1;

import java.util.ArrayList;
import java.util.List;

public class TAR {
  private TestResultType result;
  private final List<TestAssertionReportType> reports = new ArrayList<>();

  public TestResultType getResult() {
    return result;
  }

  public void setResult(TestResultType result) {
    this.result = result;
  }

  public List<TestAssertionReportType> getReports() {
    return reports;
  }
}
