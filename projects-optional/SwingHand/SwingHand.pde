MindstormsNXT nxt;
boolean flag = false;

void setup() {
  nxt = new MindstormsNXT();
  nxt.connect();
}

void draw() {

  // もしNXTが動いている最中だったら何もしない
  if (nxt.isActing()) {
    return;
  }

  if (flag == true) {
    // もしflagがtrueだったら
    nxt.setPose(Picode.pose("left"));
    flag = false;
  }
  else {
    // それ以外(つまりflagがfalse)だったら
    nxt.setPose(Picode.pose("right"));
    flag = true;
  }
}

