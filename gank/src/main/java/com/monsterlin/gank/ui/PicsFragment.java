package com.monsterlin.gank.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.container.DefaultHeader;
import com.liaoinstan.springview.widget.SpringView;
import com.monsterlin.gank.R;
import com.monsterlin.gank.adapter.PicsAdapter;
import com.monsterlin.gank.bean.Data;
import com.monsterlin.gank.bean.Results;
import com.monsterlin.gank.biz.OnItemClickListener;
import com.monsterlin.gank.constant.GankRefush;
import com.monsterlin.gank.http.HttpMethods;
import com.monsterlin.gank.utils.SnackbarUtil;
import com.monsterlin.gank.widget.LoadingDialog;

import java.util.ArrayList;

import rx.Subscriber;

/**
 * Created by monsterLin on 2016/7/17.
 */
public class PicsFragment extends Fragment{
    private SpringView springView;
    private RecyclerView picRecycler;
    private PicsAdapter picsAdapter;
    private ArrayList<Results> picList = new ArrayList<>();
    private Subscriber subscriber;
    private Context mContext;
    private LoadingDialog dialog;
    private View view;
    private FloatingActionButton fab;
    //  private boolean isGrid;

    private static final int INITSUCCESS = 0x001;

    private int currentPage = 0;
    private int pagenumber = 15;

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INITSUCCESS:

                    ArrayList<Results> resultsArrayList = ((Data) msg.obj).getResults();
                    picList.addAll(resultsArrayList);
                    picsAdapter = new PicsAdapter(picList, mContext);
                    picRecycler.setAdapter(picsAdapter);
                    //  picRecycler.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
                    picRecycler.setLayoutManager(new GridLayoutManager(mContext, 2));
                    // isGrid=true;

                    dialog.dismissDialog();
                    picsAdapter.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void OnItemClick(int position, View view) {


                        }
                    });
                    break;
                default:
                    break;


            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pics, container, false);
        initView(view);
        initData();
        initEvent();
        return view;
    }

    private void initEvent() {
        springView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                SnackbarUtil.ShortSnackbar(view, GankRefush.lowRefushEnd, SnackbarUtil.Info).show();
                springView.onFinishFreshAndLoad();
            }

            @Override
            public void onLoadmore() {
                subscriber = new Subscriber<Data>(){

                    @Override
                    public void onCompleted() {
                        Log.e("onCompleted", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("onError", "onError：" + e.getMessage());
                    }

                    @Override
                    public void onNext(Data pic) {
                        if (pic!=null){
                            picList.addAll(pic.getResults());
                            picsAdapter.notifyDataSetChanged();
                        }
                    }
                };
                HttpMethods.getInstance().getDatas(subscriber, "福利", pagenumber, currentPage);
                currentPage++;
                springView.onFinishFreshAndLoad();
            }

        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picRecycler.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
                Snackbar.make(view, "如果图片展示不完整，请狂点我~", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
//                if (isGrid){
//                    picRecycler.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
//                    isGrid=false;
//                }else {
//                    picRecycler.setLayoutManager(new GridLayoutManager(mContext, 2));
//                    isGrid=true;
//                }
            }
        });


    }


    private void initData() {
        dialog.showDialog();
        subscriber = new Subscriber<Data>() {

            @Override
            public void onCompleted() {
                Log.e("onCompleted", "onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.e("onError", "onError：" + e.getMessage());
            }

            @Override
            public void onNext(Data pic) {
                mHandle.obtainMessage(INITSUCCESS, pic).sendToTarget();
            }
        };

        HttpMethods.getInstance().getDatas(subscriber, "福利", pagenumber, currentPage);
        currentPage++;
    }

    private void initView(View view) {
        springView = (SpringView) view.findViewById(R.id.springView);
        springView.setType(SpringView.Type.FOLLOW);
        springView.setHeader(new DefaultHeader(mContext));
        springView.setFooter(new DefaultFooter(mContext));
        picRecycler = (RecyclerView) view.findViewById(R.id.picRecycler);
        dialog = new LoadingDialog(mContext);
        fab= (FloatingActionButton) view.findViewById(R.id.fab);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }
}
