allMedias多媒体插件
---------------------------
本人刚好需要一个能支持传统多媒体格式的插件，就自己编写了此插件，采用系统内置的Windows Media Player、RealPlayer、QuickTime和JWPlayer来支持一下多媒体格式：

        AVI        WMV        MPEG        MPG        MP4        FLV
        PDF        M4V        WAV        MIDI        MP3        ASF
        RM        RMVB        RA        QT        MOV        M4V

 安装本插件非常简单了，在配置(config.js)里增加插件的引用

    config.extraPlugins = 'allmedias';

如果采用了定制的工具栏，请在定制里加入'allMedias'.
v1.00 by codeex.cn
最新开发消息请访问 http://codeex.cn/category/web/ck ， 欢迎加qq群：173171134