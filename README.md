# 模拟器集（Emulations)
模拟器集是一个Leanback风格的Android tv模拟器游戏管理APP。模拟器集扫描特定目录下的游戏rom，
添加到数据库中同时可以从[The Games Db](https://thegamesdb.net)获取游戏信息，
通过[RetroArch](http://retroarch.com/)打开游戏。

## 源码编译
### 设置The Games Db APPKEY
1. 首先到The Games Db论坛申请APPKEY
2. 在源码tv/src/main/java/com/firefly/emulationstation/commom/Constants.java
中修改变量`THE_GAMES_DB_APPKEY`的值为得到的APPKEY

### 使用Android Studio打开项目并编译


## 游戏信息获取


### 游戏平台与平台ID  

| PlatformId      | Platform                            |
|:----------------|:------------------------------------|
| 3do             | 3DO                                 |
| amiga           | Amiga                               |
| amstradcpc      | Amstrad CPC                         |
| arcade          | Arcade                              |
| atari2600       | Atari 2600                          |
| atari5200       | Atari 5200                          |
| atari7800       | Atari 7800                          |
| atarijaguar     | Atari Jaguar                        |
| atarijaguarcd   | Atari Jaguar CD                     |
| atarilynx       | Atari Lynx                          |
| atarixe         | Atari XE                            |
| colecovision    | Colecovision                        |
| c64             | Commodore 64                        |
| intellivision   | Intellivision                       |
| macintosh       | Mac OS                              |
| xbox            | Microsoft Xbox                      |
| xbox360         | Microsoft Xbox 360                  |
| msx             | MSX                                 |
| neogeo          | Neo Geo                             |
| ngp             | Neo Geo Pocket                      |
| ngpc            | Neo Geo Pocket Color                |
| n3ds            | Nintendo 3DS                        |
| n64             | Nintendo 64                         |
| nds             | Nintendo DS                         |
| fds             | Famicom Disk System                 |
| nes             | Nintendo Entertainment System (NES) |
| gb              | Nintendo Game Boy                   |
| gba             | Nintendo Game Boy Advance           |
| gbc             | Nintendo Game Boy Color             |
| gc              | Nintendo GameCube                   |
| wii             | Nintendo Wii                        |
| wiiu            | Nintendo Wii U                      |
| virtualboy      | Nintendo Virtual Boy                |
| gameandwatch    | Game & Watch                        |
| pc              | PC                                  |
| sega32x         | Sega 32X                            |
| segacd          | Sega CD                             |
| dreamcast       | Sega Dreamcast                      |
| gamegear        | Sega Game Gear                      |
| genesis         | Sega Genesis                        |
| mastersystem    | Sega Master System                  |
| megadrive       | Sega Mega Drive                     |
| saturn          | Sega Saturn                         |
| sg-1000         | SEGA SG-1000                        |
| psx             | Sony Playstation                    |
| ps2             | Sony Playstation 2                  |
| ps3             | Sony Playstation 3                  |
| ps4             | Sony Playstation 4                  |
| psvita          | Sony Playstation Vita               |
| psp             | Sony Playstation Portable           |
| snes            | Super Nintendo (SNES)               |
| pcengine        | TurboGrafx 16                       |
| wonderswan      | WonderSwan                          |
| wonderswancolor | WonderSwan Color                    |
| zxspectrum      | Sinclair ZX Spectrum                |
| videopac        | Magnavox Odyssey 2                  |
| vectrex         | Vectrex                             |
| trs-80          | TRS-80 Color Computer               |
| coco            | TRS-80 Color Computer               |


### 游戏名称映射
Map file name: \<platformId\>.txt. e.g: arcade.txt
 
 
## 模拟器配置
 
## 在线游戏下载
### 游戏仓库添加