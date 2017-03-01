package com.dfm.honglv.satecobanche.main;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dfm.honglv.satecobanche.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

/**
 * Created by honglv on 01/03/2017.
 */

public class MenusFragment extends Fragment {

    private FloatingActionMenu menuAdd;

    private FloatingActionButton fabAddContruction;
    private FloatingActionButton fabAddBanche;

    private Handler mUiHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.app_bar_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //menuAdd = (FloatingActionMenu) view.findViewById(R.id.menu_add);

        //fabAddContruction = (FloatingActionButton) view.findViewById(R.id.fabAddContruction);
        //fabAddBanche = (FloatingActionButton) view.findViewById(R.id.fabAddBanche);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fabAddContruction.setOnClickListener(clickListener);
        fabAddBanche.setOnClickListener(clickListener);

        int delay = 10;

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                menuAdd.showMenuButton(true);
            }
        }, delay);

        menuAdd.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuAdd.toggle(true);
            }
        });

        menuAdd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (menuAdd.isOpened()) {
                    menuAdd.close(false);
                }
                return true;
            }
        });
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        switch (v.getId()) {
            //case R.id.fabAddContruction:
                //break;
            //case R.id.fabAddBanche:
                //break;
        }

        menuAdd.close(false);
        }
    };
}
