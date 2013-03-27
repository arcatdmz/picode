Human human;
MindstormsNXT robot;
PFont font, largeFont;
PImage left;

void setup() {

  // いろいろ初期化
  human = new Human();
  robot = new MindstormsNXT();
  robot.connect();
  font = createFont("Meiryo UI", 24, true);
  largeFont = createFont("Meiryo UI", 40, true);
  left = loadImage("left.png");
  size(640, 640);
  frameRate(15);

  // はじめの姿勢
  robot.setPose(Picode.pose("New pose (30)"));
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
  image(left, 20, 10);
  text("左手あげて！", 320, 610);

  // 姿勢がとれなかったら何もしない
  if (pose == null) {
    return;
  }

  // 左手をあげたら
  if (pose.eq(Picode.pose("New pose (23)"))) {
    if (!robot.isActing()) {
      // シュート！
      robot.setPose(Picode.pose("New pose (21)"));
    }
  } else {
    if (!robot.isActing()) {
      // それ以外のときは、はじめの姿勢に戻る
      robot.setPose(Picode.pose("New pose (20)"));
    }
  }
}

