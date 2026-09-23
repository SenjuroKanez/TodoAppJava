# Operation guide

[Home](README.md) · [Installation and startup](SETUP.md)

## What TodoApp does

TodoApp combines daily planning, task organization, shared projects, and offline editing in one browser workspace. Java services handle permissions, persistence, recurring tasks, reminders, and synchronization. The browser keeps a local task copy and a queue of edits so you can work through a lost connection.

## First use

1. Start the application using [SETUP.md](SETUP.md), then open <http://localhost:8080/login>.
2. Choose **Create an account**. Use a username of 3–50 letters, digits, or underscores, an email address, and a password of at least 12 characters (at most 72 UTF-8 bytes).
3. Sign in with your username or email. Each account starts with a personal Inbox.
4. Open **Settings** to choose Light, Dark, or your device's appearance, and check your timezone. Save preferences.

There is no default administrator account. The `admin` name in the screenshots is an ordinary demonstration account. Email verification and forgotten-password recovery are not implemented.

## Capture and edit tasks

Type in the quick-add field and press Enter, or choose **Add task** to open the full editor. Enter a title at the top, add details, then choose **Save task**. Click a task title or its edit button to reopen it.

The editor includes description, project, priority, due date/time, assignee, estimated minutes, comma-separated tags, timezone, and **Small steps**. Add steps with Enter or Add; check them off, edit their text, move them up, or remove them. Use **Add to My Day** to include a task in today's plan.

Click the round completion button to complete a task; click it again to reopen. Row checkboxes select tasks for bulk completion, adding to My Day, or moving to Trash. Drag task rows to reorder within a project.

## Choose a view

| View | What it shows |
| --- | --- |
| My Day | Tasks marked for the current day in your configured timezone. |
| Inbox | Tasks in your personal Inbox. |
| Today | Open tasks due today. |
| Upcoming | Open tasks with a future due date. |
| Overdue | Open tasks with a due date before today. |
| Assigned to me | Open tasks assigned to your account. |
| Shared with others | Tasks in projects with other members. |
| All tasks | Tasks in accessible, non-archived projects. |
| Completed | Completed tasks in active projects. |
| Trash | Soft-deleted tasks available for restoration. |

Select a project in the sidebar to focus on it. **Calendar** displays dated tasks from the current view and filters; undated tasks remain available in List. Use the month arrows to navigate.

Search matches title, description, and tags. **Filter** combines status, priority, project, assignee, tag, date ranges, and recurring/one-time tasks. Name and save a filter for reuse. Clear filters or change views if a task seems missing.

Keyboard shortcuts: `n` focuses quick capture when you are not typing in a form; `Ctrl+K` or `Cmd+K` focuses search. Escape closes a dialog.

## Projects and collaboration

Create a project using the plus beside Projects. Choose its name and color. In the project view, open **Share & manage** to edit details or invite an existing account by exact username or email. Invitations appear under the recipient's notification bell; the recipient must accept.

| Role | Permissions |
| --- | --- |
| Owner | Edit tasks, invite/remove members, set roles, and manage/archive/delete the project. |
| Editor | Create and edit project tasks, including comments and assignments. |
| Viewer | Read project tasks; cannot change them. |

The owner cannot be demoted or removed. Removing a member also clears their task assignments. Inbox cannot be shared, archived, or deleted. Moving tasks between shared projects requires ownership of both projects.

Open a saved task's **Comments** tab to post a message. The **Activity** tab records server-confirmed changes. Other members' changes arrive on refresh, reconnection, focus, or the periodic foreground sync; this is not live collaborative typing.

## Repeating tasks and reminders

In task details, expand **Repeat & reminders**. Add a due date before enabling daily, weekly, monthly, or yearly recurrence. Set an interval, optional end date, and occurrences remaining (`0` means unlimited). Weekly rules can select weekdays. Complete the task to generate the next future occurrence; missed dates are skipped. Monthly/yearly dates clamp to the last valid day and subsequent occurrences use that resulting date.

The reminder input uses your device's local time. Save and synchronize it so the server can process it. Notifications appear under the bell. To receive browser notifications, the server needs VAPID keys; choose **Enable browser reminders** in Settings and grant permission. The server must remain running, and delivery timing depends on the browser/provider. You can disable browser reminders in Settings.

## Offline use and conflicts

Sign in online first and allow the app to load and synchronize before disconnecting. Task edits are saved in this browser and queued. The top status shows Offline, pending changes, Syncing, or All changes saved. Reconnect and keep the app open, or click that status to synchronize.

Project management, invitations, preferences, and authentication require a connection. Closed-browser synchronization is not guaranteed. If a session expires, sign in again using the same account to resume.

If another edit changed the same task, your stale edit is preserved as a **Conflict copy** rather than replacing the server version. Review both tasks and merge the useful information manually. Rejected edits, including changes to a project you can no longer access, appear in **Preserved offline changes** and can be exported before dismissal. Later queued edits that depend on a conflict are also retained for review.

## Export, delete, and sign out

**Settings → Export device backup** downloads JSON containing the local snapshot, pending edits, and rejected changes. It is a device export, not a complete server backup; there is no automatic import/restore interface. Store it privately because it contains account and task information.

Move a task to Trash from its editor and restore it from the Trash view. The server purges trashed tasks after 30 days. Archive a project to stop editing it while retaining its contents; restore it from project settings. Deleting a project permanently deletes its tasks for everyone.

Sign out through Settings. Sign-out clears local task data and warns before discarding pending/rejected edits. Export or synchronize first. Browser storage can also be evicted by the browser, so offline copies should not be your only backup.

## Suggested showcase walkthrough

Create a project named `Portfolio launch`; add `Write the project overview`, `Review screenshots`, and `Publish the demo`. Give a task a due date, priority, and two small steps. Add it to My Day, complete one task, switch to Calendar, and demonstrate filters. Use a second account in a separate browser profile to show invitations and read-only access. The [README gallery](README.md#screenshots) contains the supplied screenshots.
