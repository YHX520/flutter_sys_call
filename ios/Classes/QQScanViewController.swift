//
//  QQScanViewController.swift
//  flutter_sys_call
//
//  Created by 华轩 on 2019/6/6.
//

import Foundation
//
//  QQScanViewController.swift
//  swiftScan
//
//  Created by xialibing on 15/12/10.
//  Copyright © 2015年 xialibing. All rights reserved.
//
import UIKit
import swiftScan


typealias QBcode = (String) ->()


class QQScanViewController: LBXScanViewController {
    
     var code=""
    
    /**
     @brief  扫码区域上方提示文字
     */
    var topTitle: UILabel?
    
    /**
     @brief  闪关灯开启状态
     */
    var isOpenedFlash: Bool = false
    
    // MARK: - 底部几个功能：开启闪光灯、相册、返回
    //底部显示的功能项
    var bottomItemsView: UIView?
    
    //相册
    var btnPhoto: UIButton = UIButton()
    
    //闪光灯
    var btnFlash: UIButton = UIButton()
    
    //我的二维码
    var btnMyQR: UIButton = UIButton()
    
    var qbCode: QBcode?
    
    // 或者使用实例方法调用（方法名字不固定，但参数是必须的）
  open  func setMyChangeName(tempClose: @escaping QBcode)  {
        self.qbCode = tempClose
    }
    
    override func viewDidLoad() {

        super.viewDidLoad()
        
        //需要识别后的图像
        setNeedCodeImage(needCodeImg: true)
        
        //框向上移动10个像素
        scanStyle?.centerUpOffset += 10
        
        // Do any additional setup after loading the view.
    }
    
    override func viewDidAppear(_ animated: Bool) {
        
        super.viewDidAppear(animated)
        
        drawBottomItems()
    }
    
    override func handleCodeResult(arrayResult: [LBXScanResult]) {
        
        for result: LBXScanResult in arrayResult {
            if let str = result.strScanned {
                print("扫码结果"+str)
            }
        }
        
        let result: LBXScanResult = arrayResult[0]
        
        self.dismiss(animated: true) {
            self.qbCode!(result.strScanned!);
        }

    }
    
    func drawBottomItems() {
        if (bottomItemsView != nil) {
            
            return
        }
        
        let yMax = self.view.frame.maxY - self.view.frame.minY
        
        bottomItemsView = UIView(frame: CGRect(x: 0.0, y: yMax-100, width: self.view.frame.size.width, height: 100 ) )
        
        bottomItemsView!.backgroundColor = UIColor(red: 0.0, green: 0.0, blue: 0.0, alpha: 0.6)
        
        self.view .addSubview(bottomItemsView!)
        
        let size = CGSize(width: 65, height: 87)
        
        self.btnFlash = UIButton()
        btnFlash.bounds = CGRect(x: 0, y: 0, width: size.width, height: size.height)
        btnFlash.center = CGPoint(x: bottomItemsView!.frame.width/2, y: bottomItemsView!.frame.height/2)
        
        btnFlash.setImage(UIImage(named: "qrcode_scan_btn_flash_nor"), for:UIControl.State.normal)
        btnFlash.addTarget(self, action: #selector(QQScanViewController.openOrCloseFlash), for: UIControl.Event.touchUpInside)
        
        
        self.btnPhoto = UIButton()
        btnPhoto.bounds = btnFlash.bounds
        btnPhoto.center = CGPoint(x: bottomItemsView!.frame.width/4, y: bottomItemsView!.frame.height/2)
        btnPhoto.setImage(UIImage(named: "qrcode_scan_btn_photo_nor"), for: UIControl.State.normal)
        btnPhoto.setImage(UIImage(named: "qrcode_scan_btn_photo_down"), for: UIControl.State.highlighted)
        //        btnPhoto.addTarget(self, action: Selector(("openPhotoAlbum")), for: UIControlEvents.touchUpInside)
        btnPhoto.addTarget(self, action: #selector(QQScanViewController.openLocalPhotoAlbum), for: UIControl.Event.touchUpInside)
        
        self.btnMyQR = UIButton()
        btnMyQR.bounds = btnFlash.bounds;
        btnMyQR.center = CGPoint(x: bottomItemsView!.frame.width * 3/4, y: bottomItemsView!.frame.height/2);
        btnMyQR.setImage(UIImage(named: "back_nor"), for: UIControl.State.normal)
        btnMyQR.setImage(UIImage(named: "back_pressed"), for: UIControl.State.highlighted)
        btnMyQR.addTarget(self, action: #selector(QQScanViewController.myCode), for: UIControl.Event.touchUpInside)
        
         bottomItemsView?.addSubview(btnPhoto)
        bottomItemsView?.addSubview(btnFlash)
        bottomItemsView?.addSubview(btnMyQR)
        
        self.view .addSubview(bottomItemsView!)
        self.startScan();
        
    }
    
    @objc func openLocalPhotoAlbum()
    {
        super.openPhotoAlbum();
    }
    
    //开关闪光灯
    @objc func openOrCloseFlash() {
        scanObj?.changeTorch()
        
        isOpenedFlash = !isOpenedFlash
        
        if isOpenedFlash
        {
            btnFlash.setImage(UIImage(named: "qrcode_scan_btn_flash_down"), for:UIControl.State.normal)
        }
        else
        {
            btnFlash.setImage(UIImage(named: "qrcode_scan_btn_flash_nor"), for:UIControl.State.normal)
            
        }
    }
    
    @objc func myCode() {
        qbCode!("")
       self.dismiss(animated: true, completion: nil)
    }
    
}
