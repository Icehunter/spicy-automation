package com.spicy.pipeline

public enum NotificationTypes {
  pipelineEnd("completed pipeline execution"),
  pipelineStart("began new pipeline execution"),

  public final String message

  NotificationTypes(String message) {
    this.message = message
  }
}
