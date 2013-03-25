Human human;
PFont font, largeFont;
PImage left, right, none, both;
Pose pose;
int start;
int score;
int goal;

// はじめに一度だけ実行される処理
void setup() {
  human = new Human();
  human.showCaptureFrame(true);
  font = createFont("Meiryo UI", 24, true);
  largeFont = createFont("Meiryo UI", 40, true);
  size(640, 640);
  frameRate(15);
  start = -1;
  left = loadImage("left.png");
  right = loadImage("right.png");
  none = loadImage("none.png");
  both = loadImage("both.png");
  goal = (int) random(0, 4);
}

// 1秒に15回実行されつづける処理
void draw() {

  // 姿勢を取得
  pose = human.getPose();

  // 画面に絵や得点を表示
  clearBackground(); // 背景をクリア
  showCommand(); // 指示内容の絵を表示
  showScore(); // 得点を表示

  // 姿勢が取れなかったらここで終わる
  // (正解判定に進まない)
  if (pose == null) {
    return;
  }

  // 正解判定
  boolean ok = false;
  switch (goal) {
    case 0:
      if (pose.eq(Picode.pose("New pose (3)"))) {
        ok = true;
      }
      break;
    case 1:
      if (pose.eq(Picode.pose("New pose (4)"))) {
        ok = true;
      }
      break;
    case 2:
      if (pose.eq(Picode.pose("New pose (2)"))) {
        ok = true;
      }
      break;
    case 3:
      if (pose.eq(Picode.pose("New pose (5)"))) {
        ok = true;
      }
      break;
  }

  // 姿勢が正しくなかったらここで終わる
  if (ok == false) {
    return;
  }

  // 正解!!
  calcScore();
  setNewGoal();
}

// 背景をクリア
void clearBackground() {
  if (pose == null) {
    background(200);
  } else {
    background(255);
  }
}

// 指示内容の絵を表示
void showCommand() {
  textFont(largeFont);
  textAlign(CENTER);
  fill(0);
  switch (goal) {
    case 0:
      image(left, 20, 10);
      text("左手あげて！", 320, 610);
      break;
    case 1:
      image(right, 20, 10);
      text("右手あげて！", 320, 610);
      break;
    case 2:
      image(none, 20, 10);
      text("両手さげて！", 320, 610);
      break;
    case 3:
      image(both, 20, 10);
      text("両手あげて！", 320, 610);
      break;
  }
}

// 得点を表示
void showScore() {
  textFont(font);
  textAlign(LEFT);
  fill(0);
  text("得点: " + score, 10, 30);
}

// 得点を計算
void calcScore() {
  int end = getTime();
  if (start >= 0) {
    int bonus = 100 - (end - start);
    if (bonus >= 0) {
      score += bonus;
    }
  }
  start = end;
}

// 次のゴールを設定
void setNewGoal() {
  int newGoal = (int) random(0, 3);
  if (goal <= newGoal) {
    goal = newGoal + 1;
  } else {
    goal = newGoal;
  }
}

// 今の時刻を取得
int getTime() {
  return hour() * 60 * 60 + minute() * 60 + second();
}

