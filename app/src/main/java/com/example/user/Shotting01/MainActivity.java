package com.example.user.Shotting01;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.example.user.shootinggame2.R;

public class MainActivity extends AppCompatActivity {
    GameView    mGameView;
    MediaPlayer  player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 전체 화면
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // 배경 음악
        player = MediaPlayer.create(this, R.raw.greensleeves);
        player.setVolume(0.8f, 0.8f);
        player.setLooping(true);
        player.start();

        mGameView = (GameView) findViewById(R.id.gameview);
    }


    // 옵션아이템이 선택되었을때의 행동을 정의하는 콜백함수입니다.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:    // 종료
                mGameView.mThread.stopThread();
                finish();
                break;
            case 2:    // 일시 정지
                mGameView.mThread.pauseThread();
                break;
            case 3:    // 계속 진행
                mGameView.mThread.resumeThread();
                break;
            case 4:    // 음악 연주
                player.start();
                break;
            case 5:    // 음악 정지
                player.pause();
        }
        return true;
    }


    // 옵션 아이템의 메뉴들을 초기화하는 콜백함수입니다.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Quit");              // 종료
        menu.add(0, 2, 0, "Pause");          // 일시정지
        menu.add(0, 3, 0, "Resume");       // 계속 진행
        menu.add(0, 4, 0, "Music on");     // 음악 연주
        menu.add(0, 5, 0, "Music off");     // 음악 정지
        return true;
    }
}
