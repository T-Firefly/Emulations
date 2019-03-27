# 模拟器集（Emulations)
模拟器集是一个Leanback风格的Android tv模拟器游戏管理APP。模拟器集扫描特定目录下的游戏rom，
添加到数据库中同时可以从[The Games Db](https://thegamesdb.net)获取游戏信息，
通过[RetroArch](http://retroarch.com/)打开游戏。

**仅支持arm Android N，不支持x86**

## 源码编译
1. 设置The Games Db APPKEY
    - 到The Games Db论坛申请APPKEY
    - 在源码tv/src/main/java/com/firefly/emulationstation/commom/Constants.java
        中修改变量`THE_GAMES_DB_APPKEY`的值为申请得到的APPKEY
2. 使用Android Studio打开项目并编译


## 游戏信息获取
游戏信息会从[The Games Db](https://thegamesdb.net)获取，编译时需要设置`The Games Db APPKEY`。

获取游戏信息时需要提供正确的游戏英文名，可通过映射的方式设置，设置方式参考[map/custom/README.md](./tv/src/main/assets/EmulationStation/map/custom/README.md)。

 
## 模拟器配置
模拟器配置在源码中位置为tv/src/main/assets/EmulationStation/systems.xml，在Android系统中为/sdcard/EmulationStation/systems.xml。

一个模拟器配置示例如下：
```xml
<systems>
    <system>
        <name>Arcade</name>
        <platform>arcade</platform>
        <extension>.zip</extension>
        <emulator>
            <name>retroarch</name>
            <core>${ES_DIR}/cores/fbalpha_libretro_android.so</core>
        </emulator>
        <romPath>${device}/EmulationStation/roms/arcade</romPath>
    </system>
</systems>
```
解析如下：
1. name: 模拟器显示的名称
2. platform: platformId，参考[map/custom/README.md](./tv/src/main/assets/EmulationStation/map/custom/README.md)
3. extension: rom的后缀名，如支持多个后缀可用空格分隔，如“.zip .fc”。
4. emulator: 设置启动游戏使用的模拟器
    - name: APP名，目前仅支持retroarch
    - core: retroarch加载的核心
5. romPath：游戏rom存储的路径

变量：`${ES_DIR}`被替换为/sdcard/EmulationStation，`${device}`会被替换为各个连接存储设备的根目录。

## 在线游戏下载
模拟器集支持设置游戏仓库提供APP内游戏下载功能。用户可从网上收集rom做成仓库自己或提供给他人使用。

一个仓库的目录结构如下：
```bash
repository/
         |__ com.example/
             |__ repo.json
             |__ platforms/
                 |__ nes.json
                 |__ psp.json
                     ...
```
解析如下：
1. com.example: 仓库标识，唯一值
2. repo.json: 仓库信息及入口文件，命名必须为repo.json
3. platforms: 仓库下支持的游戏平台rom信息
4. platforms/xxx.json: 游戏平台rom信息

### 文件结构
#### repo.json
```json
{
    "id": "com.firefly",
    "name": {
        "default": "ES repository",
        "zh": "ES仓库"
    },
    "description": {
        "default": "This is a official repository.",
        "zh": "这是一个官方仓库"
    },
    "version": "1.0.0",
    "url": "https://raw.githubusercontent.com/hyt-tchip/com.firefly/master/repo.json",
    "platforms": [
        {
            "platformId": "arcade",
            "url": "https://raw.githubusercontent.com/hyt-tchip/com.firefly/master/platforms/arcade.json"
        },
        ...
    ]
}
```
解析如下：
1. id: 仓库标识，唯一值，对应仓库根目录名
2. name: 仓库显示名
    - default: 仓库默认名
    - zh（语言代码）: 中文名称, 可添加不同语言的支持。
3. description: 仓库描述，格式与name相同
4. version: 仓库版本
5. url: 仓库更新下载地址
6. platforms: 仓库支持的平台
    - platformId: 平台id，参考[map/custom/README.md](./tv/src/main/assets/EmulationStation/map/custom/README.md)
    - url: 平台rom列表下载地址
    
#### platforms/xx.json
```json
[
    {
        "displayName": {
            "default": "Cadillacs and Dinosaurs", 
            "en": "Cadillacs and Dinosaurs", 
            "zh": "恐龙新世纪 (930201 世界版)"
        }, 
        "name": "dino.zip", 
        "fanart": "https://cdn.thegamesdb.net/images/original/fanart/7870-1.jpg", 
        "boxart": "https://cdn.thegamesdb.net/images/thumb/boxart/front/7870-1.jpg",
        "system": "arcade", 
        "url": "http://download.freeroms.com/mame_roms/c/dino.zip",
        "version": "1.0.0",
        "description": {
            "default": "Cadillacs and Dinosaurs: The Arcade Game is a 1993 arcade game released by Capcom. It is a side-scrolling beat 'em up based on the comic book series Xenozoic Tales. The game was produced as a tie-in to the Cadillacs and Dinosaurs animated series which was aired during the same year the game was released."
        }
    }, 
    ...
]
```
解析如下：
1. displayName: rom显示名称，支持多语言，default会用于获取游戏信息
2. name: rom文件名
3. fanart: 游戏背景图片
4. boxart: 游戏封面图片
5. system: 游戏平台，对应platformId
6. url: rom下载地址
7. version: rom版本
8. description: 游戏介绍，支持多语言
