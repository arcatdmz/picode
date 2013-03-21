namespace java com.phybots.picode.api.remote

const i32 SERVER_DEFAULT_PORT = 50001

struct MindstormsNXTPoseData {
  1: required i32 a,
  2: required i32 b,
  3: required i32 c
}

service MindstormsNXTService {
  i32 connect(1:string identifier),
  MindstormsNXTPoseData getPose(1:i32 id),
  bool setPose(1:i32 id, 2:MindstormsNXTPoseData pose),
  bool isActing(1:i32 id),
  oneway void reset(1:i32 id)
}
