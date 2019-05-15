import Flutter
import UIKit
import AudioToolbox

public class SwiftFlutterSysCallPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_sys_call", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterSysCallPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
   
    switch call.method {
    case "getPlatformVersion":
        result("iOS " + UIDevice.current.systemVersion);
         break;
    case "doVibrator":
        AudioServicesPlaySystemSound(1521);
        break;
    default:
        result(true);
    }
  }
}
