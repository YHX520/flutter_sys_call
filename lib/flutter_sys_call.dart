import 'dart:async';
import 'dart:io';
import 'package:flutter/services.dart';

import 'package:permission_handler/permission_handler.dart';

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

  static Future<String> get doTakeVideo async {
    bool isPermission = await requestPermission(Platform.isAndroid
        ? [
            PermissionGroup.camera,
            PermissionGroup.microphone,
            PermissionGroup.storage
          ]
        : [PermissionGroup.camera, PermissionGroup.microphone]);
    if (isPermission) {
      final String result = await _channel.invokeMethod('recordVideo');
      return result;
    } else {
      return "0";
    }
  }

  static Future<String> get qrScan async {
    bool isPermission = await requestPermission([
      PermissionGroup.camera,
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
      List<PermissionGroup> permissionsList) async {
    var flag = true;
    Map<PermissionGroup, PermissionStatus> permissions =
        await PermissionHandler().requestPermissions(permissionsList);
    permissions.forEach((PermissionGroup pg, PermissionStatus status) {
      if (status != PermissionStatus.granted) {
        flag = false;
      }
    });

    return flag;
  }
}
