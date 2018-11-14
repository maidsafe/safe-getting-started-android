package net.maidsafe.sample.services;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import net.maidsafe.api.Client;
import net.maidsafe.api.model.App;
import net.maidsafe.api.model.Request;
import net.maidsafe.safe_app.AppExchangeInfo;
import net.maidsafe.safe_app.AuthReq;
import net.maidsafe.safe_app.ContainerPermissions;
import net.maidsafe.safe_app.MDataEntry;
import net.maidsafe.safe_app.MDataInfo;
import net.maidsafe.sample.R;
import net.maidsafe.sample.model.Task;
import net.maidsafe.sample.model.TodoList;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SafeTodoService implements ITodoService, net.maidsafe.api.listener.OnDisconnected {

    private SafeApi api;
    private MDataInfo appData;
    final App app = new App("net.maidsafe.sample", "Safe TODO Java",
                          "Maidsafe.net", "0.1.0");
    private OnDisconnected onDisconnected;

    @Override
    public void disconnected(Object o) {
        if (onDisconnected == null) {
            return;
        }
        onDisconnected.onDisconnected();
    }

    public SafeTodoService(Context context) {
        try {
            api = SafeApi.getInstance(context);
        } catch (Exception e) {
            Toast.makeText(context, context.getText(R.string.init_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public String generateAuthURL() throws Exception {

        final String AUTH_URL_PREFIX = "safe-auth://";
        AppExchangeInfo appExchangeInfo = new AppExchangeInfo(app.getId(), "",
                app.getName(), app.getVendor());
        ContainerPermissions[] permissions = new ContainerPermissions[0];
        AuthReq authReq = new AuthReq(appExchangeInfo, true,
                permissions, permissions.length, 0);
        Request request = Client.encodeAuthReq(authReq).get();

        return AUTH_URL_PREFIX + app.getId() + '/' + request.getUri();

    }

    @Override
    public void connect(Uri authResponse, OnDisconnected disconnected) throws Exception {
        String response = authResponse.toString().replaceAll(".*\\/+", "");
        onDisconnected = disconnected;
        api.connect(response, app.getId(), this);
    }

    @Override
    public void getAppData() throws Exception {
        appData =  api.getSectionsFromAppContainer();
    }

    @Override
    public long getSectionsLength() throws Exception{
        return api.getEntriesLength(appData);
    }

    @Override
    public List<TodoList> fetchSections() throws Exception {
        List<TodoList> sectionsList = new ArrayList<>();
        List<MDataEntry> entries = api.getEntries(appData);
        entries.forEach(mDataEntry -> {
            if(mDataEntry.getValue().getContentLen() != 0) {
                sectionsList.add(TodoList.getListInfo(mDataEntry.getValue().getContent()));
            }
        });
        return sectionsList;
    }

    @Override
    public TodoList addSection(String sectionTitle) throws Exception {
        MDataInfo listInfo = api.newMutableData(16290);
        api.insertPermissions(listInfo);
        byte[] serializedMdInfo = api.serializeMdInfo(listInfo);
        TodoList list = new TodoList(sectionTitle, new Date(), serializedMdInfo);
        if(api.getEntriesLength(appData) == 0) {
            api.insertPermissions(appData);
        }
        byte[] key = getByteArray(list.getDate().hashCode());
        byte[] value = list.toStream();
        api.addEntry(key, value, appData);
        return list;
    }

    @Override
    public List<Task> fetchListItems(TodoList listInfo) throws Exception {
        MDataInfo mDataInfo = api.deserializeMdInfo(listInfo.getContent());
        List<Task> taskList = new ArrayList<>();
        List<MDataEntry> entries = api.getEntries(mDataInfo);
        entries.forEach(mDataEntry -> {
            if(mDataEntry.getValue().getContentLen() !=0) {
                taskList.add(Task.toTask(mDataEntry.getValue().getContent()));
            }
        });
        return taskList;
    }

    @Override
    public void addTask(Task task, TodoList listInfo) throws Exception {
        byte[] key = getByteArray(task.getDate().hashCode());
        byte[] value = task.toStream();
        MDataInfo mDataInfo = api.deserializeMdInfo(listInfo.getContent());
        api.addEntry(key, value, mDataInfo);
    }

    @Override
    public void deleteTask(Task task, TodoList listInfo) throws Exception {
        MDataInfo mDataInfo = api.deserializeMdInfo(listInfo.getContent());
        task.setVersion(task.getVersion() + 1);
        byte[] key = getByteArray(task.getDate().hashCode());
        api.deleteEntry(key, task.getVersion(), mDataInfo);
    }

    @Override
    public void updateTaskStatus(Task task, TodoList listInfo) throws Exception {
        MDataInfo mDataInfo = api.deserializeMdInfo(listInfo.getContent());
        task.setComplete(!task.getComplete());
        task.setVersion(task.getVersion() + 1);
        byte[] key = getByteArray(task.getDate().hashCode());
        byte[] newValue = task.toStream();
        api.updateEntry(key, newValue, task.getVersion(), mDataInfo);
    }

    @Override
    public long getEntriesLength(TodoList listInfo) throws Exception {
        MDataInfo mDataInfo = api.deserializeMdInfo(listInfo.getContent());
        return api.getEntriesLength(mDataInfo);
    }

    @Override
    public void reconnect() throws Exception{
        api.reconnect();
    }

    private byte[] getByteArray(int x) {
        return ByteBuffer.allocate(4).putInt(x).array();
    }

}
