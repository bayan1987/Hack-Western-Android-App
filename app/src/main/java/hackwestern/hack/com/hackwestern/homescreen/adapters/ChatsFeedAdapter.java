package hackwestern.hack.com.hackwestern.homescreen.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.common.collect.Lists;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hackwestern.hack.com.hackwestern.R;
import hackwestern.hack.com.hackwestern.firebase.model.ChatDataModel;
import hackwestern.hack.com.hackwestern.homescreen.model.ChatFeedDataModel;
import hackwestern.hack.com.hackwestern.widgets.AppTextView;

/**
 * Created by Sarthak on 18-11-2017
 */
public class ChatsFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private static final String KEY_CONVERSATION = "conversations";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_POSTED_AT = "postedAt";
    private static final String KEY_TEXT = "text";

    private OnFeedItemClickListener onFeedItemClickListener;

    private List<ChatFeedDataModel> dataModelList;
    private DatabaseReference firebaseDatabaseReference;
    private Query query;

    public ChatsFeedAdapter(Context context, List<ChatFeedDataModel> dataModelList) {
        this.context = context;
        this.dataModelList = dataModelList;
        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chats_feed_card, parent, false);
        return new ChatsFeedViewHolder(view);
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener clickListener) {
        onFeedItemClickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ChatsFeedViewHolder viewHolder = (ChatsFeedViewHolder) holder;

        if (dataModelList.get(position).getName() != null)
            viewHolder.tvUserTitle.setText(dataModelList.get(position).getName());
        query = firebaseDatabaseReference.child(KEY_CONVERSATION).child(dataModelList.get(position).getMessageId()).child(KEY_MESSAGES).orderByChild(KEY_POSTED_AT);
        query.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("Data", dataSnapshot.toString());
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                List<DataSnapshot> dataSnapshots = Lists.newArrayList(iterator);
                List<DataSnapshot> keyValueDataPairs = Lists.newArrayList(dataSnapshots.get(0).getChildren().iterator());
                for (int i = 0; i < keyValueDataPairs.size(); i++) {
                    if (keyValueDataPairs.get(i).getKey().equals(KEY_POSTED_AT))
                        viewHolder.lastUpdate.setText(DateUtils.getRelativeTimeSpanString(Long.parseLong(keyValueDataPairs.get(i).getValue().toString())));
                    else if (keyValueDataPairs.get(i).getKey().equals(KEY_TEXT))
                        viewHolder.tvUserText.setText(keyValueDataPairs.get(i).getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return dataModelList != null ? dataModelList.size() : 0;
    }


    public class ChatsFeedViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.linearLayoutCard)
        LinearLayout layoutCard;
        @Bind(R.id.userTitle)
        AppTextView tvUserTitle;
        @Bind(R.id.userText)
        AppTextView tvUserText;
        @Bind(R.id.userLastUpdate)
        AppTextView lastUpdate;

        public ChatsFeedViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.linearLayoutCard)
        public void onClickCard() {
            ChatFeedDataModel model = dataModelList.get(getAdapterPosition());
            onFeedItemClickListener.onItemClicked(model.getMessageId(), model.getEmail(), model.getName());
        }
    }

    public interface OnFeedItemClickListener {

        void onItemClicked(String conversationId, String email, String name);
    }
}
