package records;

public record TestRecordGenericVarargNoBody<T>(T first, T... other) {
  @SafeVarargs
  public TestRecordGenericVarargNoBody {}
}