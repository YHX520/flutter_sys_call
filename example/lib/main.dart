import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_sys_call/flutter_sys_call.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterSysCall.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
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
                takePhotoOrVideo();
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
      ),
    );
  }

  ///调用震动
  doVibrator() async {
    await FlutterSysCall.doVibrator;
  }

  ///调用视频录像
  takePhotoOrVideo() async {
    String path = await FlutterSysCall.doTakeVideo;
    print(path);
  }

  ///调用二维码扫一扫
  qrScan() async {
    String result = await FlutterSysCall.qrScan;
    print(result);
  }
}
