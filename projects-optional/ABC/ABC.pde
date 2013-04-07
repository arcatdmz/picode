MindstormsNXT nxt;

void setup() {
  nxt = new MindstormsNXT();
  nxt.connect();
}

int[] tones = new int[]{0,1,2,3,2,1,0,2,3,4,5,4,3,2,0,0,0,0,0,1,2,3,2,1,0};
int step = 0;
boolean open = true;
void draw() {
  if (nxt.isActing()) {
    return;
  }
  switch (tones[step]) {
    case 0: // ド
      if (open) {
        nxt.setPose(Picode.pose("New pose (8)"));
      } else {
        nxt.setPose(Picode.pose("New pose (7)"));
      }
      break;
    case 1: // レ
      if (open) {
        nxt.setPose(Picode.pose("New pose (10)"));
      } else {
        nxt.setPose(Picode.pose("New pose (9)"));
      }
      break;
    case 2: // ミ
      if (open) {
        nxt.setPose(Picode.pose("New pose (14)"));
      } else {
        nxt.setPose(Picode.pose("New pose (13)"));
      }
      break;
    case 3: // ファ
      if (open) {
        nxt.setPose(Picode.pose("New pose (16)"));
      } else {
        nxt.setPose(Picode.pose("New pose (15)"));
      }
      break;
    case 4: // ソ
      if (open) {
        nxt.setPose(Picode.pose("New pose (12)"));
      } else {
        nxt.setPose(Picode.pose("New pose (11)"));
      }
      break;
    case 5: // ラ
      if (open) {
        nxt.setPose(Picode.pose("New pose (18)"));
      } else {
        nxt.setPose(Picode.pose("New pose (17)"));
      }
      break;
    default:
      break;
  }
  if (!open) {
    step ++;
    println("step " + step);
    step = step % tones.length;
  }
  open = !open;
}


