Human human;
MindstormsNXT robot;
PFont font, largeFont;
PImage left, right;
int start;
int score;
boolean flag;

void setup() {
  human = new Human();
  robot = new MindstormsNXT();
  robot.connect();
  font = createFont("Meiryo UI", 24, true);
  largeFont = createFont("Meiryo UI", 40, true);
  size(640, 640);
  frameRate(15);
  start = getTime();
  left = loadImage("left.png");
  right = loadImage("right.png");
  flag = random(0, 1) > 0.5;
  if (flag) {
    robot.setPose(Picode.pose("New pose (31)"));
  } else {
    robot.setPose(Picode.pose("New pose (32)"));
  }
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
    if (flag) {
      robot.setPose(Picode.pose("New pose (31)"));
    } else {
      robot.setPose(Picode.pose("New pose (32)"));
    }
  }
}

int getTime() {
  return hour() * 60 * 60 + minute() * 60 + second();
}
