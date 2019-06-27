import Flutter
import UIKit
import AudioToolbox
import MACamera
import swiftScan

public class SwiftFlutterSysCallPlugin: NSObject, FlutterPlugin {
    var controller: FlutterViewController!
    var flutterResult:FlutterResult!;
    var messenger: FlutterBinaryMessenger;
    var timer:Timer!;
    var second=3;
    

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
        if(timer==nil){
            timer = Timer.scheduledTimer(timeInterval: 1, target: self, selector: #selector(updataSecond), userInfo: nil, repeats: true)
            //调用fire()会立即启动计时器
            timer!.fire()
        }
        
       
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
    
    //定时震动
    @objc func updataSecond() {
        if second>1 {
             AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
            second -= 1
        }else {
            //取消定时任务
         second=3
          timer.invalidate()
            timer=nil
            
        }
    }
    
    //显示摄像头
    
    func showCameras(){
    
        
        MACameraController.allowCameraAndPhoto { (allow) in
            let mController=MACameraController.init()
            mController.cameraCompletion={(mController,usn,image,krk) -> () in
                
                 mController?.dismiss(animated: true, completion: nil)
                
                if(krk){
                    self.flutterResult(usn!.path);
                }else{
                    
                    if(image==nil){
                        self.showCameras();
                        return;
                    }
                    
                    //要写入的文件夹路径和图片名
                    let dt:String = NSTemporaryDirectory().appending("seer.png") as String;
                    
                    //将Image文件写入 如上的文件夹
                    try? UIImagePNGRepresentation(image!)?.write(to: URL(fileURLWithPath: dt))
                    
                    self.flutterResult(dt)
                    
                }
               
            
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
