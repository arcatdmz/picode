namespace java jp.digitalmuseum.kinect
namespace csharp Jp.Digitalmuseum.Kinect

enum JointType {
   HIP_CENTER = 0,
   SPINE = 1,
   SHOULDER_CENTER = 2,
   HEAD = 3,
   SHOULDER_RIGHT = 4,
   ELBOW_RIGHT = 5,
   WRIST_RIGHT = 6,
   HAND_RIGHT = 7,
   SHOULDER_LEFT = 8,
   ELBOW_LEFT = 9,
   WRIST_LEFT = 10,
   HAND_LEFT = 11,
   HIP_RIGHT = 12,
   KNEE_RIGHT = 13,
   ANKLE_RIGHT = 14,
   FOOT_RIGHT = 15,
   HIP_LEFT = 16,
   KNEE_LEFT = 17,
   ANKLE_LEFT = 18,
   FOOT_LEFT = 19
}

struct Position3D {
  1: required double x,
  2: required double y,
  3: required double z
}

struct Position2D {
  1: required double x,
  2: required double y
}

struct Joint {
  1: required JointType type,
  2: required Position3D position,
  3: required Position2D screenPosition
}

struct Frame {
  1: required i32 frameId,
  2: required binary image,
  3: required Position3D position,
  4: required list<Joint> joints,
  5: optional set<string> keywords
}

service KinectService {
  oneway void addKeyword(1:string text),
  oneway void removeKeyword(1:string text),
  set<string> getKeywords(),
  oneway void setAngle(1:i32 angle),
  i32 getAngle(),
  Frame getFrame(),
  oneway void stop();
}
