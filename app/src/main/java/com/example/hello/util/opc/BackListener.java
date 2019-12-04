package com.example.hello.util.opc;


import androidx.fragment.app.FragmentManager;

public class BackListener implements FragmentManager.OnBackStackChangedListener {

    private int prec_size;
    private FragmentManager fm;

    public BackListener(FragmentManager fm) {
        this.fm = fm;
        prec_size = 0;
    }

    @Override
    public void onBackStackChanged() {
        int i = prec_size-fm.getBackStackEntryCount();
        while (i-->0) {
            ManagerOPC.getInstance().pop();
        }
        prec_size = fm.getBackStackEntryCount();
    }
}
