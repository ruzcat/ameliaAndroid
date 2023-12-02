package cl.brown.amelia.ui.device;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cl.brown.amelia.MainActivity;
import cl.brown.amelia.R;
import cl.brown.amelia.databinding.FragmentOperateDeviceBinding;
import cl.brown.amelia.model.Device;
import cl.brown.amelia.service.HttpExecutorRunner;
import cl.brown.amelia.service.HttpServiceCallable;
import cl.brown.amelia.ui.device.placeholder.OperateDeviceContent.OperateDeviceItem;
import cl.brown.amelia.ui.dialog.MasterDialog;
import cl.brown.amelia.ui.wifi.WifiFragment;
import cl.brown.amelia.util.HttpServiceDeviceUtils;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OperateDeviceItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OperateDeviceRecyclerViewAdapter extends RecyclerView.Adapter<OperateDeviceRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = OperateDeviceRecyclerViewAdapter.class.getName();

    private final List<OperateDeviceItem> mValues;
    private ViewGroup parent;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "OperateDeviceRecyclerViewAdapter: View.OnClickListener: Click");
            OperateDeviceItem deviceSelected = (OperateDeviceItem) view.getTag();
            Toast.makeText(parent.getContext(), "Device selected: "+deviceSelected.serial, Toast.LENGTH_SHORT).show();
        }
    };

    public OperateDeviceRecyclerViewAdapter(List<OperateDeviceItem> items) {
        Log.d(TAG, "Constructor:");
        mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder:");
        this.parent = parent;
        return new ViewHolder(FragmentOperateDeviceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ViewHolder thisHolder = holder;
        holder.mOperateDeviceItem = mValues.get(position);
        holder.mSerial.setText(mValues.get(position).serial);
        holder.mIp.setText(mValues.get(position).ip);
        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);

        holder.mBtnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(parent.getContext(),
                        "Up Up Up",
                        Toast.LENGTH_SHORT).show();

                HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
                httpExecutorRunner.execute(new HttpServiceCallable(HttpServiceDeviceUtils.DOWN_ACTION, thisHolder.mSerial.getText().toString(), thisHolder.mIp.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                    @Override
                    public void onComplete(Boolean result) {
                        Log.i(TAG, "httpExecutorRunner.onComplete: "+result);
                        if(result) {
                            Toast.makeText(parent.getContext(), "Cortina Arriba", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(parent.getContext(), "Error Error Error", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "httpExecutorRunner.onError: ", e);
                        Toast.makeText(parent.getContext(), "Error Error Error", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        holder.mBtnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(parent.getContext(),
                        "Down DOwn Down",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void updateList(List<Device> devices){
        mValues.clear();
        devices.forEach(device -> {
            addItem(new OperateDeviceItem(device.SERIAL, device.IP, device.PORT, device.CONFIG));
        });
    }

    private void addItem(OperateDeviceItem item){
        mValues.add(item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mSerial;
        public final TextView mIp;
        public final ImageView mBtnUp;
        public final ImageView mBtnDown;
        public OperateDeviceItem mOperateDeviceItem;

        public ViewHolder(FragmentOperateDeviceBinding binding) {
            super(binding.getRoot());
            Log.d(TAG, "ViewHolder: Constructor FragmentOperateDeviceBinding");
            mSerial = binding.deviceSerial;
            mIp = binding.deviceIp;
            mBtnUp = binding.btnUp;
            mBtnDown = binding.btnDown;
        }

        public ViewHolder(View view) {
            super(view);
            Log.d(TAG, "ViewHolder: Constructor View");
            mSerial = view.findViewById(R.id.device_serial);
            mIp = view.findViewById(R.id.device_ip);
            mBtnUp = view.findViewById(R.id.btnUp);
            mBtnDown = view.findViewById(R.id.btnDown);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mSerial.getText() + "'";
        }
    }
}