开发日志
=========
---------
## 第一天的开发 5-11
### 学习了
* 用ToolKit导入图片
* JFrame的子类重写paint来显示图片
* 直接输入abbr可以直接快速注释
### 完成了
* 导入动画的实现

---------

## 第二天的开发 5-12
### 学习了
* md的基本写法
* 常量要写在接口中
* paint()与paintComponent()区别
>paintComponent更新更好，可以达到组件级别的控制
* JLabel.setBounds的绘画逻辑
>后add的显示在最上面
* 更深入了解了鼠标的监听逻辑

### 完成了
* 游戏菜单的ui制作
---------

## 第三天的开发 5-13
### 学习了
* e.getSource()的底层逻辑，以及使用向下转型接收
* 基础的JLabel更改图标样式方法
* cardLayout 的使用方法
* Container中setCompontZOrder方法
* 用MouseAdapter代替MouseListener

### 完成了
* 游戏说明与游戏制作人员界面切换
---------

## 第四天的开发 5-14
### 学习了
* JLabel不能直接加图片，用Panel加Label,加图片用setImageIcon
* 利用分层布局实现ui与贴图的分离
* 在面板 / 按钮事件里，关闭所在的父窗口。
* Swing 的简单绘画逻辑

### 完成了
* 主地图背景加载
* 简单完成了玩家类
* 简单完成了道具类