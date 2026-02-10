package com.sisferrete.platform.audit;

public final class RequestMetadataHolder {
  private static final ThreadLocal<RequestMetadata> HOLDER = new ThreadLocal<>();

  private RequestMetadataHolder() {}

  public static void set(RequestMetadata metadata) {
    HOLDER.set(metadata);
  }

  public static RequestMetadata get() {
    return HOLDER.get();
  }

  public static void clear() {
    HOLDER.remove();
  }
}
