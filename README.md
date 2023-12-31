# 🛠 java常用脚本

🍚本项目建立的初衷是在于日常开发中，经常会遇到一些小问题需要我们写一些 *⚒小工具* 去解决。那么我就想能不能把这些常用工具收敛到这一个项目中供大家作为脚手架使用呢？

本项目目录：

1. `TreeGraph`：将列表转换为树结构，并在控制台进行打印，通过此工具可以得将包含（当前父节点id，子节点id，父节点名称，子节点名称，子节点属性）的csv文件完成可视化，其输出结果为
    ```
   └── 父节点(null)
    └── 子节点2(100.00)
        ├── 子节点3(200.00)
        │   ├── 子节点3(0)
        │   └── 子节点4(10)
        │       └── 子节点5(1)
        ├── 子节点4(10)
        │   └── 子节点5(1)
        └── 子节点5(1)
   ```
2. `CompareFiles`: 比较两个大文件中不一致的文本行，通过滑动窗口的方式进行比较，最后输出不一致的文本行总数，并打印住不一致的文本行和其最匹配的结果，只打印前5条
   ```
   查询不一致的数据条数: 1
   新文件中查询不到的数据为: 	at org.example.CompareFiles.compareFiles(CompareFiles.java:35)
   在新文件中匹配到的数据为: 	at org.example.CompareFiles.main(CompareFiles.java:27)
   在新文件中不一致的字段为: 	at org.example.CompareFiles.**************************
   ```
3. `CompareJsonFiles`: 比较两个json大文件中不一致的json行，在忽略指定不校验字段的情况下，通过滑动窗口的方式进行比较，最后输出不一致的json行总数，并打印出不一致的文本行及其最匹配的结果，只打印前5条
    ```
    查询不一致的数据条数: 3
    新文件中查询不到的数据为: {"id":"7","name":"zhangbeihai","score":"17","teacher":"santi"}
    在新文件中匹配到的数据为: {"id":"7","name":"zhangbeihai","score":"17","teacher":""}
    在新文件中不一致的字段为: {"id":"7","name":"zhangbeihai","score":"17","teacher":"**
    新文件中查询不到的数据为: {"id":"8","name":"luoxing1","score":"18","teacher":"nami"}
    在新文件中匹配到的数据为: {"id":"8","name":"luoxing","score":"18","teacher":""}
    在新文件中不一致的字段为: {"id":"8","name":"luoxing**************************"*
    新文件中查询不到的数据为: {"id":"6","name":"luoji","score":"16","teacher":"zhizi"}
    在新文件中匹配到的数据为: {"id":"6","name":"luoji","score":"16","teacher":""}
    在新文件中不一致的字段为: {"id":"6","name":"luoji","score":"16","teacher":"**
   ```