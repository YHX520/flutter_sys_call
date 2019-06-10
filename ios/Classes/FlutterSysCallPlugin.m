#import "FlutterSysCallPlugin.h"

#import <flutter_sys_call/flutter_sys_call-Swift.h>


@implementation FlutterSysCallPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterSysCallPlugin registerWithRegistrar:registrar];
}
@end
