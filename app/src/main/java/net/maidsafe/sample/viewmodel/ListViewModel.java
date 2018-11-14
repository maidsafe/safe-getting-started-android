package net.maidsafe.sample.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import net.maidsafe.safe_app.MDataInfo;
import net.maidsafe.sample.model.Task;
import net.maidsafe.sample.model.TodoList;
import net.maidsafe.sample.services.AsyncOperation;
import net.maidsafe.sample.services.IFailureHandler;
import net.maidsafe.sample.services.IProgressHandler;
import net.maidsafe.sample.services.ITodoService;
import net.maidsafe.sample.services.Result;
import net.maidsafe.sample.services.SafeApi;
import net.maidsafe.sample.services.SafeTodoService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListViewModel extends AndroidViewModel implements IFailureHandler, IProgressHandler {

    private MutableLiveData<Boolean> loading;
    private List<Task> taskList;
    private TodoList listInfo;
    private MDataInfo mdInfo;
    private MutableLiveData<List<Task>> liveTaskList;
    private ITodoService todoService;

    public ListViewModel(Application application) {
        super(application);
        taskList = new ArrayList<>();
        loading = new MutableLiveData<>();
        loading.setValue(false);
        todoService = new SafeTodoService(application.getApplicationContext());
        liveTaskList = new MutableLiveData<>();
        liveTaskList.setValue(taskList);
    }

    public TodoList getListInfo() {
        return listInfo;
    }

    public MutableLiveData<List<Task>> getTaskList() {
        return liveTaskList;
    }

    public void addTask(String description) {
        Task task = new Task(description, new Date());
        new AsyncOperation(this).execute(() -> {
            try {
                todoService.addTask(task, listInfo);
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }

        }).onResult((result) -> {
            taskList.add(task);
            liveTaskList.setValue(taskList);
        }).onException(this);

    }

    public void deleteTask(Task task) {
        new AsyncOperation(this).execute(() -> {
            try {
                todoService.deleteTask(task, listInfo);
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            taskList.remove(task);
            liveTaskList.setValue(taskList);
        }).onException(this);
    }

    public void updateTask(Task task) {
        new AsyncOperation(this).execute(() -> {
            try {
//                Services.updateEntry(mDataInfo, client, task);
                todoService.updateTaskStatus(task, listInfo);
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {

        }).onException(this);

    }

    private void fetchListItems() {
        new AsyncOperation(this).execute(() -> {
            try {
                List<Task> list = todoService.fetchListItems(listInfo);
                return new Result<List>(list);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {
            if (result != null) {
                taskList = (List<Task>) result;
                liveTaskList.setValue(taskList);
            }
        });
    }

    public void prepareList() {
        try {
            mdInfo = SafeApi.getInstance(null).deserializeMdInfo(listInfo.getContent());
            fetchListItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onFailure(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void updateStatus(boolean isLoading) {
        loading.setValue(isLoading);
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public void setListDetails(TodoList listInfo) {
        this.listInfo = listInfo;
    }

    public void clearList() {
        taskList.clear();
        if(liveTaskList != null) {
            liveTaskList.setValue(taskList);
        }
    }
}
