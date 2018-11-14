package net.maidsafe.sample.services;

import android.content.Context;
import android.net.Uri;

import net.maidsafe.sample.model.Task;
import net.maidsafe.sample.model.TodoList;

import java.util.List;

public interface ITodoService {

    String generateAuthURL() throws Exception;

    void connect(Uri authResponse, OnDisconnected disconnected) throws Exception;

    void getAppData() throws Exception;

    List<TodoList> fetchSections() throws Exception;

    TodoList addSection(String sectionTitle) throws Exception;

    long getSectionsLength() throws Exception;

    List<Task> fetchListItems(TodoList listInfo) throws Exception;

    void addTask(Task task, TodoList listInfo) throws Exception;

    void deleteTask(Task task, TodoList listInfo) throws Exception;

    void updateTaskStatus(Task task, TodoList listInfo) throws Exception;

    long getEntriesLength(TodoList listInfo) throws Exception;

    void reconnect() throws Exception;

}
