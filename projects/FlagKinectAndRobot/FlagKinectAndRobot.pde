// 変数の宣言
Human human;
MindstormsNXT nxt;
PFont font, smallFont, largeFont;
PImage left, right, none, both;
Pose pose;
int start, score, lastScore, count, goal, showCount, time;

// --------------------------------------------------------------------------------
// はじめに一度だけ実行される処理
void setup() {

  // Mindstorms NXTに接続する
  nxt = new MindstormsNXT();
  nxt.connect();

  // Kinectに接続する
  human = new Human();
  human.showCaptureFrame(true);

  // フォントを読み込む
  font = createFont("Meiryo UI Bold", 64, true);
  smallFont = createFont("Meiryo UI Bold", 32, true);
  largeFont = createFont("Meiryo UI Bold", 96, true);

  // 設定など
  frameRate(15);  // draw()を実行する頻度
  size(640, 720); // 画面サイズ
  start = -1;     // スコアを計算するための現在時刻(ゲーム開始前は仮に -1 にしておく)
  score = 0;      // 合計スコア(最初はもちろん 0)
  lastScore = 0;  // 最後に正解したとき加算されたスコア
  count = 0;      // 正解した回数
  goal = 2;       // 最初の正解ポーズを決める(2: 両手をさげて！)
  showCount = 0;  // スコアを表示する残り時間(最初は表示しないので 0 にしておく)

  // NXTに最初のポーズを取らせる
  setPose(goal);
}

// --------------------------------------------------------------------------------
// 1秒に15回実行されつづける処理
void draw() {

  // 人のポーズを取得する
  pose = human.getPose();

  // 画面にキャラクターやスコアを表示する
  showInfo();

  // NXTが動いているか、ポーズが取れなかったらここで終わる
  // (ポーズの判定に進まない)
  if (nxt.isActing()) {
    return;
  }
  if (pose == null) {
    return;
  }

  // 時間を測りはじめる
  if (start < 0) {
    start = getTime();
  }

  // ポーズが合っているか判定する
  boolean ok = false;
  switch (goal) {

    // 左手あげて！
    case 0:
      if (pose.eq(Picode.pose("fuga"))) {
        ok = true;
      }
      break;

    // 右手あげて！
    case 1:
      if (pose.eq(Picode.pose("fugafuga"))) {
        ok = true;
      }
      break;

    // 両手さげて！
    case 2:
      if (pose.eq(Picode.pose("hogehoge"))) {	
        ok = true;
      }
      break;

    // 両手あげて！
    case 3:
      if (pose.eq(Picode.pose("hoge"))) {
        ok = true;
      }
      break;
  }

  // ポーズが正しくなかったらここで終わる
  if (ok == false) {
    return;
  }

  // 正解!!
  calcScore();  // スコアを計算する
  setNewGoal(); // 次の正解ポーズを決める
  if (lastScore > 0) {
    showCount = 45; // スコアを表示する残り時間をセットする(45 ÷ 15 = 3秒間)
  }
}

// --------------------------------------------------------------------------------
// ロボットのポーズを制御する処理
void setPose(int pose) {
  switch (goal) {

    // 左手あげて！
    case 0:
      nxt.setPose(Picode.pose("left"));
      break;

    // 右手あげて！
    case 1:
      nxt.setPose(Picode.pose("right"));
      break;

    // 両手さげて！
    case 2:
      nxt.setPose(Picode.pose("none"));
      break;

    // 両手あげて！
    case 3:
      nxt.setPose(Picode.pose("both"));
      break;
  }
}

// --------------------------------------------------------------------------------
// draw() から呼ばれるサブルーチン

// 画面にキャラクターやスコアを表示する
void showInfo() {
  clearBackground();  // 背景を塗りつぶす
  showTotalScore();   // スコアの合計を表示する
  if (showCount >= 0) {
    showScore();      // 最後に正解したときのスコアを表示する
    showCount = showCount - 1; // スコアを表示する残り時間を減らす
  }
}

// 背景を塗りつぶす
void clearBackground() {
  if (pose == null) {
    background(200); // ポーズが取れなかったら灰色
  } else {
    background(160, 216, 239); // 取れたら薄青
  }
}

// 文字を表示する
void showText(String text, int x, int y) {
  showText(text, x, y, color(255, 255, 255));
}
void showText(String text, int x, int y, color col) {
  fill(0);
  text(text, x + 1, y + 1);
  text(text, x + 2, y + 1);
  text(text, x + 1, y + 2);
  fill(col);
  text(text, x, y);
}

// スコアの合計を表示する
void showTotalScore() {
  textFont(smallFont);
  textAlign(LEFT);
  fill(0);
  text("合計スコア: " + score, 10, 40);
  if (count > 0) {
    int average = time / count;
    text("平均反応時間: " + (average / 10) + "." + (average % 10) + "秒", 10, 80);
  }
}

// 最後に正解したときのスコアを表示する
void showScore() {
  textFont(largeFont);
  textAlign(CENTER);
  showText("正解！" + lastScore + "点!!", 300, 400, color(255, 60, 60));
}

// スコアを計算する
void calcScore() {
  int end = getTime();
  if (start >= 0) {
    int lap = end - start;
    time += lap;
    lastScore = 100 - lap;
    if (lastScore < 0) {
      lastScore = 0;
    }
    score = score + lastScore;
    count = count + 1;
  }
  start = -1;
}

// 次の正解ポーズを決める
void setNewGoal() {
  int newGoal = (int) random(0, 3);
  if (goal <= newGoal) {
    goal = newGoal + 1;
  } else {
    goal = newGoal;
  }
  setPose(goal);
}

// 今の時刻を取得する
int getTime() {
  return ((hour() * 60 + minute()) * 60 + second()) * 10 + millis() / 100;
}


