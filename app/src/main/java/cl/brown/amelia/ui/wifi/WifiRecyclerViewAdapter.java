package cl.brown.amelia.ui.wifi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import cl.brown.amelia.R;
import cl.brown.amelia.ui.wifi.placeholder.WifiContent.WifiItem;
import cl.brown.amelia.databinding.FragmentWifiBinding;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

/**
 * {@link RecyclerView.Adapter} that can display a {@link WifiItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class WifiRecyclerViewAdapter extends RecyclerView.Adapter<WifiRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = WifiRecyclerViewAdapter.class.getName();
    private List<WifiItem> mValues;
    private ViewGroup parent;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Log.d(TAG, "WifiRecyclerViewAdapter: View.OnClickListener: Click");
            WifiItem wifi = (WifiItem) view.getTag();
            Log.d(TAG, "WifiRecyclerViewAdapter: "+wifi);
            if(wifi != null) {
                View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_wifi_pass, null);
                TextInputEditText editText = view1.findViewById(R.id.wifiPass);
                AlertDialog alertDialog = new MaterialAlertDialogBuilder(parent.getContext())
                        .setTitle("Connect to " + wifi.ssid)
                        .setView(view1)
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            Log.d(TAG, "pass " + (MessageFormat.format("Typed text is: {0}", Objects.requireNonNull(editText.getText()))));
                            dialogInterface.dismiss();
                            WifiFragment.getInstance().connectToAp(wifi.ssid, editText.getText().toString(), wifi.securityType );
                        }).setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss()).create();
                alertDialog.show();
            }
        }
    };
    public WifiRecyclerViewAdapter(List<WifiItem> items) {
        Log.d(TAG, "Constructor:");
        mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder:");
        this.parent = parent;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_wifi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder:");
        holder.mWifiItem = mValues.get(position);
        holder.mSSID.setText(mValues.get(position).ssid);
        holder.mWaveLabel.setText( String.valueOf(mValues.get(position).waveLevel));
        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount:");
        return mValues.size();
    }

    public void updateList(List<WifiItem> items){
        Log.d(TAG, "updateList:");
        mValues = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mSSID;
        public final TextView mWaveLabel;
        public WifiItem mWifiItem;

        public ViewHolder(FragmentWifiBinding binding) {
            super(binding.getRoot());
            Log.d(TAG, "getItemCount:");
            mSSID = binding.ssid;
            mWaveLabel = binding.securityType;
        }

        public ViewHolder(View view) {
            super(view);
            Log.d(TAG, "getItemCount:");
            mSSID = view.findViewById(R.id.ssid);
            mWaveLabel = view.findViewById(R.id.security_type);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mWaveLabel.getText() + "'";
        }
    }
}