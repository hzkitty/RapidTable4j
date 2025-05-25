<div align="center">
  <div align="center">
    <h1><b>📊 RapidTable4j</b></h1>
  </div>
</div>

### 简介

RapidTable库是专门用来文档类图像的表格结构还原，表格结构模型均属于序列预测方法，结合RapidOCR，将给定图像中的表格转化对应的HTML格式。

本项目是RapidTable的Java移植版本，使用 ONNXRuntime + OpenCV，结合RapidOCR4j实现表格识别。

slanet_plus是paddlex内置的SLANet升级版模型，准确率有大幅提升

### 效果展示

<div align="center">
    <img src="https://github.com/RapidAI/RapidTable/releases/download/assets/preview.gif" alt="Demo" width="80%" height="80%">
</div>

### 模型列表 [下载地址](https://github.com/hzkitty/RapidTable4j/releases/tag/v0.0.0)

|      `model_type`      |                  模型名称                  | 推理框架 |模型大小 |推理耗时(单图 60KB)|
  |:--------------|:--------------------------------------| :------: |:------ |:------ |
|       `ppstructure_en`       | `en_ppstructure_mobile_v2_SLANet.onnx` |   onnxruntime   |7.3M |0.15s |
|       `ppstructure_zh`       | `ch_ppstructure_mobile_v2_SLANet.onnx` |   onnxruntime   |7.4M |0.15s |
| `slanet_plus` |          `slanet-plus.onnx`           |  onnxruntime    |6.8M |0.15s |

模型来源\
[PaddleOCR 表格识别](https://github.com/PaddlePaddle/PaddleOCR/blob/133d67f27dc8a241d6b2e30a9f047a0fb75bebbe/ppstructure/table/README_ch.md)\
[PaddleX-SlaNetPlus 表格识别](https://github.com/PaddlePaddle/PaddleX/blob/release/3.0-beta1/docs/module_usage/tutorials/ocr_modules/table_structure_recognition.md)\
模型下载地址：[link](https://www.modelscope.cn/models/RapidAI/RapidTable/files)

### 安装

由于模型较小，预先将slanet-plus表格识别模型(`slanet-plus.onnx`)打包进了jar包内。其余模型在初始化`RapidTable`类时，通过`TableConfig的modelPath`来指定自己模型路径。注意仅限于现在支持的`TableModelType`。


## 🎉 快速开始

安装依赖，默认使用CPU版本
```xml
<dependency>
    <groupId>io.github.hzkitty</groupId>
    <artifactId>rapid-table4j</artifactId>
    <version>1.0.0</version>
</dependency>
```
使用示例
```java
RapidTable tableEngine = RapidTable.create();
RapidOCR rapidOCR = RapidOCR.create();

File file = new File("src/test/resources/table_01.jpg");
String imgContent = file.getAbsolutePath();
OcrResult ocrResult = rapidOCR.run(imgContent);
TableResult tableResult = tableEngine.run(imgContent, ocrResult.getRecRes());
```

如果想要使用GPU, `onnxruntime_gpu` 对应版本可以在这里找到
[here](https://onnxruntime.ai/docs/execution-providers/CUDA-ExecutionProvider.html).
```xml
<dependency>
    <groupId>io.github.hzkitty</groupId>
    <artifactId>rapid-table4j</artifactId>
    <version>1.0.0</version>
    <exclusions>
      <exclusion>
        <groupId>com.microsoft.onnxruntime</groupId>
        <artifactId>onnxruntime</artifactId>
      </exclusion>
    </exclusions>
</dependency>

<!-- 1.18.0 support CUDA 12.x -->
<dependency>
    <groupId>com.microsoft.onnxruntime</groupId>
    <artifactId>onnxruntime_gpu</artifactId>
    <version>1.18.0</version>
</dependency>
```

## 鸣谢

- [RapidTable](https://github.com/RapidAI/RapidTable)

## 开源许可
使用 [Apache License 2.0](https://github.com/MyMonsterCat/DeviceTouch/blob/main/LICENSE)
