package com.todoapp.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/** Explicit transport records: no credentials cross the endpoint boundary. */
@org.jspecify.annotations.NullMarked
public final class Models {
    private Models() {}
    public record User(String id, String username, String email, String theme, String timezone) {}
    public record Project(String id, String name, String color, boolean archived, boolean inbox, String role, long version) {}
    public record Member(String projectId, String userId, String username, String role) {}
    public record Invitation(String id, String projectId, String projectName, String role) {}
    public record Step(@NotBlank @Size(max=36) String id, @NotBlank @Size(max=200) String title, boolean done) {}
    public record Comment(String id, String authorId, String author, String text, String createdAt) {}
    public record Activity(String id, String actor, String action, String createdAt) {}
    public record Recurrence(@NotNull String frequency, @Min(1) @Max(365) int interval,
        @NotNull List<Integer> weekdays, @NotNull String endDate, @Min(0) int remaining) {}
    public record Task(
        @NotBlank String id, @NotBlank String projectId, @NotBlank @Size(max=200) String title,
        @NotNull @Size(max=10000) String description, @NotNull String status, @NotNull String priority,
        @NotNull String assigneeId, @NotNull List<@NotBlank @Size(max=40) String> tags,
        @NotNull String dueDate, @NotNull String dueTime, @NotNull String timezone,
        @Min(0) @Max(100000) int estimateMinutes, @NotNull String myDay,
        @NotNull @Valid Recurrence recurrence, @NotNull List<String> reminders,
        @NotNull @Valid List<Step> steps,
        @NotNull List<Comment> comments, @NotNull List<Activity> activity,
        double position, long version, String createdAt, String updatedAt, String completedAt,
        String deletedAt, String conflictOf) {}
    public record Mutation(@NotBlank String operationId, @NotBlank String kind, long baseVersion, @NotNull @Valid Task task) {}
    public record Outcome(String operationId, String status, String message, String taskId) {}
    public record SyncRequest(long cursor, @NotNull @Valid List<Mutation> mutations, @NotNull List<String> knownProjectIds) {}
    public record Notice(String id, String projectId, String taskId, String message, String createdAt, boolean read) {}
    public record SavedFilter(String id, String name, String query) {}
    public record SyncResponse(long cursor, List<Task> tasks, List<String> removedTaskIds,
        List<Project> projects, List<Member> members, List<Invitation> invitations,
        List<Notice> notifications, List<SavedFilter> filters, List<Outcome> outcomes) {}
    public record ProjectInput(@NotBlank @Size(max=100) String name, @Pattern(regexp="#[0-9a-fA-F]{6}") String color) {}
    public record Register(@NotBlank @Pattern(regexp="[a-zA-Z0-9_]{3,50}") String username,
        @NotBlank @Email @Size(max=254) String email, @NotBlank @Size(min=12,max=72) String password, @NotBlank String timezone) {}
}
