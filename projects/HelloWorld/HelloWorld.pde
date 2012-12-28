Human human;
Robot robot;
java.awt.Robot r;
boolean lastRightHandUp = false;
boolean lastBothHandsUp = false;

void setup() {

  // 接続
  // robot =
  // new Robot("btspp://001653047aeb");
  human = new Human();

  // キーを押せるようにする
  try {
    r = new java.awt.Robot();
  } catch (java.awt.AWTException e) {
    e.printStackTrace();
  }
}

void draw() {
  boolean right = false;
  boolean both = false;
  if (human.getPose().eq(Roboko.pose("Rest pose"), 0.04)) {
    fill(200);
  } else
  if (human.getPose().eq(Roboko.pose("Hand up"), 0.04)) {
    fill(200, 40, 40);
    if (!lastRightHandUp) {
      r.keyPress(java.awt.event.KeyEvent.VK_RIGHT);
      r.keyRelease(java.awt.event.KeyEvent.VK_RIGHT);
    }
    right = true;
  } else
  if (human.getPose().eq(Roboko.pose("Both hands up"), 0.04)) {
    fill(30, 200, 30);
    if (!lastBothHandsUp) {
      r.keyPress(java.awt.event.KeyEvent.VK_LEFT);
      r.keyRelease(java.awt.event.KeyEvent.VK_LEFT);
    }
    both = true;
  } else {
    fill(0);
  }
  rect(0, 0, width-1, height-1);
  lastRightHandUp = right;
  lastBothHandsUp = both;
}
