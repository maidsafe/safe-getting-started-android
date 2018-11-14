package net.maidsafe.sample.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import net.maidsafe.sample.R;
import net.maidsafe.sample.services.ItemClickedListener;
import net.maidsafe.sample.viewmodel.ListViewModel;
import net.maidsafe.sample.model.Task;

import java.util.List;

public class ListFragment extends Fragment {

    FloatingActionButton addTaskButton;
    ListAdapter taskListAdapter;
    List<Task> taskList;
    private ListViewModel viewModel;
    private OnFragmentInteractionListener mListener;
    private RecyclerView taskListView;
    private TextView noDataText;


    public ListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(ListViewModel.class);
        viewModel.setListDetails(getArguments().getParcelable("listInfo"));

        final Observer<List<Task>> taskObserver = tasks -> taskListAdapter.updateList(tasks);

        viewModel.getTaskList().observe(this, taskObserver);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        taskListView = view.findViewById(R.id.taskListView);
        noDataText = view.findViewById(R.id.no_data_list);
        addTaskButton = view.findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v -> showAddTaskDialog());

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        taskListView.setLayoutManager(layoutManager);
        taskList = viewModel.getTaskList().getValue();
        taskListAdapter = new ListAdapter(taskList);
        taskListView.setAdapter(taskListAdapter);
        taskListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                taskList = taskListAdapter.getTaskList();
                updateView();
            }
        });
        updateView();
        return view;
    }

    public void updateView() {
        if(taskList.isEmpty()) {
            taskListView.setVisibility(View.GONE);
            noDataText.setVisibility(View.VISIBLE);
        } else {
            taskListView.setVisibility(View.VISIBLE);
            noDataText.setVisibility(View.GONE);
        }
    }

    public void showAddTaskDialog() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(getActivity()).create();
        View dialogView = View.inflate(getActivity(), R.layout.new_task_dialog, null);

        final EditText newTaskText = dialogView.findViewById(R.id.newTaskText);
        newTaskText.setOnFocusChangeListener((v, hasFocus) -> newTaskText.post(() -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(newTaskText, InputMethodManager.SHOW_IMPLICIT);
        }));
        newTaskText.requestFocus();
        Button addNewTask = dialogView.findViewById(R.id.addNewTask);
        Button cancelAddTask = dialogView.findViewById(R.id.cancelAddTask);

        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addNewTask.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cancelAddTask.setOnClickListener(view -> dialogBuilder.dismiss());
        addNewTask.setOnClickListener(view -> {
            String taskText = newTaskText.getText().toString();
            onButtonPressed(view, taskText);
            dialogBuilder.dismiss();
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void onButtonPressed(View v, Object object) {
        if (mListener != null) {
            mListener.onFragmentInteraction(v, object);
        }
    }

    public void onItemClicked(View v, Object object) {
        if (mListener != null) {
            mListener.onFragmentInteraction(v, object);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.prepareList();
        mListener.setActionBarTitle(viewModel.getListInfo().getListTitle());
        mListener.showActionBarBack(true);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View v, Object object);
        void setActionBarTitle(String title);
        void showActionBarBack(boolean displayed);
    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private List<Task> taskList;

        ListAdapter(List<Task> taskList) {
            this.taskList = taskList;

        }

        private List<Task> getTaskList() {
            return taskList;
        }

        public void updateList(List<Task> tasks) {
            this.taskList = tasks;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);

            ItemClickedListener listener = (itemView, task) -> {
                onItemClicked(itemView, task);
            };

            ViewHolder viewHolder = new ViewHolder(view, listener);
            return viewHolder;

        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            holder.checkBox.setChecked(taskList.get(position).getComplete());
            holder.taskDescription.setText(taskList.get(position).getDescription());

        }

        @Override
        public int getItemCount() {
            if (taskList != null)
                return taskList.size();
            else
                return 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ItemClickedListener itemClickListener;
            CheckBox checkBox;
            TextView taskDescription;
            Button deleteButton;

            public ViewHolder(View view, ItemClickedListener listener) {
                super(view);
                itemClickListener = listener;
                view.setOnClickListener(this);
                this.checkBox = view.findViewById(R.id.taskCheckbox);
                checkBox.setOnClickListener(v -> onButtonPressed(v, taskList.get(getAdapterPosition())));
                this.taskDescription = view.findViewById(R.id.taskDescription);
                this.deleteButton = view.findViewById(R.id.taskDeleteButton);
                this.deleteButton.setOnClickListener(v -> onButtonPressed(v, taskList.get(getAdapterPosition())));
            }

            @Override
            public void onClick(View view) {
                itemClickListener.onClick(view, taskList.get(getAdapterPosition()));
            }
        }

    }
}
