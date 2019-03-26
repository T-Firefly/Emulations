package com.firefly.emulationstation.gamelist;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.bean.GameSystem;

public class SystemInfoDialog extends DialogFragment {
    private final static String ARG_SYSTEM = "system";

    private GameSystem mGameSystem;

    public static SystemInfoDialog newInstance(@NonNull GameSystem system) {
        SystemInfoDialog systemInfoDialog = new SystemInfoDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_SYSTEM, system);
        systemInfoDialog.setArguments(bundle);

        return systemInfoDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args == null) {
            throw new IllegalArgumentException("GameSystem can't be null.");
        }

        mGameSystem = (GameSystem) args.getSerializable(ARG_SYSTEM);

        if (mGameSystem == null) {
            throw new IllegalArgumentException("GameSystem can't be null.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_system_info, container, false);

        TextView nameView = view.findViewById(R.id.name);
        TextView platformView = view.findViewById(R.id.platform_name);
        TextView extensionView = view.findViewById(R.id.support_ext);
        TextView romPathView = view.findViewById(R.id.rom_path);
        TextView coreTextView = view.findViewById(R.id.core);

        nameView.setText(mGameSystem.getName());
        platformView.setText(mGameSystem.getPlatform());
        extensionView.setText(mGameSystem.getExtension());
        romPathView.setText(mGameSystem.getRomPath());

        if ("retroarch".equals(mGameSystem.getEmulator().getName())) {
            coreTextView.setText(mGameSystem.getEmulator().getCorePath());
            view.findViewById(R.id.core_info).setVisibility(View.VISIBLE);
        }

        if (!mGameSystem.getRomPath().startsWith("/")) {
            view.findViewById(R.id.rom_path_tips).setVisibility(View.VISIBLE);
        }

        return view;
    }
}
