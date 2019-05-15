import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_sys_call/flutter_sys_call.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_sys_call');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterSysCall.platformVersion, '42');
  });
}
