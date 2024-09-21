package cl.brown.amelia.ui.device;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import cl.brown.amelia.R;
import cl.brown.amelia.databinding.FragmentOperateDeviceBinding;
import cl.brown.amelia.model.Device;
import cl.brown.amelia.http.HttpExecutorRunner;
import cl.brown.amelia.http.HttpServices;
import cl.brown.amelia.ui.device.placeholder.OperateDeviceContent.OperateDeviceItem;
import cl.brown.amelia.utils.Constants;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OperateDeviceItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OperateDeviceRecyclerViewAdapter extends RecyclerView.Adapter<OperateDeviceRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = OperateDeviceRecyclerViewAdapter.class.getName();

    private final List<OperateDeviceItem> mValues;
    private ViewGroup viewGroup;
    private OperateDeviceFragment mParent;

    /*private final View.OnClickListener mOnClickListener = view -> {
        Log.d(TAG, "OperateDeviceRecyclerViewAdapter: View.OnClickListener: Click");
        OperateDeviceItem deviceSelected = (OperateDeviceItem) view.getTag();
        //Toast.makeText(parent.getContext(), "Device selected: "+deviceSelected.serial, Toast.LENGTH_SHORT).show();
    };*/

    public OperateDeviceRecyclerViewAdapter(List<OperateDeviceItem> items, OperateDeviceFragment parent) {
        Log.d(TAG, "Constructor:");
        mValues = items;
        mParent = parent;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder:");
        this.viewGroup = parent;
        return new ViewHolder(FragmentOperateDeviceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int pos = position;
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy (E) HH:mm:ss");
        holder.mOperateDeviceItem = mValues.get(position);
        holder.mSerial.setText(mValues.get(position).serial);
        holder.mIp.setText(mValues.get(position).ip);
        holder.mTime.setText(dateTimeFormat.format(localDateTime));
        holder.itemView.setTag(mValues.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialog(viewGroup.getContext(), holder, pos);
            }
        });

        holder.mBtnViewOption.setOnClickListener(v -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(viewGroup.getContext(), holder.mBtnViewOption);
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_device);
            //adding click listener
            popup.setOnMenuItemClickListener(item -> {

                Toast.makeText(viewGroup.getContext(),
                        "Reset Config en Proceso",
                        Toast.LENGTH_SHORT).show();

                if(R.id.unlinkDevice == item.getItemId()){
                    //handle menu1 click
                    HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
                    httpExecutorRunner.execute(new HttpServices(Constants.RESET_HARD_ACTION, holder.mSerial.getText().toString(), holder.mIp.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                        @Override
                        public void onComplete(Boolean result) {
                            Log.i(TAG, "httpExecutorRunner.onComplete: "+result);
                            if(result) {
                                Toast.makeText(viewGroup.getContext(), "Dispositivo Desvinculado", Toast.LENGTH_SHORT).show();
                                /*
                                Device device = new Device(holder.mSerial.getText().toString(), "", "", "", "true");
                                boolean deleteResult = DataBase.deleteDeviceBySerial(device);
                                Log.i(TAG, "httpExecutorRunner.onComplete: deleteDeviceBySerial "+deleteResult);
                                holder.getBindingAdapter().notifyItemRemoved(pos);
                                mParent.refreshListView(pos, getItemCount());
                                */
                            }else{
                                Toast.makeText(viewGroup.getContext(), "Error Error Error", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "httpExecutorRunner.onError: ", e);
                            Toast.makeText(viewGroup.getContext(), "Error Error Error", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return false;
            });
            //displaying the popup
            popup.show();

        });

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void updateList(List<Device> devices){
        mValues.clear();
        devices.forEach(device -> addItem(new OperateDeviceItem(device.SERIAL, device.IP, device.PORT, device.CONFIG)));
    }

    private void addItem(OperateDeviceItem item){
        mValues.add(item);
    }

    private void showBottomDialog(Context context, ViewHolder holder, int position) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_detail);

        FloatingActionButton btnUp = dialog.findViewById(R.id.btnUp);
        FloatingActionButton btnDw = dialog.findViewById(R.id.btnDown);
        FloatingActionButton btnSt = dialog.findViewById(R.id.btnStop);
        FloatingActionButton btnCf = dialog.findViewById(R.id.btnSet);
        FloatingActionButton btnRf = dialog.findViewById(R.id.btnResetConfig);

        /* ********* btnUp ********* */
        btnUp.setOnClickListener(v -> {
            Toast.makeText(viewGroup.getContext(),
                    "Up",
                    Toast.LENGTH_SHORT).show();

            HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
            httpExecutorRunner.execute(new HttpServices(Constants.UP_ACTION, holder.mSerial.getText().toString(), holder.mIp.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    Log.i(TAG, "httpExecutorRunner.onComplete: "+result);
                    if(result) {
                        Toast.makeText(viewGroup.getContext(), "Cortina Arriba", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(viewGroup.getContext(), "Error Error Error", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "httpExecutorRunner.onError: ", e);
                    Toast.makeText(viewGroup.getContext(), "Error ", Toast.LENGTH_LONG).show();
                }
            });
        });

        btnUp.setOnLongClickListener(v -> {
            Toast.makeText(viewGroup.getContext(),
                    "Long Up",
                    Toast.LENGTH_LONG).show();

            HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
            httpExecutorRunner.execute(new HttpServices(Constants.UP_HOLDER_ACTION, holder.mSerial.getText().toString(), holder.mIp.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    Log.i(TAG, "httpExecutorRunner.onComplete: "+result);
                    if(result) {
                        Toast.makeText(viewGroup.getContext(), "Subiendo Cortina", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(viewGroup.getContext(), "Error Error Error", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "httpExecutorRunner.onError: ", e);
                    Toast.makeText(viewGroup.getContext(), "Error Subiendo", Toast.LENGTH_LONG).show();
                }
            });

            return true;
        });

        /* ********* btnDw ********* */
        btnDw.setOnClickListener(v -> {
            Toast.makeText(viewGroup.getContext(),
                    "Down",
                    Toast.LENGTH_SHORT).show();

            HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
            httpExecutorRunner.execute(new HttpServices(Constants.DOWN_ACTION, holder.mSerial.getText().toString(), holder.mIp.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    Log.i(TAG, "httpExecutorRunner.onComplete: "+result);
                    if(result) {
                        Toast.makeText(viewGroup.getContext(), "Cortina Abajo", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(viewGroup.getContext(), "Error", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "httpExecutorRunner.onError: ", e);
                    Toast.makeText(viewGroup.getContext(), "Error Subiendo", Toast.LENGTH_LONG).show();
                }
            });
        });

        btnDw.setOnLongClickListener(v -> {
            Toast.makeText(viewGroup.getContext(),
                    "Long Down",
                    Toast.LENGTH_LONG).show();

            HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
            httpExecutorRunner.execute(new HttpServices(Constants.DOWN_HOLDER_ACTION, holder.mSerial.getText().toString(), holder.mIp.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    Log.i(TAG, "httpExecutorRunner.onComplete: "+result);
                    if(result) {
                        Toast.makeText(viewGroup.getContext(), "Bajando Cortina", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(viewGroup.getContext(), "Error Error Error", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "httpExecutorRunner.onError: ", e);
                    Toast.makeText(viewGroup.getContext(), "Error Bajando", Toast.LENGTH_LONG).show();
                }
            });

            return true;
        });

        /* ********* btnStop ********* */
        btnSt.setOnClickListener(v -> {
            Toast.makeText(viewGroup.getContext(),
                    "Stop",
                    Toast.LENGTH_SHORT).show();

            HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
            httpExecutorRunner.execute(new HttpServices(Constants.STOP_ACTION, holder.mSerial.getText().toString(), holder.mIp.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    Log.i(TAG, "httpExecutorRunner.onComplete: "+result);
                    if(result) {
                        Toast.makeText(viewGroup.getContext(), "Cortina Detenida", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(viewGroup.getContext(), "Error Error Error", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "httpExecutorRunner.onError: ", e);
                    Toast.makeText(viewGroup.getContext(), "Error Stop", Toast.LENGTH_LONG).show();
                }
            });
        });

        /* ********* btnSetConfig ********* */
        btnCf.setOnClickListener(v -> {
            Toast.makeText(viewGroup.getContext(),
                    "Saving Config",
                    Toast.LENGTH_SHORT).show();

            HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
            httpExecutorRunner.execute(new HttpServices(Constants.SET_CONFIG_ACTION, holder.mSerial.getText().toString(), holder.mIp.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    Log.i(TAG, "httpExecutorRunner.onComplete: "+result);
                    if(result) {
                        Toast.makeText(viewGroup.getContext(), "Config Save", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(viewGroup.getContext(), "Error Error Error", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "httpExecutorRunner.onError: ", e);
                    Toast.makeText(viewGroup.getContext(), "Error Saving Config", Toast.LENGTH_LONG).show();
                }
            });
        });

        /* ********* btnSetConfig ********* */
        btnRf.setOnClickListener(v -> {
            Toast.makeText(viewGroup.getContext(),
                    "Resetting Config",
                    Toast.LENGTH_SHORT).show();

            HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
            httpExecutorRunner.execute(new HttpServices(Constants.RESET_CONFIG_ACTION, holder.mSerial.getText().toString(), holder.mIp.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    Log.i(TAG, "httpExecutorRunner.onComplete: "+result);
                    if(result) {
                        Toast.makeText(viewGroup.getContext(), "Config Reset", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(viewGroup.getContext(), "Error Config Reset", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "httpExecutorRunner.onError: ", e);
                    Toast.makeText(viewGroup.getContext(), "Error Error Error", Toast.LENGTH_LONG).show();
                }
            });
        });

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Log.w(TAG, "showBottomDialog.height: "+height);
        dialog.show();
        dialog.getWindow().setLayout(width - (width/5), height/2);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mSerial;
        public final TextView mIp;
        public final TextView mTime;
        public final TextView mBtnViewOption;
        public OperateDeviceItem mOperateDeviceItem;

        public ViewHolder(FragmentOperateDeviceBinding binding) {
            super(binding.getRoot());
            Log.d(TAG, "ViewHolder: Constructor FragmentOperateDeviceBinding");
            mSerial = binding.deviceSerial;
            mIp = binding.deviceIp;
            mTime = binding.txtTime;
            mBtnViewOption = binding.btnViewOption;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mSerial.getText() + "'";
        }
    }
}