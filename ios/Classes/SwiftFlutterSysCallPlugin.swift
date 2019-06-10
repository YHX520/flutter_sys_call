import Flutter
import UIKit
import AudioToolbox
import MACamera
import swiftScan

public class SwiftFlutterSysCallPlugin: NSObject, FlutterPlugin {
    var controller: FlutterViewController!
    var flutterResult:FlutterResult!;
    var messenger: FlutterBinaryMessenger;
    

    init(cont: FlutterViewController, messenger: FlutterBinaryMessenger) {
        self.controller = cont;
        self.messenger = messenger;
        super.init();
    }
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    
    let channel = FlutterMethodChannel(name: "flutter_sys_call", binaryMessenger: registrar.messenger())
    let app =  UIApplication.shared;
    let controller : FlutterViewController = app.delegate!.window!!.rootViewController as! FlutterViewController;
    let instance = SwiftFlutterSysCallPlugin.init(cont: controller, messenger: registrar.messenger())
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    flutterResult=result;
    switch call.method {
    case "getPlatformVersion":
        result("iOS " + UIDevice.current.systemVersion);
         break;
    case "doVibrator":
        AudioServicesPlaySystemSound(1521);
        break;
    case "recordVideo":
        showCameras();
        break;
    case "QRScan":
        showScanView();
        break;
    default:
        result(true);
    }
  }
    //显示摄像头
    
    func showCameras(){
    
        
        MACameraController.allowCameraAndPhoto { (allow) in
            let mController=MACameraController.init()
            mController.cameraCompletion={(mController,usn,uiIMage,krk) -> () in
                self.flutterResult(usn?.absoluteString);
                mController?.dismiss(animated: true, completion: nil)
            
            }
           self.controller.present(mController, animated: true, completion:nil)
        }
    }
    
    
    func showScanView(){
        let vc = QQScanViewController()
        vc.setMyChangeName{
            (qbCode) in
            self.flutterResult(qbCode)
        }
        var style = LBXScanViewStyle()
        style.animationImage = UIImage(named: "qrcode_scan_light_green")
        vc.scanStyle = style
        controller.present(vc, animated: true, completion: nil)
    }
}

class Url {
    var  url = "";
    
}
