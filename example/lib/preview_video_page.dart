import 'dart:io';

import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

class PreviewVideoPage extends StatefulWidget {
  PreviewVideoPage(this.path, {Key key}) : super(key: key);

  String path;

  @override
  State<StatefulWidget> createState() {
    return PreviewVideoState();
  }
}

class PreviewVideoState extends State<PreviewVideoPage> {
  ///视频控制器
  VideoPlayerController _controller;

  bool isLoading = true;

  @override
  void initState() {
    super.initState();

    _controller = VideoPlayerController.file(File(widget.path))
      ..initialize().then((da) {
        _controller.play();
        isLoading = false;
        setState(() {});
      });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        body: Container(
      color: Colors.black,
      child: Stack(
        children: <Widget>[
          Center(
            child: _controller.value.initialized
                ? AspectRatio(
                    aspectRatio: _controller.value.aspectRatio,
                    child: VideoPlayer(_controller),
                  )
                : Container(),
          ),
          Center(
              child: Offstage(
            offstage: _controller.value.isPlaying,
            child: FloatingActionButton(
                onPressed: () {
                  setState(() {
                    _controller.value.isPlaying
                        ? _controller.pause()
                        : _controller.play();
                  });
                },
                child: isLoading
                    ? Text("加载中")
                    : _controller.value.isPlaying
                        ? Container()
                        : IconButton(
                            icon: Icon(Icons.play_arrow),
                            onPressed: () {
                              _controller.play();
                            })),
          )),
          ConstrainedBox(
            constraints: BoxConstraints(
                maxHeight: kToolbarHeight + MediaQuery.of(context).padding.top),
            child: AppBar(
              backgroundColor: Colors.transparent,
            ),
          )
        ],
      ),
    ));
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}
