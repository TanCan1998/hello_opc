package com.example.hello.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.hello.R;
import com.example.hello.util.LogUtils;
import com.example.hello.util.SetUtils;
import com.example.hello.util.opc.ManagerOPC;
import com.example.hello.util.opc.SessionElement;
import com.example.hello.util.ToastUtils;
import com.example.hello.ui.activity.MainActivity;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.Objects;

public class SessionFragment extends Fragment implements View.OnTouchListener {

    private ManagerOPC     managerOPC;
    private SessionElement sessionElement;
    private int            session_position;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i("asdf", "hf oncre");
        ((MainActivity) Objects.requireNonNull(getActivity())).bindSessionFragment(this);

    }

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LogUtils.i("asdf", "hf oncre view");
        View root = inflater.inflate(R.layout.fragment_session, container, false);

        session_position = Objects.requireNonNull(getActivity()).getIntent().getIntExtra("sessionPosition", -1);
        String url = getActivity().getIntent().getStringExtra("url");

        if (session_position < 0) {
            ToastUtils.toast(getContext(), getString(R.string.error), TastyToast.ERROR);
            getActivity().finish();
        }

        managerOPC = ManagerOPC.getInstance();

        TextView txtSessionEndpoint = root.findViewById(R.id.txtSessionEndpoint);
        TextView txtSessionId = root.findViewById(R.id.txtSessionId);
        TextView txtUrl = root.findViewById(R.id.txtUrl);

        sessionElement = managerOPC.getSessions().get(session_position);

        txtSessionEndpoint.setText("Endpoint\n" + sessionElement.getSession().getSession().getEndpoint().getEndpointUrl());
        txtSessionId.setText(getString(R.string.session_id) + "\n" + sessionElement.getSession().getSession().getName());
        txtUrl.setText("Url\n" + url);

        Button disconnect = root.findViewById(R.id.disconnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setTitle(R.string.session_close);
                builder.setMessage(R.string.session_close_notice);
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                managerOPC.getSessions().remove(session_position);
                                sessionElement.getSession().closeAsync();
                                dialogInterface.dismiss();
                                Objects.requireNonNull(getActivity()).finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialogInterface.dismiss();
                                break;
                        }
                    }
                };
                builder.setPositiveButton(android.R.string.yes, listener);
                builder.setNegativeButton(android.R.string.no, listener);
                builder.setCancelable(false);
                Dialog g = builder.create();
                g.show();
                SetUtils.setDialog(g, SetUtils.CONFIRM_DIALOG);
            }
        });

        final LottieAnimationView lv = root.findViewById(R.id.session);
        lv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lv.isAnimating()) {
                    lv.pauseAnimation();
                } else {
                    lv.resumeAnimation();
                }
            }
        });
        lv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ToastUtils.toast(getContext(), "Ha ha", TastyToast.SUCCESS);
                return true;
            }
        });
        return root;
    }

    public int getSession_position() {
        return session_position;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

}