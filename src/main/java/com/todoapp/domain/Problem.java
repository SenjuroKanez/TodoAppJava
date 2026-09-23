package com.todoapp.domain;

/** A safe, user-facing domain error. */
public class Problem extends RuntimeException {
    public Problem(String message) { super(message); }
}
