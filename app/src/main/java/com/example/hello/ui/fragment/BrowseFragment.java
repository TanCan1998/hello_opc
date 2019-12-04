package com.example.hello.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.hello.R;
import com.example.hello.util.SetUtils;
import com.example.hello.util.opc.BackListener;
import com.example.hello.util.opc.ManagerOPC;
import com.example.hello.util.opc.thread.ThreadBrowse;
import com.example.hello.util.ToastUtils;
import com.example.hello.ui.activity.MainActivity;
import com.sdsmdg.tastytoast.TastyToast;

import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.ReferenceDescription;

import java.util.ArrayList;
import java.util.Objects;

public class BrowseFragment extends Fragment implements View.OnTouchListener {
    private int                  session_position;
    private ProgressDialog       dialog;
    private FragmentManager      fragmentManager;
    private LinearLayout         topNavLinear = null;// 顶部目录导航
    private HorizontalScrollView topNavScroll = null;// 顶部滚动部件

    @SuppressLint("ResourceType")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_browse, container, false);
        root.setOnTouchListener(this);
        init(root);
        return root;
    }

    void browseToPosition(int position, final String node) {
        ThreadBrowse t = new ThreadBrowse(session_position, position);
        dialog = ProgressDialog.show(getContext(), getString(R.string.connectingAttempt), getString(R.string.browse), true);
        SetUtils.setDialog(dialog, SetUtils.LOADING_DIALOG);
        @SuppressLint("HandlerLeak") Handler handler_browse = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                dialog.dismiss();
                if (msg.what == -1) {
                    ToastUtils.toastLong(getContext(), getString(R.string.browse_failed) + "," + getString(R.string.subscription_cleared) + "\n" + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), TastyToast.ERROR);
                    ManagerOPC.getInstance().reCreateSession();
                } else if (msg.what == -2) {
                    ToastUtils.toastLong(getContext(), getString(R.string.requestTimeout) + "," + getString(R.string.subscription_cleared), TastyToast.ERROR);
                    ManagerOPC.getInstance().reCreateSession();
                } else {
                    BrowseResponse res = (BrowseResponse) msg.obj;
                    ArrayList<String> tmp_name = new ArrayList<>();
                    ArrayList<String> tmp_namespace = new ArrayList<>();
                    ArrayList<String> tmp_nodeindex = new ArrayList<>();
                    ArrayList<String> tmp_class = new ArrayList<>();
                    for (int i = 0; i < res.getResults().length; i++) {
                        if (res.getResults()[i].getReferences() != null) {
                            for (int j = 0; j < res.getResults()[i].getReferences().length; j++) {
                                ReferenceDescription ref = res.getResults()[i].getReferences()[j];
                                tmp_name.add(ref.getDisplayName().getText());
                                tmp_namespace.add(ref.getNodeId().getNamespaceIndex() + "");
                                tmp_nodeindex.add(ref.getNodeId().getValue().toString());
                                tmp_class.add(ref.getNodeClass().toString());
                            }
                        }
                    }
                    if (tmp_name.size() > 0) {
                        Bundle nodi = new Bundle();
                        nodi.putStringArrayList("nodes", tmp_name);
                        nodi.putStringArrayList("namespace", tmp_namespace);
                        nodi.putStringArrayList("nodeindex", tmp_nodeindex);
                        nodi.putStringArrayList("nodeclass", tmp_class);

                        BrowseContainerFragment fragment = new BrowseContainerFragment();
                        fragment.setArguments(nodi);

                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right);
                        fragmentTransaction.replace(R.id.container, fragment);
                        fragmentTransaction.addToBackStack("fragment");
                        fragmentTransaction.commit();
                        addToTopNav(node);
                    } else {
                        ToastUtils.toast(getContext(), getString(R.string.no_other_nodes), TastyToast.WARNING);
                    }
                }
            }
        };
        t.start(handler_browse);
    }

    private void init(View root) {
        bindView(root);

        session_position = ((MainActivity) Objects.requireNonNull(getActivity())).getSession_position();
        if (session_position < 0) {
            ToastUtils.toast(getContext(), getString(R.string.error), TastyToast.ERROR);
            getActivity().finish();
        }

        ManagerOPC.getInstance().initStack();

        ArrayList<String> list_basicnodes_name = new ArrayList<>();
        ArrayList<String> list_basicnodes_namespace = new ArrayList<>();
        ArrayList<String> list_basicnodes_nodeindex = new ArrayList<>();
        ArrayList<String> list_basicnodes_class = new ArrayList<>();

        list_basicnodes_name.add("Root");
//        list_basicnodes_name.add("Objects");
//        list_basicnodes_name.add("Types");
//        list_basicnodes_name.add("Views");

        list_basicnodes_namespace.add(Identifiers.RootFolder.getNamespaceIndex() + "");
//        list_basicnodes_namespace.add(Identifiers.ObjectsFolder.getNamespaceIndex() + "");
//        list_basicnodes_namespace.add(Identifiers.TypesFolder.getNamespaceIndex() + "");
//        list_basicnodes_namespace.add(Identifiers.ViewsFolder.getNamespaceIndex() + "");


        list_basicnodes_nodeindex.add(Identifiers.RootFolder.getValue().toString());
//        list_basicnodes_nodeindex.add(Identifiers.ObjectsFolder.getValue().toString());
//        list_basicnodes_nodeindex.add(Identifiers.TypesFolder.getValue().toString());
//        list_basicnodes_nodeindex.add(Identifiers.ViewsFolder.getValue().toString());


        list_basicnodes_class.add("Object");
//        list_basicnodes_class.add("Object");
//        list_basicnodes_class.add("Object");
//        list_basicnodes_class.add("Object");

        Bundle node = new Bundle();
        node.putStringArrayList("nodes", list_basicnodes_name);
        node.putStringArrayList("namespace", list_basicnodes_namespace);
        node.putStringArrayList("nodeindex", list_basicnodes_nodeindex);
        node.putStringArrayList("nodeclass", list_basicnodes_class);
        node.putInt("sessionPosition", session_position);

        BrowseContainerFragment fragmentbase = new BrowseContainerFragment();
        fragmentbase.setArguments(node);

        fragmentManager = getChildFragmentManager();
        BackListener backListener = new BackListener(fragmentManager);
        fragmentManager.addOnBackStackChangedListener(backListener);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_top,
                R.anim.slide_out_bottom
        );
        fragmentTransaction.add(R.id.container, fragmentbase);
        fragmentTransaction.commit();
        addToTopNav("\uD83D\uDDA5");
    }

    private void bindView(View root) {
        topNavLinear = root.findViewById(R.id.topNavLinear);
        topNavScroll = root.findViewById(R.id.topNavScroll);
    }

    private void autoMoveToBottom() {
        topNavScroll.post(new Runnable() {
            @Override
            public void run() {
                topNavScroll.fullScroll(ScrollView.FOCUS_RIGHT);
            }
        });
    }

    private void addToTopNav(String position) {
        @SuppressLint("InflateParams") View topNavItem = LayoutInflater.from(getContext()).inflate(R.layout.item_top_nav, null);
        final TextView tv = topNavItem.findViewById(R.id.node);
        tv.setText(position);
        topNavItem.setTag(position);
        topNavLinear.addView(topNavItem);
        autoMoveToBottom();
        topNavItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while (!v.getTag().equals(topNavLinear.getChildAt(topNavLinear.getChildCount() - 1).getTag())) {
                    getChildFragmentManager().popBackStack();
                    removeNavItem();
                }
            }
        });
    }


    private void removeNavItem() {
        try {
            topNavLinear.removeViewAt(topNavLinear.getChildCount() - 1);
        } catch (Exception ignore) {
        }
    }

    public boolean onBackPress() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            removeNavItem();
            return true;
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

}