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

### 完成了
* 主地图背景加载
* 简单完成了玩家类
* 简单完成了道具类
---------

## 第五天的开发 5-15
### 学习了
* 枚举类的定义与使用
* 加深了对访问修饰符的理解与使用逻辑

### 完成了
* 加入所有的建筑属性
* 加入两个道具
---------

## 第六天的开发 5-16
### 学习了
* MouseAdapter 可少写 MouseListener方法
* 接口回调
* Timer的简单使用
* d.drawImage的画图逻辑 

### 完成了
* AI制作骰子动画
>帮我看看代码的骰子图像为什么无法加载，直接给解释就行，同时告诉我如何实现投掷骰子的动画基本实现  
> 我只是想看看为什么静止状态没骰子     
>  我的diceImg里面其实是动画精灵表，我只是把最后一张当作是静止的图片，那我的投掷骰子的动画怎么做
* <span style="color:red">AI对于图片处理存在很大不足，还得自己手动校对，人机就是人机</span>
---------

## 第七天的开发 5-17
### 学习了
* 改变foreach 的迭代顺序
* 让AI简化我的逻辑，尽管我会了，但是可以精简
### 完成了
* 未出售的房子的渲染
---------

## 第八天的开发 5-18
### 学习了
* 弹出提示框 JOptionPane全能提示框
* 图片导入顺序的逻辑

### 完成了
* 交钱系统 
* 把各个可购买地产分类
* ---------

## 第九天的开发 5-19
### 学习了
* JOptionPane.INFORMATION_MESSAGE等常量
* toString 在调试中的运用

### 完成了
* 三个层级的绘图逻辑重构
* 游戏获胜逻辑
* 交税功能
* 起点发钱功能
* 个人信息ui完成
* 房子购买与升级后的渲染
---------

## 第九天的开发 5-19
### 学习了
* 区别Swing.Timer与Javax.util.Timer
* EDT的认识
* 回调优点:解耦

### 完成了
* Event格子的事件触发
* onArrive重写
---------

## 第九天的开发 5-19
### 学习了
* 利用日志调试游戏信息
* HashMap的各种方法

### 完成了
* 玩家信息调试及输出
---------

## 第十天的开发 5-26
### 学习了
* 更加了解面向对象设计
* 用道具名锁定玩家的道具对象
* Enum枚举与集合
* JComboBox
* Consumer接口
* switch 表达式中的 yield 关键字
* 回调注册，回调的多种使用情况

### 完成了
* 道具的点击逻辑
* 道具使用的弹窗设计
* 道具列表的完善
---------

## 第十一天的开发 5-27
### 学习了
* %n 换行比 \n 更安全，可以跨平台换行
````java
        //<editor-fold desc="AI 修改之前">
        for (int i = boardConfig.getTiles().length - 1; i >= 0; i--) {
            Tile tile = boardConfig.getTiles()[i];

            if (tile instanceof Land) {
                Land land = (Land) tile;
                // 只有当地产有所有者且有建筑时才渲染
                if (land.getOwner() != null ) {
                    renderBuildingForLand(g, land);
                } else {
                    renderBuidingForNull(g, land);
                }
            }
        }
        //</editor-fold>
````

### 完成了
* 优化代码的逻辑
* AI帮助下 完成了对 MainMap 的基本重构：

> 骰子控制提取成类
> 
> 三个面板单独为三个类
