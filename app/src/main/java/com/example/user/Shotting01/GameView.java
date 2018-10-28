package com.example.user.Shotting01;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import android.view.SurfaceHolder;
import android.view.SurfaceView;


import android.view.SurfaceHolder.Callback;

import com.example.user.shootinggame2.R;

/**
 * Created by user on 2018-02-20.
 */



import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.view.SurfaceHolder.Callback;

public class GameView extends SurfaceView implements Callback {
    private Context          mContext;
    private SurfaceHolder mHolder;
    public  GameThread   mThread;

    private int width, height, cx, cy;               // 화면의 폭과 중심점
    private int x1, y1, x2, y2;                         // Viewport 시작점
    private int sx1, sy1, sx2, sy2;                  // Viewport가 스크롤되는 속도
    private Rect src, dst;                              // Viewport와 View 설정용
    private Rect src2 = new Rect();                // 전경화면용
    private Bitmap imgBack, imgBack2;           // 배경화면

    // private Bitmap[] spaceShip = new Bitmap[2];  // 우주선
    private int shipNum;
    private int w, h;                                  // 우주선의 폭과 높이
    private long counter = 0;                     // 전체 반복 횟수
    private boolean canRun = true;            // 스레드 실행용 플래그
    private boolean wait = false;                // 스레드 실행용 플래그

    //---------- 새로 추가한 부분 ---------------------
    private Bitmap[][] spaceship = new Bitmap[8][2];     // 우주선 8방향
    private Bitmap[]   arrow = new Bitmap[8];                  // 화살표 8방향
    private Rect[]     arrowRect = new Rect[8];                // 화살표 영역 8방향
    private Integer[]  arrowX = new Integer[8];                // 화살표 중심 좌표 8개
    private Integer[]  arrowY = new Integer[8];                // 화살표 중심 좌표 8개
    private int aw, ah;                                                    // 화살표의 폭과 높이
    private static int RAD = 200;                                       // 중심에서 화살표까지의 거리(반지름
    private int dir = 1;                                                      // 시작시  스크롤하는 방향
    // 스크롤 방향에 따른 벡터
    private static int Vec[][] = {{1, 0}, {1, -1}, {0, -1}, {-1, -1},
            {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};

    //-------------------------------------
    //      생성자
    //-------------------------------------
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        mHolder = holder;                             // 생성한 holder를 전역변수에 저장
        mContext = context;                        // 인수로 넘어 온 context를 전역변수에 저장
        mThread = new GameThread();         // GameThread 생성
    }

    //-------------------------------------
    //   SurfaceView가 만들어질 때 호출됨
    //-------------------------------------
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread.start();
    }

    //-------------------------------------
    //    SurfaceView의 크기가 바뀔 때 호출됨
    //-------------------------------------
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    //-------------------------------------
    //   SurfaceView가 종료될 때 호출됨
    //-------------------------------------
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean done = true;
        while (done) {
            try {
                mThread.join();                            // 스레드가 현재 step을 끝낼 때 까지 대기
                done = false;
            } catch (InterruptedException e) {      // 인터럽트가 발생하면?
                // 그 신호 무시 - 아무것도 않음
            }
        } // while
    }

