package id.sisi.si.mso.ui.inspection.list;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.InspectionList;
import id.sisi.si.mso.ui.inspection.detail.InspectionDetailActivity;
import id.sisi.si.mso.ui.inspection.detail.InspectionDetailPresenter;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by durrrr on 06-Oct-17.
 * Email: andaruwildan@gmail.com
 */
public class InspectionListAdapter extends RealmRecyclerViewAdapter<InspectionList, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    private static final int VIEW_TYPE_ACTIVITY = 2;

    private boolean mLoadingMore = false;

    public InspectionListAdapter(@Nullable OrderedRealmCollection<InspectionList> data, boolean autoUpdate) {
        super(data, autoUpdate);
    }

    public void setLoadingMore(boolean loadingMore) {
        mLoadingMore = loadingMore;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_EMPTY:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_empty, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_loading, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            case VIEW_TYPE_ACTIVITY:
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_inspection, parent, false);
                return new ViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getData().size()) {
            if (mLoadingMore) {
                return VIEW_TYPE_LOADING;
            } else return VIEW_TYPE_EMPTY;
        } else return VIEW_TYPE_ACTIVITY;
    }

    @Override
    public int getItemCount() {
        if (mLoadingMore || super.getItemCount() == 0) {
            return super.getItemCount() + 1;
        } else {
            return super.getItemCount();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (position >= getData().size() || getData().size() <= 0)
            return;

        if (getItemViewType(position) != VIEW_TYPE_ACTIVITY)
            return;

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tvNomenclature.setText(getItem(position).getEquipmentName());
        viewHolder.tvDate.setText(getItem(position).getDate());
        if(getItem(position).getCondition() != null){
            viewHolder.tvStatus.setText(getItem(position).getCondition().equals(InspectionList.CONDITION_GOOD) ? "GOOD" : "BAD");
            viewHolder.tvStatus.setTextColor(getItem(position).getCondition().equals(InspectionList.CONDITION_GOOD) ?
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.material_green_400) : ContextCompat.getColor(holder.itemView.getContext(), R.color.material_red_900));
        }
        if(getItem(position).getSynced() != null & !getItem(position).getSynced()) {
            viewHolder.tvSyncStatus.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), InspectionDetailActivity.class);
                intent.putExtra("inspection_no", getItem(position).getInspectionNo());
                intent.putExtra("equipment_name", getItem(position).getEquipmentName());
                if(getItem(position).getSynced() != null) {
                    intent.putExtra("synced", getItem(position).getSynced() ?
                            InspectionDetailPresenter.ONLINE_MODE : InspectionDetailPresenter.OFFLINE_MODE);
                }
                view.getContext().startActivity(intent);
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_nomenclature)
        TextView tvNomenclature;
        @BindView(R.id.tv_date)
        TextView tvDate;
        @BindView(R.id.tvstatus)
        TextView tvStatus;
        @BindView(R.id.tv_sync_status)
        TextView tvSyncStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
