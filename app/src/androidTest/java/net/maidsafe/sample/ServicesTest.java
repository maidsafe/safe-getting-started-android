package net.maidsafe.sample;

import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.system.Os;
import android.util.Log;

import net.maidsafe.sample.model.Task;
import net.maidsafe.sample.model.TodoList;
import net.maidsafe.sample.services.ITodoService;
import net.maidsafe.sample.services.OnDisconnected;
import net.maidsafe.sample.services.SafeTodoService;
import net.maidsafe.sample.viewmodel.MockServices;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Date;
import java.util.List;

public class ServicesTest {

    private ITodoService service;
    private OnDisconnected onDisconnected;

    public ServicesTest() {
        service = new SafeTodoService(InstrumentationRegistry.getContext());
        File mockVaultDir = new File(Os.getenv("SAFE_MOCK_VAULT_PATH"));
        mockVaultDir.mkdir();
        onDisconnected = () -> {
          Log.d("STAGE:", "Disconnected from the Network");
        };
    }

    @Test
    public void authURITest() throws Exception {
        String AUTH_URL_PREFIX = "safe-auth://";
        String authUrl = service.generateAuthURL();
        Assert.assertNotNull(authUrl);
        Assert.assertTrue(authUrl.contains(AUTH_URL_PREFIX));
    }

    @Test
    public void authResponseTest() throws Exception {
        String APP_ID = "net.maidsafe.sample";
        String authUrl = service.generateAuthURL();
        Uri authResponse = MockServices.mockAuthenticate(authUrl);
        Assert.assertEquals(authResponse.getScheme(), APP_ID);
        service.connect(authResponse, onDisconnected);
    }

    @Test
    public void mutableDataTest() throws Exception {
        String authUrl = service.generateAuthURL();
        Uri authResponse = MockServices.mockAuthenticate(authUrl);
        {
            service.connect(authResponse, onDisconnected);
            // Prepare mutable data for the first time
            service.getAppData();
            // add data
            service.addSection(randomAlphaNumeric(5));
            service.addSection(randomAlphaNumeric(5));
            Assert.assertEquals(2, service.getSectionsLength());
            TodoList todoList = service.fetchSections().get(0);
            service.addTask(new Task(randomAlphaNumeric(10), new Date()), todoList);
            service.addTask(new Task(randomAlphaNumeric(10), new Date()), todoList);
            List<Task> taskList = service.fetchListItems(todoList);
            Assert.assertEquals(2, taskList.size());
            // update data
            Task t = taskList.get(0);
            service.updateTaskStatus(t, todoList);
            taskList = service.fetchListItems(todoList);
            Assert.assertEquals(t.getVersion(), taskList.get(0).getVersion());
            // delete data
            service.deleteTask(t, todoList);
            taskList = service.fetchListItems(todoList);
            Assert.assertEquals(1, taskList.size());
        }
        // fetching existing data
        ITodoService newConnection = new SafeTodoService(InstrumentationRegistry.getContext());
        newConnection.connect(authResponse, onDisconnected);
        newConnection.getAppData();
        List<TodoList> list = service.fetchSections();
        Assert.assertEquals(2, list.size());
    }

    private static String randomAlphaNumeric(int count) {
        final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

}