//-------------------- 여기서 부터는 스레드 영역 ----------------------------

    class GameThread extends Thread {

        //-------------------------------------
        //    Thread Constructor
        //-------------------------------------
        public GameThread() {

            // 화면 해상도 구하기

            /*  deprecated된 코드

            Display display = ((WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE))
                    .getDefaultDisplay();
            width  = display.getWidth();         // 화면의 폭
            height = display.getHeight();        // 화면의 높이
            */

            DisplayMetrics disp = mContext.getResources().getDisplayMetrics();
            width = disp.widthPixels;       //화면의 가로폭
            height = disp.heightPixels;     //화면의 세로폭

            cx = width / 2;                                   // 화면의 중심
            cy = height / 2;

            Resources res = mContext.getResources();          // 리소스 읽기

            // 원경 이미지를 화면의 크기의 2배로 설정
            imgBack = BitmapFactory.decodeResource(res, R.drawable.galaxy);
            imgBack = Bitmap.createScaledBitmap(imgBack, width * 2, height * 2, true);

            // 전경
            imgBack2 = BitmapFactory.decodeResource(res, R.drawable.galaxy1);
            imgBack2 = Bitmap.createScaledBitmap(imgBack2, width * 2, height * 2, true);

            // 우주선 읽고 폭과 높이 계산
            spaceship[0][0] = BitmapFactory.decodeResource(res, R.drawable.s00);
            spaceship[0][1] = BitmapFactory.decodeResource(res, R.drawable.s01);
            w = spaceship[0][0].getWidth() / 2;
            h = spaceship[0][0].getHeight() / 2;

            // 화살표 읽고 폭과 높이 계산
            arrow[0] = BitmapFactory.decodeResource(res, R.drawable.a0);
            aw = arrow[0].getWidth() / 2;
            ah = arrow[0].getHeight() / 2;

            // 45도 간격으로 8방향 회전한 이미지 만들기
            Matrix matrix = new Matrix();
            for (int i = 1; i < 8; i++) {
                matrix.postRotate(-45);
                spaceship[i][0] = Bitmap.createBitmap(spaceship[0][0], 0, 0, w * 2, h * 2, matrix, true);
                spaceship[i][1] = Bitmap.createBitmap(spaceship[0][1], 0, 0, w * 2, h * 2, matrix, true);
                arrow[i] = Bitmap.createBitmap(arrow[0], 0, 0, aw * 2, ah * 2, matrix, true);
            }

            dst = new Rect(0, 0, width, height);        // View의 크기
            src = new Rect();                                  // Viewport용

            x1 = imgBack.getWidth() - width;            // Viewport의 시작 위치
            y1 = imgBack.getHeight() - height;
            sx1 = 2;                                                // Viewport를 1회에 이동시킬 거리
            sy1 = 2;
            CalcArrowPos();  // 화살표의 좌표 계산
        }

        // ---------------------------------
        //     화살표 8방향 좌표 구하기
        // ---------------------------------
        public void CalcArrowPos() {
            for (int i = 0; i <= 7; i++) {
                int x = (int) (cx + Math.cos(i * 45 * Math.PI / 180) * RAD);
                int y = (int) (cy - Math.sin(i * 45 * Math.PI / 180) * RAD);
                // 화살표의 중심 좌표
                arrowX[i] = x;
                arrowY[i] = y;

                // 화살표 영역을 Rect()에 저장
                arrowRect[i] = new Rect(x - aw, y - ah, x + aw, y + ah);
            }
        } // CalcArrowPos

        //-------------------------------------
        //    Thread run
        //-------------------------------------
        public void run() {
            Canvas canvas = null;                                         // canvas를 만든다
            while (canRun == true) {
                canvas = mHolder.lockCanvas();                    // canvas를 잠그고 버퍼 할당
                try {
                    synchronized (mHolder) {                        // 동기화 유지
                        ScrollViewport();                                 // 배경 화면 스크롤
                        canvas.drawBitmap(imgBack, src, dst, null);       // 배경 그리기
                        canvas.drawBitmap(imgBack2, src2, dst, null);     // 배경 그리기
                        canvas.drawBitmap(spaceship[dir][shipNum], cx - w, cy - h, null);   // 우주선
                        for (int i = 0; i <= 7; i++) {
                            canvas.drawBitmap(arrow[i], arrowX[i] - aw, arrowY[i] - ah, null);
                        }
                    }
                } finally {
                    if (canvas != null)
                        mHolder.unlockCanvasAndPost(canvas);             // canvas의 내용을 View에 전송
                }
                synchronized (this) {
                    if (wait) {
                        try {
                            wait();
                        } catch (Exception e) {
                            // nothing
                        }
                    }
                }
            } // while
        } // run

        //-------------------------------------
        //   ScrollViewport
        //-------------------------------------
        private void ScrollViewport() {
            counter++;
            shipNum = (int) (counter % 10 / 5);    // 0 or 1

            // 전경 스크롤
            x2 += sx1 * Vec[dir][0];
            y2 += sy1 * Vec[dir][1];
            if (x2 < 0) x2 = width;                               // Viewport가 왼쪽을 벗어남
            else if (x2 > width) x2 = 0;                        // Viewport가 오른쪽을 벗어남
            if (y2 < 0) y2 = height;                              // Viewport가 위를 벗어남
            else if (y2 > height) y2 = 0;                       // Viewport가 아래를 벗어남
            src2.set(x2, y2, x2 + width, y2 + height);   // Viewport 설정

            // 배경 스크롤
            if (counter % 2 == 0) {
                x1 += sx1 * Vec[dir][0];
                y1 += sy1 * Vec[dir][1];
                if (x1 < 0) x1 = width;
                else if (x1 > width) x1 = 0;
                if (y1 < 0) y1 = height;
                else if (y1 > height) y1 = 0;
                src.set(x1, y1, x1 + width, y1 + height);        // Viewport 설정
            }
        } // ScrollViewport

        //------------------------------
        // pause Thread
        //------------------------------
        public void pauseThread() {
            wait = true;
            synchronized (this) {
                this.notify();
            }
        }

        //------------------------------
        // resume Thread
        //------------------------------
        public void resumeThread() {
            wait = false;
            synchronized (this) {
                this.notify();
            }
        }

        //------------------------------
        // stop Thread
        //------------------------------
        public void stopThread() {
            canRun = false;
            synchronized (this) {
                this.notify();
            }
        }

    } // End of Thread

    // ---------------------------------
    //        onTouchEvent
    // ---------------------------------
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Rect temp = new Rect();
            synchronized (mHolder) {
                int x = (int) event.getX();            // 클릭한 위치를 Rect()로 만듦
                int y = (int) event.getY();

                // 8개의 화살표 영역에 대해 겹침 상태 조사
                for (int i = 0; i <= 7; i++) {
                    if (arrowRect[i].contains(x, y) == true) {
                        dir = i;
                        break;
                    }
                } // for
            } // synchronized
        } // if
        return true;
    } // touch

} // End of SurfaceViewFdi