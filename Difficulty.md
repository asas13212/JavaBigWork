开发难点
======
* 游戏主循环用什么实现
* 各个动画的实现逻辑
* 道具使用逻辑
* 各个事件完成的先后
* 对各个类功能理解
* 各个地产格子位置确定
----
* 在Event类中有一个事件，让玩家前进几步，但是无法直接调用startWalk
* 在Player中有startWalk()方法,这个方法只是设置状态，真正的移动需要外部的 Timer 驱动
* AI解决方案：将「动画结束后要执行的逻辑」通过 Runnable 接口（回调接口）传入 startWalkAnimation，动画完成后调用 onFinished.run() 触发回调，让「动画逻辑」和「结束后业务逻辑」解耦。
* 我的解决：直接在Event创建Timer 传入walkTiemr,自己单开一个线程走
* 解耦：动画归动画，业务归业务
  startWalkAnimation 只负责 “驱动走路动画、停止动画、刷新 UI”，完全不关心 “动画结束后要做什么”；
  结束后要做的事（afterMoveResolveTile()/ 播放音效 / 触发对话），由调用 startWalkAnimation 时传入的回调决定 —— 两个逻辑互不干扰，改一个不用动另一个。