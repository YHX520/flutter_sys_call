import 'dart:async';
import 'dart:io';
import 'package:flutter/services.dart';

import 'package:permission_handler/permission_handler.dart';
import 'package:path_provider/path_provider.dart';

class FlutterSysCall {
  static const MethodChannel _channel = const MethodChannel('flutter_sys_call');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> get doVibrator async {
    final bool result = await _channel.invokeMethod('doVibrator');
    return result;
  }

  static Future<Map<String, String>> get doTakeVideo async {
    bool isPermission = await requestPermission(Platform.isAndroid
        ? [Permission.camera, Permission.microphone, Permission.storage]
        : [Permission.camera, Permission.microphone, Permission.photos]);
    if (isPermission) {
      String result = await _channel.invokeMethod('recordVideo');

      String path = (await getApplicationDocumentsDirectory()).path;
      String fileEnd = result.substring(result.length - 3, result.length);
      print(fileEnd);
      String fileType;

      if (fileEnd == "jpg" || fileEnd == "png") {
        if (Platform.isIOS) {
          result = (await File(result).rename(path + "/temp." + fileEnd)).path;
        }
        fileType = "image";
      } else {
        if (Platform.isIOS) {
          result = (await File(result).rename(path + "/temp." + fileEnd)).path;
        }
        fileType = "video";
      }

      return {"fileType": fileType, "filePath": result};
    } else {
      return {"fileType": "", "filePath": ""};
    }
  }

  static Future<String> get qrScan async {
    bool isPermission = await requestPermission([
      Permission.camera,
    ]);
    if (isPermission) {
      final String result = await _channel.invokeMethod('QRScan');
      return result;
    } else {
      return "0";
    }
  }

  ///请求权限
  static Future<bool> requestPermission(
      List<Permission> permissionsList) async {
    var flag = true;
    Map<Permission, PermissionStatus> permissions =
        await await [
          Permission.storage,
          Permission.camera
        ].request();
    permissions.forEach((Permission pg, PermissionStatus status) {
      if (status != PermissionStatus.granted) {
        flag = false;
      }
    });

    return flag;
  }
}
