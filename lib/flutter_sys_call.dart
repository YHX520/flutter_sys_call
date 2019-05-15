import 'dart:async';

import 'package:flutter/services.dart';

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
}
