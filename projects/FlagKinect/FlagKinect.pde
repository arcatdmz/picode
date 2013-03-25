Human human;
PFont font, largeFont;
PImage left, right;
int start;
int score;
boolean flag;

void setup() {
  human = new Human();
  font = createFont("Meiryo UI", 24, true);
  largeFont = createFont("Meiryo UI", 40, true);
  size(640, 640);
  frameRate(15);
  start = getTime();
  flag = random(0, 1) > 0.5;
  left = loadImage("left.png");
  right = loadImage("right.png");
}

void draw() {

  // 姿勢取得
  Pose pose = human.getPose();

  // 背景クリア
  if (pose == null) {
    background(200);
  } else {
    background(255);
  }
  fill(0);

  // 指示
  textFont(largeFont);
  textAlign(CENTER);
  if (flag) {
    image(left, 20, 10);
    text("左手あげて！", 320, 610);
  } else {
    image(right, 20, 10);
    text("右手あげて！", 320, 610);
  }

  // 得点
  textFont(font);
  textAlign(LEFT);
  text("得点: " + score, 10, 30);

  // 正解判定
  if (pose == null) {
    return;
  }
  if (flag && pose.eq(Picode.pose("New pose (3)"))
  || !flag && pose.eq(Picode.pose("New pose (4)"))) {
    int end = getTime();
    score = 100 - (end - start);
    if (score < 0) {
      score = 0;
    }
    flag = random(0, 1) > 0.5;
  }
}

int getTime() {
  return hour() * 60 * 60 + minute() * 60 + second();
}
