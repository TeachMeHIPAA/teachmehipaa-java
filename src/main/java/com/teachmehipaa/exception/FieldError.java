package com.teachmehipaa.exception;

import java.io.Serializable;

/**
 * One invalid field from a {@code validation_failed} problem.
 *
 * @param path the dot-joined path of the field, or {@code Idempotency-Key} for a missing header
 * @param message what is wrong with it
 */
public record FieldError(String path, String message) implements Serializable {}
