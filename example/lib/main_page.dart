import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_sys_call/flutter_sys_call.dart';
import 'package:flutter_sys_call_example/preview_video_page.dart';
import 'package:path_provider/path_provider.dart';

class MainPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return MainStat();
  }
}

class MainStat extends State<MainPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
          child: Column(
        children: <Widget>[
          RaisedButton(
            onPressed: () {
              doVibrator();
            },
            child: Text("震动"),
          ),
          RaisedButton(
            onPressed: () {
              takePhotoOrVideo(context);
            },
            child: Text("拍视频"),
          ),
          RaisedButton(
            onPressed: () {
              qrScan();
            },
            child: Text("扫描二维码"),
          ),
        ],
      )),
    );
  }

  ///调用震动
  doVibrator() async {
    await FlutterSysCall.doVibrator;
  }

  ///调用视频录像
  takePhotoOrVideo(BuildContext context) async {
    Map<String, String> path = await FlutterSysCall.doTakeVideo;
    print(path);

    if (path["fileType"].isNotEmpty) {
      if (path["fileType"] == "image") {
        print("这是一张图片" + path["filePath"]);
      } else {
        print("这是视频");
        Navigator.push(
            context,
            MaterialPageRoute(
                builder: (context) => PreviewVideoPage(
                    path["filePath"].replaceAll(RegExp("file://"), ""))));
      }
    } else {
      print("可能已经拒绝了权限的分配");
    }

    print(path);
  }

  ///调用二维码扫一扫
  qrScan() async {
    String result = await FlutterSysCall.qrScan;
    print(result);
  }
}
