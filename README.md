# 用户手册

## 系统简介
   本系统是一个基于查询洪泛机制的资源共享应用程序。它允许多个节点共享和搜索文件。系统包括三个主要组件：中央服务器、节点（Peer）和客户端（Client）。

## 安装步骤
   下载并安装 Java 运行环境（如果尚未安装），并下载系统的 JAR 包，并解压到目标目录。

## 使用指南
   ### 启动中央服务器 Central Server
   1. 打开终端（命令提示符）
   2. 进入保存 JAR 包的目录
   3. 运行以下命令启动中央服务器：
      
      ```sh
      java -jar CentralServer.jar
      ```
      服务器成功启动后，会在控制台输出“Central server started at port: 12340”
      
   ### 启动节点 Peer
   1. 准备好共享文件的文件夹
   2. 打开终端（命令提示符）
   3. 进入保存 JAR 包的目录
   4. 运行以下命令启动节点：
      
      ```sh
      java -jar Peer.jar <共享目录路径> <端口号>
      ```
      例如：

      ```sh
      java -jar Peer.jar /path/to/shared 12345
      ```
      节点成功启动后，会自动注册到中央服务器，并在控制台输出“Peer started at port: <端口号>”。
      
   ### 运行客户端 Client
   1. 打开终端（命令提示符）
   2. 进入保存 JAR 包的目录
   3. 运行以下命令启动客户端：
      
      ```sh
      java -jar Client.jar <节点地址> <端口号> <文件名>
      ```
      例如：

      ```sh
      java -jar Client.jar localhost 12345 example.txt
      ```
      客户端会向指定节点发送查询请求，并尝试下载匹配的文件，文件保存路径与JAR包目录所在位置相同。

   ### 常见问题与解决方案
  #### 端口号冲突
   如果启动节点时出现 “Address already in use: bind” 错误，请确保每个节点使用不同的端口号。
  #### 文件未找到
   如果客户端查询文件时未找到，请确保节点的共享目录中包含目标文件，并且其他节点已正确注册到中央服务器。
  #### 网络连接问题
   如果节点或客户端无法连接，请检查网络连接是否正常，并确保防火墙未阻止相关端口。

  ## 结尾
  感谢您使用本系统。如果您有任何问题或建议，请积极联系我。希望本系统能为您的文件共享需求提供便捷的解决方案。
