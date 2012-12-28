import com.phybots.p5.*;
import com.phybots.p5.andy.*;
import com.phybots.hakoniwa.*;

Hakoniwa hakoniwa;
PhybotsImage img;
MobileRobot robot;
Position goal;

void setup()
{
  hakoniwa = new Hakoniwa(640, 480);
  hakoniwa.start();
  img = new PhybotsImage(hakoniwa);

  robot = new MobileRobot(
    new HakoniwaRobot(
      hakoniwa.getRealWidth() / 2,
      hakoniwa.getRealHeight() / 2));

  goal = new Position();
  goal.setScreen(-1, -1);

  size(640, 480);
}

void mouseClicked() {
  goal.setScreen(mouseX, mouseY);
  robot.moveTo(goal);
}

void draw()
{
  image(img, 0, 0);
  fill(0);

  text("Click to specify the goal.", 5, 18);

  Position p = robot.getPosition();
  int x = p.getScreenX();
  int y = p.getScreenY();
  line(x, y, x + 20, y - 15);
  line(x + 20, y - 15, x + 60, y - 15);
  text("robot", x + 25, y - 18);

  if (goal.getScreenX() >= 0) {
    x = goal.getScreenX();
    y = goal.getScreenY();
    line(x-5, y-5, x+5, y+5);
    line(x-5, y+5, x+5, y-5);
  }
}
