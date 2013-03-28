MindstormsNXT nxt;

void setup() {
  nxt = new MindstormsNXT();
  nxt.connect();
}

boolean flag = false;
void draw() {
  if (nxt.isActing()) {
    return;
  }
  if (flag) {
    nxt.setPose(Picode.pose("New pose (22)"));
  } else {
    nxt.setPose(Picode.pose("New pose (25)"));
  }
  println("switch " + flag);
  flag = !flag;
}

