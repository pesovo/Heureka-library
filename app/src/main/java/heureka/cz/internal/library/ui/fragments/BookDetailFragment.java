package heureka.cz.internal.library.ui.fragments;

/**
 * Created by Ondrej on 6. 5. 2016.
 */


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import heureka.cz.internal.library.R;
import heureka.cz.internal.library.application.CodeCamp;
import heureka.cz.internal.library.helpers.CollectionUtils;
import heureka.cz.internal.library.helpers.Config;
import heureka.cz.internal.library.helpers.RetrofitBuilder;
import heureka.cz.internal.library.repository.Book;
import heureka.cz.internal.library.repository.Info;
import heureka.cz.internal.library.repository.Settings;
import heureka.cz.internal.library.rest.ApiDescription;
import heureka.cz.internal.library.ui.BookDetailAndResActivity;
import heureka.cz.internal.library.rest.ApiDescription.ResponseHandler;
import heureka.cz.internal.library.ui.MainActivity;
import heureka.cz.internal.library.ui.adapters.AvailableRecyclerAdapter;
import heureka.cz.internal.library.ui.adapters.UsersRecyclerAdapter;
import heureka.cz.internal.library.ui.dialogs.RateDialog;
import retrofit2.Retrofit;

public class BookDetailFragment extends Fragment {

String user = "tomas";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the view from fragment_book_detailk_detail.xml
        View view = inflater.inflate(R.layout.activity_book_detail, container, false);
        return view;
    }

    public static final String KEY_CAN_BORROW = "can_borrow";
    public static final String KEY_CAN_RESERVE = "can_reserve";
    public static final String MY_BOOK = "is_my_book";

    private Book bookDetail;

    /*
     * vypujcka by měla byt mozna jen po nacteni knihy cteckou,
     * aby nedoslo k vypojceni jine knihy */
    private boolean canBorrow = false;

    /** zato rezervace kdykoliv jindy */
    private boolean canReturn = false;
    /**
     * zato rezervace kdykoliv jindy
     * */
    private boolean canReserve = false;

    private String bookCode = "";

    private ApiDescription apiDescription;

    @Inject
    CollectionUtils collectionUtils;

    @Inject
    RetrofitBuilder retrofitBuilder;

    @Inject
    Settings settings;

    @Bind(R.id.coordinator)
    View coordinator;

    @Bind(R.id.ratingBar)
    RatingBar ratingBar;

    @Bind(R.id.detail_name)
    TextView detailName;

    @Bind(R.id.tags)
    TextView detailTags;

    @Bind(R.id.form)
    TextView detailForm;

    @Bind(R.id.lang)
    TextView detailLang;

    @Bind(R.id.detail_link)
    TextView detailLink;

    @Bind(R.id.detail_available)
    RecyclerView detailAvailable;

    @Bind(R.id.detail_users)
    RecyclerView detailUsers;

    @Bind(R.id.btn_borrow)
    Button btnBorrow;

    @Bind(R.id.btn_return)
    Button btnReturn;

    @Bind(R.id.btn_reserve)
    Button btnReserve;

    @OnClick(R.id.btn_borrow)
    void borrowBook() {
        btnBorrow.setEnabled(false);

        if (settings.get() == null) {
            return;
        }

        apiDescription.borrowBook(bookCode, settings.get().getEmail(), new ApiDescription.ResponseHandler() {
            @Override
            public void onResponse(Object data) {
                Snackbar.make(coordinator, ((Info)data).getInfo(), Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure() {
                btnBorrow.setEnabled(true);
            }
        });
    }

    @OnClick(R.id.btn_reserve)
    void reserveBook() {
        btnReserve.setEnabled(false);

        if (settings.get() == null) {
            return;
        }

        apiDescription.reserveBook(bookDetail.getBookId(), settings.get().getEmail(), new ApiDescription.ResponseHandler() {
            @Override
            public void onResponse(Object data) {
                Snackbar.make(coordinator, ((Info) data).getInfo(), Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure() {
                btnBorrow.setEnabled(true);
            }
        });
    }

    @OnClick(R.id.btn_return)
    void returnBook() {
        btnReturn.setEnabled(false);

        Bundle args = new Bundle();
        args.putInt("bookId", (int)bookDetail.getBookId());
        RateDialog rateDialog= RateDialog.newInstance();
        rateDialog.setArguments(args);
        FragmentManager fm = getChildFragmentManager();

        rateDialog.show(fm, "fragment_rate_dialog");
        btnReturn.setEnabled(false);
        getFragmentManager().popBackStackImmediate();
    }

    @OnClick(R.id.detail_link)
    void detailLink() {

        String url = bookDetail.getDetailLink();
        if(url == null) {
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the view from fragment_book_detailk_detail.xml
        View view = inflater.inflate(R.layout.activity_book_detail, container, false);
        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((CodeCamp)getActivity().getApplication()).getApplicationComponent().inject(this);
        ButterKnife.bind(this, view);

        // predani objektu z aktivity
        if (getActivity().getIntent().getExtras() != null) {
            bookDetail = getActivity().getIntent().getExtras().getParcelable(MainActivity.KEY_BOOK_DETAIL);
            canBorrow = getActivity().getIntent().getExtras().getBoolean(KEY_CAN_BORROW);
            canReserve = getActivity().getIntent().getExtras().getBoolean(KEY_CAN_RESERVE);
            canReturn = getActivity().getIntent().getExtras().getBoolean(MY_BOOK);
            //toto ismzbook
            bookCode = getActivity().getIntent().getExtras().getString(BookDetailAndResActivity.KEY_CODE);
        }

        // nacteni stavu po otoceni obrazovky
        if (savedInstanceState != null) {
            bookDetail = savedInstanceState.getParcelable(MainActivity.KEY_BOOK_DETAIL);
            canBorrow = savedInstanceState.getBoolean(KEY_CAN_BORROW);
            canReserve = savedInstanceState.getBoolean(KEY_CAN_RESERVE);
            canReturn = savedInstanceState.getBoolean(MY_BOOK);
            //toto ismzbook

            bookCode = savedInstanceState.getString(BookDetailAndResActivity.KEY_CODE);
        }

        apiDescription = new ApiDescription(retrofitBuilder.provideRetrofit(settings.get() != null ? settings.get().getApiAddress() : Config.API_BASE_URL));

        if(!canBorrow) {
            btnBorrow.getLayoutParams().height = 0;
        }

        if(!canReserve) {
            btnReserve.getLayoutParams().height = 0;
        }

        if(!canReturn){
            btnReturn.getLayoutParams().height = 0;
        }


        initBook();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MainActivity.KEY_BOOK_DETAIL, bookDetail);
        outState.putBoolean(KEY_CAN_BORROW, canBorrow);
        outState.putBoolean(KEY_CAN_RESERVE, canReserve);
        outState.putBoolean(MY_BOOK, canReturn);
    }

    private void initBook() {
        detailName.setText(bookDetail.getName());
        detailTags.setText(bookDetail.getTags().size() > 0 ? collectionUtils.implode(",", bookDetail.getTags()) : "");
        detailLang.setText(bookDetail.getLang());
        detailForm.setText(bookDetail.getForm());
        detailLink.setText(bookDetail.getDetailLink());

        detailAvailable.setLayoutManager(new LinearLayoutManager(getActivity()));
        AvailableRecyclerAdapter adapterAvailable = new AvailableRecyclerAdapter(bookDetail.getAvailable());
        detailAvailable.setAdapter(adapterAvailable);

        detailUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        UsersRecyclerAdapter adapterUsers = new UsersRecyclerAdapter(bookDetail.getHolders());
        detailUsers.setAdapter(adapterUsers);
    }
public Book getBook(){
    return bookDetail;
}



}
