package heureka.cz.internal.library.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import heureka.cz.internal.library.R;
import heureka.cz.internal.library.helpers.CollectionUtils;
import heureka.cz.internal.library.repository.Book;

/**
 * Created by tomas on 6.4.16.
 */
public class BookRecyclerAdapter extends RecyclerView.Adapter<BookRecyclerAdapter.ViewHolder> {

    private ArrayList<Book> books;
    private OnTaskItemClickListener listener;

    private CollectionUtils collectionUtils;

    public BookRecyclerAdapter(@NonNull ArrayList<Book> books, CollectionUtils collectionUtils) {
        this.books = books;
        this.collectionUtils = collectionUtils;
    }

    public void setData(@NonNull ArrayList<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    public void setListener(OnTaskItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Book book = books.get(position);
        holder.name.setText(book.getName());
        holder.lang.setText(book.getLang());
        holder.form.setText(book.getForm());
        holder.tags.setText(collectionUtils.implode(",", book.getTags()));
    }

    public ArrayList<Book> getBooks() {
        return books;
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @Bind(R.id.name)
        public TextView name;

        @Bind(R.id.tags)
        public TextView tags;

        @Bind(R.id.lang)
        public TextView lang;

        @Bind(R.id.form)
        public TextView form;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                listener.onItemLongClick(getAdapterPosition());
            }
            return false;
        }
    }

    public interface OnTaskItemClickListener {
        void onItemClick(int taskPosition);
        void onItemLongClick(int taskPosition);
    }
}