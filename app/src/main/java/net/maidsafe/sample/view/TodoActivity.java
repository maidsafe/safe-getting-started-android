package net.maidsafe.sample.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.system.Os;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import net.maidsafe.sample.BuildConfig;
import net.maidsafe.sample.R;
import net.maidsafe.sample.model.TodoList;
import net.maidsafe.sample.services.OnDisconnected;
import net.maidsafe.sample.viewmodel.ListViewModel;
import net.maidsafe.sample.viewmodel.MockServices;
import net.maidsafe.sample.viewmodel.SectionViewModel;
import net.maidsafe.sample.model.Task;

import androidx.navigation.Navigation;

public class TodoActivity extends AppCompatActivity implements AuthFragment.OnFragmentInteractionListener,
        ListFragment.OnFragmentInteractionListener, TodoItemDetailsFragment.OnFragmentInteractionListener,
        ListHomeFragment.OnFragmentInteractionListener {

    SectionViewModel sectionViewModel;
    ListViewModel listViewModel;
    ProgressBar progressBar;
    View host;
    OnDisconnected onDisconnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Os.setenv("SAFE_MOCK_VAULT_PATH", getApplicationContext().getFilesDir().getPath(), true);
            Log.d("SAFE_MOCK_VAULT_PATH", getApplicationContext().getFilesDir().getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        onDisconnected = () -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.disconnected_message), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.reconnect), view -> {
                            sectionViewModel.reconnect();
                        })
                        .show();
            });
        };
        sectionViewModel = ViewModelProviders.of(this).get(SectionViewModel.class);
        listViewModel = ViewModelProviders.of(this).get(ListViewModel.class);
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            sectionViewModel.connect(data, onDisconnected);

        }
        setContentView(R.layout.activity_auth);
        progressBar = findViewById(R.id.progressBar);
        host = findViewById(R.id.my_nav_host_fragment);

        final Observer<Boolean> loadingObserver = loading -> {
            if (loading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.INVISIBLE);
            }
        };

        sectionViewModel.getLoading().observe(this, loadingObserver);
        listViewModel.getLoading().observe(this, loadingObserver);
    }

    public void onFragmentInteraction(View v, Object object) {
        switch (v.getId()) {
            case R.id.authButton:
                sectionViewModel.authenticateApplication(getApplicationContext(), onDisconnected);
                Navigation.findNavController(v).navigate(R.id.listHomeFragment);
                break;
            case R.id.new_section_add_section:
                String sectionTitle = (String) object;
                sectionViewModel.addSection(sectionTitle);
                break;
            case R.id.taskDeleteButton:
                listViewModel.deleteTask((Task) object);
                break;
            case R.id.addNewTask:
                String taskDesc = (String) object;
                listViewModel.addTask(taskDesc);
                break;
            case R.id.taskCheckbox:
                listViewModel.updateTask((Task) object);
                break;
            case R.id.todoListItem:
                Bundle task = new Bundle();
                task.putParcelable("task", (Task) object);
                Navigation.findNavController(v).navigate(R.id.todoItemDetailsFragment, task);
                break;
            case R.id.list_home_card_layout:
                Bundle listDetails = new Bundle();
                listDetails.putParcelable("listInfo", (TodoList) object);
                Navigation.findNavController(v).navigate(R.id.listFragment, listDetails);
                break;
        }
    }

    @Override
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(BuildConfig.FLAVOR.equals("mock")) {
            menu.add(Menu.NONE, 3241, Menu.NONE, getString(R.string.simulate_disconnect));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 3241) {
            MockServices.simulateDisconnect();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.my_nav_host_fragment).navigateUp();
    }

    @Override
    public void onBackPressed() {
        listViewModel.clearList();
        super.onBackPressed();
    }

    public void setActionBarText(String text) {
        getActionBar().setTitle(text);
    }

    public void showActionBarBack(boolean displayed) {
        getSupportActionBar().setDisplayShowHomeEnabled(displayed);
    }


}
