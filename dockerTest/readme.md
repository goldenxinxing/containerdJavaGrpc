- 需要的能力
  - 挂载目录，供容器运行时将结果写入：result/{taskid}/
  - 挂载文件，将运行时状态更新写入【不用container监控，因为container结束可能是非正常结束】
  - 指定CPU、GPU及相应quota
  - 传入环境变量（任务id、模型信息、ds信息）

- GPU 相关
  - cuda 和 gpu关系？
  - 如何在docker-client中配置gpu quota？
  - 如何获取nvidia gpu信息：https://blog.csdn.net/qq_36874177/article/details/109534115