package net.maidsafe.sample.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.maidsafe.sample.R;
import net.maidsafe.sample.model.TodoList;
import net.maidsafe.sample.services.ItemClickedListener;
import net.maidsafe.sample.viewmodel.SectionViewModel;

import java.util.List;

public class ListHomeFragment extends Fragment {

    private RecyclerView listHomeView;
    private TextView noDataText;
    FloatingActionButton addSectionButton;
    private ListsHomeAdapter adapter;
    private List<TodoList> sectionsList;
    private SectionViewModel viewModel;

    private OnFragmentInteractionListener mListener;

    public ListHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(SectionViewModel.class);

        final Observer<List<TodoList>> sectionsObserver = sections -> adapter.updateList(sections);

        viewModel.getSections().observe(this, sectionsObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setActionBarTitle("My Lists");
        mListener.showActionBarBack(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_list_home, container, false);
        listHomeView  = view.findViewById(R.id.list_home_view);
        noDataText = view.findViewById(R.id.no_data_section);
        addSectionButton = view.findViewById(R.id.addSectionButton);
        addSectionButton.setOnClickListener(v -> showAddSectionDialog());

        sectionsList = viewModel.getSections().getValue();
        adapter = new ListsHomeAdapter(sectionsList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        listHomeView.setLayoutManager(mLayoutManager);
        listHomeView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(1), true));
        listHomeView.setItemAnimator(new DefaultItemAnimator());
        listHomeView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                sectionsList = adapter.getSectionsList();
                updateView();
            }
        });
        updateView();
        return view;
    }

    private void updateView() {
        if(sectionsList.isEmpty()) {
            listHomeView.setVisibility(View.GONE);
            noDataText.setVisibility(View.VISIBLE);
        } else {
            listHomeView.setVisibility(View.VISIBLE);
            noDataText.setVisibility(View.GONE);
        }
    }

    public void showAddSectionDialog() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(getActivity()).create();
        View dialogView = View.inflate(getActivity(), R.layout.new_section_dialog, null);

        final EditText newSectionText = dialogView.findViewById(R.id.new_section_edit_text);
        newSectionText.setOnFocusChangeListener((v, hasFocus) -> newSectionText.post(() -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(newSectionText, InputMethodManager.SHOW_IMPLICIT);
        }));
        newSectionText.requestFocus();
        Button addNewSection = dialogView.findViewById(R.id.new_section_add_section);
        Button cancelAddSection = dialogView.findViewById(R.id.new_section_cancel);
        newSectionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addNewSection.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cancelAddSection.setOnClickListener(view -> dialogBuilder.dismiss());
        addNewSection.setOnClickListener(view -> {
            String taskText = newSectionText.getText().toString();
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View v, Object o);
        void setActionBarTitle(String title);
        void showActionBarBack(boolean displayed);
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    class ListsHomeAdapter extends RecyclerView.Adapter<ListsHomeAdapter.MyViewHolder> {

        private List<TodoList> sectionsList;

        public ListsHomeAdapter(List<TodoList> sectionsList) {
            this.sectionsList = sectionsList;
        }

        public void updateList(List<TodoList> sections) {
            this.sectionsList = sections;
            notifyDataSetChanged();
        }

        private List<TodoList> getSectionsList() {
            return sectionsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_home_card, parent, false);

            ItemClickedListener listener = (itemView, task) -> {
                onItemClicked(itemView, task);
            };

            return new MyViewHolder(view, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.title.setText(sectionsList.get(position).getListTitle());
        }

        @Override
        public int getItemCount() {
            return sectionsList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ItemClickedListener itemClickListener;
            public TextView title;
            SectionViewModel viewModel;

            public MyViewHolder(View view, ItemClickedListener listener) {
                super(view);
                itemClickListener = listener;
                view.setOnClickListener(this);
                viewModel = ViewModelProviders.of(getActivity()).get(SectionViewModel.class);
                title = view.findViewById(R.id.list_card_title);
            }

            @Override
            public void onClick(View view) {
                itemClickListener.onClick(view, sectionsList.get(getAdapterPosition()));
            }
        }

    }

}


